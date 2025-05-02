package com.js.nowakelock.data.counter

import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import kotlin.math.max

/**
 * Unit tests for WakelockCounter class
 * Tests all core functionality and edge cases
 */
class WakelockCounterTest {
    
    private lateinit var counter: WakelockCounter
    
    @Before
    fun setUp() {
        counter = WakelockCounter()
    }
    
    /**
     * Test initial counter state - should be 0 count and 0 timestamp
     */
    @Test
    fun initialState_shouldBeZero() {
        assertEquals("Initial active count should be zero", 0, counter.getActiveCount())
        assertEquals("Initial interval start time should be zero", 0L, counter.getIntervalStartTime())
    }
    
    /**
     * Test first increment (0->1) - should return 0 duration
     */
    @Test
    fun increment_firstActivation_shouldReturnZero() {
        val now = 1000L
        val result = counter.increment(now)
        
        assertEquals("First activation should return zero duration", 0L, result)
        assertEquals("Active count should be 1", 1, counter.getActiveCount())
        assertEquals("Interval start time should be updated", now, counter.getIntervalStartTime())
    }
    
    /**
     * Test subsequent increments - should return non-zero duration
     */
    @Test
    fun increment_subsequentActivation_shouldReturnDuration() {
        val time1 = 1000L
        val time2 = 2500L
        
        counter.increment(time1) // First activation
        val result = counter.increment(time2) // Second activation
        
        assertEquals("Subsequent activation should return duration since last change", 1500L, result)
        assertEquals("Active count should be 2", 2, counter.getActiveCount())
        assertEquals("Interval start time should be updated", time2, counter.getIntervalStartTime())
    }
    
    /**
     * Test decrement with positive remaining count
     */
    @Test
    fun decrement_withPositiveRemaining_shouldReturnDurationAndCount() {
        val time1 = 1000L
        val time2 = 2000L
        val time3 = 3500L
        
        counter.increment(time1)
        counter.increment(time2)
        val (duration, remaining) = counter.decrement(time3)
        
        assertEquals("Decrement should return duration since last change", 1500L, duration)
        assertEquals("Remaining count should be 1", 1, remaining)
        assertEquals("Active count should be updated", 1, counter.getActiveCount())
        assertEquals("Interval start time should be updated", time3, counter.getIntervalStartTime())
    }
    
    /**
     * Test decrement to zero
     */
    @Test
    fun decrement_toZero_shouldReturnCorrectValues() {
        val time1 = 1000L
        val time2 = 2500L
        
        counter.increment(time1)
        val (duration, remaining) = counter.decrement(time2)
        
        assertEquals("Decrement to zero should return correct duration", 1500L, duration)
        assertEquals("Remaining count should be 0", 0, remaining)
        assertEquals("Active count should be zero", 0, counter.getActiveCount())
    }
    
    /**
     * Test decrement below zero (invalid state)
     */
    @Test
    fun decrement_belowZero_shouldHandleGracefully() {
        val time = 1000L
        val (duration, remaining) = counter.decrement(time)
        
        assertEquals("Invalid decrement should return zero duration", 0L, duration)
        assertEquals("Remaining count should be 0", 0, remaining)
        assertEquals("Active count should be zero", 0, counter.getActiveCount())
    }
    
    /**
     * Test negative time difference handling
     */
    @Test
    fun timeOperations_withNegativeTimeDifference_shouldReturnZero() {
        val time1 = 2000L
        val time2 = 1000L // Earlier than time1
        
        counter.increment(time1)
        val result = counter.increment(time2)
        
        assertEquals("Negative time difference should return zero duration", 0L, result)
        assertEquals("Interval start time should be updated even with earlier time", time2, counter.getIntervalStartTime())
    }
    
    /**
     * Test getOngoingDuration with active counter
     */
    @Test
    fun getOngoingDuration_withActiveCounter_shouldReturnCorrectDuration() {
        val startTime = 1000L
        val currentTime = 3000L
        
        counter.increment(startTime)
        val duration = counter.getOngoingDuration(currentTime)
        
        assertEquals("Ongoing duration should be calculated correctly", 2000L, duration)
    }
    
    /**
     * Test getOngoingDuration with inactive counter
     */
    @Test
    fun getOngoingDuration_withInactiveCounter_shouldReturnZero() {
        val currentTime = 1000L
        val duration = counter.getOngoingDuration(currentTime)
        
        assertEquals("Inactive counter should return zero ongoing duration", 0L, duration)
    }
    
    /**
     * Test multiple increments and decrements sequence
     */
    @Test
    fun multipleOperations_shouldMaintainCorrectState() {
        val times = listOf(1000L, 2000L, 3000L, 4000L, 5000L)
        
        // First activation
        counter.increment(times[0])
        assertEquals(1, counter.getActiveCount())
        
        // Second activation
        val duration1 = counter.increment(times[1])
        assertEquals(1000L, duration1)
        assertEquals(2, counter.getActiveCount())
        
        // First deactivation
        val (duration2, remaining1) = counter.decrement(times[2])
        assertEquals(1000L, duration2)
        assertEquals(1, remaining1)
        
        // Third activation
        val duration3 = counter.increment(times[3])
        assertEquals(1000L, duration3)
        assertEquals(2, counter.getActiveCount())
        
        // Second deactivation
        val (duration4, remaining2) = counter.decrement(times[4])
        assertEquals(1000L, duration4)
        assertEquals(1, remaining2)
    }
} 