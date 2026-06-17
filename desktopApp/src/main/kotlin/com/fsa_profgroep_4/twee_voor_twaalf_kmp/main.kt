package com.fsa_profgroep_4.twee_voor_twaalf_kmp

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.di.initKoin

fun main() {
    // Start Koin once, before the Compose window opens.
    initKoin()
    runApp()
}

private fun runApp() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Tweevoortwaalfkmp",
    ) {
        App()
    }
}