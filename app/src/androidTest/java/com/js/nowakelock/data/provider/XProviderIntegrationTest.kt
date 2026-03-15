package com.js.nowakelock.data.provider

import android.os.Bundle
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import com.js.nowakelock.data.db.Type
import com.js.nowakelock.data.db.entity.Info
import com.js.nowakelock.data.db.entity.InfoEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class XProviderIntegrationTest {

    private data class TestEvent(
        val packageName: String,
        val name: String,
        val instanceId: String,
        val startTime: Long
    )

    private lateinit var provider: XProvider

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        provider = XProvider.getInstance(context)
        clearAll()
    }

    private fun clearAll() {
        provider.getMethod(
            ProviderMethod.ClearData.value,
            Bundle().apply { putBoolean("clearAll", true) }
        )
    }

    private fun insertTestWakelockEvent(): TestEvent {
        val now = System.currentTimeMillis()
        val packageName = "com.example.test"
        val eventName = "TEST_WAKELOCK"
        val instanceId = InfoEvent.generateInstanceId("test", now)

        provider.getMethod(
            ProviderMethod.NewEvent.value,
            Bundle().apply {
                putString("name", eventName)
                putString("type", Type.Wakelock.value)
                putString("packageName", packageName)
                putInt("userId", 0)
                putLong("startTime", now)
                putBoolean("isBlocked", false)
                putString("instanceId", instanceId)
            }
        )

        return TestEvent(
            packageName = packageName,
            name = eventName,
            instanceId = instanceId,
            startTime = now
        )
    }

    private fun Bundle.requireInfos(): List<Info> {
        @Suppress("UNCHECKED_CAST")
        return (getSerializable("infos") as Array<Info>).toList()
    }

    private fun Bundle.requireEvents(): List<InfoEvent> {
        @Suppress("UNCHECKED_CAST")
        return (getSerializable("events") as Array<InfoEvent>).toList()
    }

    @Test
    fun checkHookActive_returnsActiveAndVersion() {
        val result = provider.getMethod(ProviderMethod.CheckHookActive.value, Bundle())
        assertNotNull(result)

        val bundle = result!!
        assertTrue(bundle.getBoolean("active"))
        assertFalse(bundle.getString("version").isNullOrBlank())
    }

    @Test
    fun newEvent_thenLoadInfosAndEvents_containsInsertedRows() {
        val event = insertTestWakelockEvent()

        val infos = provider.getMethod(
            ProviderMethod.LoadInfos.value,
            Bundle().apply {
                putString("type", Type.Wakelock.value)
                putString("packageName", event.packageName)
                putInt("userId", 0)
            }
        )!!.requireInfos()
        assertTrue(infos.any { it.name == event.name && it.type == Type.Wakelock })

        val events = provider.getMethod(
            ProviderMethod.LoadEvents.value,
            Bundle().apply {
                putString("type", Type.Wakelock.value)
                putString("packageName", event.packageName)
                putInt("userId", 0)
            }
        )!!.requireEvents()
        assertEquals(1, events.size)
        assertEquals(event.instanceId, events.first().instanceId)
        assertEquals(event.name, events.first().name)
        assertEquals(Type.Wakelock, events.first().type)
    }

    @Test
    fun clearData_removesInsertedRows() {
        val event = insertTestWakelockEvent()

        clearAll()

        val infos = provider.getMethod(
            ProviderMethod.LoadInfos.value,
            Bundle().apply {
                putString("type", Type.Wakelock.value)
                putString("packageName", event.packageName)
                putInt("userId", 0)
            }
        )!!.requireInfos()
        assertTrue(infos.isEmpty())

        val events = provider.getMethod(
            ProviderMethod.LoadEvents.value,
            Bundle().apply {
                putString("type", Type.Wakelock.value)
                putString("packageName", event.packageName)
                putInt("userId", 0)
            }
        )!!.requireEvents()
        assertTrue(events.isEmpty())
    }
}
