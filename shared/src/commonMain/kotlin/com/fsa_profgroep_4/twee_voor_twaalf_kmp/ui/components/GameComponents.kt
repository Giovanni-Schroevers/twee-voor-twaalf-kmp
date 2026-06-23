package com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.theme.BrandRed
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.theme.BrandRedInk
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.theme.BrandRedSoft
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.theme.Fill
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.theme.Ink
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.theme.Line
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.theme.Muted
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.theme.Paper
import kotlin.math.cos
import kotlin.math.sin

/**
 * Red game header (the wireframe's `PlayHead`/`PuzzleHead`): back chevron, centered
 * [title], `mm:ss` [time]. Optional [below] content (e.g. the letter strip) renders
 * on the same red surface.
 */
@Composable
fun GameHeader(
    title: String,
    time: String,
    onBack: (() -> Unit)? = null,
    warning: Boolean = false,
    below: (@Composable () -> Unit)? = null,
) {
    Column(modifier = Modifier.fillMaxWidth().background(BrandRed)) {
        Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onBack != null) {
                Box(
                    modifier = Modifier.size(36.dp).clip(CircleShape).clickable(onClick = onBack),
                    contentAlignment = Alignment.Center,
                ) { Text("‹", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold) }
            } else {
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
            )
            // In the warning window the timer flips to an urgent white pill.
            if (warning) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White)
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                ) {
                    Text(text = time, color = BrandRedInk, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            } else {
                Text(text = time, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
        if (below != null) {
            Box(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)) { below() }
        }
    }
}

/**
 * The twelve first-letters collected across the round (the wireframe's `.lstrip`).
 * Renders on a red surface; [currentIndex] is highlighted white.
 */
@Composable
fun LetterStrip(letters: List<Char?>, currentIndex: Int?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        letters.forEachIndexed { index, letter ->
            val current = index == currentIndex
            val shape = RoundedCornerShape(5.dp)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .clip(shape)
                    .background(if (current) Color.White else Color.White.copy(alpha = 0.12f))
                    .border(1.5.dp, Color.White.copy(alpha = if (current) 1f else 0.5f), shape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = letter?.toString() ?: "",
                    color = if (current) BrandRedInk else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                )
            }
        }
    }
}

/**
 * Paardensprong 3×3 grid (the wireframe's `.knight`), from a 9-char [grid] string.
 * The center cell is never part of a knight's-move path, so it's shown disabled
 * (an "×") regardless of what the data holds there — just like on TV.
 */
@Composable
fun KnightGrid(grid: String, modifier: Modifier = Modifier) {
    // The 8 letters fill the 8 outer cells in reading order; the center is never
    // part of a knight's-move path, so it holds no letter and shows a disabled "×".
    val letters = grid.filter { !it.isWhitespace() }
    Column(
        modifier = modifier.width(200.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        for (row in 0 until 3) {
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                for (col in 0 until 3) {
                    val isCenter = row == 1 && col == 1
                    // Index into the letters skipping the center cell.
                    val cellIndex = row * 3 + col
                    val letterIndex = if (cellIndex > 4) cellIndex - 1 else cellIndex
                    val char = if (isCenter || letterIndex >= letters.length) ' ' else letters[letterIndex]
                    val shape = RoundedCornerShape(8.dp)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(shape)
                            .background(if (isCenter) Fill else Paper)
                            .border(1.5.dp, Line, shape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = if (isCenter) "×" else char.uppercaseChar().toString().trim(),
                            color = if (isCenter) Muted else Ink,
                            fontWeight = FontWeight.Bold,
                            fontSize = 26.sp,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Taartpuzzel circle (the wireframe's `.pie`): the [answer]'s letters placed around
 * a ring, with the slot at [missingIndex] shown as "?".
 */
@Composable
fun TaartPie(answer: String, missingIndex: Int, modifier: Modifier = Modifier) {
    val size = 210f
    val center = size / 2f
    val radius = 84f
    val cell = 34f
    val letters = answer.uppercase()
    Box(modifier = modifier.size(size.dp)) {
        // Outer ring.
        Box(
            modifier = Modifier
                .size(size.dp)
                .clip(CircleShape)
                .background(Paper)
                .border(1.5.dp, Line, CircleShape),
        )
        letters.forEachIndexed { index, char ->
            val angle = (-90.0 + index * (360.0 / letters.length)) * (kotlin.math.PI / 180.0)
            val x = center + radius * cos(angle).toFloat() - cell / 2f
            val y = center + radius * sin(angle).toFloat() - cell / 2f
            val missing = index == missingIndex
            Box(
                modifier = Modifier
                    .offset(x.dp, y.dp)
                    .size(cell.dp)
                    .clip(CircleShape)
                    .background(if (missing) BrandRedSoft else Paper)
                    .border(1.5.dp, if (missing) BrandRed else Line, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (missing) "?" else char.toString(),
                    color = if (missing) BrandRedInk else Ink,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                )
            }
        }
        // Center hub.
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(54.dp)
                .clip(CircleShape)
                .background(Fill)
                .border(1.5.dp, Line, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "draai de\ntaart",
                color = BrandRedInk,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/**
 * The twelve word slots (the wireframe's `.wgrid`). Each slot shows the letter that
 * has been placed into it (from the bank), or is empty.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WordGrid(slots: List<Char?>) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        slots.forEach { char ->
            val filled = char != null
            val shape = RoundedCornerShape(4.dp)
            Box(
                modifier = Modifier
                    .size(width = 26.dp, height = 34.dp)
                    .clip(shape)
                    .background(if (filled) BrandRedSoft else Color.Transparent)
                    .border(2.dp, if (filled) BrandRed else Line, shape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = char?.toString() ?: "",
                    color = if (filled) BrandRedInk else Ink,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                )
            }
        }
    }
}

/** One bank chip tied to a question [position]; [char] is the letter to show (null = blank). */
data class LetterChip(val position: Int, val char: Char?, val placed: Boolean)

/**
 * The collected-letters bank (the wireframe's `.bank`), in question order. Tapping a
 * chip places its letter into the grid (and reveals the correct letter for a wrong
 * answer); placed chips are dimmed.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LetterBank(chips: List<LetterChip>, onChipClick: (Int) -> Unit) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(7.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        chips.forEach { chip ->
            val shape = RoundedCornerShape(8.dp)
            val alpha = if (chip.placed) 0.4f else 1f
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(shape)
                    .background(Fill.copy(alpha = alpha))
                    .border(1.5.dp, Line.copy(alpha = alpha), shape)
                    .clickable { onChipClick(chip.position) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = chip.char?.toString() ?: "",
                    color = Ink.copy(alpha = if (chip.placed) 0.5f else 1f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                )
            }
        }
    }
}

/** Thin horizontal rule (the wireframe's `.divider`). */
@Composable
fun GameDivider() {
    Box(modifier = Modifier.fillMaxWidth().height(1.5.dp).background(Line.copy(alpha = 0.5f)))
}
