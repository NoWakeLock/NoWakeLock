package com.js.nowakelock.data.repository.daDetail

import com.js.nowakelock.data.db.Type
import com.js.nowakelock.data.db.dao.DADao
import com.js.nowakelock.data.db.dao.InfoEventDao
import com.js.nowakelock.data.db.entity.InfoEvent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.*

class RecentEventDurationTest {
    private suspend fun display(event: InfoEvent): com.js.nowakelock.data.model.EventItem {
        val dao = mock<InfoEventDao>()
        whenever(dao.getRecentEvents(eq("entry"), eq(event.type), eq(0), any(), eq(20)))
            .thenReturn(flowOf(listOf(event)))
        return DADetailRepositoryImpl(mock<DADao>(), dao)
            .getRecentEvents("entry", event.type, 0, 20).first().single()
    }

    @Test fun `blocked wake and instantaneous events have no holding duration`() = runBlocking {
        val instant = listOf(
            InfoEvent(type = Type.Wakelock, startTime = 1_700_000_000_000, isBlocked = true),
            InfoEvent(type = Type.Alarm, startTime = 1_700_000_000_000),
            InfoEvent(type = Type.Service, startTime = 1_700_000_000_000)
        )
        for (event in instant) assertEquals("${event.type} blocked=${event.isBlocked}", 0L, display(event).duration)
    }

    @Test fun `completed allowed wake keeps measured holding duration`() = runBlocking {
        val item = display(InfoEvent(type = Type.Wakelock,
            startTime = 1_700_000_000_000, endTime = 1_700_000_001_500))
        assertEquals(1500L, item.duration)
        assertEquals("1 s", item.formattedDuration)
    }
}
