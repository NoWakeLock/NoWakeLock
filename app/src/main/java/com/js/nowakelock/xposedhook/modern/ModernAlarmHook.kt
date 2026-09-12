package com.js.nowakelock.xposedhook.modern

import android.content.Context
import android.os.Build
import android.os.PowerManager
import android.os.SystemClock
import com.js.nowakelock.base.getUserId
import com.js.nowakelock.data.db.Type
import com.js.nowakelock.xposedhook.model.XpNSP
import com.js.nowakelock.xposedhook.model.XpRecord
import com.js.nowakelock.xposedhook.model.RuntimeTransfer
import java.util.concurrent.atomic.AtomicBoolean
import io.github.libxposed.api.XposedInterface
import java.lang.reflect.Field
import java.util.concurrent.ConcurrentHashMap

object ModernAlarmHook {
    var booted: Boolean
        get() = RuntimeTransfer.get<AtomicBoolean>("booted").get()
        set(value) { RuntimeTransfer.get<AtomicBoolean>("booted").set(value) }

    private val lastAllowTime = RuntimeTransfer.get<ConcurrentHashMap<String, Long>>("alarmTimes")

    private val positionsByMethod = ConcurrentHashMap<java.lang.reflect.Executable, AlarmParamPositions>()

    private val reportedFailures = ConcurrentHashMap.newKeySet<java.lang.reflect.Executable>()

    fun hook(xposed: XposedInterface, classLoader: ClassLoader) {
        val className = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            "com.android.server.alarm.AlarmManagerService"
        } else {
            "com.android.server.AlarmManagerService"
        }
        val cls = ModernHookSupport.loadClass(className, classLoader) ?: return
        val methods = cls.declaredMethods.filter { it.name == "triggerAlarmsLocked" }
        ModernXposedLog.info("Found ${methods.size} Modern triggerAlarmsLocked methods")
        for (method in methods) {
            ModernHookSupport.hookMethod(xposed, method) { chain ->
                val result = chain.proceed()
                try {
                    handleTriggerAlarms(chain)
                } catch (e: Throwable) {
                    ModernXposedLog.error("Error in Modern triggerAlarmsLocked hook", e)
                }
                result
            }
        }
    }

    private fun handleTriggerAlarms(chain: XposedInterface.Chain) {
        val positions = positionsByMethod[chain.executable]
        if (positions != null) {
            extractWithPositions(chain, positions)
        } else {
            extractAndCacheAlarmParameters(chain)
        }
    }

    private fun extractAndCacheAlarmParameters(chain: XposedInterface.Chain) {
        val androidVersionIndex = when (Build.VERSION.SDK_INT) {
            in Build.VERSION_CODES.S..Int.MAX_VALUE -> 0
            in Build.VERSION_CODES.Q..Build.VERSION_CODES.R -> 1
            in Build.VERSION_CODES.N..Build.VERSION_CODES.P -> 2
            else -> 0
        }

        if (androidVersionIndex < positionStrategies.size) {
            val positions = positionStrategies[androidVersionIndex]
            if (extractWithPositions(chain, positions)) {
                positionsByMethod[chain.executable] = positions
                ModernXposedLog.info("Modern alarm parameter positions cached for Android ${Build.VERSION.SDK_INT}")
                return
            }
        }

        for ((index, positions) in positionStrategies.withIndex()) {
            if (index == androidVersionIndex) continue
            if (extractWithPositions(chain, positions)) {
                positionsByMethod[chain.executable] = positions
                ModernXposedLog.info("Modern alarm parameter positions cached from fallback strategy $index")
                return
            }
        }

        val detectedPos = detectTriggerListPos(ModernHookSupport.args(chain))
        if (detectedPos != null) {
            val positions = AlarmParamPositions(detectedPos)
            if (extractWithPositions(chain, positions)) {
                positionsByMethod[chain.executable] = positions
                ModernXposedLog.info("Modern alarm triggerListPos detected as $detectedPos")
                return
            }
        }

        if (reportedFailures.add(chain.executable)) ModernXposedLog.info("All Modern triggerAlarmsLocked parameter extraction strategies failed")
    }

    private fun extractWithPositions(
        chain: XposedInterface.Chain,
        positions: AlarmParamPositions
    ): Boolean {
        val args = ModernHookSupport.args(chain)
        if (args.size <= positions.triggerListPos) return false
        val triggerList = args[positions.triggerListPos] as? ArrayList<*> ?: return false
        val context = ModernHookSupport.findContext(chain) ?: return false
        hookAlarmsLocked(triggerList, context)
        return true
    }

    private fun detectTriggerListPos(args: List<Any?>): Int? {
        for (i in args.indices) {
            val list = args[i] as? ArrayList<*> ?: continue
            val first = list.firstOrNull { it != null } ?: continue
            if (looksLikeAlarm(first)) return i
        }
        for (i in args.indices) {
            if (args[i] is ArrayList<*>) return i
        }
        return null
    }

    private fun looksLikeAlarm(value: Any): Boolean {
        return field(value.javaClass, "statsTag") != null
            && field(value.javaClass, "packageName") != null
            && field(value.javaClass, "uid") != null
    }

    private fun hookAlarmsLocked(triggerList: ArrayList<*>, context: Context) {
        for (i in triggerList.size - 1 downTo 0) {
            val alarmInfo = extractAlarmInfo(triggerList[i]) ?: continue
            val (alarmName, packageName, uid) = alarmInfo
            val userId = getUserId(uid)
            val now = SystemClock.elapsedRealtime()
            val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            val isScreenOff = booted && !pm.isInteractive
            val blocked = block(
                alarmName,
                packageName,
                userId,
                getLastAllowTime(lastAllowTime, alarmName, packageName, userId),
                now,
                isScreenOff
            )

            if (blocked) {
                triggerList.removeAt(i)
                ModernXposedLog.info("$packageName alarm: $alarmName block $booted ${pm.isInteractive}")
                XpRecord.blockEvent(alarmName, packageName, Type.Alarm, context, userId)
            } else {
                recordLastAllowTime(lastAllowTime, alarmName, packageName, userId, now)
                XpRecord.newEvent(alarmName, packageName, Type.Alarm, context, userId)
            }
        }
    }

    private fun extractAlarmInfo(alarm: Any?): Triple<String, String, Int>? {
        if (alarm == null) return null
        return try {
            val statsTag = readField(alarm, "statsTag") as String
            val alarmName = statsTag.replace(Regex("\\*.*\\*:"), "")
            val packageName = readField(alarm, "packageName") as String
            val uid = readField(alarm, "uid") as Int
            Triple(alarmName, packageName, uid)
        } catch (e: Throwable) {
            ModernXposedLog.error("Unable to extract Modern alarm info", e)
            null
        }
    }

    private fun block(
        name: String,
        packageName: String,
        userId: Int,
        lastActive: Long,
        now: Long,
        isLocked: Boolean
    ): Boolean {
        val config = XpNSP.getInstance().decisionView()
        return config.flag(name, packageName, Type.Alarm, userId)
            || isLocked && config.flagLock(name, packageName, Type.Alarm, userId)
            || config.aTI(now, lastActive, name, packageName, Type.Alarm, userId)
            || config.rE(name, packageName, Type.Alarm, userId)
    }

    private fun getLastAllowTime(
        lastAllowTimes: Map<String, Long>,
        name: String,
        packageName: String,
        userId: Int
    ): Long = lastAllowTimes[lastAllowKey(name, packageName, userId)] ?: 0L

    private fun recordLastAllowTime(
        lastAllowTimes: MutableMap<String, Long>,
        name: String,
        packageName: String,
        userId: Int,
        now: Long
    ) {
        lastAllowTimes[lastAllowKey(name, packageName, userId)] = now
    }

    private fun lastAllowKey(name: String, packageName: String, userId: Int): String {
        return "$name|$packageName|$userId"
    }

    private fun readField(value: Any, name: String): Any? {
        val field = field(value.javaClass, name) ?: throw NoSuchFieldException(name)
        field.isAccessible = true
        return field.get(value)
    }

    private fun field(clazz: Class<*>, name: String): Field? {
        var current: Class<*>? = clazz
        while (current != null) {
            try {
                return current.getDeclaredField(name)
            } catch (_: NoSuchFieldException) {
                current = current.superclass
            }
        }
        return null
    }

    private data class AlarmParamPositions(val triggerListPos: Int)

    private val positionStrategies = listOf(
        AlarmParamPositions(0),
        AlarmParamPositions(0),
        AlarmParamPositions(0)
    )
}
