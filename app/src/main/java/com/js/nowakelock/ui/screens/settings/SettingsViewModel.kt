package com.js.nowakelock.ui.screens.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.js.nowakelock.base.SPTools
import com.js.nowakelock.data.repository.backup.BackupManager
import com.js.nowakelock.data.repository.preferences.UserPreferencesRepository
import com.js.nowakelock.data.repository.preferences.UserPreferencesRepository.LanguageMode
import com.js.nowakelock.data.repository.preferences.UserPreferencesRepository.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
    val debugMode: Boolean = false
)

/**
 * ViewModel for Settings screen
 */
open class SettingsViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val backupManager: BackupManager
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
                    showMessage("备份创建成功")
                } else {
                    showMessage("备份创建失败: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                showMessage("备份创建失败: ${e.message}")
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
                    showMessage("恢复成功")
                } else {
                    showMessage("恢复失败: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                showMessage("恢复失败: ${e.message}")
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
     * 显示消息
     */
    private fun showMessage(message: String) {
        _uiState.update { it.copy(message = message) }
    }
} 