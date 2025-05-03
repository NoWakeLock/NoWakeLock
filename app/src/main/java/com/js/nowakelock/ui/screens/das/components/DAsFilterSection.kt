package com.js.nowakelock.ui.screens.das.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.js.nowakelock.R
import com.js.nowakelock.ui.screens.das.DAFilterOption

/**
 * Filter section component for the wakelocks screen
 * Provides filter buttons to select between All, Blocked and Allowed wakelocks
 * Styled to match AppsFilterSection for UI consistency
 */
@Composable
fun DAFilterSection(
    currentFilter: DAFilterOption,
    onFilterChanged: (DAFilterOption) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp), // Reduced padding
        horizontalArrangement = Arrangement.spacedBy(6.dp), // Reduced spacing
        verticalAlignment = Alignment.CenterVertically
    ) {
        // All filter
        FilterButton(
            text = stringResource(R.string.menu_filter_all),
            isSelected = currentFilter == DAFilterOption.ALL,
            onClick = { onFilterChanged(DAFilterOption.ALL) },
            modifier = Modifier.weight(1f)
        )
        
        // Blocked filter
        FilterButton(
            text = stringResource(R.string.menu_filter_blocked),
            isSelected = currentFilter == DAFilterOption.BLOCKED,
            onClick = { onFilterChanged(DAFilterOption.BLOCKED) },
            modifier = Modifier.weight(1f),
            selectedColor = MaterialTheme.colorScheme.errorContainer,
            selectedContentColor = MaterialTheme.colorScheme.onErrorContainer
        )
        
        // Allowed filter
        FilterButton(
            text = stringResource(R.string.menu_filter_allowed),
            isSelected = currentFilter == DAFilterOption.ALLOWED,
            onClick = { onFilterChanged(DAFilterOption.ALLOWED) },
            modifier = Modifier.weight(1f),
            selectedColor = MaterialTheme.colorScheme.tertiaryContainer,
            selectedContentColor = MaterialTheme.colorScheme.onTertiaryContainer
        )
        
        // Invisible fourth filter (placeholder to match Apps screen's layout)
        Box(
            modifier = Modifier
                .weight(1f)
                .alpha(0f) // Completely invisible
                .semantics { 
                    contentDescription = "" // Empty for accessibility services to ignore
                }
        ) {
            // Empty box with same size/weight characteristics
            Surface(
                shape = MaterialTheme.shapes.large,
                color = Color.Transparent,
                contentColor = Color.Transparent,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier.padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "",
                        style = MaterialTheme.typography.labelMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * Filter button component to ensure consistent styling across filters
 */
@Composable
private fun FilterButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedColor: Color = MaterialTheme.colorScheme.primaryContainer,
    selectedContentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    Surface(
        modifier = modifier,
        onClick = onClick,
        shape = MaterialTheme.shapes.large, // More rounded corners
        color = if (isSelected) 
            selectedColor 
        else 
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), // Lighter background when not selected
        contentColor = if (isSelected)
            selectedContentColor
        else
            MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp, // Flatter appearance
        shadowElevation = if (isSelected) 1.dp else 0.dp, // Minimal shadow
        border = BorderStroke(
            width = 0.5.dp,
            color = if (isSelected)
                selectedColor.copy(alpha = 0.5f)
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
                text = text,
                style = MaterialTheme.typography.labelMedium, // Slightly smaller text
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
fun PreviewWakelocksFilterSection() {
    DAFilterSection(
        currentFilter = DAFilterOption.ALL,
        onFilterChanged = {}
    )
} 