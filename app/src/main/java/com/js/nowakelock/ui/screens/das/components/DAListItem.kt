package com.js.nowakelock.ui.screens.das.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
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
 * Redesigned with Card layout for better presentation
 * Optimized for information density following MD3 guidelines
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
    // get status color
    val statusColor = getStatusColor(daItem)

    // For time window input
    var timeWindowText by remember {
        mutableStateOf(daItem.timeWindowSec?.toString() ?: "0")
    }

    // Card with status bar indicator
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.5.dp,
        shadowElevation = 1.dp
    ) {
        // measure content, then apply correct height to status bar
        SubcomposeLayout { constraints ->
            // measure content
            val contentPlaceable = subcompose("content") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    // Info section
                    InfoSection(daItem)

                    // Subtle divider
                    HorizontalDivider(
                        modifier = Modifier
                            .padding(horizontal = 0.dp)
                            .fillMaxWidth(),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // Control section
                    ControlSection(
                        daItem = daItem,
                        timeWindowText = timeWindowText,
                        onTimeWindowTextChange = { newValue ->
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
                        onToggleFullBlock = onToggleFullBlock,
                        onToggleScreenOffBlock = onToggleScreenOffBlock
                    )
                }
            }.first().measure(constraints)

            // measure content height
            val contentHeight = contentPlaceable.height
            val indicatorPlaceable = subcompose("indicator") {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(contentHeight.toDp())
                        .background(
                            color = statusColor,
                            shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                        )
                )
            }.first()
                .measure(constraints.copy(minWidth = 4.dp.roundToPx(), maxWidth = 4.dp.roundToPx()))

            // layout content
            layout(constraints.maxWidth, contentHeight) {
                // place content (set padding to leave space for status bar)
                contentPlaceable.place(4.dp.roundToPx(), 0)
                // place status bar
                indicatorPlaceable.place(0, 0)
            }
        }
    }
}

/**
 * get status color
 * follow Material Design 3 color system semantic colors
 * there are 5 states:
 * 1. no restrictions
 * 2. full block
 * 3. screen off block only
 * 4. time window block only
 * 5. screen off + time window block
 */
@Composable
private fun getStatusColor(daItem: DAItem): Color {
    return when {
        // 1. full block
        daItem.fullBlocked ->
            MaterialTheme.colorScheme.error.copy(alpha = 0.85f)

        // 2. screen off block + time window block
        daItem.screenOffBlock && daItem.timeWindowSec != 0 ->
            Color(0xFFE65100).copy(alpha = 0.85f) // Deep Orange

        // 3. screen off block only
        daItem.screenOffBlock ->
            Color(0xFFFF9800).copy(alpha = 0.85f) // Orange

        // 4. time window block only
        daItem.timeWindowSec != 0 ->
            Color(0xFFFFC107).copy(alpha = 0.85f) // Amber

        // 5. no restrictions
        else ->
            AllowedGreen.copy(alpha = 0.85f)
    }
}

/**
 * Info section containing icon, name, package name and stats
 * Optimized for density and vertical space
 */
@Composable
private fun InfoSection(daItem: DAItem) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        val (icon, name, packageName, count, countTime) = createRefs()

        // Icon
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .constrainAs(icon) {
                    start.linkTo(parent.start, 8.dp)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                }, contentAlignment = Alignment.Center
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

        // Name - put stats on same row for longer names
        Text(
            text = daItem.name,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.constrainAs(name) {
                width = Dimension.fillToConstraints
                start.linkTo(icon.end, 12.dp)
                end.linkTo(parent.end)
                top.linkTo(parent.top)
            })


        // Package name
        Text(
            text = daItem.packageName,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.constrainAs(packageName) {
                start.linkTo(icon.end, 12.dp)
                top.linkTo(name.bottom, 4.dp)
            })

        // Count chip - more compact
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier
                .height(22.dp)
                .constrainAs(count) {
                    start.linkTo(icon.end, 12.dp)
                    top.linkTo(packageName.bottom, 8.dp)
                }) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Bolt,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(Modifier.width(2.dp))
                Text(
                    text = "${daItem.count}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        // 仅在 Wakelock 类型时显示唤醒时间
        if (daItem.type == Type.Wakelock) {
            Surface(
                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .height(22.dp)
                    .constrainAs(countTime) {
                        start.linkTo(count.end, 8.dp)
                        top.linkTo(packageName.bottom, 8.dp)
                    }) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Spacer(Modifier.width(2.dp))
                    Text(
                        text = formatTime(daItem.countTime),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }
    }
}

/**
 * Control section containing switches and time window input
 * Optimized for compact layout
 */
@Composable
private fun ControlSection(
    daItem: DAItem,
    timeWindowText: String,
    onTimeWindowTextChange: (String) -> Unit,
    onToggleFullBlock: (Boolean) -> Unit,
    onToggleScreenOffBlock: (Boolean) -> Unit
) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        val (blockLabel, block, screenOffLabel, screenOffBlock, timeLabel, time) = createRefs()
        // Block section
        // Full block label and switch
        Text(
            text = "Allow",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.constrainAs(blockLabel) {
                start.linkTo(parent.start, 16.dp)
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
            },
        )
        Switch(
            checked = !daItem.fullBlocked,
            onCheckedChange = onToggleFullBlock,
            modifier = Modifier
                .scale(0.75f)
                .constrainAs(block) {
                    start.linkTo(blockLabel.end, 4.dp)
                    top.linkTo(blockLabel.top)
                    bottom.linkTo(blockLabel.bottom)
                },

            thumbContent = if (daItem.fullBlocked) {
                {
                    Icon(
                        imageVector = Icons.Outlined.Bolt,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp)
                    )
                }
            } else null)

        // Screen off label and switch
        Text(
            text = "Screen Off",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.constrainAs(screenOffLabel) {
                start.linkTo(block.end, 8.dp)
                top.linkTo(block.top)
                bottom.linkTo(block.bottom)
            })
        Switch(
            checked = !daItem.screenOffBlock,
            onCheckedChange = onToggleScreenOffBlock,
            enabled = !daItem.fullBlocked,
            modifier = Modifier
                .scale(0.75f)
                .constrainAs(screenOffBlock) {
                    start.linkTo(screenOffLabel.end, 4.dp)
                    top.linkTo(screenOffLabel.top)
                    bottom.linkTo(screenOffLabel.bottom)
                })


        // Timeout section
        Text(
            text = "Time",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.constrainAs(timeLabel) {
                start.linkTo(screenOffBlock.end, 8.dp)
                top.linkTo(screenOffBlock.top)
                bottom.linkTo(screenOffBlock.bottom)
            })

        // BasicTextField
        BasicTextField(
            value = timeWindowText,
            onValueChange = onTimeWindowTextChange,
            modifier = Modifier
                .width(65.dp)
                .height(36.dp)
                .border(
                    width = 1.dp,
                    color = if (!daItem.fullBlocked) MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .constrainAs(time) {
                    start.linkTo(timeLabel.end, 0.dp)
                    top.linkTo(timeLabel.top)
                    bottom.linkTo(timeLabel.bottom)
                    end.linkTo(parent.end)
                },
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                textAlign = TextAlign.End,
                color = if (!daItem.fullBlocked)
                    MaterialTheme.colorScheme.onSurface
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            enabled = !daItem.fullBlocked,
            singleLine = true,
            decorationBox = { innerTextField ->
                if (timeWindowText.isEmpty()) {
                    Text(
                        text = "0",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                        textAlign = TextAlign.End
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "s",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (!daItem.fullBlocked)
                            MaterialTheme.colorScheme.onSurfaceVariant
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.weight(1f)) {
                            innerTextField()
                        }
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "s",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (!daItem.fullBlocked)
                                MaterialTheme.colorScheme.onSurfaceVariant
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                        )
                    }
                }
            }
        )
    }
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
    // Sample for preview
    val dAItem = DAItem(
        name = "KEEP_SCREEN_ON_FLAG",
        packageName = "com.facebook.katana",
        count = 47,
        blockCount = 12,
        countTime = 8100000, // 2h 15m in milliseconds
        fullBlocked = true,
        timeWindowSec = 0,
        screenOffBlock = false,
        type = Type.Wakelock
    )

    DAListItem(
        daItem = dAItem,
        onToggleFullBlock = {},
        onToggleScreenOffBlock = {},
        onTimeWindowChange = {})
}

@Composable
@Preview(showBackground = true)
fun Preview2() {
    // Sample for preview
    val dAItem = DAItem(
        name = "KEEP_SCREEN_ON_FLAG",
        packageName = "com.facebook.katana",
        count = 47,
        blockCount = 12,
        countTime = 8100000, // 2h 15m in milliseconds
        fullBlocked = false,
        timeWindowSec = 1,
        screenOffBlock = true,
        type = Type.Wakelock
    )

    DAListItem(
        daItem = dAItem,
        onToggleFullBlock = {},
        onToggleScreenOffBlock = {},
        onTimeWindowChange = {})
}

@Composable
@Preview(showBackground = true)
fun PreviewDAListItemWithTimeWindow() {
    // Sample wakelock for preview with time window
    val dAItem = DAItem(
        name = "GCM_RECONNECT_WAKELOCK",
        packageName = "com.google.android.gms",
        count = 36,
        blockCount = 0,
        countTime = 5400000, // 1h 30m in milliseconds
        fullBlocked = false,
        timeWindowSec = 60,
        screenOffBlock = false,
        type = Type.Wakelock
    )

    DAListItem(
        daItem = dAItem,
        onToggleFullBlock = {},
        onToggleScreenOffBlock = {},
        onTimeWindowChange = {})
}