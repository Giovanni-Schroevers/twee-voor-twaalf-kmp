package com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/* Brand palette, translated from the wireframe's CSS custom properties
   (wireframe.css `:root`). The red is the sRGB equivalent of the design's
   `oklch(0.575 0.205 26)`. */
val BrandRed = Color(0xFFD72E30)
val BrandRedInk = Color(0xFFB5282A)
val BrandRedSoft = Color(0xFFFBE9E7)

val Paper = Color(0xFFFBFAF7)
val Ink = Color(0xFF33312E)
val InkSoft = Color(0xFF6F6B66)
val Muted = Color(0xFF9B9690)
val Line = Color(0xFFB9B4AD)
val LineSoft = Color(0xFFD9D5CE)
val Fill = Color(0xFFEFECE7)
val Fill2 = Color(0xFFE6E2DB)
val Danger = Color(0xFFB23B34)

private val BrandColorScheme = lightColorScheme(
    primary = BrandRed,
    onPrimary = Color.White,
    primaryContainer = BrandRedSoft,
    onPrimaryContainer = BrandRedInk,
    background = Paper,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = Fill,
    onSurfaceVariant = InkSoft,
    outline = Line,
    outlineVariant = LineSoft,
    error = Danger,
    onError = Color.White,
)

/**
 * Wraps content in the 2v12 brand theme (Material 3). All screens render inside
 * this so colours come from `MaterialTheme.colorScheme` rather than hard-coded
 * values. Custom display fonts (Gaegu/Hanken in the wireframe) are intentionally
 * out of scope — the platform default is used.
 */
@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BrandColorScheme,
        content = content,
    )
}
