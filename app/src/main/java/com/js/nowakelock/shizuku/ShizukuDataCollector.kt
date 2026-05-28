package com.js.nowakelock.shizuku

import android.content.Context
import com.js.nowakelock.data.db.entity.InfoEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.ConcurrentHashMap

object ShizukuDataCollector {

    private var pollJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    private const val POLL_INTERVAL_MS = 15000L // 15 seconds

    // State trackers to prevent database flooding and UI OOM crashes
    private val activeWakelocks = ConcurrentHashMap<String, InfoEvent>()
    private val activeAlarms = ConcurrentHashMap<String, InfoEvent>()
    private val activeServices = ConcurrentHashMap<String, InfoEvent>()

    fun startPolling(context: Context) {
        if (pollJob?.isActive == true) return

        pollJob = scope.launch {
            while (isActive) {
                if (ShizukuManager.hasPermission()) {
                    // Timeout wrap prevents dumpsys from permanently hanging the collector thread
                    withTimeoutOrNull(25000L) {
                        collectAndSaveData(context)
                    }
                }
                delay(POLL_INTERVAL_MS)
            }
        }
    }

    fun stopPolling() {
        pollJob?.cancel()
        pollJob = null
        activeWakelocks.clear()
        activeAlarms.clear()
        activeServices.clear()
    }

    private suspend fun collectAndSaveData(context: Context) {
        val currentTimestamp = System.currentTimeMillis()

        // 1. Collect Wakelocks (with OS-level 10s timeout to prevent system_server hangs)
        val currentWakelocks = mutableSetOf<String>()
        try {
            ShizukuManager.executeCommand("dumpsys -t 10 power | grep WAKE_LOCK") { line ->
                val event = ShizukuParser.parseWakelockLine(line, currentTimestamp)
                if (event != null) {
                    val key = "${event.packageName}:${event.name}"
                    currentWakelocks.add(key)
                    
                    if (!activeWakelocks.containsKey(key)) {
                        activeWakelocks[key] = event
                        com.js.nowakelock.xposedhook.model.XpRecord.newEvent(
                            name = event.name,
                            packageName = event.packageName,
                            type = event.type,
                            context = context,
                            userId = event.userId,
                            startTime = event.startTime,
                            instanceId = key
                        )
                    }
                }
            }
            
            // End wakelocks that are no longer active
            val endedWakelocks = activeWakelocks.keys.minus(currentWakelocks)
            for (key in endedWakelocks) {
                activeWakelocks.remove(key)?.let { event ->
                    com.js.nowakelock.xposedhook.model.XpRecord.endEvent(
                        name = event.name,
                        packageName = event.packageName,
                        type = event.type,
                        context = context,
                        userId = event.userId,
                        startTime = event.startTime,
                        endTime = currentTimestamp,
                        instanceId = key
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Collect Alarms
        val currentAlarms = mutableSetOf<String>()
        try {
            ShizukuManager.executeCommand("dumpsys -t 10 alarm | grep \"tag=\\*\"") { line ->
                val event = ShizukuParser.parseAlarmLine(line, currentTimestamp)
                if (event != null) {
                    val key = "${event.packageName}:${event.name}"
                    currentAlarms.add(key)
                    
                    if (!activeAlarms.containsKey(key)) {
                        activeAlarms[key] = event
                        com.js.nowakelock.xposedhook.model.XpRecord.newEvent(
                            name = event.name,
                            packageName = event.packageName,
                            type = event.type,
                            context = context,
                            userId = event.userId,
                            startTime = event.startTime,
                            instanceId = key
                        )
                    }
                }
            }
            
            val endedAlarms = activeAlarms.keys.minus(currentAlarms)
            for (key in endedAlarms) {
                activeAlarms.remove(key)?.let { event ->
                    com.js.nowakelock.xposedhook.model.XpRecord.endEvent(
                        name = event.name,
                        packageName = event.packageName,
                        type = event.type,
                        context = context,
                        userId = event.userId,
                        startTime = event.startTime,
                        endTime = currentTimestamp,
                        instanceId = key
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 3. Collect Services
        val currentServices = mutableSetOf<String>()
        try {
            ShizukuManager.executeCommand("dumpsys -t 10 activity services | grep ServiceRecord") { line ->
                val event = ShizukuParser.parseServiceLine(line, currentTimestamp)
                if (event != null) {
                        val key = "${event.packageName}:${event.name}"
                        currentServices.add(key)
                        
                        if (!activeServices.containsKey(key)) {
                            activeServices[key] = event
                            com.js.nowakelock.xposedhook.model.XpRecord.newEvent(
                                name = event.name,
                                packageName = event.packageName,
                                type = event.type,
                                context = context,
                                userId = event.userId,
                                startTime = event.startTime,
                                instanceId = key
                            )
                        }
                }
            }
            
            val endedServices = activeServices.keys.minus(currentServices)
            for (key in endedServices) {
                activeServices.remove(key)?.let { event ->
                    com.js.nowakelock.xposedhook.model.XpRecord.endEvent(
                        name = event.name,
                        packageName = event.packageName,
                        type = event.type,
                        context = context,
                        userId = event.userId,
                        startTime = event.startTime,
                        endTime = currentTimestamp,
                        instanceId = key
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}