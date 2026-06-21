package com.fsa_profgroep_4.twee_voor_twaalf_kmp.audio

import android.media.AudioManager
import android.media.ToneGenerator

/** Uses Android's built-in [ToneGenerator] — no audio assets, no Context needed. */
actual class SoundPlayer {

    // Constructing a ToneGenerator can rarely fail if audio resources are busy; guard it.
    private val toneGenerator: ToneGenerator? =
        runCatching { ToneGenerator(AudioManager.STREAM_MUSIC, VOLUME) }.getOrNull()

    actual fun playSignal() {
        runCatching { toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, SIGNAL_MS) }
    }

    actual fun playTick() {
        runCatching { toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, TICK_MS) }
    }

    private companion object {
        const val VOLUME = 80
        const val SIGNAL_MS = 250
        const val TICK_MS = 90
    }
}
