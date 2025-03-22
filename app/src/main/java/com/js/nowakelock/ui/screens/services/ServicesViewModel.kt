package com.js.nowakelock.ui.screens.services

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ServicesUiState(
    val isLoading: Boolean = false,
    val message: String = ""
)

class ServicesViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ServicesUiState())
    val uiState: StateFlow<ServicesUiState> = _uiState.asStateFlow()
} 