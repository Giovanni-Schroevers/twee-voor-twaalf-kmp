package com.fsa_profgroep_4.twee_voor_twaalf_kmp.di

import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.mp.KoinPlatformTools

/**
 * Starts Koin once for the whole app.
 *
 * Call this exactly once at each platform's entry point, *before* any UI that
 * resolves dependencies:
 *   - Android  -> [com.fsa_profgroep_4.twee_voor_twaalf_kmp.MainActivity.onCreate]
 *   - Desktop  -> `main()`
 *   - iOS      -> `MainViewController()` (or from Swift in `iOSApp`)
 *
 * [config] lets a platform add bindings it alone can provide — most commonly
 * `androidContext(...)` / `androidLogger()` on Android once you add `koin-android`.
 *
 * The guard makes a second call a no-op, so it's safe even when an entry point
 * is re-created (e.g. iOS view controller re-instantiation or an Android
 * preview), and you won't get a "Koin already started" crash.
 */
fun initKoin(config: KoinAppDeclaration? = null) {
    if (KoinPlatformTools.defaultContext().getOrNull() != null) return
    startKoin {
        config?.invoke(this)
        modules(appModules)
    }
}
