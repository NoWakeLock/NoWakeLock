package com.js.nowakelock.ui.screens.apps

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AppsUiState(
    val isLoading: Boolean = false,
    val message: String = ""
)

class AppsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AppsUiState())
    val uiState: StateFlow<AppsUiState> = _uiState.asStateFlow()
} 