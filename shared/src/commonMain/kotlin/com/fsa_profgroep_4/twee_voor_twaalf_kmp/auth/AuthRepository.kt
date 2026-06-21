package com.fsa_profgroep_4.twee_voor_twaalf_kmp.auth

import com.fsa_profgroep_4.twee_voor_twaalf_kmp.network.AuthApi
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.network.AuthTokenStore
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.network.BackendException
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.network.ChangePasswordRequest
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.network.LoginRequest
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.network.RegisterRequest
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.network.UpdateUserRequest
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.network.UserDto
import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.authProvider
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * The app's single source of truth for who is signed in.
 *
 * It owns the [currentUser] state the UI observes and coordinates the two pieces
 * of the auth flow: the [AuthApi] (talks to the backend) and the [AuthTokenStore]
 * (holds the bearer token the HttpClient attaches to authenticated requests).
 *
 * Methods return a [Result] so callers (ViewModels) can show an error message
 * without dealing with exceptions or HTTP details. A failure carries a
 * user-facing Dutch message.
 */
class AuthRepository(
    private val api: AuthApi,
    private val tokenStore: AuthTokenStore,
    private val client: HttpClient,
) {
    private val _currentUser = MutableStateFlow<UserDto?>(null)

    /** The signed-in user, or `null` when logged out. */
    val currentUser: StateFlow<UserDto?> = _currentUser.asStateFlow()

    /** Logs in and, on success, stores the token and the user. */
    suspend fun login(username: String, password: String): Result<Unit> = runCatching {
        val auth = api.login(LoginRequest(username.trim(), password))
            ?: throw AuthException("Onjuiste gebruikersnaam of wachtwoord.")
        applySession(auth.token, auth.user)
    }.toUnit()

    /**
     * Registers an account and immediately logs in with the same credentials —
     * the register endpoint only returns the new id, not a token.
     */
    suspend fun register(
        username: String,
        email: String,
        password: String,
    ): Result<Unit> = runCatching {
        api.register(RegisterRequest(username.trim(), email.trim(), password))
        val auth = api.login(LoginRequest(username.trim(), password))
            ?: throw AuthException("Account aangemaakt, maar automatisch inloggen mislukte.")
        applySession(auth.token, auth.user)
    }.toUnit()

    /** Updates the signed-in user's username/email. */
    suspend fun updateProfile(username: String, email: String): Result<Unit> = runCatching {
        if (_currentUser.value == null) throw AuthException("Je bent niet ingelogd.")
        val updated = api.updateUser(UpdateUserRequest(username.trim(), email.trim()))
        _currentUser.value = updated
    }.toUnit()

    /** Uploads a new avatar image and refreshes the signed-in user. */
    suspend fun uploadAvatar(bytes: ByteArray, filename: String, mimeType: String): Result<Unit> = runCatching {
        if (_currentUser.value == null) throw AuthException("Je bent niet ingelogd.")
        _currentUser.value = api.uploadAvatar(bytes, filename, mimeType)
    }.toUnit()

    /** Changes the signed-in user's password. */
    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> = runCatching {
        if (_currentUser.value == null) throw AuthException("Je bent niet ingelogd.")
        api.changePassword(ChangePasswordRequest(currentPassword, newPassword))
    }.toUnit()

    /** Deletes the account and logs out locally. */
    suspend fun deleteAccount(): Result<Unit> = runCatching {
        api.deleteUser()
        logout()
    }.toUnit()

    /** Clears the token and the signed-in user. */
    fun logout() {
        tokenStore.clear()
        // Ktor's bearer plugin caches the token after first use; clear that cache
        // too, otherwise a stale token would be attached after switching backends.
        client.authProvider<BearerAuthProvider>()?.clearToken()
        _currentUser.value = null
    }

    private fun applySession(token: String, user: UserDto) {
        // No refresh token from this backend — pass null; the HttpClient's bearer
        // provider uses an empty refresh token under the hood.
        tokenStore.update(accessToken = token, refreshToken = null)
        _currentUser.value = user
    }
}

/** Carries a user-facing message for an expected auth failure. */
class AuthException(message: String) : Exception(message)

/**
 * Collapses a [Result] of any type into `Result<Unit>`, turning any failure into
 * an [AuthException] whose message describes what actually went wrong (see
 * [describe]) so the UI can show it instead of a generic message.
 */
private fun <T> Result<T>.toUnit(): Result<Unit> = fold(
    onSuccess = { Result.success(Unit) },
    onFailure = { Result.failure(AuthException(it.describe())) },
)

/** Builds a user-facing message that includes the actual cause. */
private fun Throwable.describe(): String = when (this) {
    // Deliberate, already user-facing messages (e.g. invalid credentials).
    is AuthException -> message ?: "Er ging iets mis."
    // The backend's status + raw response body.
    is BackendException -> message ?: "Serverfout."
    // Network/parse/other failures: include the type so it's debuggable.
    else -> "${this::class.simpleName}: ${message ?: "onbekende fout"}"
}
