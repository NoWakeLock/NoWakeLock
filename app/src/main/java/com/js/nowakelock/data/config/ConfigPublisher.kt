package com.js.nowakelock.data.config

import android.util.Log
import com.js.nowakelock.data.db.dao.AppDaDao
import com.js.nowakelock.data.db.dao.DADao
import com.js.nowakelock.data.db.entity.AppSt
import com.js.nowakelock.data.db.entity.St
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.CancellationException
import com.js.nowakelock.BasicApp

class ConfigPublisher(
    private val daDao: DADao,
    private val appDaDao: AppDaDao,
    private val remotePreferencesManager: XposedRemotePreferencesManager,
    private val localState: ConfigLocalState = ConfigLocalState(BasicApp.context),
    private val legacyBackend: XposedConfigBackend = LegacySharedPreferencesBackend()
) {
    private val publishMutex = Mutex()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    private var lastPublishError: String? = null

    fun start() {
        remotePreferencesManager.register {
            publishAllAsync()
        }
        publishAllAsync()
    }

    fun publishAllAsync() {
        scope.launch {
            try {
                publishAll()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                lastPublishError = e.message
                Log.e(TAG, "Unable to publish configuration", e)
            }
        }
    }

    suspend fun publishAll(): Boolean = withContext(Dispatchers.IO) {
        publishMutex.withLock { publishCurrentSafely() }
    }

    // Callers persist to Room first; re-read after taking the publication lock.
    suspend fun publishSt(@Suppress("UNUSED_PARAMETER") st: St): Boolean = publishAll()

    suspend fun publishAppSt(@Suppress("UNUSED_PARAMETER") appSt: AppSt): Boolean = publishAll()

    fun debugEnabled(): Boolean = localState.debug()

    suspend fun publishDebug(enabled: Boolean): Boolean = withContext(Dispatchers.IO) {
        publishMutex.withLock {
            check(localState.saveDebug(enabled)) {
                "Unable to save debug configuration"
            }
            publishCurrentSafely()
        }
    }

    fun backendStatus(): ConfigBackendStatus {
        val status = remotePreferencesManager.status(legacyBackend.isReadable)
        return status.copy(lastError = mergeErrors(lastPublishError, status.lastError),
            publishedRevision = localState.publishedRevision(), requestedRevision = localState.revision())
    }

    private suspend fun publishCurrentSafely(): Boolean = try {
        publishCurrent()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        lastPublishError = "Configuration saved; publication failed: ${e.message}"
        Log.e(TAG, lastPublishError, e)
        false
    }

    private suspend fun publishCurrent(): Boolean {
        val push = remotePreferencesManager.supportsPush()
        val floor = if (push) maxOf(
            remotePreferencesManager.getRemotePreferences()?.getLong(XposedConfigKeys.REVISION, 0) ?: 0,
            try { ConfigPush.observed(BasicApp.context) }
            catch (e: CancellationException) { throw e }
            catch (_: Exception) { 0L }) else 0
        val snapshot = ConfigSnapshotWriter.snapshot(daDao.loadAllSts(), appDaDao.loadAllAppSts(),
            localState.debug(), localState.nextRevision(floor))
        if (push) ConfigPush.decode(ConfigPush.encode(snapshot)) // Validate before changing durable state.
        val published = forEachBackend { backend ->
            backend.publish(snapshot)
        }
        if (published) localState.markPublished(snapshot.revision)
        if (published && push && !ConfigPush.sendWithRetry(BasicApp.context, snapshot)) {
            lastPublishError = "Configuration saved; system has not confirmed activation"
            return false
        }
        return published
    }

    private fun forEachBackend(action: (XposedConfigBackend) -> Boolean): Boolean {
        var allSucceeded = true
        var publishError: String? = null
        val backends = buildList {
            if (legacyBackend.isReadable) add(legacyBackend)
            remotePreferencesManager.getRemotePreferences()?.let {
                add(SharedPreferencesConfigBackend(ConfigBackendStatus.BACKEND_REMOTE, it))
            }
        }
        if (backends.isEmpty()) {
            lastPublishError = "Configuration saved; no framework backend available"
            return false
        }
        backends.forEach { backend ->
            try {
                if (!action(backend)) {
                    allSucceeded = false
                    val message = "Failed to publish config to ${backend.name}"
                    publishError = message
                    Log.e(TAG, message)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                allSucceeded = false
                publishError = "Failed to publish config to ${backend.name}: ${e.message}"
                Log.e(TAG, "Failed to publish config to ${backend.name}", e)
            }
        }
        lastPublishError = if (allSucceeded) null else publishError
        return allSucceeded
    }

    private fun mergeErrors(primary: String?, secondary: String?): String? {
        return when {
            primary.isNullOrBlank() -> secondary
            secondary.isNullOrBlank() -> primary
            primary == secondary -> primary
            else -> "$primary; $secondary"
        }
    }

    companion object {
        private const val TAG = "ConfigPublisher"
    }
}
