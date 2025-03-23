package com.js.nowakelock.ui.screens.apps.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.js.nowakelock.R
import com.js.nowakelock.ui.screens.apps.FilterOption

/**
 * Filter section component that allows switching between different app filters
 * Uses pill-shaped tabs with minimal padding and spacing for better visual flow
 */
@Composable
fun AppsFilterSection(
    currentFilter: FilterOption,
    onFilterChange: (FilterOption) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp), // Reduced padding
        horizontalArrangement = Arrangement.spacedBy(6.dp), // Reduced spacing
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilterOption.entries.forEach { filter ->
            val isSelected = currentFilter == filter
            
            // Each filter option is a pill-shaped button
            Surface(
                modifier = Modifier.weight(1f),
                onClick = { onFilterChange(filter) },
                shape = MaterialTheme.shapes.large, // More rounded corners like in prototype
                color = if (isSelected) 
                    MaterialTheme.colorScheme.primaryContainer 
                else 
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), // Lighter background when not selected
                contentColor = if (isSelected)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurface,
                tonalElevation = 0.dp, // Flatter appearance
                shadowElevation = if (isSelected) 1.dp else 0.dp, // Minimal shadow
                border = BorderStroke(
                    width = 0.5.dp,
                    color = if (isSelected)
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    else
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp), // Reduced vertical padding
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (filter) {
                            FilterOption.ALL -> stringResource(R.string.menu_filter_all)
                            FilterOption.USER -> stringResource(R.string.menu_filter_user)
                            FilterOption.SYSTEM -> stringResource(R.string.menu_filter_system)
                            FilterOption.MODIFIED -> stringResource(R.string.menu_filter_modified)
                        },
                        style = MaterialTheme.typography.labelMedium, // Slightly smaller text
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
} 