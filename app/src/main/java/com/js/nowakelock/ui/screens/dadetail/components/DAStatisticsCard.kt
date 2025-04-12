package com.js.nowakelock.ui.screens.dadetail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.js.nowakelock.R
import com.js.nowakelock.data.model.DAStatistics

/**
 * A card displaying statistics for a device automation item.
 * Shows total count, blocked count, total time, and saved time.
 *
 * @param statistics The statistics to display
 * @param modifier Optional modifier for the component
 */
@Composable
fun DAStatisticsCard(
    statistics: DAStatistics,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Total count
            StatisticItem(
                value = statistics.totalCount.toString(),
                label = stringResource(R.string.stat_total_count),
                modifier = Modifier.weight(1f)
            )

            // Blocked count
            StatisticItem(
                value = statistics.blockedCount.toString(),
                label = stringResource(R.string.stat_blocked_count),
                modifier = Modifier.weight(1f)
            )

            // Total time
            StatisticItem(
                value = statistics.formattedTotalTime,
                label = stringResource(R.string.stat_total_time),
                modifier = Modifier.weight(1f)
            )

            // Saved time
            StatisticItem(
                value = statistics.formattedSavedTime,
                label = stringResource(R.string.stat_saved_time),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * A single statistic item with a value and label.
 */
@Composable
private fun StatisticItem(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}