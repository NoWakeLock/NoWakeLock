package com.js.nowakelock.data.counter

import com.js.nowakelock.data.db.Type
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class WakelockRegistryBasicTest {
    private lateinit var registry: WakelockRegistry

    @Before
    fun setUp() {
        registry = WakelockRegistry.getInstance()
        registry.clearAll()
    }

    @Test
    fun generateKey_shouldCreateExpectedFormat() {
        val key = WakelockRegistry.generateKey("tag", "pkg", Type.Wakelock, 10)
        assertEquals("tag|pkg|Wakelock|10", key)
    }

    @Test
    fun nonWakelockType_isIgnored() {
        val duration = registry.handleAcquire("a", "pkg", Type.Alarm, 0, 1000L, "i1")
        assertEquals(0L, duration)
        assertEquals(0, registry.getTotalTrackedWakelocks())
        assertTrue(registry.getActiveWakelockStats().isEmpty())
    }

    @Test
    fun acquireRelease_updatesStatsAndDurations() {
        val name = "wl"
        val pkg = "pkg"
        val type = Type.Wakelock
        val userId = 0
        val key = WakelockRegistry.generateKey(name, pkg, type, userId)

        assertEquals(0L, registry.handleAcquire(name, pkg, type, userId, 1000L, "i1"))
        assertEquals(1, registry.getActiveWakelockStats()[key])

        assertEquals(500L, registry.handleAcquire(name, pkg, type, userId, 1500L, "i2"))
        assertEquals(2, registry.getActiveWakelockStats()[key])

        assertEquals(500L, registry.handleRelease(name, pkg, type, userId, 2000L, "i1"))
        assertEquals(1, registry.getActiveWakelockStats()[key])

        assertEquals(300L, registry.handleRelease(name, pkg, type, userId, 2300L, "i2"))
        assertTrue(registry.getActiveWakelockStats().isEmpty())
        assertEquals(1, registry.getTotalTrackedWakelocks())
    }

    @Test
    fun clearAll_removesAllCounters() {
        registry.handleAcquire("wl", "pkg", Type.Wakelock, 0, 1000L, "i1")
        assertEquals(1, registry.getTotalTrackedWakelocks())

        registry.clearAll()
        assertEquals(0, registry.getTotalTrackedWakelocks())
        assertTrue(registry.getActiveWakelockStats().isEmpty())
    }
}
