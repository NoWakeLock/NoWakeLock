package com.js.nowakelock.ui.screens.apps

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.PullRefreshState
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.js.nowakelock.R
import com.js.nowakelock.data.model.AppWithStats
import com.js.nowakelock.ui.screens.apps.components.AppListItem
import com.js.nowakelock.ui.screens.apps.components.AppsFilterSection
import com.js.nowakelock.ui.screens.apps.components.AppsSearchBar
import com.js.nowakelock.ui.screens.apps.components.AppsSortSection
import org.koin.androidx.compose.koinViewModel

/**
 * Main screen for displaying apps with wakelock statistics
 * Layout optimized for better visual hierarchy and spacing
 * Following MD3 best practices with subtle dividers instead of background differences
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AppsScreen(
    viewModel: AppsViewModel = koinViewModel(),
    onAppClick: (AppWithStats) -> Unit = {},
    isSearchActive: Boolean = false,
    onSearchActiveChange: (Boolean) -> Unit = {},
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {},
    showSearchBarInContent: Boolean = true // Default to showing the search bar in content
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Sync the external search active state with the viewModel
    LaunchedEffect(isSearchActive) {
        viewModel.setSearchActive(isSearchActive)
    }
    
    // Sync the external search query with the viewModel
    LaunchedEffect(searchQuery) {
        if (uiState.searchQuery != searchQuery) {
            viewModel.updateSearchQuery(searchQuery)
        }
    }
    
    // Sync the viewModel search query with the external state
    LaunchedEffect(uiState.searchQuery) {
        if (searchQuery != uiState.searchQuery) {
            onSearchQueryChange(uiState.searchQuery)
        }
    }
    
    // Whether to show the pull refresh indicator
    val showPullRefresh = uiState.isLoading && uiState.loadingSource == LoadingSource.USER_PULL
    // Whether to show the central loader
    val showCentralLoader = uiState.isLoading && uiState.loadingSource != LoadingSource.USER_PULL && uiState.loadingSource != LoadingSource.NONE
    
    val pullRefreshState = rememberPullRefreshState(
        refreshing = showPullRefresh,
        onRefresh = { viewModel.refreshData(LoadingSource.USER_PULL) }
    )
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Control panel section with search, filter and sort - no background color difference
            Column {
                // Search Bar - placed above filter section (only if showSearchBarInContent is true)
                if (showSearchBarInContent) {
                    AppsSearchBar(
                        searchQuery = uiState.searchQuery,
                        onSearchQueryChange = { 
                            viewModel.updateSearchQuery(it)
                            onSearchQueryChange(it)
                        },
                        onSearch = { viewModel.updateSearchQuery(it) },
                        onActiveChange = { 
                            viewModel.setSearchActive(it)
                            onSearchActiveChange(it)
                        },
                        isSearchActive = uiState.isSearchActive
                    )
                }
                
                // Filter section
                AppsFilterSection(
                    currentFilter = uiState.currentFilterOption,
                    onFilterChange = { viewModel.changeFilterOption(it) }
                )
                
                // Sort section
                AppsSortSection(
                    currentSort = uiState.currentSortOption,
                    onSortChange = { viewModel.changeSortOption(it) }
                )
                
                // Clean divider between controls and content - subtle but visible
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            }
            
            // Content area
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .pullRefresh(pullRefreshState)
            ) {
                // Show the central loader if needed
                if (showCentralLoader) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else if (!uiState.isLoading && uiState.apps.isEmpty()) {
                    // Empty state - show when not loading and list is empty
                    EmptyAppsContent(
                        filterOption = uiState.currentFilterOption,
                        searchQuery = uiState.searchQuery,
                        modifier = Modifier.align(Alignment.Center),
                        viewModel = viewModel
                    )
                } else {
                    // Apps list
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 8.dp)
                    ) {
                        items(
                            items = uiState.apps,
                            key = { "${it.appInfo.packageName}_${it.appInfo.userId}" }
                        ) { app ->
                            AppListItem(
                                appWithStats = app,
                                onItemClick = onAppClick
                            )
                        }
                    }
                }
                
                // Error message
                if (uiState.message.isNotEmpty()) {
                    Snackbar(
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.BottomCenter)
                    ) {
                        Text(text = uiState.message)
                    }
                }

                // Show the pull refresh indicator only when needed
                PullRefreshIndicator(
                    refreshing = showPullRefresh,
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter),
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * Component displayed when app list is empty
 */
@Composable
private fun EmptyAppsContent(
    filterOption: FilterOption,
    searchQuery: String,
    modifier: Modifier = Modifier,
    viewModel: AppsViewModel
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // If we have a search query, show search-specific empty state
        val isSearching = searchQuery.isNotEmpty()
        
        Text(
            text = if (isSearching) {
                "No apps found matching \"$searchQuery\""
            } else {
                when (filterOption) {
                    FilterOption.ALL -> "No applications found"
                    FilterOption.USER -> "No user applications found"
                    FilterOption.SYSTEM -> "No system applications found"
                    FilterOption.MODIFIED -> "No applications with wakelock activity"
                }
            },
            style = MaterialTheme.typography.titleMedium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = if (isSearching) {
                "Try a different search term or filter"
            } else {
                when (filterOption) {
                    FilterOption.ALL -> "Try syncing your application data"
                    FilterOption.USER -> "Try changing to a different filter"
                    FilterOption.SYSTEM -> "Try changing to a different filter"
                    FilterOption.MODIFIED -> "No applications have used wakelocks yet"
                }
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isSearching) {
                Button(
                    onClick = { viewModel.updateSearchQuery("") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Text(text = "Clear Search")
                }
            }
            
            Button(
                onClick = { viewModel.refreshData(LoadingSource.INITIAL) }
            ) {
                Text(text = "Refresh Data")
            }
        }
    }
} 