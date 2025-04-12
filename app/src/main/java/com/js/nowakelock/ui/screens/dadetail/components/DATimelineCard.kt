package com.js.nowakelock.ui.screens.dadetail.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.js.nowakelock.R
import com.js.nowakelock.data.model.HourData
import com.js.nowakelock.ui.components.InfoCard

/**
 * A card displaying a timeline of activity for a device automation item.
 * 
 * @param timelineData The hourly data to display in the timeline
 * @param modifier Optional modifier for the component
 */
@Composable
fun DATimelineCard(
    timelineData: List<HourData>,
    modifier: Modifier = Modifier
) {
    InfoCard(
        title = stringResource(R.string.activity_timeline),
        modifier = modifier
    ) {
        Text(
            text = stringResource(R.string.past_24_hours),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Display legend
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            // Total legend item
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 16.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(12.dp)
                ) {}
                Text(
                    text = stringResource(R.string.total),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
            
            // Blocked legend item
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(12.dp)
                ) {}
                Text(
                    text = stringResource(R.string.blocked),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
        
        // Timeline chart
        TimelineChart(
            data = timelineData,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )
    }
}

/**
 * Preview for DATimelineCard
 */
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun DATimelineCardPreview() {
    val mockTimelineData = List(24) { hour ->
        HourData(
            hour = hour,
            label = if (hour == 0) "12AM" else if (hour < 12) "${hour}AM" else if (hour == 12) "12PM" else "${hour-12}PM",
            total = (5..20).random(),
            blocked = (0..5).random()
        )
    }
    
    androidx.compose.material3.Surface {
        DATimelineCard(timelineData = mockTimelineData)
    }
}

/**
 * Preview for DATimelineCard with empty data
 */
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun DATimelineCardEmptyPreview() {
    androidx.compose.material3.Surface {
        DATimelineCard(timelineData = emptyList())
    }
} 