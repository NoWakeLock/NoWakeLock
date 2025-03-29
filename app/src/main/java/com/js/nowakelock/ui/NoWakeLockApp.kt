package com.js.nowakelock.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.js.nowakelock.ui.components.NoWakeLockBottomNavBar
import com.js.nowakelock.ui.components.NoWakeLockTopAppBar
import com.js.nowakelock.ui.components.TopAppBarEvent
import com.js.nowakelock.ui.navigation.NavRoutes
import com.js.nowakelock.ui.navigation.NoWakeLockNavGraph
import com.js.nowakelock.ui.screens.das.DAsViewModel
import com.js.nowakelock.ui.theme.NoWakeLockTheme
import org.koin.androidx.compose.KoinAndroidContext
import org.koin.androidx.compose.koinViewModel

/**
 * Main app composable that sets up the overall UI structure
 * Edge-to-edge display is handled by enableEdgeToEdge() in MainActivity
 */
@Composable
fun NoWakeLockApp() {
    KoinAndroidContext{
        NoWakeLockTheme {
            val navController = rememberNavController()
            
            // Create app-level search state
            val isSearchActive = rememberSaveable { mutableStateOf(false) }
            val searchQuery = rememberSaveable { mutableStateOf("") }

            // Obtain the wakelocks ViewModel for app-level access to refresh function
//            val DAsViewModel: DAsViewModel = koinViewModel()

            // Navigation state for dynamic TopAppBar
            val navBackStackEntry = navController.currentBackStackEntry
            val currentRoute = navBackStackEntry?.destination?.route
            
            // Reset search state whenever route changes
            LaunchedEffect(navBackStackEntry) {
                val route = navBackStackEntry?.destination?.route
                if (route != NavRoutes.APPS) {
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
                                    // Activate search mode
                                    isSearchActive.value = true
                                    
                                    // If not on Apps screen, navigate to Apps screen first
                                    val route = navController.currentBackStackEntry?.destination?.route
                                    if (route != NavRoutes.APPS) {
                                        navController.navigate(NavRoutes.APPS)
                                    }
                                }
                                is TopAppBarEvent.MenuClicked -> {
                                    // Handle menu click (not yet implemented)
                                }
                                is TopAppBarEvent.SearchQueryChanged -> {
                                    // Update search query
                                    searchQuery.value = event.query
                                }
                                is TopAppBarEvent.SearchDismissed -> {
                                    // Close search and clear query
                                    isSearchActive.value = false
                                    searchQuery.value = ""
                                }
                                is TopAppBarEvent.RefreshClicked -> {
                                    // Handle refresh based on current route
                                    when (currentRoute) {
//                                        NavRoutes.WAKELOCKS -> DAsViewModel.refreshData()
                                        // Add other screen refresh actions as needed
                                    }
                                }
                            }
                        },
                        // Pass current route for dynamic actions in TopAppBar
                        currentRoute = currentRoute
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
                    onSearchQueryChange = { searchQuery.value = it },
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