package com.js.nowakelock.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.js.nowakelock.ui.components.NoWakeLockBottomNavBar
import com.js.nowakelock.ui.components.NoWakeLockTopAppBar
import com.js.nowakelock.ui.components.TopAppBarEvent
import com.js.nowakelock.ui.navigation.NavRoutes
import com.js.nowakelock.ui.navigation.NoWakeLockNavGraph
import com.js.nowakelock.ui.theme.NoWakeLockTheme
import org.koin.androidx.compose.KoinAndroidContext

@Composable
fun NoWakeLockApp() {
    KoinAndroidContext{
        NoWakeLockTheme {
            val navController = rememberNavController()
            
            // Create app-level search state
            val isSearchActive = rememberSaveable { mutableStateOf(false) }
            val searchQuery = rememberSaveable { mutableStateOf("") }
            
            // 每次路线变化时重置搜索状态
            val navBackStackEntry = navController.currentBackStackEntry
            LaunchedEffect(navBackStackEntry) {
                val currentRoute = navBackStackEntry?.destination?.route
                if (currentRoute != NavRoutes.APPS) {
                    isSearchActive.value = false
                }
            }
            
            Scaffold(
                topBar = { 
                    NoWakeLockTopAppBar(
                        navController = navController,
                        isSearchActive = isSearchActive.value,
                        searchQuery = searchQuery.value,
                        onEvent = { event ->
                            when (event) {
                                is TopAppBarEvent.SearchClicked -> {
                                    // 激活搜索模式
                                    isSearchActive.value = true
                                    
                                    // 如果不在Apps页面，先导航到Apps页面
                                    val currentRoute = navController.currentBackStackEntry?.destination?.route
                                    if (currentRoute != NavRoutes.APPS) {
                                        navController.navigate(NavRoutes.APPS)
                                    }
                                }
                                is TopAppBarEvent.MenuClicked -> {
                                    // 处理菜单点击（暂未实现）
                                }
                                is TopAppBarEvent.SearchQueryChanged -> {
                                    // 更新搜索查询
                                    searchQuery.value = event.query
                                }
                                is TopAppBarEvent.SearchDismissed -> {
                                    // 关闭搜索并清空查询
                                    isSearchActive.value = false
                                    searchQuery.value = ""
                                }
                            }
                        }
                    ) 
                },
                bottomBar = { NoWakeLockBottomNavBar(navController) }
            ) { paddingValues ->
                NoWakeLockNavGraph(
                    navController = navController,
                    modifier = Modifier.padding(paddingValues),
                    isSearchActive = isSearchActive.value,
                    onSearchActiveChange = { isSearchActive.value = it },
                    searchQuery = searchQuery.value,
                    onSearchQueryChange = { searchQuery.value = it }
                )
            }
        }
    }
}

@Composable
@Preview
fun NoWakeLockAppPreview() {
    NoWakeLockApp()
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    NoWakeLockTheme {
        Greeting("Android")
    }
}