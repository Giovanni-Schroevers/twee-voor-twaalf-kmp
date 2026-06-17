package com.fsa_profgroep_4.twee_voor_twaalf_kmp

import androidx.compose.ui.window.ComposeUIViewController
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.di.initKoin

fun MainViewController() = ComposeUIViewController {
    // Start Koin before the Compose UI is built. Guarded inside initKoin(), so
    // it's a no-op if the view controller is re-created.
    initKoin()
    App()
}