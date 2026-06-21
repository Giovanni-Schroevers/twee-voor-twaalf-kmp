package com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.game.GamePhase
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.game.GameUiState
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.game.GameViewModel
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.game.MatchResult
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.network.Question
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.network.QuestionType
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.components.BrandButton
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.components.BrandButtonStyle
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.components.BrandTopBar
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.components.Eyebrow
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.components.GameDivider
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.components.GameHeader
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.components.KnightGrid
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.components.LabeledField
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.components.LetterBank
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.components.LetterChip
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.components.LetterStrip
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.components.TaartPie
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.components.WordGrid
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.theme.BrandRed
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.theme.BrandRedInk
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.theme.BrandRedSoft
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.theme.Ink
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.theme.InkSoft
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.theme.Line
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.theme.LineSoft
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.theme.Muted
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.theme.Paper
import org.koin.compose.koinInject

/**
 * Hosts gameplay: the answering phase (regular + the two mini-puzzles), then the
 * twaalfletterwoord phase, then an "ingeleverd" placeholder (results is the next
 * slice). Works for both solo and online via [GameViewModel].
 */
@Composable
fun GameScreen(onExit: () -> Unit) {
    val viewModel = koinInject<GameViewModel>()
    val state by viewModel.state.collectAsState()

    // Online sessions are closed when the game leaves the composition (koinInject
    // ViewModels don't get onCleared).
    DisposableEffect(Unit) {
        onDispose { viewModel.disconnect() }
    }

    when {
        state.error != null -> MessageScreen(title = "Oeps", body = state.error!!, onExit = onExit)
        state.round == null -> LoadingScreen()
        else -> when (state.phase) {
            is GamePhase.Answering -> AnsweringPhase(state, viewModel, onExit)
            GamePhase.Word -> WordPhase(state, viewModel, onExit)
            GamePhase.Results -> ResultsPhase(state, onExit)
        }
    }
}

@Composable
private fun AnsweringPhase(state: GameUiState, viewModel: GameViewModel, onExit: () -> Unit) {
    val index = state.currentIndex ?: 0
    val question = state.currentQuestion ?: return
    val number = index + 1
    val total = state.questionCount
    val isRegular = question.type == QuestionType.REGULAR

    Box(modifier = Modifier.fillMaxSize().background(Paper)) {
        Column(modifier = Modifier.fillMaxSize()) {
            GameHeader(
                title = headerTitle(number, total, question),
                time = formatTime(state.remainingSeconds),
                onBack = onExit,
                warning = state.timerWarning,
                below = if (isRegular) {
                    { LetterStrip(letters = state.collectedLetters, currentIndex = index) }
                } else {
                    null
                },
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                when (question.type) {
                    QuestionType.REGULAR -> RegularBody(number, question)
                    QuestionType.PAARDENSPRONG -> PaardensprongBody(question)
                    QuestionType.TAARTPUZZEL -> TaartpuzzelBody(question)
                }

                LabeledField(
                    label = answerLabel(question),
                    value = state.answers[index].orEmpty(),
                    onValueChange = viewModel::onAnswerChange,
                    helper = if (isRegular) "alleen de beginletter telt mee voor het woord" else null,
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BrandButton(
                        text = "Vorige",
                        onClick = viewModel::previous,
                        style = BrandButtonStyle.Grey,
                        enabled = index > 0,
                        modifier = Modifier.weight(1f),
                    )
                    BrandButton(
                        text = "Overslaan",
                        onClick = viewModel::skip,
                        style = BrandButtonStyle.Text,
                        modifier = Modifier.weight(1f),
                    )
                    BrandButton(
                        text = if (number == total) "Klaar" else "Volgende",
                        onClick = viewModel::next,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        if (state.waitingForOpponent) {
            WaitingOverlay()
        }
    }
}

@Composable
private fun ColumnScope.RegularBody(number: Int, question: Question) {
    Eyebrow("Vraag $number · ${question.category}")
    Text(
        text = question.questionText ?: "—",
        color = Ink,
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
    )
}

@Composable
private fun ColumnScope.PaardensprongBody(question: Question) {
    PuzzleCard(
        "Maak een woord van 8 letters met paardensprongen (2 + 1, zoals het schaakpaard). " +
            "Beginletter en richting onbekend.",
    )
    question.paardensprong?.let { KnightGrid(grid = it.grid, modifier = Modifier.align(Alignment.CenterHorizontally)) }
}

@Composable
private fun ColumnScope.TaartpuzzelBody(question: Question) {
    PuzzleCard("Vorm een woord met of tegen de klok in. Eén letter ontbreekt — welke?")
    question.taartpuzzel?.let {
        TaartPie(
            answer = question.correctAnswer,
            missingIndex = it.missingIndex,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
    }
}

/** A column receiver is needed for [Modifier.align]; these bodies live inside the scroll Column. */
@Composable
private fun ColumnScope.PuzzleCard(text: String) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.5.dp, LineSoft, shape)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Eyebrow("Mini-puzzel")
        Text(text, color = InkSoft, fontSize = 13.sp)
    }
}

@Composable
private fun WordPhase(state: GameUiState, viewModel: GameViewModel, onExit: () -> Unit) {
    val chips = state.letters.map {
        LetterChip(position = it.presentationIndex, char = if (it.placed) it.correct else it.typed, placed = it.placed)
    }

    Column(modifier = Modifier.fillMaxSize().background(Paper)) {
        GameHeader(
            title = "Het twaalfletterwoord",
            time = formatTime(state.remainingSeconds),
            onBack = onExit,
            warning = state.timerWarning,
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                "Tik je verzamelde letters om ze op hun plek te zetten. Een fout antwoord toont de juiste letter.",
                color = InkSoft,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            WordGrid(slots = state.slots)
            LetterBank(chips = chips, onChipClick = viewModel::onLetterClick)
            GameDivider()
            LabeledField(
                label = "Jouw antwoord",
                value = state.guess,
                onValueChange = viewModel::onGuessChange,
            )
            Spacer(Modifier.weight(1f))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                BrandButton(
                    text = "Wissen",
                    onClick = viewModel::clearGrid,
                    style = BrandButtonStyle.Grey,
                    modifier = Modifier.weight(1f),
                )
                BrandButton(
                    text = "Inleveren",
                    onClick = viewModel::submit,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun ResultsPhase(state: GameUiState, onExit: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(Paper)) {
        BrandTopBar(title = "Uitslag", flat = true)
        if (state.isOnline && state.waitingForResult) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    CircularProgressIndicator()
                    Text("Wachten op de uitslag…", color = InkSoft, fontSize = 14.sp)
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(resultTitle(state), color = BrandRedInk, fontWeight = FontWeight.Bold, fontSize = 26.sp)
                ScoreCard(name = "Jij", score = state.myScore ?: 0, winner = isMyWin(state))
                if (state.isOnline && state.opponentName != null) {
                    ScoreCard(
                        name = state.opponentName,
                        score = state.opponentScore ?: 0,
                        winner = state.matchResult == MatchResult.LOST,
                    )
                }
                Text(
                    text = if (state.guessedWord) "Je raadde het twaalfletterwoord goed." else "Het twaalfletterwoord was fout.",
                    color = if (state.guessedWord) Ink else Muted,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                )
                state.round?.word?.let {
                    Text("Het woord was: $it", color = InkSoft, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }
                Spacer(Modifier.weight(1f))
                BrandButton(text = "Naar home", onClick = onExit)
            }
        }
    }
}

@Composable
private fun ScoreCard(name: String, score: Int, winner: Boolean) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (winner) BrandRedSoft else Color.Transparent)
            .border(1.5.dp, if (winner) BrandRed else Line, shape)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (winner) Text("👑", fontSize = 18.sp)
            Text(name, color = Ink, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        Text(
            "$score punten",
            color = if (winner) BrandRedInk else Ink,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
        )
    }
}

private fun resultTitle(state: GameUiState): String = when {
    !state.isOnline -> if (state.guessedWord) "Goed gespeeld!" else "Helaas!"
    state.matchResult == MatchResult.WON -> "Gewonnen! 👑"
    state.matchResult == MatchResult.LOST -> "Verloren"
    state.matchResult == MatchResult.TIE -> "Gelijkspel"
    else -> "Uitslag"
}

private fun isMyWin(state: GameUiState): Boolean =
    if (state.isOnline) state.matchResult == MatchResult.WON else state.guessedWord

@Composable
private fun WaitingOverlay() {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            CircularProgressIndicator(color = Color.White)
            Text(
                "Wachten op je tegenstander…",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
            )
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(modifier = Modifier.fillMaxSize().background(Paper), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun MessageScreen(title: String, body: String, onExit: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(Paper).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(1f))
        Text(title, color = Ink, fontWeight = FontWeight.Bold, fontSize = 22.sp)
        Text(body, color = Muted, fontSize = 14.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.weight(1f))
        BrandButton(text = "Naar home", onClick = onExit)
    }
}

private fun headerTitle(number: Int, total: Int, question: Question): String = when (question.type) {
    QuestionType.REGULAR -> "Vraag $number / $total"
    QuestionType.PAARDENSPRONG -> "Vraag $number · Paardensprong"
    QuestionType.TAARTPUZZEL -> "Vraag $number · Taartpuzzel"
}

private fun answerLabel(question: Question): String = when (question.type) {
    QuestionType.REGULAR -> "Jouw antwoord"
    QuestionType.PAARDENSPRONG -> "Antwoord (8 letters)"
    QuestionType.TAARTPUZZEL -> "Ontbrekende letter"
}

private fun formatTime(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}
