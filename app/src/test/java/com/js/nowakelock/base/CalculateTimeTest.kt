package com.js.nowakelock.base

import com.js.nowakelock.data.db.Type
import com.js.nowakelock.data.db.entity.InfoEvent
import org.junit.Assert.assertEquals
import org.junit.Test

class CalculateTimeTest {
    @Test
    fun empty_returnsZero() {
        assertEquals(0L, calculateTime(emptyList()))
    }

    @Test
    fun nonWakelockType_returnsZero() {
        val events = listOf(
            InfoEvent(type = Type.Alarm, startTime = 0L, endTime = 1000L)
        )
        assertEquals(0L, calculateTime(events))
    }

    @Test
    fun wakelockWithoutEndTime_returnsZero() {
        val events = listOf(
            InfoEvent(type = Type.Wakelock, startTime = 0L, endTime = null)
        )
        assertEquals(0L, calculateTime(events))
    }

    @Test
    fun singleEvent_returnsDuration() {
        val events = listOf(
            InfoEvent(type = Type.Wakelock, startTime = 100L, endTime = 600L)
        )
        assertEquals(500L, calculateTime(events))
    }

    @Test
    fun nonOverlappingEvents_sumDurations() {
        val events = listOf(
            InfoEvent(type = Type.Wakelock, startTime = 0L, endTime = 100L),
            InfoEvent(type = Type.Wakelock, startTime = 200L, endTime = 260L)
        )
        assertEquals(160L, calculateTime(events))
    }

    @Test
    fun overlappingEvents_unionDuration() {
        val events = listOf(
            InfoEvent(type = Type.Wakelock, startTime = 0L, endTime = 100L),
            InfoEvent(type = Type.Wakelock, startTime = 50L, endTime = 120L)
        )
        assertEquals(120L, calculateTime(events))
    }

    @Test
    fun nestedOverlaps_unionDuration() {
        val events = listOf(
            InfoEvent(type = Type.Wakelock, startTime = 0L, endTime = 300L),
            InfoEvent(type = Type.Wakelock, startTime = 50L, endTime = 120L),
            InfoEvent(type = Type.Wakelock, startTime = 150L, endTime = 250L)
        )
        assertEquals(300L, calculateTime(events))
    }
}
