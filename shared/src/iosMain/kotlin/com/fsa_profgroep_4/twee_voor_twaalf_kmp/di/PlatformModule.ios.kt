package com.fsa_profgroep_4.twee_voor_twaalf_kmp.di

import androidx.room.Room
import androidx.room.RoomDatabase
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.data.AppDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

/** iOS stores the database in the app's Documents directory. */
@OptIn(ExperimentalForeignApi::class)
actual val platformModule: Module = module {
    single<RoomDatabase.Builder<AppDatabase>> {
        val documents = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = true,
            error = null,
        )
        val path = requireNotNull(documents?.path) { "Could not resolve iOS Documents directory" }
        Room.databaseBuilder<AppDatabase>(name = "$path/twee_voor_twaalf.db")
    }
}
