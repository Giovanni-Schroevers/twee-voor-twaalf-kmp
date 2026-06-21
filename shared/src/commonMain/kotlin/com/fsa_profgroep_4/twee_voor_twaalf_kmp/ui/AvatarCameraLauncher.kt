package com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui

import androidx.compose.runtime.Composable
import io.github.vinceglb.filekit.PlatformFile

/**
 * Remembers a launcher that takes a photo with the device camera, invoking
 * [onImage] with the captured file. Returns `null` on platforms without a camera
 * (desktop), so callers can hide the "take photo" option there.
 *
 * The mobile actuals use FileKit's camera picker, which also requests the runtime
 * camera permission. Gallery picking (`rememberFilePickerLauncher`) needs no
 * expect/actual — it works on every platform from common code.
 */
@Composable
expect fun rememberAvatarCameraLauncher(onImage: (PlatformFile) -> Unit): (() -> Unit)?
