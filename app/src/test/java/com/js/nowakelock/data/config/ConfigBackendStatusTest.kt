package com.js.nowakelock.data.config

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ConfigBackendStatusTest {
    @Test
    fun `active backend names reflect readable backends`() {
        assertEquals(ConfigBackendStatus.BACKEND_NONE, ConfigBackendStatus.activeBackendName(false, false))
        assertEquals(ConfigBackendStatus.BACKEND_LEGACY, ConfigBackendStatus.activeBackendName(true, false))
        assertEquals(ConfigBackendStatus.BACKEND_REMOTE, ConfigBackendStatus.activeBackendName(false, true))
        assertEquals(ConfigBackendStatus.BACKEND_DUAL, ConfigBackendStatus.activeBackendName(true, true))
    }

    @Test
    fun `bundle round trip keeps backend status`() {
        val original = ConfigBackendStatus(
            legacyReadable = true,
            remoteReadable = true,
            activeBackend = ConfigBackendStatus.BACKEND_DUAL,
            frameworkName = "Vector",
            frameworkVersion = "2.0",
            lastError = "none"
        )

        val restored = ConfigBackendStatus.fromBundle(original.toBundle())

        assertTrue(restored.backendAvailable)
        assertTrue(restored.legacyReadable)
        assertTrue(restored.remoteReadable)
        assertEquals(ConfigBackendStatus.BACKEND_DUAL, restored.activeBackend)
        assertEquals("Vector", restored.frameworkName)
        assertEquals("2.0", restored.frameworkVersion)
        assertEquals("none", restored.lastError)
    }

    @Test
    fun `empty status is unavailable`() {
        assertFalse(ConfigBackendStatus().backendAvailable)
    }
}
