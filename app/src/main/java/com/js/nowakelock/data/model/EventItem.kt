package com.js.nowakelock.data.model

import com.js.nowakelock.data.db.Type

/**
 * Data class representing a device automation event for UI display.
 * @param time The start time of the event in milliseconds
 * @param duration The duration of the event in milliseconds
 * @param isBlocked Whether the event was blocked
 * @param formattedTime Formatted time string for display
 * @param formattedDuration Formatted duration string for display
 */
data class EventItem(
    val type: Type,
    val time: Long,
    val duration: Long,
    val isBlocked: Boolean,
    val formattedTime: String,
    val formattedDuration: String
)
