package com.fsa_profgroep_4.twee_voor_twaalf_kmp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * The single persisted row of game settings. There is only ever one row (keyed by
 * the fixed [SINGLETON_ID]), so writes upsert that same id. Enum values are stored
 * by their `name` and mapped back in [GameSettingsRepository].
 */
@Entity(tableName = "game_settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = SINGLETON_ID,
    val puzzle: String,
    val quizMode: String,
) {
    companion object {
        const val SINGLETON_ID = 0
    }
}
