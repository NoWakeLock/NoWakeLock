package com.js.nowakelock.data.manager

import android.content.Context
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import com.js.nowakelock.base.getCPResult
import com.js.nowakelock.data.db.Type
import com.js.nowakelock.data.model.CheckStatus
import com.js.nowakelock.data.model.ModuleCheckResult
import com.js.nowakelock.data.provider.ProviderMethod
import com.js.nowakelock.data.repository.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * Manager class responsible for detecting device restarts and checking module status.
 * This ensures that module checks are performed after a device restart
 * and the first launch of the app, but not on subsequent launches.
 */
class ModuleCheckManager(
    private val context: Context,
    private val userPreferencesRepository: UserPreferencesRepository
) {
    companion object {
        private const val TAG = "ModuleCheckManager"
    }

    /**
     * Checks if the device has been restarted and performs module checks if needed.
     * This method should be called during app initialization, after Koin setup.
     *
     * @return true if checks were performed, false otherwise
     */
    fun checkIfNeeded(): Boolean {
        // Get current boot time (milliseconds since boot)
        val currentBootTime = SystemClock.elapsedRealtime()
        var checkPerformed = false

        try {
            runBlocking {
                // Get last recorded boot time and check status
                val lastRecordedTime = userPreferencesRepository.lastBootTime.first()
                val checkDone = userPreferencesRepository.moduleCheckDoneForCurrentBoot.first()

                // Device has been restarted if currentBootTime is less than lastRecordedTime
                // or if this is the first run (lastRecordedTime = 0)
                val isAfterReboot = currentBootTime < lastRecordedTime || lastRecordedTime == 0L

                Log.d(TAG, "Boot check: current=$currentBootTime, last=$lastRecordedTime, checkDone=$checkDone, isAfterReboot=$isAfterReboot")

                // Perform module checks if device restarted and check not done yet for this boot cycle
                if (isAfterReboot || !checkDone) {
                    Log.i(TAG, "Device restarted or first run detected, performing module checks")
                    
                    // Perform module checks
                    performModuleChecks()
                    checkPerformed = true

                    // Update preferences to mark check as done for this boot cycle
                    userPreferencesRepository.setModuleCheckDone(true)
                    
                    Log.i(TAG, "Module checks completed and preferences updated")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during module check: " + e.message, e)
            return false
        }

        return checkPerformed
    }

    /**
     * Performs module checks and returns the result
     * This can be called independently if needed
     * 
     * @return The module check result object
     */
    fun performModuleChecks(): ModuleCheckResult {
        val moduleActive = checkModuleActive()
        val moduleVersion = if (moduleActive) getModuleVersion() else null
        val hookStatus = mapOf(
            Type.Wakelock to checkHookEffectiveness(Type.Wakelock),
            Type.Alarm to checkHookEffectiveness(Type.Alarm),
            Type.Service to checkHookEffectiveness(Type.Service)
        )
        val configPathValid = checkConfigPath()
        val shizukuActive = com.js.nowakelock.base.isShizukuActive()
        
        // Determine overall status based on component statuses
        val overallStatus = when {
            moduleActive && !configPathValid -> CheckStatus.ERROR
            moduleActive && (!hookStatus[Type.Wakelock]!! || !hookStatus[Type.Alarm]!! || !hookStatus[Type.Service]!!) -> CheckStatus.WARNING
            moduleActive -> CheckStatus.NORMAL
            shizukuActive -> CheckStatus.SHIZUKU
            else -> CheckStatus.ERROR
        }
        
        return ModuleCheckResult(
            moduleActive = moduleActive,
            moduleVersion = moduleVersion,
            hookStatus = hookStatus,
            configPathValid = configPathValid,
            shizukuActive = shizukuActive,
            overallStatus = overallStatus
        )
    }
    
    /**
     * Check if the module is active using ContentProvider
     */
    private fun checkModuleActive(): Boolean {
        return try {
            val args = Bundle()
            val result = getCPResult(context, ProviderMethod.CheckHookActive.value, args)
            result?.getBoolean("active", false) ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Error checking module active status: ${e.message}")
            false
        }
    }
    
    /**
     * Get the module version
     */
    private fun getModuleVersion(): String? {
        return try {
            val args = Bundle()
            val result = getCPResult(context, ProviderMethod.CheckHookActive.value, args)
            result?.getString("version")
        } catch (e: Exception) {
            Log.e(TAG, "Error getting module version: ${e.message}")
            null
        }
    }
    
    /**
     * Check if the hook for the given type has data in the database
     */
    private fun checkHookEffectiveness(type: Type): Boolean {
        return try {
            val args = Bundle().apply {
                putString("type", type.value)
            }
            val result = getCPResult(context, ProviderMethod.CheckHookEffectiveness.value, args)
            result?.getBoolean("hasData", false) ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Error checking hook effectiveness for ${type.value}: ${e.message}")
            false
        }
    }
    
    /**
     * Check if the config path exists
     */
    private fun checkConfigPath(): Boolean {
        return try {
            val args = Bundle()
            val result = getCPResult(context, ProviderMethod.CheckSharedPreferencesPath.value, args)
            result?.getBoolean("pathExists", false) ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Error checking config path: ${e.message}")
            false
        }
    }
} 