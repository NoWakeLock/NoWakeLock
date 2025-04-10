package com.js.nowakelock.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.js.nowakelock.data.db.Type
import com.js.nowakelock.data.db.entity.InfoEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface InfoEventDao : BaseDao<InfoEvent> {
    @Query("SELECT * FROM info_event")
    suspend fun loadAllEvents(): List<InfoEvent>

    @Query("SELECT * FROM info_event WHERE userId_event = :userId")
    suspend fun loadEvents(userId: Int): List<InfoEvent>

    @Query("SELECT * FROM info_event WHERE type_event = :type")
    suspend fun loadEvents(type: Type): List<InfoEvent>

    @Query("SELECT * FROM info_event WHERE type_event = :type AND userId_event = :userId")
    suspend fun loadEvents(type: Type, userId: Int): List<InfoEvent>

    @Query("SELECT * FROM info_event WHERE packageName_event = :packageName AND userId_event = :userId")
    suspend fun loadEvents(packageName: String, userId: Int): List<InfoEvent>

    @Query("SELECT * FROM info_event WHERE packageName_event = :packageName AND type_event = :type AND userId_event = :userId")
    suspend fun loadEvents(packageName: String, type: Type, userId: Int): List<InfoEvent>

    @Query("SELECT * FROM info_event WHERE name_event = :name AND type_event = :type AND userId_event = :userId")
    suspend fun loadEvent(name: String, type: Type, userId: Int = 0): InfoEvent?

    @Query("SELECT * FROM info_event WHERE packageName_event = :packageName AND type_event = :type AND startTime >= :startTime AND startTime <= :endTime AND userId_event = :userId")
    suspend fun loadEventsInTimeRange(
        packageName: String,
        type: Type,
        startTime: Long,
        endTime: Long,
        userId: Int = 0
    ): List<InfoEvent>

    @Query("SELECT * FROM info_event WHERE packageName_event = :packageName AND startTime >= :startTime AND startTime <= :endTime AND userId_event = :userId")
    suspend fun loadEventsInTimeRange(
        packageName: String,
        startTime: Long,
        endTime: Long,
        userId: Int = 0
    ): List<InfoEvent>

    @Query("SELECT * FROM info_event WHERE eventKey = :eventKey")
    suspend fun loadEventByKey(eventKey: String): InfoEvent?

    @Query("DELETE FROM info_event WHERE startTime < :timestamp")
    suspend fun deleteEventsOlderThan(timestamp: Long)

    @Query("DELETE FROM info_event")
    suspend fun clearAll()

    /**
     * Gets recent events for a device automation item, starting from a specific time
     * @param name The name of the item
     * @param type The type of the item
     * @param userId The user ID
     * @param startTime The start time in milliseconds
     * @param limit Maximum number of events to return
     * @return Flow of list of InfoEvent sorted by start time (descending)
     */
    @Query(
        "SELECT * FROM info_event WHERE name_event = :name AND type_event = :type " +
                "AND userId_event = :userId AND startTime >= :startTime ORDER BY startTime DESC LIMIT :limit"
    )
    fun getRecentEvents(
        name: String,
        type: Type,
        userId: Int,
        startTime: Long,
        limit: Int
    ): Flow<List<InfoEvent>>

    /**
     * Gets events in a time range for a device automation item
     * @param name The name of the item
     * @param type The type of the item
     * @param userId The user ID
     * @param startTime The start time in milliseconds
     * @return Flow of list of InfoEvent within the specified time range
     */
    @Query(
        "SELECT * FROM info_event WHERE name_event = :name AND type_event = :type " +
                "AND userId_event = :userId AND startTime >= :startTime"
    )
    fun getEventsInTimeRange(
        name: String,
        type: Type,
        userId: Int,
        startTime: Long
    ): Flow<List<InfoEvent>>
} 