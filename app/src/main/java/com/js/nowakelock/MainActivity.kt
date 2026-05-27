package com.js.nowakelock

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.js.nowakelock.base.isModuleActive
import com.js.nowakelock.data.repository.preferences.UserPreferencesRepository
import com.js.nowakelock.ui.NoWakeLockApp
import com.js.nowakelock.ui.utils.LanguageUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject

/**
 * Main activity that hosts the entire app UI
 * Uses Material 3 edge-to-edge display
 */
class MainActivity : ComponentActivity() {

    private val userPreferencesRepository: UserPreferencesRepository by inject()
    private var currentLanguageMode: UserPreferencesRepository.LanguageMode? = null

    override fun attachBaseContext(newBase: Context) {
        // Initialize Koin to access repositories before super.attachBaseContext is called
        // This is a custom solution for this app, that assumes Koin has been initialized in Application class
        val appContext = newBase.applicationContext
        val repo = UserPreferencesRepository(appContext)
        
        // Get the language preference synchronously
        val languageMode = runBlocking { 
            repo.languageMode.first() 
        }
        currentLanguageMode = languageMode

        // Apply the language setting
        val context = LanguageUtils.setAppLanguage(newBase, languageMode)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Attempt to initialize Shizuku monitoring mode if available
        if (com.js.nowakelock.shizuku.ShizukuManager.isShizukuAvailable()) {
            com.js.nowakelock.shizuku.ShizukuManager.requestPermission()
            if (com.js.nowakelock.shizuku.ShizukuManager.hasPermission()) {
                com.js.nowakelock.shizuku.ShizukuMonitorService.start(applicationContext)
            }
        } else if (!isModuleActive()) {
            // Only show the "Module not active" toast if neither Shizuku nor Xposed is active
            Toast.makeText(this, getString(R.string.active), Toast.LENGTH_LONG).show()
        }

        // Enable edge-to-edge display (must be called before setContent)
        // This replaces the SystemUiController functionality with official API
        enableEdgeToEdge()
        
        // Monitor language changes and recreate activity if needed
        lifecycleScope.launch {
            userPreferencesRepository.languageMode.collect { languageMode ->
                if (currentLanguageMode != languageMode) {
                    currentLanguageMode = languageMode
                    recreate() // Recreate the activity to apply new language
                }
            }
        }
        
        setContent {
            NoWakeLockApp()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Do not stop ShizukuMonitorService here; it runs in the background
    }
}