package com.js.nowakelock.data.repository.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension property for DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

/**
 * Repository class that manages user preferences such as theme and language
 */
class UserPreferencesRepository(private val context: Context) {

    // PreferencesKeys
    companion object {
        val THEME_KEY = stringPreferencesKey("theme")
        val LANGUAGE_KEY = stringPreferencesKey("language")
        val POWER_FLAG_KEY = booleanPreferencesKey("power_flag")
        val CLEAR_FLAG_KEY = booleanPreferencesKey("clear_flag")
        // Keys for boot reset functionality
        val LAST_BOOT_TIME_KEY = longPreferencesKey("last_boot_time")
        val RESET_DONE_KEY = booleanPreferencesKey("reset_done_for_current_boot")
        // Key for module check functionality
        val MODULE_CHECK_DONE_KEY = booleanPreferencesKey("module_check_done_for_current_boot")
    }

    // Possible theme values
    enum class ThemeMode {
        LIGHT, DARK, SYSTEM;
        
        companion object {
            fun fromString(value: String?): ThemeMode {
                return when (value) {
                    "light" -> LIGHT
                    "dark" -> DARK
                    else -> SYSTEM
                }
            }
        }
    }
    
    // Possible language values
    enum class LanguageMode {
        ENGLISH, CHINESE, FRENCH, GERMAN, TRADITIONAL_CHINESE, SYSTEM;
        
        companion object {
            fun fromString(value: String?): LanguageMode {
                return when (value) {
                    "en" -> ENGLISH
                    "zh" -> CHINESE
                    "fr" -> FRENCH
                    "de" -> GERMAN
                    "zh-rTW" -> TRADITIONAL_CHINESE
                    else -> SYSTEM
                }
            }
            
            fun toLocaleString(mode: LanguageMode): String {
                return when (mode) {
                    ENGLISH -> "en"
                    CHINESE -> "zh"
                    FRENCH -> "fr"
                    GERMAN -> "de"
                    TRADITIONAL_CHINESE -> "zh-rTW"
                    SYSTEM -> ""
                }
            }
        }
    }

    // Get theme mode flow
    val themeMode: Flow<ThemeMode> = context.dataStore.data
        .map { preferences ->
            ThemeMode.fromString(preferences[THEME_KEY])
        }

    // Get language mode flow
    val languageMode: Flow<LanguageMode> = context.dataStore.data
        .map { preferences ->
            LanguageMode.fromString(preferences[LANGUAGE_KEY])
        }
        
    // Get power flag flow - default to false if not set
    val powerFlag: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[POWER_FLAG_KEY] ?: false
        }
        
    // Get clear flag flow - default to false if not set
    val clearFlag: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[CLEAR_FLAG_KEY] ?: false
        }
        
    // Get last boot time flow - default to 0 if not set
    val lastBootTime: Flow<Long> = context.dataStore.data
        .map { preferences -> 
            preferences[LAST_BOOT_TIME_KEY] ?: 0L 
        }
    
    // Get reset done flag flow - default to false if not set
    val resetDoneForCurrentBoot: Flow<Boolean> = context.dataStore.data
        .map { preferences -> 
            preferences[RESET_DONE_KEY] ?: false 
        }

    // Get module check done flag flow - default to false if not set
    val moduleCheckDoneForCurrentBoot: Flow<Boolean> = context.dataStore.data
        .map { preferences -> 
            preferences[MODULE_CHECK_DONE_KEY] ?: false 
        }

    // Save theme preference
    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = mode.name.lowercase()
        }
    }

    // Save language preference
    suspend fun setLanguageMode(mode: LanguageMode) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = LanguageMode.toLocaleString(mode)
        }
    }
    
    // Save power flag preference
    suspend fun setPowerFlag(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[POWER_FLAG_KEY] = enabled
        }
    }
    
    // Save clear flag preference
    suspend fun setClearFlag(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[CLEAR_FLAG_KEY] = enabled
        }
    }
    
    // Save last boot time preference
    suspend fun setLastBootTime(timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[LAST_BOOT_TIME_KEY] = timestamp
        }
    }
    
    // Save reset done preference
    suspend fun setResetDone(done: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[RESET_DONE_KEY] = done
        }
    }

    // Save module check done preference
    suspend fun setModuleCheckDone(done: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[MODULE_CHECK_DONE_KEY] = done
        }
    }
} 