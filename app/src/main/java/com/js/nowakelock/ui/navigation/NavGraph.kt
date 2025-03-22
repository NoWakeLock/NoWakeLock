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

@Composable
fun NoWakeLockNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.APPS,
        modifier = modifier
    ) {
        composable(NavRoutes.APPS) {
            AppsScreen()
        }
        composable(NavRoutes.WAKELOCKS) {
            WakelocksScreen()
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