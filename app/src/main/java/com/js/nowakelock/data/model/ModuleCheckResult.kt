package com.js.nowakelock.data.model

import com.js.nowakelock.data.db.Type
import com.js.nowakelock.data.config.ConfigBackendStatus

/**
 * Represents the result of a module check operation
 */
data class ModuleCheckResult(
    val moduleActive: Boolean,
    val moduleVersion: String?,
    val hookStatus: Map<Type, Boolean>, // Status of each hook type (Wakelock, Alarm, Service)
    val configPathValid: Boolean,
    val configBackendStatus: ConfigBackendStatus,
    val overallStatus: CheckStatus // Overall status determined from component statuses
) {
    companion object {
        /**
         * Creates an empty/initial result with all checks failed
         */
        fun createEmpty(): ModuleCheckResult {
            val emptyHookStatus = mapOf(
                Type.Wakelock to false,
                Type.Alarm to false,
                Type.Service to false
            )
            return ModuleCheckResult(
                moduleActive = false,
                moduleVersion = null,
                hookStatus = emptyHookStatus,
                configPathValid = false,
                configBackendStatus = ConfigBackendStatus(),
                overallStatus = CheckStatus.ERROR
            )
        }

        fun determineOverallStatus(
            moduleActive: Boolean,
            hookStatus: Map<Type, Boolean>,
            configBackendStatus: ConfigBackendStatus
        ): CheckStatus {
            return when {
                !moduleActive || !configBackendStatus.backendAvailable -> CheckStatus.ERROR
                configBackendStatus.remoteReadable && !configBackendStatus.synchronizationConfirmed -> CheckStatus.WARNING
                listOf(Type.Wakelock, Type.Alarm, Type.Service).any { hookStatus[it] != true } -> CheckStatus.WARNING
                else -> CheckStatus.NORMAL
            }
        }
    }
}

/**
 * Represents the overall status of module checks
 */
enum class CheckStatus {
    NORMAL,   // All checks passed
    WARNING,  // Some hooks not working but module is active
    ERROR     // Module not active or config path invalid
}
