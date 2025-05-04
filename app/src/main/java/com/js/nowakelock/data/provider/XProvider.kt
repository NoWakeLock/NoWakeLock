package com.js.nowakelock.data.provider

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.provider.Settings
import android.util.Log
import com.js.nowakelock.BuildConfig
import com.js.nowakelock.base.stringToType
import com.js.nowakelock.data.counter.WakelockRegistry
import com.js.nowakelock.data.db.InfoDatabase
import com.js.nowakelock.data.db.Type
import com.js.nowakelock.data.db.dao.InfoDao
import com.js.nowakelock.data.db.dao.InfoEventDao
import com.js.nowakelock.data.db.entity.Info
import com.js.nowakelock.data.db.entity.InfoEvent
import com.js.nowakelock.xposedhook.XpUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

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
    CheckHookActive("CheckHookActive") // Verify hook is active
}

/**
 * Content Provider implementation for NoWakeLock
 * Handles database interactions for tracking events and statistics
 */
class XProvider(
    context: Context
) {
    private var db: InfoDatabase =
        InfoDatabase.getInstance(context).also { it.clearAllTables() } // clear every time
    private var dao: InfoDao = db.infoDao()
    private var eventDao: InfoEventDao = db.infoEventDao()
    private var unixTimeBoot = System.currentTimeMillis() - SystemClock.elapsedRealtime()
    private val wakelockRegistry = WakelockRegistry.getInstance()
    private val TAG = "XProvider"

    companion object {
        @Volatile
        private var instance: XProvider? = null

        fun getInstance(context: Context): XProvider {
            if (instance == null) {
                instance = XProvider(context)
            }
            return instance!!
        }
    }

    /**
     * Route method calls to appropriate handler functions
     */
    fun getMethod(methodName: String, bundle: Bundle): Bundle? {
        return when (methodName) {
            ProviderMethod.NewEvent.value -> newEvent(bundle)
            ProviderMethod.EndEvent.value -> endEvent(bundle)
            ProviderMethod.LoadInfos.value -> loadInfos(bundle)
            ProviderMethod.LoadEvents.value -> loadEvents(bundle)
            ProviderMethod.ClearData.value -> clearData(bundle)
            ProviderMethod.CheckHookActive.value -> checkHookActive(bundle)
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
    private fun newEvent(bundle: Bundle): Bundle {
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

        runBlocking(Dispatchers.IO) {
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
                    return@runBlocking
                }

                else -> {
                    // Increment normal event count
                    dao.upCountPO(name, type, userId)
                }
            }

            // Special handling for Wakelock type events
            if (type == Type.Wakelock) {
                try {
                    wakelockRegistry.handleAcquire(name, packageName, type, userId, startTime, instanceId)
                        .takeIf { it > 0 }?.let { durationToAdd ->
                            dao.upCountTime(durationToAdd, name, type, userId)
                        }
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating countTime on acquire: ${e.message}")
                }
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
    private fun endEvent(bundle: Bundle): Bundle {
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

        runBlocking(Dispatchers.IO) {
            // Verify event record exists
            val event = eventDao.loadEventById(instanceId) ?: run {
                Log.e(TAG, "Event not found for instanceId: $instanceId")
                return@runBlocking
            }

            // Update event end time
            event.endTime = endTime
            if (startTime > 0) {
                event.startTime = startTime
            }

            eventDao.insert(event)

            // Calculate duration using WakelockRegistry
            try {
                val durationToAdd = wakelockRegistry.handleRelease(
                    name, packageName, type, userId, endTime, instanceId
                )
                if (durationToAdd > 0) {
                    dao.upCountTime(durationToAdd, name, type, userId)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error calculating duration: ${e.message}")
            }

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

        val events: Array<InfoEvent> = runBlocking(Dispatchers.IO) {
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
        val infos: Array<Info> = runBlocking(Dispatchers.IO) {
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

        runBlocking {
            if (clearAll) {
                dao.clearAll()
                eventDao.clearAll()
                // Also clear the wakelock registry
                wakelockRegistry.clearAll()
            } else {
                dao.rstAllCount()
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
        }
    }
}