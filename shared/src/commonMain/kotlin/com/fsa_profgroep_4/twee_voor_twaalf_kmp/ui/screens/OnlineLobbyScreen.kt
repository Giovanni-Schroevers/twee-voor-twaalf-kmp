package com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.game.LobbyRole
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.game.OnlineLobbyUiState
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.game.OnlineLobbyViewModel
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.network.BackendUrlProvider
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.network.PlayerProfile
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.network.PuzzlePreference
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.network.QuizMode
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.components.Avatar
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.components.BrandButton
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.components.BrandButtonStyle
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.components.BrandTopBar
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.components.ConnectionChip
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.components.Dropdown
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.components.FormMessage
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.components.LabeledField
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.components.Segmented
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.theme.BrandRed
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.theme.BrandRedInk
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.theme.BrandRedSoft
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.theme.Danger
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.theme.Ink
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.theme.InkSoft
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.theme.Line
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.theme.LineSoft
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.theme.Muted
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.theme.Paper
import org.koin.compose.koinInject

/**
 * Online lobby — wireframe `ConfigOnline`. Opening the screen auto-hosts (the
 * websocket opens and a join code appears); entering a code and tapping Join
 * switches this device to a guest of that lobby, hiding the host-only controls.
 * Starting the match is a later slice, so the Start button is inert for now.
 */
@Composable
fun OnlineLobbyScreen(
    onBack: () -> Unit,
    onOpenAccount: () -> Unit,
) {
    val viewModel = koinInject<OnlineLobbyViewModel>()
    val urlProvider = koinInject<BackendUrlProvider>()
    val state by viewModel.state.collectAsState()

    // ViewModels here come from Koin, not a ViewModelStore, so close the socket
    // ourselves when the lobby leaves the composition (e.g. on back).
    DisposableEffect(Unit) {
        onDispose { viewModel.disconnect() }
    }

    Column(modifier = Modifier.fillMaxSize().background(Paper)) {
        BrandTopBar(title = "Online spel", flat = true, onBack = onBack)

        if (state.loggedOut) {
            LoggedOutPrompt(onOpenAccount)
            return@Column
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            state.error?.let { FormMessage(text = it, color = Danger) }

            when (state.role) {
                LobbyRole.HOST -> HostContent(state, viewModel, urlProvider)
                LobbyRole.GUEST -> GuestContent(state, urlProvider)
            }
        }
    }
}

@Composable
private fun HostContent(
    state: OnlineLobbyUiState,
    viewModel: OnlineLobbyViewModel,
    urlProvider: BackendUrlProvider,
) {
    // Your code, big and shareable.
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(BrandRedSoft)
            .border(1.5.dp, BrandRed, shape)
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            "JOUW SPELCODE",
            color = BrandRedInk,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
        )
        Text(
            text = state.code ?: "····",
            color = BrandRedInk,
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 8.sp,
        )
        Text("Deel met je tegenstander", color = InkSoft, fontSize = 12.sp)
    }

    Dropdown(
        label = "Mini-puzzel",
        options = PuzzlePreference.entries,
        selected = state.puzzle,
        optionLabel = { it.label },
        onSelect = viewModel::onPuzzleChange,
    )

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Quiz voor beide teams", color = InkSoft, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Segmented(
            options = listOf("Zelfde quiz", "Andere quiz"),
            selectedIndex = if (state.quizMode == QuizMode.SAME) 0 else 1,
            onSelect = { viewModel.onQuizModeChange(if (it == 0) QuizMode.SAME else QuizMode.DIFFERENT) },
        )
    }

    // Or join someone else's game instead.
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Of doe mee met een ander spel", color = InkSoft, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            LabeledField(
                label = "Code",
                value = state.joinCodeInput,
                onValueChange = viewModel::onJoinCodeChange,
                modifier = Modifier.weight(1f),
            )
            BrandButton(
                text = "Join",
                onClick = viewModel::join,
                style = BrandButtonStyle.Outlined,
                modifier = Modifier,
            )
        }
    }

    PlayersCard {
        PlayerRow(
            name = state.youName,
            sub = "jij · host",
            avatarUrl = urlProvider.avatarUrl(state.youAvatar),
            trailing = { ConnectionChip(connected = state.connected, text = "verbonden") },
        )
        Divider()
        OpponentRow(state.opponent, urlProvider)
    }

    BrandButton(
        text = "Start spel",
        onClick = { /* gameplay is the next slice */ },
    )
}

@Composable
private fun GuestContent(
    state: OnlineLobbyUiState,
    urlProvider: BackendUrlProvider,
) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(BrandRedSoft)
            .border(1.5.dp, BrandRed, shape)
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text("Je doet mee met", color = InkSoft, fontSize = 12.sp)
        Text(
            text = state.code ?: "",
            color = BrandRedInk,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 6.sp,
        )
    }

    PlayersCard {
        // The host is our opponent in guest mode.
        OpponentRow(state.opponent, urlProvider, subOverride = "host")
        Divider()
        PlayerRow(
            name = state.youName,
            sub = "jij · tegenstander",
            avatarUrl = urlProvider.avatarUrl(state.youAvatar),
            trailing = { ConnectionChip(connected = state.connected, text = "verbonden") },
        )
    }

    Text(
        "Wacht tot de host het spel start.",
        color = Muted,
        fontSize = 13.sp,
        modifier = Modifier.fillMaxWidth(),
    )
}

/** Bordered card wrapping the two player rows. */
@Composable
private fun PlayersCard(content: @Composable () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.5.dp, Line, shape)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        content()
    }
}

@Composable
private fun PlayerRow(
    name: String,
    sub: String,
    avatarUrl: String?,
    trailing: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            Avatar(size = 36.dp, initial = name.firstOrNull(), imageUrl = avatarUrl)
            Column {
                Text(name, color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(sub, color = Muted, fontSize = 11.sp)
            }
        }
        trailing()
    }
}

/** Opponent row: their profile when present, otherwise a "waiting" placeholder. */
@Composable
private fun OpponentRow(
    opponent: PlayerProfile?,
    urlProvider: BackendUrlProvider,
    subOverride: String? = null,
) {
    if (opponent == null) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                Avatar(size = 36.dp)
                Text("Wachten op speler…", color = Muted, fontSize = 14.sp)
            }
            ConnectionChip(connected = false, text = "wacht")
        }
    } else {
        PlayerRow(
            name = opponent.username,
            sub = subOverride ?: "tegenstander",
            avatarUrl = urlProvider.avatarUrl(opponent.avatar),
            trailing = { ConnectionChip(connected = true, text = "verbonden") },
        )
    }
}

@Composable
private fun Divider() {
    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(LineSoft))
}

/** Shown when no one is signed in — online play needs an account. */
@Composable
private fun LoggedOutPrompt(onOpenAccount: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            "Log in om online te spelen",
            color = Ink,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
        )
        Text(
            "Je hebt een account nodig zodat je tegenstander ziet wie je bent.",
            color = Muted,
            fontSize = 13.sp,
        )
        BrandButton(text = "Naar account", onClick = onOpenAccount)
    }
}
