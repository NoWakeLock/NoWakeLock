package com.js.nowakelock.data.repository.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
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
        ENGLISH, CHINESE, FRENCH, SYSTEM;
        
        companion object {
            fun fromString(value: String?): LanguageMode {
                return when (value) {
                    "en" -> ENGLISH
                    "zh" -> CHINESE
                    "fr" -> FRENCH
                    else -> SYSTEM
                }
            }
            
            fun toLocaleString(mode: LanguageMode): String {
                return when (mode) {
                    ENGLISH -> "en"
                    CHINESE -> "zh"
                    FRENCH -> "fr"
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
} 