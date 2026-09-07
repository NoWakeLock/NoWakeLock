package com.js.nowakelock.xposedhook.model

import com.js.nowakelock.data.config.ConfigBackendStatus
import com.js.nowakelock.data.config.XposedConfigKeys
import com.js.nowakelock.data.db.Type
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HookConfigReaderTest {
    @Test
    fun `remote reader uses its own value when key exists`() {
        val reader = FakeHookConfigReader(
            backendName = ConfigBackendStatus.BACKEND_REMOTE,
            booleans = mapOf("flag" to true),
            remoteReadable = true
        )

        assertEquals(true, reader.getBoolean("flag", false))
        assertEquals(ConfigBackendStatus.BACKEND_REMOTE, reader.status().activeBackend)
    }

    @Test
    fun `legacy reader remains available as a separate backend`() {
        val reader = FakeHookConfigReader(
            backendName = ConfigBackendStatus.BACKEND_LEGACY,
            longs = mapOf("ati" to 30L),
            legacyReadable = true
        )

        assertEquals(30L, reader.getLong("ati", 0L))
        assertEquals(ConfigBackendStatus.BACKEND_LEGACY, reader.status().activeBackend)
    }

    @Test
    fun `remote reader keeps empty set when key exists`() {
        val reader = FakeHookConfigReader(
            backendName = ConfigBackendStatus.BACKEND_REMOTE,
            sets = mapOf("regex" to emptySet()),
            remoteReadable = true
        )

        assertTrue(reader.getStringSet("regex").isEmpty())
    }

    @Test
    fun `xpNSP remote reader uses conservative default when key is missing`() {
        XpNSP.installReader(
            FakeHookConfigReader(
                backendName = ConfigBackendStatus.BACKEND_REMOTE,
                remoteReadable = true
            )
        )

        val blocked = XpNSP.getInstance().flag("legacyOnly", "example.app", Type.Wakelock, 0)

        assertFalse(blocked)
        assertEquals(ConfigBackendStatus.BACKEND_REMOTE, XpNSP.getInstance().backendStatus().activeBackend)
    }

    @Test
    fun `xpNSP allow interval uses milliseconds without scaling`() {
        val key = XposedConfigKeys.allowTimeInterval("wake", "example.app", Type.Wakelock, 0)
        XpNSP.installReader(
            FakeHookConfigReader(
                backendName = ConfigBackendStatus.BACKEND_REMOTE,
                longs = mapOf(key to 3000L),
                remoteReadable = true
            )
        )

        assertTrue(XpNSP.getInstance().aTI(4000L, 2000L, "wake", "example.app", Type.Wakelock, 0))
        assertFalse(XpNSP.getInstance().aTI(6001L, 2000L, "wake", "example.app", Type.Wakelock, 0))
    }

    private class FakeHookConfigReader(
        override val backendName: String = ConfigBackendStatus.BACKEND_NONE,
        private val booleans: Map<String, Boolean> = emptyMap(),
        private val longs: Map<String, Long> = emptyMap(),
        private val sets: Map<String, Set<String>> = emptyMap(),
        private val legacyReadable: Boolean = false,
        private val remoteReadable: Boolean = false
    ) : HookConfigReader {
        override val isReadable: Boolean = legacyReadable || remoteReadable

        override fun contains(key: String): Boolean {
            return booleans.containsKey(key) || longs.containsKey(key) || sets.containsKey(key)
        }

        override fun refresh() = Unit

        override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
            return booleans[key] ?: defaultValue
        }

        override fun getLong(key: String, defaultValue: Long): Long {
            return longs[key] ?: defaultValue
        }

        override fun getStringSet(key: String): Set<String> {
            return sets[key] ?: emptySet()
        }

        override fun status(): ConfigBackendStatus {
            return ConfigBackendStatus(
                legacyReadable = legacyReadable,
                remoteReadable = remoteReadable,
                activeBackend = backendName
            )
        }
    }
}
