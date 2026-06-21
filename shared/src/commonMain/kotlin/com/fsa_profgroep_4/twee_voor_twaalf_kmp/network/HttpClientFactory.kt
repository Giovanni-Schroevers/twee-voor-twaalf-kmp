package com.fsa_profgroep_4.twee_voor_twaalf_kmp.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.request.url
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Builds the app's single, shared [HttpClient].
 *
 * Note there's no engine argument: Ktor picks the engine from whatever is on the
 * classpath for each target (okhttp on Android, darwin on iOS, cio on desktop —
 * wired in `build.gradle.kts`). That's why all of this lives in commonMain.
 *
 * `ContentNegotiation` + `json(...)` teaches Ktor to (de)serialize request and
 * response bodies as JSON using kotlinx.serialization. `ignoreUnknownKeys` makes
 * parsing tolerant of extra fields the API returns that we don't model.
 *
 * The [tokenStore] feeds the bearer-auth flow: after login writes tokens to it,
 * every request automatically carries `Authorization: Bearer <token>`.
 */
fun createHttpClient(
    tokenStore: AuthTokenStore,
    urlProvider: BackendUrlProvider,
): HttpClient = HttpClient {
    // Every request without an absolute URL is resolved against the backend's
    // base URL, so APIs can use short relative paths like `api/login`. The block
    // runs per request and reads `urlProvider.current`, so changing the URL at
    // runtime (Settings screen) takes effect immediately — no client rebuild.
    install(DefaultRequest) {
        url(urlProvider.current)
    }
    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
            }
        )
    }
    // Enables timeouts and, crucially, per-request overrides (see GameApi). We set
    // no global request cap on purpose: that would close the long-lived game
    // websocket. Only connection setup is bounded here.
    install(HttpTimeout) {
        connectTimeoutMillis = 15_000
    }
    // Enables client.webSocket { ... } / client.webSocketSession(...). The engine
    // does the actual WS work (okhttp/cio fully; darwin in Ktor 3.x) — see
    // EchoSocket for usage.
    install(WebSockets)

    // Bearer-token auth. `loadTokens` supplies the Authorization header for each
    // request. There's no refresh flow (no refresh endpoint) — when the access
    // token is rejected, the user simply logs in again.
    install(Auth) {
        bearer {
            loadTokens {
                tokenStore.accessToken?.let { access ->
                    BearerTokens(accessToken = access, refreshToken = tokenStore.refreshToken ?: "")
                }
            }
        }
    }
}
