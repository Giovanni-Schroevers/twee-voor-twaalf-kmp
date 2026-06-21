package com.fsa_profgroep_4.twee_voor_twaalf_kmp.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json

/**
 * An event from the lobby websocket, in the client's own vocabulary (the raw wire
 * messages live in [LobbyServerMessage]). [OnlineLobbyViewModel] folds these into
 * its UI state.
 */
sealed interface LobbyEvent {
    /** The socket opened; we're connected but nothing has happened yet. */
    data object Opened : LobbyEvent

    /** We are hosting; [code] is the join code to share. */
    data class Created(val code: String) : LobbyEvent

    /** We joined someone's lobby; [opponent] is the host. */
    data class Joined(val code: String, val opponent: PlayerProfile) : LobbyEvent

    /** A second player joined the lobby we host; [opponent] is that player. */
    data class OpponentJoined(val opponent: PlayerProfile) : LobbyEvent

    /** The server rejected us or reported a problem. */
    data class Failed(val message: String) : LobbyEvent

    /** The connection closed (server side or because collection stopped). */
    data object Closed : LobbyEvent
}

/**
 * Talks to the backend's `/ws/lobby` endpoint. Like [EchoSocket], the connection is
 * modelled as a cold [Flow]: collecting [connect] opens the socket, and cancelling
 * the collection closes it. The role (host vs guest) is chosen by whether a [code]
 * is supplied — exactly as the backend's query-parameter contract expects.
 */
class LobbyClient(
    private val client: HttpClient,
    private val urlProvider: BackendUrlProvider,
) {
    private val json = Json {
        ignoreUnknownKeys = true
        classDiscriminator = "type"
    }

    /**
     * Connects as host (when [code] is null) or as guest of [code]. Emits
     * [LobbyEvent.Opened] once connected, then one event per server message, and
     * [LobbyEvent.Closed] when the socket ends. Frames that don't parse as a known
     * lobby message (e.g. future gameplay messages) are ignored.
     */
    fun connect(username: String, avatar: String?, code: String?): Flow<LobbyEvent> = flow {
        client.webSocket(urlString = lobbyUrl(username, avatar, code)) {
            emit(LobbyEvent.Opened)
            for (frame in incoming) {
                if (frame !is Frame.Text) continue
                val event = parse(frame.readText()) ?: continue
                emit(event)
            }
        }
        emit(LobbyEvent.Closed)
    }

    /** Decodes one text frame into a [LobbyEvent], or null if it isn't one we model. */
    private fun parse(text: String): LobbyEvent? = try {
        when (val message = json.decodeFromString<LobbyServerMessage>(text)) {
            is LobbyServerMessage.LobbyCreated -> LobbyEvent.Created(message.code)
            is LobbyServerMessage.JoinedLobby -> LobbyEvent.Joined(message.code, message.opponent)
            is LobbyServerMessage.PlayerJoined -> LobbyEvent.OpponentJoined(message.opponent)
            is LobbyServerMessage.LobbyError -> LobbyEvent.Failed(message.message)
        }
    } catch (e: Exception) {
        null // unknown/future message type — ignore for the lobby phase
    }

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
