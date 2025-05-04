package com.js.nowakelock.data.manager

import android.content.Context
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import com.js.nowakelock.data.db.AppDatabase
import com.js.nowakelock.data.repository.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * Manager class responsible for detecting device restarts and resetting database tables.
 * This ensures that infoDao and infoEventDao tables are reset after a device restart
 * and the first launch of the app, but not on subsequent launches.
 */
class BootResetManager(
    private val context: Context,
    private val userPreferencesRepository: UserPreferencesRepository
) {
    companion object {
        private const val TAG = "BootResetManager"
    }

    /**
     * Checks if the device has been restarted and resets the tables if needed.
     * This method should be called during app initialization, after Koin setup.
     *
     * @return true if tables were reset, false otherwise
     */
    fun checkAndResetIfNeeded(): Boolean {
        // Get current boot time (milliseconds since boot)
        val currentBootTime = SystemClock.elapsedRealtime()
        var resetPerformed = false

        try {
            runBlocking {
                // Get last recorded boot time and reset status
                val lastRecordedTime = userPreferencesRepository.lastBootTime.first()
                val resetDone = userPreferencesRepository.resetDoneForCurrentBoot.first()

                // Device has been restarted if currentBootTime is less than lastRecordedTime
                // or if this is the first run (lastRecordedTime = 0)
                val isAfterReboot = currentBootTime < lastRecordedTime || lastRecordedTime == 0L

                Log.d(TAG, "Boot check: current=$currentBootTime, last=$lastRecordedTime, resetDone=$resetDone, isAfterReboot=$isAfterReboot")

                // Reset tables if device restarted and reset not done yet for this boot cycle
                if (isAfterReboot || !resetDone) {
                    Log.i(TAG, "Device restarted or first run detected, resetting info and info_event tables")
                    
                    // Reset the tables
                    resetTables()
                    resetPerformed = true

                    // Update preferences to mark reset as done for this boot cycle
                    userPreferencesRepository.setLastBootTime(currentBootTime)
                    userPreferencesRepository.setResetDone(true)
                    
                    Log.i(TAG, "Tables reset completed and preferences updated")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during boot reset check: " + e.message, e)
            // Show error toast
            showErrorToast("Failed to reset database after restart")
            return false
        }

        return resetPerformed
    }

    /**
     * Resets the info and info_event tables by calling clearAll() on their DAOs.
     * This is a suspend function and should be called from a coroutine context.
     */
    private suspend fun resetTables() {
        val db = AppDatabase.getInstance(context)
        
        // Clear info table
        Log.d(TAG, "Resetting info table...")
        db.infoDao().clearAll()
        
        // Clear info_event table
        Log.d(TAG, "Resetting info_event table...")
        db.infoEventDao().clearAll()
        
        Log.i(TAG, "Database tables reset successfully: info and info_event")
    }

    /**
     * Displays an error toast message to the user.
     *
     * @param message The error message to display
     */
    private fun showErrorToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
} 