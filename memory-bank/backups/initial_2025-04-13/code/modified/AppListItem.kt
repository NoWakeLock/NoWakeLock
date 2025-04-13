// Partial backup of AppListItem.kt
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
        // ... rest of the implementation not included in this backup for brevity
    }
} 