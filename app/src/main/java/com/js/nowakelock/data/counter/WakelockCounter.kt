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
     * Increments the active count and returns duration since the last change if needed
     * 
     * @param now Current timestamp in milliseconds
     * @return Duration in milliseconds to add to countTime (0 if first activation)
     */
    fun increment(now: Long): Long {
        // First activation (0 -> 1)
        if (activeCount.compareAndSet(0, 1)) {
            intervalStartTime = now
            return 0
        }
        
        // Already active, update duration and increment counter
        val duration = max(0, now - intervalStartTime)
        intervalStartTime = now
        activeCount.incrementAndGet()
        return duration
    }
    
    /**
     * Decrements the active count and returns the duration since the last change
     * 
     * @param now Current timestamp in milliseconds
     * @return Pair of (duration, remaining count)
     */
    fun decrement(now: Long): Pair<Long, Int> {
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
} 