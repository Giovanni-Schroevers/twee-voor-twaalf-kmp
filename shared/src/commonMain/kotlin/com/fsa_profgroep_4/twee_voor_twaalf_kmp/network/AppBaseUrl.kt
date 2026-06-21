package com.fsa_profgroep_4.twee_voor_twaalf_kmp.network

/**
 * The base URL of the backend.
 *
 * Two regimes:
 *  - **Deployed backend**: set the `backend.url` Gradle property (in
 *    gradle.properties, ~/.gradle/gradle.properties, or `-Pbackend.url=...`). It's
 *    baked into [BACKEND_URL] at build time and used as-is on every platform.
 *  - **Local development** (property unset → [BACKEND_URL] is blank): falls back to
 *    [localDevBaseUrl], which picks the right localhost host per platform. This is
 *    why the Android, desktop and iOS run configs all work with no flags.
 *
 * The value must end with a trailing slash — relative request paths (e.g.
 * `api/login`) are resolved against it (see [createHttpClient]).
 */
fun appBaseUrl(): String = BACKEND_URL.ifBlank { localDevBaseUrl() }

/**
 * The localhost base URL for the current platform during development. The Android
 * emulator reaches the host machine via `10.0.2.2`; desktop and the iOS simulator
 * share the host's network, so `localhost` works there.
 */
internal expect fun localDevBaseUrl(): String
