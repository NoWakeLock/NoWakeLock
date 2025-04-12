package com.js.nowakelock.ui.screens.dadetail.components

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.js.nowakelock.R
import com.js.nowakelock.ui.components.InfoCard
import com.js.nowakelock.ui.components.NumberInputItem
import com.js.nowakelock.ui.components.SwitchItem
import com.js.nowakelock.ui.screens.dadetail.DASettingsState

/**
 * A card displaying settings for a device automation item.
 * Allows toggling blocking, setting conditions, and configuring time intervals.
 * 
 * @param settingsState The current settings state
 * @param onBlockingSettingChanged Callback for when the blocking setting changes
 * @param onConditionSettingsChanged Callback for when condition settings change
 * @param onTimeIntervalChanged Callback for when the time interval changes
 * @param modifier Optional modifier for the component
 */
@Composable
fun DASettingsCard(
    settingsState: DASettingsState,
    onBlockingSettingChanged: (Boolean) -> Unit,
    onConditionSettingsChanged: (Boolean, Boolean) -> Unit,
    onTimeIntervalChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    InfoCard(
        title = stringResource(R.string.settings_title),
        modifier = modifier
    ) {
        // Main blocking switch
        SwitchItem(
            title = stringResource(R.string.block_this_item),
            subtitle = stringResource(R.string.block_this_item_desc),
            checked = settingsState.isBlocked,
            onCheckedChange = onBlockingSettingChanged,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Conditional settings (only shown when blocking is enabled)
        if (settingsState.isBlocked) {
            // Sleep-only condition
            SwitchItem(
                title = stringResource(R.string.block_during_sleep),
                checked = settingsState.sleepOnly,
                onCheckedChange = { sleepOnly ->
                    onConditionSettingsChanged(sleepOnly, settingsState.screenOffOnly)
                },
                modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
            )
            
            // Screen-off-only condition
            SwitchItem(
                title = stringResource(R.string.block_during_screen_off),
                checked = settingsState.screenOffOnly,
                onCheckedChange = { screenOffOnly ->
                    onConditionSettingsChanged(settingsState.sleepOnly, screenOffOnly)
                },
                modifier = Modifier.padding(start = 16.dp, bottom = 16.dp)
            )
            
            // Time interval setting
            NumberInputItem(
                title = stringResource(R.string.allowed_time_interval),
                subtitle = stringResource(R.string.allowed_time_interval_desc),
                value = settingsState.timeInterval,
                suffix = stringResource(R.string.seconds),
                onValueChange = onTimeIntervalChanged
            )
        }
    }
} 