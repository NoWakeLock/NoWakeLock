package com.js.nowakelock.data.counter

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class WakelockCounterTest {
    private lateinit var counter: WakelockCounter

    @Before
    fun setUp() {
        counter = WakelockCounter()
    }

    @Test
    fun initialState_isZeroAndUntracked() {
        assertEquals(0, counter.getActiveCount())
        assertEquals(0L, counter.getIntervalStartTime())
        assertFalse(counter.isTracked("i1"))
    }

    @Test
    fun increment_firstInstance_returnsZeroAndTracks() {
        val duration = counter.increment(1000L, "i1")

        assertEquals(0L, duration)
        assertEquals(1, counter.getActiveCount())
        assertEquals(1000L, counter.getIntervalStartTime())
        assertTrue(counter.isTracked("i1"))
    }

    @Test
    fun increment_sameInstance_doesNotIncreaseActiveCount() {
        counter.increment(1000L, "i1")
        val duration = counter.increment(1500L, "i1")

        assertEquals(500L, duration)
        assertEquals(1, counter.getActiveCount())
        assertEquals(1500L, counter.getIntervalStartTime())
    }

    @Test
    fun acquireTwoInstances_thenRelease_updatesCountAndDurations() {
        counter.increment(1000L, "i1")
        assertEquals(500L, counter.increment(1500L, "i2"))

        val (d1, remaining1) = counter.decrement(2000L, "i1")
        assertEquals(500L, d1)
        assertEquals(1, remaining1)

        val (d2, remaining2) = counter.decrement(2300L, "i2")
        assertEquals(300L, d2)
        assertEquals(0, remaining2)

        assertEquals(0, counter.getActiveCount())
        assertFalse(counter.isTracked("i1"))
        assertFalse(counter.isTracked("i2"))
    }

    @Test
    fun decrement_untrackedInstance_isNoOp() {
        val (duration, remaining) = counter.decrement(1000L, "missing")

        assertEquals(0L, duration)
        assertEquals(0, remaining)
        assertEquals(0, counter.getActiveCount())
    }
}
