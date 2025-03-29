package com.js.nowakelock.ui.screens.das.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.js.nowakelock.data.db.Type
import com.js.nowakelock.data.model.DAItem
import com.js.nowakelock.ui.theme.AllowedGreen
import com.js.nowakelock.ui.theme.BlockedRed

/**
 * WakelockListItem displays a single wakelock with its settings
 * Redesigned with ConstraintLayout for better spacing and alignment
 * Includes status indicator, info section, and compact controls
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DAListItem(
    daItem: DAItem,
    onToggleFullBlock: (Boolean) -> Unit,
    onToggleScreenOffBlock: (Boolean) -> Unit,
    onTimeWindowChange: (Int) -> Unit,
    onItemClick: (DAItem) -> Unit = {}
) {
    // Status bar color based on wakelock state
    val statusBarColor = when {
        daItem.fullBlocked -> BlockedRed.copy(alpha = 0.7f) // Red with alpha
        // only for wakelocks / alarm
        (daItem.type == Type.Wakelock || daItem.type == Type.Alarm) && daItem.timeWindowSec != null -> Color(
            0xFFFF9800
        ).copy(alpha = 0.7f) // Orange
        else -> AllowedGreen.copy(alpha = 0.7f)
    }

    // For time window input
    var timeWindowText by remember {
        mutableStateOf(daItem.timeWindowSec?.toString() ?: "0")
    }

    // Main container with status bar indicator
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .clickable { onItemClick(daItem) }
            .drawWithContent {
                val barHeight = 48.dp.toPx()
                val yOffset = (size.height - barHeight) / 2
                drawRoundRect(
                    color = statusBarColor,
                    topLeft = androidx.compose.ui.geometry.Offset(6.dp.toPx(), yOffset),
                    size = Size(3.dp.toPx(), barHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.5.dp.toPx())
                )
                drawContent()
            }
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp)
    ) {
        ConstraintLayout(modifier = Modifier.fillMaxWidth()) {
            // Create references for the compos
            val (icon, name, packageName, statsRow, timeInput, blockSwitch) = createRefs()

            // Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .constrainAs(icon) {
                        start.linkTo(parent.start)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (daItem.type) {
                        Type.Wakelock -> Icons.Outlined.Lock
                        Type.Alarm -> Icons.Outlined.AccessTime
                        Type.Service -> Icons.Outlined.Build
                        else -> Icons.Outlined.Lock
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }

            // name
            Text(
                text = daItem.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.constrainAs(name) {
                    width = Dimension.fillToConstraints
                    start.linkTo(icon.end, 16.dp)
                    end.linkTo(parent.end)
                    top.linkTo(parent.top)
                }
            )

            // Package name
            Text(
                text = daItem.packageName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.constrainAs(packageName) {
                    start.linkTo(name.start)
                    top.linkTo(name.bottom, 4.dp)
                }
            )

            // Stats row
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.constrainAs(statsRow) {
                    start.linkTo(name.start)
                    top.linkTo(packageName.bottom, 4.dp)
                }
            ) {
                // Count chip
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Bolt,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "${daItem.count}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
                // Time chip, only for wakelocks
                if (daItem.type == Type.Wakelock) {
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AccessTime,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = formatTime(daItem.countTime),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
            }

            // Time window input, only for wakelocks
            if (daItem.type == Type.Wakelock) {
                OutlinedTextField(
                    value = timeWindowText,
                    onValueChange = { newValue ->
                        // only accept digits and empty input
                        if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                            // null or empty input should not trigger onTimeWindowChange
                            if (newValue.isEmpty()) {
                                timeWindowText = "0"
                                onTimeWindowChange(0)
                            } else {
                                timeWindowText = newValue
                                newValue.toIntOrNull()?.let { intValue ->
                                    onTimeWindowChange(intValue)
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .width(90.dp)
                        .constrainAs(timeInput) {
                            end.linkTo(blockSwitch.start, 4.dp)
                            top.linkTo(blockSwitch.top, 4.dp)
                            bottom.linkTo(statsRow.bottom, 4.dp)
                        },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    enabled = !daItem.fullBlocked,
                    singleLine = true,
                    suffix = {
                        Text(
                            text = "s",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                        disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = 0.38f
                        )
                    )
                )
            }

            // Block switch
            Switch(
                checked = !daItem.fullBlocked,
                onCheckedChange = onToggleFullBlock,
                modifier = Modifier
                    .scale(0.9f)
                    .constrainAs(blockSwitch) {
                        end.linkTo(parent.end)
                        top.linkTo(name.bottom, 4.dp)
                        bottom.linkTo(statsRow.bottom)
                    },
                thumbContent = if (daItem.fullBlocked) {
                    {
                        Icon(
                            imageVector = Icons.Outlined.Bolt,
                            contentDescription = null,
                            modifier = Modifier.size(0.dp)
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

/**
 * Helper function to format time in milliseconds to a human-readable string
 */
@Composable
private fun formatTime(timeInMillis: Long): String {
    val seconds = timeInMillis / 1000
    return when {
        seconds < 60 -> "${seconds}s"
        seconds < 3600 -> "${seconds / 60}m"
        else -> "${seconds / 3600}h ${(seconds % 3600) / 60}m"
    }
}

@Composable
@Preview(showBackground = true)
fun PreviewDAListItem() {
    // Sample  for preview
    val dAItem = DAItem(
        name = "KEEP_SCREEN_ON_FLAG",
        packageName = "com.facebook.katana",
        count = 47,
        blockCount = 12,
        countTime = 8100000, // 2h 15m in milliseconds
        fullBlocked = true,
        timeWindowSec = null
    )

    DAListItem(
        daItem = dAItem,
        onToggleFullBlock = {},
        onToggleScreenOffBlock = {},
        onTimeWindowChange = {}
    )
}

@Composable
@Preview(showBackground = true)
fun PreviewDAListItemWithTimeWindow() {
    // Sample wakelock for preview with time window
    val daItem = DAItem(
        name = "GCM_RECONNECT_WAKELOCK",
        packageName = "com.google.android.gms",
        count = 36,
        blockCount = 0,
        countTime = 5400000, // 1h 30m in milliseconds
        fullBlocked = false,
        timeWindowSec = 60
    )

    DAListItem(
        daItem = daItem,
        onToggleFullBlock = {},
        onToggleScreenOffBlock = {},
        onTimeWindowChange = {}
    )
} 