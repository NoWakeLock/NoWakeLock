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
import com.js.nowakelock.ui.screens.appdetail.AppDetailScreen
import com.js.nowakelock.ui.screens.modulecheck.ModuleCheckScreen
import com.js.nowakelock.ui.screens.modulecheck.ModuleCheckViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

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
        startDestination = Apps(),
        modifier = modifier
    ) {
        // 使用类型导航
        composable<Apps> { backStackEntry ->
            val appsParams = backStackEntry.toRoute<Apps>()
            
            AppsScreen(
                isSearchActive = isSearchActive,
                onSearchActiveChange = onSearchActiveChange,
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchQueryChange,
                onTopAppBarEvent = onTopAppBarEvent,
                currentUserId = currentUserId,  // 使用外部传入的值
                navigateToAppDetail = { packageName, label ->
                    navController.navigate(
                        AppDetail(
                            packageName = packageName, 
                            userId = currentUserId, 
                            label = label
                        )
                    )
                }
            )
        }

        composable<Wakelocks> { backStackEntry ->
            val params = backStackEntry.toRoute<Wakelocks>()
            
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
                onSearchQueryChange = onSearchQueryChange,
                packageName = params.packageName,
                userId = params.userId,
                onTopAppBarEvent = onTopAppBarEvent
            )
        }

        composable<Alarms> { backStackEntry ->
            val params = backStackEntry.toRoute<Alarms>()
            
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
                onSearchQueryChange = onSearchQueryChange,
                packageName = params.packageName,
                userId = params.userId,
                onTopAppBarEvent = onTopAppBarEvent
            )
        }

        composable<Services> { backStackEntry ->
            val params = backStackEntry.toRoute<Services>()
            
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
                onSearchQueryChange = onSearchQueryChange,
                packageName = params.packageName,
                userId = params.userId,
                onTopAppBarEvent = onTopAppBarEvent
            )
        }

        composable(NavRoutes.SETTINGS) {
            SettingsScreen(navController = navController)
        }

        composable(NavRoutes.MODULE_CHECK) {
            // Get the viewModel for the screen to access its methods later
            val moduleCheckViewModel: ModuleCheckViewModel = koinViewModel()
            val temp = stringResource(id = R.string.module_check)
            
            // Set the detail title when navigating to this screen
            LaunchedEffect(Unit) {
                onTopAppBarEvent(TopAppBarEvent.SetDetailTitle(temp))
            }
            
            // Override the global RefreshClicked handler when on this screen
            LaunchedEffect(Unit) {
                val refreshHandler: (TopAppBarEvent) -> Unit = { event ->
                    if (event is TopAppBarEvent.RefreshClicked) {
                        // Call the viewModel's refresh method
                        moduleCheckViewModel.checkModuleStatus()
                    }
                    // Forward the event to the original handler
                    onTopAppBarEvent(event)
                }
                
                // Update the TopAppBar refresh action
                onTopAppBarEvent(TopAppBarEvent.RefreshClicked)
            }
            
            ModuleCheckScreen(
                onBackClick = {
                    // Clear detail title when navigating away
                    onTopAppBarEvent(TopAppBarEvent.ClearDetailTitle)
                    navController.navigateUp()
                },
                viewModel = moduleCheckViewModel
            )
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

        // App Detail Screen
        composable<AppDetail> { backStackEntry ->
            val appDetail = backStackEntry.toRoute<AppDetail>()
            val label = appDetail.label
            LaunchedEffect(label) {
                onTopAppBarEvent(TopAppBarEvent.SetDetailTitle(label))
            }

            AppDetailScreen(
                packageName = appDetail.packageName,
                userId = appDetail.userId,
                onNavigateBack = {
                    // Clear detail title when navigating away
                    onTopAppBarEvent(TopAppBarEvent.ClearDetailTitle)
                    navController.navigateUp()
                },
                navController = navController,
                isSearchActive = isSearchActive,
                searchQuery = searchQuery
            )
        }
    }
} 