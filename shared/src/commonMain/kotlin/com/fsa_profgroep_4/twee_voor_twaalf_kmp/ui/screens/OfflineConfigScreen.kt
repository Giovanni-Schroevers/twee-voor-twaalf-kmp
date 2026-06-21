package com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.data.GameSettingsRepository
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.network.PuzzlePreference
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.components.BrandButton
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.components.BrandTopBar
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.components.Dropdown
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.components.Eyebrow
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.theme.Ink
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.theme.Line
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.theme.Muted
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.theme.Paper
import org.koin.compose.koinInject

/**
 * Offline (single-device) game setup — wireframe `ConfigOffline`. Pick the puzzle
 * type, read the "one device" explainer, then start. The puzzle choice is saved
 * locally (shared with the online lobby). Starting the game is a later slice, so
 * the button is intentionally inert for now.
 */
@Composable
fun OfflineConfigScreen(onBack: () -> Unit) {
    val settings = koinInject<GameSettingsRepository>()
    val saved by settings.settings.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(Paper)) {
        BrandTopBar(title = "Offline spel", flat = true, onBack = onBack)
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Eyebrow("Instellingen")
            Dropdown(
                label = "Mini-puzzel",
                options = PuzzlePreference.entries,
                selected = saved.puzzle,
                optionLabel = { it.label },
                onSelect = { settings.setPuzzle(it) },
            )
            InfoCard(
                title = "Eén toestel",
                body = "Speel om de beurt op dit toestel. Geen tegenstander nodig.",
            )
            Spacer(Modifier.weight(1f))
            BrandButton(
                text = "Start spel",
                onClick = { /* gameplay is the next slice */ },
                leading = { Text("▶", color = Color.White, fontSize = 15.sp) },
            )
        }
    }
}

/** A bordered explainer card (the wireframe's `.card`). */
@Composable
private fun InfoCard(title: String, body: String) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.5.dp, Line, shape)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(title, color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        Text(body, color = Muted, fontSize = 13.sp)
    }
}
