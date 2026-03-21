package com.flipwise.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.flipwise.app.ui.components.BottomNavigationBar
import com.flipwise.app.ui.navigation.AppNavigation
import com.flipwise.app.ui.navigation.Screen
import com.flipwise.app.ui.theme.FlipWiseTheme

import com.google.firebase.FirebaseApp
import android.util.Log

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Security: Prevent screenshots and video recording of the app's content
        window.setFlags(
            android.view.WindowManager.LayoutParams.FLAG_SECURE,
            android.view.WindowManager.LayoutParams.FLAG_SECURE
        )

        try {
            FirebaseApp.initializeApp(this)
            Log.d("Firebase", "Firebase initialized successfully")
        } catch (e: Exception) {
            Log.e("Firebase", "Firebase initialization failed", e)
        }

        setContent {
            FlipWiseTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val bottomNavRoutes = setOf(
                    Screen.Home.route,
                    Screen.DeckList.route,
                    Screen.Achievements.route,
                    Screen.StudyTracker.route,
                    Screen.Profile.route,
                    Screen.Leaderboard.route
                )
                val showBottomBar = currentRoute in bottomNavRoutes

                Scaffold(
                    modifier  = Modifier.fillMaxSize(),
                    bottomBar = { if (showBottomBar) BottomNavigationBar(navController) }
                ) { paddingValues ->
                    AppNavigation(
                        navController = navController,
                        modifier      = Modifier.padding(paddingValues)
                    )
                }
            }
        }
    }
}