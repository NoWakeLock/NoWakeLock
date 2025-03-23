package com.js.nowakelock.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
            
            Scaffold(
                topBar = { 
                    NoWakeLockTopAppBar(
                        navController = navController,
                        isSearchActive = isSearchActive.value,
                        searchQuery = searchQuery.value,
                        onEvent = { event ->
                            when (event) {
                                is TopAppBarEvent.SearchClicked -> {
                                    // Toggle search active state
                                    isSearchActive.value = true
                                    
                                    // If we're on the Apps screen, activate search
                                    val currentRoute = navController.currentBackStackEntry?.destination?.route
                                    if (currentRoute != NavRoutes.APPS) {
                                        // Navigate to apps screen with search activated
                                        navController.navigate(NavRoutes.APPS)
                                    }
                                }
                                is TopAppBarEvent.MenuClicked -> {
                                    // Handle menu click (not implemented)
                                }
                                is TopAppBarEvent.SearchQueryChanged -> {
                                    // Update search query
                                    searchQuery.value = event.query
                                }
                                is TopAppBarEvent.SearchDismissed -> {
                                    // Close search
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