package com.js.nowakelock.data.config

import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import io.github.libxposed.service.XposedService
import io.github.libxposed.service.XposedServiceHelper
import java.util.concurrent.atomic.AtomicBoolean

interface XposedRemotePreferencesManager {
    fun supportsPush(): Boolean = false
    fun register(onRemoteAvailable: () -> Unit)
    fun getRemotePreferences(): SharedPreferences?
    fun status(legacyReadable: Boolean): ConfigBackendStatus
    fun diagnostics(): String? = null
}

object XposedRemotePreferencesManagers {
    private val modern by lazy { LibXposedRemotePreferencesManager() }
    fun create(): XposedRemotePreferencesManager {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            modern
        } else {
            NoOpXposedRemotePreferencesManager
        }
    }
}

private object NoOpXposedRemotePreferencesManager : XposedRemotePreferencesManager {
    override fun register(onRemoteAvailable: () -> Unit) = Unit
    override fun getRemotePreferences(): SharedPreferences? = null
    override fun status(legacyReadable: Boolean): ConfigBackendStatus {
        return ConfigBackendStatus(
            legacyReadable = legacyReadable,
            activeBackend = ConfigBackendStatus.activeBackendName(legacyReadable, false)
        )
    }
}

private class LibXposedRemotePreferencesManager : XposedRemotePreferencesManager {
    override fun supportsPush(): Boolean = try { service?.apiVersion == 102 } catch (_: Exception) { false }
    @Volatile
    private var service: XposedService? = null

    @Volatile
    private var frameworkName: String? = null

    @Volatile
    private var frameworkVersion: String? = null

    @Volatile
    private var lastError: String? = null

    private val registered = AtomicBoolean(false)

    override fun register(onRemoteAvailable: () -> Unit) {
        if (!registered.compareAndSet(false, true)) return

        XposedServiceHelper.registerListener(object : XposedServiceHelper.OnServiceListener {
            override fun onServiceBind(service: XposedService) {
                this@LibXposedRemotePreferencesManager.service = service
                refreshFrameworkInfo(service)
                onRemoteAvailable()
            }

            override fun onServiceDied(service: XposedService) {
                if (this@LibXposedRemotePreferencesManager.service == service) {
                    this@LibXposedRemotePreferencesManager.service = null
                    lastError = "Xposed service died"
                }
            }
        })
    }

    override fun diagnostics(): String {
        val current = service ?: return "Framework service: disconnected"
        return try {
            if (current.apiVersion < 102) "Running targets: unavailable on this framework"
            else current.runningTargets.joinToString("\n") {
                "${it.processName} pid=${it.pid} uid=${it.uid} ${it.state} version=${it.loadedVersionCode}"
            }.ifEmpty { "Framework reports no running targets" }
        } catch (e: Exception) {
            "Running targets unavailable: ${e.message}"
        }
    }

    override fun getRemotePreferences(): SharedPreferences? {
        val currentService = service ?: return null
        return try {
            val apiVersion = currentService.apiVersion
            val fixedOld = apiVersion == 100 && currentService.frameworkName == "LSPosed" &&
                currentService.frameworkVersion.startsWith("1.11.0")
            if (apiVersion != API_102 && !fixedOld) {
                lastError = "Unsupported Xposed service API $apiVersion"
                null
            } else if (!fixedOld && currentService.frameworkProperties and XposedService.PROP_CAP_REMOTE == 0L) {
                lastError = "Remote preferences capability is unavailable"
                null
            } else {
                currentService.getRemotePreferences(XposedConfigKeys.GROUP_NAME).also { lastError = null }
            }
        } catch (e: Throwable) {
            lastError = e.message
            Log.e(TAG, "Unable to get remote preferences", e)
            null
        }
    }

    override fun status(legacyReadable: Boolean): ConfigBackendStatus {
        val remoteReadable = getRemotePreferences() != null
        return ConfigBackendStatus(
            legacyReadable = legacyReadable,
            remoteReadable = remoteReadable,
            activeBackend = ConfigBackendStatus.activeBackendName(legacyReadable, remoteReadable),
            frameworkName = frameworkName,
            frameworkVersion = frameworkVersion,
            lastError = lastError
        )
    }

    private fun refreshFrameworkInfo(service: XposedService) {
        try {
            frameworkName = service.frameworkName
            frameworkVersion = service.frameworkVersion
            lastError = null
        } catch (e: Throwable) {
            lastError = e.message
            Log.e(TAG, "Unable to read framework info", e)
        }
    }

    companion object {
        private const val TAG = "XposedRemotePrefs"
        private const val API_102 = 102
    }
}
