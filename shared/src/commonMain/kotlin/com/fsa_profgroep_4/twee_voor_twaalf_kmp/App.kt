package com.fsa_profgroep_4.twee_voor_twaalf_kmp

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject

import tweevoortwaalfkmp.shared.generated.resources.Res
import tweevoortwaalfkmp.shared.generated.resources.compose_multiplatform

@Composable
@Preview
fun App() {
    // Register Coil's app-wide ImageLoader with the Ktor network fetcher. This
    // must run once near the root: it's what lets AsyncImage load https URLs, and
    // it's required for iOS (which has no ServiceLoader to auto-wire the fetcher).
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components { add(KtorNetworkFetcherFactory()) }
            .build()
    }

    MaterialTheme {
        // Demonstration of Koin: rather than `Greeting()`, we ask Koin for the
        // instance it built (see `appModule`). For this to work, initKoin() must
        // have run at the platform entry point.
        val greeting = koinInject<Greeting>()
        var showContent by remember { mutableStateOf(false) }
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(onClick = { showContent = !showContent }) {
                Text("Click me!")
            }
            AnimatedVisibility(showContent) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Image(painterResource(Res.drawable.compose_multiplatform), null)
                    Text("Compose: ${greeting.greet()}")
                    // Coil demo: loads a remote image over the network.
                    AsyncImage(
                        model = "https://picsum.photos/200",
                        contentDescription = "Demonstration remote image",
                        modifier = Modifier.size(120.dp),
                    )
                }
            }
        }
    }
}
