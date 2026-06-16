package com.template.app.presentation.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.template.app.presentation.ui.screens.*

// ──── Route constants ─────────────────────────────────────────────────────────
object Routes {
    const val ONBOARDING = "onboarding"
    const val MAIN = "main" // Container for the 5-tab UI

    // Primary Tabs
    const val DASHBOARD = "dashboard"
    const val DISPLAY = "display"
    const val AUDIO = "audio"
    const val NETWORK = "network"
    const val MEDIA = "media"

    // More Menu Screens
    const val FILES = "files"
    const val PROCESSES = "processes"
    const val SECURITY = "security"
    const val SCHEDULER = "scheduler"
    const val MAINTENANCE = "maintenance"
    const val POWER = "power"
    const val CLIPBOARD = "clipboard"
    const val INPUT_CONTROL = "input_control"
    const val NOTIFICATIONS = "notifications"
    const val SETTINGS = "settings"

    const val USERS = "users"
    const val USER_DETAIL = "user/{userId}"

    fun userDetail(userId: String) = "user/$userId"
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String = Routes.ONBOARDING
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onOnboardingComplete = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.MAIN) {
            MainScreen(
                onLogout = {
                    navController.navigate(Routes.ONBOARDING) {
                        popUpTo(Routes.MAIN) { inclusive = true }
                    }
                }
            )
        }

    }
}
