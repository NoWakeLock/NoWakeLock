package com.js.nowakelock.ui.screens.dadetail.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.js.nowakelock.R
import com.js.nowakelock.data.db.Type
import com.js.nowakelock.data.model.EventItem
import com.js.nowakelock.ui.components.InfoCard

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
                    Divider(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        thickness = 0.5.dp
                    )
                }
            }
        }
    }
} 