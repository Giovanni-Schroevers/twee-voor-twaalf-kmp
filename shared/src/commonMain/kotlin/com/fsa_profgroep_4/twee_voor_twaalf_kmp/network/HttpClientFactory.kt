package com.fsa_profgroep_4.twee_voor_twaalf_kmp.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
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
 */
fun createHttpClient(): HttpClient = HttpClient {
    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
            }
        )
    }
    // Enables client.webSocket { ... } / client.webSocketSession(...). The engine
    // does the actual WS work (okhttp/cio fully; darwin in Ktor 3.x) — see
    // EchoSocket for usage.
    install(WebSockets)
}
