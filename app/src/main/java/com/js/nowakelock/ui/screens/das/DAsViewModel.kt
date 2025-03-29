package com.js.nowakelock.ui.screens.das


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.js.nowakelock.data.model.DAItem
import com.js.nowakelock.data.repository.daitem.DARepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

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
 * Sort options for DA listing
 */
enum class DASortOption {
    NAME, COUNT, TIME
}

/**
 * Filter options for DA listing
 */
enum class DAFilterOption {
    ALL, BLOCKED, ALLOWED
}

/**
 * UI state for DAs Screen
 */
data class DAsUiState(
    val isLoading: Boolean = false,
    val loadingSource: LoadingSource = LoadingSource.NONE,
    val das: List<DAItem> = emptyList(),
    val currentSortOption: DASortOption = DASortOption.NAME,
    val currentFilterOption: DAFilterOption = DAFilterOption.ALL,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val message: String = "",
    // Summary statistics
    val totalDAs: Int = 0,
    val blockedCount: Int = 0,
    val allowedCount: Int = 0
)

/**
 * ViewModel for DAs Screen
 * Handles loading, sorting, filtering, and searching of DA data
 * Also manages DA settings updates
 */
open class DAsViewModel(
    private val daRepository: DARepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        DAsUiState(
            isLoading = true, loadingSource = LoadingSource.INITIAL
        )
    )
    val uiState: StateFlow<DAsUiState> = _uiState.asStateFlow()

    // For debouncing time window settings updates
    private var timeWindowUpdateJob: Job? = null

    init {
        // Initial data load
        loadDAs(LoadingSource.INITIAL)

        // Sync data with content provider after short delay
        viewModelScope.launch {
            delay(300) // Short delay to let UI render first
            refreshData(LoadingSource.INITIAL)
        }
    }

    /**
     * Loads DAs based on current sort, filter and search options
     * @param source The source of the loading operation
     */
    private fun loadDAs(source: LoadingSource = LoadingSource.NONE) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadingSource = source) }

            try {
                // Select data flow based on sort option
                val daFlow = when (_uiState.value.currentSortOption) {
                    DASortOption.NAME -> daRepository.getDAItemsSortedByName()
                    DASortOption.COUNT -> daRepository.getDAItemsSortedByCount()
                    DASortOption.TIME -> daRepository.getDAItemsSortedByTime()
                }

                // Collect and update UI state
                daFlow.collect { daList ->
                    // Apply filtering
                    val filteredList = when (_uiState.value.currentFilterOption) {
                        DAFilterOption.ALL -> daList
                        DAFilterOption.BLOCKED -> daList.filter { it.fullBlocked }
                        DAFilterOption.ALLOWED -> daList.filter { !it.fullBlocked }
                    }

                    // Apply search filter if needed
                    val searchQuery = _uiState.value.searchQuery.trim().lowercase()
                    val searchFilteredList = if (searchQuery.isNotEmpty()) {
                        filteredList.filter { da ->
                            da.name.lowercase().contains(searchQuery) || da.packageName.lowercase()
                                .contains(searchQuery)
                        }
                    } else {
                        filteredList
                    }

                    // Calculate summary statistics
                    val blockedCount = daList.count { it.fullBlocked }
                    val totalCount = daList.size

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loadingSource = LoadingSource.NONE,
                            das = searchFilteredList,
                            totalDAs = totalCount,
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
                        message = "Error loading das: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Changes the current sort option and reloads data
     */
    fun changeSortOption(option: DASortOption) {
        if (_uiState.value.currentSortOption != option) {
            _uiState.update { it.copy(currentSortOption = option) }
            loadDAs(LoadingSource.SORT_CHANGE)
        }
    }

    /**
     * Changes the current filter option and reloads data
     */
    fun changeFilterOption(option: DAFilterOption) {
        if (_uiState.value.currentFilterOption != option) {
            _uiState.update { it.copy(currentFilterOption = option) }
            loadDAs(LoadingSource.FILTER_CHANGE)
        }
    }

    /**
     * Updates the search query and reloads data
     */
    fun updateSearchQuery(query: String) {
        if (_uiState.value.searchQuery != query) {
            _uiState.update { it.copy(searchQuery = query) }
            loadDAs(LoadingSource.SEARCH_CHANGE)
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
     * Refreshes DAs data by syncing from system
     * @param source The source of the refresh operation
     */
    fun refreshData(source: LoadingSource = LoadingSource.USER_PULL) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, loadingSource = source, message = "") }
                daRepository.syncDB()
                // Keep loading source consistent
                loadDAs(source)
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
     * Updates DA block state
     * Triggered by toggle switch in UI
     */
    fun updateDAFullBlockState(
        daItem: DAItem, isBlocked: Boolean
    ) {
        viewModelScope.launch {
            val st = DAItem.toSt(daItem)
            st.fullBlock = isBlocked

            try {
                daRepository.updateDAItemSettings(st)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        message = "Error updating da: ${daItem.type.value} ${daItem.name} ${daItem.packageName}" + " ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Updates DA screen off block state
     * Triggered by toggle switch in UI
     */
    fun updateDAScreenOffBlockState(
        daItem: DAItem, isBlocked: Boolean
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val st = DAItem.toSt(daItem)
            st.screenOffBlock = isBlocked

            try {
                daRepository.updateDAItemSettings(st)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        message = "Error updating da: ${daItem.type.value} ${daItem.name} ${daItem.packageName}" + " ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Updates DA time window with debouncing
     * Triggered by time window input in UI
     */
    fun updateDATimeWindow(
        daItem: DAItem, timeWindow: Int
    ) {
        // Cancel any pending update job
        timeWindowUpdateJob?.cancel()

        // Start a new update job with debounce delay
        timeWindowUpdateJob = viewModelScope.launch {
            delay(300) // Debounce delay

            val st = DAItem.toSt(daItem)
            st.timeWindowMs = TimeUnit.SECONDS.toMillis(timeWindow.toLong())
            try {
                daRepository.updateDAItemSettings(st)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        message = "Error updating da: ${daItem.type.value} ${daItem.name} ${daItem.packageName}" + " ${e.message}"
                    )
                }
            }
        }
    }
} 