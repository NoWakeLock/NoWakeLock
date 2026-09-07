package com.js.nowakelock.xposedhook

import android.content.SharedPreferences
import com.js.nowakelock.xposedhook.model.SharedPreferencesHookConfigReader
import org.junit.Assert.*
import org.junit.Test
import org.mockito.kotlin.*

class HookConfigReaderCostTest {
    @Test fun `rule lookups use typed access and never copy entire preference map`() {
        val prefs = mock<SharedPreferences>()
        whenever(prefs.all).thenReturn(mapOf("debug" to true, "__nwl_revision" to 1L))
        val reader = SharedPreferencesHookConfigReader(prefs, null, null)
        clearInvocations(prefs)
        repeat(20) { assertTrue(reader.capture().getBoolean("debug")) }
        verifyNoInteractions(prefs)
    }

    @Test fun `malformed remote value uses conservative default`() {
        val prefs = mock<SharedPreferences>()
        whenever(prefs.getLong("rule", 0)).thenThrow(ClassCastException("bad type"))
        val reader = SharedPreferencesHookConfigReader(prefs, null, null)
        assertEquals(0L, reader.getLong("rule"))
    }
}
