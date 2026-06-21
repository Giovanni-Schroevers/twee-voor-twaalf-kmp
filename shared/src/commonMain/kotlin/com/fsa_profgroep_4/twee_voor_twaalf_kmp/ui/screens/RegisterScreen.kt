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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.auth.RegisterViewModel
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.components.BrandButton
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.components.BrandTopBar
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.components.FormMessage
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.components.LabeledField
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.theme.Danger
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.theme.Paper
import org.koin.compose.koinInject

@Composable
fun RegisterScreen(
    onBack: () -> Unit,
    onRegistered: () -> Unit,
) {
    val viewModel = koinInject<RegisterViewModel>()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.success) {
        if (state.success) onRegistered()
    }

    Column(modifier = Modifier.fillMaxSize().background(Paper)) {
        BrandTopBar(title = "Registreren", flat = true, onBack = onBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Avatar is set after signing in (Account screen), since the upload
            // endpoint requires authentication.
            LabeledField(
                label = "Gebruikersnaam",
                value = state.username,
                onValueChange = viewModel::onUsernameChange,
            )
            LabeledField(
                label = "E-mail",
                value = state.email,
                onValueChange = viewModel::onEmailChange,
                keyboardType = KeyboardType.Email,
            )
            LabeledField(
                label = "Wachtwoord",
                value = state.password,
                onValueChange = viewModel::onPasswordChange,
                isPassword = true,
                keyboardType = KeyboardType.Password,
                helper = "Minimaal 8 tekens",
            )
            state.error?.let { FormMessage(text = it, color = Danger) }
            BrandButton(
                text = "Account aanmaken",
                onClick = viewModel::submit,
                loading = state.isLoading,
            )
        }
    }
}
