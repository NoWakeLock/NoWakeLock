package com.js.nowakelock.ui.screens.apps

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.js.nowakelock.ui.screens.apps.components.AppsSortSection
import org.koin.androidx.compose.koinViewModel

/**
 * Main screen for displaying apps with wakelock statistics
 */
@Composable
fun AppsScreen(
    viewModel: AppsViewModel = koinViewModel(),
    onAppClick: (AppWithStats) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Filter tabs
        AppsFilterSection(
            currentFilter = uiState.currentFilterOption,
            onFilterChange = { viewModel.changeFilterOption(it) }
        )
        
        // Sort control
        AppsSortSection(
            currentSort = uiState.currentSortOption,
            onSortChange = { viewModel.changeSortOption(it) }
        )
        
        // Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            // Loading indicator
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (uiState.apps.isEmpty()) {
                // Empty state
                EmptyAppsContent(
                    filterOption = uiState.currentFilterOption,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                // Apps list
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
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
        }
    }
}

/**
 * Component displayed when app list is empty
 */
@Composable
private fun EmptyAppsContent(
    filterOption: FilterOption,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = when (filterOption) {
                FilterOption.ALL -> "No applications found"
                FilterOption.USER -> "No user applications found"
                FilterOption.SYSTEM -> "No system applications found"
                FilterOption.MODIFIED -> "No applications with wakelock activity"
            },
            style = MaterialTheme.typography.titleMedium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = when (filterOption) {
                FilterOption.ALL -> "Try syncing your application data"
                FilterOption.USER -> "Try changing to a different filter"
                FilterOption.SYSTEM -> "Try changing to a different filter"
                FilterOption.MODIFIED -> "No applications have used wakelocks yet"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = { /* Call viewModel.refreshData() */ }
        ) {
            Text(text = "Refresh Data")
        }
    }
} 