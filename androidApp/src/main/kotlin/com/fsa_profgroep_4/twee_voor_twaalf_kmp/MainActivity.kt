package com.fsa_profgroep_4.twee_voor_twaalf_kmp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.di.initKoin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Start Koin before any Composable resolves a dependency.
        // initKoin() is safe to call more than once (it no-ops if already started).
        // Once you add the `koin-android` dependency you can pass
        // `initKoin { androidContext(applicationContext) }` to make the
        // Android Context injectable.
        initKoin()

        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}