package com.fsa_profgroep_4.twee_voor_twaalf_kmp.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Demonstration WebSocket client.
 *
 * Where the REST example ([KtorExampleApi]) did one request → one response, a
 * WebSocket keeps a connection *open* and streams messages both ways. We model
 * the incoming stream as a [Flow]: collecting [messages] opens the socket, and
 * each server message is emitted as it arrives. When the collector stops (or the
 * server closes), the `webSocket { }` block ends and the connection closes.
 *
 * Uses a public echo server (it replies with whatever you send) purely as a
 * runnable stand-in — swap the URL for your real game/quiz socket later.
 *
 * Call it from a coroutine, e.g. in a ViewModel:
 *   viewModelScope.launch { echoSocket.messages().collect { /* update state */ } }
 */
class EchoSocket(
    private val client: HttpClient,
) {
    fun messages(): Flow<String> = flow {
        client.webSocket(urlString = ECHO_URL) {
            // Send one message; the echo server sends it straight back.
            send(Frame.Text("Hello from Twee voor Twaalf (KMP)!"))

            // Stream incoming frames. `incoming` is a channel we loop over until
            // the connection closes.
            for (frame in incoming) {
                if (frame is Frame.Text) {
                    emit(frame.readText())
                }
            }
        }
    }

    private companion object {
        const val ECHO_URL = "wss://ws.postman-echo.com/raw"
    }
}
