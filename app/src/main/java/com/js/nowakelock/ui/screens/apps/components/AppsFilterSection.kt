package com.js.nowakelock.ui.screens.apps.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.js.nowakelock.ui.screens.apps.FilterOption

/**
 * Filter section component that allows switching between different app filters
 */
@Composable
fun AppsFilterSection(
    currentFilter: FilterOption,
    onFilterChange: (FilterOption) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = currentFilter.ordinal,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        edgePadding = 16.dp,
    ) {
        FilterOption.values().forEachIndexed { index, filter ->
            Tab(
                selected = currentFilter.ordinal == index,
                onClick = { onFilterChange(filter) },
                text = {
                    Text(
                        text = when (filter) {
                            FilterOption.ALL -> "All Apps"
                            FilterOption.USER -> "User Apps"
                            FilterOption.SYSTEM -> "System Apps"
                            FilterOption.MODIFIED -> "Modified"
                        }
                    )
                }
            )
        }
    }
} 