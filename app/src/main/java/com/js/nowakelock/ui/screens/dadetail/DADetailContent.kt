package com.js.nowakelock.ui.screens.dadetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.js.nowakelock.data.model.DAInfoEntry
import com.js.nowakelock.data.model.DAItem
import com.js.nowakelock.data.model.DAStatistics
import com.js.nowakelock.data.model.EventItem
import com.js.nowakelock.data.model.HourData

/**
 * Main content for the device automation detail screen.
 * Displays all information about a device automation item.
 * 
 * @param state The success state containing all data
 * @param settingsState The current settings state
 * @param onBlockingSettingChanged Callback for when the blocking setting changes
 * @param onConditionSettingsChanged Callback for when condition settings change
 * @param onTimeIntervalChanged Callback for when the time interval changes
 * @param modifier Optional modifier for the component
 */
@Composable
fun DADetailContent(
    state: DADetailState.Success,
    settingsState: DASettingsState,
    onBlockingSettingChanged: (Boolean) -> Unit,
    onConditionSettingsChanged: (Boolean, Boolean) -> Unit,
    onTimeIntervalChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header section
        item {
            DAHeaderSection(
                daItem = state.daItem
            )
        }
        
        // Statistics card
        item {
            DAStatisticsCard(
                statistics = state.statistics
            )
        }
        
        // Info card (only if info is available)
        item {
            DAInfoCard(
                info = state.info,
                type = state.daItem.type
            )
        }
        
        // Settings card
        item {
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
        
        // Recent activities card
        item {
            DARecentActivitiesCard(
                activities = state.recentEvents,
                type = state.daItem.type
            )
        }
    }
} 