package com.js.nowakelock

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.js.nowakelock.base.isModuleActive
import com.js.nowakelock.ui.NoWakeLockApp

/**
 * Main activity that hosts the entire app UI
 * Uses Material 3 edge-to-edge display
 */
class MainActivity : ComponentActivity() {

//    private lateinit var toolbar: Toolbar
//    private lateinit var drawerLayout: DrawerLayout

//    private val mainViewModel: MainViewModel by viewModel(named("MainVm"))


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if the module is active
        if (!isModuleActive()) {
            Toast.makeText(this, getString(R.string.active), Toast.LENGTH_LONG).show()
        }

        // Enable edge-to-edge display (must be called before setContent)
        // This replaces the SystemUiController functionality with official API
        enableEdgeToEdge()
        
        setContent {
            NoWakeLockApp()
        }
    }
}