package com.js.nowakelock.ui.screens.wakelocks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.js.nowakelock.data.model.WakelockItem
import com.js.nowakelock.data.repository.wakelock.WakelockRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Defines the loading source to track the origin of data loading operations
 */
enum class LoadingSource {
    NONE,           // No loading
    INITIAL,        // Initial load
    USER_PULL,      // User pull-to-refresh
    FILTER_CHANGE,  // Filter changed
    SORT_CHANGE,    // Sort order changed
    SEARCH_CHANGE   // Search query changed
}

/**
 * Sort options for wakelock listing
 */
enum class WakelockSortOption {
    NAME, COUNT, TIME
}

/**
 * Filter options for wakelock listing
 */
enum class WakelockFilterOption {
    ALL, BLOCKED, ALLOWED
}

/**
 * UI state for Wakelocks Screen
 */
data class WakelocksUiState(
    val isLoading: Boolean = false,
    val loadingSource: LoadingSource = LoadingSource.NONE,
    val wakelocks: List<WakelockItem> = emptyList(),
    val currentSortOption: WakelockSortOption = WakelockSortOption.NAME,
    val currentFilterOption: WakelockFilterOption = WakelockFilterOption.ALL,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val message: String = "",
    // Summary statistics
    val totalWakelocks: Int = 0,
    val blockedCount: Int = 0,
    val allowedCount: Int = 0
)

/**
 * ViewModel for Wakelocks Screen
 * Handles loading, sorting, filtering, and searching of wakelock data
 * Also manages wakelock settings updates
 */
class WakelocksViewModel(
    private val wakelockRepository: WakelockRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(WakelocksUiState(
        isLoading = true,
        loadingSource = LoadingSource.INITIAL
    ))
    val uiState: StateFlow<WakelocksUiState> = _uiState.asStateFlow()
    
    // For debouncing time window settings updates
    private var timeWindowUpdateJob: Job? = null
    
    init {
        // Initial data load
        loadWakelocks(LoadingSource.INITIAL)
        
        // Sync data with content provider after short delay
        viewModelScope.launch {
            delay(300) // Short delay to let UI render first
            refreshData(LoadingSource.INITIAL)
        }
    }
    
    /**
     * Loads wakelocks based on current sort, filter and search options
     * @param source The source of the loading operation
     */
    private fun loadWakelocks(source: LoadingSource = LoadingSource.NONE) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadingSource = source) }
            
            try {
                // Select data flow based on sort option
                val wakelockFlow = when (_uiState.value.currentSortOption) {
                    WakelockSortOption.NAME -> wakelockRepository.getWakelocksSortedByName()
                    WakelockSortOption.COUNT -> wakelockRepository.getWakelocksSortedByCount()
                    WakelockSortOption.TIME -> wakelockRepository.getWakelocksSortedByTime()
                }
                
                // Collect and update UI state
                wakelockFlow.collect { wakelockList ->
                    // Apply filtering
                    val filteredList = when (_uiState.value.currentFilterOption) {
                        WakelockFilterOption.ALL -> wakelockList
                        WakelockFilterOption.BLOCKED -> wakelockList.filter { it.isBlocked }
                        WakelockFilterOption.ALLOWED -> wakelockList.filter { !it.isBlocked }
                    }
                    
                    // Apply search filter if needed
                    val searchQuery = _uiState.value.searchQuery.trim().lowercase()
                    val searchFilteredList = if (searchQuery.isNotEmpty()) {
                        filteredList.filter { wakelock ->
                            wakelock.name.lowercase().contains(searchQuery) ||
                                    wakelock.packageName.lowercase().contains(searchQuery)
                        }
                    } else {
                        filteredList
                    }
                    
                    // Calculate summary statistics
                    val blockedCount = wakelockList.count { it.isBlocked }
                    val totalCount = wakelockList.size
                    
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            loadingSource = LoadingSource.NONE,
                            wakelocks = searchFilteredList,
                            totalWakelocks = totalCount,
                            blockedCount = blockedCount,
                            allowedCount = totalCount - blockedCount
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        loadingSource = LoadingSource.NONE,
                        message = "Error loading wakelocks: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Changes the current sort option and reloads data
     */
    fun changeSortOption(option: WakelockSortOption) {
        if (_uiState.value.currentSortOption != option) {
            _uiState.update { it.copy(currentSortOption = option) }
            loadWakelocks(LoadingSource.SORT_CHANGE)
        }
    }
    
    /**
     * Changes the current filter option and reloads data
     */
    fun changeFilterOption(option: WakelockFilterOption) {
        if (_uiState.value.currentFilterOption != option) {
            _uiState.update { it.copy(currentFilterOption = option) }
            loadWakelocks(LoadingSource.FILTER_CHANGE)
        }
    }
    
    /**
     * Updates the search query and reloads data
     */
    fun updateSearchQuery(query: String) {
        if (_uiState.value.searchQuery != query) {
            _uiState.update { it.copy(searchQuery = query) }
            loadWakelocks(LoadingSource.SEARCH_CHANGE)
        }
    }
    
    /**
     * Updates the search active state
     */
    fun setSearchActive(active: Boolean) {
        _uiState.update { it.copy(isSearchActive = active) }
        // If search is deactivated and there was a search query, clear it
        if (!active && _uiState.value.searchQuery.isNotEmpty()) {
            updateSearchQuery("")
        }
    }
    
    /**
     * Refreshes wakelock data by syncing from system
     * @param source The source of the refresh operation
     */
    fun refreshData(source: LoadingSource = LoadingSource.USER_PULL) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, loadingSource = source, message = "") }
                wakelockRepository.syncWakelocks()
                // Keep loading source consistent
                loadWakelocks(source)
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
    
    /**
     * Updates wakelock block state
     * Triggered by toggle switch in UI
     */
    fun updateWakelockBlockState(
        name: String,
        packageName: String,
        userId: Int,
        isBlocked: Boolean
    ) {
        viewModelScope.launch {
            try {
                // When blocking, clear time window
                // When unblocking, use default time window of 60 seconds
                val timeWindow = if (isBlocked) null else 60
                
                wakelockRepository.updateWakelockSettings(
                    name = name,
                    packageName = packageName,
                    userId = userId,
                    isBlocked = isBlocked,
                    timeWindow = timeWindow
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(message = "Error updating wakelock: ${e.message}")
                }
            }
        }
    }
    
    /**
     * Updates wakelock time window with debouncing
     * Triggered by time window input in UI
     */
    fun updateWakelockTimeWindow(
        name: String,
        packageName: String,
        userId: Int,
        timeWindow: Int
    ) {
        // Cancel any pending update job
        timeWindowUpdateJob?.cancel()
        
        // Start a new update job with debounce delay
        timeWindowUpdateJob = viewModelScope.launch {
            delay(300) // Debounce delay
            try {
                wakelockRepository.updateWakelockSettings(
                    name = name,
                    packageName = packageName,
                    userId = userId,
                    isBlocked = false, // Setting time window implies not fully blocking
                    timeWindow = timeWindow
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(message = "Error updating time window: ${e.message}")
                }
            }
        }
    }
} 