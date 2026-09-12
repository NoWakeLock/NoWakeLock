package com.js.nowakelock.xposedhook.modern

import android.content.Context
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.SystemClock
import com.js.nowakelock.base.getUserId
import com.js.nowakelock.data.db.Type
import com.js.nowakelock.data.db.entity.InfoEvent
import com.js.nowakelock.xposedhook.model.XpNSP
import com.js.nowakelock.xposedhook.model.XpRecord
import com.js.nowakelock.xposedhook.model.RuntimeTransfer
import java.util.concurrent.atomic.AtomicBoolean
import io.github.libxposed.api.XposedInterface
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap

object ModernWakelockHook {
    var booted: Boolean
        get() = RuntimeTransfer.get<AtomicBoolean>("booted").get()
        set(value) { RuntimeTransfer.get<AtomicBoolean>("booted").set(value) }

    private val activeWakeLocks = RuntimeTransfer.get<ConcurrentHashMap<IBinder, Array<Any>>>("wakes")

    private val lastAllowTime = RuntimeTransfer.get<ConcurrentHashMap<String, Long>>("wakeTimes")

    private val acquirePositions = ConcurrentHashMap<java.lang.reflect.Executable, WakeLockParamPositions>()

    private val reportedFailures = ConcurrentHashMap.newKeySet<java.lang.reflect.Executable>()

    fun hook(xposed: XposedInterface, classLoader: ClassLoader) {
        val cls = ModernHookSupport.loadClass(
            "com.android.server.power.PowerManagerService",
            classLoader
        ) ?: return

        hookAcquireMethods(xposed, cls)
        hookReleaseMethods(xposed, cls)
    }

    private fun hookAcquireMethods(xposed: XposedInterface, cls: Class<*>) {
        var methods = cls.declaredMethods.filter { it.name == "acquireWakeLockInternal" }
        if (methods.isEmpty()) {
            methods = cls.declaredMethods.filter { it.name.contains("acquireWakeLockInternal") }
        }
        ModernXposedLog.info("Found ${methods.size} Modern acquireWakeLockInternal methods")
        for (method in methods) {
            ModernHookSupport.hookMethod(xposed, method) { chain ->
                try {
                    if (handleAcquire(chain)) {
                        return@hookMethod ModernHookSupport.defaultReturn(chain)
                    }
                } catch (e: Throwable) {
                    ModernXposedLog.error("Error in Modern acquireWakeLockInternal hook", e)
                }
                chain.proceed()
            }
        }
    }

    private fun hookReleaseMethods(xposed: XposedInterface, cls: Class<*>) {
        var methods = cls.declaredMethods.filter { it.name == "releaseWakeLockInternal" }
        if (methods.isEmpty()) {
            methods = cls.declaredMethods.filter { it.name.contains("releaseWakeLockInternal") }
        }
        ModernXposedLog.info("Found ${methods.size} Modern releaseWakeLockInternal methods")
        for (method in methods) {
            ModernHookSupport.hookMethod(xposed, method) { chain ->
                try {
                    val lock = ModernHookSupport.args(chain).firstOrNull { it is IBinder } as? IBinder
                    val context = ModernHookSupport.findContext(chain)
                    if (lock != null && context != null) {
                        handleWakeLockRelease(lock, context)
                    }
                } catch (e: Throwable) {
                    ModernXposedLog.error("Error in Modern releaseWakeLockInternal hook", e)
                }
                chain.proceed()
            }
        }
    }

    private fun handleAcquire(chain: XposedInterface.Chain): Boolean {
        val positions = acquirePositions[chain.executable]
        if (positions != null) {
            return extractWithPositions(chain, positions)?.blocked ?: false
        }
        return extractAndCacheWakeLockParameters(chain)
    }

    private fun extractAndCacheWakeLockParameters(chain: XposedInterface.Chain): Boolean {
        val androidVersionIndex = when (Build.VERSION.SDK_INT) {
            in Build.VERSION_CODES.S..Int.MAX_VALUE -> 0
            in Build.VERSION_CODES.Q..Build.VERSION_CODES.R -> 1
            in Build.VERSION_CODES.N..Build.VERSION_CODES.P -> 2
            else -> 0
        }

        if (androidVersionIndex < positionStrategies.size) {
            val extracted = tryExtractWithPositions(chain, positionStrategies[androidVersionIndex])
            if (extracted != null) {
                acquirePositions[chain.executable] = extracted.positions
                ModernXposedLog.info("Modern wakelock parameter positions cached for Android ${Build.VERSION.SDK_INT}")
                return extracted.blocked
            }
        }

        for ((index, positions) in positionStrategies.withIndex()) {
            if (index == androidVersionIndex) continue
            val extracted = tryExtractWithPositions(chain, positions)
            if (extracted != null) {
                acquirePositions[chain.executable] = extracted.positions
                ModernXposedLog.info("Modern wakelock parameter positions cached from fallback strategy $index")
                return extracted.blocked
            }
        }

        if (reportedFailures.add(chain.executable)) ModernXposedLog.info("All Modern acquireWakeLockInternal parameter extraction strategies failed")
        return false
    }

    private fun tryExtractWithPositions(
        chain: XposedInterface.Chain,
        positions: WakeLockParamPositions
    ): WakeLockExtraction? {
        extractWithPositions(chain, positions)?.let { return it }
        val args = ModernHookSupport.args(chain)
        if (shouldShiftForNestAccessor(args)) {
            val shifted = positions.shifted()
            extractWithPositions(chain, shifted)?.let { return it }
        }
        return null
    }

    private fun extractWithPositions(
        chain: XposedInterface.Chain,
        positions: WakeLockParamPositions
    ): WakeLockExtraction? {
        val args = ModernHookSupport.args(chain)
        if (args.size <= maxOf(positions.lockPos, positions.tagPos, positions.packagePos, positions.uidPos)) {
            return null
        }
        val lock = args[positions.lockPos] as? IBinder ?: return null
        val name = args[positions.tagPos] as? String ?: return null
        val packageName = args[positions.packagePos] as? String ?: return null
        val uid = args[positions.uidPos] as? Int ?: return null
        val context = ModernHookSupport.findContext(chain) ?: return null
        val blocked = handleWakeLockAcquire(packageName, name, uid, lock, context)
        return WakeLockExtraction(positions, blocked)
    }

    private fun shouldShiftForNestAccessor(args: List<Any?>): Boolean {
        return args.firstOrNull()?.javaClass?.name == "com.android.server.power.PowerManagerService"
    }

    private fun handleWakeLockAcquire(
        packageName: String,
        name: String,
        uid: Int,
        lock: IBinder,
        context: Context
    ): Boolean {
        val userId = getUserId(uid)
        val now = SystemClock.elapsedRealtime()
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val instanceId = generateInstanceId(lock, now)
        val blocked = block(
            name,
            packageName,
            userId,
            getLastAllowTime(lastAllowTime, name, packageName, userId),
            now,
            booted && !pm.isInteractive
        )

        if (blocked) {
            ModernXposedLog.info("$packageName wakeLock:$name block '${pm.isInteractive}' '$booted'")
            XpRecord.blockEvent(name, packageName, Type.Wakelock, context, userId, now, instanceId)
            return true
        }

        recordLastAllowTime(lastAllowTime, name, packageName, userId, now)
        activeWakeLocks[lock] = arrayOf(name, packageName, userId, now, instanceId)
        XpRecord.newEvent(name, packageName, Type.Wakelock, context, userId, now, instanceId)
        return false
    }

    private fun handleWakeLockRelease(lock: IBinder, context: Context) {
        val trace = activeWakeLocks.remove(lock) ?: return
        val now = SystemClock.elapsedRealtime()
        XpRecord.endEvent(
            name = trace[0] as String,
            packageName = trace[1] as String,
            type = Type.Wakelock,
            context = context,
            userId = trace[2] as Int,
            startTime = trace[3] as Long,
            endTime = now,
            instanceId = trace[4] as String
        )
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
        return config.flag(name, packageName, Type.Wakelock, userId)
            || isLocked && config.flagLock(name, packageName, Type.Wakelock, userId)
            || config.aTI(now, lastActive, name, packageName, Type.Wakelock, userId)
            || config.rE(name, packageName, Type.Wakelock, userId)
    }

    private fun generateInstanceId(lock: IBinder, timestamp: Long): String {
        return InfoEvent.generateInstanceId(System.identityHashCode(lock).toString(16), timestamp)
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

    private data class WakeLockParamPositions(
        val lockPos: Int,
        val tagPos: Int,
        val packagePos: Int,
        val uidPos: Int
    ) {
        fun shifted(): WakeLockParamPositions {
            return WakeLockParamPositions(lockPos + 1, tagPos + 1, packagePos + 1, uidPos + 1)
        }
    }

    private data class WakeLockExtraction(
        val positions: WakeLockParamPositions,
        val blocked: Boolean
    )

    private val positionStrategies = listOf(
        WakeLockParamPositions(0, 3, 4, 7),
        WakeLockParamPositions(0, 2, 3, 6),
        WakeLockParamPositions(0, 2, 3, 6)
    )
}
