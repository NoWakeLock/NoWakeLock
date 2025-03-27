package com.js.nowakelock.data.model

import com.js.nowakelock.data.db.entity.Info
import com.js.nowakelock.data.db.entity.St
import java.util.concurrent.TimeUnit

/**
 * Data class representing a Wakelock item in the UI
 * Combines wakelock information and settings
 */
data class WakelockItem(
    // Basic information
    val name: String,
    val packageName: String,
    val userId: Int = 0,
    
    // Statistics
    val count: Int = 0,
    val blockCount: Int = 0,
    val countTime: Long = 0,
    
    // Settings
    val isBlocked: Boolean = false,
    val blockOnlyWhenSleeping: Boolean = false,
    val timeWindow: Int? = null  // in seconds, null if fully blocked
) {
    /**
     * Percentage of times this wakelock was blocked
     */
    val blockPercentage: Float
        get() = if (count > 0) (blockCount.toFloat() / count) * 100f else 0f
        
    /**
     * Whether this wakelock is partially blocked (has time window)
     */
    val isPartiallyBlocked: Boolean
        get() = isBlocked && timeWindow != null
        
    /**
     * Formats the time for display in the UI
     */
    fun getFormattedTime(): String {
        if (countTime == 0L) return "0s"
        
        val hours = TimeUnit.MILLISECONDS.toHours(countTime)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(countTime) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(countTime) % 60
        
        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m ${seconds}s"
            else -> "${seconds}s"
        }
    }
    
    /**
     * Formats the time saved for display in the UI
     */
    fun getFormattedSavedTime(): String {
        if (blockCount == 0) return "0s saved"
        
        // Estimate saved time based on average wakelock duration
        val avgDuration = if (count > blockCount) countTime / (count - blockCount) else 0
        val savedTime = blockCount * avgDuration
        
        val hours = TimeUnit.MILLISECONDS.toHours(savedTime)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(savedTime) % 60
        
        return when {
            hours > 0 -> "${hours}h ${minutes}m saved"
            minutes > 0 -> "${minutes}m saved"
            else -> "${TimeUnit.MILLISECONDS.toSeconds(savedTime)}s saved"
        }
    }
    
    companion object {
        /**
         * Creates a WakelockItem from Info and St entities
         */
        fun fromEntities(info: Info, st: St?): WakelockItem {
            return WakelockItem(
                name = info.name,
                packageName = info.packageName,
                userId = info.userId,
                count = info.count,
                blockCount = info.blockCount,
                countTime = info.countTime,
                isBlocked = st?.flag ?: false,
                blockOnlyWhenSleeping = st?.flagLock ?: false,
                timeWindow = if (st?.flag == true && st.allowTimeInterval > 0) 
                    TimeUnit.MILLISECONDS.toSeconds(st.allowTimeInterval).toInt() else null
            )
        }
    }
} 