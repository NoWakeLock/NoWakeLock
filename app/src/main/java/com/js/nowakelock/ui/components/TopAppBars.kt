package com.js.nowakelock.ui.components

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.js.nowakelock.R
import com.js.nowakelock.base.LogUtil
import com.js.nowakelock.data.model.UserInfo
import com.js.nowakelock.ui.navigation.NavRoutes

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
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    // Use passed currentRoute or get it from NavController if not provided
    val route = currentRoute ?: navBackStackEntry?.destination?.route
    // Check for the new type-safe route format as well as the legacy format for backward compatibility
    val isDetailScreen = route?.contains(NavRoutes.DADETAIL) == true

    LogUtil.d("NoWakeLockTopAppBar", "Current route: $route, isDetailScreen: $isDetailScreen")

    val title = when {
        isDetailScreen -> if (detailTitle?.isEmpty() == true) detailTitle else stringResource(id = R.string.WakeLock)
        route == NavRoutes.APPS -> stringResource(id = R.string.Apps)
        route == NavRoutes.WAKELOCKS -> stringResource(id = R.string.WakeLock)
        route == NavRoutes.ALARMS -> stringResource(id = R.string.Alarm)
        route == NavRoutes.SERVICES -> stringResource(id = R.string.Service)
        route == NavRoutes.SETTINGS -> stringResource(id = R.string.settings)
        else -> stringResource(id = R.string.app_name)
    }

    // Only show search interface in relevant screens
    val showSearch = isSearchActive && (route == NavRoutes.APPS ||
            route == NavRoutes.WAKELOCKS ||
            route == NavRoutes.ALARMS ||
            route == NavRoutes.SERVICES)
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    // Auto-focus search field when activated
    LaunchedEffect(showSearch) {
        if (showSearch) {
            focusRequester.requestFocus()
        }
    }

    if (showSearch) {
        TopAppBar(
            title = {},
            navigationIcon = {
                // return button
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
                TextField(
                    value = searchQuery,
                    onValueChange = { onEvent(TopAppBarEvent.SearchQueryChanged(it)) },
                    placeholder = {
                        Text(
                            when (route) {
                                NavRoutes.APPS -> "Search apps"
                                NavRoutes.WAKELOCKS -> "Search wakelocks"
                                NavRoutes.ALARMS -> "Search alarms"
                                NavRoutes.SERVICES -> "Search services"
                                else -> "Search"
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.85f) // avoid full width
                        .focusRequester(focusRequester),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                            alpha = 0.7f
                        ),
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                    shape = RoundedCornerShape(24.dp),
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
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
    } else {
        // Normal TopAppBar with dynamic actions based on current route
        TopAppBar(
            title = { Text(text = title) },
            navigationIcon = {
                if (isDetailScreen) {
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
                Row {
                    // for all screens, display the search button first, then other operations (user switcher/refresh)
                    // all related screens display the search button, ensure the size is consistent
                    if (route == NavRoutes.APPS ||
                        route == NavRoutes.WAKELOCKS ||
                        route == NavRoutes.ALARMS ||
                        route == NavRoutes.SERVICES
                    ) {

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

                    // 根据路由显示不同的额外操作按钮
                    when (route) {
                        NavRoutes.APPS -> {
                            // 用户切换器 - 只在Apps界面显示
                            if (availableUsers.isNotEmpty()) {
                                UserSwitcher(
                                    currentUserId = currentUserId,
                                    availableUsers = availableUsers,
                                    onUserChanged = { newUserId ->
                                        onEvent(TopAppBarEvent.UserChanged(newUserId))
                                    }
                                )
                            }
                        }

                        NavRoutes.WAKELOCKS, NavRoutes.ALARMS, NavRoutes.SERVICES -> {
                            // 刷新按钮 - 在其他数据界面显示，确保尺寸与UserSwitcher一致
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
                    }


                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface
            )
        )
    }
} 