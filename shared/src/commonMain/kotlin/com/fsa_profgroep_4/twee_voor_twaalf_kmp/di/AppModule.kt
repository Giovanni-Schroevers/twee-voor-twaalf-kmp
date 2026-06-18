package com.fsa_profgroep_4.twee_voor_twaalf_kmp.di

import com.fsa_profgroep_4.twee_voor_twaalf_kmp.Greeting
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.network.EchoSocket
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.network.ExampleApi
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.network.KtorExampleApi
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.network.createHttpClient
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

    // One shared HttpClient for the whole app (expensive to create, safe to reuse).
    single { createHttpClient() }

    // Bind the ExampleApi interface to its Ktor implementation; `get()` pulls the
    // HttpClient above. Call it from a coroutine: `get<ExampleApi>().firstPost()`.
    single<ExampleApi> { KtorExampleApi(get()) }

    // WebSocket demo, reusing the same HttpClient. Collect it from a coroutine:
    // `get<EchoSocket>().messages().collect { ... }`.
    single { EchoSocket(get()) }
}

/** All modules the app starts with. Add more here as features grow. */
val appModules: List<Module> = listOf(appModule)
