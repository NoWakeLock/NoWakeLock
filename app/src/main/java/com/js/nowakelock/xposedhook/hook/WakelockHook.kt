package com.js.nowakelock.xposedhook.hook

import android.app.AndroidAppHelper
import android.content.Context
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.SystemClock
import com.js.nowakelock.base.getUserId
import com.js.nowakelock.data.db.Type
import com.js.nowakelock.data.db.entity.InfoEvent
import com.js.nowakelock.xposedhook.XpUtil
import com.js.nowakelock.xposedhook.model.XpNSP
import com.js.nowakelock.xposedhook.model.XpRecord
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.lang.reflect.Method
import java.util.concurrent.atomic.AtomicReference

// GUARDED - ASK BEFORE MODIFYING
class WakelockHook {
    companion object {

        private val type = Type.Wakelock
        var booted = false

        @Volatile
        private var wlTs = HashMap<IBinder, WLT>()//wakelock witch active

        @Volatile
        private var lastAllowTime = HashMap<String, Long>()//wakelock last allow time

        // Data class to store valid parameter positions for acquireWakeLockInternal
        private data class WakeLockParamPositions(
            val lockPos: Int,       // IBinder parameter position
            val tagPos: Int,        // wakeLockName/tag parameter position
            val packagePos: Int,    // packageName parameter position
            val uidPos: Int         // uid parameter position
        )

        // Cache for parameter positions using AtomicReference for thread safety
        @Volatile
        private var acquireWakeLockPositionsRef: AtomicReference<WakeLockParamPositions?> =
            AtomicReference(null)

        // Flag to indicate if all extraction attempts have failed
        @Volatile
        private var acquireWakeLockHookFailed = false

        // Predefined parameter positions for different Android versions
        private val acquireWakeLockPositionStrategies = listOf(
            // Android 12+ (API 31+)
            WakeLockParamPositions(0, 3, 4, 7),
            // Android 10-11 (API 29-30)
            WakeLockParamPositions(0, 2, 3, 6),
            // Android 7-9 (API 24-28)
            WakeLockParamPositions(0, 2, 3, 6)
        )

        // CRITICAL - BUSINESS LOGIC
        fun hookWakeLocks(lpparam: XC_LoadPackage.LoadPackageParam) {
            //for test
//            wakelockTest(lpparam)

            // Try the unified adaptive hook first
            if (!unifiedWakeLockHook(lpparam)) {
                // Fall back to version-specific hooks if unified approach fails
                XpUtil.log("Falling back to version-specific wakelock hooks")
                when (Build.VERSION.SDK_INT) {
                    //Try for alarm hooks for API levels >= 31 (S or higher)
                    in Build.VERSION_CODES.S..40 -> wakeLockHook31(lpparam)
                    //hooks for API levels 24-30 (N ~ R)
                    in Build.VERSION_CODES.N..Build.VERSION_CODES.R -> wakeLockHook24to30(lpparam)
                }
            }
        }

        /**
         * Unified wakelock hook approach that works across all Android versions
         * Returns true if hooks were successfully applied, false otherwise
         */
        private fun unifiedWakeLockHook(lpparam: XC_LoadPackage.LoadPackageParam): Boolean {
            try {
                XpUtil.log("Trying unified wakelock hook for Android ${Build.VERSION.SDK_INT}")
                
                // Get the PowerManagerService class
                val powerManagerServiceClass = 
                    XpUtil.getClass("com.android.server.power.PowerManagerService", lpparam.classLoader)
                    ?: return false
                
                // Hook acquireWakeLockInternal methods
                val acquireSuccess = hookAcquireWakeLockMethods(powerManagerServiceClass, lpparam)
                
                // Hook releaseWakeLockInternal method
                val releaseSuccess = hookReleaseWakeLockMethod(powerManagerServiceClass, lpparam)
                
                return acquireSuccess && releaseSuccess
            } catch (e: Throwable) {
                XpUtil.log("Error in unified wakelock hook: ${e.message}")
                e.printStackTrace()
                return false
            }
        }

        /**
         * Hook all acquireWakeLockInternal methods
         */
        private fun hookAcquireWakeLockMethods(
            powerManagerServiceClass: Class<*>,
            lpparam: XC_LoadPackage.LoadPackageParam
        ): Boolean {
            try {
                // Find all methods named acquireWakeLockInternal.
                // Samsung/OneUI may not expose the original method name and instead uses a Nest accessor
                // like: m773$$Nest$macquireWakeLockInternal(...)
                var methods =
                    powerManagerServiceClass.declaredMethods.filter { it.name == "acquireWakeLockInternal" }

                if (methods.isEmpty()) {
                    methods =
                        powerManagerServiceClass.declaredMethods.filter { it.name.contains("acquireWakeLockInternal") }
                    if (methods.isNotEmpty()) {
                        XpUtil.log("Fallback: Found ${methods.size} acquireWakeLockInternal-like methods via contains()")
                    }
                }
                
                XpUtil.log("Found ${methods.size} acquireWakeLockInternal methods")
                
                if (methods.isEmpty()) {
                    XpUtil.log("No acquireWakeLockInternal methods found!")
                    return false
                }
                
                // Hook each method found
                for (method in methods) {
                    hookAcquireWakeLockMethod(method, lpparam)
                }
                return true
            } catch (e: Throwable) {
                XpUtil.log("Error hooking acquireWakeLockInternal methods: ${e.message}")
                e.printStackTrace()
                return false
            }
        }

        /**
         * Hook releaseWakeLockInternal method
         */
        private fun hookReleaseWakeLockMethod(
            powerManagerServiceClass: Class<*>,
            lpparam: XC_LoadPackage.LoadPackageParam
        ): Boolean {
            try {
                // Hook the releaseWakeLockInternal method
                XposedHelpers.findAndHookMethod(
                    powerManagerServiceClass,
                    "releaseWakeLockInternal",
                    IBinder::class.java,
                    Int::class.javaPrimitiveType,
                    object : XC_MethodHook() {
                        @Throws(Throwable::class)
                        override fun beforeHookedMethod(param: MethodHookParam) {
                            try {
                                val context = AndroidAppHelper.currentApplication()
                                val lock = param.args.firstOrNull { it is IBinder } as? IBinder ?: return
                                handleWakeLockRelease(lock, context)
                            } catch (e: Exception) {
                                XpUtil.log("Error in releaseWakeLockInternal hook: ${e.message}")
                            }
                        }
                    })
                return true
            } catch (e: Throwable) {
                XpUtil.log("Error hooking releaseWakeLockInternal method: ${e.message}")

                // Samsung/OneUI fallback: Nest accessor method, e.g. m787$$Nest$mreleaseWakeLockInternal(PMS, IBinder, int)
                try {
                    val methods =
                        powerManagerServiceClass.declaredMethods.filter { it.name.contains("releaseWakeLockInternal") }

                    XpUtil.log("Fallback: Found ${methods.size} releaseWakeLockInternal-like methods via contains()")
                    if (methods.isEmpty()) {
                        e.printStackTrace()
                        return false
                    }

                    for (method in methods) {
                        XpUtil.log("Hooking releaseWakeLockInternal-like method: ${method.name}(${method.parameterTypes.joinToString()})")
                        XposedBridge.hookMethod(method, object : XC_MethodHook() {
                            @Throws(Throwable::class)
                            override fun beforeHookedMethod(param: MethodHookParam) {
                                try {
                                    val context = AndroidAppHelper.currentApplication()
                                    val lock = param.args.firstOrNull { it is IBinder } as? IBinder ?: return
                                    handleWakeLockRelease(lock, context)
                                } catch (t: Throwable) {
                                    XpUtil.log("Error in releaseWakeLockInternal-like hook: ${t.message}")
                                }
                            }
                        })
                    }
                    return true
                } catch (t: Throwable) {
                    XpUtil.log("Fallback error hooking releaseWakeLockInternal-like methods: ${t.message}")
                    t.printStackTrace()
                    return false
                }
            }
        }

        /**
         * Hook a specific acquireWakeLockInternal method with parameter caching
         */
        private fun hookAcquireWakeLockMethod(
            method: Method,
            lpparam: XC_LoadPackage.LoadPackageParam
        ) {
            XpUtil.log("Hooking acquireWakeLockInternal method with signature: ${method.parameterTypes.joinToString()}")
            
            XposedBridge.hookMethod(method, object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun beforeHookedMethod(param: MethodHookParam) {
                    try {
                        // Check if we have cached positions
                        val positions = acquireWakeLockPositionsRef.get()
                        
                        if (positions != null) {
                            if(XpNSP.getInstance().getDebug()){
                                XpUtil.log("Using cached positions for acquireWakeLockInternal on Android ${Build.VERSION.SDK_INT}")
                            }
                            // Use cached positions to extract parameters
                            extractParametersFromCache(param, positions)
                        } else if (!acquireWakeLockHookFailed) {
                            if (XpNSP.getInstance().getDebug()){
                                XpUtil.log("No cached positions for acquireWakeLockInternal, trying to extract parameters")
                            }
                            // Try to extract parameters using strategies
                            extractAndCacheWakeLockParameters(param)
                        }
                    } catch (e: Exception) {
                        XpUtil.log("Error in acquireWakeLockInternal hook callback: ${e.message}")
                        e.printStackTrace()
                    }
                }
            })
        }

        /**
         * Extract parameters using cached positions
         */
        private fun extractParametersFromCache(
            param: XC_MethodHook.MethodHookParam,
            positions: WakeLockParamPositions
        ) {
            try {
                val args = param.args
                
                // Extract parameters using cached positions
                val lock = args[positions.lockPos] as? IBinder
                val wN = args[positions.tagPos] as? String
                val pN = args[positions.packagePos] as? String
                val uid = args[positions.uidPos] as? Int
                
                // Process the wakelock if parameters are valid
                if (lock != null && wN != null && pN != null && uid != null) {
                    val context = AndroidAppHelper.currentApplication()
                    handleWakeLockAcquire(param, pN, wN, uid, lock, context)
                }
            } catch (e: Exception) {
                XpUtil.log("Error extracting parameters from cache: ${e.message}")
            }
        }

        /**
         * Try to extract and cache parameters for acquireWakeLockInternal
         */
        private fun extractAndCacheWakeLockParameters(param: XC_MethodHook.MethodHookParam) {
            val args = param.args
            
            // First try the strategy for current Android version
            val androidVersionIndex = when (Build.VERSION.SDK_INT) {
                in Build.VERSION_CODES.S..Int.MAX_VALUE -> 0 // Android 12+
                in Build.VERSION_CODES.Q..Build.VERSION_CODES.R -> 1 // Android 10-11
                in Build.VERSION_CODES.N..Build.VERSION_CODES.P -> 2 // Android 7-9
                else -> 0 // Default to newest version strategy
            }
            
            // Try the expected strategy for current version first
            if (androidVersionIndex < acquireWakeLockPositionStrategies.size) {
                val positions = acquireWakeLockPositionStrategies[androidVersionIndex]
                val extracted = tryExtractWithPositions(param, positions)
                if (extracted != null) {
                    // Cache successful positions (may be shifted for Nest accessor)
                    acquireWakeLockPositionsRef.set(extracted)
                    XpUtil.log("Successfully extracted parameters for acquireWakeLockInternal on Android ${Build.VERSION.SDK_INT}")
                    return
                } else {
                    // Log that the expected strategy failed
                    XpUtil.log("Expected acquireWakeLockInternal parameter positions for Android ${Build.VERSION.SDK_INT} failed")
                }
            }
            
            // Try all strategies if the expected one failed
            XpUtil.log("Trying all strategies for acquireWakeLockInternal on Android ${Build.VERSION.SDK_INT}")
            for ((index, positions) in acquireWakeLockPositionStrategies.withIndex()) {
                if (index != androidVersionIndex) {
                    val extracted = tryExtractWithPositions(param, positions)
                    if (extracted == null) {
                        continue
                    }

                    // Cache successful positions (may be shifted for Nest accessor)
                    acquireWakeLockPositionsRef.set(extracted)
                    
                    // Log warning that we're using different positions than expected
                    XpUtil.log("Warning: Using unexpected parameter positions for acquireWakeLockInternal on Android ${Build.VERSION.SDK_INT}")
                    XpUtil.log("Expected index: $androidVersionIndex, Actual index: $index")
                    return
                }
            }
            
            // If all strategies failed, mark as failed
            acquireWakeLockHookFailed = true
            XpUtil.log("All acquireWakeLockInternal parameter extraction strategies failed")
        }

        /**
         * Try to extract parameters using specific positions
         */
        private fun tryExtractWithPositions(
            param: XC_MethodHook.MethodHookParam,
            positions: WakeLockParamPositions
        ): WakeLockParamPositions? {
            val args = param.args

            // First try positions as-is
            if (tryExtractWithExactPositions(param, positions)) {
                return positions
            }

            // Samsung/OneUI Nest accessor: static method adds a leading PowerManagerService parameter.
            // Example (A16): m773$$Nest$macquireWakeLockInternal(PMS, IBinder, int, int, String, String, ..., int uid, ...)
            if (shouldShiftForNestAccessor(args)) {
                val shifted = WakeLockParamPositions(
                    positions.lockPos + 1,
                    positions.tagPos + 1,
                    positions.packagePos + 1,
                    positions.uidPos + 1
                )
                if (tryExtractWithExactPositions(param, shifted)) {
                    return shifted
                }
            }

            return null
        }

        private fun shouldShiftForNestAccessor(args: Array<Any?>): Boolean {
            val first = args.firstOrNull() ?: return false
            return first.javaClass.name == "com.android.server.power.PowerManagerService"
        }

        private fun tryExtractWithExactPositions(
            param: XC_MethodHook.MethodHookParam,
            positions: WakeLockParamPositions
        ): Boolean {
            val args = param.args

            // Check if positions are valid for this args array
            if (args.size <= maxOf(
                    positions.lockPos,
                    positions.tagPos,
                    positions.packagePos,
                    positions.uidPos
                )
            ) {
                return false
            }

            try {
                // Extract parameters
                val lock = args[positions.lockPos] as? IBinder
                val wN = args[positions.tagPos] as? String
                val pN = args[positions.packagePos] as? String
                val uid = args[positions.uidPos] as? Int

                // Validate parameters
                if (lock != null && wN != null && pN != null && uid != null) {
                    // Parameters are valid, process the wakelock
                    val context = AndroidAppHelper.currentApplication()
                    handleWakeLockAcquire(param, pN, wN, uid, lock, context)
                    return true
                }
            } catch (e: Exception) {
                // This positions strategy failed
                return false
            }

            return false
        }

        // Original methods preserved below
        private fun wakelockTest(lpparam: XC_LoadPackage.LoadPackageParam) {
            // if no debug enable
            if (!XpNSP.getInstance().getDebug())
                return

            val tmp: Class<*>? =
                XpUtil.getClass("com.android.server.power.PowerManagerService", lpparam.classLoader)

            tmp?.let {
                XposedBridge.hookAllMethods(
                    it, "acquireWakeLockInternal",
                    object : XC_MethodHook() {
                        @Throws(Throwable::class)
                        override fun beforeHookedMethod(param: MethodHookParam) {
                            XpUtil.log("acquireWakeLockInternal param size: ${param.args.size}")
                            try {
                                val lock = param.args[0] as IBinder
                                val wN = param.args[3] as String
                                val pN = param.args[4] as String
                                val uid = param.args[7] as Int
                                XpUtil.log("Android S: $lock $wN $pN $uid")
                            } catch (e: Exception) {
                                XpUtil.log("${e.message}")
                            }

                            try {
                                val lock = param.args[0] as IBinder
                                val wN = param.args[2] as String
                                val pN = param.args[3] as String
                                val uid = param.args[6] as Int
                                XpUtil.log("Android  bR: $lock $wN $pN $uid")
                            } catch (e: Exception) {
                                XpUtil.log("${e.message}")
                            }
                        }
                    }
                )
            }
        }

        private fun wakeLockHook31(lpparam: XC_LoadPackage.LoadPackageParam) {
            //https://cs.android.com/android/platform/superproject/+/android-12.1.0_r8:frameworks/base/services/core/java/com/android/server/power/PowerManagerService.java?hl=zh-cn
            //private void acquireWakeLockInternal(IBinder lock, int displayId, int flags, String tag,
            //         String packageName, WorkSource ws, String historyTag, int uid, int pid)
//            XposedHelpers.findAndHookMethod("com.android.server.power.PowerManagerService",
//                lpparam.classLoader,
//                "acquireWakeLockInternal",
//                IBinder::class.java,
//                Int::class.javaPrimitiveType, Int::class.javaPrimitiveType,
//                String::class.java, String::class.java,//wakeLockName packageName
//                WorkSource::class.java,
//                String::class.java, Int::class.javaPrimitiveType, Int::class.javaPrimitiveType,
//                object : XC_MethodHook() {
//                    @Throws(Throwable::class)
//                    override fun beforeHookedMethod(param: MethodHookParam) {
//
//                        val lock = param.args[0] as IBinder
//                        val wN = param.args[3] as String
//                        val pN = param.args[4] as String
//                        val uid = param.args[7] as Int
//                        val context =
//                            XposedHelpers.getObjectField(param.thisObject, "mContext") as Context
//
//                        handleWakeLockAcquire(param, pN, wN, uid, lock, context)
//                    }
//                })

            val tmp: Class<*>? =
                XpUtil.getClass("com.android.server.power.PowerManagerService", lpparam.classLoader)

            tmp?.let {
                XposedBridge.hookAllMethods(
                    it, "acquireWakeLockInternal",
                    object : XC_MethodHook() {
                        @Throws(Throwable::class)
                        override fun beforeHookedMethod(param: MethodHookParam) {

                            try {
                                val lock = param.args[0] as IBinder
                                val wN = param.args[3] as String
                                val pN = param.args[4] as String
                                val uid = param.args[7] as Int

                                val context = AndroidAppHelper.currentApplication()
                                handleWakeLockAcquire(param, pN, wN, uid, lock, context)
                            } catch (e: Exception) {
                                XpUtil.log("${e.message}")
                            }
                        }
                    }
                )
            }

            XposedHelpers.findAndHookMethod(
                "com.android.server.power.PowerManagerService",
                lpparam.classLoader,
                "releaseWakeLockInternal",
                IBinder::class.java,
                Int::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    @Throws(Throwable::class)
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val context = AndroidAppHelper.currentApplication()
                        val lock = param.args[0] as IBinder
                        handleWakeLockRelease(lock, context)
                    }
                })
        }

        private fun wakeLockHook24to30(lpparam: XC_LoadPackage.LoadPackageParam) {
            //https://cs.android.com/android/platform/superproject/+/android-11.0.0_r1:frameworks/base/services/core/java/com/android/server/power/PowerManagerService.java
//            XposedHelpers.findAndHookMethod("com.android.server.power.PowerManagerService",
//                lpparam.classLoader,
//                "acquireWakeLockInternal",
//                IBinder::class.java, Int::class.javaPrimitiveType,
//                String::class.java, String::class.java,////wakeLockName packageName
//                WorkSource::class.java, String::class.java,
//                Int::class.javaPrimitiveType, Int::class.javaPrimitiveType,
//                object : XC_MethodHook() {
//                    @Throws(Throwable::class)
//                    override fun beforeHookedMethod(param: MethodHookParam) {
//
//                        val lock = param.args[0] as IBinder
////                        val flags = param.args[1] as Int
//                        val wN = param.args[2] as String
//                        val pN = param.args[3] as String
////                        val ws = param.args[4] as WorkSource?
////                        val historyTag = param.args[5] as String
//                        val uid = param.args[6] as Int
////                        val pid = param.args[7] as Int
//                        val context =
//                            XposedHelpers.getObjectField(param.thisObject, "mContext") as Context
//
//                        handleWakeLockAcquire(param, pN, wN, uid, lock, context)
//                    }
//                })

            val tmp: Class<*>? =
                XpUtil.getClass("com.android.server.power.PowerManagerService", lpparam.classLoader)

            tmp?.let {
                XposedBridge.hookAllMethods(
                    it, "acquireWakeLockInternal",
                    object : XC_MethodHook() {
                        @Throws(Throwable::class)
                        override fun beforeHookedMethod(param: MethodHookParam) {
                            try {
                                val lock = param.args[0] as IBinder
                                val wN = param.args[2] as String
                                val pN = param.args[3] as String
                                val uid = param.args[6] as Int

                                val context: Context =
                                    AndroidAppHelper.currentApplication().applicationContext
                                handleWakeLockAcquire(param, pN, wN, uid, lock, context)
                            } catch (e: Exception) {
                                XpUtil.log("${e.message}")
                            }
                        }
                    }
                )
            }

            XposedHelpers.findAndHookMethod(
                "com.android.server.power.PowerManagerService",
                lpparam.classLoader,
                "releaseWakeLockInternal",
                IBinder::class.java,
                Int::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    @Throws(Throwable::class)
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val context: Context =
                            AndroidAppHelper.currentApplication().applicationContext
                        val lock = param.args[0] as IBinder
                        handleWakeLockRelease(lock, context)
                    }
                })
        }

        /**
         * get instance id
         */
        private fun generateInstanceId(lock: IBinder, timestamp: Long): String {
            val iBinderHash = System.identityHashCode(lock).toString(16) // get iBinder hash
            return InfoEvent.generateInstanceId(iBinderHash, timestamp)
        }

        internal fun lastAllowKey(name: String, packageName: String, userId: Int): String {
            return "$name|$packageName|$userId"
        }

        internal fun getLastAllowTime(
            lastAllowTimes: Map<String, Long>,
            name: String,
            packageName: String,
            userId: Int
        ): Long {
            return lastAllowTimes[lastAllowKey(name, packageName, userId)] ?: 0L
        }

        internal fun recordLastAllowTime(
            lastAllowTimes: MutableMap<String, Long>,
            name: String,
            packageName: String,
            userId: Int,
            now: Long
        ) {
            lastAllowTimes[lastAllowKey(name, packageName, userId)] = now
        }

        // PROTECTED - DO NOT MODIFY
        // handle wakelock acquire
        private fun handleWakeLockAcquire(
            param: XC_MethodHook.MethodHookParam,
            pN: String, wN: String, uid: Int,
            lock: IBinder, context: Context
        ) {
            val userId = getUserId(uid)
            val now = SystemClock.elapsedRealtime()
            val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            val instanceId = generateInstanceId(lock, now)
            val isBlocked =
                block(
                    wN,
                    pN,
                    userId,
                    getLastAllowTime(lastAllowTime, wN, pN, userId),
                    now,
                    booted && !pm.isInteractive
                )

            if (isBlocked) {
                XpUtil.log("$pN wakeLock:$wN block '${pm.isInteractive}' '$booted'")
                param.result = null
                // Record blocked event
                XpRecord.blockEvent(wN, pN, Type.Wakelock, context, userId, now, instanceId)
                return
            }

            // Allow wakelock
            recordLastAllowTime(lastAllowTime, wN, pN, userId, now)
            wlTs[lock] = WLT(
                wakelockName = wN, packageName = pN, userId = userId,
                startTime = now, instanceId = instanceId
            )
            // new event
            XpRecord.newEvent(
                name = wN,
                packageName = pN,
                type = Type.Wakelock,
                context = context,
                userId = userId,
                startTime = now,
                instanceId = instanceId
            )

//            XpUtil.log("$pN wakeLock:$wN 新实例创建 instanceId=$instanceId")
        }

        /**
         * Handles the release of a wakelock
         *
         * @param lock The IBinder object representing the wakelock
         * @param context Application context
         */
        private fun handleWakeLockRelease(lock: IBinder, context: Context) {
            // Get current time and wakelock tracking object
            val wlT = wlTs[lock] ?: return
            val now = SystemClock.elapsedRealtime()
            if (wlT.instanceId.isNotEmpty()) {
                wlT.instanceId = generateInstanceId(lock, wlT.startTime)
            }

            XpRecord.endEvent(
                name = wlT.wakelockName,
                packageName = wlT.packageName,
                type = type,
                context = context,
                userId = wlT.userId,
                startTime = wlT.startTime,
                endTime = now,
                instanceId = wlT.instanceId
            )

            // Remove the wakelock from tracking
            wlTs.remove(lock)

//            XpUtil.log("${wlT.packageName} wakeLock:${wlT.wakelockName} 实例已移除 instanceId=${wlT.instanceId}")
        }

        // PROTECTED - DO NOT MODIFY
        // get wakelock should block or not
        private fun block(
            wN: String, packageName: String, userId: Int,
            lastActive: Long, now: Long, isLocked: Boolean
        ): Boolean {
            val xpNSP = XpNSP.getInstance()
            return xpNSP.flag(wN, packageName, type, userId)
                    || isLocked && xpNSP.flagLock(wN, packageName, type, userId)
                    || xpNSP.aTI(now, lastActive, wN, packageName, type, userId)
                    || xpNSP.rE(wN, packageName, type, userId)
        }
    }

    data class WLT(
        val wakelockName: String,
        val packageName: String,
        var userId: Int = 0,
        var startTime: Long = 0,
        var instanceId: String = "",  // IBinder hash
    )
}
