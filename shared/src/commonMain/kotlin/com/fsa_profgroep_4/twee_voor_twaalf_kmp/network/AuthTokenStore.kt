package com.fsa_profgroep_4.twee_voor_twaalf_kmp.network

/**
 * Holds the current auth tokens for the bearer-auth flow.
 *
 * **In-memory only for now** — tokens are lost on app restart and are not
 * encrypted or persisted. That's deliberate: secure/persistent storage was
 * intentionally left out of scope. When you add real storage later (Keychain /
 * EncryptedSharedPreferences / DataStore), keep this same API and back it with
 * that store — nothing else in the auth wiring needs to change.
 *
 * Your login/register flow calls [update] after a successful auth response;
 * [HttpClient][io.ktor.client.HttpClient]'s Auth plugin reads these back to
 * attach the `Authorization: Bearer ...` header. [clear] is your logout.
 */
class AuthTokenStore {
    var accessToken: String? = null
        private set
    var refreshToken: String? = null
        private set

    fun update(accessToken: String?, refreshToken: String?) {
        this.accessToken = accessToken
        this.refreshToken = refreshToken
    }

    fun clear() = update(null, null)
}
