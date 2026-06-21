package com.fsa_profgroep_4.twee_voor_twaalf_kmp.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    /** Flips to true once login succeeds, so the screen can navigate away. */
    val success: Boolean = false,
)

/**
 * Holds the login form state and runs the login call. Validation mirrors the
 * wireframe: both fields are required; the backend decides whether the
 * credentials are valid.
 */
class LoginViewModel(
    private val auth: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun onUsernameChange(value: String) = _state.update { it.copy(username = value, error = null) }
    fun onPasswordChange(value: String) = _state.update { it.copy(password = value, error = null) }

    fun submit() {
        val current = _state.value
        if (current.isLoading) return
        if (current.username.isBlank() || current.password.isBlank()) {
            _state.update { it.copy(error = "Vul je gebruikersnaam en wachtwoord in.") }
            return
        }
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val result = auth.login(current.username, current.password)
            _state.update {
                it.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message,
                    success = result.isSuccess,
                )
            }
        }
    }
}
