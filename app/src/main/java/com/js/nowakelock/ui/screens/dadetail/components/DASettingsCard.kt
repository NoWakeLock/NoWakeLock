package com.js.nowakelock.ui.screens.dadetail.components

import android.annotation.SuppressLint
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.js.nowakelock.R
import com.js.nowakelock.data.db.Type
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
    onConditionSettingsChanged: (Boolean) -> Unit,
    onTimeIntervalChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    InfoCard(
        title = stringResource(R.string.settings_title), modifier = modifier
    ) {
        val title = when (settingsState.type) {
            Type.Wakelock -> stringResource(R.string.allow_wakelock)
            Type.Alarm -> stringResource(R.string.allow_alarm)
            Type.Service -> stringResource(R.string.allow_service)
            else -> stringResource(R.string.allow_wakelock)
        }
        val subtitle = when (settingsState.type) {
            Type.Wakelock -> stringResource(R.string.allow_wakelock_desc)
            Type.Alarm -> stringResource(R.string.allow_alarm_desc)
            Type.Service -> stringResource(R.string.allow_service_desc)
            else -> stringResource(R.string.allow_wakelock_desc)
        }
        // Main blocking switch
        SwitchItem(
            title = title,
            subtitle = subtitle,
            checked = !settingsState.fullBlocked,
            onCheckedChange = { checked ->
                onBlockingSettingChanged(!checked)
            },
            modifier = Modifier.padding(bottom = 4.dp)
        )

        val titleScreenOff = when (settingsState.type) {
            Type.Wakelock -> stringResource(R.string.allow_screenOff_wakelock)
            Type.Alarm -> stringResource(R.string.allow_screenOff_alarm)
            Type.Service -> stringResource(R.string.allow_screenOff_service)
            else -> stringResource(R.string.allow_screenOff_wakelock)
        }
        val subtitleScreenOff = when (settingsState.type) {
            Type.Wakelock -> stringResource(R.string.allow_screenOff_wakelock_desc)
            Type.Alarm -> stringResource(R.string.allow_screenOff_alarm_desc)
            Type.Service -> stringResource(R.string.allow_screenOff_service_desc)
            else -> stringResource(R.string.allow_screenOff_wakelock_desc)
        }
        // Screen-off
        SwitchItem(
            title = titleScreenOff,
            subtitle = subtitleScreenOff,
            checked = !settingsState.screenOffBlock,
            onCheckedChange = { checked ->
                onConditionSettingsChanged(!checked)
            },
            modifier = Modifier.padding(bottom = 4.dp),
            enabled = !settingsState.fullBlocked
        )

        // Time interval setting with BasicTextField
        TimeIntervalItem(
            title = stringResource(R.string.allowed_time_interval),
            subtitle = stringResource(R.string.allowed_time_interval_desc),
            value = settingsState.timeInterval,
            onValueChange = onTimeIntervalChanged,
            enabled = !settingsState.fullBlocked
        )
    }
}

@Composable
fun SwitchItem(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 0.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(
                    alpha = 0.6f
                )
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = 0.6f
                    )
                )
            }
        }
        Switch(
            checked = checked, onCheckedChange = onCheckedChange, enabled = enabled
        )
    }
}

/**
 * A simpler time interval input item with BasicTextField
 *
 * @param title The main text to display
 * @param subtitle Optional secondary text to display below the title
 * @param value The current numeric value
 * @param onValueChange Callback invoked when the value changes
 * @param modifier Optional modifier for the component
 * @param enabled Whether the input is enabled
 */
@Composable
fun TimeIntervalItem(
    title: String,
    subtitle: String? = null,
    value: Int,
    onValueChange: (Int) -> Unit,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var textValue by remember(value) { mutableStateOf(if (value == 0) "" else value.toString()) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(
                    alpha = 0.6f
                )
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = 0.6f
                    )
                )
            }
        }

        // BasicTextField implementation similar to DAListItem
        BasicTextField(
            value = textValue,
            onValueChange = { newText ->
                // Only allow numeric input
                if (newText.isEmpty() || newText.all { it.isDigit() }) {
                    textValue = newText
                    // Update the actual value if valid
                    onValueChange(newText.toIntOrNull() ?: 0)
                }
            },
            modifier = Modifier
                .width(65.dp)
                .height(36.dp)
                .border(
                    width = 1.dp,
                    color = if (enabled) MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 8.dp, vertical = 8.dp),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                textAlign = TextAlign.End,
                color = if (enabled) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            decorationBox = { innerTextField ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.weight(1f)) {
                        if (textValue.isEmpty()) {
                            Text(
                                text = "0",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            innerTextField()
                        }
                    }
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "s",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (enabled)
                            MaterialTheme.colorScheme.onSurfaceVariant
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                    )
                }
            }
        )
    }
}

/**
 * Preview for DASettingsCard with blocking enabled
 */
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun DASettingsCardBlockingEnabledPreview() {
    val settingsState = DASettingsState(
        fullBlocked = true, screenOffBlock = false, timeInterval = 30
    )

    androidx.compose.material3.Surface {
        DASettingsCard(
            settingsState = settingsState,
            onBlockingSettingChanged = {},
            onConditionSettingsChanged = { _ -> },
            onTimeIntervalChanged = {})
    }
}

/**
 * Preview for DASettingsCard with blocking disabled
 */
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun DASettingsCardBlockingDisabledPreview() {
    val settingsState = DASettingsState(
        fullBlocked = false, screenOffBlock = false, timeInterval = 0
    )

    androidx.compose.material3.Surface {
        DASettingsCard(
            settingsState = settingsState,
            onBlockingSettingChanged = {},
            onConditionSettingsChanged = { _ -> },
            onTimeIntervalChanged = {})
    }
}
