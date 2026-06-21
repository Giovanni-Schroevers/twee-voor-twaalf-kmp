package com.fsa_profgroep_4.twee_voor_twaalf_kmp.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Wire models for the online lobby + gameplay, mirroring the backend's `model`
 * package (~/School/2-voor-12). The `submit_score` / `game_finished` messages are
 * the next slice and are intentionally absent — [OnlineSession] drops any inbound
 * frame it can't decode, so they don't break the stream.
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
 * A message the server pushes over the websocket (lobby + gameplay). Sealed and
 * tagged with a `"type"` field.
 */
@Serializable
sealed interface ServerMessage {

    /** Sent to the host right after connecting: the code others use to join. */
    @Serializable
    @SerialName("lobby_created")
    data class LobbyCreated(val code: String) : ServerMessage

    /** Sent to a joining player once accepted, carrying the host as [opponent]. */
    @Serializable
    @SerialName("joined_lobby")
    data class JoinedLobby(val code: String, val opponent: PlayerProfile) : ServerMessage

    /** Pushed to the host when the second player joins, carrying them as [opponent]. */
    @Serializable
    @SerialName("player_joined")
    data class PlayerJoined(val opponent: PlayerProfile) : ServerMessage

    /** Pushed to both players when the game starts, carrying the round to play. */
    @Serializable
    @SerialName("game_started")
    data class GameStarted(val round: SoloRound) : ServerMessage

    /** Pushed to both players at the same moment to advance to the word phase. */
    @Serializable
    @SerialName("proceed_to_word")
    data object ProceedToWord : ServerMessage

    /** Pushed to both players once both have submitted a score, carrying the result. */
    @Serializable
    @SerialName("game_finished")
    data class GameFinished(val result: GameResult) : ServerMessage

    /** Sent before the server closes a connection it cannot accept. */
    @Serializable
    @SerialName("error")
    data class LobbyError(val message: String) : ServerMessage
}

/**
 * A message the client sends over the websocket. Sealed and tagged with `"type"`.
 * (`submit_score` is the next slice.)
 */
@Serializable
sealed interface ClientMessage {

    /** Sent by the host to begin the match. */
    @Serializable
    @SerialName("start_game")
    data class StartGame(val quizMode: QuizMode, val puzzle: PuzzlePreference) : ClientMessage

    /** Sent by a player once they have answered all twelve questions. */
    @Serializable
    @SerialName("finished_answering")
    data object FinishedAnswering : ClientMessage

    /** Sent by a player with their final score; once both are in, the game finishes. */
    @Serializable
    @SerialName("submit_score")
    data class SubmitScore(val score: Int) : ClientMessage
}
