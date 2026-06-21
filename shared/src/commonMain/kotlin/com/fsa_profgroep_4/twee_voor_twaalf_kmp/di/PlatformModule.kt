package com.fsa_profgroep_4.twee_voor_twaalf_kmp.di

import org.koin.core.module.Module

/**
 * Per-platform Koin bindings. Each target provides a
 * `RoomDatabase.Builder<AppDatabase>` here, since building it needs a
 * platform-specific path (and, on Android, the application `Context`). The common
 * [appModule] turns that builder into the database.
 */
expect val platformModule: Module
