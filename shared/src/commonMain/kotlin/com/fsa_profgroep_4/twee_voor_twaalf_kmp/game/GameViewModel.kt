package com.fsa_profgroep_4.twee_voor_twaalf_kmp.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.network.ClientMessage
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.network.OnlineSession
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.network.Question
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.network.ServerMessage
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.network.SoloRound
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Which part of the game is on screen. */
sealed interface GamePhase {
    data class Answering(val index: Int) : GamePhase
    data object Word : GamePhase
    data object Submitted : GamePhase
}

/**
 * A letter collected for one question. [presentationIndex] is its place in the bank
 * (the order questions were answered); [wordPosition] is the slot in the
 * twaalfletterwoord it belongs to (which differs, since questions are shuffled).
 * [typed] is the first letter of the player's answer (possibly wrong); placing the
 * chip drops the [correct] letter into [wordPosition].
 */
data class CollectedLetter(
    val presentationIndex: Int,
    val wordPosition: Int,
    val typed: Char?,
    val correct: Char,
    val placed: Boolean = false,
)

data class GameUiState(
    val round: SoloRound? = null,
    /**
     * The shuffled question order: `order[presentationIndex] = wordPosition`. Questions
     * are answered in this order so the collected letters don't spell the word.
     */
    val order: List<Int> = emptyList(),
    val phase: GamePhase = GamePhase.Answering(0),
    /** Typed answer per presentation index. */
    val answers: Map<Int, String> = emptyMap(),
    val remainingSeconds: Int = 0,
    // Word phase:
    /** The grid slots, indexed by word position; a slot holds its correct letter once placed. */
    val slots: List<Char?> = emptyList(),
    /** The collected letters in bank (presentation) order. */
    val letters: List<CollectedLetter> = emptyList(),
    /** The word the player types as their answer — independent of the grid. */
    val guess: String = "",
    /** Online only: finished answering, waiting for the opponent before the word phase. */
    val waitingForOpponent: Boolean = false,
    val error: String? = null,
) {
    val currentIndex: Int? get() = (phase as? GamePhase.Answering)?.index
    val questionCount: Int get() = order.size

    val currentQuestion: Question?
        get() {
            val index = currentIndex ?: return null
            val wordPosition = order.getOrNull(index) ?: return null
            return round?.questions?.getOrNull(wordPosition)
        }

    /** Header strip: the first letter collected so far, in presentation order (null = blank). */
    val collectedLetters: List<Char?>
        get() = (0 until questionCount).map { answers[it]?.trim()?.firstOrNull()?.uppercaseChar() }
}

/**
 * Drives gameplay for both modes (see [PendingGame] from [GameSessionHolder]):
 * answering the twelve questions, then arranging collected letters into the
 * twaalfletterwoord. Money/scoring and the results screen are a later slice, so
 * "Inleveren" lands on a [GamePhase.Submitted] placeholder.
 *
 * Solo runs entirely locally. Online shares the lobby's [OnlineSession]: finishing
 * answering sends `finished_answering` and waits for the server's `proceed_to_word`
 * (which arrives once both players are done, or on the server timeout).
 */
class GameViewModel(
    holder: GameSessionHolder,
) : ViewModel() {

    private val _state = MutableStateFlow(GameUiState())
    val state: StateFlow<GameUiState> = _state.asStateFlow()

    private var onlineSession: OnlineSession? = null
    private var timerJob: Job? = null

    init {
        when (val pending = holder.take()) {
            null -> _state.update { it.copy(error = "Geen actief spel gevonden.") }
            is PendingGame.Solo -> startRound(pending.round)
            is PendingGame.Online -> {
                onlineSession = pending.session
                startRound(pending.round)
                listenOnline(pending.session)
            }
        }
    }

    private fun startRound(round: SoloRound) {
        // Shuffle the question order so the collected letters don't spell the word.
        val order = round.questions.indices.shuffled()
        _state.update { it.copy(round = round, order = order, phase = GamePhase.Answering(0)) }
        startCountdown(ANSWER_SECONDS) { finishAnswering() }
    }

    private fun listenOnline(session: OnlineSession) {
        viewModelScope.launch {
            session.incoming.collect { message ->
                when (message) {
                    ServerMessage.ProceedToWord -> enterWordPhase()
                    is ServerMessage.LobbyError -> _state.update { it.copy(error = message.message) }
                    else -> Unit // game_finished etc. is a later slice
                }
            }
        }
    }

    // --- answering ---

    fun onAnswerChange(text: String) {
        val index = _state.value.currentIndex ?: return
        _state.update { it.copy(answers = it.answers + (index to text), error = null) }
    }

    fun next() {
        val index = _state.value.currentIndex ?: return
        if (index < _state.value.questionCount - 1) {
            _state.update { it.copy(phase = GamePhase.Answering(index + 1)) }
        } else {
            finishAnswering()
        }
    }

    fun previous() {
        val index = _state.value.currentIndex ?: return
        if (index > 0) _state.update { it.copy(phase = GamePhase.Answering(index - 1)) }
    }

    fun skip() {
        val index = _state.value.currentIndex ?: return
        _state.update { it.copy(answers = it.answers - index) }
        next()
    }

    private fun finishAnswering() {
        if (_state.value.phase !is GamePhase.Answering) return
        timerJob?.cancel()
        val session = onlineSession
        if (session == null) {
            enterWordPhase() // solo: straight to the word phase
        } else {
            _state.update { it.copy(waitingForOpponent = true) }
            viewModelScope.launch { runCatching { session.send(ClientMessage.FinishedAnswering) } }
        }
    }

    private fun enterWordPhase() {
        if (_state.value.phase !is GamePhase.Answering) return
        val state = _state.value
        val questions = state.round?.questions.orEmpty()
        val letters = state.order.mapIndexed { presentationIndex, wordPosition ->
            CollectedLetter(
                presentationIndex = presentationIndex,
                wordPosition = wordPosition,
                typed = state.answers[presentationIndex]?.trim()?.firstOrNull()?.uppercaseChar(),
                correct = questions.getOrNull(wordPosition)?.correctLetter?.firstOrNull()?.uppercaseChar() ?: ' ',
            )
        }
        _state.update {
            it.copy(
                phase = GamePhase.Word,
                letters = letters,
                slots = List(state.order.size) { null },
                guess = "",
                waitingForOpponent = false,
            )
        }
        startCountdown(WORD_SECONDS) { submit() }
    }

    // --- word phase ---

    /**
     * Tapping the bank chip at [presentationIndex] drops its correct letter into the
     * word slot it belongs to ([CollectedLetter.wordPosition]) — so a wrong answer's
     * chip reveals and places the question's actual letter. Tapping it again removes it.
     */
    fun onLetterClick(presentationIndex: Int) {
        val letter = _state.value.letters.firstOrNull { it.presentationIndex == presentationIndex } ?: return
        val nowPlaced = !letter.placed
        _state.update {
            it.copy(
                letters = it.letters.map { l ->
                    if (l.presentationIndex == presentationIndex) l.copy(placed = nowPlaced) else l
                },
                slots = it.slots.toMutableList().also { slots ->
                    slots[letter.wordPosition] = if (nowPlaced) letter.correct else null
                },
            )
        }
    }

    /** Clears every placed letter from the grid. */
    fun clearGrid() {
        _state.update {
            it.copy(
                slots = List(it.slots.size) { null },
                letters = it.letters.map { l -> l.copy(placed = false) },
            )
        }
    }

    /** The player types their answer; only letters, capped at the word length. */
    fun onGuessChange(text: String) {
        val maxLength = _state.value.slots.size
        val cleaned = text.filter { it.isLetter() }.uppercase().take(maxLength)
        _state.update { it.copy(guess = cleaned) }
    }

    fun clearGuess() = _state.update { it.copy(guess = "") }

    fun submit() {
        timerJob?.cancel()
        _state.update { it.copy(phase = GamePhase.Submitted) }
    }

    // --- timer ---

    private fun startCountdown(seconds: Int, onExpire: () -> Unit) {
        timerJob?.cancel()
        _state.update { it.copy(remainingSeconds = seconds) }
        timerJob = viewModelScope.launch {
            var remaining = seconds
            while (remaining > 0) {
                delay(1000)
                remaining--
                _state.update { it.copy(remainingSeconds = remaining) }
            }
            onExpire()
        }
    }

    /** Closes the online session (if any). Called from the screen's `onDispose`. */
    fun disconnect() {
        timerJob?.cancel()
        val session = onlineSession
        onlineSession = null
        if (session != null) viewModelScope.launch { runCatching { session.close() } }
    }

    override fun onCleared() {
        disconnect()
    }

    private companion object {
        const val ANSWER_SECONDS = 300
        const val WORD_SECONDS = 120
    }
}
