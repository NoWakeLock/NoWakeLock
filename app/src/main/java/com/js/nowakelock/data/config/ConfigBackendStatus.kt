package com.js.nowakelock.data.config

import android.os.Bundle

data class ConfigBackendStatus(
    val legacyReadable: Boolean = false,
    val remoteReadable: Boolean = false,
    val activeBackend: String = BACKEND_NONE,
    val frameworkName: String? = null,
    val frameworkVersion: String? = null,
    val lastError: String? = null,
    val publishedRevision: Long = 0,
    val requestedRevision: Long = 0,
    val observedRevision: Long = 0,
    val hookObservedRevision: Long = 0,
    val processName: String? = null,
    val diagnostics: String? = null
) {
    val synchronizationConfirmed: Boolean
        get() = publishedRevision > 0 && publishedRevision == observedRevision &&
            requestedRevision <= publishedRevision && hookObservedRevision == publishedRevision
    val backendAvailable: Boolean
        get() = legacyReadable || remoteReadable

    fun toBundle(): Bundle = Bundle().apply {
        putBoolean(KEY_BACKEND_AVAILABLE, backendAvailable)
        putBoolean(KEY_LEGACY_READABLE, legacyReadable)
        putBoolean(KEY_REMOTE_READABLE, remoteReadable)
        putString(KEY_ACTIVE_BACKEND, activeBackend)
        putString(KEY_FRAMEWORK_NAME, frameworkName)
        putString(KEY_FRAMEWORK_VERSION, frameworkVersion)
        putString(KEY_LAST_ERROR, lastError)
        putLong("publishedRevision", publishedRevision)
        putLong("requestedRevision", requestedRevision)
        putLong("observedRevision", observedRevision)
        putLong("hookObservedRevision", hookObservedRevision)
        putString("processName", processName)
        putString("diagnostics", diagnostics)
    }

    companion object {
        const val BACKEND_NONE = "none"
        const val BACKEND_LEGACY = "legacy"
        const val BACKEND_REMOTE = "remote"
        const val BACKEND_DUAL = "dual"

        const val KEY_BACKEND_AVAILABLE = "backendAvailable"
        const val KEY_LEGACY_READABLE = "legacyReadable"
        const val KEY_REMOTE_READABLE = "remoteReadable"
        const val KEY_ACTIVE_BACKEND = "activeBackend"
        const val KEY_FRAMEWORK_NAME = "frameworkName"
        const val KEY_FRAMEWORK_VERSION = "frameworkVersion"
        const val KEY_LAST_ERROR = "lastError"

        fun fromBundle(bundle: Bundle?): ConfigBackendStatus {
            if (bundle == null) return ConfigBackendStatus()
            return ConfigBackendStatus(
                legacyReadable = bundle.getBoolean(KEY_LEGACY_READABLE, false),
                remoteReadable = bundle.getBoolean(KEY_REMOTE_READABLE, false),
                activeBackend = bundle.getString(KEY_ACTIVE_BACKEND) ?: BACKEND_NONE,
                frameworkName = bundle.getString(KEY_FRAMEWORK_NAME),
                frameworkVersion = bundle.getString(KEY_FRAMEWORK_VERSION),
                lastError = bundle.getString(KEY_LAST_ERROR),
                publishedRevision = bundle.getLong("publishedRevision", 0),
                requestedRevision = bundle.getLong("requestedRevision", 0),
                observedRevision = bundle.getLong("observedRevision", 0),
                hookObservedRevision = bundle.getLong("hookObservedRevision", 0),
                processName = bundle.getString("processName"),
                diagnostics = bundle.getString("diagnostics")
            )
        }

        fun activeBackendName(legacyReadable: Boolean, remoteReadable: Boolean): String {
            return when {
                legacyReadable && remoteReadable -> BACKEND_DUAL
                remoteReadable -> BACKEND_REMOTE
                legacyReadable -> BACKEND_LEGACY
                else -> BACKEND_NONE
            }
        }
    }
}
