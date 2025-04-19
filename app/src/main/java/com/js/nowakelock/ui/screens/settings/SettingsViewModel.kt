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
    val languageMode: LanguageMode = LanguageMode.SYSTEM
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
} 