package com.js.nowakelock.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
//import androidx.navigation.compose.navArgument
import androidx.navigation.toRoute
import com.js.nowakelock.R
import com.js.nowakelock.base.stringToType
import com.js.nowakelock.data.db.Type
import com.js.nowakelock.ui.components.TopAppBarEvent
import com.js.nowakelock.ui.screens.apps.AppsScreen
import com.js.nowakelock.ui.screens.das.AlarmScreen
import com.js.nowakelock.ui.screens.settings.SettingsScreen
import com.js.nowakelock.ui.screens.das.ServiceScreen
import com.js.nowakelock.ui.screens.das.WakelockScreen
import com.js.nowakelock.ui.screens.dadetail.DADetailScreen
import kotlinx.coroutines.launch

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
                        DADetail(
                            daName = name,
                            packageName = packageName,
                            userId = currentUserId,
                            type = Type.Wakelock.value
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
                        DADetail(
                            daName = name,
                            packageName = packageName,
                            userId = currentUserId,
                            type = Type.Alarm.value
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
                        DADetail(
                            daName = name,
                            packageName = packageName,
                            userId = currentUserId,
                            type = Type.Service.value
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
        composable<DADetail> { backStackEntry ->
            val daDetail = backStackEntry.toRoute<DADetail>()
            val type = stringToType(daDetail.type)

            val title = when (type) {
                Type.Wakelock -> stringResource(id = R.string.WakeLock)
                Type.Alarm -> stringResource(id = R.string.Alarm)
                Type.Service -> stringResource(id = R.string.Service)
                else -> stringResource(id = R.string.app_name)
            }

            LaunchedEffect(type) {
                onTopAppBarEvent(TopAppBarEvent.SetDetailTitle(title))
            }

            DADetailScreen(
                daId = daDetail.daName,
                type = type,
                userId = daDetail.userId,
                onNavigateBack = {
                    // Clear detail title when navigating away
                    onTopAppBarEvent(TopAppBarEvent.ClearDetailTitle)
                    navController.navigateUp()
                }
            )
        }
    }
} 