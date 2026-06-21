package com.fsa_profgroep_4.twee_voor_twaalf_kmp

import com.fsa_profgroep_4.twee_voor_twaalf_kmp.game.scoreRound
import kotlin.test.Test
import kotlin.test.assertEquals

class ScoringTest {

    @Test
    fun allCorrect_noLetters_getsStartPlusPerAnswerPlusFullBonus() {
        // 500 + 12*10 + 100 (0 mistakes) - 0 = 720
        assertEquals(720, scoreRound(correctCount = 12, questionCount = 12, boughtLetters = 0, guessedWord = true))
    }

    @Test
    fun mistakeBonusTiers() {
        // base = 500 + 10*correct; bonus by mistakes (12 - correct).
        assertEquals(500 + 110 + 75, scoreRound(11, 12, 0, true)) // 1 mistake → +75
        assertEquals(500 + 100 + 50, scoreRound(10, 12, 0, true)) // 2 mistakes → +50
        assertEquals(500 + 90 + 25, scoreRound(9, 12, 0, true))   // 3 mistakes → +25
        assertEquals(500 + 80 + 0, scoreRound(8, 12, 0, true))    // 4 mistakes → +0
    }

    @Test
    fun boughtLettersCost50Each() {
        // 500 + 12*10 + 100 - 3*50 = 570
        assertEquals(570, scoreRound(12, 12, boughtLetters = 3, guessedWord = true))
    }

    @Test
    fun wrongWordScoresZero() {
        assertEquals(0, scoreRound(correctCount = 12, questionCount = 12, boughtLetters = 0, guessedWord = false))
    }

    @Test
    fun neverGoesNegative() {
        assertEquals(0, scoreRound(correctCount = 0, questionCount = 12, boughtLetters = 12, guessedWord = true))
    }
}
