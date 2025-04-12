package com.js.nowakelock.ui.screens.dadetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.js.nowakelock.R
import com.js.nowakelock.data.db.Type
import com.js.nowakelock.data.model.DAInfoEntry
import com.js.nowakelock.data.model.DAItem
import com.js.nowakelock.data.model.DAStatistics
import com.js.nowakelock.data.model.EventItem
import com.js.nowakelock.data.model.HourData
import org.koin.androidx.compose.koinViewModel

/**
 * Main screen for device automation item details.
 * Now implemented as a pure content component without its own Scaffold.
 * Handles different states (loading, success, error) and provides proper UI for each.
 * 
 * @param daId The ID of the device automation item (required for backward compatibility)
 * @param type The type of the device automation item (required for backward compatibility)
 * @param userId The user ID (required for backward compatibility)
 * @param onNavigateBack Callback for navigating back
 * @param viewModel The view model for this screen
 */
@Composable
fun DADetailScreen(
    daId: String,
    type: Type,
    userId: Int,
    onNavigateBack: () -> Unit,
    viewModel: DADetailViewModel = koinViewModel()
) {
    // Observe states
    val uiState by viewModel.uiState.collectAsState()
    val settingsState by viewModel.settingsState.collectAsState()
    
    // Handle error states with LaunchedEffect to show in parent's snackbar
    if (uiState is DADetailState.Error) {
        val errorMessage = (uiState as DADetailState.Error).message
        LaunchedEffect(errorMessage) {
            // Note: In a production app, we would use a callback to display this in the parent's snackbar
            // For now, we handle error display in the component itself
        }
    }
    
    // Main content
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (val state = uiState) {
            is DADetailState.Loading -> {
                // Loading state
                CircularProgressIndicator()
            }
            is DADetailState.Success -> {
                // Success state - show content
                DADetailContent(
                    state = state,
                    settingsState = settingsState,
                    onBlockingSettingChanged = viewModel::updateBlockingSetting,
                    onConditionSettingsChanged = viewModel::updateConditionSettings,
                    onTimeIntervalChanged = viewModel::updateTimeInterval,
                    modifier = Modifier.fillMaxSize()
                )
            }
            is DADetailState.Error -> {
                // Error state
                Text(
                    text = stringResource(R.string.error_loading_data),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

///**
// * Preview for DADetailScreen in loading state
// */
//@Composable
//@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
//fun DADetailScreenLoadingPreview() {
//    val mockViewModel = object : DADetailViewModel() {
//        override val uiState = kotlinx.coroutines.flow.MutableStateFlow<DADetailState>(DADetailState.Loading)
//        override val settingsState = kotlinx.coroutines.flow.MutableStateFlow(DASettingsState())
//    }
//
//    DADetailScreen(
//        daId = "sample_wakelock",
//        type = Type.Wakelock,
//        userId = 0,
//        onNavigateBack = {},
//        viewModel = mockViewModel
//    )
//}
//
///**
// * Preview for DADetailScreen in success state
// */
//@Composable
//@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
//fun DADetailScreenSuccessPreview() {
//    val mockDAItem = DAItem(
//        name = "SampleWakelock",
//        packageName = "com.example.app",
//        type = Type.Wakelock,
//        count = 100,
//        blockCount = 30,
//        countTime = 3600000 // 1 hour
//    )
//
//    val mockStatistics = DAStatistics(
//        totalCount = 100,
//        blockedCount = 30,
//        totalTime = 3600000,
//        savedTime = 1080000,
//        formattedTotalTime = "1h 0m",
//        formattedSavedTime = "18m saved"
//    )
//
//    val mockTimelineData = List(24) { hour ->
//        HourData(
//            hour = hour,
//            label = if (hour == 0) "12AM" else if (hour < 12) "${hour}AM" else if (hour == 12) "12PM" else "${hour-12}PM",
//            total = (5..20).random(),
//            blocked = (0..5).random()
//        )
//    }
//
//    val mockEvents = List(5) { index ->
//        EventItem(
//            time = System.currentTimeMillis() - (index * 3600000),
//            duration = (10000..300000).random().toLong(),
//            isBlocked = index % 2 == 0,
//            formattedTime = "1:30 PM",
//            formattedDuration = "${(1..5).random()}m ${(1..59).random()}s"
//        )
//    }
//
//    val mockViewModel = object : DADetailViewModel() {
//        override val uiState = kotlinx.coroutines.flow.MutableStateFlow<DADetailState>(
//            DADetailState.Success(
//                daItem = mockDAItem,
//                info = DAInfoEntry(
//                    id = "sample_wakelock",
//                    name = "SampleWakelock",
//                    type = Type.Wakelock,
//                    packageName = "com.example.app",
//                    safeToBlock = "safe",
//                    description = "This is a sample wakelock for preview purposes.",
//                    recommendation = "Can be safely blocked when screen is off.",
//                    warning = null,
//                    tags = listOf("battery", "background")
//                ),
//                statistics = mockStatistics,
//                timelineData = mockTimelineData,
//                recentEvents = mockEvents
//            )
//        )
//
//        override val settingsState = kotlinx.coroutines.flow.MutableStateFlow(
//            DASettingsState(
//                isBlocked = true,
//                sleepOnly = true,
//                screenOffOnly = false,
//                timeInterval = 30
//            )
//        )
//    }
//
//    DADetailScreen(
//        daId = "sample_wakelock",
//        type = Type.Wakelock,
//        userId = 0,
//        onNavigateBack = {},
//        viewModel = mockViewModel
//    )
//}
//
///**
// * Preview for DADetailScreen in error state
// */
//@Composable
//@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
//fun DADetailScreenErrorPreview() {
//    val mockViewModel = object : DADetailViewModel() {
//        override val uiState = kotlinx.coroutines.flow.MutableStateFlow<DADetailState>(
//            DADetailState.Error("Failed to load data. Please try again.")
//        )
//        override val settingsState = kotlinx.coroutines.flow.MutableStateFlow(DASettingsState())
//    }
//
//    DADetailScreen(
//        daId = "sample_wakelock",
//        type = Type.Wakelock,
//        userId = 0,
//        onNavigateBack = {},
//        viewModel = mockViewModel
//    )
//}