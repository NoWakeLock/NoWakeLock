package com.js.nowakelock.data.provider

import android.content.Context
import android.os.Bundle
import androidx.test.core.app.ApplicationProvider
import com.js.nowakelock.data.counter.WakelockRegistry
import com.js.nowakelock.data.db.InfoDatabase
import com.js.nowakelock.data.db.Type
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class XProviderWakelockDurationTest {
    @Test fun `batch records ordered events and rejects old generation after clear`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val recorder = com.js.nowakelock.xposedhook.model.XpRecord
        val epochBefore = recorder.currentGeneration()
        fun entry(id: String, time: Long, end: Boolean = false) =
            com.js.nowakelock.xposedhook.model.XpRecord.Event(epochBefore, context,
                if (end) "EndEvent" else "NewEvent", Bundle().apply {
                    putString("name", name); putString("packageName", packageName)
                    putString("type", Type.Wakelock.value); putString("instanceId", id)
                    putLong(if (end) "endTime" else "startTime", epoch + time)
                })
        provider.recordBatch(listOf(entry("one", 0), entry("two", 100), entry("one", 150, true), entry("two", 200, true)))
        assertEquals(200L, requireNotNull(db.infoDao().loadInfo(name, Type.Wakelock)).countTime)
        assertEquals(2, requireNotNull(db.infoDao().loadInfo(name, Type.Wakelock)).count)
        provider.getMethod("ClearData", Bundle())
        provider.recordBatch(listOf(entry("stale", 300)))
        assertEquals(0, requireNotNull(db.infoDao().loadInfo(name, Type.Wakelock)).count)
        assertTrue(db.infoEventDao().loadAllEvents().isEmpty())
        provider.recordBatch(listOf(entry("fresh", 400).copy(generation = recorder.currentGeneration())))
        assertEquals(1, requireNotNull(db.infoDao().loadInfo(name, Type.Wakelock)).count)
    }
    private lateinit var provider: XProvider
    private lateinit var db: InfoDatabase
    private val registry = WakelockRegistry.getInstance()
    private val name = "duration-regression"
    private val packageName = "test.app"
    private val epoch = 1_700_000_000_000L

    @Before fun setup() = runBlocking(Dispatchers.IO) {
        registry.clearAll()
        val context = ApplicationProvider.getApplicationContext<Context>()
        provider = XProvider(context)
        db = InfoDatabase.getInstance(context)
    }

    @After fun cleanup() = runBlocking(Dispatchers.IO) {
        if (::db.isInitialized) db.close()
        // Room's singleton must not retain a connection across Robolectric sandboxes.
        InfoDatabase::class.java.getDeclaredField("instance").apply {
            isAccessible = true
            set(null, null)
        }
        registry.clearAll()
    }

    private fun event(id: String, time: Long, blocked: Boolean = false, end: Boolean = false) {
        provider.getMethod(if (end) "EndEvent" else "NewEvent", Bundle().apply {
            putString("name", name)
            putString("packageName", packageName)
            putString("type", Type.Wakelock.value)
            putString("instanceId", id)
            putLong(if (end) "endTime" else "startTime", epoch + time)
            putBoolean("isBlocked", blocked)
        })
    }

    @Test fun `first blocked request then allowed lock counts only actual hold time`() = runBlocking {
        event("blocked", 0, blocked = true)
        event("allowed", 300_000)
        event("allowed", 300_150, end = true)
        val info = requireNotNull(db.infoDao().loadInfo(name, Type.Wakelock))
        assertEquals(1, info.blockCount)
        assertEquals(1, info.count)
        assertEquals(150L, info.countTime)
        assertEquals(0L, registry.getOngoingDuration(name, packageName, Type.Wakelock, 0, epoch + 400_000))
        assertTrue(requireNotNull(db.infoEventDao().loadEventById("blocked")).isBlocked)
    }

    @Test fun `first blocked request never creates an active holding interval`() = runBlocking {
        event("blocked", 0, blocked = true)
        assertEquals(0L, registry.getOngoingDuration(name, packageName, Type.Wakelock, 0, epoch + 300_000))
        assertEquals(0L, requireNotNull(db.infoDao().loadInfo(name, Type.Wakelock)).countTime)
    }

    @Test fun `reset statistics clears blocked counts as well as allowed counts and time`() = runBlocking {
        event("allowed", 0)
        event("allowed", 150, end = true)
        event("blocked", 200, blocked = true)
        provider.getMethod("ClearData", Bundle())
        val info = requireNotNull(db.infoDao().loadInfo(name, Type.Wakelock))
        assertEquals(0, info.count)
        assertEquals(0, info.blockCount)
        assertEquals(0L, info.countTime)
        assertTrue(db.infoEventDao().loadAllEvents().isEmpty())
        event("after-reset", 300)
        event("after-reset", 350, end = true)
        assertEquals(50L, requireNotNull(db.infoDao().loadInfo(name, Type.Wakelock)).countTime)
    }

    @Test fun `blocked request during overlapping allowed locks preserves union duration`() = runBlocking {
        event("one", 0)
        event("blocked", 50, blocked = true)
        event("two", 100)
        event("one", 150, end = true)
        event("two", 200, end = true)
        val info = requireNotNull(db.infoDao().loadInfo(name, Type.Wakelock))
        assertEquals(200L, info.countTime)
        assertEquals(2, info.count)
        assertEquals(1, info.blockCount)
    }

    @Test fun `repeated acquire and release do not duplicate holding duration`() = runBlocking {
        event("one", 0)
        event("one", 50)
        event("one", 150, end = true)
        event("one", 200, end = true)
        assertEquals(150L, requireNotNull(db.infoDao().loadInfo(name, Type.Wakelock)).countTime)
        assertEquals(0L, registry.getOngoingDuration(name, packageName, Type.Wakelock, 0, epoch + 300))
    }
}
