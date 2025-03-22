package com.js.nowakelock.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.js.nowakelock.R
import com.js.nowakelock.ui.navigation.NavRoutes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoWakeLockTopAppBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    val title = when (currentRoute) {
        NavRoutes.APPS -> stringResource(id = R.string.Apps)
        NavRoutes.WAKELOCKS -> stringResource(id = R.string.WakeLock)
        NavRoutes.ALARMS -> stringResource(id = R.string.Alarm)
        NavRoutes.SERVICES -> stringResource(id = R.string.Service)
        NavRoutes.SETTINGS -> stringResource(id = R.string.settings)
        else -> stringResource(id = R.string.app_name)
    }

    TopAppBar(
        title = { Text(text = title) },
        actions = {
            // 搜索按钮
            IconButton(onClick = { /* 处理搜索 */ }) {
                Icon(Icons.Default.Search, contentDescription = "搜索")
            }
            // 菜单按钮
            IconButton(onClick = { /* 处理菜单 */ }) {
                Icon(Icons.Default.MoreVert, contentDescription = "更多选项")
            }
        }
    )
} 