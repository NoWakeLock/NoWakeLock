package com.js.nowakelock.xposedhook

import android.content.SharedPreferences
import com.js.nowakelock.xposedhook.model.SharedPreferencesHookConfigReader
import org.junit.Assert.*
import org.junit.Test
import org.mockito.kotlin.*
import java.util.concurrent.atomic.AtomicReference

class AtomicRuleSnapshotTest {
    @Test fun `captured decision stays coherent while newer generations are published`() {
        val prefs = mock<SharedPreferences>()
        whenever(prefs.all).thenReturn(emptyMap())
        val reader = SharedPreferencesHookConfigReader(prefs, "Vector", "2.2")
        reader.accept(mapOf("__nwl_revision" to 1L, "debug" to true))
        val old = reader.capture()
        val failure = AtomicReference<Throwable?>()
        val writer = Thread {
            try { for (i in 2L..3000L) reader.accept(mapOf("__nwl_revision" to i, "debug" to (i % 2 == 1L))) }
            catch (e: Throwable) { failure.set(e) }
        }
        writer.start()
        repeat(10000) {
            val captured = reader.capture()
            assertEquals(captured.getLong("__nwl_revision") % 2 == 1L, captured.getBoolean("debug"))
        }
        writer.join()
        failure.get()?.let { throw it }
        assertEquals(1L, old.getLong("__nwl_revision"))
        assertTrue(old.getBoolean("debug"))
        assertEquals(3000L, reader.status().observedRevision)
    }
    @Test fun `stale and conflicting snapshots cannot replace an applied generation`() {
        val prefs = mock<SharedPreferences>()
        whenever(prefs.all).thenReturn(emptyMap())
        val reader = SharedPreferencesHookConfigReader(prefs, null, null)
        val values = mapOf("__nwl_revision" to 4L, "debug" to true)
        reader.accept(values)
        reader.accept(values)
        assertThrows(IllegalArgumentException::class.java) { reader.accept(values + ("__nwl_revision" to 3L)) }
        assertThrows(IllegalArgumentException::class.java) { reader.accept(values + ("debug" to false)) }
        assertTrue(reader.getBoolean("debug"))
    }
    @Test fun `published collections do not change when the sender mutates its objects`() {
        val prefs = mock<SharedPreferences>()
        whenever(prefs.all).thenReturn(emptyMap())
        val reader = SharedPreferencesHookConfigReader(prefs, null, null)
        val names = mutableSetOf("wake.*")
        reader.accept(mapOf("__nwl_revision" to 1L, "Wakelock_app_0_rE" to names))
        names.clear()
        assertTrue(reader.capture().matches("Wakelock_app_0_rE", "wakeABC"))
    }
}
