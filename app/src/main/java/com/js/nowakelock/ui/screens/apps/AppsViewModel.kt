package com.js.nowakelock.ui.screens.apps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.js.nowakelock.data.db.Type
import com.js.nowakelock.data.model.AppWithStats
import com.js.nowakelock.data.repository.appdas.AppDasRepo
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * 定义加载来源，用于区分不同类型的加载操作
 */
enum class LoadingSource {
    NONE,           // 无加载
    INITIAL,        // 初始加载
    USER_PULL,      // 用户下拉刷新
    FILTER_CHANGE,  // 过滤器更改
    SORT_CHANGE     // 排序更改
}

/**
 * UI State for Apps Screen
 */
data class AppsUiState(
    val isLoading: Boolean = false,
    val loadingSource: LoadingSource = LoadingSource.NONE,
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
    private val _uiState = MutableStateFlow(AppsUiState(
        isLoading = true, 
        loadingSource = LoadingSource.INITIAL
    ))
    val uiState: StateFlow<AppsUiState> = _uiState.asStateFlow()

    init {
        // Initial data load from database
        loadApps(LoadingSource.INITIAL)
        
        // Sync data after a short delay to let UI render first
        viewModelScope.launch {
            delay(300) // Short delay to show initial UI
            refreshData(LoadingSource.INITIAL) // Sync with system
        }
    }

    /**
     * Loads apps based on current sort and filter options
     * @param source 加载操作的来源
     */
    private fun loadApps(source: LoadingSource = LoadingSource.NONE) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadingSource = source) }
            
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
                            loadingSource = LoadingSource.NONE,
                            apps = filteredApps
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        loadingSource = LoadingSource.NONE,
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
            loadApps(LoadingSource.SORT_CHANGE)
        }
    }

    /**
     * Changes the current filter option and reloads data
     */
    fun changeFilterOption(option: FilterOption) {
        if (_uiState.value.currentFilterOption != option) {
            _uiState.update { it.copy(currentFilterOption = option) }
            loadApps(LoadingSource.FILTER_CHANGE)
        }
    }

    /**
     * Refreshes app data by syncing from system
     * @param source 刷新操作的来源，默认为用户下拉
     */
    fun refreshData(source: LoadingSource = LoadingSource.USER_PULL) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, loadingSource = source, message = "") }
                appDasRepo.syncAppInfos()
                appDasRepo.syncInfos()
                // 保持加载来源一致
                loadApps(source)
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        loadingSource = LoadingSource.NONE,
                        message = "Error refreshing data: ${e.message}"
                    )
                }
            }
        }
    }
} 