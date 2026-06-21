package com.fsa_profgroep_4.twee_voor_twaalf_kmp

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.data.AppDatabase
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.data.SessionStore
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.network.UserDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** Proves the login session (JWT + user) persists to disk and clears on logout. */
class SessionStoreTest {

    private val dbFile: File = File.createTempFile("session-test", ".db")

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
    fun session_surviveReopen_andClears() = runBlocking {
        val user = UserDto(id = 7u, username = "quizkoning", email = "q@example.com", avatar = "avatars/7.png")

        val db = openDatabase()
        SessionStore(db.sessionDao()).save(token = "jwt-abc-123", user = user)
        db.close()

        // Reopen: the session must still be there, with the same token and user.
        val reopenedDb = openDatabase()
        val store = SessionStore(reopenedDb.sessionDao())
        val restored = store.load()
        assertEquals("jwt-abc-123", restored?.token)
        assertEquals(user, restored?.user)

        // Logout clears it.
        store.clear()
        assertNull(store.load())
        reopenedDb.close()
    }
}
