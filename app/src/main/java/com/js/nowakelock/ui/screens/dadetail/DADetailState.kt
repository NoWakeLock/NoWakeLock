package com.js.nowakelock.ui.screens.dadetail

import com.js.nowakelock.data.model.DAInfoEntry
import com.js.nowakelock.data.model.DAItem
import com.js.nowakelock.data.model.DAStatistics
import com.js.nowakelock.data.model.EventItem
import com.js.nowakelock.data.model.HourData

/**
 * Sealed class representing the UI state of the device automation detail screen.
 */
sealed class DADetailState {
    /**
     * Loading state when data is being fetched
     */
    object Loading : DADetailState()
    
    /**
     * Success state when data is loaded successfully
     * @param daItem The device automation item data
     * @param info The detailed information about the item (can be null)
     * @param statistics The statistics for the item
     * @param timelineData The timeline data for visualization
     * @param recentEvents List of recent events
     */
    data class Success(
        val daItem: DAItem,
        val info: DAInfoEntry?,
        val statistics: DAStatistics,
        val timelineData: List<HourData>,
        val recentEvents: List<EventItem>
    ) : DADetailState()
    
    /**
     * Error state when data loading fails
     * @param message Error message to display
     */
    data class Error(val message: String) : DADetailState()
}