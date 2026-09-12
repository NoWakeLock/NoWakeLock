package com.js.nowakelock.data.config

import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import io.github.libxposed.service.XposedService
import io.github.libxposed.service.XposedServiceHelper
import java.util.concurrent.atomic.AtomicBoolean
import io.github.libxposed.service.HookedTarget
import io.github.libxposed.service.HotReloadResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.CancellationException
import kotlin.coroutines.resume

interface XposedRemotePreferencesManager {
    fun supportsPush(): Boolean = false
    fun register(onRemoteAvailable: () -> Unit)
    fun getRemotePreferences(): SharedPreferences?
    fun status(legacyReadable: Boolean): ConfigBackendStatus
    fun diagnostics(): String? = null
    suspend fun reloadCode(retryCurrentTargets: Boolean = false): CodeReloadReport = CodeReloadReport(error = "API 102 hot reload is unavailable")
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
    private val reloadInProgress = AtomicBoolean(false)

    override suspend fun reloadCode(retryCurrentTargets: Boolean): CodeReloadReport = withContext(Dispatchers.IO) {
        if (!reloadInProgress.compareAndSet(false, true)) return@withContext CodeReloadReport(error = "Reload is already in progress")
        try {
            val current = service ?: return@withContext CodeReloadReport(error = "Framework service is disconnected")
            if (current.apiVersion != 102) return@withContext CodeReloadReport(error = "This framework does not support API 102 hot reload")
            val targets = current.runningTargets
            if (targets.isEmpty()) return@withContext CodeReloadReport(error = "Framework reports no running targets")
            val results = targets.map { target ->
                val result = when (target.state) {
                    HookedTarget.State.UP_TO_DATE -> if (retryCurrentTargets) requestReload(current, target) else "ALREADY_CURRENT" to null
                    HookedTarget.State.RELOADING -> "IN_PROGRESS" to null
                    else -> requestReload(current, target)
                }
                CodeReloadTarget(target.processName, target.pid, target.uid, result.first, target.state.name, result.second)
            }
            val refreshed = current.runningTargets
            CodeReloadReport(targets = results.map { result ->
                val after = refreshed.singleOrNull { it.pid == result.pid && it.uid == result.uid && it.processName == result.process }
                result.copy(state = after?.state?.name ?: "TARGET_GONE")
            })
        } catch (e: CancellationException) { throw e }
        catch (e: Exception) { CodeReloadReport(error = e.message ?: e.javaClass.simpleName) }
        finally { reloadInProgress.set(false) }
    }

    private suspend fun requestReload(current: XposedService, target: HookedTarget): Pair<String, String?> {
        return withTimeoutOrNull(15_000) {
            suspendCancellableCoroutine { continuation ->
                val delivered = AtomicBoolean(false)
                try {
                    current.hotReloadModule(target, null) { _, result ->
                        val status = if (result.status() == HotReloadResult.Status.FAILED && result.message() == null)
                            "REFUSED" else result.status().name
                        if (delivered.compareAndSet(false, true) && continuation.isActive)
                            continuation.resume(status to result.message())
                    }
                } catch (e: Exception) {
                    if (delivered.compareAndSet(false, true) && continuation.isActive)
                        continuation.resume("FAILED" to e.message)
                }
            }
        } ?: ("TIMEOUT" to "No completion callback; refresh status before retrying")
    }

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
