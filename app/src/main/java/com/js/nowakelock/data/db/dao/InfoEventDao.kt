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
    suspend fun loadEventsInTimeRange(packageName: String, type: Type, startTime: Long, endTime: Long, userId: Int = 0): List<InfoEvent>
    
    @Query("SELECT * FROM info_event WHERE packageName_event = :packageName AND startTime >= :startTime AND startTime <= :endTime AND userId_event = :userId")
    suspend fun loadEventsInTimeRange(packageName: String, startTime: Long, endTime: Long, userId: Int = 0): List<InfoEvent>
    
    @Query("SELECT * FROM info_event WHERE eventKey = :eventKey")
    suspend fun loadEventByKey(eventKey: String): InfoEvent?
    
    @Query("DELETE FROM info_event WHERE startTime < :timestamp")
    suspend fun deleteEventsOlderThan(timestamp: Long)
    
    @Query("DELETE FROM info_event")
    suspend fun clearAll()
} 