package com.js.nowakelock.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.js.nowakelock.base.stringToType
import com.js.nowakelock.data.db.Type
import com.js.nowakelock.ui.components.TopAppBarEvent
import com.js.nowakelock.ui.screens.apps.AppsScreen
import com.js.nowakelock.ui.screens.das.AlarmScreen
import com.js.nowakelock.ui.screens.settings.SettingsScreen
import com.js.nowakelock.ui.screens.das.ServiceScreen
import com.js.nowakelock.ui.screens.das.WakelockScreen
import com.js.nowakelock.ui.screens.dadetail.DADetailScreen

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
                navigateToDADetail = { name, packageName ->
                    navController.navigate(
                        NavRoutes.daDetail(
                            daName = name,
                            packageName = packageName,
                            userId = currentUserId,
                            type = Type.Wakelock
                        )
                    )
                },
                isSearchActive = isSearchActive,
                onSearchActiveChange = onSearchActiveChange,
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchQueryChange
            )
        }

        composable(NavRoutes.ALARMS) {
            AlarmScreen(
                navigateToDADetail = { name, packageName ->
                    navController.navigate(
                        NavRoutes.daDetail(
                            daName = name,
                            packageName = packageName,
                            userId = currentUserId,
                            type = Type.Alarm
                        )
                    )
                },
                isSearchActive = isSearchActive,
                onSearchActiveChange = onSearchActiveChange,
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchQueryChange
            )
        }

        composable(NavRoutes.SERVICES) {
            ServiceScreen(
                navigateToDADetail = { name, packageName ->
                    navController.navigate(
                        NavRoutes.daDetail(
                            daName = name,
                            packageName = packageName,
                            userId = currentUserId,
                            type = Type.Service
                        )
                    )
                },
                isSearchActive = isSearchActive,
                onSearchActiveChange = onSearchActiveChange,
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchQueryChange
            )
        }

        composable(NavRoutes.SETTINGS) {
            SettingsScreen()
        }

        // DA Detail Screen
        composable(
            route = NavRoutes.DA_DETAIL,
            arguments = listOf(
                navArgument("daName") { 
                    type = NavType.StringType 
                },
                navArgument("packageName") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("userId") {
                    type = NavType.IntType
                    defaultValue = 0
                },
                navArgument("type") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val daName = backStackEntry.arguments?.getString("daName") ?: ""
            val packageName = backStackEntry.arguments?.getString("packageName")
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            val typeString = backStackEntry.arguments?.getString("type") ?: Type.UnKnow.value
            val type = stringToType(typeString)

            DADetailScreen(
                daId = daName,
                type = type,
                userId = userId,
                onNavigateBack = { navController.navigateUp() }
            )
        }
    }
} 