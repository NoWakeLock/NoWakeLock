package com.js.nowakelock.ui.screens.das

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.js.nowakelock.data.db.Type
import com.js.nowakelock.data.model.DAItem
import com.js.nowakelock.ui.components.EmptyView
import com.js.nowakelock.ui.components.LoadingView
import com.js.nowakelock.ui.components.TopAppBarEvent
import com.js.nowakelock.ui.screens.das.components.DAListItem
import com.js.nowakelock.ui.screens.das.components.DAFilterSection
import com.js.nowakelock.ui.screens.das.components.DAsSortSection
import com.js.nowakelock.ui.screens.das.components.DAsSummary
import com.js.nowakelock.ui.screens.das.components.ServiceListItem

/**
 * Main screen for Wakelocks display
 * Shows list of wakelocks with filtering, sorting, and summary
 * The TopAppBar is handled by the parent NoWakeLockApp
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DAsScreen(
    navigateToDADetail: (name: String, packageName: String) -> Unit = { _, _ -> },
    type: Type,
    viewModel: DAsViewModel,
    isSearchActive: Boolean = false,
    onSearchActiveChange: (Boolean) -> Unit = {},
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {},
    packageName: String? = null,
    userId: Int? = null,
    onTopAppBarEvent: (TopAppBarEvent) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val isAppDetailScreen = packageName != null && packageName != ""

    // Use derivedStateOf to prevent unnecessary recompositions
    val dasList by remember(uiState.das) {
        derivedStateOf { uiState.das }
    }

    // Update viewModel search state when external search state changes
    LaunchedEffect(isSearchActive, searchQuery) {
        viewModel.setSearchActive(isSearchActive)
        viewModel.updateSearchQuery(searchQuery)
    }

    // Set package name and user ID filters
    LaunchedEffect(packageName, userId) {
        viewModel.setAppFilter(packageName, userId)
    }
    
    // Move syncSt call to LaunchedEffect to prevent calling it on every recomposition
    LaunchedEffect(type) {
        viewModel.syncSt(type)
    }
    
    // Handle refresh button click events
    LaunchedEffect(Unit) {
        val refreshHandler: (TopAppBarEvent) -> Unit = { event ->
            if (event is TopAppBarEvent.RefreshClicked) {
                // Call the viewModel's refresh method
                viewModel.refreshData()
            }
            // Forward the event to the original handler
            onTopAppBarEvent(event)
        }
        
        // Set up the refresh handler
        refreshHandler(TopAppBarEvent.RefreshClicked)
    }

    // Top app bar scrolling behavior 
    // We keep the scrolling behavior for content padding
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    // Content only, the TopAppBar is in the parent NoWakeLockApp
    Column(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) {
        // Control panel section with filter and sort options
        Column {
            // Filter section
            DAFilterSection(
                currentFilter = uiState.currentFilterOption,
                onFilterChanged = viewModel::changeFilterOption
            )

            // Sort section
            DAsSortSection(
                currentSort = uiState.currentSortOption,
                onSortChanged = viewModel::changeSortOption,
                type = type
            )

            // Clean divider between controls and content - subtle but visible
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
        }

        // Content area - list with Summary card as first item
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (uiState.isLoading && uiState.das.isEmpty()) {
                LoadingView(
                    modifier = Modifier.fillMaxSize(),
                    message = "Loading " + type.value,
                )
            } else if (!uiState.isLoading && uiState.das.isEmpty()) {
                EmptyView(
                    modifier = Modifier.fillMaxSize(),
                    message = if (searchQuery.isNotEmpty()) {
                        "No ${type.value} found matching \"$searchQuery\""
                    } else {
                        "No ${type.value} found"
                    },
                    onRefresh = { viewModel.refreshData() }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    if (!isAppDetailScreen) {
                        // Summary card as first list item
                        item(key = "summary_card") {
                            DAsSummary(
                                type = type,
                                total = uiState.totalDAs,
                                blockedCount = uiState.blockedCount,
                                allowedCount = uiState.allowedCount,
                                modifier = Modifier.padding(top = 8.dp) // Add some top padding
                            )
                        }
                    }

                    // DA list items
                    items<DAItem>(
                        items = dasList,
                        // Use a comprehensive key that includes all fields affecting UI appearance
                        // This prevents unnecessary recompositions when only settings change
                        key = { daItem -> 
                            // Create a stable key containing all identity and state information
                            "${daItem.name}_${daItem.packageName}_${daItem.userId}_" +
                            "${daItem.fullBlocked}_${daItem.screenOffBlock}_${daItem.timeWindowSec}_" +
                            "${daItem.count}"
                        },
                        contentType = { daItem ->
                            when {
                                daItem.fullBlocked -> "blocked"
                                daItem.screenOffBlock -> "screen_off_blocked"
                                daItem.timeWindowSec != 0 -> "time_window"
                                else -> "normal"
                            }
                        }
                    ) { daItem ->
                        // Remove nested key() wrapper which causes additional recompositions
                        // Directly render the appropriate list item
                        when (type) {
                            Type.Service -> ServiceListItem(
                                daItem = daItem,
                                onToggleFullBlock = { enable ->
                                    viewModel.updateDAFullBlockState(
                                        daItem = daItem, isBlocked = !enable
                                    )
                                },
                                onItemClick = {
                                    navigateToDADetail(it.name, it.packageName)
                                }
                            )

                            else -> DAListItem(
                                daItem = daItem,
                                onToggleFullBlock = { enable ->
                                    viewModel.updateDAFullBlockState(
                                        daItem = daItem, isBlocked = !enable
                                    )
                                },
                                onToggleScreenOffBlock = { enable ->
                                    viewModel.updateDAScreenOffBlockState(
                                        daItem = daItem, isBlocked = !enable
                                    )
                                },
                                onTimeWindowChange = { timeWindow ->
                                    viewModel.updateDATimeWindow(
                                        daItem = daItem, timeWindow = timeWindow
                                    )
                                },
                                onItemClick = {
                                    navigateToDADetail(it.name, it.packageName)
                                }
                            )
                        }
                    }

                    // Extra space at the bottom
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }

                // Show refresh indicator for pull-to-refresh
                if (uiState.isLoading && uiState.loadingSource == LoadingSource.USER_PULL) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Show error message as a Snackbar
            if (uiState.message.isNotEmpty()) {
                Snackbar(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.BottomCenter)
                ) {
                    Text(uiState.message)
                }
            }
        }
    }
} 