package com.js.nowakelock.ui.screens.dadetail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Api
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.js.nowakelock.data.db.Type
import com.js.nowakelock.data.model.DAItem

/**
 * Header section for the device automation detail screen.
 * Shows the item name, package name, and an icon based on the type.
 * 
 * @param daItem The device automation item to display
 * @param modifier Optional modifier for the component
 */
@Composable
fun DAHeaderSection(
    daItem: DAItem,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(16.dp)
    ) {
        // Icon based on type
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = getIconForType(daItem.type),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        
        // Name and package
        Column(
            modifier = Modifier.padding(start = 16.dp)
        ) {
            Text(
                text = daItem.name,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = daItem.packageName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Helper function to get the appropriate icon for each type of device automation.
 */
@Composable
private fun getIconForType(type: Type): ImageVector {
    return when (type) {
        Type.Wakelock -> Icons.Default.Bolt
        Type.Alarm -> Icons.Default.Alarm
        Type.Service -> Icons.Default.Api
        else -> Icons.Default.QuestionMark
    }
}

/**
 * Preview for DAHeaderSection
 */
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun DAHeaderSectionPreview() {
    val mockDAItem = DAItem(
        name = "SampleWakelock",
        packageName = "com.example.app",
        type = Type.Wakelock,
        count = 100,
        blockCount = 30,
        countTime = 3600000 // 1 hour
    )
    
    androidx.compose.material3.Surface {
        DAHeaderSection(daItem = mockDAItem)
    }
}

/**
 * Preview for DAHeaderSection with different types
 */
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun DAHeaderSectionTypesPreview() {
    androidx.compose.foundation.layout.Column {
        // Wakelock
        DAHeaderSection(
            daItem = DAItem(
                name = "SampleWakelock",
                packageName = "com.example.app",
                type = Type.Wakelock
            )
        )
        
        // Alarm
        DAHeaderSection(
            daItem = DAItem(
                name = "SampleAlarm",
                packageName = "com.example.app",
                type = Type.Alarm
            )
        )
        
        // Service
        DAHeaderSection(
            daItem = DAItem(
                name = "SampleService",
                packageName = "com.example.app",
                type = Type.Service
            )
        )
    }
}