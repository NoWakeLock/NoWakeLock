package com.js.nowakelock.ui.screens.wakelocks

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class WakelocksUiState(
    val isLoading: Boolean = false,
    val message: String = ""
)

class WakelocksViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(WakelocksUiState())
    val uiState: StateFlow<WakelocksUiState> = _uiState.asStateFlow()
} 