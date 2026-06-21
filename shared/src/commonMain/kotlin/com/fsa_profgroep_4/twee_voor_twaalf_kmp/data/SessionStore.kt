package com.fsa_profgroep_4.twee_voor_twaalf_kmp.data

import com.fsa_profgroep_4.twee_voor_twaalf_kmp.network.UserDto
import kotlinx.serialization.json.Json

/** A restored login session: the JWT and the user it belongs to. */
data class StoredSession(val token: String, val user: UserDto)

/**
 * Persists the login session (JWT + user) in Room so the user stays signed in
 * across restarts. [AuthRepository] is the only caller; it restores on startup and
 * writes through on login / profile change / logout.
 *
 * Security note: the token is stored in plain text. That is an accepted trade-off
 * for now — secure storage (Keychain / EncryptedSharedPreferences) is out of scope.
 */
class SessionStore(private val dao: SessionDao) {

    private val json = Json { ignoreUnknownKeys = true }

    /** Loads the saved session, or null if there isn't one (or it can't be parsed). */
    suspend fun load(): StoredSession? {
        val row = dao.get() ?: return null
        val user = runCatching {
            json.decodeFromString(UserDto.serializer(), row.userJson)
        }.getOrNull() ?: return null
        return StoredSession(row.token, user)
    }

    /** Saves (or replaces) the session for [token] / [user]. */
    suspend fun save(token: String, user: UserDto) {
        dao.upsert(
            SessionEntity(
                token = token,
                userJson = json.encodeToString(UserDto.serializer(), user),
            ),
        )
    }

    /** Updates just the stored user (keeping the existing token); no-op if logged out. */
    suspend fun updateUser(user: UserDto) {
        val token = dao.get()?.token ?: return
        save(token, user)
    }

    /** Clears the saved session (logout). */
    suspend fun clear() = dao.clear()
}
