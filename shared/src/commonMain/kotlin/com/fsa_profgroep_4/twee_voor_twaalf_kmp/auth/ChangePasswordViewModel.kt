package com.fsa_profgroep_4.twee_voor_twaalf_kmp.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChangePasswordUiState(
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    /** True after the password was changed; the screen shows a confirmation. */
    val success: Boolean = false,
)

/** Minimum new-password length, matching registration. */
private const val MIN_PASSWORD_LENGTH = 8

/**
 * Holds the change-password form and runs the call via [AuthRepository]. The
 * backend verifies the current password and rejects a wrong one with 400, which
 * surfaces as the error message.
 */
class ChangePasswordViewModel(
    private val auth: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ChangePasswordUiState())
    val state: StateFlow<ChangePasswordUiState> = _state.asStateFlow()

    fun onCurrentPasswordChange(value: String) =
        _state.update { it.copy(currentPassword = value, error = null) }

    fun onNewPasswordChange(value: String) =
        _state.update { it.copy(newPassword = value, error = null) }

    fun onConfirmPasswordChange(value: String) =
        _state.update { it.copy(confirmPassword = value, error = null) }

    fun submit() {
        val current = _state.value
        if (current.isLoading || current.success) return
        val validationError = validate(current)
        if (validationError != null) {
            _state.update { it.copy(error = validationError) }
            return
        }
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val result = auth.changePassword(current.currentPassword, current.newPassword)
            _state.update {
                it.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message,
                    success = result.isSuccess,
                )
            }
        }
    }

    private fun validate(state: ChangePasswordUiState): String? = when {
        state.currentPassword.isBlank() || state.newPassword.isBlank() || state.confirmPassword.isBlank() ->
            "Vul alle velden in."
        state.newPassword.length < MIN_PASSWORD_LENGTH ->
            "Nieuw wachtwoord moet minimaal $MIN_PASSWORD_LENGTH tekens zijn."
        state.newPassword != state.confirmPassword ->
            "De nieuwe wachtwoorden komen niet overeen."
        state.newPassword == state.currentPassword ->
            "Het nieuwe wachtwoord moet anders zijn dan het huidige."
        else -> null
    }
}
