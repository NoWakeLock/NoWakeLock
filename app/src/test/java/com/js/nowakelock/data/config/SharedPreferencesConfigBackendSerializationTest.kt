package com.js.nowakelock.data.config

import android.content.SharedPreferences
import com.js.nowakelock.data.db.entity.AppSt
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.ObjectStreamClass

class SharedPreferencesConfigBackendSerializationTest {
    @Test fun `snapshot with empty regex groups can be read outside module classloader`() {
        val wire = FrameworkWire()
        val app = AppSt(packageName = "com.tencent.mm", userId = 10,
            rE_Alarm = setOf("ALARM_ACTION\\([0-9]+\\)"))

        assertTrue(wire.backend.publish(ConfigSnapshotWriter.snapshot(emptyList(), listOf(app), false, 12)))

        assertEquals(emptySet<String>(), wire.received["Wakelock_com.tencent.mm_10_rE"])
        assertEquals(app.rE_Alarm, wire.received["Alarm_com.tencent.mm_10_rE"])
        assertEquals(12L, wire.received[XposedConfigKeys.REVISION])
    }

    @Test fun `individual empty and populated sets can be read outside module classloader`() {
        for (values in listOf(emptySet(), setOf("wake.*"), setOf("wake.*", "alarm.*"))) {
            val wire = FrameworkWire()
            assertTrue(wire.backend.putStringSet("Wakelock_app_10_rE", values))
            assertEquals(values, wire.received["Wakelock_app_10_rE"])
        }
    }

    // libxposed service 102 serializes the supplied sets inside a java.util.HashMap.
    // The receiving framework cannot resolve the module's Kotlin or obfuscated classes.
    private class FrameworkWire {
        val received = hashMapOf<String, Any>()
        private val pending = hashMapOf<String, Any>()
        private val preferences = mock<SharedPreferences>()
        private val editor = mock<SharedPreferences.Editor>()
        val backend = SharedPreferencesConfigBackend("remote", preferences)

        init {
            whenever(preferences.all).thenReturn(emptyMap())
            whenever(preferences.edit()).thenReturn(editor)
            whenever(editor.putStringSet(any(), any())).thenAnswer {
                pending[it.getArgument(0)] = it.getArgument<Set<String>>(1)
                editor
            }
            whenever(editor.putBoolean(any(), any())).thenAnswer {
                pending[it.getArgument(0)] = it.getArgument<Boolean>(1)
                editor
            }
            whenever(editor.putLong(any(), any())).thenAnswer {
                pending[it.getArgument(0)] = it.getArgument<Long>(1)
                editor
            }
            whenever(editor.commit()).thenAnswer {
                val bytes = ByteArrayOutputStream().also { out ->
                    ObjectOutputStream(out).use { it.writeObject(pending) }
                }.toByteArray()
                val decoded = object : ObjectInputStream(ByteArrayInputStream(bytes)) {
                    override fun resolveClass(desc: ObjectStreamClass): Class<*> =
                        Class.forName(desc.name, false, ClassLoader.getSystemClassLoader().parent)
                }.use { it.readObject() }
                @Suppress("UNCHECKED_CAST")
                received.putAll(decoded as Map<String, Any>)
                true
            }
        }
    }
}
