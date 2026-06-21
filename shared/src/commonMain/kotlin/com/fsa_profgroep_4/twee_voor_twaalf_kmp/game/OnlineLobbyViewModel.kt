package com.fsa_profgroep_4.twee_voor_twaalf_kmp.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.auth.AuthRepository
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.data.GameSettingsRepository
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.network.ClientMessage
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.network.OnlineGameClient
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.network.OnlineSession
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.network.PlayerProfile
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.network.PuzzlePreference
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.network.QuizMode
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.network.ServerMessage
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.network.backendMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Whether this device is hosting the lobby or has joined someone else's. */
enum class LobbyRole { HOST, GUEST }

data class OnlineLobbyUiState(
    /** True when no one is signed in — online play needs a username server-side. */
    val loggedOut: Boolean = false,
    val role: LobbyRole = LobbyRole.HOST,
    /** Our own host code (HOST) or the code we joined (GUEST); null until known. */
    val code: String? = null,
    val youName: String = "",
    val youAvatar: String? = null,
    val opponent: PlayerProfile? = null,
    /** Whether the websocket is currently open. */
    val connected: Boolean = false,
    // Host-only settings, persisted via GameSettingsRepository.
    val puzzle: PuzzlePreference = PuzzlePreference.RANDOM,
    val quizMode: QuizMode = QuizMode.SAME,
    val joinCodeInput: String = "",
    val error: String? = null,
    /** Flips true once the game has started; the screen navigates to the game. */
    val navigateToGame: Boolean = false,
)

/**
 * Backs the online lobby. On open it auto-hosts (one websocket via [OnlineGameClient]);
 * entering a code and joining closes that connection and opens a guest one. A
 * generation counter makes role switches clean: events from a superseded connection
 * are ignored.
 *
 * The host's "Start spel" sends `start_game`; the resulting `game_started` (received
 * by both players) hands the live [OnlineSession] + round to [GameSessionHolder] and
 * signals navigation — the same socket then carries the game.
 */
class OnlineLobbyViewModel(
    private val auth: AuthRepository,
    private val client: OnlineGameClient,
    private val settings: GameSettingsRepository,
    private val holder: GameSessionHolder,
) : ViewModel() {

    private val _state = MutableStateFlow(OnlineLobbyUiState())
    val state: StateFlow<OnlineLobbyUiState> = _state.asStateFlow()

    private var connectionJob: Job? = null
    private var session: OnlineSession? = null
    private var generation = 0

    /** Set once the session has been handed to the game, so we don't close it. */
    private var handedOff = false

    init {
        // Keep the puzzle/quiz-mode in state mirrored to the persisted settings.
        viewModelScope.launch {
            settings.settings.collect { saved ->
                _state.update { it.copy(puzzle = saved.puzzle, quizMode = saved.quizMode) }
            }
        }

        val user = auth.currentUser.value
        if (user == null) {
            _state.update { it.copy(loggedOut = true) }
        } else {
            _state.update { it.copy(youName = user.username, youAvatar = user.avatar) }
            connect(LobbyRole.HOST, code = null)
        }
    }

    /** Switches this device to a guest of the code typed in the join field. */
    fun join() {
        val code = _state.value.joinCodeInput.trim().uppercase()
        if (code.isBlank()) return
        connect(LobbyRole.GUEST, code = code)
    }

    /** Host action: ask the server to start the match. */
    fun start() {
        val current = session ?: return
        val ui = _state.value
        if (ui.role != LobbyRole.HOST) return
        if (ui.opponent == null) {
            _state.update { it.copy(error = "Wacht tot een speler meedoet.") }
            return
        }
        viewModelScope.launch {
            runCatching { current.send(ClientMessage.StartGame(ui.quizMode, ui.puzzle)) }
                .onFailure { failure ->
                    _state.update { it.copy(error = failure.backendMessage("Kon het spel niet starten.")) }
                }
        }
    }

    fun onPuzzleChange(value: PuzzlePreference) = settings.setPuzzle(value)

    fun onQuizModeChange(value: QuizMode) = settings.setQuizMode(value)

    fun onJoinCodeChange(value: String) =
        _state.update { it.copy(joinCodeInput = value, error = null) }

    /** (Re)opens the websocket in the given [role]; closes any current connection. */
    private fun connect(role: LobbyRole, code: String?) {
        val user = auth.currentUser.value ?: return
        val generationAtStart = ++generation
        val previous = session
        connectionJob?.cancel()
        session = null
        _state.update {
            it.copy(
                role = role,
                connected = false,
                opponent = null,
                error = null,
                code = if (role == LobbyRole.HOST) null else code,
            )
        }
        connectionJob = viewModelScope.launch {
            previous?.let { runCatching { it.close() } }
            val opened = runCatching { client.connect(user.username, user.avatar, code) }.getOrElse { failure ->
                if (generationAtStart == generation) {
                    _state.update {
                        it.copy(error = failure.backendMessage("Kon geen verbinding maken met de server."))
                    }
                }
                return@launch
            }
            if (generationAtStart != generation) {
                runCatching { opened.close() }
                return@launch
            }
            session = opened
            _state.update { it.copy(connected = true) }
            opened.incoming.collect { message ->
                if (generationAtStart != generation) return@collect
                reduce(message, role, opened)
            }
            if (generationAtStart == generation) _state.update { it.copy(connected = false) }
        }
    }

    private fun reduce(message: ServerMessage, role: LobbyRole, current: OnlineSession) {
        when (message) {
            is ServerMessage.LobbyCreated -> _state.update { it.copy(code = message.code) }
            is ServerMessage.JoinedLobby ->
                _state.update { it.copy(code = message.code, opponent = message.opponent) }
            is ServerMessage.PlayerJoined -> _state.update { it.copy(opponent = message.opponent) }
            is ServerMessage.GameStarted -> handOff(current, message)
            ServerMessage.ProceedToWord -> Unit // not relevant in the lobby
            is ServerMessage.LobbyError -> {
                _state.update { it.copy(error = message.message) }
                // A bad/full join leaves us with no lobby; fall back to hosting our own.
                if (role == LobbyRole.GUEST) connect(LobbyRole.HOST, code = null)
            }
        }
    }

    /** Hands the live session + round to the game and stops driving it from the lobby. */
    private fun handOff(current: OnlineSession, started: ServerMessage.GameStarted) {
        holder.set(PendingGame.Online(started.round, current))
        handedOff = true
        generation++ // ignore any further events here
        connectionJob?.cancel()
        connectionJob = null
        session = null
        _state.update { it.copy(navigateToGame = true) }
    }

    /**
     * Closes the websocket unless it was handed to the game. Called from the screen's
     * `onDispose`, since this app resolves ViewModels via Koin (`koinInject`) rather
     * than a ViewModelStore, so [onCleared] is not guaranteed to run.
     */
    fun disconnect() {
        generation++
        connectionJob?.cancel()
        connectionJob = null
        if (!handedOff) {
            val open = session
            session = null
            if (open != null) viewModelScope.launch { runCatching { open.close() } }
        }
    }

    override fun onCleared() {
        disconnect()
    }
}
