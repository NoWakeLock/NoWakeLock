package com.js.nowakelock.ui.screens.das.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.js.nowakelock.ui.screens.das.DASortOption

/**
 * Sort section component for the DAs screen
 * Provides options to sort by name, count, or time
 * Styled to match AppsSortSection for UI consistency
 */
@Composable
fun DAsSortSection(
    currentSort: DASortOption,
    onSortChanged: (DASortOption) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp), // Reduced vertical padding for better spacing
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Sort by label with icon for better visual hierarchy
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Sort,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Sort by:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Simple text options for sorting
        Row(
            modifier = Modifier.wrapContentWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Name sort option
            SortOption(
                text = "Name",
                selected = currentSort == DASortOption.NAME,
                onClick = { onSortChanged(DASortOption.NAME) }
            )
            
            // Count sort option
            SortOption(
                text = "Count",
                selected = currentSort == DASortOption.COUNT,
                onClick = { onSortChanged(DASortOption.COUNT) }
            )
            
            // Time sort option
            SortOption(
                text = "Time",
                selected = currentSort == DASortOption.TIME,
                onClick = { onSortChanged(DASortOption.TIME) }
            )
        }
    }
}

/**
 * Individual sort option component
 */
@Composable
private fun SortOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .wrapContentSize()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (selected) 
                MaterialTheme.colorScheme.primary 
            else 
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
@Preview(showBackground = true)
fun PreviewWakelocksSortSection() {
    DAsSortSection(
        currentSort = DASortOption.COUNT,
        onSortChanged = {}
    )
} 