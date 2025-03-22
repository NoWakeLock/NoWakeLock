package com.js.nowakelock.ui.screens.apps.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.js.nowakelock.data.model.AppWithStats
import com.js.nowakelock.ui.theme.AllowedGreen
import com.js.nowakelock.ui.theme.BlockedRed

/**
 * List item component that displays app info with wakelock statistics
 */
@Composable
fun AppListItem(
    appWithStats: AppWithStats,
    onItemClick: (AppWithStats) -> Unit
) {
    val context = LocalContext.current
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onItemClick(appWithStats) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Icon
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(appWithStats.appInfo.icon)
                    .crossfade(true)
                    .build(),
                contentDescription = "App icon",
                modifier = Modifier
                    .size(48.dp)
                    .clip(MaterialTheme.shapes.small)
            )
            
            // App Details
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                // App Name
                Text(
                    text = appWithStats.appInfo.label,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Package Name
                Text(
                    text = appWithStats.appInfo.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                // App Type (System or User)
                Text(
                    text = if (appWithStats.appInfo.system) "System App" else "User App",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Stats Section
            Column(
                horizontalAlignment = Alignment.End
            ) {
                // WakeLock Count
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "${appWithStats.wakelockCount}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "wakes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // WakeLock Time
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = appWithStats.getFormattedTime(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "active",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Blocked/Allowed Status
                if (appWithStats.wakelockBlockedCount > 0) {
                    Text(
                        text = "${appWithStats.wakelockBlockedCount} blocked",
                        style = MaterialTheme.typography.labelSmall,
                        color = BlockedRed
                    )
                } else if (appWithStats.wakelockCount > 0) {
                    Text(
                        text = "Allowed",
                        style = MaterialTheme.typography.labelSmall,
                        color = AllowedGreen
                    )
                }
            }
        }
    }
} 