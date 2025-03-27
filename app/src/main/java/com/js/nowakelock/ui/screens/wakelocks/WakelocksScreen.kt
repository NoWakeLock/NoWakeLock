package com.js.nowakelock.ui.screens.wakelocks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.js.nowakelock.ui.components.EmptyView
import com.js.nowakelock.ui.components.LoadingView
import com.js.nowakelock.ui.screens.wakelocks.components.*
import org.koin.androidx.compose.koinViewModel

/**
 * Main screen for Wakelocks display
 * Shows list of wakelocks with filtering, sorting, and summary
 * The TopAppBar is handled by the parent NoWakeLockApp
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WakelocksScreen(
    navigateToWakelockDetail: (name: String, packageName: String) -> Unit = { _, _ -> },
    viewModel: WakelocksViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Top app bar scrolling behavior 
    // We keep the scrolling behavior for content padding
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    
    // Content only, the TopAppBar is in the parent NoWakeLockApp
    Column(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) {
        // Filter section
        WakelocksFilterSection(
            currentFilter = uiState.currentFilterOption,
            onFilterChanged = viewModel::changeFilterOption
        )
        
        // Sort section
        WakelocksSortSection(
            currentSort = uiState.currentSortOption,
            onSortChanged = viewModel::changeSortOption
        )
        
        // Summary card
        WakelocksSummary(
            totalWakelocks = uiState.totalWakelocks,
            blockedCount = uiState.blockedCount,
            allowedCount = uiState.allowedCount
        )
        
        // Wakelock list
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (uiState.isLoading && uiState.wakelocks.isEmpty()) {
                LoadingView(
                    modifier = Modifier.fillMaxSize(),
                    message = "Loading wakelocks..."
                )
            } else if (!uiState.isLoading && uiState.wakelocks.isEmpty()) {
                EmptyView(
                    modifier = Modifier.fillMaxSize(),
                    message = "No wakelocks found",
                    onRefresh = { viewModel.refreshData() }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = uiState.wakelocks,
                        key = { "${it.name}_${it.packageName}_${it.userId}" }
                    ) { wakelockItem ->
                        WakelockListItem(
                            wakelockItem = wakelockItem,
                            onToggleBlock = { isBlocked ->
                                viewModel.updateWakelockBlockState(
                                    name = wakelockItem.name,
                                    packageName = wakelockItem.packageName,
                                    userId = wakelockItem.userId,
                                    isBlocked = isBlocked
                                )
                            },
                            onTimeWindowChange = { timeWindow ->
                                viewModel.updateWakelockTimeWindow(
                                    name = wakelockItem.name,
                                    packageName = wakelockItem.packageName,
                                    userId = wakelockItem.userId,
                                    timeWindow = timeWindow
                                )
                            },
                            onItemClick = {
                                navigateToWakelockDetail(it.name, it.packageName)
                            }
                        )
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