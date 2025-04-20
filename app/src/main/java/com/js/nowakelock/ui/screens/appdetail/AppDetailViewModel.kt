package com.js.nowakelock.ui.screens.appdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.js.nowakelock.R
import com.js.nowakelock.data.model.AppStatistics
import com.js.nowakelock.data.model.AppWithStats
import com.js.nowakelock.data.repository.appDetail.AppDetailRepository
import com.js.nowakelock.data.repository.appdas.AppDasRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 应用详情页的UI状态
 */
data class AppDetailUiState(
    val isLoading: Boolean = true,
    val appInfo: AppWithStats? = null,
    val error: String? = null,
    val isBlocked: Boolean = false
)

/**
 * 应用详情页的ViewModel
 */
class AppDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val appDasRepo: AppDetailRepository
) : ViewModel() {

    private val packageName: String = checkNotNull(savedStateHandle["packageName"])
    private val userId: Int = checkNotNull(savedStateHandle["userId"])
    private val _uiState = MutableStateFlow(AppDetailUiState())
    val uiState = _uiState.asStateFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppDetailUiState()
        )

    init {
        loadAppDetail()

    }

    /**
     * 加载应用详情
     */
    private fun loadAppDetail() {
        viewModelScope.launch {
            try {
                val appInfoFw = appDasRepo.getAppsWithStat(packageName, userId)

                appInfoFw.collect { appInfo ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            appInfo = appInfo,
                            isBlocked = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = (e.message ?: R.string.error_loading_app_detail).toString()
                    )
                }
            }
        }
    }

    fun toggleAppBlockStatus() {
//        viewModelScope.launch {
//            val currentAppInfo = _uiState.value.appInfo ?: return@launch
//            val newBlockState = !currentAppInfo.isBlocked
//
//            try {
//                appDasRepo.setAppBlocked(packageName, newBlockState)
//
//                // 更新UI状态
//                _uiState.update { it.copy(isBlocked = newBlockState) }
//
//                // 重新加载应用详情以确保数据一致性
//                loadAppDetail()
//            } catch (e: Exception) {
//                _uiState.update { it.copy(
//                    error = "更改屏蔽状态失败: ${e.message}"
//                ) }
//            }
//        }
    }
} 