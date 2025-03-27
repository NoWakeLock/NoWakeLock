package com.js.nowakelock.ui.screens.wakelocks.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Sort
import androidx.compose.material.icons.outlined.SortByAlpha
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.js.nowakelock.R
import com.js.nowakelock.ui.screens.wakelocks.WakelockSortOption

/**
 * Sort section component for the wakelocks screen
 * Provides options to sort by name, count, or time
 */
@Composable
fun WakelocksSortSection(
    currentSort: WakelockSortOption,
    onSortChanged: (WakelockSortOption) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSortMenu by remember { mutableStateOf(false) }
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Sort by:",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(end = 8.dp)
        )
        
        // Name sort button
        SortButton(
            selected = currentSort == WakelockSortOption.NAME,
            onClick = { onSortChanged(WakelockSortOption.NAME) },
            text = "Name",
            icon = Icons.Outlined.SortByAlpha
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Count sort button
        SortButton(
            selected = currentSort == WakelockSortOption.COUNT,
            onClick = { onSortChanged(WakelockSortOption.COUNT) },
            text = "Count",
            icon = Icons.Outlined.Sort
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Time sort button
        SortButton(
            selected = currentSort == WakelockSortOption.TIME,
            onClick = { onSortChanged(WakelockSortOption.TIME) },
            text = "Time",
            icon = Icons.Outlined.AccessTime
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SortButton(
    selected: Boolean,
    onClick: () -> Unit,
    text: String,
    icon: Any  // Can be ImageVector, Painter, etc.
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { 
            Text(
                text = text,
                textAlign = TextAlign.Center
            )
        },
        leadingIcon = {
            when (icon) {
                is androidx.compose.ui.graphics.vector.ImageVector -> {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        trailingIcon = if (selected) {
            {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        } else null
    )
}

@Composable
@Preview(showBackground = true)
fun PreviewWakelocksSortSection() {
    WakelocksSortSection(
        currentSort = WakelockSortOption.COUNT,
        onSortChanged = {}
    )
} 