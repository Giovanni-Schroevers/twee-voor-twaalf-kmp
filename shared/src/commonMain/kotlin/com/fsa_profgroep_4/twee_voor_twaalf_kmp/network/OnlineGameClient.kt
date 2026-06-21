package com.fsa_profgroep_4.twee_voor_twaalf_kmp.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.url
import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json

/**
 * A live, bidirectional connection to the backend's `/ws/lobby` socket. Unlike a
 * one-shot request, this single socket carries the whole online game: lobby
 * (host/join), then `start_game` → `game_started` → `finished_answering` →
 * `proceed_to_word`. Both the lobby and the game read [incoming] and call [send]
 * on the same instance, so it is created in the lobby and handed to the game.
 */
class OnlineSession(private val session: DefaultClientWebSocketSession) {

    private val json = Json {
        ignoreUnknownKeys = true
        classDiscriminator = "type"
    }

    /**
     * Server messages as they arrive. A cold flow over the socket's frame channel:
     * collecting it consumes frames, so it is collected by one place at a time
     * (the lobby, then the game). Frames that don't decode to a known [ServerMessage]
     * (e.g. the not-yet-modelled `game_finished`) are dropped.
     */
    val incoming: Flow<ServerMessage> = flow {
        for (frame in session.incoming) {
            if (frame !is Frame.Text) continue
            val message = runCatching {
                json.decodeFromString(ServerMessage.serializer(), frame.readText())
            }.getOrNull() ?: continue
            emit(message)
        }
    }

    suspend fun send(message: ClientMessage) {
        session.send(Frame.Text(json.encodeToString(ClientMessage.serializer(), message)))
    }

    suspend fun close() {
        session.close()
    }
}

/**
 * Opens [OnlineSession]s against `/ws/lobby`. The role (host vs guest) is chosen by
 * whether a [code] is supplied — exactly as the backend's query-parameter contract
 * expects.
 */
class OnlineGameClient(
    private val client: HttpClient,
    private val urlProvider: BackendUrlProvider,
) {
    /** Connects as host (when [code] is null) or as guest of [code]. */
    suspend fun connect(username: String, avatar: String?, code: String?): OnlineSession =
        OnlineSession(client.webSocketSession { url(lobbyUrl(username, avatar, code)) })

    /** Builds the `ws(s)://…/ws/lobby?username=…[&code=…][&avatar=…]` URL. */
    private fun lobbyUrl(username: String, avatar: String?, code: String?): String =
        URLBuilder(urlProvider.current).apply {
            protocol = if (protocol == URLProtocol.HTTPS) URLProtocol.WSS else URLProtocol.WS
            pathSegments = listOf("ws", "lobby")
            parameters.append("username", username)
            code?.let { parameters.append("code", it) }
            avatar?.takeIf { it.isNotBlank() }?.let { parameters.append("avatar", it) }
        }.buildString()
}
