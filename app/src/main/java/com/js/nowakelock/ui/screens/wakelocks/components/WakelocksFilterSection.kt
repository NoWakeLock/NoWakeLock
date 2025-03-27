package com.js.nowakelock.ui.screens.wakelocks.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.js.nowakelock.ui.screens.wakelocks.WakelockFilterOption

/**
 * Filter section component for the wakelocks screen
 * Provides filter chips to select between All, Blocked and Allowed wakelocks
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WakelocksFilterSection(
    currentFilter: WakelockFilterOption,
    onFilterChanged: (WakelockFilterOption) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // All filter
        FilterChip(
            selected = currentFilter == WakelockFilterOption.ALL,
            onClick = { onFilterChanged(WakelockFilterOption.ALL) },
            label = { Text("All") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        )
        
        // Blocked filter
        FilterChip(
            selected = currentFilter == WakelockFilterOption.BLOCKED,
            onClick = { onFilterChanged(WakelockFilterOption.BLOCKED) },
            label = { Text("Blocked") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer
            )
        )
        
        // Allowed filter
        FilterChip(
            selected = currentFilter == WakelockFilterOption.ALLOWED,
            onClick = { onFilterChanged(WakelockFilterOption.ALLOWED) },
            label = { Text("Allowed") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer
            )
        )
    }
}

@Composable
@Preview(showBackground = true)
fun PreviewWakelocksFilterSection() {
    WakelocksFilterSection(
        currentFilter = WakelockFilterOption.ALL,
        onFilterChanged = {}
    )
} 