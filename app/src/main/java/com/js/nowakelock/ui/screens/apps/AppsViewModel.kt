package com.js.nowakelock.ui.screens.apps

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.js.nowakelock.data.db.Type
import com.js.nowakelock.data.model.AppWithStats
import com.js.nowakelock.data.model.UserInfo
import com.js.nowakelock.data.repository.appdas.AppDasRepo
import com.js.nowakelock.ui.navigation.params.AppsScreenParams
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Loading source for apps screen
 */
enum class LoadingSource {
    NONE,           // no loading
    INITIAL,        // initial load
    USER_PULL,      // user pull-to-refresh
    FILTER_CHANGE,  // filter changed
    SORT_CHANGE,    // sort changed
    SEARCH_CHANGE,  // search changed
    USER_CHANGE     // user changed
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
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val currentUserId: Int = 0,
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
 * Handles loading, sorting, filtering and searching of application data
 */
class AppsViewModel(
    private val appDasRepo: AppDasRepo,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    // 从SavedStateHandle读取参数
    private var currentUserId: Int
        get() = savedStateHandle.get<Int>(AppsScreenParams.CURRENT_USER_ID) ?: 0
        set(value) { savedStateHandle[AppsScreenParams.CURRENT_USER_ID] = value }
        
    private var searchQuery: String
        get() = savedStateHandle.get<String>(AppsScreenParams.SEARCH_QUERY) ?: ""
        set(value) { savedStateHandle[AppsScreenParams.SEARCH_QUERY] = value }
        
    private var isSearchActive: Boolean
        get() = savedStateHandle.get<Boolean>(AppsScreenParams.IS_SEARCH_ACTIVE) ?: false
        set(value) { savedStateHandle[AppsScreenParams.IS_SEARCH_ACTIVE] = value }
        
    private var currentFilterOption: FilterOption
        get() = savedStateHandle.get<FilterOption>(AppsScreenParams.FILTER_OPTION) ?: FilterOption.ALL
        set(value) { savedStateHandle[AppsScreenParams.FILTER_OPTION] = value }
        
    private var currentSortOption: SortOption
        get() = savedStateHandle.get<SortOption>(AppsScreenParams.SORT_OPTION) ?: SortOption.NAME
        set(value) { savedStateHandle[AppsScreenParams.SORT_OPTION] = value }

    private val _uiState = MutableStateFlow(AppsUiState(
        isLoading = true,
        loadingSource = LoadingSource.INITIAL,
        currentUserId = currentUserId,
        searchQuery = searchQuery,
        isSearchActive = isSearchActive,
        currentFilterOption = currentFilterOption,
        currentSortOption = currentSortOption
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
     * change current user
     * @param userId the user id to switch to
     */
    fun changeUser(userId: Int) {
        if (currentUserId != userId) {
            currentUserId = userId
            _uiState.update { it.copy(currentUserId = userId) }
            loadApps(LoadingSource.USER_CHANGE)
        }
    }

    /**
     * Loads apps based on current sort, filter and search options
     * @param source the source of the load operation
     */
    private fun loadApps(source: LoadingSource = LoadingSource.NONE) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadingSource = source) }
            
            try {
                // Get the appropriate flow based on current sort option
                val appsFlow = when (currentSortOption) {
                    SortOption.NAME -> appDasRepo.getAppsWithStatsSortedByName()
                    SortOption.COUNT -> appDasRepo.getAppsWithStatsSortedByCount()
                    SortOption.TIME -> appDasRepo.getAppsWithStatsSortedByTime()
                }
                
                // only filter apps for the current selected user
                
                // Update UI state with the apps flow
                appsFlow.collect { appsList ->
                    // filter apps by userId
                    val userFilteredApps = appsList.filter { it.appInfo.userId == currentUserId }
                    
                    // filter by app type
                    val typeFilteredApps = when (currentFilterOption) {
                        FilterOption.ALL -> userFilteredApps
                        FilterOption.USER -> userFilteredApps.filter { !it.appInfo.system }
                        FilterOption.SYSTEM -> userFilteredApps.filter { it.appInfo.system }
                        FilterOption.MODIFIED -> userFilteredApps.filter { it.wakelockCount > 0 }
                    }
                    
                    // Then apply search filter if needed
                    val query = searchQuery.trim().lowercase()
                    val searchFilteredApps = if (query.isNotEmpty()) {
                        typeFilteredApps.filter { app ->
                            app.appInfo.label.lowercase().contains(query) ||
                                    app.appInfo.packageName.lowercase().contains(query)
                        }
                    } else {
                        typeFilteredApps
                    }
                    
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            loadingSource = LoadingSource.NONE,
                            apps = searchFilteredApps
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
        if (currentSortOption != option) {
            currentSortOption = option
            _uiState.update { it.copy(currentSortOption = option) }
            loadApps(LoadingSource.SORT_CHANGE)
        }
    }

    /**
     * Changes the current filter option and reloads data
     */
    fun changeFilterOption(option: FilterOption) {
        if (currentFilterOption != option) {
            currentFilterOption = option
            _uiState.update { it.copy(currentFilterOption = option) }
            loadApps(LoadingSource.FILTER_CHANGE)
        }
    }

    /**
     * Updates the search query and reloads data
     */
    fun updateSearchQuery(query: String) {
        if (searchQuery != query) {
            searchQuery = query
            _uiState.update { it.copy(searchQuery = query) }
            loadApps(LoadingSource.SEARCH_CHANGE)
        }
    }

    /**
     * Updates the search active state
     */
    @JvmName("updateSearchActiveState")
    fun setSearchActive(active: Boolean) {
        isSearchActive = active
        _uiState.update { it.copy(isSearchActive = active) }
        // If search is deactivated and there was a search query, clear it
        if (!active && searchQuery.isNotEmpty()) {
            updateSearchQuery("")
        }
    }

    /**
     * Refreshes app data by syncing from system
     * @param source the source of the refresh operation, default is user pull
     */
    fun refreshData(source: LoadingSource = LoadingSource.USER_PULL) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, loadingSource = source, message = "") }
                appDasRepo.syncAppInfos()
                appDasRepo.syncInfos()
                
                // keep the loading source consistent
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