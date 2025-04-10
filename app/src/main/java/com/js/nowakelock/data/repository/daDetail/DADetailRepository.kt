package com.js.nowakelock.data.repository.daDetail

import com.js.nowakelock.data.db.Type
import com.js.nowakelock.data.db.entity.St
import com.js.nowakelock.data.model.DAItem
import com.js.nowakelock.data.model.EventItem
import com.js.nowakelock.data.model.HourData
import kotlinx.coroutines.flow.Flow

/**
 * Repository for accessing and managing device automation item details.
 * Provides methods to get item data, recent events, timeline data, and update settings.
 */
interface DADetailRepository {
    /**
     * Get device automation item data
     * @param name Name of the item
     * @param userId User ID
     * @return Flow of DAItem
     */
    fun getDAItem(name: String, type: Type, userId: Int = 0): Flow<DAItem>

    /**
     * Get recent events for a device automation item
     * @param name Name of the item
     * @param type Type of the item
     * @param userId User ID
     * @param limit Maximum number of events to return
     * @return Flow of EventItem list
     */
    fun getRecentEvents(
        name: String,
        type: Type,
        userId: Int,
        limit: Int
    ): Flow<List<EventItem>>

    /**
     * Get timeline data for a device automation item
     * @param name Name of the item
     * @param type Type of the item
     * @param userId User ID
     * @param hours Number of hours to include in timeline
     * @return Flow of HourData list
     */
    fun getTimelineData(
        name: String,
        type: Type,
        userId: Int,
        hours: Int
    ): Flow<List<HourData>>

    /**
     * Update settings for a device automation item
     * @param setting The settings to update
     * @return True if update was successful, false otherwise
     */
    suspend fun updateDAItemSettings(setting: St)
}