package com.js.nowakelock.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.js.nowakelock.data.repository.preferences.UserPreferencesRepository
import com.js.nowakelock.data.repository.preferences.UserPreferencesRepository.ThemeMode
import com.js.nowakelock.data.repository.preferences.UserPreferencesRepository.LanguageMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * UI state class for the Settings screen
 */
data class SettingsUiState(
    val isLoading: Boolean = false,
    val message: String = "",
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val languageMode: LanguageMode = LanguageMode.SYSTEM,
    val powerFlag: Boolean = false,
    val clearFlag: Boolean = false
)

/**
 * ViewModel for the Settings screen that manages user preferences
 */
open class SettingsViewModel(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
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
    }

    /**
     * Update theme preference
     */
    fun updateTheme(themeMode: ThemeMode) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                userPreferencesRepository.setThemeMode(themeMode)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    themeMode = themeMode
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
    fun updateLanguage(languageMode: LanguageMode) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                userPreferencesRepository.setLanguageMode(languageMode)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    languageMode = languageMode
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
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    message = e.message ?: "Error updating clear flag"
                )
            }
        }
    }
} 