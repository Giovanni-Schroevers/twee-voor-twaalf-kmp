package com.fsa_profgroep_4.twee_voor_twaalf_kmp.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.audio.SoundPlayer
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.network.ClientMessage
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.network.GameOutcome
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.network.GameResult
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.network.OnlineSession
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.network.Question
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.network.ServerMessage
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.network.SoloRound
import kotlinx.coroutines.CancellationException
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
    data object Results : GamePhase
}

/** How an online match ended, from this player's perspective. */
enum class MatchResult { WON, LOST, TIE }

/**
 * A letter collected for one question. [presentationIndex] is its place in the bank
 * (the order questions were answered). [typed] is the first letter of the player's
 * answer (possibly wrong); placing the chip drops the [correct] letter into the first
 * free word slot that needs it. [placedAt] is the slot it currently occupies, or null
 * while it sits unplaced in the bank.
 */
data class CollectedLetter(
    val presentationIndex: Int,
    val typed: Char?,
    val correct: Char,
    val placedAt: Int? = null,
) {
    val placed: Boolean get() = placedAt != null
}

data class GameUiState(
    val round: SoloRound? = null,
    /**
     * The question order: `order[presentationIndex] = wordPosition`. Currently the
     * identity mapping (questions are shown in the backend's word order), but kept as
     * an indirection so a shuffle can be reintroduced later without touching the rest.
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
    /** True once the countdown enters its warning window (last 2 min / last 30s). */
    val timerWarning: Boolean = false,
    /** Online only: finished answering, waiting for the opponent before the word phase. */
    val waitingForOpponent: Boolean = false,
    val isOnline: Boolean = false,
    // Results:
    val myScore: Int? = null,
    val guessedWord: Boolean = false,
    /** Online only: submitted our score, waiting for the server's game_finished. */
    val waitingForResult: Boolean = false,
    val opponentName: String? = null,
    val opponentScore: Int? = null,
    val matchResult: MatchResult? = null,
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
 * answering the twelve questions (15-minute countdown), then arranging collected
 * letters into the twaalfletterwoord (2-minute countdown), then the results.
 *
 * Solo runs entirely locally. Online shares the lobby's [OnlineSession]: finishing
 * answering sends `finished_answering` and waits for the server's `proceed_to_word`,
 * and the score is exchanged via `submit_score` / `game_finished`.
 *
 * [sound] plays the countdown cues: a signal entering the last 2 minutes of
 * answering / last 30 seconds of the word round, and a tick each of the final seconds.
 */
class GameViewModel(
    holder: GameSessionHolder,
    private val sound: SoundPlayer,
) : ViewModel() {

    private val _state = MutableStateFlow(GameUiState())
    val state: StateFlow<GameUiState> = _state.asStateFlow()

    private var onlineSession: OnlineSession? = null
    private var isHost: Boolean = false
    private var timerJob: Job? = null

    init {
        when (val pending = holder.take()) {
            null -> _state.update { it.copy(error = "Geen actief spel gevonden.") }
            is PendingGame.Solo -> startRound(pending.round)
            is PendingGame.Online -> {
                onlineSession = pending.session
                isHost = pending.isHost
                _state.update { it.copy(isOnline = true) }
                startRound(pending.round)
                listenOnline(pending.session)
            }
        }
    }

    private fun startRound(round: SoloRound) {
        // Present questions in the backend's order so both players see the same
        // quiz in the same order (the backend does not shuffle the round).
        val order = round.questions.indices.toList()
        _state.update { it.copy(round = round, order = order, phase = GamePhase.Answering(0)) }
        // Answering round: 15 min, with a signal entering the last 2 minutes.
        startCountdown(
            seconds = ANSWER_SECONDS,
            onSecond = { remaining -> if (remaining == ANSWER_WARNING_SECONDS) raiseWarning() },
            onExpire = { finishAnswering() },
        )
    }

    private fun listenOnline(session: OnlineSession) {
        viewModelScope.launch {
            try {
                session.incoming.collect { message ->
                    when (message) {
                        ServerMessage.ProceedToWord -> enterWordPhase()
                        is ServerMessage.GameFinished -> applyResult(message.result)
                        is ServerMessage.LobbyError -> _state.update { it.copy(error = message.message) }
                        else -> Unit
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                _state.update { it.copy(error = "De verbinding met de server is verbroken.") }
            }
        }
    }

    private fun applyResult(result: GameResult) {
        val me = if (isHost) result.host else result.guest
        val opponent = if (isHost) result.guest else result.host
        val match = when (result.outcome) {
            GameOutcome.TIE -> MatchResult.TIE
            GameOutcome.HOST_WON -> if (isHost) MatchResult.WON else MatchResult.LOST
            GameOutcome.GUEST_WON -> if (isHost) MatchResult.LOST else MatchResult.WON
        }
        _state.update {
            it.copy(
                myScore = me.score,
                opponentName = opponent.profile.username,
                opponentScore = opponent.score,
                matchResult = match,
                waitingForResult = false,
            )
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
        // Word round: 2 min, signal at 30s left, then a tick each of the last 10s.
        startCountdown(
            seconds = WORD_SECONDS,
            onSecond = { remaining ->
                if (remaining == WORD_WARNING_SECONDS) raiseWarning()
                if (remaining in 1..TICK_FROM_SECONDS) sound.playTick()
            },
            onExpire = { submit() },
        )
    }

    private fun raiseWarning() {
        sound.playSignal()
        _state.update { it.copy(timerWarning = true) }
    }

    // --- word phase ---

    /**
     * Tapping the bank chip at [presentationIndex] drops its correct letter into the
     * first still-empty word slot that needs that letter (so duplicate letters fill
     * left to right), revealing the question's actual letter even for a wrong answer.
     * Tapping a placed chip again pulls it back out of the slot it occupies.
     */
    fun onLetterClick(presentationIndex: Int) {
        val state = _state.value
        val letter = state.letters.firstOrNull { it.presentationIndex == presentationIndex } ?: return
        val word = state.round?.word ?: return

        val placedAt = letter.placedAt
        if (placedAt != null) {
            // Already placed: pull it back out of its slot.
            _state.update {
                it.copy(
                    letters = it.letters.map { l ->
                        if (l.presentationIndex == presentationIndex) l.copy(placedAt = null) else l
                    },
                    slots = it.slots.toMutableList().also { slots -> slots[placedAt] = null },
                )
            }
            return
        }

        // Not placed: find the first empty slot whose word letter matches this letter.
        val target = state.slots.indices.firstOrNull { i ->
            state.slots[i] == null && word.getOrNull(i)?.uppercaseChar() == letter.correct
        } ?: return // no free slot needs this letter

        _state.update {
            it.copy(
                letters = it.letters.map { l ->
                    if (l.presentationIndex == presentationIndex) l.copy(placedAt = target) else l
                },
                slots = it.slots.toMutableList().also { slots -> slots[target] = letter.correct },
            )
        }
    }

    /** Clears every placed letter from the grid. */
    fun clearGrid() {
        _state.update {
            it.copy(
                slots = List(it.slots.size) { null },
                letters = it.letters.map { l -> l.copy(placedAt = null) },
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

    /**
     * Scores the round and shows the results. Solo shows the score immediately; online
     * sends `submit_score` and waits for the server's `game_finished` (which decides the
     * winner). A wrong twaalfletterwoord means a score of 0 — you lose.
     */
    fun submit() {
        if (_state.value.phase != GamePhase.Word) return
        timerJob?.cancel()
        val state = _state.value
        val round = state.round ?: return
        val guessed = state.guess.equals(round.word, ignoreCase = true)
        val score = computeScore(state, round, guessed)
        val session = onlineSession
        if (session == null) {
            _state.update { it.copy(phase = GamePhase.Results, myScore = score, guessedWord = guessed) }
        } else {
            _state.update {
                it.copy(phase = GamePhase.Results, myScore = score, guessedWord = guessed, waitingForResult = true)
            }
            viewModelScope.launch { runCatching { session.send(ClientMessage.SubmitScore(score)) } }
        }
    }

    /** Counts correct answers and bought letters, then applies [scoreRound]. */
    private fun computeScore(state: GameUiState, round: SoloRound, guessedCorrectly: Boolean): Int {
        val correctCount = state.order.indices.count { presentationIndex ->
            val question = round.questions.getOrNull(state.order[presentationIndex])
            val answer = state.answers[presentationIndex]?.trim().orEmpty()
            question != null && answer.isNotEmpty() &&
                answer.equals(question.correctAnswer.trim(), ignoreCase = true)
        }
        val boughtLetters = state.slots.count { it != null }
        return scoreRound(
            correctCount = correctCount,
            questionCount = state.order.size,
            boughtLetters = boughtLetters,
            guessedWord = guessedCorrectly,
        )
    }

    // --- timer ---

    private fun startCountdown(seconds: Int, onSecond: (Int) -> Unit = {}, onExpire: () -> Unit) {
        timerJob?.cancel()
        _state.update { it.copy(remainingSeconds = seconds, timerWarning = false) }
        timerJob = viewModelScope.launch {
            var remaining = seconds
            while (remaining > 0) {
                delay(1000)
                remaining--
                _state.update { it.copy(remainingSeconds = remaining) }
                onSecond(remaining)
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
        const val ANSWER_SECONDS = 15 * 60   // first round: 15 minutes
        const val ANSWER_WARNING_SECONDS = 2 * 60 // signal when the last 2 minutes start
        const val WORD_SECONDS = 2 * 60      // word round: 2 minutes
        const val WORD_WARNING_SECONDS = 30  // signal when the last 30 seconds start
        const val TICK_FROM_SECONDS = 10     // tick each of the final 10 seconds
    }
}
