package com.js.nowakelock.data.model

/**
 * Data class representing statistics for a device automation item.
 * @param totalCount Total number of events
 * @param blockedCount Number of blocked events
 * @param totalTime Total active time in milliseconds
 * @param savedTime Estimated saved time in milliseconds
 * @param formattedTotalTime Formatted total time string for display
 * @param formattedSavedTime Formatted saved time string for display
 */
data class DAStatistics(
    val totalCount: Int,
    val blockedCount: Int,
    val totalTime: Long,
    val savedTime: Long,
    val formattedTotalTime: String,
    val formattedSavedTime: String
)
