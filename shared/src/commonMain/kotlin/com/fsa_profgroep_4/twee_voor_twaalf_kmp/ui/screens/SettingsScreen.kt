package com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.auth.SettingsViewModel
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.components.BrandButton
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.components.BrandButtonStyle
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.components.BrandTopBar
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.components.FormMessage
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.components.LabeledField
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.theme.BrandRedInk
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.theme.Danger
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.theme.InkSoft
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.theme.Muted
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.theme.Paper
import org.koin.compose.koinInject

/**
 * Lets the user point the app at a different backend at runtime — handy for a
 * demo against a Cloudflare tunnel, where the URL changes each session.
 */
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
) {
    val viewModel = koinInject<SettingsViewModel>()
    val state by viewModel.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(Paper)) {
        BrandTopBar(title = "Serverinstellingen", flat = true, onBack = onBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Stel de backend-URL in waarmee de app verbindt. Handig tijdens een demo met een Cloudflare-tunnel.",
                color = InkSoft,
                fontSize = 14.sp,
            )
            LabeledField(
                label = "Backend-URL",
                value = state.url,
                onValueChange = viewModel::onUrlChange,
                keyboardType = KeyboardType.Uri,
                helper = "Bijv. https://abc-123.trycloudflare.com/",
            )
            state.error?.let { FormMessage(text = it, color = Danger) }
            state.feedback?.let { FormMessage(text = it, color = BrandRedInk) }

            BrandButton(text = "Opslaan", onClick = viewModel::save)
            BrandButton(
                text = "Standaard herstellen",
                onClick = viewModel::resetToDefault,
                style = BrandButtonStyle.Text,
            )
            Text(
                text = "Standaard: ${viewModel.defaultUrl}",
                color = Muted,
                fontSize = 11.sp,
            )
        }
    }
}
