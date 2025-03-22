package com.js.nowakelock

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.js.nowakelock.base.isModuleActive
import com.js.nowakelock.ui.NoWakeLockApp

class MainActivity : ComponentActivity() {

//    private lateinit var toolbar: Toolbar
//    private lateinit var drawerLayout: DrawerLayout

//    private val mainViewModel: MainViewModel by viewModel(named("MainVm"))


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //check module active
        if (!isModuleActive()) {
            Toast.makeText(this, getString(R.string.active), Toast.LENGTH_LONG).show()
        }

        enableEdgeToEdge()
        setContent {
            NoWakeLockApp()
        }
    }
}