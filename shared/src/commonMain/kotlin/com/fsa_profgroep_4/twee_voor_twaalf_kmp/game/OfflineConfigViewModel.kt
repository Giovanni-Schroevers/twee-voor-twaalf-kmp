package com.fsa_profgroep_4.twee_voor_twaalf_kmp.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.data.GameSettingsRepository
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.network.GameApi
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.network.PuzzlePreference
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.network.backendMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OfflineConfigUiState(
    val puzzle: PuzzlePreference = PuzzlePreference.RANDOM,
    val loading: Boolean = false,
    val error: String? = null,
    /** Flips true once a round is fetched and staged; the screen navigates to the game. */
    val ready: Boolean = false,
)

/**
 * Backs the offline setup screen. The puzzle choice mirrors the persisted
 * [GameSettingsRepository]; "Start spel" fetches a solo round, stages it in the
 * [GameSessionHolder] and signals navigation to the game screen.
 */
class OfflineConfigViewModel(
    private val gameApi: GameApi,
    private val settings: GameSettingsRepository,
    private val holder: GameSessionHolder,
) : ViewModel() {

    private val _state = MutableStateFlow(OfflineConfigUiState())
    val state: StateFlow<OfflineConfigUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            settings.settings.collect { saved -> _state.update { it.copy(puzzle = saved.puzzle) } }
        }
    }

    fun onPuzzleChange(value: PuzzlePreference) = settings.setPuzzle(value)

    fun start() {
        if (_state.value.loading) return
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            runCatching { gameApi.soloRound(_state.value.puzzle) }
                .onSuccess { round ->
                    holder.set(PendingGame.Solo(round))
                    _state.update { it.copy(loading = false, ready = true) }
                }
                .onFailure { failure ->
                    val message = failure.backendMessage("Kon het spel niet starten. Staat de server aan?")
                    _state.update { it.copy(loading = false, error = message) }
                }
        }
    }

    /** Acknowledge the navigation so returning to this screen doesn't re-trigger it. */
    fun consumeReady() = _state.update { it.copy(ready = false) }
}
