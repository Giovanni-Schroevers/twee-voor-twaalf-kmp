package com.fsa_profgroep_4.twee_voor_twaalf_kmp.data

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor

/**
 * The app's Room database. Persists game settings and the login session; gameplay
 * history (scores etc.) can add more entities here later.
 *
 * The platform-specific builders live in each source set's `platformModule`
 * (see `di/PlatformModule.*`), since the builder needs a platform path/Context.
 *
 * Schema changes bump [version]; the builder uses destructive migration (see
 * `appModule`), so an older on-disk database is simply recreated.
 */
@Database(entities = [SettingsEntity::class, SessionEntity::class], version = 2)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun settingsDao(): SettingsDao
    abstract fun sessionDao(): SessionDao
}

/**
 * Room generates the actual implementation of this constructor for each platform
 * (so there is intentionally no hand-written `actual`).
 */
@Suppress("NO_ACTUAL_FOR_EXPECT", "KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
