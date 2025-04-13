package com.js.nowakelock.data.model

import com.js.nowakelock.data.db.Type
import com.js.nowakelock.data.db.entity.Info
import com.js.nowakelock.data.db.entity.St
import java.util.concurrent.TimeUnit

/**
 * Data class representing a DA item in the UI
 * Combines information and settings
 */
data class DAItem(
    // Basic information
    val name: String,
    val packageName: String,
    val userId: Int = 0,
    val type: Type = Type.UnKnow,

    // Statistics
    val count: Int = 0,
    val blockCount: Int = 0,
    val countTime: Long = 0, // milliseconds
    val blockCountTime: Long = 0, // milliseconds

    // Settings
    val fullBlocked: Boolean = false,
    val screenOffBlock: Boolean = false,
    val timeWindowSec: Int = 0  // in seconds
) {
    /**
     * Percentage of times this was blocked
     */
    val blockPercentage: Float
        get() = if (count > 0) (blockCount.toFloat() / count) * 100f else 0f

    /**
     * Whether this is partially blocked (has time window)
     */
    val isPartiallyBlocked: Boolean
        get() = fullBlocked && timeWindowSec != 0

    /**
     * Checks if this DAItem represents the same item as another
     * based on core identity attributes (name, package, userId)
     * This is useful for optimized UI updates
     */
    fun isSameItemAs(other: DAItem): Boolean {
        return name == other.name &&
               packageName == other.packageName &&
               userId == other.userId &&
               type == other.type
    }

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

    private fun formatTime(timeInMillis: Long): String {
        val seconds = timeInMillis / 1000
        return when {
            seconds < 60 -> "${seconds}s"
            seconds < 3600 -> "${seconds / 60}m"
            else -> "${seconds / 3600}h ${(seconds % 3600) / 60}m"
        }
    }

    fun getCountTimeFormat(): String {
        return formatTime(countTime)
    }

    fun getBlockCountTimeFormat(): String {
        return formatTime(blockCountTime)
    }

    /**
     * Formats the time saved for display in the UI
     */
    fun getFormattedSavedTime(): String {
        if (blockCount == 0) return "0s saved"

        // Estimate saved time based on average duration
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
         * Creates a DAItem from Info and St entities
         */
        fun fromEntities(info: Info, st: St?): DAItem {
            if (info.count != 0) // calculate blockCountTime
                info.blockCountTime = info.blockCount * (info.countTime / info.count)
            return DAItem(
                name = info.name,
                packageName = info.packageName,
                userId = info.userId,
                type = info.type,
                count = info.count,
                blockCount = info.blockCount,
                countTime = info.countTime,
                blockCountTime = info.blockCountTime,
                fullBlocked = st?.fullBlock ?: false,
                screenOffBlock = st?.screenOffBlock ?: false,
                timeWindowSec = st?.timeWindowMs?.let {
                    TimeUnit.MILLISECONDS.toSeconds(it).toInt()
                } ?: 0
            )
        }

        fun toSt(daItem: DAItem): St {
            return St(
                name = daItem.name,
                packageName = daItem.packageName,
                type = daItem.type,
                userId = daItem.userId,
                fullBlock = daItem.fullBlocked,
                screenOffBlock = daItem.screenOffBlock,
                timeWindowMs = TimeUnit.SECONDS.toMillis(daItem.timeWindowSec.toLong())
            )
        }

        fun toInfo(daItem: DAItem): Info {
            return Info(
                name = daItem.name,
                packageName = daItem.packageName,
                userId = daItem.userId,
                type = daItem.type,
                count = daItem.count,
                blockCount = daItem.blockCount,
                countTime = daItem.countTime
            )
        }
    }
} 