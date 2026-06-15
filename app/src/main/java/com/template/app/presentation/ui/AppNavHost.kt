package com.template.app.presentation.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.template.app.presentation.ui.screens.DashboardScreen
import com.template.app.presentation.ui.screens.OnboardingScreen
import com.template.app.presentation.ui.screens.UserDetailScreen
import com.template.app.presentation.ui.screens.UsersScreen

// ──── Route constants ─────────────────────────────────────────────────────────
object Routes {
    const val ONBOARDING = "onboarding"
    const val DASHBOARD = "dashboard"
    const val USERS = "users"
    const val USER_DETAIL = "user/{userId}"

    fun userDetail(userId: String) = "user/$userId"
}

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.ONBOARDING
    ) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen()
        }

        composable(Routes.DASHBOARD) {
            DashboardScreen()
        }

        composable(Routes.USERS) {
            UsersScreen(
                onNavigateToDetail = { userId ->
                    navController.navigate(Routes.userDetail(userId))
                }
            )
        }

        composable(
            route = Routes.USER_DETAIL,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
            UserDetailScreen(
                userId = userId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
