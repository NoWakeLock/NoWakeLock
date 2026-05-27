package com.js.nowakelock.data.model

import com.js.nowakelock.data.db.Type

/**
 * Represents the result of a module check operation
 */
data class ModuleCheckResult(
    val moduleActive: Boolean,
    val moduleVersion: String?,
    val hookStatus: Map<Type, Boolean>, // Status of each hook type (Wakelock, Alarm, Service)
    val configPathValid: Boolean,
    val shizukuActive: Boolean = false,
    val overallStatus: CheckStatus // Overall status determined from component statuses
) {
    val isShizukuMode: Boolean get() = shizukuActive && !moduleActive

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
                shizukuActive = false,
                overallStatus = CheckStatus.ERROR
            )
        }
    }
}

/**
 * Represents the overall status of module checks
 */
enum class CheckStatus {
    NORMAL,   // All checks passed
    WARNING,  // Some hooks not working but module is active
    SHIZUKU,  // Shizuku Monitor Mode is active (no Xposed)
    ERROR     // Module not active or config path invalid
} 