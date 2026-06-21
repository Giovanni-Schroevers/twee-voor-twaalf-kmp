package com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.rememberAvatarCameraLauncher
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.theme.BrandRed
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.auth.AccountUiState
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.auth.AccountViewModel
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.network.UserDto
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.components.Avatar
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.components.BrandButton
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.components.BrandButtonStyle
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.components.BrandTopBar
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.components.FormMessage
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.components.LabeledField
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.theme.BrandRedInk
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.theme.Danger
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.theme.Ink
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.theme.InkSoft
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.theme.Muted
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.theme.Paper
import org.koin.compose.koinInject

@Composable
fun AccountScreen(
    onBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onChangePassword: () -> Unit,
) {
    val viewModel = koinInject<AccountViewModel>()
    val user by viewModel.user.collectAsState()
    val state by viewModel.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(Paper)) {
        BrandTopBar(title = "Account", flat = true, onBack = onBack)
        val current = user
        if (current == null) {
            LoggedOutContent(
                onLogin = onNavigateToLogin,
                onRegister = onNavigateToRegister,
            )
        } else {
            LoggedInContent(
                user = current,
                state = state,
                viewModel = viewModel,
                onChangePassword = onChangePassword,
            )
        }
    }
}

@Composable
private fun LoggedOutContent(
    onLogin: () -> Unit,
    onRegister: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Spacer(Modifier.padding(top = 20.dp))
        Avatar(size = 90.dp)
        Text("Niet ingelogd", color = Ink, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(
            text = "Log in om je profiel, avatar en scores te bewaren.",
            color = InkSoft,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 240.dp),
        )
        BrandButton(text = "Inloggen", onClick = onLogin)
        BrandButton(text = "Registreren", onClick = onRegister, style = BrandButtonStyle.Outlined)
    }
}

@Composable
private fun LoggedInContent(
    user: UserDto,
    state: AccountUiState,
    viewModel: AccountViewModel,
    onChangePassword: () -> Unit,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AvatarWithPicker(
            user = user,
            avatarUrl = viewModel.avatarUrl(user.avatar),
            uploading = state.isUploadingAvatar,
            onImage = viewModel::uploadAvatar,
        )
        Text(user.username, color = Ink, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(user.email, color = Muted, fontSize = 11.sp)

        Spacer(Modifier.padding(top = 4.dp))

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
        state.error?.let { FormMessage(text = it, color = Danger) }
        state.feedback?.let { FormMessage(text = it, color = BrandRedInk) }

        BrandButton(
            text = "Opslaan",
            onClick = viewModel::save,
            loading = state.isSaving,
        )

        Spacer(Modifier.padding(top = 6.dp))

        BrandButton(
            text = "Wachtwoord wijzigen",
            onClick = onChangePassword,
            style = BrandButtonStyle.Outlined,
        )
        BrandButton(text = "Uitloggen", onClick = viewModel::logout, style = BrandButtonStyle.Grey)
        BrandButton(
            text = "Account verwijderen",
            onClick = { showDeleteDialog = true },
            style = BrandButtonStyle.Danger,
            loading = state.isDeleting,
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Account verwijderen?") },
            text = { Text("Dit kan niet ongedaan worden gemaakt. Je account en gegevens worden permanent verwijderd.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteAccount()
                }) {
                    Text("Verwijderen", color = Danger)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Annuleren")
                }
            },
        )
    }
}

/**
 * Avatar with an edit badge that opens a gallery (all platforms) or camera
 * (mobile only) picker. While uploading it shows a spinner overlay.
 */
@Composable
private fun AvatarWithPicker(
    user: UserDto,
    avatarUrl: String?,
    uploading: Boolean,
    onImage: (PlatformFile) -> Unit,
) {
    val gallery = rememberFilePickerLauncher(type = FileKitType.Image) { file ->
        file?.let(onImage)
    }
    val camera = rememberAvatarCameraLauncher(onImage)
    var menuOpen by remember { mutableStateOf(false) }

    Box {
        Avatar(size = 88.dp, initial = user.username.firstOrNull(), imageUrl = avatarUrl)

        if (uploading) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp)
            }
        }

        // Edit badge (bottom-right of the avatar).
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(30.dp)
                .clip(CircleShape)
                .background(BrandRed)
                .clickable(enabled = !uploading) {
                    // No camera on desktop -> go straight to the gallery.
                    if (camera == null) gallery.launch() else menuOpen = true
                },
            contentAlignment = Alignment.Center,
        ) {
            Text("✎", color = Color.White, fontSize = 15.sp)
        }

        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
            DropdownMenuItem(
                text = { Text("Kies uit galerij") },
                onClick = {
                    menuOpen = false
                    gallery.launch()
                },
            )
            if (camera != null) {
                DropdownMenuItem(
                    text = { Text("Maak een foto") },
                    onClick = {
                        menuOpen = false
                        camera()
                    },
                )
            }
        }
    }
}
