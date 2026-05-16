package com.js.nowakelock.ui.screens.apps

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.js.nowakelock.data.model.AppWithStats
import com.js.nowakelock.data.model.UserInfo
import com.js.nowakelock.data.repository.appdas.AppDasRepo
import com.js.nowakelock.ui.navigation.params.AppsScreenParams
import com.js.nowakelock.base.LogUtil
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

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
@Serializable
enum class SortOption {
    NAME, COUNT, TIME
}

/**
 * Filter options for app listing
 */
@Serializable
enum class FilterOption {
    ALL, USER, SYSTEM
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

    // 添加用于跟踪加载作业的属性
    private var loadDataJob: Job? = null

    init {
        // 使用 triggerDataLoad 进行初始数据加载
        // triggerDataLoad(LoadingSource.INITIAL, immediate = true)
        
        // 延迟同步系统数据
        viewModelScope.launch {
            delay(300) // 短暂延迟，让UI先渲染
            refreshData(LoadingSource.INITIAL) // 同步系统数据
        }
    }
    
    /**
     * 统一的数据加载方法，处理加载请求的协调和防抖
     * @param source 加载源
     * @param immediate 是否立即加载（不应用防抖）
     */
    private fun triggerDataLoad(source: LoadingSource, immediate: Boolean = false) {
        // 取消正在进行的加载作业
        loadDataJob?.cancel()
        
        // 启动新的加载作业
        loadDataJob = viewModelScope.launch {
            // 对非立即加载的操作应用防抖延迟
            // 初始加载和用户主动刷新不需要防抖
            if (!immediate && source != LoadingSource.INITIAL && source != LoadingSource.USER_PULL) {
                LogUtil.d("AppsViewModel", "Debouncing load for source: $source")
                delay(200) // 防抖延迟
            }
            loadApps(source)
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
            // 用户变更是关键参数，应立即加载
            triggerDataLoad(LoadingSource.USER_CHANGE, immediate = true)
        }
    }

    /**
     * Loads apps based on current sort, filter and search options
     * @param source the source of the load operation
     */
    private suspend fun loadApps(source: LoadingSource = LoadingSource.NONE) {
        _uiState.update { it.copy(isLoading = true, loadingSource = source) }

        try {
            val appsFlow = when (currentSortOption) {
                SortOption.NAME -> appDasRepo.getAppsWithStatsSortedByName()
                SortOption.COUNT -> appDasRepo.getAppsWithStatsSortedByCount()
                SortOption.TIME -> appDasRepo.getAppsWithStatsSortedByTime()
            }

            appsFlow
                .conflate()
                .collect { appsList ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loadingSource = LoadingSource.NONE,
                            apps = filterApps(appsList)
                        )
                    }
                }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            LogUtil.e("AppsViewModel", "Error loading apps: ${e.message}")
            _uiState.update {
                it.copy(
                    isLoading = false,
                    loadingSource = LoadingSource.NONE,
                    message = "Error loading apps: ${e.message}"
                )
            }
        }
    }

    private fun filterApps(appsList: List<AppWithStats>): List<AppWithStats> {
        val userFilteredApps = appsList.filter { it.appInfo.userId == currentUserId }

        val typeFilteredApps = when (currentFilterOption) {
            FilterOption.ALL -> userFilteredApps
            FilterOption.USER -> userFilteredApps.filter { !it.appInfo.system }
            FilterOption.SYSTEM -> userFilteredApps.filter { it.appInfo.system }
        }

        val query = searchQuery.trim().lowercase()
        return if (query.isNotEmpty()) {
            typeFilteredApps.filter { app ->
                app.appInfo.label.lowercase().contains(query) ||
                    app.appInfo.packageName.lowercase().contains(query)
            }
        } else {
            typeFilteredApps
        }
    }

    /**
     * Changes the current sort option and reloads data
     */
    fun changeSortOption(option: SortOption) {
        if (currentSortOption != option) {
            currentSortOption = option
            _uiState.update { it.copy(currentSortOption = option) }
            // 使用 triggerDataLoad 替代直接调用 loadApps
            triggerDataLoad(LoadingSource.SORT_CHANGE)
        }
    }

    /**
     * Changes the current filter option and reloads data
     */
    fun changeFilterOption(option: FilterOption) {
        if (currentFilterOption != option) {
            currentFilterOption = option
            _uiState.update { it.copy(currentFilterOption = option) }
            // 使用 triggerDataLoad 替代直接调用 loadApps
            triggerDataLoad(LoadingSource.FILTER_CHANGE)
        }
    }

    /**
     * Updates the search query and reloads data
     */
    fun updateSearchQuery(query: String) {
        if (searchQuery != query) {
            searchQuery = query
            _uiState.update { it.copy(searchQuery = query) }
            // 搜索查询应该使用防抖，不需要立即加载
            triggerDataLoad(LoadingSource.SEARCH_CHANGE)
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
                
                // 分阶段执行同步，减少全部失败的可能性
                try {
                    appDasRepo.syncAppInfos()
                } catch (e: Exception) {
                    LogUtil.e("AppsViewModel", "Error syncing app infos: ${e.message}")
                    // 继续执行，尝试同步其他数据
                }
                
                try {
                    appDasRepo.syncInfos()
                } catch (e: Exception) {
                    LogUtil.e("AppsViewModel", "Error syncing infos: ${e.message}")
                    // 继续执行，确保UI更新
                }
                
                // 使用 triggerDataLoad 替代直接调用 loadApps，立即加载
                triggerDataLoad(source, immediate = true)
            } catch (e: Exception) {
                LogUtil.e("AppsViewModel", "Error refreshing data: ${e.message}")
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
