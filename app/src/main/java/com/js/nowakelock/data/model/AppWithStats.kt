package com.js.nowakelock.data.model

import com.js.nowakelock.data.db.entity.AppInfo

/**
 * Data class that combines AppInfo with wakelock statistics
 * Used for displaying app entries with their associated wakelock stats
 */
data class AppWithStats(
    val appInfo: AppInfo,
    val wakelockCount: Int = 0,
    val wakelockBlockedCount: Int = 0, 
    val wakelockTime: Long = 0,
    val wakelockNames: List<String> = emptyList()
) {
    /**
     * Returns formatted time string from milliseconds
     */
    fun getFormattedTime(): String {
        if (wakelockTime <= 0) return "0s"
        
        val seconds = wakelockTime / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        
        return when {
            hours > 0 -> "${hours}h ${minutes % 60}m"
            minutes > 0 -> "${minutes}m ${seconds % 60}s" 
            else -> "${seconds}s"
        }
    }
} 