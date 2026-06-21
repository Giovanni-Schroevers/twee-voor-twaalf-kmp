package com.fsa_profgroep_4.twee_voor_twaalf_kmp.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Game round models, mirroring the backend's `model` package (~/School/2-voor-12).
 *
 * These are lenient client DTOs: unlike the server's `Question`, there is no `init {}`
 * validation here, so a slightly unexpected payload deserializes instead of crashing.
 * Enum constant names match the server exactly (it serializes them by name).
 */

/** The three kinds of question in a round. */
@Serializable
enum class QuestionType { REGULAR, PAARDENSPRONG, TAARTPUZZEL }

/** Reading direction of the taartpuzzel circle. */
@Serializable
enum class Direction { CLOCKWISE, COUNTERCLOCKWISE }

/** Paardensprong detail: a 3×3 grid of letters, row-major as 9 characters. */
@Serializable
data class PaardensprongPuzzle(val grid: String)

/**
 * Taartpuzzel detail: the answer's letters sit around a circle read in [direction];
 * [missingIndex] is the 0-based position in the answer that is hidden.
 */
@Serializable
data class TaartpuzzelPuzzle(
    val missingIndex: Int,
    val direction: Direction,
)

/**
 * One question in a round. Regular questions carry [questionText]; the puzzle types
 * carry their [paardensprong] / [taartpuzzel] payload. [correctLetter] is the letter
 * this answer contributes to the twaalfletterwoord (not always the first letter).
 */
@Serializable
data class Question(
    val type: QuestionType,
    val category: String,
    val correctAnswer: String,
    val correctLetter: String,
    val questionText: String? = null,
    val paardensprong: PaardensprongPuzzle? = null,
    val taartpuzzel: TaartpuzzelPuzzle? = null,
)

/**
 * A generated round: the twaalfletterwoord and the twelve questions whose
 * [Question.correctLetter] values, in order, spell that [word].
 */
@Serializable
data class SoloRound(
    val word: String,
    val questions: List<Question>,
)

/** Request body for `POST /game/solo`. */
@Serializable
data class SoloRoundRequest(val puzzle: PuzzlePreference)

/** Who won an online match. */
@Serializable
enum class GameOutcome {
    @SerialName("host_won")
    HOST_WON,

    @SerialName("guest_won")
    GUEST_WON,

    @SerialName("tie")
    TIE,
}

/** A single player's final outcome: who they are and the score they reported. */
@Serializable
data class PlayerResult(val profile: PlayerProfile, val score: Int)

/** The final result of an online match, used for the results screen. */
@Serializable
data class GameResult(
    val host: PlayerResult,
    val guest: PlayerResult,
    val outcome: GameOutcome,
)
