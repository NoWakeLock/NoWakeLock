package com.js.nowakelock.ui.screens.dadetail.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.js.nowakelock.R
import com.js.nowakelock.data.model.EventItem

/**
 * A component displaying a single activity item for a device automation event.
 * 
 * @param event The event item to display
 * @param modifier Optional modifier for the component
 */
@Composable
fun ActivityItem(
    event: EventItem,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        // Status indicator (allowed or blocked)
        Surface(
            color = if (!event.isBlocked) Color(0xFF36B37E) else MaterialTheme.colorScheme.primary,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = if (!event.isBlocked) Icons.Default.Check else Icons.Default.Close,
                contentDescription = if (!event.isBlocked) 
                    stringResource(R.string.status_allowed) 
                else 
                    stringResource(R.string.status_blocked),
                tint = Color.White,
                modifier = Modifier.padding(4.dp)
            )
        }
        
        // Time and duration
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
        ) {
            Text(
                text = stringResource(
                    if (!event.isBlocked) R.string.status_allowed else R.string.status_blocked
                ),
                style = MaterialTheme.typography.titleSmall,
                color = if (!event.isBlocked) Color(0xFF36B37E) else MaterialTheme.colorScheme.primary
            )
            Row {
                Text(
                    text = event.formattedTime,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.duration_separator, event.formattedDuration),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Preview for ActivityItem in allowed state
 */
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun ActivityItemAllowedPreview() {
    val mockEvent = EventItem(
        time = System.currentTimeMillis(),
        duration = 120000, // 2 minutes
        isBlocked = false,
        formattedTime = "1:30 PM",
        formattedDuration = "2m 0s"
    )
    
    androidx.compose.material3.Surface {
        ActivityItem(event = mockEvent)
    }
}

/**
 * Preview for ActivityItem in blocked state
 */
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun ActivityItemBlockedPreview() {
    val mockEvent = EventItem(
        time = System.currentTimeMillis(),
        duration = 180000, // 3 minutes
        isBlocked = true,
        formattedTime = "2:45 PM",
        formattedDuration = "3m 0s"
    )
    
    androidx.compose.material3.Surface {
        ActivityItem(event = mockEvent)
    }
} 