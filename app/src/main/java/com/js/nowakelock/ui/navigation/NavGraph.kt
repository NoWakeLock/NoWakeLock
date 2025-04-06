package com.js.nowakelock.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.js.nowakelock.ui.components.TopAppBarEvent
import com.js.nowakelock.ui.screens.apps.AppsScreen
import com.js.nowakelock.ui.screens.das.AlarmScreen
import com.js.nowakelock.ui.screens.settings.SettingsScreen
import com.js.nowakelock.ui.screens.das.ServiceScreen
import com.js.nowakelock.ui.screens.das.WakelockScreen

/**
 * Main navigation graph for the NoWakeLock app.
 * Handles navigation between main screens and passing shared state/viewmodels.
 */
@Composable
fun NoWakeLockNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    isSearchActive: Boolean = false,
    onSearchActiveChange: (Boolean) -> Unit = {},
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {},
    onTopAppBarEvent: (TopAppBarEvent) -> Unit = {},
    currentUserId: Int = 0
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.APPS,
        modifier = modifier
    ) {
        composable(NavRoutes.APPS) {
            AppsScreen(
                isSearchActive = isSearchActive,
                onSearchActiveChange = onSearchActiveChange,
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchQueryChange,
                onTopAppBarEvent = onTopAppBarEvent,
                currentUserId = currentUserId
            )
        }

        composable(NavRoutes.WAKELOCKS) {
            WakelockScreen(
                isSearchActive = isSearchActive,
                onSearchActiveChange = onSearchActiveChange,
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchQueryChange
            )
        }

        composable(NavRoutes.ALARMS) {
            AlarmScreen(
                isSearchActive = isSearchActive,
                onSearchActiveChange = onSearchActiveChange,
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchQueryChange
            )
        }

        composable(NavRoutes.SERVICES) {
            ServiceScreen(
                isSearchActive = isSearchActive,
                onSearchActiveChange = onSearchActiveChange,
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchQueryChange
            )
        }

        composable(NavRoutes.SETTINGS) {
            SettingsScreen()
        }
    }
} 