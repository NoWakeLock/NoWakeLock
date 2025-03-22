package com.js.nowakelock.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
//import androidx.compose.material.icons.filled.BoltOn
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.js.nowakelock.R
import com.js.nowakelock.ui.navigation.NavRoutes

data class BottomNavItem(
    val route: String,
    val titleResId: Int,
    val icon: @Composable () -> Unit
)

@Composable
fun NoWakeLockBottomNavBar(navController: NavController) {
    val items = listOf(
        BottomNavItem(
            route = NavRoutes.APPS,
            titleResId = R.string.Apps,
            icon = { Icon(Icons.Default.Apps, contentDescription = null) }
        ),
        BottomNavItem(
            route = NavRoutes.WAKELOCKS,
            titleResId = R.string.WakeLock,
            icon = { Icon(Icons.Default.Lock, contentDescription = null) }
        ),
        BottomNavItem(
            route = NavRoutes.ALARMS,
            titleResId = R.string.Alarm,
            icon = { Icon(Icons.Default.Alarm, contentDescription = null) }
        ),
        BottomNavItem(
            route = NavRoutes.SERVICES,
            titleResId = R.string.Service,
            icon = { Icon(Icons.Default.Build, contentDescription = null) }
        ),
        BottomNavItem(
            route = NavRoutes.SETTINGS,
            titleResId = R.string.settings,
            icon = { Icon(Icons.Default.Settings, contentDescription = null) }
        )
    )

    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry.value?.destination

    NavigationBar {
        items.forEach { item ->
            val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
            NavigationBarItem(
                icon = item.icon,
                label = { Text(stringResource(id = item.titleResId)) },
                selected = selected,
                onClick = {
                    if (!selected) {
                        navController.navigate(item.route) {
                            // 避免创建多个堆栈
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            // 避免多次点击创建多个实例
                            launchSingleTop = true
                            // 恢复状态
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
} 