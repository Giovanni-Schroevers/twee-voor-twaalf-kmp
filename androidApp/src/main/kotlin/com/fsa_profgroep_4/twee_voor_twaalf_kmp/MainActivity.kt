package com.fsa_profgroep_4.twee_voor_twaalf_kmp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.di.initKoinAndroid

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Start Koin before any Composable resolves a dependency. initKoinAndroid
        // is safe to call more than once (it no-ops if already started) and
        // registers the application Context so the platform Koin module can build
        // the Room database from it.
        initKoinAndroid(this)

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