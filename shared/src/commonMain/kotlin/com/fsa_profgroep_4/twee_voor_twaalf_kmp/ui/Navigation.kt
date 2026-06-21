package com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.screens.AccountScreen
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.screens.ChangePasswordScreen
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.screens.HomeScreen
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.screens.LoginScreen
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.screens.OfflineConfigScreen
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.screens.OnlineLobbyScreen
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.screens.RegisterScreen
import com.fsa_profgroep_4.twee_voor_twaalf_kmp.ui.screens.SettingsScreen
import kotlinx.serialization.Serializable

/* The destinations in the app. Each is a NavKey so it can live on the
   Navigation3 back stack; @Serializable keeps them ready for state saving. */
@Serializable
data object HomeKey : NavKey

@Serializable
data object LoginKey : NavKey

@Serializable
data object RegisterKey : NavKey

@Serializable
data object AccountKey : NavKey

@Serializable
data object SettingsKey : NavKey

@Serializable
data object ChangePasswordKey : NavKey

@Serializable
data object OfflineConfigKey : NavKey

@Serializable
data object OnlineLobbyKey : NavKey

/**
 * Hosts the app's navigation. A mutable back stack starts at [HomeKey]; screens
 * push/pop keys through the callbacks below. After a successful login/register we
 * drop the auth screens so the user lands back on whatever opened them (usually
 * Account, which then renders its signed-in state).
 */
@Composable
fun AppNavHost() {
    val backStack = remember { mutableStateListOf<NavKey>(HomeKey) }

    fun goTo(key: NavKey) {
        if (backStack.lastOrNull() != key) backStack.add(key)
    }

    fun pop() {
        if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
    }

    fun finishAuth() {
        backStack.removeAll { it == LoginKey || it == RegisterKey }
        if (backStack.isEmpty()) backStack.add(HomeKey)
    }

    NavDisplay(
        backStack = backStack,
        onBack = { pop() },
        entryProvider = entryProvider {
            entry<HomeKey> {
                HomeScreen(
                    onOpenAccount = { goTo(AccountKey) },
                    onOpenSettings = { goTo(SettingsKey) },
                    onOfflineGame = { goTo(OfflineConfigKey) },
                    onOnlineGame = { goTo(OnlineLobbyKey) },
                )
            }
            entry<LoginKey> {
                LoginScreen(
                    onBack = { pop() },
                    onNavigateToRegister = { goTo(RegisterKey) },
                    onLoggedIn = { finishAuth() },
                )
            }
            entry<RegisterKey> {
                RegisterScreen(
                    onBack = { pop() },
                    onRegistered = { finishAuth() },
                )
            }
            entry<AccountKey> {
                AccountScreen(
                    onBack = { pop() },
                    onNavigateToLogin = { goTo(LoginKey) },
                    onNavigateToRegister = { goTo(RegisterKey) },
                    onChangePassword = { goTo(ChangePasswordKey) },
                )
            }
            entry<SettingsKey> {
                SettingsScreen(onBack = { pop() })
            }
            entry<ChangePasswordKey> {
                ChangePasswordScreen(onBack = { pop() })
            }
            entry<OfflineConfigKey> {
                OfflineConfigScreen(onBack = { pop() })
            }
            entry<OnlineLobbyKey> {
                OnlineLobbyScreen(
                    onBack = { pop() },
                    onOpenAccount = { goTo(AccountKey) },
                )
            }
        },
    )
}
