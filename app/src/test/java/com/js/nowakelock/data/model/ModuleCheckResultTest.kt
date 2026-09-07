package com.js.nowakelock.data.model

import com.js.nowakelock.data.config.ConfigBackendStatus
import com.js.nowakelock.data.db.Type
import org.junit.Assert.assertEquals
import org.junit.Test

class ModuleCheckResultTest {
    @Test
    fun `module inactive is error`() {
        assertEquals(
            CheckStatus.ERROR,
            ModuleCheckResult.determineOverallStatus(
                moduleActive = false,
                hookStatus = allHooks(true),
                configBackendStatus = ConfigBackendStatus(legacyReadable = true)
            )
        )
    }

    @Test
    fun `no readable backend is error`() {
        assertEquals(
            CheckStatus.ERROR,
            ModuleCheckResult.determineOverallStatus(
                moduleActive = true,
                hookStatus = allHooks(true),
                configBackendStatus = ConfigBackendStatus()
            )
        )
    }

    @Test
    fun `missing hook data is warning when module and backend are usable`() {
        assertEquals(
            CheckStatus.WARNING,
            ModuleCheckResult.determineOverallStatus(
                moduleActive = true,
                hookStatus = mapOf(
                    Type.Wakelock to true,
                    Type.Alarm to false,
                    Type.Service to true
                ),
                configBackendStatus = ConfigBackendStatus(remoteReadable = true)
            )
        )
    }

    @Test
    fun `active module readable backend and hook data is normal`() {
        assertEquals(
            CheckStatus.NORMAL,
            ModuleCheckResult.determineOverallStatus(
                moduleActive = true,
                hookStatus = allHooks(true),
                configBackendStatus = ConfigBackendStatus(remoteReadable = true,
                    publishedRevision = 7, observedRevision = 7, requestedRevision = 7, hookObservedRevision = 7)
            )
        )
    }

    private fun allHooks(value: Boolean): Map<Type, Boolean> = mapOf(
        Type.Wakelock to value,
        Type.Alarm to value,
        Type.Service to value
    )
}
