package com.fsa_profgroep_4.twee_voor_twaalf_kmp.di

import com.fsa_profgroep_4.twee_voor_twaalf_kmp.Greeting
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.auth.AccountViewModel
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.auth.AuthRepository
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.auth.ChangePasswordViewModel
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.auth.LoginViewModel
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.auth.RegisterViewModel
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.auth.SettingsViewModel
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.network.AuthApi
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.network.AuthTokenStore
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.network.BackendUrlProvider
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.network.EchoSocket
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.network.ExampleApi
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.network.KtorAuthApi
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

    // Holds auth tokens for the bearer flow. In-memory for now (see AuthTokenStore).
    // Login writes to it via `get<AuthTokenStore>().update(access, refresh)`.
    single { AuthTokenStore() }

    // Holds the backend base URL and lets it be changed at runtime (Settings
    // screen). The HttpClient below reads it per request. In-memory like the
    // AuthTokenStore — resets to the build-time default on restart.
    single { BackendUrlProvider() }

    // One shared HttpClient for the whole app (expensive to create, safe to reuse).
    // `get()` supplies the AuthTokenStore (bearer token) and BackendUrlProvider
    // (base URL read per request).
    single { createHttpClient(get(), get()) }

    // Bind the ExampleApi interface to its Ktor implementation; `get()` pulls the
    // HttpClient above. Call it from a coroutine: `get<ExampleApi>().firstPost()`.
    single<ExampleApi> { KtorExampleApi(get()) }

    // WebSocket demo, reusing the same HttpClient. Collect it from a coroutine:
    // `get<EchoSocket>().messages().collect { ... }`.
    single { EchoSocket(get()) }

    // --- auth / user flow ---
    // The backend's user endpoints (register/login/update/delete), behind the
    // AuthApi interface so callers and tests can swap it.
    single<AuthApi> { KtorAuthApi(get()) }

    // Single source of truth for the signed-in user + token. UI observes its
    // `currentUser` StateFlow; ViewModels call its login/register/etc.
    single { AuthRepository(get(), get(), get()) }

    // Screen ViewModels. `factory` hands a fresh instance to each screen that
    // resolves it via koinInject(); all share the single AuthRepository above.
    factory { LoginViewModel(get()) }
    factory { RegisterViewModel(get()) }
    factory { AccountViewModel(get(), get()) }
    factory { SettingsViewModel(get(), get()) }
    factory { ChangePasswordViewModel(get()) }
}

/** All modules the app starts with. Add more here as features grow. */
val appModules: List<Module> = listOf(appModule)
