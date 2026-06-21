package com.fsa_profgroep_4.twee_voor_twaalf_kmp.audio

import java.awt.Toolkit

/** Desktop uses the AWT system beep — simple and asset-free. */
actual class SoundPlayer {

    actual fun playSignal() {
        runCatching { Toolkit.getDefaultToolkit().beep() }
    }

    actual fun playTick() {
        runCatching { Toolkit.getDefaultToolkit().beep() }
    }
}
