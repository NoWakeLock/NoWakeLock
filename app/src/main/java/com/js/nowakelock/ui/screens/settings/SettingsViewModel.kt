package com.js.nowakelock.ui.screens.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.js.nowakelock.base.SPTools
import com.js.nowakelock.data.db.AppDatabase
import com.js.nowakelock.data.provider.ProviderMethod
import com.js.nowakelock.data.repository.backup.BackupManager
import com.js.nowakelock.data.repository.preferences.UserPreferencesRepository
import com.js.nowakelock.data.repository.preferences.UserPreferencesRepository.LanguageMode
import com.js.nowakelock.data.repository.preferences.UserPreferencesRepository.ThemeMode
import com.js.nowakelock.base.getCPResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.os.Bundle
import android.content.Context
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.delay
import androidx.annotation.StringRes
import com.js.nowakelock.R
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

/**
 * UI state for Settings screen
 */
data class SettingsUiState(
    val isLoading: Boolean = false,
    val message: String = "",
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val languageMode: LanguageMode = LanguageMode.SYSTEM,
    val powerFlag: Boolean = false,
    val clearFlag: Boolean = false,
    val backupInProgress: Boolean = false,
    val restoreInProgress: Boolean = false,
    val debugMode: Boolean = false,
    val dataTimeRange: String = "",
    val clearDataInProgress: Boolean = false
)

/**
 * ViewModel for Settings screen
 */
open class SettingsViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val backupManager: BackupManager,
    private val context: Context
) : ViewModel() {
    // UI状态
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    // Theme preference from repository
    val themeMode = userPreferencesRepository.themeMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemeMode.SYSTEM
        )

    // Language preference from repository
    val languageMode = userPreferencesRepository.languageMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LanguageMode.SYSTEM
        )
        
    // Power flag preference from repository
    val powerFlag = userPreferencesRepository.powerFlag
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )
    
    // Clear flag preference from repository
    val clearFlag = userPreferencesRepository.clearFlag
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )
        
    // Debug mode from SPTools
    private val _debugMode = MutableStateFlow(SPTools.getBoolean("debug", false))
    val debugMode: StateFlow<Boolean> = _debugMode

    init {
        // Initialize UI state with current preferences
        viewModelScope.launch {
            userPreferencesRepository.themeMode.collect { theme ->
                _uiState.value = _uiState.value.copy(themeMode = theme)
            }
        }

        viewModelScope.launch {
            userPreferencesRepository.languageMode.collect { language ->
                _uiState.value = _uiState.value.copy(languageMode = language)
            }
        }
        
        viewModelScope.launch {
            userPreferencesRepository.powerFlag.collect { powerFlag ->
                _uiState.value = _uiState.value.copy(powerFlag = powerFlag)
            }
        }
        
        viewModelScope.launch {
            userPreferencesRepository.clearFlag.collect { clearFlag ->
                _uiState.value = _uiState.value.copy(clearFlag = clearFlag)
            }
        }
        
        // Set initial debug mode state
        _uiState.value = _uiState.value.copy(debugMode = _debugMode.value)
        
        // Load initial data time range
        loadDataTimeRange()
    }

    /**
     * Update theme preference
     */
    fun updateTheme(mode: ThemeMode) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                userPreferencesRepository.setThemeMode(mode)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    themeMode = mode
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = e.message ?: "Error updating theme"
                )
            }
        }
    }

    /**
     * Update language preference
     */
    fun updateLanguage(mode: LanguageMode) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                userPreferencesRepository.setLanguageMode(mode)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    languageMode = mode
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = e.message ?: "Error updating language"
                )
            }
        }
    }
    
    /**
     * Update power flag preference
     */
    fun updatePowerFlag(enabled: Boolean) {
        viewModelScope.launch {
            try {
                userPreferencesRepository.setPowerFlag(enabled)
                // 不更新整个 uiState，只在出错时更新消息
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    message = e.message ?: "Error updating power flag"
                )
            }
        }
    }
    
    /**
     * Update clear flag preference
     */
    fun updateClearFlag(enabled: Boolean) {
        viewModelScope.launch {
            try {
                userPreferencesRepository.setClearFlag(enabled)
                // 不更新整个 uiState，只在出错时更新消息
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    message = e.message ?: "Error updating clear flag"
                )
            }
        }
    }
    
    /**
     * Update debug mode using SPTools
     */
    fun updateDebugMode(enabled: Boolean) {
        SPTools.setBoolean("debug", enabled)
        _debugMode.value = enabled
    }
    
    /**
     * 创建备份
     * @param uri 保存备份的文件URI
     */
    fun createBackup(uri: Uri) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(backupInProgress = true) }
                val result = backupManager.createBackup(uri)
                
                if (result.isSuccess) {
                    showMessage(context.getString(R.string.backup_created))
                } else {
                    showMessage(context.getString(R.string.backup_creation_failed, result.exceptionOrNull()?.message))
                }
            } catch (e: Exception) {
                showMessage(context.getString(R.string.backup_creation_failed, e.message))
            } finally {
                _uiState.update { it.copy(backupInProgress = false) }
            }
        }
    }
    
    /**
     * 从备份中恢复
     * @param uri 备份文件的URI
     */
    fun restoreBackup(uri: Uri) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(restoreInProgress = true) }
                val result = backupManager.restoreBackup(uri)
                
                if (result.isSuccess) {
                    showMessage(context.getString(R.string.restore_success))
                } else {
                    showMessage(context.getString(R.string.restore_failed, result.exceptionOrNull()?.message))
                }
            } catch (e: Exception) {
                showMessage(context.getString(R.string.restore_failed, e.message))
            } finally {
                _uiState.update { it.copy(restoreInProgress = false) }
            }
        }
    }
    
    /**
     * 获取格式化的日期字符串，用于备份文件命名
     */
    fun getFormattedDate(): String {
        return backupManager.getFormattedDate()
    }
    
    /**
     * Calculate and load the data time range for display
     */
    fun loadDataTimeRange() {
        viewModelScope.launch {
            try {
                val earliestTime = withContext(Dispatchers.IO) {
                    AppDatabase.getInstance(context).infoEventDao().getEarliestEventTime()
                }
                
                if (earliestTime == null || earliestTime == 0L) {
                    _uiState.update { it.copy(dataTimeRange = "No data") }
                    return@launch
                }
                
                val now = System.currentTimeMillis()
                val diffMillis = now - earliestTime
                
                val days = TimeUnit.MILLISECONDS.toDays(diffMillis)
                val hours = TimeUnit.MILLISECONDS.toHours(diffMillis) % 24
                
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                val formattedDate = dateFormat.format(Date(earliestTime))
                
                val rangeText = if (days > 0) {
                    "$formattedDate ($days days ${hours}h)"
                } else if (hours > 0) {
                    "$formattedDate (${hours}h)"
                } else {
                    "$formattedDate (< 1h)"
                }
                
                _uiState.update { it.copy(dataTimeRange = rangeText) }
            } catch (e: Exception) {
                _uiState.update { it.copy(dataTimeRange = "Error loading data range") }
            }
        }
    }
    
    /**
     * Clear all data from local and remote databases
     */
    fun clearAllData() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(clearDataInProgress = true) }
                
                // Run clearing operations in parallel
                withContext(Dispatchers.IO) {
                    // Use coroutineScope for structured concurrency
                    coroutineScope {
                        // Clear remote database via ContentProvider
                        val remoteClearJob = async {
                            val args = Bundle().apply {
                                putBoolean("clearAll", true)
                            }
                            getCPResult(context, ProviderMethod.ClearData.value, args)
                        }
                        
                        // Clear local database tables in parallel
                        val db = AppDatabase.getInstance(context)
                        val clearInfoJob = async { db.infoDao().clearAll() }
                        val clearEventJob = async { db.infoEventDao().clearAll() }
                        
                        // Wait for all operations to complete
                        remoteClearJob.await()
                        clearInfoJob.await()
                        clearEventJob.await()
                    }
                }
                
                // Update data time range after clearing
                loadDataTimeRange()
                
                showMessage(context.getString(R.string.data_cleared))
            } catch (e: Exception) {
                showMessage(context.getString(R.string.clear_data_failed, e.message))
            } finally {
                _uiState.update { it.copy(clearDataInProgress = false) }
            }
        }
    }
    
    /**
     * Shows a message in the UI and automatically clears it after a delay
     */
    private fun showMessage(message: String) {
        _uiState.update { it.copy(message = message) }
        
        // Clear the message after a delay
        viewModelScope.launch {
            delay(2000) // Clear message after 3 seconds
            _uiState.update { it.copy(message = "") }
        }
    }
} 