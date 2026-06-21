package com.fsa_profgroep_4.twee_voor_twaalf_kmp.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Wire models for the online lobby, mirroring the backend's `model` package
 * (~/School/2-voor-12). Only the lobby messages are modelled here; the gameplay
 * messages (`game_started`, scores, …) arrive in a later slice and are simply
 * skipped by [LobbyClient] until then.
 *
 * The `@SerialName` tags must match the backend exactly, since they are the JSON
 * `"type"` discriminator and field names on the wire.
 */

/** Which puzzle a player wants in their round. Backs the "Mini-puzzel" dropdown. */
@Serializable
enum class PuzzlePreference(val label: String) {
    @SerialName("taartpuzzel")
    TAARTPUZZEL("Taartpuzzel"),

    @SerialName("paardensprong")
    PAARDENSPRONG("Paardensprong"),

    @SerialName("random")
    RANDOM("Willekeurig"),
}

/** Whether both players answer the same quiz or each gets their own. */
@Serializable
enum class QuizMode {
    @SerialName("same")
    SAME,

    @SerialName("different")
    DIFFERENT,
}

/**
 * The public identity of a player in a lobby: the [username] they connected with
 * and an optional avatar [avatar] path (resolved to a full URL for display via
 * [BackendUrlProvider.avatarUrl]).
 */
@Serializable
data class PlayerProfile(
    val username: String,
    val avatar: String? = null,
)

/**
 * A message the server pushes over the lobby websocket. Sealed and tagged with a
 * `"type"` field; only the messages relevant to the lobby phase are modelled.
 */
@Serializable
sealed interface LobbyServerMessage {

    /** Sent to the host right after connecting: the code others use to join. */
    @Serializable
    @SerialName("lobby_created")
    data class LobbyCreated(val code: String) : LobbyServerMessage

    /** Sent to a joining player once accepted, carrying the host as [opponent]. */
    @Serializable
    @SerialName("joined_lobby")
    data class JoinedLobby(val code: String, val opponent: PlayerProfile) : LobbyServerMessage

    /** Pushed to the host when the second player joins, carrying them as [opponent]. */
    @Serializable
    @SerialName("player_joined")
    data class PlayerJoined(val opponent: PlayerProfile) : LobbyServerMessage

    /** Sent before the server closes a connection it cannot accept. */
    @Serializable
    @SerialName("error")
    data class LobbyError(val message: String) : LobbyServerMessage
}
