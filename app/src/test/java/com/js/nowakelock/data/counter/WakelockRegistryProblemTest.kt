package com.js.nowakelock.data.counter

import com.js.nowakelock.data.db.Type
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

class WakelockRegistryProblemTest {
    private lateinit var registry: WakelockRegistry

    @Before
    fun setUp() {
        registry = WakelockRegistry.getInstance()
        registry.clearAll()
    }

    @Test
    fun getActiveWakelockStats_onlyReturnsActiveWakelocks() {
        val pkg = "com.example.test"
        val userId = 0

        registry.handleAcquire("wl1", pkg, Type.Wakelock, userId, 1000L, "i1")
        registry.handleAcquire("wl2", pkg, Type.Wakelock, userId, 1000L, "i2")

        registry.handleRelease("wl1", pkg, Type.Wakelock, userId, 1500L, "i1")

        val key1 = WakelockRegistry.generateKey("wl1", pkg, Type.Wakelock, userId)
        val key2 = WakelockRegistry.generateKey("wl2", pkg, Type.Wakelock, userId)

        val stats = registry.getActiveWakelockStats()
        assertFalse(stats.containsKey(key1))
        assertEquals(1, stats[key2])
        assertEquals(2, registry.getTotalTrackedWakelocks())
    }
}
