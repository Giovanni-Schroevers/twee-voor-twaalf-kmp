package com.fsa_profgroep_4.twee_voor_twaalf_kmp.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.network.BackendUrlProvider
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.network.UserDto
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AccountUiState(
    val username: String = "",
    val email: String = "",
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val isUploadingAvatar: Boolean = false,
    /** Transient success note shown after a save (e.g. "Opgeslagen"). */
    val feedback: String? = null,
    val error: String? = null,
)

/**
 * Backs the logged-in Account screen: exposes the signed-in [user] for display
 * and an editable copy of the username/email that saves via [AuthRepository].
 * Logout and account deletion are also routed through here.
 *
 * The screen keys off [AuthRepository.currentUser] for the logged-in/out split;
 * this ViewModel only matters once a user is present.
 */
class AccountViewModel(
    private val auth: AuthRepository,
    private val urlProvider: BackendUrlProvider,
) : ViewModel() {

    /** The signed-in user, or null once logged out / account deleted. */
    val user: StateFlow<UserDto?> = auth.currentUser

    /** Full URL for the current user's avatar, or null when there's none. */
    fun avatarUrl(path: String?): String? = urlProvider.avatarUrl(path)

    private val _state = MutableStateFlow(AccountUiState())
    val state: StateFlow<AccountUiState> = _state.asStateFlow()

    init {
        // Seed the editable fields from whoever is currently signed in.
        auth.currentUser.value?.let { existing ->
            _state.update { it.copy(username = existing.username, email = existing.email) }
        }
    }

    fun onUsernameChange(value: String) =
        _state.update { it.copy(username = value, error = null, feedback = null) }

    fun onEmailChange(value: String) =
        _state.update { it.copy(email = value, error = null, feedback = null) }

    fun save() {
        val current = _state.value
        if (current.isSaving) return
        if (current.username.isBlank() || current.email.isBlank()) {
            _state.update { it.copy(error = "Gebruikersnaam en e-mail mogen niet leeg zijn.") }
            return
        }
        if ('@' !in current.email) {
            _state.update { it.copy(error = "Vul een geldig e-mailadres in.") }
            return
        }
        _state.update { it.copy(isSaving = true, error = null, feedback = null) }
        viewModelScope.launch {
            val result = auth.updateProfile(current.username, current.email)
            _state.update {
                it.copy(
                    isSaving = false,
                    error = result.exceptionOrNull()?.message,
                    feedback = if (result.isSuccess) "Opgeslagen" else null,
                )
            }
        }
    }

    /** Reads the picked image and uploads it as the new avatar. */
    fun uploadAvatar(file: PlatformFile) {
        if (_state.value.isUploadingAvatar) return
        _state.update { it.copy(isUploadingAvatar = true, error = null, feedback = null) }
        viewModelScope.launch {
            val bytes = file.readBytes()
            val filename = file.name.ifBlank { "avatar.jpg" }
            val result = auth.uploadAvatar(bytes, filename, mimeFromFileName(filename))
            _state.update {
                it.copy(
                    isUploadingAvatar = false,
                    error = result.exceptionOrNull()?.message,
                    feedback = if (result.isSuccess) "Avatar bijgewerkt" else null,
                )
            }
        }
    }

    fun logout() = auth.logout()

    private fun mimeFromFileName(name: String): String =
        when (name.substringAfterLast('.', "").lowercase()) {
            "png" -> "image/png"
            "webp" -> "image/webp"
            else -> "image/jpeg" // jpg/jpeg and camera captures
        }

    fun deleteAccount() {
        if (_state.value.isDeleting) return
        _state.update { it.copy(isDeleting = true, error = null, feedback = null) }
        viewModelScope.launch {
            val result = auth.deleteAccount()
            // On success currentUser flips to null and the screen navigates away;
            // only a failure needs to be reflected back here.
            if (result.isFailure) {
                _state.update {
                    it.copy(isDeleting = false, error = result.exceptionOrNull()?.message)
                }
            }
        }
    }
}
