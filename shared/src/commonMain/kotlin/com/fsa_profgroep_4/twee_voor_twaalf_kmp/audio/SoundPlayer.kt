package com.fsa_profgroep_4.twee_voor_twaalf_kmp.audio

/**
 * Plays short countdown cues during a game. [playSignal] marks a phase warning
 * (entering the last 2 minutes of answering, or the last 30 seconds of the word
 * round); [playTick] is the per-second alarm tick in the final seconds. Backed by
 * built-in platform tones, so there are no audio assets to ship.
 */
expect class SoundPlayer() {
    fun playSignal()
    fun playTick()
}
