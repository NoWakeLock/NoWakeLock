package com.js.nowakelock.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.js.nowakelock.ui.screens.alarms.AlarmsScreen
import com.js.nowakelock.ui.screens.apps.AppsScreen
import com.js.nowakelock.ui.screens.services.ServicesScreen
import com.js.nowakelock.ui.screens.settings.SettingsScreen
import com.js.nowakelock.ui.screens.wakelocks.WakelocksScreen
import com.js.nowakelock.ui.screens.wakelocks.WakelocksViewModel

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
    wakelocksViewModel: WakelocksViewModel? = null
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
                onSearchQueryChange = onSearchQueryChange
            )
        }
        composable(NavRoutes.WAKELOCKS) {
            // Pass the ViewModel from parent to avoid duplicate ViewModel instances
            wakelocksViewModel?.let {
                WakelocksScreen(viewModel = it)
            } ?: WakelocksScreen()
        }
        composable(NavRoutes.ALARMS) {
            AlarmsScreen()
        }
        composable(NavRoutes.SERVICES) {
            ServicesScreen()
        }
        composable(NavRoutes.SETTINGS) {
            SettingsScreen()
        }
    }
} 