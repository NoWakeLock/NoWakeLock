package com.js.nowakelock.ui.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import com.js.nowakelock.data.repository.preferences.UserPreferencesRepository.LanguageMode
import java.util.Locale

/**
 * Utility functions for handling language changes in the app
 */
object LanguageUtils {

    /**
     * Update the locale of the app based on the selected language mode
     * @param context The application context
     * @param languageMode The selected language mode
     * @return A new context with the updated locale
     */
    fun setAppLanguage(context: Context, languageMode: LanguageMode): Context {
        val locale = when (languageMode) {
            LanguageMode.ENGLISH -> Locale("en")
            LanguageMode.CHINESE -> Locale("zh")
            LanguageMode.FRENCH -> Locale("fr")
            LanguageMode.SYSTEM -> Locale.getDefault()
        }

        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
            context
        }
    }
} 