package com.js.nowakelock.xposedhook.modern

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import com.js.nowakelock.data.db.Type
import com.js.nowakelock.xposedhook.model.XpNSP
import com.js.nowakelock.xposedhook.model.XpRecord
import io.github.libxposed.api.XposedInterface
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.atomic.AtomicBoolean
import com.js.nowakelock.xposedhook.model.RuntimeTransfer

object ModernServiceHook {
    private val activeCalls = RuntimeTransfer.serviceCalls()
    var booted: Boolean
        get() = RuntimeTransfer.get<AtomicBoolean>("booted").get()
        set(value) { RuntimeTransfer.get<AtomicBoolean>("booted").set(value) }

    fun hook(xposed: XposedInterface, classLoader: ClassLoader) {
        val cls = ModernHookSupport.loadClass(
            "com.android.server.am.ActiveServices",
            classLoader
        ) ?: return

        hookServiceClass(xposed, cls)
    }

    internal fun hookServiceClass(xposed: XposedInterface, cls: Class<*>) {
        hookServiceMethods(
            xposed = xposed,
            methods = cls.declaredMethods.filter { it.name == "startServiceLocked" },
            label = "startServiceLocked",
            strategies = startPositionStrategies,
            androidVersionIndex = startAndroidVersionIndex()
        )

        hookServiceMethods(
            xposed = xposed,
            methods = cls.declaredMethods.filter { it.name == "bindServiceLocked" },
            label = "bindServiceLocked",
            strategies = bindPositionStrategies,
            androidVersionIndex = bindAndroidVersionIndex()
        )
    }

    private fun hookServiceMethods(
        xposed: XposedInterface,
        methods: List<java.lang.reflect.Method>,
        label: String,
        strategies: List<ServiceParamPositions>,
        androidVersionIndex: Int
    ) {
        ModernXposedLog.info("Found ${methods.size} Modern $label methods")
        for (method in methods) {
            val positionsRef = AtomicReference<ServiceParamPositions?>(null)
            val reportedFailure = java.util.concurrent.atomic.AtomicBoolean(false)
            ModernHookSupport.hookMethod(xposed, method) { chain ->
                val previous = activeCalls.get()
                try {
                    val blocked = try {
                        handleService(
                            chain,
                            positionsRef,
                            { if (reportedFailure.compareAndSet(false, true))
                                ModernXposedLog.info("Parameter extraction failed: ${method.toGenericString()}; will retry") },
                            strategies,
                            androidVersionIndex,
                            label
                        )
                    } catch (e: Throwable) {
                        ModernXposedLog.error("Error in Modern $label hook", e)
                        false
                    }
                    if (blocked) ModernHookSupport.defaultReturn(chain) else chain.proceed()
                } finally {
                    if (previous == null) activeCalls.remove() else activeCalls.set(previous)
                }
            }
        }
    }

    private fun handleService(
        chain: XposedInterface.Chain,
        positionsRef: AtomicReference<ServiceParamPositions?>,
        markFailed: () -> Unit,
        strategies: List<ServiceParamPositions>,
        androidVersionIndex: Int,
        label: String
    ): Boolean {
        val positions = positionsRef.get()
        if (positions != null) {
            return extractWithPositions(chain, positions, label)?.blocked ?: false
        }
        return extractAndCacheServiceParameters(
                chain,
                positionsRef,
                markFailed,
                strategies,
                androidVersionIndex,
                label
            )
    }

    private fun extractAndCacheServiceParameters(
        chain: XposedInterface.Chain,
        positionsRef: AtomicReference<ServiceParamPositions?>,
        markFailed: () -> Unit,
        strategies: List<ServiceParamPositions>,
        androidVersionIndex: Int,
        label: String
    ): Boolean {
        if (androidVersionIndex < strategies.size) {
            val positions = strategies[androidVersionIndex]
            val extracted = extractWithPositions(chain, positions, label)
            if (extracted != null) {
                positionsRef.set(positions)
                ModernXposedLog.info("Modern $label parameter positions cached for Android ${Build.VERSION.SDK_INT}")
                return extracted.blocked
            }
        }

        for ((index, positions) in strategies.withIndex()) {
            if (index == androidVersionIndex) continue
            val extracted = extractWithPositions(chain, positions, label)
            if (extracted != null) {
                positionsRef.set(positions)
                ModernXposedLog.info("Modern $label parameter positions cached from fallback strategy $index")
                return extracted.blocked
            }
        }

        markFailed()
        return false
    }

    private fun extractWithPositions(
        chain: XposedInterface.Chain,
        positions: ServiceParamPositions,
        label: String
    ): ServiceExtraction? {
        val args = ModernHookSupport.args(chain)
        if (args.size <= maxOf(positions.servicePos, positions.packagePos, positions.userIdPos)) {
            return null
        }
        val service = args[positions.servicePos] as? Intent ?: return null
        val packageName = args[positions.packagePos] as? String ?: return null
        val userId = args[positions.userIdPos] as? Int ?: return null
        if (userId !in 0..1000) return null
        val context = ModernHookSupport.findContext(chain) ?: return null
        // ROM overloads may delegate to each other with the same request. Evaluate and
        // record that request once; independent intents, users and start/bind stay distinct.
        var parent = activeCalls.get()
        while (parent != null) {
            if (parent[0] == label && parent[1] === service &&
                parent[2] == packageName && parent[3] == userId) return ServiceExtraction(positions, false)
            @Suppress("UNCHECKED_CAST")
            val previous = parent[4] as? Array<Any?>
            parent = previous
        }
        val blocked = hookStartServiceLocked(service, packageName, context, userId)
        activeCalls.set(arrayOf(label, service, packageName, userId, activeCalls.get()))
        return ServiceExtraction(positions, blocked)
    }

    private fun hookStartServiceLocked(
        service: Intent,
        packageName: String,
        context: Context,
        userId: Int
    ): Boolean {
        val serviceName = service.component?.flattenToShortString() ?: return false
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val blocked = block(serviceName, packageName, userId, booted && !pm.isInteractive)

        if (blocked) {
            ModernXposedLog.info("$packageName service: $serviceName block $booted ${pm.isInteractive}")
            XpRecord.blockEvent(serviceName, packageName, Type.Service, context, userId)
            return true
        }

        XpRecord.newEvent(serviceName, packageName, Type.Service, context, userId)
        return false
    }

    private fun block(
        name: String,
        packageName: String,
        userId: Int,
        isLocked: Boolean
    ): Boolean {
        val config = XpNSP.getInstance().decisionView()
        return shouldBlockService(
            fullBlock = config.flag(name, packageName, Type.Service, userId),
            screenOffBlock = config.flagLock(name, packageName, Type.Service, userId),
            isLocked = isLocked
        )
    }

    private fun shouldBlockService(
        fullBlock: Boolean,
        screenOffBlock: Boolean,
        isLocked: Boolean
    ): Boolean = fullBlock || (isLocked && screenOffBlock)

    private fun startAndroidVersionIndex(): Int {
        return when (Build.VERSION.SDK_INT) {
            in Build.VERSION_CODES.UPSIDE_DOWN_CAKE..Int.MAX_VALUE -> 0
            in Build.VERSION_CODES.S..Build.VERSION_CODES.TIRAMISU -> 1
            Build.VERSION_CODES.R -> 2
            Build.VERSION_CODES.Q -> 3
            in Build.VERSION_CODES.O..Build.VERSION_CODES.P -> 4
            in Build.VERSION_CODES.N..Build.VERSION_CODES.N_MR1 -> 5
            else -> 0
        }
    }

    private fun bindAndroidVersionIndex(): Int {
        return when (Build.VERSION.SDK_INT) {
            in Build.VERSION_CODES.UPSIDE_DOWN_CAKE..Int.MAX_VALUE -> 0
            in Build.VERSION_CODES.S..Build.VERSION_CODES.TIRAMISU -> 1
            else -> 2
        }
    }

    private data class ServiceParamPositions(
        val servicePos: Int,
        val packagePos: Int,
        val userIdPos: Int
    )

    private data class ServiceExtraction(
        val positions: ServiceParamPositions,
        val blocked: Boolean
    )

    private val startPositionStrategies = listOf(
        ServiceParamPositions(1, 6, 8),
        ServiceParamPositions(1, 6, 8),
        ServiceParamPositions(1, 6, 8),
        ServiceParamPositions(1, 6, 7),
        ServiceParamPositions(1, 6, 7),
        ServiceParamPositions(1, 5, 6)
    )

    private val bindPositionStrategies = listOf(
        ServiceParamPositions(2, 11, 12),
        ServiceParamPositions(2, 7, 8),
        ServiceParamPositions(2, 7, 8)
    )
}
