package com.js.nowakelock.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.js.nowakelock.R
import com.js.nowakelock.ui.navigation.NavRoutes

// Event to be handled by the appropriate screen
sealed class TopAppBarEvent {
    object SearchClicked : TopAppBarEvent()
    object MenuClicked : TopAppBarEvent()
    data class SearchQueryChanged(val query: String) : TopAppBarEvent()
    object SearchDismissed : TopAppBarEvent()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoWakeLockTopAppBar(
    navController: NavController,
    onEvent: (TopAppBarEvent) -> Unit = {},
    isSearchActive: Boolean = false,
    searchQuery: String = ""
) {
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

    // Only show search interface in apps screen
    val showSearch = isSearchActive && currentRoute == NavRoutes.APPS
    val focusRequester = remember { FocusRequester() }

    TopAppBar(
        title = { 
            // Show search field or title based on search state
            if (showSearch) {
                TextField(
                    value = searchQuery,
                    onValueChange = { onEvent(TopAppBarEvent.SearchQueryChanged(it)) },
                    placeholder = { Text("Search apps") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                    leadingIcon = {
                        IconButton(onClick = { onEvent(TopAppBarEvent.SearchDismissed) }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onEvent(TopAppBarEvent.SearchQueryChanged("")) }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search"
                                )
                            }
                        }
                    }
                )
            } else {
                Text(text = title)
            }
        },
        actions = {
            // Show search icon only when not in search mode
            if (!showSearch) {
                // 搜索按钮
                IconButton(onClick = { onEvent(TopAppBarEvent.SearchClicked) }) {
                    Icon(Icons.Default.Search, contentDescription = "搜索")
                }
                // 菜单按钮
                IconButton(onClick = { onEvent(TopAppBarEvent.MenuClicked) }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "更多选项")
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
} 