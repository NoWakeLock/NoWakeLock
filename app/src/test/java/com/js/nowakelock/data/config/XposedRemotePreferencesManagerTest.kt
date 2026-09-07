package com.js.nowakelock.data.config

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class XposedRemotePreferencesManagerTest {
    @Test
    @Config(sdk = [24])
    fun `factory returns no-op manager before API 26`() {
        val manager = XposedRemotePreferencesManagers.create()
        var callbackCalled = false

        manager.register { callbackCalled = true }

        val status = manager.status(legacyReadable = true)
        assertFalse(callbackCalled)
        assertNull(manager.getRemotePreferences())
        assertTrue(status.legacyReadable)
        assertFalse(status.remoteReadable)
        assertEquals(ConfigBackendStatus.BACKEND_LEGACY, status.activeBackend)
    }
}
