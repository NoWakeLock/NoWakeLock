package com.js.nowakelock.shizuku

import android.content.Context
import android.util.Log

object ShizukuBlocker {
    private const val TAG = "ShizukuBlocker"

    /**
     * Apply blocking settings for an app using AppOps and ActivityManager
     * 
     * @param context the context
     * @param packageName the target package name
     * @param blockWakelock whether wakelocks should be blocked
     * @param blockAlarm whether alarms should be blocked
     * @param blockService whether services should be blocked
     */
    fun applyBlockSettings(
        context: Context,
        packageName: String,
        blockWakelock: Boolean,
        blockAlarm: Boolean,
        blockService: Boolean
    ) {
        if (!ShizukuManager.hasPermission()) {
            Log.w(TAG, "Cannot apply Shizuku block settings: Permission not granted")
            return
        }

        // Apply Wakelock block via AppOps
        val wakelockMode = if (blockWakelock) "ignore" else "allow"
        ShizukuManager.executeCommand("cmd appops set $packageName WAKE_LOCK $wakelockMode")
        Log.d(TAG, "Set WAKE_LOCK to $wakelockMode for $packageName")

        // Apply Alarm block (approximate by using RUN_IN_BACKGROUND and ALARM options)
        // Some systems support 'ALARM_WAKEUP', but RUN_ANY_IN_BACKGROUND is more universal for background execution
        val alarmMode = if (blockAlarm) "ignore" else "allow"
        ShizukuManager.executeCommand("cmd appops set $packageName RUN_IN_BACKGROUND $alarmMode")
        Log.d(TAG, "Set RUN_IN_BACKGROUND to $alarmMode for $packageName")

        // Apply Service block (can't completely block services via appops easily, 
        // but we can try to force stop it if block is true. Continuous polling might be needed for real service blocking)
        if (blockService) {
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