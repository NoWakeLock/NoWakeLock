package com.js.nowakelock.ui.screens.dadetail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.js.nowakelock.data.db.Type
import com.js.nowakelock.data.model.DAInfo
import com.js.nowakelock.data.model.DAItem

/**
 * Combined header card component that includes the icon, title, package name,
 * and statistics data with a divider in between.
 */
@Composable
fun DADetailHeaderCard(
    daItem: DAItem,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header part - Icon, name and package
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon part from original header component
                DAIconWithBackground(daItem = daItem)

                // Name and package part
                Column(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .weight(1f)
                ) {
                    Text(
                        text = daItem.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = daItem.packageName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Simple divider
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // Statistics part
            DAStatisticsRow(daItem = daItem)
        }
    }
}

/**
 * Statistics row component displaying count, blocked, total time and saved time
 */
@Composable
private fun DAStatisticsRow(
    daItem: DAItem,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
//        horizontalArrangement = Arrangement.SpaceEvenly
        horizontalArrangement = if (daItem.type == Type.Wakelock)
            Arrangement.SpaceEvenly else Arrangement.SpaceAround
    ) {
        // Count statistic
        StatisticItem(
            value = daItem.count.toString(),
            label = "Count"
        )

        // Blocked statistic
        StatisticItem(
            value = daItem.blockCount.toString(),
            label = "Blocked"
        )

        if (daItem.type == Type.Wakelock) {
            // Total time statistic
            StatisticItem(
                value = daItem.getCountTimeFormat(),
                label = "Total time"
            )

            // Saved time statistic
            StatisticItem(
                value = daItem.getBlockCountTimeFormat(),
                label = "Saved time"
            )
        }
    }
}

@Composable
fun DAIconWithBackground(
    daItem: DAItem,
    modifier: Modifier = Modifier
) {
    val icon = when (daItem.type) {
        Type.Wakelock -> Icons.Outlined.Lock
        Type.Alarm -> Icons.Outlined.AccessTime
        Type.Service -> Icons.Outlined.Build
        Type.UnKnow -> Icons.Outlined.Lock
    }

    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "${daItem.type.name} icon",
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * Individual statistic item with value and label
 */
@Composable
private fun StatisticItem(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
fun DADetailHeaderCardPreview() {
    val mockDAItem = DAItem(
        name = "SampleWakelock",
        packageName = "com.example.app",
        type = Type.Wakelock,
        count = 100,
        blockCount = 50,
        countTime = 3600000L, // 1 hour in milliseconds
        blockCountTime = 1800000L // 30 minutes in milliseconds
    )

    DADetailHeaderCard(daItem = mockDAItem)
}