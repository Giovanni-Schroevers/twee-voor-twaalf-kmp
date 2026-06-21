package com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.theme.BrandRed
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.theme.BrandRedInk
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.theme.Danger
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.theme.Fill
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.theme.Fill2
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.theme.Ink
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.theme.InkSoft
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.theme.Line
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.theme.LineSoft
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.theme.Muted
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.theme.Paper

/**
 * Shared UI primitives that mirror the wireframe kit (`wireframe.css`): the
 * wordmark, buttons, text field, avatar and top bar. Screens compose these so
 * they stay declarative and consistent.
 */

/** The "2v12" logo: a rounded red chip with white text (or inverted on a red surface). */
@Composable
fun Wordmark(
    modifier: Modifier = Modifier,
    inverted: Boolean = false,
    fontSize: Int = 30,
) {
    val background = if (inverted) Color.White else BrandRed
    val foreground = if (inverted) BrandRed else Color.White
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(11.dp))
            .background(background)
            .padding(horizontal = 12.dp, vertical = 4.dp),
    ) {
        Text(
            text = "2v12",
            color = foreground,
            fontWeight = FontWeight.Bold,
            fontSize = fontSize.sp,
        )
    }
}

enum class BrandButtonStyle { Contained, Outlined, Text, Grey, Danger }

/**
 * Full-width button matching the wireframe's MUI-flavoured variants. [leading]
 * is an optional icon/glyph drawn before the label; [loading] swaps the label
 * for a spinner and blocks taps.
 */
@Composable
fun BrandButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    style: BrandButtonStyle = BrandButtonStyle.Contained,
    enabled: Boolean = true,
    loading: Boolean = false,
    leading: (@Composable () -> Unit)? = null,
) {
    val shape = RoundedCornerShape(8.dp)
    val background = when (style) {
        BrandButtonStyle.Contained -> BrandRed
        BrandButtonStyle.Grey -> Fill2
        else -> Color.Transparent
    }
    val foreground = when (style) {
        BrandButtonStyle.Contained -> Color.White
        BrandButtonStyle.Grey -> Ink
        BrandButtonStyle.Danger -> Danger
        else -> BrandRedInk
    }
    val borderColor = when (style) {
        BrandButtonStyle.Outlined -> BrandRed
        BrandButtonStyle.Danger -> Danger
        else -> null
    }
    val clickable = enabled && !loading
    var box = modifier
        .clip(shape)
        .background(background)
    if (borderColor != null) box = box.border(1.5.dp, borderColor, shape)
    box = box
        .clickable(enabled = clickable) { onClick() }
        .heightIn(min = 52.dp)
        .padding(horizontal = 18.dp, vertical = 13.dp)

    Box(modifier = box, contentAlignment = Alignment.Center) {
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = foreground, strokeWidth = 2.dp)
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(9.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                leading?.invoke()
                Text(text = text, color = foreground, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
        }
    }
}

/**
 * Outlined text field with a floating label, mirroring the wireframe's `.field`.
 * Pass [errorText] to show a red helper line (it takes priority over [helper]).
 */
@Composable
fun LabeledField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    helper: String? = null,
    errorText: String? = null,
    enabled: Boolean = true,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        singleLine = true,
        shape = RoundedCornerShape(8.dp),
        label = { Text(label) },
        isError = errorText != null,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        supportingText = (errorText ?: helper)?.let { text -> { Text(text) } },
    )
}

/**
 * Circular avatar. Shows the image at [imageUrl] when present (loaded with Coil),
 * else [initial] when provided, else a simple drawn person silhouette.
 */
@Composable
fun Avatar(
    size: Dp,
    modifier: Modifier = Modifier,
    initial: Char? = null,
    imageUrl: String? = null,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Fill)
            .border(1.5.dp, Line, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else if (initial != null) {
            Text(
                text = initial.uppercaseChar().toString(),
                color = InkSoft,
                fontWeight = FontWeight.Bold,
                fontSize = (size.value * 0.4f).sp,
            )
        } else {
            Canvas(modifier = Modifier.size(size * 0.55f)) {
                val w = this.size.width
                val h = this.size.height
                // Head
                drawCircle(color = Muted, radius = w * 0.22f, center = Offset(w / 2f, h * 0.3f))
                // Shoulders (lower half-disc)
                drawArc(
                    color = Muted,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = true,
                    topLeft = Offset(w * 0.1f, h * 0.55f),
                    size = Size(w * 0.8f, h * 0.7f),
                )
            }
        }
    }
}

/**
 * App bar. Red surface with white content by default; pass [flat] for the light
 * variant used on form screens. [onBack] adds a back chevron; [trailing] is an
 * optional end slot (e.g. the avatar on Home).
 */
@Composable
fun BrandTopBar(
    title: String,
    modifier: Modifier = Modifier,
    flat: Boolean = false,
    onBack: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val background = if (flat) Paper else BrandRed
    val foreground = if (flat) Ink else Color.White
    Column(modifier = modifier.fillMaxWidth().background(background)) {
        // Extend the bar's colour up behind the status bar; the row below sits
        // clear of the clock/battery. Zero-height on desktop.
        Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onBack != null) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center,
                ) {
                    Text("‹", color = foreground, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = title,
                color = foreground,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp),
            )
            if (trailing != null) trailing()
        }
        if (flat) {
            // Hairline under the flat bar (the design's inset bottom border).
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(LineSoft))
        }
    }
}

/** A small right-chevron glyph used in list rows / cards. */
@Composable
fun ChevronRight(color: Color = Muted) {
    Text("›", color = color, fontSize = 24.sp, fontWeight = FontWeight.Bold)
}

/** Centered error/helper line used below forms. */
@Composable
fun FormMessage(text: String, color: Color, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = color,
        fontSize = 13.sp,
        textAlign = TextAlign.Center,
        modifier = modifier.fillMaxWidth(),
    )
}
