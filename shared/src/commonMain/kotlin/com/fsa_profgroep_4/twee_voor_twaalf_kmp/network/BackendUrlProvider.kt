package com.fsa_profgroep_4.twee_voor_twaalf_kmp.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Holds the backend base URL the app currently talks to, and lets it be changed
 * at runtime (see the Settings screen).
 *
 * This is what makes a live demo against a **Cloudflare tunnel** possible: the
 * tunnel URL changes each session, so it can't be a compile-time value. The
 * shared [HttpClient][io.ktor.client.HttpClient] reads [current] inside its
 * `DefaultRequest` block, which runs per request — so a change here takes effect
 * on the very next call without rebuilding the client.
 *
 * In-memory only (like [AuthTokenStore]): the value resets to [defaultBaseUrl] on
 * app restart. [defaultBaseUrl] is the build-time default from [appBaseUrl].
 */
class BackendUrlProvider {
    /** The build-time default (Gradle `backend.url`, or the per-platform localhost). */
    val defaultBaseUrl: String = normalize(appBaseUrl())

    private val _baseUrl = MutableStateFlow(defaultBaseUrl)

    /** Observable current base URL (always ends with a slash). */
    val baseUrl: StateFlow<String> = _baseUrl.asStateFlow()

    /** Snapshot read used by the HttpClient per request. */
    val current: String get() = _baseUrl.value

    /** Points the app at [url] (trimmed; a trailing slash is added if missing). */
    fun update(url: String) {
        _baseUrl.value = normalize(url)
    }

    /** Restores the build-time default. */
    fun resetToDefault() {
        _baseUrl.value = defaultBaseUrl
    }

    /**
     * Turns a stored avatar path (e.g. `avatars/<uuid>.png`) into a full URL
     * against the current backend, for loading with Coil. Null/blank in → null.
     */
    fun avatarUrl(path: String?): String? =
        path?.takeIf { it.isNotBlank() }
            ?.let { current.trimEnd('/') + "/" + it.trimStart('/') }

    private companion object {
        fun normalize(raw: String): String {
            val trimmed = raw.trim()
            return if (trimmed.endsWith("/")) trimmed else "$trimmed/"
        }
    }
}
