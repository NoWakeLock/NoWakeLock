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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.js.nowakelock.R
import com.js.nowakelock.data.db.Type
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Main screen for device automation item details.
 * Now implemented as a pure content component without its own Scaffold.
 * Handles different states (loading, success, error) and provides proper UI for each.
 * 
 * @param daId The ID of the device automation item
 * @param type The type of the device automation item
 * @param userId The user ID
 * @param onNavigateBack Callback for navigating back
 * @param viewModel The view model for this screen
 */
@Composable
fun DADetailScreen(
    daId: String,
    type: Type,
    userId: Int,
    onNavigateBack: () -> Unit,
    viewModel: DADetailViewModel = koinViewModel { parametersOf(daId, type, userId) }
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