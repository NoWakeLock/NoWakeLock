package com.js.nowakelock.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.js.nowakelock.R
import com.js.nowakelock.data.model.UserInfo
import com.js.nowakelock.data.repository.appdas.AppDasRepo
import com.js.nowakelock.ui.components.*
import com.js.nowakelock.ui.navigation.NoWakeLockNavGraph
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get
import org.koin.compose.koinInject

/**
 * Main app composable that sets up the overall UI structure
 * Edge-to-edge display is handled by enableEdgeToEdge() in MainActivity
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoWakeLockApp(
    appDasRepo: AppDasRepo = koinInject(),
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute != null

    // State for the search functionality
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    
    // State for user management
    var currentUserId by remember { mutableStateOf(0) } // Default to user 0 (primary user)
    var availableUsers by remember { mutableStateOf(listOf(UserInfo.createPrimaryUser())) } // Initialize with primary user
    
    // State for detail screen title
    var detailTitle by remember { mutableStateOf<String?>(null) }
    
    // Load available users on app start
    LaunchedEffect(Unit) {
        try {
            // Asynchronously load users without blocking UI
            val users = appDasRepo.getAvailableUsers()
            if (users.isNotEmpty()) {
                availableUsers = users
                // Verify if current user is valid in the loaded list
                if (users.none { it.userId == currentUserId }) {
                    currentUserId = 0 // Reset to primary user if current ID is invalid
                }
            }
        } catch (e: Exception) {
            // Log and show error but don't block UI
            scope.launch {
                snackbarHostState.showSnackbar("Error loading users: ${e.message}")
            }
        }
    }

    val onTopAppBarEvent: (TopAppBarEvent) -> Unit = { event ->
        when (event) {
            is TopAppBarEvent.SearchClicked -> {
                isSearchActive = true
            }
            is TopAppBarEvent.SearchDismissed -> {
                isSearchActive = false
                searchQuery = ""
            }
            is TopAppBarEvent.SearchQueryChanged -> {
                searchQuery = event.query
            }
            is TopAppBarEvent.UserChanged -> {
                // Handle user change at app level
                currentUserId = event.userId
            }
            is TopAppBarEvent.MenuClicked -> {
                // Handle menu click
            }
            is TopAppBarEvent.RefreshClicked -> {
                // Handle refresh
            }
            is TopAppBarEvent.SetDetailTitle -> {
                detailTitle = event.title
            }
            is TopAppBarEvent.ClearDetailTitle -> {
                detailTitle = null
            }
        }
    }

    Scaffold(
        topBar = {
            NoWakeLockTopAppBar(
                navController = navController,
                onEvent = onTopAppBarEvent,
                isSearchActive = isSearchActive,
                searchQuery = searchQuery,
                currentUserId = currentUserId,
                availableUsers = availableUsers,
                detailTitle = detailTitle
            )
        },
        bottomBar = {
            if (showBottomBar) {
                NoWakeLockBottomNavBar(navController = navController)
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            NoWakeLockNavGraph(
                navController = navController,
                onTopAppBarEvent = onTopAppBarEvent,
                isSearchActive = isSearchActive,
                searchQuery = searchQuery,
                currentUserId = currentUserId
            )
        }
    }
}

@Composable
@Preview
fun NoWakeLockAppPreview() {
    NoWakeLockApp()
}