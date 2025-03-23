package com.js.nowakelock.ui.components

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
                    placeholder = { Text("Search apps") },
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
        // Normal TopAppBar
        TopAppBar(
            title = { Text(text = title) },
            actions = {
                Row {
                    // Search button
                    if (currentRoute == NavRoutes.APPS) {
                        IconButton(
                            onClick = { onEvent(TopAppBarEvent.SearchClicked) },
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .clip(CircleShape)
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "搜索",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    //todo: more options button?
//                    IconButton(
//                        onClick = { onEvent(TopAppBarEvent.MenuClicked) },
//                        modifier = Modifier
//                            .padding(horizontal = 4.dp)
//                            .clip(CircleShape)
//                    ) {
//                        Icon(
//                            Icons.Default.MoreVert,
//                            contentDescription = "更多选项",
//                            tint = MaterialTheme.colorScheme.onSurface
//                        )
//                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface
            )
        )
    }
} 