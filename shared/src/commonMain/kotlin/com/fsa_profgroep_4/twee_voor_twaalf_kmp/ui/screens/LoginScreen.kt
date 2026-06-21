package com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.auth.LoginViewModel
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.components.BrandButton
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.components.BrandTopBar
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.components.FormMessage
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.components.LabeledField
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.components.Wordmark
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.theme.BrandRedInk
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.theme.Danger
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.theme.InkSoft
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.theme.Paper
import org.koin.compose.koinInject

@Composable
fun LoginScreen(
    onBack: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onLoggedIn: () -> Unit,
) {
    val viewModel = koinInject<LoginViewModel>()
    val state by viewModel.state.collectAsState()

    // Navigate away exactly once when login flips to success.
    LaunchedEffect(state.success) {
        if (state.success) onLoggedIn()
    }

    Column(modifier = Modifier.fillMaxSize().background(Paper)) {
        BrandTopBar(title = "Inloggen", flat = true, onBack = onBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Wordmark(modifier = Modifier.padding(top = 8.dp))
            LabeledField(
                label = "Gebruikersnaam",
                value = state.username,
                onValueChange = viewModel::onUsernameChange,
            )
            LabeledField(
                label = "Wachtwoord",
                value = state.password,
                onValueChange = viewModel::onPasswordChange,
                isPassword = true,
                keyboardType = KeyboardType.Password,
            )
            state.error?.let { FormMessage(text = it, color = Danger) }
            BrandButton(
                text = "Inloggen",
                onClick = viewModel::submit,
                loading = state.isLoading,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Nog geen account? ", color = InkSoft)
                Text(
                    text = "Registreer",
                    color = BrandRedInk,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable(onClick = onNavigateToRegister),
                )
            }
        }
    }
}
