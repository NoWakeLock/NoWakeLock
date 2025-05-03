package com.js.nowakelock.data.counter

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.max

/**
 * WakelockCounter tracks the active instances of a specific wakelock and calculates non-overlapping duration.
 *
 * This class is thread-safe using AtomicInteger for the counter and a volatile variable for timestamp.
 */
class WakelockCounter {
    /**
     * Number of active wakelock instances with this identifier
     */
    private val activeCount = AtomicInteger(0)

    /**
     * Timestamp of the last counter change or duration update
     * This is used as the reference point for calculating non-overlapping durations
     */
    @Volatile
    private var intervalStartTime: Long = 0
    
    /**
     * Set of tracked instance IDs to prevent double-counting
     */
    private val trackedInstances = ConcurrentHashMap.newKeySet<String>()
    /**
     * Increments the active count and returns duration since the last change if needed
     *
     * @param now Current timestamp in milliseconds
     * @param instanceId Unique identifier for this specific wakelock instance
     * @return Duration in milliseconds to add to countTime (0 if first activation or already counted)
     */
    fun increment(now: Long, instanceId: String): Long {
        // If instance already tracked, just update time and return duration
        if (!trackedInstances.add(instanceId)) {
            val duration = max(0, now - intervalStartTime)
            intervalStartTime = now
            return duration
        }
        
        val prevCount = activeCount.getAndIncrement()
        val duration = if (prevCount == 0) {
            // First activation (0 -> 1), no duration to add
            0L
        } else {
            // Already active, calculate duration since last change
            max(0, now - intervalStartTime)
        }
        
        // Update the interval start time for future calculations
        intervalStartTime = now
        return duration
    }

    /**
     * Decrements the active count and returns the duration since the last change
     *
     * @param now Current timestamp in milliseconds
     * @param instanceId Unique identifier for this specific wakelock instance
     * @return Pair of (duration, remaining count)
     */
    fun decrement(now: Long, instanceId: String): Pair<Long, Int> {
        // Check if this instance is being tracked
        if (!trackedInstances.remove(instanceId)) {
            // Not tracked, can't decrement
            return Pair(0, activeCount.get())
        }
        
        // Calculate duration since last update
        val duration = max(0, now - intervalStartTime)

        // Update timestamp and decrement counter
        intervalStartTime = now
        val remaining = activeCount.decrementAndGet()

        // Handle invalid state (negative count)
        if (remaining < 0) {
            activeCount.set(0)
            return Pair(0, 0)
        }

        return Pair(duration, remaining)
    }

    /**
     * Gets the current active count
     */
    fun getActiveCount(): Int {
        return activeCount.get()
    }

    /**
     * Gets the current interval start time
     */
    fun getIntervalStartTime(): Long {
        return intervalStartTime
    }

    /**
     * Calculates ongoing duration if the counter is active
     *
     * @param now Current timestamp in milliseconds
     * @return Duration from interval start to now if active, 0 otherwise
     */
    fun getOngoingDuration(now: Long): Long {
        return if (activeCount.get() > 0) max(0, now - intervalStartTime) else 0
    }
    
    /**
     * Checks if an instance is currently being tracked
     * 
     * @param instanceId Unique identifier for this specific wakelock instance
     * @return true if the instance is being tracked, false otherwise
     */
    fun isTracked(instanceId: String): Boolean {
        return trackedInstances.contains(instanceId)
    }
} 