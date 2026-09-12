package com.js.nowakelock.data.provider

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.provider.Settings
import android.util.Log
import com.js.nowakelock.BuildConfig
import com.js.nowakelock.base.stringToType
import com.js.nowakelock.data.config.ConfigBackendStatus
import com.js.nowakelock.data.counter.WakelockRegistry
import com.js.nowakelock.data.db.InfoDatabase
import com.js.nowakelock.data.db.Type
import com.js.nowakelock.data.db.dao.InfoDao
import com.js.nowakelock.data.db.dao.InfoEventDao
import com.js.nowakelock.data.db.entity.Info
import com.js.nowakelock.data.db.entity.InfoEvent
import com.js.nowakelock.xposedhook.XpUtil
import kotlinx.coroutines.runBlocking
import androidx.room.withTransaction
import com.js.nowakelock.xposedhook.model.XpRecord
import com.js.nowakelock.xposedhook.model.XpNSP
import com.js.nowakelock.xposedhook.model.RuntimeTransfer
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.TimeUnit
import kotlin.concurrent.withLock

/**
 * Get the ContentProvider URI
 */
fun getURI(): Uri {
    return Settings.System.CONTENT_URI
}

/**
 * Content Provider method identifiers
 */
enum class ProviderMethod(var value: String) {
    // Event-related methods
    NewEvent("NewEvent"),     // Record event start/block with statistics update
    EndEvent("EndEvent"),        // Record end time for events (primarily for wakelock)

    // Data access methods
    LoadInfos("LoadInfos"),         // Load statistics summaries
    LoadEvents("LoadEvents"),       // Load detailed event records

    // Management methods
    ClearData("ClearData"),         // Clear statistics and events
    CheckHookActive("CheckHookActive"), // Verify hook is active
    
    // Module check methods
    CheckHookEffectiveness("CheckHookEffectiveness"), // Check if hook has data
    CheckSharedPreferencesPath("CheckSharedPreferencesPath"), // Legacy alias
    CheckConfigBackendStatus("CheckConfigBackendStatus"), // Check config backend
    ReportRuntime("ReportRuntime")
}

/**
 * Content Provider implementation for NoWakeLock
 * Handles database interactions for tracking events and statistics
 */
class XProvider(
    private val context: Context
) {
    private var db: InfoDatabase =
        InfoDatabase.getInstance(context).also {
            val created = RuntimeTransfer.get<AtomicBoolean>("providerCreated")
            if (!RuntimeTransfer.modern || !created.get()) it.clearAllTables()
            if (RuntimeTransfer.modern) created.set(true)
        }
    private var dao: InfoDao = db.infoDao()
    private var eventDao: InfoEventDao = db.infoEventDao()
    private var unixTimeBoot = System.currentTimeMillis() - SystemClock.elapsedRealtime()
    private val wakelockRegistry = WakelockRegistry.getInstance()
    private val TAG = "XProvider"
    private val runtimeReports = java.util.concurrent.ConcurrentHashMap<Int, Bundle>()

    @Volatile private var clearCutoff = 0L
    private var lastDropped = 0L
    private var durationReliable = true
    @Volatile private var retired = false
    private val operations = ReentrantLock()

    init {
        if (RuntimeTransfer.modern) {
            RuntimeTransfer.put("providerContext", context)
            @Suppress("UNCHECKED_CAST")
            (RuntimeTransfer.state()["providerState"] as? Array<Any>)?.let { state ->
                clearCutoff = state[0] as Long
                lastDropped = state[1] as Long
                durationReliable = state[2] as Boolean
                @Suppress("UNCHECKED_CAST")
                wakelockRegistry.restore(state[3] as Map<String, Array<Any>>)
                unixTimeBoot = state[4] as Long
            }
        }
    }

    private fun retire(): Boolean {
        if (!operations.tryLock(1500, TimeUnit.MILLISECONDS)) return false
        try {
            retired = true
            RuntimeTransfer.put("providerState", arrayOf(clearCutoff, lastDropped, durationReliable,
                wakelockRegistry.transferState(), unixTimeBoot))
            return InfoDatabase.closeForReload()
        } finally { operations.unlock() }
    }
    private fun resume() {
        if (!retired) return // A timed-out retirement must not wait again on the busy operation.
        operations.withLock {
            db = InfoDatabase.getInstance(context)
            dao = db.infoDao()
            eventDao = db.infoEventDao()
            retired = false
        }
    }

    fun invalidateDuration() = operations.withLock {
        durationReliable = false
        wakelockRegistry.clearAll()
    }

    companion object {
        @Volatile
        private var instance: XProvider? = null
        private val ownership = ReentrantLock()
        fun retireForReload(): Boolean {
            if (!ownership.tryLock(1500, TimeUnit.MILLISECONDS)) return false
            try {
                RuntimeTransfer.retired = true
                return instance?.retire() ?: true
            } finally { ownership.unlock() }
        }
        fun resumeAfterRefusal() { instance?.resume() }

        fun getInstance(context: Context): XProvider = ownership.withLock {
            if (instance == null) {
                check(!RuntimeTransfer.retired) { "Provider generation is retired" }
                instance = XProvider(context).also { XpRecord.attachProvider(it) }
            }
            instance!!
        }
    }

    /**
     * Route method calls to appropriate handler functions
     */
    fun recordBatch(events: List<XpRecord.Event>) = operations.withLock {
        val dropped = XpRecord.diagnostics().getLong("recordDropped")
        if (dropped != lastDropped) {
            // Lost acquire/release information must not leave a phantom active interval.
            wakelockRegistry.clearAll()
            durationReliable = false
            lastDropped = dropped
        }
        runBlocking {
            db.withTransaction {
                for (event in events) {
                    if (event.generation != XpRecord.currentGeneration() || stale(event.args)) continue
                    when (event.method) {
                        ProviderMethod.NewEvent.value -> newEvent(event.args)
                        ProviderMethod.EndEvent.value -> endEvent(event.args)
                    }
                }
            }
        }
    }

    private fun stale(bundle: Bundle): Boolean =
        bundle.containsKey("__recordEnqueuedAt") && bundle.getLong("__recordEnqueuedAt") <= clearCutoff

    fun getMethod(methodName: String, bundle: Bundle): Bundle? = operations.withLock {
        if (retired) return@withLock null // Modern ingress is queued before reaching this point.
        if ((methodName == ProviderMethod.NewEvent.value || methodName == ProviderMethod.EndEvent.value) && stale(bundle)) return@withLock Bundle()
        if (methodName == ProviderMethod.ReportRuntime.value) {
            // Reports are diagnostic evidence only; never permit arbitrary apps to forge them.
            if (android.os.Binder.getCallingUid() != 1000) return@withLock null
            val pid = android.os.Binder.getCallingPid()
            val now = SystemClock.elapsedRealtime()
            runtimeReports.entries.removeAll { now - it.value.getLong("observedAt") > 60_000 }
            runtimeReports[pid] = Bundle(bundle)
            return@withLock Bundle()
        }
        when (methodName) {
            ProviderMethod.NewEvent.value -> runBlocking { db.withTransaction { newEvent(bundle) } }
            ProviderMethod.EndEvent.value -> runBlocking { db.withTransaction { endEvent(bundle) } }
            ProviderMethod.LoadInfos.value -> loadInfos(bundle)
            ProviderMethod.LoadEvents.value -> loadEvents(bundle)
            ProviderMethod.ClearData.value -> clearData(bundle)
            ProviderMethod.CheckHookActive.value -> checkHookActive(bundle)
            ProviderMethod.CheckHookEffectiveness.value -> checkHookEffectiveness(bundle)
            ProviderMethod.CheckSharedPreferencesPath.value -> checkConfigBackendStatus(bundle)
            ProviderMethod.CheckConfigBackendStatus.value -> checkConfigBackendStatus(bundle)
            else -> null
        }
    }

    /**
     * Record event start or block with statistics update
     * Handles both normal and blocked events in a unified method
     *
     * @param bundle Parameters including:
     *   - name: Event name
     *   - type: Event type
     *   - packageName: Package name
     *   - userId: User ID
     *   - startTime: Event start time
     *   - isBlocked: Whether the event is blocked (default false)
     *   - instanceId: Unique instance ID based on IBinder hash
     * @return Bundle containing eventKey for normal events
     */
    private suspend fun newEvent(bundle: Bundle): Bundle {
        val name = bundle.getString("name") ?: ""
        val type = stringToType(bundle.getString("type") ?: "")
        val packageName = bundle.getString("packageName") ?: ""
        val userId = bundle.getInt("userId", 0)
        var startTime = bundle.getLong("startTime", System.currentTimeMillis())
        val isBlocked = bundle.getBoolean("isBlocked", false)

        // Get instanceId
        val instanceId = bundle.getString("instanceId") ?: run {
            Log.e(TAG, "Instance ID is null")
            return Bundle()
        }

        // Adjust timestamps before 2000-01-01
        if (startTime < 946684800000) {
            startTime += unixTimeBoot
        }

//        XpUtil.log("CP newEvent: $name, $packageName, $type, $userId, $startTime, $isBlocked, $instanceId")

        // Create and insert event record
        val infoEvent = InfoEvent(
            instanceId = instanceId,  // primary key
            name = name,
            type = type,
            packageName = packageName,
            userId = userId,
            startTime = startTime,
            isBlocked = isBlocked
        )
        eventDao.insert(infoEvent)

        // Update statistics
        val info = dao.loadInfo(name, type, userId)

        when {
            info == null -> {
                // Create new statistics record if none exists
                dao.insert(
                    Info(
                        name = name,
                        type = type,
                        packageName = packageName,
                        userId = userId,
                        count = if (!isBlocked) 1 else 0,
                        blockCount = if (isBlocked) 1 else 0
                    )
                )
            }

            isBlocked -> {
                // Just increment block count and return early
                dao.upBlockCountPO(name, type, userId)
                return Bundle()
            }

            else -> {
                // Increment normal event count
                dao.upCountPO(name, type, userId)
            }
        }

        // A blocked request is recorded, but never acquires a wakelock. This
        // also applies when the request created the first statistics row.
        if (type == Type.Wakelock && !isBlocked && durationReliable) {
            try {
                wakelockRegistry.handleAcquire(name, packageName, type, userId, startTime, instanceId)
                    .takeIf { it > 0 }?.let { durationToAdd ->
                        dao.upCountTime(durationToAdd, name, type, userId)
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating countTime on acquire: ${e.message}")
            }
        }


        return Bundle()
    }

    /**
     * Record event end time (primarily for wakelock events)
     * Updates event record and duration statistics
     *
     * @param bundle Parameters including:
     *   - name: Event name
     *   - type: Event type (must be Wakelock)
     *   - packageName: Package name
     *   - userId: User ID
     *   - endTime: Event end time
     *   - instanceId: Unique instance ID based on IBinder hash
     * @return Empty bundle
     */
    private suspend fun endEvent(bundle: Bundle): Bundle {
        val name = bundle.getString("name") ?: ""
        val type = stringToType(bundle.getString("type") ?: "")
        val packageName = bundle.getString("packageName") ?: ""
        val userId = bundle.getInt("userId", 0)
        val instanceId = bundle.getString("instanceId") ?: run {
            Log.e(TAG, "Instance ID is null")
            return Bundle()
        }

        // Only wakelock events are supported
        if (type != Type.Wakelock) {
            return Bundle()
        }

        // Normalize timestamp
        var endTime = bundle.getLong("endTime", System.currentTimeMillis())
        if (endTime < 946684800000) { // Before 2000-01-01
            endTime += unixTimeBoot
        }
        var startTime = bundle.getLong("startTime", -1)
        if (startTime in 1..946684799999) { // Before 2000-01-01
            startTime += unixTimeBoot
        }

//        XpUtil.log("CP endEvent: $name, $packageName, $type, $userId, $startTime, $endTime, $instanceId")

        // Verify event record exists
        val event = eventDao.loadEventById(instanceId) ?: run {
            Log.e(TAG, "Event not found for instanceId: $instanceId")
            return Bundle()
        }

        // Update event end time
        event.endTime = endTime
        if (startTime > 0) {
            event.startTime = startTime
        }

        eventDao.insert(event)

        // Calculate duration using WakelockRegistry
        try {
            val durationToAdd = if (durationReliable) wakelockRegistry.handleRelease(
                name, packageName, type, userId, endTime, instanceId
            ) else 0L
            if (durationToAdd > 0) {
                dao.upCountTime(durationToAdd, name, type, userId)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating duration: ${e.message}")
        }



        return Bundle()
    }

    /**
     * Load event records with optional filtering
     *
     * @param bundle Parameters including:
     *   - type: Optional event type filter
     *   - packageName: Optional package name filter
     *   - userId: User ID filter
     *   - startTime: Optional time range start
     *   - endTime: Optional time range end
     * @return Bundle containing array of InfoEvent objects
     */
    private fun loadEvents(bundle: Bundle): Bundle {
        val type: Type = stringToType(bundle.getString("type") ?: "")
        val packageName = bundle.getString("packageName") ?: ""
        val userId: Int = bundle.getInt("userId", 0)
        val startTime = bundle.getLong("startTime", 0)
        val endTime = bundle.getLong("endTime", System.currentTimeMillis())

        val events: Array<InfoEvent> = runBlocking {
            if (packageName.isEmpty() && type == Type.UnKnow) {
                eventDao.loadAllEvents().toTypedArray()
            } else if (packageName.isEmpty() && type != Type.UnKnow) {
                eventDao.loadEvents(type).toTypedArray()
            } else if (packageName.isNotEmpty() && type == Type.UnKnow) {
                if (startTime > 0) {
                    eventDao.loadEventsInTimeRange(packageName, startTime, endTime, userId)
                        .toTypedArray()
                } else {
                    eventDao.loadEvents(packageName, userId).toTypedArray()
                }
            } else {
                if (startTime > 0) {
                    eventDao.loadEventsInTimeRange(packageName, type, startTime, endTime, userId)
                        .toTypedArray()
                } else {
                    eventDao.loadEvents(packageName, type, userId).toTypedArray()
                }
            }
        }

        return Bundle().apply {
            putSerializable("events", events)
        }
    }

    /**
     * Load statistics summaries with optional filtering
     *
     * @param bundle Parameters including:
     *   - type: Optional event type filter
     *   - packageName: Optional package name filter
     *   - userId: User ID filter
     * @return Bundle containing array of Info objects
     */
    private fun loadInfos(bundle: Bundle): Bundle {
        val type: Type = stringToType(bundle.getString("type") ?: "")
        val packageName = bundle.getString("packageName") ?: ""
        val userId: Int = bundle.getInt("userId", 0)
        val infos: Array<Info> = runBlocking {
            if (packageName.isEmpty() && type == Type.UnKnow) {
                dao.loadInfos().toTypedArray()
            } else if (packageName.isEmpty() && type != Type.UnKnow) {
                dao.loadInfos(type).toTypedArray()
            } else if (packageName.isNotEmpty() && type == Type.UnKnow) {
                dao.loadInfos(packageName, userId).toTypedArray()
            } else {
                dao.loadInfos(packageName, type, userId).toTypedArray()
            }
        }

        return Bundle().apply {
            putSerializable("infos", infos)
        }
    }

    /**
     * Clear statistics and events data
     *
     * @param bundle Parameters:
     *   - clearAll: Whether to clear all data (true) or just counts (false)
     * @return Empty bundle
     */
    private fun clearData(bundle: Bundle): Bundle {
        val clearAll = bundle.getBoolean("clearAll", false)
        clearCutoff = SystemClock.elapsedRealtimeNanos()
        XpRecord.clearGeneration()
        durationReliable = true
        lastDropped = XpRecord.diagnostics().getLong("recordDropped")

        runBlocking {
            if (clearAll) {
                dao.clearAll()
                eventDao.clearAll()
                // Also clear the wakelock registry
                wakelockRegistry.clearAll()
            } else {
                dao.rstAllCount()
                dao.rstAllBlockCount()
                dao.rstAllCountTime()
                eventDao.clearAll()
                // Also clear the wakelock registry
                wakelockRegistry.clearAll()
            }
        }
        return Bundle()
    }

    /**
     * Check if hook is active and get version information
     *
     * @return Bundle with active status and version
     */
    private fun checkHookActive(bundle: Bundle): Bundle {
        return Bundle().apply {
            putBoolean("active", true)
            putString("version", BuildConfig.VERSION_NAME)
            putString("providerBuildId", BuildConfig.HOOK_BUILD_ID)
            putInt("providerPid", android.os.Process.myPid())
            val system = runtimeReports.entries.filter {
                it.value.getString("process") == "system_server" &&
                    SystemClock.elapsedRealtime() - it.value.getLong("observedAt") in 0..60_000
            }.maxByOrNull { it.value.getLong("observedAt") }
            putString("systemBuildId", if (XpNSP.getInstance().api102SystemRuntime && RuntimeTransfer.systemHookObserved)
                BuildConfig.HOOK_BUILD_ID else system?.value?.takeIf { it.getBoolean("hookCodeObserved") }?.getString("hookBuildId"))
            putInt("systemPid", if (XpNSP.getInstance().api102SystemRuntime) android.os.Process.myPid() else system?.key ?: 0)
            putBoolean("providerCodeReady", RuntimeTransfer.providerInstalled && RuntimeTransfer.reloadFailure == null)
            putBoolean("systemCodeReady", if (XpNSP.getInstance().api102SystemRuntime)
                RuntimeTransfer.systemInstalled && RuntimeTransfer.reloadFailure == null else system?.value?.getBoolean("hookCodeReady") == true)
            putString("providerReloadFailure", RuntimeTransfer.reloadFailure)
            putString("systemReloadFailure", if (XpNSP.getInstance().api102SystemRuntime)
                RuntimeTransfer.reloadFailure else system?.value?.getString("hookReloadFailure"))
        }
    }

    /**
     * Check if hook has data in the database for the specified type
     * 
     * @param bundle Parameters including:
     *   - type: Event type to check (Wakelock, Alarm, Service)
     * @return Bundle with hasData status
     */
    private fun checkHookEffectiveness(bundle: Bundle): Bundle {
        val typeString = bundle.getString("type") ?: ""
        val type = stringToType(typeString)
        
        var hasData = false
        
        // Check if there's any data for the given type
        runBlocking {
            val count = dao.getCountByType(type)
            hasData = count > 0
        }
        
        return Bundle().apply {
            putBoolean("hasData", hasData)
            putString("type", type.value)
        }
    }

    /**
     * Check if at least one hook-readable configuration backend is available.
     * 
     * @return Bundle with backend status
     */
    private fun checkConfigBackendStatus(bundle: Bundle): Bundle {
        val status = try {
            XpNSP.getInstance().backendStatus()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking config backend: ${e.message}")
            ConfigBackendStatus(lastError = e.message)
        }
        
        return status.toBundle().apply {
            val now = SystemClock.elapsedRealtime()
            putLong("hookObservedRevision", runtimeReports.values
                .filter { it.getString("process") == "system_server" &&
                    now - it.getLong("observedAt") in 0..60_000 }
                .maxOfOrNull { it.getLong("observedRevision") } ?: 0)
            if (XpNSP.getInstance().api102SystemRuntime) {
                putLong("hookObservedRevision", status.observedRevision)
            }
            putAll(XpRecord.diagnostics())
            putAll(checkHookActive(Bundle()))
            putBoolean("recordDurationReliable", durationReliable)
            putBoolean("pathExists", status.backendAvailable)
            putString("processName", "SettingsProvider pid=${android.os.Process.myPid()}")
            putString("diagnostics", "Statistics: ${XpRecord.diagnostics()} durationReliable=$durationReliable\n" + "SettingsProvider pid=${android.os.Process.myPid()} read=${status.observedRevision}\n" +
                runtimeReports.entries.joinToString("\n") { (pid, report) ->
                    "pid=$pid read=${report.getLong("observedRevision")} observedAt=${report.getLong("observedAt")}ms\n${report.getString("hooks")}"
                })
        }
    }
}
