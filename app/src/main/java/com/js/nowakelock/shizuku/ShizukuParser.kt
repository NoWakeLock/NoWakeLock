package com.js.nowakelock.shizuku

import android.content.pm.PackageManager
import com.js.nowakelock.BasicApp
import com.js.nowakelock.data.db.Type
import com.js.nowakelock.data.db.entity.InfoEvent

object ShizukuParser {

    private val tagRegex = Regex("'([^']+)'")
    private val wsRegex = Regex("""ws=WorkSource\{([^}]+)\}""")
    private val pkgRegex = Regex("""\b([a-z][a-z0-9_]*(\.[a-z0-9_]+)+)\b""")
    private val chainUidRegex = Regex("""\((\d{4,})\)""")
    private val tagPkgRegex = Regex("""([a-z][a-z0-9_]*(\.[a-z0-9_]+)+)""")
    private val uidRegex = Regex("""uid=(\d+)""")

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
     * Parses a single line from `dumpsys power` and extracts active wakelock.
     */
    fun parseWakelockLine(line: String, currentTimestamp: Long): InfoEvent? {
        if (line.contains("WAKE_LOCK") && line.contains("ACQ=")) {
            // Extract tag inside single quotes: 'MyWakelockTag'
            val tagMatch = tagRegex.find(line)
            val tag = tagMatch?.groupValues?.get(1) ?: return null

            var packageName = "unknown"
            var uid = -1
            
            // 1. Try to extract from WorkSource e.g. ws=WorkSource{10656 com.brave.browser}
            val wsMatch = wsRegex.find(line)
            if (wsMatch != null) {
                val wsContent = wsMatch.groupValues[1]
                val pkgMatch = pkgRegex.find(wsContent)
                if (pkgMatch != null) {
                    packageName = pkgMatch.value
                } else {
                    // try to grab uid from WorkChain e.g. WorkChain{(10656)
                    val chainUidMatch = chainUidRegex.find(wsContent)
                    if (chainUidMatch != null) {
                        uid = chainUidMatch.groupValues[1].toInt()
                    }
                }
            }
            
            // 2. Look in the tag itself (very common for *job* / alarms)
            if (packageName == "unknown") {
                val pkgMatch = tagPkgRegex.find(tag)
                if (pkgMatch != null) {
                    packageName = pkgMatch.value
                }
            }
            
            // 3. Fallback to standard UID parsing
            if (packageName == "unknown" && uid == -1) {
                val uidMatch = uidRegex.find(line)
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
                return InfoEvent(
                    instanceId = instanceId,
                    name = tag,
                    type = Type.Wakelock,
                    packageName = packageName,
                    userId = 0, // In production, calculated via (uid / 100000)
                    startTime = currentTimestamp
                )
            }
        }
        return null
    }

    private val alarmRegex = Regex("""tag=\*.*?\*:(.*)""")

    /**
     * Parses a single line from `dumpsys alarm` and extracts active alarm.
     */
    fun parseAlarmLine(line: String, currentTimestamp: Long): InfoEvent? {
        val match = alarmRegex.find(line)
        if (match != null) {
            val tagFull = match.groupValues[1]
            val parts = tagFull.split("/")
            val packageName = parts.getOrNull(0) ?: "unknown"
            val tag = parts.getOrNull(1) ?: tagFull

            val instanceId = InfoEvent.generateInstanceId(tag.hashCode().toString(), currentTimestamp)

            return InfoEvent(
                instanceId = instanceId,
                name = tag,
                type = Type.Alarm,
                packageName = packageName,
                userId = 0,
                startTime = currentTimestamp
            )
        }
        return null
    }

    private val serviceRegex = Regex("""\* ServiceRecord\{.*? u(\d+) ([^/]+)/([^}]+)\}""")

    /**
     * Parses a single line from `dumpsys activity services` and extracts active service.
     */
    fun parseServiceLine(line: String, currentTimestamp: Long): InfoEvent? {
        val match = serviceRegex.find(line)
        if (match != null) {
            val userId = match.groupValues[1].toIntOrNull() ?: 0
            val packageName = match.groupValues[2]
            val serviceName = match.groupValues[3]

            val instanceId = InfoEvent.generateInstanceId(serviceName.hashCode().toString(), currentTimestamp)

            return InfoEvent(
                instanceId = instanceId,
                name = serviceName,
                type = Type.Service,
                packageName = packageName,
                userId = userId,
                startTime = currentTimestamp
            )
        }
        return null
    }
}
