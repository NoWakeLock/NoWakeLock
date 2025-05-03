package com.js.nowakelock.ui.screens.dadetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.js.nowakelock.data.db.Type
import com.js.nowakelock.data.model.DAInfoEntry
import com.js.nowakelock.data.model.DAItem
import com.js.nowakelock.data.model.DAStatistics
import com.js.nowakelock.data.model.EventItem
import com.js.nowakelock.data.model.HourData
import com.js.nowakelock.ui.screens.dadetail.components.DADetailHeaderCard
import com.js.nowakelock.ui.screens.dadetail.components.DAInfoCard
import com.js.nowakelock.ui.screens.dadetail.components.DARecentActivitiesCard
import com.js.nowakelock.ui.screens.dadetail.components.DASettingsCard
import com.js.nowakelock.ui.screens.dadetail.components.DATimelineCard

/**
 * Main content for the device automation detail screen.
 * Displays all information about a device automation item.
 *
 * @param state The success state containing all data
 * @param onBlockingSettingChanged Callback for when the blocking setting changes
 * @param onConditionSettingsChanged Callback for when condition settings change
 * @param onTimeIntervalChanged Callback for when the time interval changes
 * @param modifier Optional modifier for the component
 */
@Composable
fun DADetailContent(
    state: DADetailState.Success,
    onBlockingSettingChanged: (Boolean) -> Unit,
    onConditionSettingsChanged: (Boolean) -> Unit,
    onTimeIntervalChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val daItem = state.daItem
        item {
            DADetailHeaderCard(daItem = daItem)
        }

        // Info card (only if info is available)
        item {
            DAInfoCard(
                info = state.info,
                type = state.daItem.type
            )
        }

        // Settings card - 从daItem创建settingsState
        item {
            // 从DAItem提取设置信息创建DASettingsState
            val settingsState = DASettingsState(
                fullBlocked = daItem.fullBlocked,
                screenOffBlock = daItem.screenOffBlock,
                timeInterval = daItem.timeWindowSec,
                type = daItem.type
            )
            
            DASettingsCard(
                settingsState = settingsState,
                onBlockingSettingChanged = onBlockingSettingChanged,
                onConditionSettingsChanged = onConditionSettingsChanged,
                onTimeIntervalChanged = onTimeIntervalChanged
            )
        }

        // Timeline card
        item {
            DATimelineCard(
                timelineData = state.timelineData
            )
        }

        //TODO(Recent activities not work good)
//        item {
//            DARecentActivitiesCard(
//                activities = state.recentEvents,
//                type = state.daItem.type
//            )
//        }
    }
}

/**
 * Preview for DADetailContent
 */
@Composable
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
fun DADetailContentPreview() {
    val mockDAItem = DAItem(
        name = "SampleWakelock",
        packageName = "com.example.app",
        type = Type.Wakelock,
        count = 100,
        blockCount = 30,
        countTime = 3600000 // 1 hour
    )

    val mockStatistics = DAStatistics(
        totalCount = 100,
        blockedCount = 30,
        totalTime = 3600000,
        savedTime = 1080000,
        formattedTotalTime = "1h 0m",
        formattedSavedTime = "18m saved"
    )

    val mockTimelineData = List(24) { hour ->
        HourData(
            hour = hour,
            label = if (hour == 0) "12AM" else if (hour < 12) "${hour}AM" else if (hour == 12) "12PM" else "${hour - 12}PM",
            total = (5..20).random(),
            blocked = (0..5).random()
        )
    }

    val mockEvents = List(5) { index ->
        EventItem(
            type = Type.Wakelock,
            time = System.currentTimeMillis() - (index * 3600000),
            duration = (10000..300000).random().toLong(),
            isBlocked = index % 2 == 0,
            formattedTime = "1:30 PM",
            formattedDuration = "${(1..5).random()}m ${(1..59).random()}s"
        )
    }

    val successState = DADetailState.Success(
        daItem = mockDAItem,
        info = DAInfoEntry(
            id = "sample_wakelock",
            name = "SampleWakelock",
            type = Type.Wakelock,
            packageName = "com.example.app",
            safeToBlock = "safe",
            description = "This is a sample wakelock for preview purposes.",
            recommendation = "Can be safely blocked when screen is off.",
            warning = null,
            tags = listOf("battery", "background")
        ),
        statistics = mockStatistics,
        timelineData = mockTimelineData,
        recentEvents = mockEvents
    )

    androidx.compose.material3.Surface {
        DADetailContent(
            state = successState,
            onBlockingSettingChanged = {},
            onConditionSettingsChanged = { _ -> },
            onTimeIntervalChanged = {}
        )
    }
} 