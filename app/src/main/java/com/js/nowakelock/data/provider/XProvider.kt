package com.js.nowakelock.data.provider

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.provider.Settings
import android.util.Log
import com.js.nowakelock.BuildConfig
import com.js.nowakelock.base.infoToBundle
import com.js.nowakelock.base.stringToType
import com.js.nowakelock.data.counter.WakelockRegistry
import com.js.nowakelock.data.db.InfoDatabase
import com.js.nowakelock.data.db.Type
import com.js.nowakelock.data.db.dao.InfoDao
import com.js.nowakelock.data.db.dao.InfoEventDao
import com.js.nowakelock.data.db.entity.Info
import com.js.nowakelock.data.db.entity.InfoEvent
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
    RecordEvent("RecordEvent"),     // Record event start/block with statistics update
    EndEvent("EndEvent"),           // Record end time for events (primarily for wakelock)

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
    private var db: InfoDatabase = InfoDatabase.getInstance(context)
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
            ProviderMethod.RecordEvent.value -> recordEvent(bundle)
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
     * @return Bundle containing eventKey for normal events
     */
    private fun recordEvent(bundle: Bundle): Bundle {
        val name: String = bundle.getString("name") ?: ""
        val type: Type = stringToType(bundle.getString("type") ?: "")
        val packageName = bundle.getString("packageName") ?: ""
        val userId: Int = bundle.getInt("userId", 0)
        var startTime = bundle.getLong("startTime", System.currentTimeMillis())
        val isBlocked = bundle.getBoolean("isBlocked", false)

        // Ignore events before 2000-01-01
        // no idea why..
       if (startTime < 946684800000) {
           startTime = unixTimeBoot + startTime
       }

        // Generate unique event key
        val eventKey = InfoEvent.generateEventKey(name, packageName, type, userId, startTime)

        runBlocking(Dispatchers.IO) {
            val infoEvent = InfoEvent(
                name = name,
                type = type,
                packageName = packageName,
                userId = userId,
                startTime = startTime,
                isBlocked = isBlocked,
                eventKey = eventKey
            )
            eventDao.insert(infoEvent)

            // Update statistics
            val info = dao.loadInfo(name, type, userId)
            if (info != null) {
                if (isBlocked) {
                    dao.upBlockCountPO(name, type, userId)
                } else {
                    dao.upCountPO(name, type, userId)
                    
                    // For non-blocked wakelocks, calculate accurate duration using WakelockRegistry
                    if (type == Type.Wakelock) {
                        try {
                            val durationToAdd = wakelockRegistry.handleAcquire(
                                name, packageName, type, userId, startTime
                            )
                            if (durationToAdd > 0) {
                                dao.upCountTime(durationToAdd, name, type, userId)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error updating countTime on acquire: ${e.message}")
                        }
                    }
                }
            } else {
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
                
                // For first occurrence of a non-blocked wakelock, initialize registry
                if (!isBlocked && type == Type.Wakelock) {
                    try {
                        wakelockRegistry.handleAcquire(name, packageName, type, userId, startTime)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error initializing wakelock counter: ${e.message}")
                    }
                }
            }
        }

        // Return event key for later reference
        return Bundle().apply {
            if (!isBlocked) {
                putString("eventKey", eventKey)
            }
        }
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
     *   - startTime: Optional start time for rebuilding eventKey
     *   - eventKey: Optional direct event key for faster lookup
     * @return Empty bundle
     */
    private fun endEvent(bundle: Bundle): Bundle {
        val name: String = bundle.getString("name") ?: ""
        val type: Type = stringToType(bundle.getString("type") ?: "")
        val packageName = bundle.getString("packageName") ?: ""
        val userId: Int = bundle.getInt("userId", 0)
        var endTime = bundle.getLong("endTime", System.currentTimeMillis())
        val startTime = bundle.getLong("startTime", 0)  // For rebuilding eventKey

        if (type != Type.Wakelock) {
            return Bundle()
        }

        // Ignore events before 2000-01-01
        // no idea why..
       if (endTime < 946684800000) {
           endTime = unixTimeBoot + endTime
       }

        // Get event key from parameters or rebuild it
        val eventKey = bundle.getString("eventKey") ?: InfoEvent.generateEventKey(
            name,
            packageName,
            type,
            userId,
            startTime
        )

        runBlocking(Dispatchers.IO) {
            // Find event by key for efficient lookup
            val targetEvent = eventDao.loadEventByKey(eventKey)

            if (targetEvent != null && targetEvent.endTime == null && !targetEvent.isBlocked) {
                // Update event end time in database
                targetEvent.endTime = endTime
                eventDao.insert(targetEvent)

                // Calculate accurate duration using WakelockRegistry instead of simple subtraction
                try {
                    val durationToAdd = wakelockRegistry.handleRelease(
                        name, packageName, type, userId, endTime
                    )
                    if (durationToAdd > 0) {
                        dao.upCountTime(durationToAdd, name, type, userId)
                    }
                } catch (e: Exception) {
                    // Fallback to simple duration calculation in case of registry error
                    Log.e(TAG, "Error using registry for duration, falling back: ${e.message}")
                    val simpleDuration = endTime - targetEvent.startTime
                    dao.upCountTime(simpleDuration, name, type, userId)
                }
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