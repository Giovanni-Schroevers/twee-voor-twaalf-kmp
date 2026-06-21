package com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.auth.AuthRepository
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.network.BackendUrlProvider
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.components.Avatar
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.components.ChevronRight
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.components.Wordmark
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.theme.BrandRed
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.theme.Ink
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.theme.InkSoft
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.theme.Line
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.theme.Muted
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.theme.Paper
import org.koin.compose.koinInject

/**
 * Home — wireframe Variant A ("cards"). A flat bar with the wordmark and an
 * avatar that opens Account, an intro line, then two tappable game-mode cards.
 * The game screens themselves are out of scope, so the card callbacks are wired
 * but currently lead nowhere.
 */
@Composable
fun HomeScreen(
    onOpenAccount: () -> Unit,
    onOpenSettings: () -> Unit,
    onOfflineGame: () -> Unit,
    onOnlineGame: () -> Unit,
) {
    // Show the user's avatar (or initial) when signed in.
    val auth = koinInject<AuthRepository>()
    val urlProvider = koinInject<BackendUrlProvider>()
    val user by auth.currentUser.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(Paper)) {
        // Red app bar (design's Home header). Its colour extends up behind the
        // status bar; the row sits below it.
        Column(modifier = Modifier.fillMaxWidth().background(BrandRed)) {
            Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Wordmark(fontSize = 20, inverted = true)
                Spacer(Modifier.weight(1f))
                // Server settings — lets a presenter point the app at a Cloudflare
                // tunnel during a demo.
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onOpenSettings),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("⚙", color = Color.White, fontSize = 20.sp)
                }
                Avatar(
                    size = 36.dp,
                    initial = user?.username?.firstOrNull(),
                    imageUrl = urlProvider.avatarUrl(user?.avatar),
                    modifier = Modifier.clip(CircleShape).clickable(onClick = onOpenAccount),
                )
            }
        }
        Column(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 22.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Twaalf vragen, één twaalfletterwoord. Kies je spel.",
                color = InkSoft,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 4.dp),
            )
            GameModeCard(
                title = "Offline spel",
                subtitle = "Samen op dit toestel",
                onClick = onOfflineGame,
            )
            GameModeCard(
                title = "Online spel",
                subtitle = "Tegen een tweede toestel",
                onClick = onOnlineGame,
            )
        }
    }
}

@Composable
private fun GameModeCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.5.dp, Line, shape)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Ink, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(subtitle, color = Muted, fontSize = 12.sp)
        }
        Box(contentAlignment = Alignment.Center) { ChevronRight() }
    }
}
