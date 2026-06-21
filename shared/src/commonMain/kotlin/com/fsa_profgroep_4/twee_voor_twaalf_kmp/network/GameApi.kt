package com.fsa_profgroep_4.twee_voor_twaalf_kmp.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.timeout
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess

/**
 * The backend's solo-game endpoint. Solo play is client-authoritative: the round
 * comes with its answers and the client runs the timer and scoring. Depending on
 * the interface (not the Ktor class) keeps callers testable — same pattern as
 * [AuthApi].
 */
interface GameApi {
    /** Fetches a fresh solo round for the given puzzle preference. */
    suspend fun soloRound(puzzle: PuzzlePreference): SoloRound
}

/** Ktor-backed [GameApi], reusing the shared [HttpClient] (base URL + JSON). */
class KtorGameApi(
    private val client: HttpClient,
) : GameApi {

    override suspend fun soloRound(puzzle: PuzzlePreference): SoloRound {
        val response = client.post("api/game/solo") {
            // Round generation is LLM-backed and retries up to 5×, so it can take
            // far longer than the default socket read timeout — give it room.
            timeout {
                requestTimeoutMillis = 60_000
                socketTimeoutMillis = 60_000
            }
            contentType(ContentType.Application.Json)
            setBody(SoloRoundRequest(puzzle))
        }
        if (!response.status.isSuccess()) {
            val body = runCatching { response.bodyAsText() }.getOrDefault("")
            throw BackendException(response.status.value, body)
        }
        return response.body()
    }
}
