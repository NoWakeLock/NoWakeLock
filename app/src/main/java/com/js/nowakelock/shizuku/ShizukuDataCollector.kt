package com.js.nowakelock.shizuku

import android.content.Context
import android.os.SystemClock
import com.js.nowakelock.data.db.InfoDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

object ShizukuDataCollector {

    private var pollJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    private const val POLL_INTERVAL_MS = 10000L // 10 seconds

    fun startPolling(context: Context) {
        if (pollJob?.isActive == true) return

        pollJob = scope.launch {
            while (isActive) {
                if (ShizukuManager.hasPermission()) {
                    collectAndSaveData(context)
                }
                delay(POLL_INTERVAL_MS)
            }
        }
    }

    fun stopPolling() {
        pollJob?.cancel()
        pollJob = null
    }

    private suspend fun collectAndSaveData(context: Context) {
        val currentTimestamp = System.currentTimeMillis()

        // 1. Collect Wakelocks
        try {
            val wakelockOutput = ShizukuManager.executeCommand("dumpsys power")
            val wakelocks = ShizukuParser.parseWakelocks(wakelockOutput, currentTimestamp)
            for (event in wakelocks) {
                com.js.nowakelock.xposedhook.model.XpRecord.newEvent(
                    name = event.name,
                    packageName = event.packageName,
                    type = event.type,
                    context = context,
                    userId = event.userId,
                    startTime = event.startTime,
                    instanceId = event.instanceId
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Collect Alarms
        try {
            val alarmOutput = ShizukuManager.executeCommand("dumpsys alarm")
            val alarms = ShizukuParser.parseAlarms(alarmOutput, currentTimestamp)
            for (event in alarms) {
                com.js.nowakelock.xposedhook.model.XpRecord.newEvent(
                    name = event.name,
                    packageName = event.packageName,
                    type = event.type,
                    context = context,
                    userId = event.userId,
                    startTime = event.startTime,
                    instanceId = event.instanceId
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 3. Collect Services
        try {
            val serviceOutput = ShizukuManager.executeCommand("dumpsys activity services")
            val services = ShizukuParser.parseServices(serviceOutput, currentTimestamp)
            for (event in services) {
                com.js.nowakelock.xposedhook.model.XpRecord.newEvent(
                    name = event.name,
                    packageName = event.packageName,
                    type = event.type,
                    context = context,
                    userId = event.userId,
                    startTime = event.startTime,
                    instanceId = event.instanceId
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
