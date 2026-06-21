package com.fsa_profgroep_4.twee_voor_twaalf_kmp.audio

import platform.AudioToolbox.AudioServicesPlaySystemSound

/** Plays built-in iOS system sounds via AudioToolbox — no audio assets. */
actual class SoundPlayer {

    actual fun playSignal() {
        AudioServicesPlaySystemSound(SIGNAL_SOUND)
    }

    actual fun playTick() {
        AudioServicesPlaySystemSound(TICK_SOUND)
    }

    private companion object {
        // System sound IDs: a short alert for the warning, a "tink" for each tick.
        const val SIGNAL_SOUND: UInt = 1005u
        const val TICK_SOUND: UInt = 1103u
    }
}
