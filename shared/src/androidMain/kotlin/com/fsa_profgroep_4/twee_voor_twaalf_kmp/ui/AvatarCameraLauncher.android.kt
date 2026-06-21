package com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui

import androidx.compose.runtime.Composable
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.compose.rememberCameraPickerLauncher

@Composable
actual fun rememberAvatarCameraLauncher(onImage: (PlatformFile) -> Unit): (() -> Unit)? {
    // FileKit's camera picker handles the runtime CAMERA permission itself.
    val launcher = rememberCameraPickerLauncher { file -> file?.let(onImage) }
    return { launcher.launch() }
}
