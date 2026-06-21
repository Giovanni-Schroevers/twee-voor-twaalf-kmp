package com.fsa_profgroep_4.twee_voor_twaalf_kmp.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegisterUiState(
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    /** Flips to true once registration (and auto-login) succeeds. */
    val success: Boolean = false,
)

/** Minimum password length, matching the "Minimaal 8 tekens" helper in the design. */
private const val MIN_PASSWORD_LENGTH = 8

/**
 * Holds the registration form state. On submit it registers and auto-logs-in via
 * [AuthRepository] (the backend's register endpoint returns no token).
 */
class RegisterViewModel(
    private val auth: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterUiState())
    val state: StateFlow<RegisterUiState> = _state.asStateFlow()

    fun onUsernameChange(value: String) = _state.update { it.copy(username = value, error = null) }
    fun onEmailChange(value: String) = _state.update { it.copy(email = value, error = null) }
    fun onPasswordChange(value: String) = _state.update { it.copy(password = value, error = null) }

    fun submit() {
        val current = _state.value
        if (current.isLoading) return
        val validationError = validate(current)
        if (validationError != null) {
            _state.update { it.copy(error = validationError) }
            return
        }
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val result = auth.register(current.username, current.email, current.password)
            _state.update {
                it.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message,
                    success = result.isSuccess,
                )
            }
        }
    }

    private fun validate(state: RegisterUiState): String? = when {
        state.username.isBlank() || state.email.isBlank() || state.password.isBlank() ->
            "Vul alle velden in."
        '@' !in state.email ->
            "Vul een geldig e-mailadres in."
        state.password.length < MIN_PASSWORD_LENGTH ->
            "Wachtwoord moet minimaal $MIN_PASSWORD_LENGTH tekens zijn."
        else -> null
    }
}
