package com.js.nowakelock.ui.screens.alarms

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AlarmsUiState(
    val isLoading: Boolean = false,
    val message: String = ""
)

class AlarmsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AlarmsUiState())
    val uiState: StateFlow<AlarmsUiState> = _uiState.asStateFlow()
} 