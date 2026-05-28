package com.js.nowakelock.shizuku

import android.content.Context
import android.util.Log

object ShizukuRestricter {
    private const val TAG = "ShizukuRestricter"

    /**
     * Apply restriction settings for an app using AppOps and ActivityManager
     * 
     * @param context the context
     * @param packageName the target package name
     * @param restrictWakelock whether wakelocks should be restricted
     * @param restrictAlarm whether alarms should be restricted
     * @param restrictService whether services should be restricted
     */
    fun applyRestrictSettings(
        context: Context,
        packageName: String,
        restrictWakelock: Boolean,
        restrictAlarm: Boolean,
        restrictService: Boolean
    ) {
        if (!ShizukuManager.hasPermission()) {
            Log.w(TAG, "Cannot apply Shizuku restrict settings: Permission not granted")
            return
        }

        // Apply Wakelock restriction via AppOps
        val wakelockMode = if (restrictWakelock) "ignore" else "allow"
        ShizukuManager.executeCommand("cmd appops set $packageName WAKE_LOCK $wakelockMode")
        Log.d(TAG, "Set WAKE_LOCK to $wakelockMode for $packageName")

        // Apply Alarm restriction (approximate by using RUN_IN_BACKGROUND and ALARM options)
        // Some systems support 'ALARM_WAKEUP', but RUN_ANY_IN_BACKGROUND is more universal for background execution
        val alarmMode = if (restrictAlarm) "ignore" else "allow"
        ShizukuManager.executeCommand("cmd appops set $packageName RUN_IN_BACKGROUND $alarmMode")
        Log.d(TAG, "Set RUN_IN_BACKGROUND to $alarmMode for $packageName")

        // Apply Service restriction (can't completely restrict services via appops easily, 
        // but we can try to force stop it if restriction is true. Continuous polling might be needed for real service restriction)
        if (restrictService) {
            // This is a one-time kill; a background service would need to monitor this
            ShizukuManager.executeCommand("am force-stop $packageName")
            Log.d(TAG, "Force stopped $packageName to prevent services")
        }
    }
    
    /**
     * Force stop a specific service
     */
    fun stopService(packageName: String, serviceName: String) {
        if (!ShizukuManager.hasPermission()) return
        
        // Form intent for the service
        val component = "$packageName/$serviceName"
        ShizukuManager.executeCommand("am stopservice $component")
        Log.d(TAG, "Stopped service $component")
    }
}