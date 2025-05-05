package com.js.nowakelock.data.counter

import android.util.Log
import com.js.nowakelock.data.db.Type
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.junit.After

/**
 * Basic unit tests for WakelockRegistry class
 * Contains the core functionality tests
 */
class WakelockRegistryBasicTest {
    
//    private lateinit var registry: WakelockRegistry
//
//    @Before
//    fun setUp() {
//        // Reset singleton before each test
//        TestUtils.resetWakelockRegistry()
//
//        // Get a clean instance
//        registry = WakelockRegistry.getInstance()
//    }
//
//    @After
//    fun tearDown() {
//        // Ensure registry is reset after each test
//        TestUtils.resetWakelockRegistry()
//    }
//
//    /**
//     * Test getInstance returns the same instance
//     */
//    @Test
//    fun getInstance_multipleCalls_shouldReturnSameInstance() {
//        val instance1 = WakelockRegistry.getInstance()
//        val instance2 = WakelockRegistry.getInstance()
//
//        assertSame("Multiple getInstance calls should return the same instance", instance1, instance2)
//    }
//
//    /**
//     * Test generateKey method creates expected format
//     */
//    @Test
//    fun generateKey_shouldCreateExpectedFormat() {
//        val name = "testWakelock"
//        val packageName = "com.example.test"
//        val type = Type.Wakelock
//        val userId = 10
//
//        val key = WakelockRegistry.generateKey(name, packageName, type, userId)
//        val expected = "$name|$packageName|${type.value}|$userId"
//
//        assertEquals("Generated key should match expected format", expected, key)
//    }
//
//    /**
//     * Test handleAcquire for first acquisition
//     */
//    @Test
//    fun handleAcquire_firstAcquisition_shouldReturnZero() {
//        val name = "testWakelock"
//        val packageName = "com.example.test"
//        val type = Type.Wakelock
//        val userId = 0
//        val time = 1000L
//
//        val duration = registry.handleAcquire(name, packageName, type, userId, time)
//
//        assertEquals("First acquisition should return zero duration", 0L, duration)
//    }
//
//    /**
//     * Test handleAcquire for subsequent acquisitions
//     */
//    @Test
//    fun handleAcquire_subsequentAcquisition_shouldReturnDuration() {
//        val name = "testWakelock"
//        val packageName = "com.example.test"
//        val type = Type.Wakelock
//        val userId = 0
//        val time1 = 1000L
//        val time2 = 3000L
//
//        // First acquisition
//        registry.handleAcquire(name, packageName, type, userId, time1)
//
//        // Subsequent acquisition
//        val duration = registry.handleAcquire(name, packageName, type, userId, time2)
//
//        assertEquals("Subsequent acquisition should return non-zero duration", 2000L, duration)
//    }
//
//    /**
//     * Test handleRelease after acquisition
//     */
//    @Test
//    fun handleRelease_afterAcquisition_shouldReturnDuration() {
//        val name = "testWakelock"
//        val packageName = "com.example.test"
//        val type = Type.Wakelock
//        val userId = 0
//        val time1 = 1000L
//        val time2 = 2500L
//
//        // Acquire wakelock
//        registry.handleAcquire(name, packageName, type, userId, time1)
//
//        // Release wakelock
//        val duration = registry.handleRelease(name, packageName, type, userId, time2)
//
//        assertEquals("Release should return correct duration", 1500L, duration)
//    }
//
//    /**
//     * Test handleRelease for non-existent wakelock
//     */
//    @Test
//    fun handleRelease_nonExistentWakelock_shouldReturnZero() {
//        val name = "testWakelock"
//        val packageName = "com.example.test"
//        val type = Type.Wakelock
//        val userId = 0
//        val time = 1000L
//
//        val duration = registry.handleRelease(name, packageName, type, userId, time)
//
//        assertEquals("Release for non-existent wakelock should return zero", 0L, duration)
//    }
//
//    /**
//     * Test non-wakelock type handling in acquire
//     */
//    @Test
//    fun handleAcquire_nonWakelockType_shouldReturnZero() {
//        val name = "testAlarm"
//        val packageName = "com.example.test"
//        val type = Type.Alarm // Not a wakelock
//        val userId = 0
//        val time = 1000L
//
//        val duration = registry.handleAcquire(name, packageName, type, userId, time)
//
//        assertEquals("Non-wakelock type should return zero duration", 0L, duration)
//    }
//
//    /**
//     * Test non-wakelock type handling in release
//     */
//    @Test
//    fun handleRelease_nonWakelockType_shouldReturnZero() {
//        val name = "testAlarm"
//        val packageName = "com.example.test"
//        val type = Type.Alarm // Not a wakelock
//        val userId = 0
//        val time = 1000L
//
//        val duration = registry.handleRelease(name, packageName, type, userId, time)
//
//        assertEquals("Non-wakelock type should return zero duration", 0L, duration)
//    }
//
//    /**
//     * Test clearAll method
//     */
//    @Test
//    fun clearAll_shouldRemoveAllCounters() {
//        val name = "testWakelock"
//        val packageName = "com.example.test"
//        val type = Type.Wakelock
//        val userId = 0
//        val time = 1000L
//
//        // Add a wakelock
//        registry.handleAcquire(name, packageName, type, userId, time)
//
//        // Clear all
//        registry.clearAll()
//
//        // Check if cleared by trying to get stats
//        val stats = registry.getActiveWakelockStats()
//        assertTrue("Active wakelock stats should be empty after clearAll", stats.isEmpty())
//    }
//
//    /**
//     * Test getOngoingDuration for active wakelock
//     */
//    @Test
//    fun getOngoingDuration_activeWakelock_shouldReturnCorrectDuration() {
//        val name = "testWakelock"
//        val packageName = "com.example.test"
//        val type = Type.Wakelock
//        val userId = 0
//        val time1 = 1000L
//        val time2 = 3000L
//
//        // Acquire wakelock
//        registry.handleAcquire(name, packageName, type, userId, time1)
//
//        // Get ongoing duration
//        val duration = registry.getOngoingDuration(name, packageName, type, userId, time2)
//
//        assertEquals("Should return correct ongoing duration", 2000L, duration)
//    }
//
//    /**
//     * Test getOngoingDuration for inactive wakelock
//     */
//    @Test
//    fun getOngoingDuration_inactiveWakelock_shouldReturnZero() {
//        val name = "testWakelock"
//        val packageName = "com.example.test"
//        val type = Type.Wakelock
//        val userId = 0
//        val time = 1000L
//
//        // No wakelock acquisition, just check duration
//        val duration = registry.getOngoingDuration(name, packageName, type, userId, time)
//
//        assertEquals("Inactive wakelock should return zero ongoing duration", 0L, duration)
//    }
} 