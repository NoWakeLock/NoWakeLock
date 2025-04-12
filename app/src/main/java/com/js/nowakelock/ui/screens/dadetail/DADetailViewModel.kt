package com.js.nowakelock.ui.screens.dadetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.js.nowakelock.base.stringToType
import com.js.nowakelock.data.db.Type
import com.js.nowakelock.data.db.entity.St
import com.js.nowakelock.data.model.DAInfoEntry
import com.js.nowakelock.data.model.DAItem
import com.js.nowakelock.data.model.DAStatistics
import com.js.nowakelock.data.model.EventItem
import com.js.nowakelock.data.model.HourData
import com.js.nowakelock.data.repository.daDetail.DADetailRepository
import com.js.nowakelock.data.repository.daDetail.DAInfoRepository
import com.js.nowakelock.ui.navigation.DADetail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * ViewModel for the device automation detail screen.
 * Manages loading data, calculating statistics, and updating settings.
 */
class DADetailViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val daDetailRepository: DADetailRepository,
    private val daInfoRepository: DAInfoRepository
) : ViewModel() {

    // Extract navigation parameters from SavedStateHandle
    private val daDetail = savedStateHandle.toRoute<DADetail>()
    private val name: String = daDetail.daName ?: ""
    private val type: Type = stringToType(daDetail.type ?: Type.UnKnow.value)
    private val userId: Int = daDetail.userId ?: 0

    // UI state
    private val _uiState = MutableStateFlow<DADetailState>(DADetailState.Loading)
    val uiState: StateFlow<DADetailState> = _uiState.asStateFlow()

    // Settings state
    private val _settingsState = MutableStateFlow(DASettingsState())
    val settingsState: StateFlow<DASettingsState> = _settingsState.asStateFlow()

    // Initialize data loading
    init {
        loadData()
    }

    /**
     * Load all necessary data for the detail screen
     */
    private fun loadData() {
        viewModelScope.launch {
            try {
                // Load device automation item data
                val daItemFlow = daDetailRepository.getDAItem(name, type, userId)

                // Load recent events
                val recentEventsFlow = daDetailRepository.getRecentEvents(name, type, userId, 10)

                // Load timeline data
                val timelineDataFlow = daDetailRepository.getTimelineData(name, type, userId, 24)

                // Combine all data flows
                combine(
                    daItemFlow, recentEventsFlow, timelineDataFlow
                ) { daItem, recentEvents, timelineData ->
                    // Update settings state
                    _settingsState.value = DASettingsState(
                        isBlocked = daItem.fullBlocked,
                        sleepOnly = false, // This needs to be determined from actual settings
                        screenOffOnly = daItem.screenOffBlock,
                        timeInterval = daItem.timeWindowSec
                    )

                    // Load DA info entry asynchronously
                    val infoEntry = daInfoRepository.getInfo(daItem.name, daItem.packageName)

                    // Calculate statistics
                    val allowedCount = daItem.count - daItem.blockCount
                    val allowedTime = daItem.countTime
                    val savedTime = calculateSavedTime(allowedTime, allowedCount, daItem.blockCount)

                    val statistics = DAStatistics(
                        totalCount = daItem.count,
                        blockedCount = daItem.blockCount,
                        totalTime = daItem.countTime,
                        savedTime = savedTime,
                        formattedTotalTime = formatTime(daItem.countTime),
                        formattedSavedTime = formatTime(savedTime)
                    )

                    // Return success state
                    DADetailState.Success(
                        daItem = daItem,
                        info = infoEntry,
                        statistics = statistics,
                        timelineData = timelineData,
                        recentEvents = recentEvents
                    )
                }.catch { e ->
                    _uiState.value = DADetailState.Error(e.message ?: "Unknown error")
                }.collectLatest { state ->
                    _uiState.value = state
                }
            } catch (e: Exception) {
                _uiState.value = DADetailState.Error(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Calculate saved time based on the formula:
     * savedTime = allowedTime / allowedCount * blockedCount
     */
    private fun calculateSavedTime(allowedTime: Long, allowedCount: Int, blockedCount: Int): Long {
        return if (allowedCount > 0) {
            (allowedTime / allowedCount) * blockedCount
        } else {
            0L
        }
    }

    /**
     * Format time in milliseconds to a human-readable string
     */
    private fun formatTime(timeMs: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(timeMs)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(timeMs) % 60

        return if (hours > 0) {
            "${hours}h ${minutes}m"
        } else {
            "${minutes}m"
        }
    }

    /**
     * Update blocking setting
     */
    fun updateBlockingSetting(isBlocked: Boolean) {
        viewModelScope.launch {
            val currentState = uiState.value
            if (currentState is DADetailState.Success) {
                val st = DAItem.toSt(currentState.daItem.copy(fullBlocked = isBlocked))
                try {
                    daDetailRepository.updateDAItemSettings(st)
                } catch (e: Exception) {
                    _uiState.value = DADetailState.Error(e.message ?: "Unknown error")
                }
            }
        }
    }

    /**
     * Update condition settings
     */
    fun updateConditionSettings(sleepOnly: Boolean, screenOffOnly: Boolean) {
        viewModelScope.launch {
            val currentState = uiState.value
            if (currentState is DADetailState.Success) {
                val daItem = currentState.daItem
                val setting = DAItem.toSt(daItem.copy(screenOffBlock = screenOffOnly))

                try {
                    daDetailRepository.updateDAItemSettings(setting)
                } catch (e: Exception) {
                    _uiState.value = DADetailState.Error(e.message ?: "Unknown error")
                }
            }
        }
    }

    /**
     * Update time interval setting
     */
    fun updateTimeInterval(seconds: Int) {
        viewModelScope.launch {
            val currentState = uiState.value
            if (currentState is DADetailState.Success) {
                val daItem = currentState.daItem
                val setting = DAItem.toSt(daItem.copy(timeWindowSec = seconds))

                try {
                    daDetailRepository.updateDAItemSettings(setting)
                } catch (e: Exception) {
                    _uiState.value = DADetailState.Error(e.message ?: "Unknown error")
                }
            }
        }
    }
}