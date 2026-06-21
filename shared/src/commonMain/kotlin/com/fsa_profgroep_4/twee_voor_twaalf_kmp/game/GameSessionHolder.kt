package com.fsa_profgroep_4.twee_voor_twaalf_kmp.game

import com.fsa_profgroep_4.twee_voor_twaalf_kmp.network.OnlineSession
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.network.SoloRound

/** A game that's been set up and is ready to play, handed from a lobby to the game screen. */
sealed interface PendingGame {
    val round: SoloRound

    /** Offline single-device play; no server, the client runs the timer locally. */
    data class Solo(override val round: SoloRound) : PendingGame

    /**
     * Online play over the shared [session]; advancing is server-driven. [isHost] lets
     * the game read its own side out of the server's result.
     */
    data class Online(
        override val round: SoloRound,
        val session: OnlineSession,
        val isHost: Boolean,
    ) : PendingGame
}

/**
 * One-shot bridge between a setup screen (offline config / online lobby) and the
 * game screen. The setup side sets [pending] just before navigating; [GameViewModel]
 * consumes it on creation via [take]. A Koin `single`, since it must outlive the
 * screen that produced it.
 */
class GameSessionHolder {
    var pending: PendingGame? = null
        private set

    fun set(game: PendingGame) {
        pending = game
    }

    /** Returns the pending game and clears it, so a back-and-forth doesn't replay it. */
    fun take(): PendingGame? = pending.also { pending = null }
}
