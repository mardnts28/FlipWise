package com.flipwise.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.*
import androidx.navigation.compose.*
import com.flipwise.app.ui.screens.*

sealed class Screen(val route: String) {
    object Splash       : Screen("splash")
    object Onboarding   : Screen("onboarding")
    object Home         : Screen("home")
    object DeckList     : Screen("decks")
    object Achievements : Screen("achievements")
    object StudyTracker : Screen("tracker")
    object Settings     : Screen("settings")
    object Profile      : Screen("profile")
    object DeckDetail   : Screen("deck/{deckId}") {
        fun createRoute(deckId: String) = "deck/$deckId"
    }
    object StudyMode    : Screen("study/{deckId}") {
        fun createRoute(deckId: String) = "study/$deckId"
    }
}

@Composable
fun AppNavigation(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController    = navController,
        startDestination = Screen.Splash.route,
        modifier         = modifier
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(onNavigateNext = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            })
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(onComplete = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Onboarding.route) { inclusive = true }
                }
            })
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToDeck  = { deckId -> navController.navigate(Screen.DeckDetail.createRoute(deckId)) },
                onNavigateToStudy = { deckId ->
                    if (deckId != null) {
                        navController.navigate(Screen.StudyMode.createRoute(deckId))
                    } else {
                        // General study or navigate to deck list
                        navController.navigate(Screen.DeckList.route)
                    }
                },
                onNavigateToDecks = { navController.navigate(Screen.DeckList.route) }
            )
        }

        composable(Screen.DeckList.route) {
            DeckListScreen(
                onNavigateToDeck = { deckId -> navController.navigate(Screen.DeckDetail.createRoute(deckId)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route     = Screen.DeckDetail.route,
            arguments = listOf(navArgument("deckId") { type = NavType.StringType })
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getString("deckId") ?: return@composable
            DeckDetailScreen(
                deckId       = deckId,
                onBack       = { navController.popBackStack() },
                onStartStudy = { navController.navigate(Screen.StudyMode.createRoute(deckId)) }
            )
        }

        composable(
            route     = Screen.StudyMode.route,
            arguments = listOf(navArgument("deckId") { type = NavType.StringType })
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getString("deckId") ?: return@composable
            StudyModeScreen(
                deckId     = deckId,
                onComplete = { navController.popBackStack() },
                onBack     = { navController.popBackStack() }
            )
        }

        composable(Screen.Achievements.route) { 
            AchievementsScreen(onBack = { navController.popBackStack() }) 
        }
        
        composable(Screen.StudyTracker.route) { 
            StudyTrackerScreen(onBack = { navController.popBackStack() }) 
        }

        composable(Screen.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Profile.route) {
            ProfileScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
