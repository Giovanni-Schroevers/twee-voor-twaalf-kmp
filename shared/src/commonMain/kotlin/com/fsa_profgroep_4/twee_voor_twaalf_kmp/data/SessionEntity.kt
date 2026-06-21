package com.fsa_profgroep_4.twee_voor_twaalf_kmp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * The single persisted login session: the JWT plus the signed-in user (stored as
 * JSON so it stays resilient to changes in `UserDto`). Like the settings row there
 * is only ever one, keyed by the fixed [SINGLETON_ID].
 *
 * Note: the token is stored in plain text. That's a deliberate, accepted trade-off
 * for now (no Keychain / EncryptedSharedPreferences) — see [SessionStore].
 */
@Entity(tableName = "session")
data class SessionEntity(
    @PrimaryKey val id: Int = SINGLETON_ID,
    val token: String,
    val userJson: String,
) {
    companion object {
        const val SINGLETON_ID = 0
    }
}
