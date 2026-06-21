package com.fsa_profgroep_4.twee_voor_twaalf_kmp.di

import android.content.Context
import org.koin.dsl.module

/**
 * Android entry point for [initKoin]: registers the application [Context] so the
 * platform module can build the Room database from it. Lives in the shared module
 * so the app module doesn't need a direct Koin dependency — `MainActivity` just
 * passes its context.
 */
fun initKoinAndroid(context: Context) = initKoin {
    val appContext = context.applicationContext
    modules(module { single<Context> { appContext } })
}
