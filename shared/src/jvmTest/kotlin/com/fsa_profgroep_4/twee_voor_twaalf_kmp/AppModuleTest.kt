package com.fsa_profgroep_4.twee_voor_twaalf_kmp

import com.fsa_profgroep_4.twee_voor_twaalf_kmp.di.appModule
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.test.verify.verify
import kotlin.test.Test

/**
 * Closes Koin's main gap as a *runtime* DI framework: a missing binding normally
 * only blows up when the app actually requests it. `verify()` walks every
 * definition in the module and checks each constructor parameter can be
 * resolved, turning that runtime crash into a failing test in CI.
 *
 * Run with: `./gradlew :shared:jvmTest`
 */
class AppModuleTest {

    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun appModule_dependencyGraphResolves() {
        appModule.verify()
    }
}
