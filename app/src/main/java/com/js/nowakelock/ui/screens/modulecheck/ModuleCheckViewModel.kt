package com.js.nowakelock.ui.screens.modulecheck

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.js.nowakelock.data.model.CheckStatus
import com.js.nowakelock.data.model.ModuleCheckResult
import com.js.nowakelock.data.repository.modulecheck.ModuleCheckRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI state for the module check screen
 */
data class ModuleCheckUiState(
    val isLoading: Boolean = false,
    val result: ModuleCheckResult? = null,
    val error: String? = null
)

/**
 * ViewModel for module check functionality
 */
class ModuleCheckViewModel(
    private val moduleCheckRepository: ModuleCheckRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ModuleCheckUiState(isLoading = false))
    val uiState: StateFlow<ModuleCheckUiState> = _uiState.asStateFlow()
    
    init {
        checkModuleStatus()
    }
    
    /**
     * Perform module check and update UI state
     */
    fun checkModuleStatus() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                
                moduleCheckRepository.checkModuleStatus().collect { result ->
                    // First emission is empty result (loading state)
                    // Second emission is actual result
                    if (result.moduleActive || result.hookStatus.values.any { it } || result.configPathValid) {
                        _uiState.update { 
                            it.copy(isLoading = false, result = result) 
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = e.message ?: "Error checking module status"
                    )
                }
            }
        }
    }
    
    /**
     * Get text description for a check status
     */
    fun getStatusDescription(status: CheckStatus): String {
        return when (status) {
            CheckStatus.NORMAL -> "All module components are working properly"
            CheckStatus.WARNING -> "Module is active but some hooks are not working"
            CheckStatus.ERROR -> "Critical module components are not working"
        }
    }
} 