package com.js.nowakelock.data.repository.wakelock

import com.js.nowakelock.data.model.DAItem
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Wakelock data operations
 * Provides methods to fetch and update wakelock information
 */
interface WakelockRepository {
    
    /**
     * Retrieves all wakelocks sorted by name
     */
    fun getWakelocksSortedByName(): Flow<List<DAItem>>
    
    /**
     * Retrieves all wakelocks sorted by count
     */
    fun getWakelocksSortedByCount(): Flow<List<DAItem>>
    
    /**
     * Retrieves all wakelocks sorted by time
     */
    fun getWakelocksSortedByTime(): Flow<List<DAItem>>
    
    /**
     * Updates wakelock settings (block status and time window)
     * @param name Wakelock name
     * @param packageName Package name of the app
     * @param isBlocked Whether the wakelock should be blocked
     * @param timeWindow Time window in seconds (null if completely blocked)
     */
    suspend fun updateWakelockSettings(
        name: String, 
        packageName: String, 
        userId: Int = 0,
        isBlocked: Boolean, 
        timeWindow: Int?
    )
    
    /**
     * Synchronizes wakelock data with the system
     */
    suspend fun syncWakelocks()
} 