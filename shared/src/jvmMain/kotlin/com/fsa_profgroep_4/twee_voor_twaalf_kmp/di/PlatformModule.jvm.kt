package com.fsa_profgroep_4.twee_voor_twaalf_kmp.di

import androidx.room.Room
import androidx.room.RoomDatabase
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.data.AppDatabase
import org.koin.core.module.Module
import org.koin.dsl.module
import java.io.File

/** Desktop stores the database under the user's home directory so it persists. */
actual val platformModule: Module = module {
    single<RoomDatabase.Builder<AppDatabase>> {
        val dir = File(System.getProperty("user.home"), ".tweevoortwaalf").apply { mkdirs() }
        val dbFile = File(dir, "app.db")
        Room.databaseBuilder<AppDatabase>(name = dbFile.absolutePath)
    }
}
