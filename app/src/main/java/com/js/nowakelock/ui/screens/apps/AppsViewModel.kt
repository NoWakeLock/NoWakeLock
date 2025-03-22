package com.js.nowakelock.ui.screens.apps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.js.nowakelock.data.db.Type
import com.js.nowakelock.data.model.AppWithStats
import com.js.nowakelock.data.repository.appdas.AppDasRepo
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * UI State for Apps Screen
 */
data class AppsUiState(
    val isLoading: Boolean = false,
    val apps: List<AppWithStats> = emptyList(),
    val currentSortOption: SortOption = SortOption.NAME,
    val currentFilterOption: FilterOption = FilterOption.ALL,
    val message: String = ""
)

/**
 * Sort options for app listing
 */
enum class SortOption {
    NAME, COUNT, TIME
}

/**
 * Filter options for app listing
 */
enum class FilterOption {
    ALL, USER, SYSTEM, MODIFIED
}

/**
 * ViewModel for Apps Screen
 * Handles loading, sorting and filtering of application data
 */
class AppsViewModel(
    private val appDasRepo: AppDasRepo
) : ViewModel() {
    private val _uiState = MutableStateFlow(AppsUiState(isLoading = true))
    val uiState: StateFlow<AppsUiState> = _uiState.asStateFlow()

    init {
        // Initial data load
        loadApps()
    }

    /**
     * Loads apps based on current sort and filter options
     */
    private fun loadApps() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // Get the appropriate flow based on current sort option
                val appsFlow = when (_uiState.value.currentSortOption) {
                    SortOption.NAME -> appDasRepo.getAppsWithStatsSortedByName()
                    SortOption.COUNT -> appDasRepo.getAppsWithStatsSortedByCount()
                    SortOption.TIME -> appDasRepo.getAppsWithStatsSortedByTime()
                }
                
                // Update UI state with the apps flow
                appsFlow.collect { appsList ->
                    // Filter as needed
                    val filteredApps = when (_uiState.value.currentFilterOption) {
                        FilterOption.ALL -> appsList
                        FilterOption.USER -> appsList.filter { !it.appInfo.system }
                        FilterOption.SYSTEM -> appsList.filter { it.appInfo.system }
                        FilterOption.MODIFIED -> appsList.filter { it.wakelockCount > 0 }
                    }
                    
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            apps = filteredApps
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        message = "Error loading apps: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Changes the current sort option and reloads data
     */
    fun changeSortOption(option: SortOption) {
        if (_uiState.value.currentSortOption != option) {
            _uiState.update { it.copy(currentSortOption = option) }
            loadApps()
        }
    }

    /**
     * Changes the current filter option and reloads data
     */
    fun changeFilterOption(option: FilterOption) {
        if (_uiState.value.currentFilterOption != option) {
            _uiState.update { it.copy(currentFilterOption = option) }
            loadApps()
        }
    }

    /**
     * Refreshes app data
     */
    fun refreshData() {
        viewModelScope.launch {
            try {
                appDasRepo.syncAppInfos()
                appDasRepo.syncInfos()
                loadApps()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(message = "Error refreshing data: ${e.message}")
                }
            }
        }
    }
} 