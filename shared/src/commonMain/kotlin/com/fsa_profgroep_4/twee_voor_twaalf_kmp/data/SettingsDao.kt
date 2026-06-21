package com.fsa_profgroep_4.twee_voor_twaalf_kmp.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

/** Reads and writes the single [SettingsEntity] row. */
@Dao
interface SettingsDao {

    /** The saved settings, or null when nothing has been saved yet. */
    @Query("SELECT * FROM game_settings WHERE id = 0 LIMIT 1")
    suspend fun get(): SettingsEntity?

    /** Inserts or replaces the single settings row. */
    @Upsert
    suspend fun upsert(entity: SettingsEntity)
}
