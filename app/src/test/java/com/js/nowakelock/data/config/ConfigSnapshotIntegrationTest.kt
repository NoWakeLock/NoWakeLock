package com.js.nowakelock.data.config

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.js.nowakelock.BasicApp
import com.js.nowakelock.data.db.Type
import com.js.nowakelock.data.db.entity.St
import com.js.nowakelock.xposedhook.model.SharedPreferencesHookConfigReader
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class ConfigSnapshotIntegrationTest {
    private lateinit var context: Context
    @Before fun setup() {
        context = ApplicationProvider.getApplicationContext()
        BasicApp.context = context
        context.getSharedPreferences("snapshot-test", 0).edit().clear().commit()
        context.getSharedPreferences("nwl_config_state", 0).edit().clear().commit()
    }

    @Test fun `snapshot removes stale rules preserves unrelated data and publishes revision`() {
        val prefs = context.getSharedPreferences("snapshot-test", 0)
        val oldKey = XposedConfigKeys.flag("old", "example.app", Type.Wakelock, 0)
        prefs.edit().putBoolean(oldKey, true).putString("unrelated", "keep").commit()
        val backend = SharedPreferencesConfigBackend("remote", prefs)
        val rule = St(name = "new", packageName = "example.app", type = Type.Wakelock, fullBlock = true)
        assertTrue(backend.publish(ConfigSnapshotWriter.snapshot(listOf(rule), emptyList(), true, 42)))
        assertFalse(prefs.contains(oldKey))
        assertEquals("keep", prefs.getString("unrelated", null))
        assertTrue(prefs.getBoolean(XposedConfigKeys.flag(rule), false))
        val reader = SharedPreferencesHookConfigReader(prefs, "test", "102")
        assertTrue(reader.isReadable)
        assertEquals(42L, reader.status().observedRevision)
    }

    @Test fun `empty remote preferences do not prove a published snapshot exists`() {
        val reader = SharedPreferencesHookConfigReader(context.getSharedPreferences("snapshot-test", 0), null, null)
        assertFalse(reader.isReadable)
        assertEquals(0L, reader.status().observedRevision)
    }

    @Test fun `debug survives recreated application state without legacy backend`() {
        assertTrue(ConfigLocalState(context).saveDebug(true))
        val recreated = ConfigLocalState(context)
        assertTrue(recreated.debug())
        val first = recreated.nextRevision()
        assertTrue(ConfigLocalState(context).nextRevision() > first)
        assertEquals(0L, recreated.publishedRevision())
    }
}
