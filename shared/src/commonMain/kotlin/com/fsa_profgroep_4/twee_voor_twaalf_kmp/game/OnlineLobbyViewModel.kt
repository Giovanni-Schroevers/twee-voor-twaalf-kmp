package com.fsa_profgroep_4.twee_voor_twaalf_kmp.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.auth.AuthRepository
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.data.GameSettingsRepository
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.network.LobbyClient
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.network.LobbyEvent
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.network.PlayerProfile
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.network.PuzzlePreference
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.network.QuizMode
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
    // Host-only settings, persisted via GameSettingsRepository (sent with
    // start_game in a later slice).
    val puzzle: PuzzlePreference = PuzzlePreference.RANDOM,
    val quizMode: QuizMode = QuizMode.SAME,
    val joinCodeInput: String = "",
    val error: String? = null,
)

/**
 * Backs the online lobby. On open it auto-hosts (one websocket via [LobbyClient]);
 * entering a code and joining cancels that connection and opens a guest one. A
 * generation counter makes role switches clean: events from a superseded
 * connection are ignored, so a stale "closed" can't clobber the new state.
 *
 * Starting the game is a later slice, so the host's [puzzle]/[quizMode] are kept in
 * state but not yet sent.
 */
class OnlineLobbyViewModel(
    private val auth: AuthRepository,
    private val lobbyClient: LobbyClient,
    private val settings: GameSettingsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(OnlineLobbyUiState())
    val state: StateFlow<OnlineLobbyUiState> = _state.asStateFlow()

    private var connectionJob: Job? = null
    private var generation = 0

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

    // Persist the choice; the change flows back into state via the collector above.
    fun onPuzzleChange(value: PuzzlePreference) = settings.setPuzzle(value)

    fun onQuizModeChange(value: QuizMode) = settings.setQuizMode(value)

    fun onJoinCodeChange(value: String) =
        _state.update { it.copy(joinCodeInput = value, error = null) }

    /** (Re)opens the websocket in the given [role]; cancels any current connection. */
    private fun connect(role: LobbyRole, code: String?) {
        val user = auth.currentUser.value ?: return
        connectionJob?.cancel()
        val myGeneration = ++generation
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
            lobbyClient.connect(user.username, user.avatar, code).collect { event ->
                if (myGeneration != generation) return@collect // superseded
                reduce(event, role)
            }
        }
    }

    private fun reduce(event: LobbyEvent, role: LobbyRole) {
        when (event) {
            LobbyEvent.Opened -> _state.update { it.copy(connected = true) }
            is LobbyEvent.Created -> _state.update { it.copy(code = event.code, connected = true) }
            is LobbyEvent.Joined ->
                _state.update { it.copy(code = event.code, opponent = event.opponent, connected = true) }
            is LobbyEvent.OpponentJoined -> _state.update { it.copy(opponent = event.opponent) }
            is LobbyEvent.Failed -> {
                _state.update { it.copy(error = event.message, connected = false) }
                // A bad/full join leaves us with no lobby; fall back to hosting our own.
                if (role == LobbyRole.GUEST) connect(LobbyRole.HOST, code = null)
            }
            LobbyEvent.Closed -> _state.update { it.copy(connected = false) }
        }
    }

    /**
     * Closes the websocket. Called from the screen's `onDispose`, since this app
     * resolves ViewModels via Koin (`koinInject`) rather than a ViewModelStore, so
     * [onCleared] is not guaranteed to run when the user leaves the lobby.
     */
    fun disconnect() {
        generation++ // ignore any in-flight events from the cancelled connection
        connectionJob?.cancel()
        connectionJob = null
    }

    override fun onCleared() {
        disconnect()
    }
}
