package com.js.nowakelock.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
//import androidx.compose.material.icons.filled.BoltOn
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.twotone.Apps
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.js.nowakelock.R
import com.js.nowakelock.ui.navigation.*
import com.js.nowakelock.ui.screens.apps.FilterOption
import com.js.nowakelock.ui.screens.apps.SortOption
import com.js.nowakelock.ui.screens.das.DAFilterOption
import com.js.nowakelock.ui.screens.das.DASortOption

sealed class BottomNavItem(
    val route: String, 
    val titleResId: Int, 
    val icon: @Composable () -> Unit,
    val createNavRoute: () -> Any
)

class AppsNavItem(currentUserId: Int = 0) : BottomNavItem(
    route = NavRoutes.APPS,
    titleResId = R.string.Apps,
    icon = { Icon(Icons.Rounded.Apps, contentDescription = null) },
    createNavRoute = { 
        Apps(
            currentUserId = currentUserId,
            searchQuery = "",
            filterOption = FilterOption.ALL,
            sortOption = SortOption.NAME
        ) 
    }
)

class WakelocksNavItem(userId: Int? = null) : BottomNavItem(
    route = NavRoutes.WAKELOCKS,
    titleResId = R.string.WakeLock,
    icon = { Icon(Icons.Rounded.Lock, contentDescription = null) },
    createNavRoute = { 
        Wakelocks(
            packageName = null,
            userId = userId,
            searchQuery = "",
            filterOption = DAFilterOption.ALL,
            sortOption = DASortOption.NAME
        ) 
    }
)

class AlarmsNavItem(userId: Int? = null) : BottomNavItem(
    route = NavRoutes.ALARMS,
    titleResId = R.string.Alarm,
    icon = { Icon(Icons.Rounded.Alarm, contentDescription = null) },
    createNavRoute = { 
        Alarms(
            packageName = null,
            userId = userId,
            searchQuery = "",
            filterOption = DAFilterOption.ALL,
            sortOption = DASortOption.NAME
        ) 
    }
)

class ServicesNavItem(userId: Int? = null) : BottomNavItem(
    route = NavRoutes.SERVICES,
    titleResId = R.string.Service,
    icon = { Icon(Icons.Rounded.Build, contentDescription = null) },
    createNavRoute = { 
        Services(
            packageName = null,
            userId = userId,
            searchQuery = "",
            filterOption = DAFilterOption.ALL,
            sortOption = DASortOption.NAME
        ) 
    }
)

class SettingsNavItem : BottomNavItem(
    route = NavRoutes.SETTINGS,
    titleResId = R.string.settings,
    icon = { Icon(Icons.Rounded.Settings, contentDescription = null) },
    createNavRoute = { NavRoutes.SETTINGS }
)

@Composable
fun NoWakeLockBottomNavBar(
    navController: NavController,
    currentUserId: Int = 0
) {
    val items = listOf(
        AppsNavItem(currentUserId),
        WakelocksNavItem(currentUserId),
        AlarmsNavItem(currentUserId),
        ServicesNavItem(currentUserId),
        SettingsNavItem()
    )

    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry.value?.destination

    NavigationBar {
        items.forEach { item ->
            val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
            NavigationBarItem(
                icon = item.icon, 
                label = {
                    Text(
                        stringResource(id = item.titleResId),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )
                }, 
                selected = selected, 
                onClick = {
                    if (!selected) {
                        if (item is SettingsNavItem) {
                            // Settings使用字符串导航
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
                        } else {
                            // 其他项使用类型导航
                            navController.navigate(item.createNavRoute()) {
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
                }
            )
        }
    }
}