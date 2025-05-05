package com.js.nowakelock.ui.screens.das.components

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.js.nowakelock.R
import com.js.nowakelock.data.db.Type
import com.js.nowakelock.ui.screens.das.DASortOption

/**
 * Sort section component for the DAs screen
 * Provides options to sort by name, count, or time (time only for Wakelock type)
 * Styled to match AppsSortSection for UI consistency
 */
@Composable
fun DAsSortSection(
    currentSort: DASortOption,
    onSortChanged: (DASortOption) -> Unit,
    type: Type = Type.Wakelock, // Add type parameter with default value
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
    // If current sort is TIME but type doesn't support TIME sorting, switch to NAME
    LaunchedEffect(type, currentSort) {
        if (currentSort == DASortOption.TIME && type != Type.Wakelock) {
            onSortChanged(DASortOption.NAME)
        }
    }

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
                imageVector = Icons.AutoMirrored.Filled.Sort,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.sort_by),
                style = MaterialTheme.typography.bodyLarge,
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
                text = stringResource(R.string.sort_name),
                selected = currentSort == DASortOption.NAME,
                onClick = { onSortChanged(DASortOption.NAME) }
            )
            
            // Count sort option
            SortOption(
                text = stringResource(R.string.sort_count),
                selected = currentSort == DASortOption.COUNT,
                onClick = { onSortChanged(DASortOption.COUNT) }
            )
            
            // Time sort option - only show for Wakelock type
            if (type == Type.Wakelock) {
                SortOption(
                    text = stringResource(R.string.sort_time),
                    selected = currentSort == DASortOption.TIME,
                    onClick = { onSortChanged(DASortOption.TIME) }
                )
            }
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
            style = MaterialTheme.typography.bodyLarge,
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
        onSortChanged = {},
        type = Type.Wakelock
    )
}

@Composable
@Preview(showBackground = true)
fun PreviewAlarmSortSection() {
    DAsSortSection(
        currentSort = DASortOption.NAME,
        onSortChanged = {},
        type = Type.Alarm
    )
} 