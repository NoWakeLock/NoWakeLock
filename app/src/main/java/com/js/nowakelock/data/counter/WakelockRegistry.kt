package com.js.nowakelock.data.counter

import android.util.Log
import com.js.nowakelock.data.db.Type
import java.util.concurrent.ConcurrentHashMap

/**
 * WakelockRegistry manages all wakelock counters to handle overlapping duration calculations.
 *
 * This is a singleton class that tracks the active state of all wakelocks and calculates
 * accurate non-overlapping duration times.
 */
class WakelockRegistry private constructor() {
    /**
     * Map of wakelock identifiers to their respective counters
     * The key is a combination of name, packageName, type, and userId
     */
    private val counters = ConcurrentHashMap<String, WakelockCounter>()

    companion object {
        private const val TAG = "WakelockRegistry"
        
        @Volatile
        private var instance: WakelockRegistry? = null
        
        /**
         * Gets the singleton instance of WakelockRegistry
         */
        fun getInstance(): WakelockRegistry {
            return instance ?: synchronized(this) {
                instance ?: WakelockRegistry().also { instance = it }
            }
        }
        
        /**
         * Generates a unique key for a wakelock based on its identifying attributes
         */
        fun generateKey(name: String, packageName: String, type: Type, userId: Int): String {
            return "$name|$packageName|${type.value}|$userId"
        }
    }
    
    /**
     * Handles a wakelock acquire event and calculates accurate duration
     *
     * @param name Wakelock name
     * @param packageName Package name
     * @param type Event type (should be Wakelock)
     * @param userId User ID
     * @param startTime Event start time
     * @return Duration to add to countTime (0 for first activation of this wakelock)
     */
    fun handleAcquire(
        name: String,
        packageName: String,
        type: Type,
        userId: Int,
        startTime: Long
    ): Long {
        if (type != Type.Wakelock) {
            return 0L
        }
        
        try {
            val key = generateKey(name, packageName, type, userId)
            val counter = getOrCreateCounter(key)
            return counter.increment(startTime)
        } catch (e: Exception) {
            Log.e(TAG, "Error handling wakelock acquire: ${e.message}")
            return 0L
        }
    }
    
    /**
     * Handles a wakelock release event and calculates accurate duration
     *
     * @param name Wakelock name
     * @param packageName Package name
     * @param type Event type (should be Wakelock)
     * @param userId User ID
     * @param endTime Event end time
     * @return Duration to add to countTime
     */
    fun handleRelease(
        name: String,
        packageName: String,
        type: Type,
        userId: Int,
        endTime: Long
    ): Long {
        if (type != Type.Wakelock) {
            return 0L
        }
        
        try {
            val key = generateKey(name, packageName, type, userId)
            val counter = counters[key] ?: return 0L
            
            val (duration, remaining) = counter.decrement(endTime)
            return duration
        } catch (e: Exception) {
            Log.e(TAG, "Error handling wakelock release: ${e.message}")
            return 0L
        }
    }
    
    /**
     * Gets an existing counter or creates a new one if it doesn't exist
     *
     * @param key The unique wakelock identifier key
     * @return The wakelock counter instance
     */
    private fun getOrCreateCounter(key: String): WakelockCounter {
        return counters.getOrPut(key) { WakelockCounter() }
    }
    
    /**
     * Clears all counters, typically called on system restart
     */
    fun clearAll() {
        counters.clear()
    }
    
    /**
     * Gets the ongoing duration for a specific wakelock if it's currently active
     *
     * @param name Wakelock name
     * @param packageName Package name
     * @param type Event type (should be Wakelock)
     * @param userId User ID
     * @param now Current timestamp
     * @return Ongoing duration or 0 if not active
     */
    fun getOngoingDuration(
        name: String,
        packageName: String,
        type: Type,
        userId: Int,
        now: Long
    ): Long {
        if (type != Type.Wakelock) {
            return 0L
        }
        
        try {
            val key = generateKey(name, packageName, type, userId)
            val counter = counters[key] ?: return 0L
            return counter.getOngoingDuration(now)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting ongoing duration: ${e.message}")
            return 0L
        }
    }
    
    /**
     * Gets statistics about active wakelocks
     *
     * @return Map of wakelock keys to their active counts
     */
    fun getActiveWakelockStats(): Map<String, Int> {
        val result = HashMap<String, Int>()
        try {
            for ((key, counter) in counters) {
                val activeCount = counter.getActiveCount()
                if (activeCount > 0) {
                    result[key] = activeCount
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting active wakelock stats: ${e.message}")
        }
        return result
    }
    
    /**
     * Get total number of tracked wakelocks
     */
    fun getTotalTrackedWakelocks(): Int {
        return counters.size
    }
} 