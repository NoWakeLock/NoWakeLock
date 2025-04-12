package com.js.nowakelock.ui.screens.dadetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.js.nowakelock.R
import com.js.nowakelock.data.db.Type
import com.js.nowakelock.data.model.DAInfoEntry
import com.js.nowakelock.ui.components.InfoCard

/**
 * A card displaying detailed information about a device automation item.
 * Shows description, recommendations, and warnings if available.
 *
 * @param info The detailed information to display (can be null)
 * @param type The type of the device automation item, used for fallback messages
 * @param modifier Optional modifier for the component
 */
@Composable
fun DAInfoCard(
    info: DAInfoEntry?,
    type: Type,
    modifier: Modifier = Modifier
) {
    InfoCard(
        title = stringResource(R.string.about_this_item),
        modifier = modifier
    ) {
        if (info == null) {
            // Display type-specific fallback message when no info is available
            val fallbackMessage = when (type) {
                Type.Wakelock -> stringResource(R.string.no_info_wakelock)
                Type.Alarm -> stringResource(R.string.no_info_alarm)
                Type.Service -> stringResource(R.string.no_info_service)
                Type.UnKnow -> stringResource(R.string.no_info_unknown)
            }

            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = fallbackMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        } else {
            // Description
            Text(
                text = info.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Recommendation (if available)
            if (!info.recommendation.isNullOrBlank()) {
                Text(
                    text = stringResource(R.string.recommendations),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = Color(0xFF36B37E) // Green color for recommendations
                    )
                    Text(
                        text = info.recommendation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Warning (if available)
            if (!info.warning.isNullOrBlank()) {
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFFFC400) // Amber color for warnings
                    )
                    Text(
                        text = info.warning,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}