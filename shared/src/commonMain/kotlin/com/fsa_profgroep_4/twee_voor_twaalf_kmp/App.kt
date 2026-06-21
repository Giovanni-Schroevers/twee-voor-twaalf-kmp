package com.fsa_profgroep_4.twee_voor_twaalf_kmp

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.AppNavHost
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.theme.AppTheme

@Composable
@Preview
fun App() {
    // Register Coil's app-wide ImageLoader with the Ktor network fetcher. This
    // must run once near the root: it's what lets AsyncImage load https URLs (e.g.
    // user avatars later), and it's required for iOS (which has no ServiceLoader
    // to auto-wire the fetcher).
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components { add(KtorNetworkFetcherFactory()) }
            .build()
    }

    AppTheme {
        // Paper fills the whole window (incl. behind the system bars). Only the
        // bottom (navigation/gesture bar) is inset here; the TOP inset is handled
        // by each screen's app bar (BrandTopBar / Home header), so the coloured
        // bar can extend up behind the status bar with its content padded below.
        // enableEdgeToEdge() on Android makes these insets non-zero; iOS uses its
        // safe area; desktop reports zero.
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Box(modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.navigationBars)) {
                AppNavHost()
            }
        }
    }
}
