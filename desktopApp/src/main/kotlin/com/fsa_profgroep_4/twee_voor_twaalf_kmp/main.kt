package com.fsa_profgroep_4.twee_voor_twaalf_kmp

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.di.initKoin

fun main() {
    // Start Koin once, before the Compose window opens.
    initKoin()
    runApp()
}

private fun runApp() = application {
    // Phone-sized window at a fixed position so the desktop build mirrors the
    // mobile layout the UI is designed for.
    val windowState = rememberWindowState(
        size = DpSize(400.dp, 860.dp),
        position = WindowPosition(60.dp, 40.dp),
    )
    Window(
        onCloseRequest = ::exitApplication,
        state = windowState,
        title = "Tweevoortwaalfkmp",
    ) {
        App()
    }
}