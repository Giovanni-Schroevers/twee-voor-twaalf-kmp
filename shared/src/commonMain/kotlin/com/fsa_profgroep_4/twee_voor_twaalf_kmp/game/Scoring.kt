package com.fsa_profgroep_4.twee_voor_twaalf_kmp.game

/**
 * 2 voor 12 scoring. A team starts at 500 points:
 *  - +10 for each question answered correctly from memory,
 *  - a bonus for few mistakes over the twelve questions (0→+100, 1→+75, 2→+50, 3→+25, else 0),
 *  - minus a flat cost per bought (revealed) letter.
 *
 * Guessing the twaalfletterwoord wrong means a score of 0 — you lose. The result is
 * floored at 0 so buying many letters can't go negative.
 */
internal fun scoreRound(
    correctCount: Int,
    questionCount: Int,
    boughtLetters: Int,
    guessedWord: Boolean,
): Int {
    if (!guessedWord) return 0
    val mistakes = questionCount - correctCount
    val mistakeBonus = when (mistakes) {
        0 -> 100
        1 -> 75
        2 -> 50
        3 -> 25
        else -> 0
    }
    val score = START_SCORE + CORRECT_POINTS * correctCount + mistakeBonus - LETTER_COST * boughtLetters
    return score.coerceAtLeast(0)
}

private const val START_SCORE = 500
private const val CORRECT_POINTS = 10
private const val LETTER_COST = 50
