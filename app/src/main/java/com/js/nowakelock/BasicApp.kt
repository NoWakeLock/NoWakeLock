package com.js.nowakelock

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.google.gson.Gson
import com.js.nowakelock.data.broadcastreceiver.PowerConnectionReceiver
import com.js.nowakelock.data.manager.BootResetManager
import com.js.nowakelock.data.repository.preferences.UserPreferencesRepository
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.java.KoinJavaComponent.inject

class BasicApp : Application() {
    companion object {
        private const val TAG = "BasicApp"
        
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
        lateinit var gson: Gson
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        gson = Gson()

        // Initialize Koin for dependency injection
        startKoin {
            androidContext(this@BasicApp)
            modules(appModule())
            androidLogger(Level.DEBUG)
        }
        
        // Get UserPreferencesRepository from Koin
        val userPreferencesRepository: UserPreferencesRepository by inject(UserPreferencesRepository::class.java)
        
        // Initialize and use BootResetManager to check if we need to reset tables after device restart
        val bootResetManager = BootResetManager(context, userPreferencesRepository)
        val resetPerformed = bootResetManager.checkAndResetIfNeeded()
        
        if (resetPerformed) {
            Log.i(TAG, "Database tables (info and info_event) reset after device restart")
        }

        // Register PowerConnectionReceiver
        registerPowerConnectionReceiver()
    }

    private fun registerPowerConnectionReceiver() {
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_POWER_CONNECTED)
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED)
        registerReceiver(PowerConnectionReceiver(), filter)
    }
}