package com.js.nowakelock.ui.screens.appdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.js.nowakelock.R
import com.js.nowakelock.base.LogUtil
import com.js.nowakelock.data.db.entity.AppSt
import com.js.nowakelock.data.model.AppWithStats
import com.js.nowakelock.data.repository.appDetail.AppDetailRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

/**
 * UI state for app detail screen
 */
data class AppDetailUiState(
    val isLoading: Boolean = true,
    val appInfo: AppWithStats? = null,
    val appSt: AppSt? = null,
    val error: String? = null,
    val isBlocked: Boolean = false
)

/**
 * ViewModel for app detail screen
 * Handles loading app data and managing app settings
 */
class AppDetailViewModel(
    savedStateHandle: SavedStateHandle, private val appDetailRepo: AppDetailRepository
) : ViewModel() {

    private val packageName: String = checkNotNull(savedStateHandle["packageName"])
    private val userId: Int = checkNotNull(savedStateHandle["userId"])
    private val _uiState = MutableStateFlow(AppDetailUiState())
    val uiState = _uiState.asStateFlow().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppDetailUiState()
    )

    init {
        loadAppDetail()
        loadAppSt()
    }

    /**
     * Loads app details including statistics
     */
    private fun loadAppDetail() {
        viewModelScope.launch {
            try {
                val appInfoFw = appDetailRepo.getAppsWithStat(packageName, userId)

                appInfoFw.collect { appInfo ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false, appInfo = appInfo, isBlocked = false
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

    /**
     * Loads app settings data
     * Creates default settings if not found
     */
    private fun loadAppSt() {
        viewModelScope.launch {
            try {
                appDetailRepo.getAppSt(packageName, userId).collect { appSt ->
                    val effectiveAppSt = appSt ?: createDefaultAppSt()
                    _uiState.update { currentState ->
                        currentState.copy(appSt = effectiveAppSt)
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Error loading app settings: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Creates default app settings
     */
    private fun createDefaultAppSt(): AppSt {
        return AppSt(
            packageName = packageName,
            userId = userId,
            wakelock = false,
            alarm = false,
            service = false,
            rE_Wakelock = emptySet(),
            rE_Alarm = emptySet(),
            rE_Service = emptySet()
        )
    }

    /**
     * Updates global wakelock block setting
     */
    fun updateWakelockBlock(isBlocked: Boolean) {
        _uiState.value.appSt?.let { currentAppSt ->
            val updatedAppSt = currentAppSt.copy(wakelock = isBlocked)
            saveAppSt(updatedAppSt)
        }
    }

    /**
     * Updates global alarm block setting
     */
    fun updateAlarmBlock(isBlocked: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        _uiState.value.appSt?.let { currentAppSt ->
            val updatedAppSt = currentAppSt.copy(alarm = isBlocked)
            saveAppSt(updatedAppSt)
        }
    }

    /**
     * Updates global service block setting
     */
    fun updateServiceBlock(isBlocked: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        _uiState.value.appSt?.let { currentAppSt ->
            val updatedAppSt = currentAppSt.copy(service = isBlocked)
            saveAppSt(updatedAppSt)
        }
    }

    /**
     * Adds a wakelock pattern if valid
     * @return true if pattern was valid and added, false otherwise
     */
    fun addWakelockPattern(pattern: String) = viewModelScope.launch(Dispatchers.IO) {
        if (validateRegexPattern(pattern)) {
            _uiState.value.appSt?.let { currentAppSt ->
                val updatedPatterns = currentAppSt.rE_Wakelock + pattern
                val updatedAppSt = currentAppSt.copy(rE_Wakelock = updatedPatterns)
                saveAppSt(updatedAppSt)
            }
        }
    }

    /**
     * Removes a wakelock pattern
     */
    fun removeWakelockPattern(pattern: String) = viewModelScope.launch(Dispatchers.IO) {
        _uiState.value.appSt?.let { currentAppSt ->
            val updatedPatterns = currentAppSt.rE_Wakelock - pattern
            val updatedAppSt = currentAppSt.copy(rE_Wakelock = updatedPatterns)
            saveAppSt(updatedAppSt)
        }
    }

    /**
     * Updates all wakelock patterns
     * @return true if all patterns were valid and updated, false otherwise
     */
    fun updateWakelockPatterns(patterns: Set<String>) = viewModelScope.launch(Dispatchers.IO) {
        // Validate all patterns first
        for (pattern in patterns) {
            if (validateRegexPattern(pattern)) {

                _uiState.value.appSt?.let { currentAppSt ->
                    val updatedAppSt = currentAppSt.copy(rE_Wakelock = patterns)
                    saveAppSt(updatedAppSt)
                }
            }
        }
    }

    /**
     * Adds an alarm pattern if valid
     * @return true if pattern was valid and added, false otherwise
     */
    fun addAlarmPattern(pattern: String) = viewModelScope.launch(Dispatchers.IO) {
        if (validateRegexPattern(pattern)) {
            _uiState.value.appSt?.let { currentAppSt ->
                val updatedPatterns = currentAppSt.rE_Alarm + pattern
                val updatedAppSt = currentAppSt.copy(rE_Alarm = updatedPatterns)
                saveAppSt(updatedAppSt)
            }
        }

    }

    /**
     * Removes an alarm pattern
     */
    fun removeAlarmPattern(pattern: String) {
        _uiState.value.appSt?.let { currentAppSt ->
            val updatedPatterns = currentAppSt.rE_Alarm - pattern
            val updatedAppSt = currentAppSt.copy(rE_Alarm = updatedPatterns)
            saveAppSt(updatedAppSt)
        }
    }

    /**
     * Updates all alarm patterns
     * @return true if all patterns were valid and updated, false otherwise
     */
    fun updateAlarmPatterns(patterns: Set<String>) = viewModelScope.launch(Dispatchers.IO) {
        // Validate all patterns first
        for (pattern in patterns) {
            if (validateRegexPattern(pattern)) {
                _uiState.value.appSt?.let { currentAppSt ->
                    val updatedAppSt = currentAppSt.copy(rE_Alarm = patterns)
                    saveAppSt(updatedAppSt)
                }
            }
        }
    }

    /**
     * Adds a service pattern if valid
     * @return true if pattern was valid and added, false otherwise
     */
    fun addServicePattern(pattern: String) = viewModelScope.launch(Dispatchers.IO) {
        if (validateRegexPattern(pattern)) {

            _uiState.value.appSt?.let { currentAppSt ->
                val updatedPatterns = currentAppSt.rE_Service + pattern
                val updatedAppSt = currentAppSt.copy(rE_Service = updatedPatterns)
                saveAppSt(updatedAppSt)
            }
        }
    }

    /**
     * Removes a service pattern
     */
    fun removeServicePattern(pattern: String) = viewModelScope.launch(Dispatchers.IO) {
        _uiState.value.appSt?.let { currentAppSt ->
            val updatedPatterns = currentAppSt.rE_Service - pattern
            val updatedAppSt = currentAppSt.copy(rE_Service = updatedPatterns)
            saveAppSt(updatedAppSt)
        }
    }

    /**
     * Updates all service patterns
     * @return true if all patterns were valid and updated, false otherwise
     */
    fun updateServicePatterns(patterns: Set<String>) = viewModelScope.launch(Dispatchers.IO) {
        // Validate all patterns first
        for (pattern in patterns) {
            if (validateRegexPattern(pattern)) {
                _uiState.value.appSt?.let { currentAppSt ->
                    val updatedAppSt = currentAppSt.copy(rE_Service = patterns)
                    saveAppSt(updatedAppSt)
                }
            }
        }
    }

    /**
     * Validates a regex pattern
     * @return true if pattern is valid, false otherwise
     */
    fun validateRegexPattern(pattern: String): Boolean {
        return try {
            Pattern.compile(pattern)
            true
        } catch (e: PatternSyntaxException) {
            false
        }
    }

    /**
     * Saves current app settings to database
     */
    private fun saveAppSt(appSt: AppSt) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val success = appDetailRepo.updateAppSt(appSt)
            if (!success) {
                LogUtil.d("AppDetailViewModel", "Failed to save app settings")
            }
        } catch (e: Exception) {
            LogUtil.d("AppDetailViewModel", "Error saving app settings: ${e.message}")
        }
    }

    fun toggleAppBlockStatus() {
        // Function left empty for future implementation
    }
} 