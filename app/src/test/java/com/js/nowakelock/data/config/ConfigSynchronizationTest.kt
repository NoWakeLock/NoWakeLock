package com.js.nowakelock.data.config

import org.junit.Assert.*
import org.junit.Test

class ConfigSynchronizationTest {
    private val current = ConfigBackendStatus(remoteReadable = true, requestedRevision = 8,
        publishedRevision = 8, observedRevision = 8, hookObservedRevision = 8)
    @Test fun `both reader processes must acknowledge latest published data`() {
        assertTrue(current.synchronizationConfirmed)
        assertFalse(current.copy(hookObservedRevision = 0).synchronizationConfirmed)
        assertFalse(current.copy(hookObservedRevision = 7).synchronizationConfirmed)
        assertFalse(current.copy(observedRevision = 7).synchronizationConfirmed)
        assertFalse(current.copy(requestedRevision = 9).synchronizationConfirmed)
    }
}
