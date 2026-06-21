package com.fsa_profgroep_4.twee_voor_twaalf_kmp.data

import com.fsa_profgroep_4.twee_voor_twaalf_kmp.network.PuzzlePreference
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.network.QuizMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** The locally persisted game settings, with sensible defaults. */
data class GameSettings(
    val puzzle: PuzzlePreference = PuzzlePreference.RANDOM,
    val quizMode: QuizMode = QuizMode.SAME,
)

/**
 * Single source of truth for the player's saved game settings, backed by Room so
 * they survive restarts. The puzzle preference is shared by both the offline and
 * online setup screens (set once, used everywhere); the quiz mode is online-only.
 *
 * [settings] is held in memory and updated synchronously by the setters, so the UI
 * reflects a change immediately. Each change is then written through to the
 * database under a [Mutex] — serialising writes and always persisting the latest
 * snapshot, so two quick edits can't clobber each other.
 */
class GameSettingsRepository(private val dao: SettingsDao) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val writeMutex = Mutex()

    private val _settings = MutableStateFlow(GameSettings())
    val settings: StateFlow<GameSettings> = _settings.asStateFlow()

    /** Guards the initial load from overwriting a change the user just made. */
    private var userTouched = false

    init {
        scope.launch {
            val stored = dao.get()?.toGameSettings()
            if (stored != null && !userTouched) _settings.value = stored
        }
    }

    fun setPuzzle(value: PuzzlePreference) = change { it.copy(puzzle = value) }

    fun setQuizMode(value: QuizMode) = change { it.copy(quizMode = value) }

    private fun change(transform: (GameSettings) -> GameSettings) {
        userTouched = true
        _settings.update(transform)
        scope.launch {
            // Reads the latest snapshot inside the lock, so whichever write runs
            // last persists the final state.
            writeMutex.withLock { dao.upsert(_settings.value.toEntity()) }
        }
    }
}

private fun GameSettings.toEntity(): SettingsEntity =
    SettingsEntity(puzzle = puzzle.name, quizMode = quizMode.name)

/** Maps a stored row to [GameSettings], falling back to defaults for unknown values. */
private fun SettingsEntity.toGameSettings(): GameSettings = GameSettings(
    puzzle = enumValueOrNull<PuzzlePreference>(puzzle) ?: PuzzlePreference.RANDOM,
    quizMode = enumValueOrNull<QuizMode>(quizMode) ?: QuizMode.SAME,
)

private inline fun <reified T : Enum<T>> enumValueOrNull(name: String): T? =
    enumValues<T>().firstOrNull { it.name == name }
