package com.js.nowakelock.data.model

/**
 * Data class representing aggregated hourly data for timeline visualization.
 * @param hour The hour (0-23)
 * @param label The formatted label for the hour (e.g., "12PM")
 * @param total Total number of events in this hour
 * @param blocked Number of blocked events in this hour
 */
data class HourData(
    val hour: Int,
    val label: String,
    val total: Int,
    val blocked: Int
)