package com.fsa_profgroep_4.twee_voor_twaalf_kmp.network

/**
 * Turns a failed backend call into a user-facing message that surfaces what the
 * **server** actually said. A [BackendException] already carries `HTTP <status>:
 * <body>` (the raw server response); other failures (network/parse) include their
 * type so they stay debuggable. Mirrors the auth flow's error handling so the game
 * shows real server errors instead of a generic "something went wrong".
 */
fun Throwable.backendMessage(fallback: String = "Er ging iets mis."): String = when (this) {
    is BackendException -> message ?: fallback
    else -> "${this::class.simpleName}: ${message ?: fallback}"
}
