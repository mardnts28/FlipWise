package com.flipwise.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.*
import androidx.navigation.compose.*
import com.flipwise.app.ui.screens.*
import com.flipwise.app.viewmodel.ProfileViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

sealed class Screen(val route: String) {
    object Splash       : Screen("splash")
    object Onboarding   : Screen("onboarding")
    object Home         : Screen("home")
    object DeckList     : Screen("decks")
    object Achievements : Screen("achievements")
    object StudyTracker : Screen("tracker")
    object Settings     : Screen("settings")
    object Profile      : Screen("profile")
    object Login        : Screen("login")
    object Register     : Screen("register")
    object RegisterSuccess : Screen("register_success/{email}") {
        fun createRoute(email: String) = "register_success/$email"
    }
    object CompleteProfile : Screen("complete_profile")
    object Leaderboard  : Screen("leaderboard")
    object DeckDetail   : Screen("deck/{deckId}") {
        fun createRoute(deckId: String) = "deck/$deckId"
    }
    object StudyMode    : Screen("study/{deckId}") {
        fun createRoute(deckId: String) = "study/$deckId"
    }
    object ChallengeDetail : Screen("challenge/{challengeId}") {
        fun createRoute(challengeId: String) = "challenge/$challengeId"
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
            val scope = androidx.compose.runtime.rememberCoroutineScope()
            val profileViewModel: ProfileViewModel = viewModel()
            SplashScreen(onNavigateNext = {
                scope.launch {
                    if (profileViewModel.isUserLoggedIn) {
                        val profile = profileViewModel.syncProfile()
                        if (profile == null || profile.username == "flipper" || profile.username.isBlank()) {
                            navController.navigate(Screen.CompleteProfile.route) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        } else {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        }
                    } else {
                        // User not logged in, show onboarding first
                        navController.navigate(Screen.Onboarding.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                }
            })
        }

        composable(Screen.Login.route) {
            val scope = androidx.compose.runtime.rememberCoroutineScope()
            val profileViewModel: ProfileViewModel = viewModel()
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onLoginSuccess = {
                    scope.launch {
                        val profile = profileViewModel.fullSync()
                        // Check if username (nickname) is set to default "flipper" or blank
                        if (profile == null || profile.username == "flipper" || profile.username.isBlank()) {
                            navController.navigate(Screen.CompleteProfile.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        } else {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                    }
                }
            )
        }

        composable(Screen.CompleteProfile.route) {
            CompleteProfileScreen(
                onComplete = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.CompleteProfile.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                onRegisterSuccess = { email ->
                    navController.navigate(Screen.RegisterSuccess.createRoute(email)) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.RegisterSuccess.route,
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            RegisterSuccessScreen(
                email = email,
                onContinueToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.RegisterSuccess.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(onComplete = {
                // After onboarding, navigate to Login
                navController.navigate(Screen.Login.route) {
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
                onNavigateToDecks = { navController.navigate(Screen.DeckList.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
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
            val profileViewModel: ProfileViewModel = viewModel()
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onLogout = {
                    profileViewModel.signOut()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onAccountDeleted = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToChallenge = { id -> navController.navigate(Screen.ChallengeDetail.createRoute(id)) }
            )
        }

        composable(Screen.Leaderboard.route) {
            LeaderboardScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route     = Screen.ChallengeDetail.route,
            arguments = listOf(navArgument("challengeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("challengeId") ?: return@composable
            ChallengeDetailScreen(challengeId = id, onBack = { navController.popBackStack() })
        }
    }
}
