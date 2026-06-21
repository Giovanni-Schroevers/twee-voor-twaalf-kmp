package com.fsa_profgroep_4.twee_voor_twaalf_kmp

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.data.AppDatabase
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.data.GameSettingsRepository
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.network.PuzzlePreference
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.network.QuizMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Proves the game settings actually persist to disk via Room: write them through a
 * [GameSettingsRepository] on one database instance, then reopen the same file and
 * read them back.
 */
class GameSettingsRepositoryTest {

    private val dbFile: File = File.createTempFile("game-settings-test", ".db")

    private fun openDatabase(): AppDatabase =
        Room.databaseBuilder<AppDatabase>(name = dbFile.absolutePath)
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.Default)
            .build()

    @AfterTest
    fun cleanUp() {
        dbFile.delete()
    }

    @Test
    fun savedSettings_surviveReopen() = runBlocking {
        val db = openDatabase()
        val repo = GameSettingsRepository(db.settingsDao())

        repo.setPuzzle(PuzzlePreference.PAARDENSPRONG)
        repo.setQuizMode(QuizMode.DIFFERENT)

        // Wait until the write-through has actually landed in the database file.
        withTimeout(5_000) {
            while (true) {
                val row = db.settingsDao().get()
                if (row?.puzzle == "PAARDENSPRONG" && row.quizMode == "DIFFERENT") break
                delay(25)
            }
        }
        db.close()

        // Reopen the same file — the values must still be there.
        val reopened = openDatabase()
        val row = reopened.settingsDao().get()
        assertEquals("PAARDENSPRONG", row?.puzzle)
        assertEquals("DIFFERENT", row?.quizMode)
        reopened.close()
    }
}
