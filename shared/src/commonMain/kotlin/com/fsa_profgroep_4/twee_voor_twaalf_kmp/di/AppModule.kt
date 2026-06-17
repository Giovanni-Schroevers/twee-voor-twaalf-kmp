package com.fsa_profgroep_4.twee_voor_twaalf_kmp.di

import com.fsa_profgroep_4.twee_voor_twaalf_kmp.Greeting
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * The single source of truth for how dependencies are built and wired together.
 * Add new bindings here as the app grows.
 *
 * Common builders you'll reach for:
 *   - `single { ... }`   one shared instance for the whole app
 *   - `factory { ... }`  a fresh instance every time it's requested
 *
 * Inside a builder, call `get()` to pull in another dependency.
 */
val appModule: Module = module {
    // Demonstration binding only: instead of `Greeting()` being constructed by
    // hand in the UI, Koin provides it. Swap this for real dependencies later.
    single { Greeting() }
}

/** All modules the app starts with. Add more here as features grow. */
val appModules: List<Module> = listOf(appModule)
