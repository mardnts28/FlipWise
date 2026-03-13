package com.flipwise.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.flipwise.app.ui.navigation.Screen
import com.flipwise.app.ui.theme.*

sealed class BottomNavItem(
    val route: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: String
) {
    object Home    : BottomNavItem(Screen.Home.route,         Icons.Default.Home,                "Home")
    object Decks   : BottomNavItem(Screen.DeckList.route,     Icons.Default.CollectionsBookmark, "Decks")
    object Awards  : BottomNavItem(Screen.Achievements.route, Icons.Default.EmojiEvents,         "Awards")
    object Tracker : BottomNavItem(Screen.StudyTracker.route, Icons.Default.BarChart,            "Tracker")
    object Profile : BottomNavItem(Screen.Profile.route,      Icons.Default.Person,              "Profile")
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Decks,
        BottomNavItem.Awards,
        BottomNavItem.Tracker,
        BottomNavItem.Profile
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = Color.White,
        contentColor   = NavyInk,
        tonalElevation = 8.dp
    ) {
        items.forEach { item ->
            NavigationBarItem(
                icon     = { Icon(item.icon, contentDescription = item.label) },
                label    = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick  = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState    = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor   = GrapePop,
                    selectedTextColor   = GrapePop,
                    unselectedIconColor = NavyInk60,
                    unselectedTextColor = NavyInk60,
                    indicatorColor      = GrapePop20
                )
            )
        }
    }
}
