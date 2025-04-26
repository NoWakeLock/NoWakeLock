package com.js.nowakelock.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.js.nowakelock.R
import com.js.nowakelock.base.LogUtil
import com.js.nowakelock.data.model.UserInfo
import com.js.nowakelock.ui.navigation.NavRoutes
import com.js.nowakelock.ui.theme.NoWakeLockTheme

// Event to be handled by the appropriate screen
sealed class TopAppBarEvent {
    object SearchClicked : TopAppBarEvent()
    object MenuClicked : TopAppBarEvent()
    object RefreshClicked : TopAppBarEvent()
    data class SearchQueryChanged(val query: String) : TopAppBarEvent()
    object SearchDismissed : TopAppBarEvent()
    data class UserChanged(val userId: Int) : TopAppBarEvent()
    data class SetDetailTitle(val title: String) : TopAppBarEvent()
    object ClearDetailTitle : TopAppBarEvent()
}

/**
 * Screen types for more semantic route handling
 */
private enum class ScreenType {
    APPS, WAKELOCKS, ALARMS, SERVICES, SETTINGS, DETAIL, OTHER
}

/**
 * UI state for TopAppBar to simplify rendering logic
 * 
 * @param screenType The current screen type
 * @param isDetail Whether the current screen is a detail screen
 * @param title The title to display in the TopAppBar
 * @param showSearch Whether search functionality should be available
 * @param showUserSwitcher Whether user switcher should be displayed
 * @param showRefreshButton Whether refresh button should be displayed
 */
private data class TopAppBarUiState(
    val screenType: ScreenType,
    val isDetail: Boolean,
    val title: String,
    val showSearch: Boolean,
    val showUserSwitcher: Boolean,
    val showRefreshButton: Boolean
)

/**
 * Route utilities to encapsulate route handling logic
 */
private object RouteUtils {
    /**
     * Check if route contains the specified screen identifier
     * Works with both string-based and type-based navigation
     */
    fun isRouteOf(route: String, screenType: String): Boolean {
        return route.contains(screenType)
    }
    
    /**
     * Determine if the current route is a detail screen
     */
    fun isDetailScreen(route: String): Boolean {
        return isRouteOf(route, NavRoutes.DADETAIL) || isRouteOf(route, NavRoutes.APPDETAIL)
    }
    
    /**
     * Get the screen type enum from route
     */
    fun getScreenType(route: String): ScreenType {
        return when {
            isRouteOf(route, NavRoutes.APPS) -> ScreenType.APPS
            isRouteOf(route, NavRoutes.WAKELOCKS) -> ScreenType.WAKELOCKS
            isRouteOf(route, NavRoutes.ALARMS) -> ScreenType.ALARMS
            isRouteOf(route, NavRoutes.SERVICES) -> ScreenType.SERVICES
            isRouteOf(route, NavRoutes.SETTINGS) -> ScreenType.SETTINGS
            isDetailScreen(route) -> ScreenType.DETAIL
            else -> ScreenType.OTHER
        }
    }
    
    /**
     * Get the title for the current route
     */
    @Composable
    fun getScreenTitle(route: String, detailTitle: String?): String {
        return when {
            isDetailScreen(route) -> detailTitle ?: "Detail"
            isRouteOf(route, NavRoutes.APPS) -> stringResource(id = R.string.Apps)
            isRouteOf(route, NavRoutes.WAKELOCKS) -> stringResource(id = R.string.WakeLock)
            isRouteOf(route, NavRoutes.ALARMS) -> stringResource(id = R.string.Alarm)
            isRouteOf(route, NavRoutes.SERVICES) -> stringResource(id = R.string.Service)
            isRouteOf(route, NavRoutes.SETTINGS) -> stringResource(id = R.string.settings)
            else -> stringResource(id = R.string.app_name)
        }
    }
    
    /**
     * Check if search functionality should be available for the current route
     */
    fun shouldShowSearch(route: String): Boolean {
        val screenType = getScreenType(route)
        return screenType == ScreenType.APPS || 
               screenType == ScreenType.WAKELOCKS || 
               screenType == ScreenType.ALARMS || 
               screenType == ScreenType.SERVICES ||
               isRouteOf(route, NavRoutes.APPDETAIL)  // Add AppDetail route to support search
    }
}

/**
 * Remember TopAppBar UI state based on route and parameters
 */
@Composable
private fun rememberTopAppBarUiState(
    route: String,
    detailTitle: String?
): TopAppBarUiState {
    val screenType = RouteUtils.getScreenType(route)
    val isDetail = RouteUtils.isDetailScreen(route)
    val title = RouteUtils.getScreenTitle(route, detailTitle)
    
    return remember(route, detailTitle) {
        TopAppBarUiState(
            screenType = screenType,
            isDetail = isDetail,
            title = title,
            showSearch = RouteUtils.shouldShowSearch(route),
            showUserSwitcher = screenType == ScreenType.APPS,
            showRefreshButton = screenType == ScreenType.WAKELOCKS || 
                                screenType == ScreenType.ALARMS || 
                                screenType == ScreenType.SERVICES
        )
    }
}

/**
 * Standard app bar colors for consistency across the app
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun standardTopAppBarColors() = TopAppBarDefaults.topAppBarColors(
    containerColor = MaterialTheme.colorScheme.surface,
    scrolledContainerColor = MaterialTheme.colorScheme.surface,
    titleContentColor = MaterialTheme.colorScheme.onSurface,
    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
    actionIconContentColor = MaterialTheme.colorScheme.onSurface
)

/**
 * Standard search text field colors for consistency
 */
@Composable
private fun searchTextFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
)

/**
 * Main TopAppBar component for NoWakeLock application.
 * Displays different content based on current route and state.
 * 
 * @param navController Navigation controller for routing and navigation
 * @param onEvent Event callback for TopAppBar actions
 * @param isSearchActive Whether search mode is active
 * @param searchQuery Current search query text
 * @param currentRoute Optional override for current route
 * @param currentUserId Current user ID for user switcher
 * @param availableUsers Available users for user switcher
 * @param detailTitle Optional title for detail screens
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoWakeLockTopAppBar(
    navController: NavController,
    onEvent: (TopAppBarEvent) -> Unit = {},
    isSearchActive: Boolean = false,
    searchQuery: String = "",
    currentRoute: String? = null,
    currentUserId: Int = 0,
    availableUsers: List<UserInfo> = emptyList(),
    detailTitle: String? = null
) {
    // Get current route from NavController or use provided override
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val route: String = (currentRoute ?: navBackStackEntry?.destination?.route).toString()

    // Log current route for debugging
    LogUtil.d("NoWakeLockTopAppBar", "Current route: $route")
    
    // Derive UI state from route and parameters
    val uiState = rememberTopAppBarUiState(route, detailTitle)
    
    // Setup for search functionality
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    
    // Auto-focus search field when activated
    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            focusRequester.requestFocus()
        }
    }

    // Show search mode or standard mode based on state
    if (isSearchActive && uiState.showSearch) {
        SearchModeTopBar(
            route = route,
            searchQuery = searchQuery,
            onEvent = onEvent,
            focusRequester = focusRequester,
            focusManager = focusManager
        )
    } else {
        StandardModeTopBar(
            uiState = uiState,
            navController = navController,
            onEvent = onEvent,
            currentUserId = currentUserId,
            availableUsers = availableUsers
        )
    }
}

/**
 * TopAppBar in search mode with search text field
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchModeTopBar(
    route: String,
    searchQuery: String,
    onEvent: (TopAppBarEvent) -> Unit,
    focusRequester: FocusRequester,
    focusManager: FocusManager
) {
    TopAppBar(
        title = {},
        navigationIcon = {
            // Back button to exit search mode
            IconButton(
                onClick = {
                    onEvent(TopAppBarEvent.SearchDismissed)
                    focusManager.clearFocus()
                }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        actions = {
            // Search text field
            TextField(
                value = searchQuery,
                onValueChange = { onEvent(TopAppBarEvent.SearchQueryChanged(it)) },
                placeholder = {
                    Text(
                        text = getSearchPlaceholder(route),
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                modifier = Modifier
                    .fillMaxWidth(0.85f) // avoid full width
                    .padding(end = 8.dp)
                    .focusRequester(focusRequester),
                singleLine = true,
                colors = searchTextFieldColors(),
                shape = RoundedCornerShape(28.dp),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onEvent(TopAppBarEvent.SearchQueryChanged("")) }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            )
        },
        colors = standardTopAppBarColors()
    )
}

/**
 * Get appropriate search placeholder text based on route
 */
@Composable
private fun getSearchPlaceholder(route: String): String {
    return when {
        RouteUtils.isRouteOf(route, NavRoutes.APPS) -> "Search apps"
        RouteUtils.isRouteOf(route, NavRoutes.WAKELOCKS) -> "Search wakelocks"
        RouteUtils.isRouteOf(route, NavRoutes.ALARMS) -> "Search alarms"
        RouteUtils.isRouteOf(route, NavRoutes.SERVICES) -> "Search services"
        else -> "Search"
    }
}

/**
 * Standard mode TopAppBar with title and actions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StandardModeTopBar(
    uiState: TopAppBarUiState,
    navController: NavController,
    onEvent: (TopAppBarEvent) -> Unit,
    currentUserId: Int,
    availableUsers: List<UserInfo>
) {
    TopAppBar(
        title = {
            Text(
                text = uiState.title,
                style = MaterialTheme.typography.titleLarge
            )
        },
        navigationIcon = {
            // Show back button only on detail screens
            if (uiState.isDetail) {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = stringResource(id = R.string.navigate_back),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        actions = {
            TopBarActions(
                uiState = uiState,
                currentUserId = currentUserId,
                availableUsers = availableUsers,
                onEvent = onEvent
            )
        },
        colors = standardTopAppBarColors()
    )
}

/**
 * TopBar action buttons area
 */
@Composable
private fun TopBarActions(
    uiState: TopAppBarUiState,
    currentUserId: Int,
    availableUsers: List<UserInfo>,
    onEvent: (TopAppBarEvent) -> Unit
) {
    Row {
        // First show search button if search is available for this screen
        if (uiState.showSearch) {
            SearchButton(onEvent)
        }
        
        // Then show screen-specific actions
        if (uiState.showUserSwitcher && availableUsers.isNotEmpty()) {
            UserSwitcher(
                currentUserId = currentUserId,
                availableUsers = availableUsers,
                onUserChanged = { newUserId -> onEvent(TopAppBarEvent.UserChanged(newUserId)) }
            )
        } else if (uiState.showRefreshButton) {
            RefreshButton(onEvent)
        }
    }
}

/**
 * Search button component
 */
@Composable
private fun SearchButton(onEvent: (TopAppBarEvent) -> Unit) {
    IconButton(
        onClick = { onEvent(TopAppBarEvent.SearchClicked) },
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .size(48.dp)
    ) {
        Icon(
            Icons.Default.Search,
            contentDescription = "Search",
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * Refresh button component
 */
@Composable
private fun RefreshButton(onEvent: (TopAppBarEvent) -> Unit) {
    IconButton(
        onClick = { onEvent(TopAppBarEvent.RefreshClicked) },
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .size(48.dp)
    ) {
        Icon(
            Icons.Default.Refresh,
            contentDescription = "Refresh",
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(24.dp)
        )
    }
}

// Preview functions

@Preview(name = "Apps Screen")
@Composable
private fun PreviewAppsTopBar() {
    NoWakeLockTheme {
        val mockNavController = rememberNavController()
        NoWakeLockTopAppBar(
            navController = mockNavController,
            currentRoute = NavRoutes.APPS,
            availableUsers = listOf(UserInfo(0, "Primary", false))
        )
    }
}

@Preview(name = "Detail Screen")
@Composable
private fun PreviewDetailTopBar() {
    NoWakeLockTheme {
        val mockNavController = rememberNavController()
        NoWakeLockTopAppBar(
            navController = mockNavController,
            currentRoute = NavRoutes.DADETAIL,
            detailTitle = "Wakelock Details"
        )
    }
}

@Preview(name = "Search Active")
@Composable
private fun PreviewSearchActiveTopBar() {
    NoWakeLockTheme {
        val mockNavController = rememberNavController()
        NoWakeLockTopAppBar(
            navController = mockNavController,
            currentRoute = NavRoutes.APPS,
            isSearchActive = true,
            searchQuery = "test"
        )
    }
} 