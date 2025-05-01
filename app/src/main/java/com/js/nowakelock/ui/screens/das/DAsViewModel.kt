package com.js.nowakelock.ui.screens.das


import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.js.nowakelock.data.model.DAItem
import com.js.nowakelock.data.repository.daitem.DARepository
import com.js.nowakelock.ui.navigation.params.DAsScreenParams
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
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
    val das: ImmutableList<DAItem> = kotlinx.collections.immutable.persistentListOf(),
    val currentSortOption: DASortOption = DASortOption.NAME,
    val currentFilterOption: DAFilterOption = DAFilterOption.ALL,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val message: String = "",
    // Summary statistics
    val totalDAs: Int = 0,
    val blockedCount: Int = 0,
    val allowedCount: Int = 0,
    // App filtering
    val packageName: String? = null,
    val userId: Int? = null
)

/**
 * ViewModel for DAs Screen
 * Handles loading, sorting, filtering, and searching of DA data
 * Also manages DA settings updates
 */
open class DAsViewModel(
    private val daRepository: DARepository, private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    // read arguments from savedStateHandle
    var packageName: String?
        get() = savedStateHandle.get<String>(DAsScreenParams.PACKAGE_NAME)
        set(value) {
            savedStateHandle[DAsScreenParams.PACKAGE_NAME] = value
        }

    var userId: Int?
        get() = savedStateHandle.get<Int>(DAsScreenParams.USER_ID)
        set(value) {
            savedStateHandle[DAsScreenParams.USER_ID] = value
        }

    var searchQuery: String
        get() = savedStateHandle.get<String>(DAsScreenParams.SEARCH_QUERY) ?: ""
        set(value) {
            savedStateHandle[DAsScreenParams.SEARCH_QUERY] = value
        }

    var isSearchActive: Boolean
        get() = savedStateHandle.get<Boolean>(DAsScreenParams.IS_SEARCH_ACTIVE) ?: false
        set(value) {
            savedStateHandle[DAsScreenParams.IS_SEARCH_ACTIVE] = value
        }

    private var currentFilterOption: DAFilterOption
        get() = savedStateHandle.get<DAFilterOption>(DAsScreenParams.FILTER_OPTION)
            ?: DAFilterOption.ALL
        set(value) {
            savedStateHandle[DAsScreenParams.FILTER_OPTION] = value
        }

    private var currentSortOption: DASortOption
        get() = savedStateHandle.get<DASortOption>(DAsScreenParams.SORT_OPTION) ?: DASortOption.NAME
        set(value) {
            savedStateHandle[DAsScreenParams.SORT_OPTION] = value
        }

    private val _uiState = MutableStateFlow(
        DAsUiState(
            isLoading = true,
            loadingSource = LoadingSource.INITIAL,
            searchQuery = searchQuery,
            isSearchActive = isSearchActive,
            currentFilterOption = currentFilterOption,
            currentSortOption = currentSortOption,
            packageName = packageName,
            userId = userId
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
                val daFlow = when (currentSortOption) {
                    DASortOption.NAME -> daRepository.getDAItemsSortedByName(
                        packageName = packageName ?: "", userId = userId ?: -1
                    )

                    DASortOption.COUNT -> daRepository.getDAItemsSortedByCount(
                        packageName = packageName ?: "", userId = userId ?: -1
                    )

                    DASortOption.TIME -> daRepository.getDAItemsSortedByTime(
                        packageName = packageName ?: "", userId = userId ?: -1
                    )
                }

                // Collect and update UI state
                daFlow
                    // Apply custom distinctUntilChanged to filter out equivalent lists
                    .distinctUntilChanged { old, new ->
                        if (old.size != new.size) return@distinctUntilChanged false

                        // Check if lists contain the same items with the same state
                        val oldMap = old.associateBy { "${it.name}_${it.packageName}_${it.userId}" }
                        val newMap = new.associateBy { "${it.name}_${it.packageName}_${it.userId}" }

                        if (oldMap.keys != newMap.keys) return@distinctUntilChanged false

                        // Deep comparison of relevant state properties
                        oldMap.keys.all { key ->
                            val oldItem = oldMap[key]!!
                            val newItem = newMap[key]!!

                            oldItem.fullBlocked == newItem.fullBlocked && oldItem.screenOffBlock == newItem.screenOffBlock && oldItem.timeWindowSec == newItem.timeWindowSec
                        }
                    }.collect { daList ->
                        // packageName / userId
                        val pkgFilteredList = daList.filter { da ->
                            (packageName == null || da.packageName == packageName) && (userId == null || da.userId == userId)
                        }

                        // app filter
                        val filteredList = when (currentFilterOption) {
                            DAFilterOption.ALL -> pkgFilteredList
                            DAFilterOption.BLOCKED -> pkgFilteredList.filter { it.fullBlocked }
                            DAFilterOption.ALLOWED -> pkgFilteredList.filter { !it.fullBlocked }
                        }

                        // search filter
                        val query = searchQuery.trim().lowercase()
                        val searchFilteredList = if (query.isNotEmpty()) {
                            filteredList.filter { da ->
                                da.name.lowercase().contains(query) || da.packageName.lowercase()
                                    .contains(query)
                            }
                        } else {
                            filteredList
                        }

                        // count
                        val totalCount = pkgFilteredList.size
                        val blockedCount = pkgFilteredList.count { it.fullBlocked }

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                loadingSource = LoadingSource.NONE,
                                das = searchFilteredList.toImmutableList(),
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
        if (currentSortOption != option) {
            currentSortOption = option
            _uiState.update { it.copy(currentSortOption = option) }
            loadDAs(LoadingSource.SORT_CHANGE)
        }
    }

    /**
     * Changes the current filter option and reloads data
     */
    fun changeFilterOption(option: DAFilterOption) {
        if (currentFilterOption != option) {
            currentFilterOption = option
            _uiState.update { it.copy(currentFilterOption = option) }
            loadDAs(LoadingSource.FILTER_CHANGE)
        }
    }

    /**
     * Updates the search query and reloads data
     */
    fun updateSearchQuery(query: String) {
        if (searchQuery != query) {
            searchQuery = query
            _uiState.update { it.copy(searchQuery = query) }
            loadDAs(LoadingSource.SEARCH_CHANGE)
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
     * 设置包名和用户ID过滤器
     */
    fun setAppFilter(packageName: String?, userId: Int?) {
        if (this.packageName != packageName || this.userId != userId) {
            this.packageName = packageName
            this.userId = userId
            _uiState.update {
                it.copy(
                    packageName = packageName, userId = userId
                )
            }
            loadDAs(LoadingSource.FILTER_CHANGE)
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
                daRepository.syncEvents()
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