package com.fsa_profgroep_4.twee_voor_twaalf_kmp.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

/** Reads, writes and clears the single persisted [SessionEntity]. */
@Dao
interface SessionDao {

    /** The saved session, or null when logged out / never logged in. */
    @Query("SELECT * FROM session WHERE id = 0 LIMIT 1")
    suspend fun get(): SessionEntity?

    /** Inserts or replaces the single session row. */
    @Upsert
    suspend fun upsert(entity: SessionEntity)

    /** Removes the saved session (logout). */
    @Query("DELETE FROM session")
    suspend fun clear()
}
