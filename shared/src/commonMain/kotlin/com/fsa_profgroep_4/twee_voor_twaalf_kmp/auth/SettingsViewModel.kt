package com.fsa_profgroep_4.twee_voor_twaalf_kmp.auth

import androidx.lifecycle.ViewModel
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.network.BackendUrlProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SettingsUiState(
    val url: String = "",
    val feedback: String? = null,
    val error: String? = null,
)

/**
 * Backs the Settings screen, where the user points the app at a different backend
 * (e.g. a Cloudflare tunnel during a demo).
 *
 * Saving updates the [BackendUrlProvider] — picked up by the HttpClient on the
 * next request — and logs out, since any existing token belongs to the previous
 * backend.
 */
class SettingsViewModel(
    private val urlProvider: BackendUrlProvider,
    private val auth: AuthRepository,
) : ViewModel() {

    /** The build-time default, shown on the reset action. */
    val defaultUrl: String = urlProvider.defaultBaseUrl

    private val _state = MutableStateFlow(SettingsUiState(url = urlProvider.current))
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    fun onUrlChange(value: String) =
        _state.update { it.copy(url = value, error = null, feedback = null) }

    fun save() {
        val raw = _state.value.url.trim()
        if (!raw.startsWith("http://") && !raw.startsWith("https://")) {
            _state.update { it.copy(error = "URL moet met http:// of https:// beginnen.") }
            return
        }
        urlProvider.update(raw)
        auth.logout()
        _state.update {
            it.copy(
                url = urlProvider.current,
                feedback = "Backend ingesteld. Je bent uitgelogd.",
                error = null,
            )
        }
    }

    fun resetToDefault() {
        urlProvider.resetToDefault()
        auth.logout()
        _state.update {
            it.copy(url = urlProvider.current, feedback = "Standaard hersteld.", error = null)
        }
    }
}
