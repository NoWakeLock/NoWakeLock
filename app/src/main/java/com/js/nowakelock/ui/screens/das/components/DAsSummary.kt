package com.js.nowakelock.ui.screens.das.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.js.nowakelock.data.db.Type
import com.js.nowakelock.ui.theme.AllowedGreen
import com.js.nowakelock.ui.theme.BlockedRed

/**
 * Summary component for the DAs screen
 * Shows total DA count and blocked/allowed distribution
 */
@Composable
fun DAsSummary(
    type: Type,
    total: Int,
    blockedCount: Int,
    allowedCount: Int,
    modifier: Modifier = Modifier
) {
    // Calculate percentages
    val blockedPercentage =
        if (total > 0) (blockedCount.toFloat() / total) * 100 else 0f
    val allowedPercentage =
        if (total > 0) (allowedCount.toFloat() / total) * 100 else 0f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Title and total count
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = type.value + " Summary",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "$total",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = "Last 24 hours",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Progress indicator
            LinearProgressIndicator(
                progress = blockedPercentage / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp),
                color = BlockedRed,
                trackColor = AllowedGreen
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Stats breakdown
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${blockedPercentage.toInt()}% Blocked (${blockedCount})",
                    style = MaterialTheme.typography.bodySmall,
                    color = BlockedRed
                )

                Text(
                    text = "${allowedPercentage.toInt()}% Allowed (${allowedCount})",
                    style = MaterialTheme.typography.bodySmall,
                    color = AllowedGreen,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun PreviewDAsSummary() {
    DAsSummary(
        type = Type.Wakelock,
        total = 247,
        blockedCount = 160,
        allowedCount = 87
    )
} 