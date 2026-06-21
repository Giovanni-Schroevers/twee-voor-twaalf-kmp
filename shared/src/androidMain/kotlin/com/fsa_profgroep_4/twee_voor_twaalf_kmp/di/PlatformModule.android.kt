package com.fsa_profgroep_4.twee_voor_twaalf_kmp.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.data.AppDatabase
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Android needs the application [Context] to locate the database file. The Context
 * is registered into Koin by `MainActivity` (`initKoin { single<Context> { … } }`).
 */
actual val platformModule: Module = module {
    single<RoomDatabase.Builder<AppDatabase>> {
        val context = get<Context>().applicationContext
        val dbFile = context.getDatabasePath("twee_voor_twaalf.db")
        Room.databaseBuilder<AppDatabase>(context = context, name = dbFile.absolutePath)
    }
}
