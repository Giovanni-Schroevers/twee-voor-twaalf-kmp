package com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui

import androidx.compose.runtime.Composable
import io.github.vinceglb.filekit.PlatformFile

/** Desktop has no camera picker — only the gallery/file picker is offered. */
@Composable
actual fun rememberAvatarCameraLauncher(onImage: (PlatformFile) -> Unit): (() -> Unit)? = null
