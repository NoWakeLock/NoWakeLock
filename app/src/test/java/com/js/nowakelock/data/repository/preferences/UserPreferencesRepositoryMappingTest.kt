package com.js.nowakelock.data.repository.preferences

import org.junit.Assert.assertEquals
import org.junit.Test

class UserPreferencesRepositoryMappingTest {
    @Test
    fun themeMode_fromString_mapsKnownValues() {
        assertEquals(UserPreferencesRepository.ThemeMode.LIGHT, UserPreferencesRepository.ThemeMode.fromString("light"))
        assertEquals(UserPreferencesRepository.ThemeMode.DARK, UserPreferencesRepository.ThemeMode.fromString("dark"))
        assertEquals(UserPreferencesRepository.ThemeMode.SYSTEM, UserPreferencesRepository.ThemeMode.fromString(null))
        assertEquals(UserPreferencesRepository.ThemeMode.SYSTEM, UserPreferencesRepository.ThemeMode.fromString(""))
        assertEquals(UserPreferencesRepository.ThemeMode.SYSTEM, UserPreferencesRepository.ThemeMode.fromString("unknown"))
    }

    @Test
    fun languageMode_fromString_mapsKnownValues() {
        assertEquals(UserPreferencesRepository.LanguageMode.ENGLISH, UserPreferencesRepository.LanguageMode.fromString("en"))
        assertEquals(UserPreferencesRepository.LanguageMode.CHINESE, UserPreferencesRepository.LanguageMode.fromString("zh"))
        assertEquals(UserPreferencesRepository.LanguageMode.FRENCH, UserPreferencesRepository.LanguageMode.fromString("fr"))
        assertEquals(UserPreferencesRepository.LanguageMode.GERMAN, UserPreferencesRepository.LanguageMode.fromString("de"))
        assertEquals(UserPreferencesRepository.LanguageMode.TRADITIONAL_CHINESE, UserPreferencesRepository.LanguageMode.fromString("zh-rTW"))
        assertEquals(UserPreferencesRepository.LanguageMode.SYSTEM, UserPreferencesRepository.LanguageMode.fromString(null))
        assertEquals(UserPreferencesRepository.LanguageMode.SYSTEM, UserPreferencesRepository.LanguageMode.fromString(""))
        assertEquals(UserPreferencesRepository.LanguageMode.SYSTEM, UserPreferencesRepository.LanguageMode.fromString("unknown"))
    }

    @Test
    fun languageMode_toLocaleString_roundtrips() {
        for (mode in UserPreferencesRepository.LanguageMode.entries) {
            val raw = UserPreferencesRepository.LanguageMode.toLocaleString(mode)
            assertEquals(mode, UserPreferencesRepository.LanguageMode.fromString(raw))
        }
    }
}
