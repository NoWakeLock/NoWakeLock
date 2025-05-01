package com.js.nowakelock.ui.screens.dadetail.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
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
import com.js.nowakelock.data.db.Type
import com.js.nowakelock.data.model.EventItem

/**
 * A card displaying recent activities for a device automation item.
 *
 * @param activities The list of recent activities to display
 * @param type The type of the device automation item, used for empty state message
 * @param modifier Optional modifier for the component
 */
@Composable
fun DARecentActivitiesCard(
    activities: List<EventItem>,
    type: Type,
    modifier: Modifier = Modifier
) {
    InfoCard(
        title = stringResource(R.string.recent_activities),
        modifier = modifier
    ) {
        if (activities.isEmpty()) {
            // Display type-specific empty state message
            val emptyMessage = when (type) {
                Type.Wakelock -> stringResource(R.string.no_recent_wakelock_activities)
                Type.Alarm -> stringResource(R.string.no_recent_alarm_activities)
                Type.Service -> stringResource(R.string.no_recent_service_activities)
                Type.UnKnow -> stringResource(R.string.no_recent_activities)
            }

            Text(
                text = emptyMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            // Display activity items with dividers
            activities.forEachIndexed { index, activity ->
                ActivityItem(
                    event = activity,
                    modifier = Modifier.fillMaxWidth()
                )

                // Add divider except after the last item
                if (index < activities.size - 1) {
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
        }
    }
}

/**
 * Preview for DARecentActivitiesCard with activities
 */
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun DARecentActivitiesCardPreview() {
    val mockActivities = List(5) { index ->
        EventItem(
            type = Type.Wakelock,
            time = System.currentTimeMillis() - (index * 3600000),
            duration = (10000..300000).random().toLong(),
            isBlocked = index % 2 == 0,
            formattedTime = "1:30 PM",
            formattedDuration = "${(1..5).random()}m ${(1..59).random()}s"
        )
    }

    Surface {
        DARecentActivitiesCard(
            activities = mockActivities,
            type = Type.Wakelock
        )
    }
}

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
            .padding(vertical = 8.dp)
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
                if (event.type != Type.Wakelock) {
                    Text(
                        text = stringResource(R.string.duration_separator, event.formattedDuration),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
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
        type = Type.Wakelock,
        time = System.currentTimeMillis(),
        duration = 120000, // 2 minutes
        isBlocked = false,
        formattedTime = "1:30 PM",
        formattedDuration = "2m 0s"
    )

    Surface {
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
        type = Type.Wakelock,
        time = System.currentTimeMillis(),
        duration = 180000, // 3 minutes
        isBlocked = true,
        formattedTime = "2:45 PM",
        formattedDuration = "3m 0s"
    )

    Surface {
        ActivityItem(event = mockEvent)
    }
}

/**
 * Preview for DARecentActivitiesCard with empty activities
 */
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun DARecentActivitiesCardEmptyPreview() {
    Surface {
        DARecentActivitiesCard(
            activities = emptyList(),
            type = Type.Alarm
        )
    }
} 