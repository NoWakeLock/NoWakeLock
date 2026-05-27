package com.js.nowakelock.shizuku

import android.content.pm.PackageManager
import com.js.nowakelock.BasicApp
import com.js.nowakelock.data.db.Type
import com.js.nowakelock.data.db.entity.InfoEvent

object ShizukuParser {

    /**
     * Helper to get package name from UID
     */
    private fun getPackageNameFromUid(uid: Int): String {
        if (uid <= 0) return "unknown"
        try {
            val pm = BasicApp.context.packageManager
            val packages = pm.getPackagesForUid(uid)
            if (!packages.isNullOrEmpty()) {
                return packages[0]
            }
        } catch (e: Exception) {
            // Ignore
        }
        return "uid_$uid"
    }

    /**
     * Parses the output of `dumpsys power` and extracts active wakelocks.
     */
    fun parseWakelocks(output: String, currentTimestamp: Long): List<InfoEvent> {
        val events = mutableListOf<InfoEvent>()
        
        output.lineSequence().forEach { line ->
            if (line.contains("WAKE_LOCK") && line.contains("ACQ=")) {
                // Extract tag inside single quotes: 'MyWakelockTag'
                val tagMatch = Regex("'([^']+)'").find(line)
                val tag = tagMatch?.groupValues?.get(1) ?: return@forEach

                var packageName = "unknown"
                var uid = -1
                
                // 1. Try to extract from WorkSource e.g. ws=WorkSource{10656 com.brave.browser}
                val wsMatch = Regex("""ws=WorkSource\{([^}]+)\}""").find(line)
                if (wsMatch != null) {
                    val wsContent = wsMatch.groupValues[1]
                    val pkgMatch = Regex("""\b([a-z][a-z0-9_]*(\.[a-z0-9_]+)+)\b""").find(wsContent)
                    if (pkgMatch != null) {
                        packageName = pkgMatch.value
                    } else {
                        // try to grab uid from WorkChain e.g. WorkChain{(10656)
                        val chainUidMatch = Regex("""\((\d{4,})\)""").find(wsContent)
                        if (chainUidMatch != null) {
                            uid = chainUidMatch.groupValues[1].toInt()
                        }
                    }
                }
                
                // 2. Look in the tag itself (very common for *job* / alarms)
                if (packageName == "unknown") {
                    val pkgMatch = Regex("""([a-z][a-z0-9_]*(\.[a-z0-9_]+)+)""").find(tag)
                    if (pkgMatch != null) {
                        packageName = pkgMatch.value
                    }
                }
                
                // 3. Fallback to standard UID parsing
                if (packageName == "unknown" && uid == -1) {
                    val uidMatch = Regex("""uid=(\d+)""").find(line)
                    if (uidMatch != null) {
                        uid = uidMatch.groupValues[1].toInt()
                    }
                }
                
                // Convert UID to package name if needed
                if (packageName == "unknown" && uid > 0) {
                    packageName = getPackageNameFromUid(uid)
                }

                // If we successfully found a real app
                if (packageName != "unknown" && packageName != "android") {
                    val instanceId = InfoEvent.generateInstanceId(tag.hashCode().toString(), currentTimestamp)
                    events.add(
                        InfoEvent(
                            instanceId = instanceId,
                            name = tag,
                            type = Type.Wakelock,
                            packageName = packageName,
                            userId = 0, // In production, calculated via (uid / 100000)
                            startTime = currentTimestamp
                        )
                    )
                }
            }
        }
        return events
    }

    /**
     * Parses the output of `dumpsys alarm` and extracts active alarms.
     */
    fun parseAlarms(output: String, currentTimestamp: Long): List<InfoEvent> {
        val events = mutableListOf<InfoEvent>()
        // Match lines like: tag=*walarm*:com.example.app/action ...
        // and extract package info from surrounding lines if needed.
        // A simple approach is matching tag lines directly:
        val regex = Regex("""tag=\*.*?\*:(.*)""")
        
        output.lineSequence().forEach { line ->
            val match = regex.find(line)
            if (match != null) {
                val tagFull = match.groupValues[1]
                val parts = tagFull.split("/")
                val packageName = parts.getOrNull(0) ?: "unknown"
                val tag = parts.getOrNull(1) ?: tagFull

                val instanceId = InfoEvent.generateInstanceId(tag.hashCode().toString(), currentTimestamp)

                events.add(
                    InfoEvent(
                        instanceId = instanceId,
                        name = tag,
                        type = Type.Alarm,
                        packageName = packageName,
                        userId = 0,
                        startTime = currentTimestamp
                    )
                )
            }
        }
        return events
    }

    /**
     * Parses the output of `dumpsys activity services` and extracts active services.
     */
    fun parseServices(output: String, currentTimestamp: Long): List<InfoEvent> {
        val events = mutableListOf<InfoEvent>()
        // Match lines like: * ServiceRecord{... u0 com.example.app/.MyService}
        val regex = Regex("""\* ServiceRecord\{.*? u(\d+) ([^/]+)/([^}]+)\}""")
        
        output.lineSequence().forEach { line ->
            val match = regex.find(line)
            if (match != null) {
                val userId = match.groupValues[1].toIntOrNull() ?: 0
                val packageName = match.groupValues[2]
                val serviceName = match.groupValues[3]

                val instanceId = InfoEvent.generateInstanceId(serviceName.hashCode().toString(), currentTimestamp)

                events.add(
                    InfoEvent(
                        instanceId = instanceId,
                        name = serviceName,
                        type = Type.Service,
                        packageName = packageName,
                        userId = userId,
                        startTime = currentTimestamp
                    )
                )
            }
        }
        return events
    }
}
