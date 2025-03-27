package com.js.nowakelock.ui.screens.wakelocks.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.js.nowakelock.data.model.WakelockItem
import com.js.nowakelock.ui.theme.AllowedGreen
import com.js.nowakelock.ui.theme.BlockedRed

/**
 * WakelockListItem displays a single wakelock with its settings
 * Follows Material Design 3 principles with a clean layout
 * Includes status indicator, info section, and quick settings controls
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WakelockListItem(
    wakelockItem: WakelockItem,
    onToggleBlock: (Boolean) -> Unit,
    onTimeWindowChange: (Int) -> Unit,
    onItemClick: (WakelockItem) -> Unit = {}
) {
    // Status bar color based on wakelock state
    val statusBarColor = when {
        wakelockItem.isBlocked && wakelockItem.timeWindow == null -> BlockedRed
        wakelockItem.isBlocked && wakelockItem.timeWindow != null -> Color(0xFFFF9800) // Orange
        else -> AllowedGreen
    }
    
    // For time window input
    var timeWindowText by remember { 
        mutableStateOf(wakelockItem.timeWindow?.toString() ?: "60") 
    }
    
    // TimeWindow input state
    var isTimeWindowEditing by remember { mutableStateOf(false) }
    
    // Row container
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 80.dp)
            .clickable { onItemClick(wakelockItem) }
            .padding(end = 16.dp)
    ) {
        // Status indicator bar
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(color = statusBarColor)
        )
        
        // Icon
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Bolt,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
        
        // Info column
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 12.dp)
        ) {
            // Wakelock name
            Text(
                text = wakelockItem.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            // Package name
            Text(
                text = wakelockItem.packageName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            // Count
            Text(
                text = "Count: ${wakelockItem.count}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Quick settings
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.padding(start = 8.dp)
        ) {
            // Time window input (only enabled when not fully blocked)
            OutlinedTextField(
                value = timeWindowText,
                onValueChange = { newValue ->
                    // Only accept numeric input
                    if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                        timeWindowText = newValue
                        // Convert to int and notify if valid
                        newValue.toIntOrNull()?.let { intValue ->
                            if (intValue > 0) {
                                onTimeWindowChange(intValue)
                            }
                        }
                    }
                },
                label = { Text("Seconds", style = MaterialTheme.typography.labelSmall) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle = MaterialTheme.typography.bodyMedium,
                enabled = !wakelockItem.isBlocked,
                modifier = Modifier
                    .width(90.dp)
                    .height(60.dp),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Block switch
            Switch(
                checked = wakelockItem.isBlocked,
                onCheckedChange = onToggleBlock,
                thumbContent = if (wakelockItem.isBlocked) {
                    {
                        Icon(
                            imageVector = Icons.Outlined.Bolt,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else null
            )
        }
    }
    
    // Divider
    HorizontalDivider(
        modifier = Modifier
            .padding(start = 16.dp)
            .fillMaxWidth(),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

@Composable
@Preview(showBackground = true)
fun PreviewWakelockListItem() {
    // Sample wakelock for preview
    val wakelockItem = WakelockItem(
        name = "KEEP_SCREEN_ON_FLAG",
        packageName = "com.facebook.katana",
        count = 47,
        blockCount = 12,
        countTime = 8100000, // 2h 15m in milliseconds
        isBlocked = true,
        timeWindow = null
    )
    
    WakelockListItem(
        wakelockItem = wakelockItem,
        onToggleBlock = {},
        onTimeWindowChange = {}
    )
}

@Composable
@Preview(showBackground = true)
fun PreviewWakelockListItemWithTimeWindow() {
    // Sample wakelock for preview with time window
    val wakelockItem = WakelockItem(
        name = "GCM_RECONNECT_WAKELOCK",
        packageName = "com.google.android.gms",
        count = 36,
        blockCount = 0,
        countTime = 5400000, // 1h 30m in milliseconds
        isBlocked = false,
        timeWindow = 60
    )
    
    WakelockListItem(
        wakelockItem = wakelockItem,
        onToggleBlock = {},
        onTimeWindowChange = {}
    )
} 