package com.js.nowakelock.ui.screens.apps.components

import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.graphics.drawable.toBitmap
import com.js.nowakelock.R
import com.js.nowakelock.data.db.entity.AppInfo
import com.js.nowakelock.data.model.AppWithStats

/**
 * List item component displays app info with wakelock statistics
 * Following Material Design 3 best practices with simplified layout structure
 * Uses native components and flatter hierarchy for better performance
 */
@Composable
fun AppListItem(
    appWithStats: AppWithStats,
    onItemClick: (AppWithStats) -> Unit
) {
    val context = LocalContext.current

    // Get app icon using PackageManager
    val appIcon = remember {
        try {
            context.packageManager.getApplicationIcon(appWithStats.appInfo.packageName)
                .toBitmap()
                .asImageBitmap()
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    // Determine if this is a system app
    val isSystemApp = appWithStats.appInfo.system

    // Use Surface with subtle elevation to create the floating card effect
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        onClick = { onItemClick(appWithStats) },
        shape = RoundedCornerShape(16.dp), // Slightly more rounded corners for the card
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.5.dp, // Very subtle tonal elevation for depth
        shadowElevation = 1.dp // Minimal shadow for the floating effect
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 72.dp)
                .let {
                    // primary color for system apps
                    val indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    it.drawWithContent {
                        if (isSystemApp) {
                            val barHeight = 48.dp.toPx()
                            val yOffset = (size.height - barHeight) / 2

                            drawRoundRect(
                                color = indicatorColor,
                                topLeft = androidx.compose.ui.geometry.Offset(6.dp.toPx(), yOffset),
                                size = Size(3.dp.toPx(), barHeight),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.5.dp.toPx())
                            )
                        }
                        drawContent()
                    }
                }
                .padding(16.dp)
        ) {
            // App icon with rounded rectangle shape (less rounded corners)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp)) // Slightly rounded corners for icon
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.takeIf { appIcon == null }
                            ?: Color.Transparent
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (appIcon != null) {
                    Image(
                        bitmap = appIcon,
                        contentDescription = "Icon for ${appWithStats.appInfo.label}",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Fallback if icon can't be loaded
                    Text(
                        text = appWithStats.appInfo.label.take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Text and chips content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                // App name
                Text(
                    text = appWithStats.appInfo.label,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Package name
                Text(
                    text = appWithStats.appInfo.packageName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Indicator chips using simplified Surface components
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Wakelock count indicator
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
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${appWithStats.wakelockCount + appWithStats.alarmCount + appWithStats.serviceCount}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    // Duration indicator
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
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = appWithStats.getFormattedTime(),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
@Preview
fun PreviewAppListItem() {
    MaterialTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                // Regular app
                val appWithStats = AppWithStats(
                    appInfo = AppInfo(
                        packageName = "com.example.app",
                        label = "Example App",
                        system = false,
                        icon = R.drawable.ic_launcher_foreground,
                    ),
                    wakelockCount = 5,
                    wakelockTime = 3600L // 1 hour in seconds
                )
                AppListItem(appWithStats) {}

                // System app
                val systemAppWithStats = AppWithStats(
                    appInfo = AppInfo(
                        packageName = "com.android.system",
                        label = "System App",
                        system = true,
                        icon = R.drawable.ic_launcher_foreground,
                    ),
                    wakelockCount = 12,
                    wakelockTime = 7200L // 2 hours in seconds
                )
                AppListItem(systemAppWithStats) {}
            }
        }
    }
}