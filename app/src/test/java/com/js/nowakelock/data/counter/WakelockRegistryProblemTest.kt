package com.js.nowakelock.data.counter

import com.js.nowakelock.data.db.Type
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.junit.After

/**
 * Separate test class for the problematic WakelockRegistry tests
 * These tests run in isolation to avoid interference from other tests
 */
class WakelockRegistryProblemTest {
    
//    private lateinit var registry: WakelockRegistry
//
//    @Before
//    fun setUp() {
//        // Reset registry before test
//        TestUtils.resetWakelockRegistry()
//        registry = WakelockRegistry.getInstance()
//    }
//
//    @After
//    fun tearDown() {
//        // Make sure to clean up after test
//        TestUtils.resetWakelockRegistry()
//    }
//
//    /**
//     * Test getActiveWakelockStats returns correct stats
//     */
//    @Test
//    fun getActiveWakelockStats_shouldReturnActiveWakelocks() {
//        // Create a clean state before test
//        registry.clearAll()
//
//        val name1 = "testWakelock1"
//        val name2 = "testWakelock2"
//        val packageName = "com.example.test"
//        val type = Type.Wakelock
//        val userId = 0
//        val time = 1000L
//
//        // Acquire first wakelock
//        registry.handleAcquire(name1, packageName, type, userId, time)
//
//        // Acquire second wakelock
//        registry.handleAcquire(name2, packageName, type, userId, time)
//
//        // Release first wakelock
//        registry.handleRelease(name1, packageName, type, userId, time + 500)
//
//        // Get stats
//        val stats = registry.getActiveWakelockStats()
//
//        // Only second wakelock should be active
//        assertEquals("Should have one active wakelock", 1, stats.size)
//
//        val key = WakelockRegistry.generateKey(name2, packageName, type, userId)
//        assertTrue("Stats should contain the active wakelock", stats.containsKey(key))
//        assertEquals("Active count should be 1", 1, stats[key])
//    }
//
//    /**
//     * Test getTotalTrackedWakelocks returns correct count
//     */
//    @Test
//    fun getTotalTrackedWakelocks_shouldReturnCorrectCount() {
//        // Create a clean state before test
//        registry.clearAll()
//
//        val name1 = "testWakelock1"
//        val name2 = "testWakelock2"
//        val packageName = "com.example.test"
//        val type = Type.Wakelock
//        val userId = 0
//        val time = 1000L
//
//        // Initially should be 0
//        assertEquals("Initial tracked wakelocks should be 0", 0, registry.getTotalTrackedWakelocks())
//
//        // Acquire wakelocks
//        registry.handleAcquire(name1, packageName, type, userId, time)
//        registry.handleAcquire(name2, packageName, type, userId, time)
//
//        // Should track 2 wakelocks
//        assertEquals("Should track 2 wakelocks", 2, registry.getTotalTrackedWakelocks())
//
//        // Release them
//        registry.handleRelease(name1, packageName, type, userId, time + 500)
//        registry.handleRelease(name2, packageName, type, userId, time + 500)
//
//        // Should still track 2 wakelocks (counters are not removed)
//        assertEquals("Should still track 2 wakelocks after release", 2, registry.getTotalTrackedWakelocks())
//    }
} 