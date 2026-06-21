package com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.auth.ChangePasswordViewModel
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.components.BrandButton
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.components.BrandTopBar
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.components.FormMessage
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.components.LabeledField
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.theme.Danger
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.theme.Ink
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.theme.InkSoft
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.theme.Paper
import org.koin.compose.koinInject

@Composable
fun ChangePasswordScreen(
    onBack: () -> Unit,
) {
    val viewModel = koinInject<ChangePasswordViewModel>()
    val state by viewModel.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(Paper)) {
        BrandTopBar(title = "Wachtwoord wijzigen", flat = true, onBack = onBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (state.success) {
                Spacer(Modifier.padding(top = 12.dp))
                Text("Je wachtwoord is gewijzigd.", color = Ink, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                BrandButton(text = "Terug", onClick = onBack)
            } else {
                Text(
                    text = "Kies een nieuw wachtwoord. Je hebt je huidige wachtwoord nodig om de wijziging te bevestigen.",
                    color = InkSoft,
                    fontSize = 14.sp,
                )
                LabeledField(
                    label = "Huidig wachtwoord",
                    value = state.currentPassword,
                    onValueChange = viewModel::onCurrentPasswordChange,
                    isPassword = true,
                    keyboardType = KeyboardType.Password,
                )
                LabeledField(
                    label = "Nieuw wachtwoord",
                    value = state.newPassword,
                    onValueChange = viewModel::onNewPasswordChange,
                    isPassword = true,
                    keyboardType = KeyboardType.Password,
                    helper = "Minimaal 8 tekens",
                )
                LabeledField(
                    label = "Bevestig nieuw wachtwoord",
                    value = state.confirmPassword,
                    onValueChange = viewModel::onConfirmPasswordChange,
                    isPassword = true,
                    keyboardType = KeyboardType.Password,
                )
                state.error?.let { FormMessage(text = it, color = Danger) }
                BrandButton(
                    text = "Wachtwoord wijzigen",
                    onClick = viewModel::submit,
                    loading = state.isLoading,
                )
            }
        }
    }
}
