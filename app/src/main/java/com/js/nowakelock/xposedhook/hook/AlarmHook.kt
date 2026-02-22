package com.js.nowakelock.xposedhook.hook

import android.app.AndroidAppHelper
import android.content.Context
import android.os.Build
import android.os.PowerManager
import android.os.SystemClock
import com.js.nowakelock.base.getUserId
import com.js.nowakelock.data.db.Type
import com.js.nowakelock.xposedhook.XpUtil
import com.js.nowakelock.xposedhook.model.XpNSP
import com.js.nowakelock.xposedhook.model.XpRecord
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.lang.reflect.Method
import java.util.*
import java.util.concurrent.atomic.AtomicReference


class AlarmHook {
    companion object {

        private val type = Type.Alarm
        var booted = false

        @Volatile
        private var lastAllowTime = HashMap<String, Long>()//last allow time

        // Data class to store valid parameter positions
        private data class AlarmParamPositions(
            val triggerListPos: Int      // Position of triggerList parameter
        )

        // Cache for parameter positions using AtomicReference for thread safety
        @Volatile
        private var alarmPositionsRef: AtomicReference<AlarmParamPositions?> =
            AtomicReference(null)

        // Flag to indicate if all extraction attempts have failed
        @Volatile
        private var alarmHookFailed = false

        // Predefined parameter positions for different Android versions
        private val alarmPositionStrategies = listOf(
            // Android 12+ (API 31+)
            AlarmParamPositions(0),
            // Android 10-11 (API 29-30)
            AlarmParamPositions(0),
            // Android 7-9 (API 24-28)
            AlarmParamPositions(0)
        )

        fun hookAlarm(lpparam: XC_LoadPackage.LoadPackageParam) {
            XpUtil.log("Hooking Alarm ${Build.VERSION.SDK_INT}")

            // Use unified hook approach for all Android versions
            unifiedAlarmHook(lpparam)
        }

        /**
         * Unified alarm hook approach that works across all Android versions
         */
        private fun unifiedAlarmHook(lpparam: XC_LoadPackage.LoadPackageParam) {
            try {
                // Get the AlarmManagerService class based on Android version
                val alarmManagerServiceClass = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    // Android 12+ (API 31+)
                    XposedHelpers.findClass("com.android.server.alarm.AlarmManagerService", lpparam.classLoader)
                } else {
                    // Android 11 and below (API <= 30)
                    XposedHelpers.findClass("com.android.server.AlarmManagerService", lpparam.classLoader)
                }

                // Hook triggerAlarmsLocked methods
                hookAlarmMethods(alarmManagerServiceClass, lpparam)
            } catch (e: Throwable) {
                XpUtil.log("Error in unified alarm hook: ${e.message}")
                e.printStackTrace()
            }
        }

        /**
         * Hook all triggerAlarmsLocked methods
         */
        private fun hookAlarmMethods(
            alarmManagerServiceClass: Class<*>,
            lpparam: XC_LoadPackage.LoadPackageParam
        ) {
            try {
                // Find all methods named triggerAlarmsLocked
                val methods =
                    alarmManagerServiceClass.declaredMethods.filter { it.name == "triggerAlarmsLocked" }

                XpUtil.log("Found ${methods.size} triggerAlarmsLocked methods")

                if (methods.isEmpty()) {
                    XpUtil.log("No triggerAlarmsLocked methods found!")
                    return
                }

                // Hook each method found
                for (method in methods) {
                    hookAlarmMethod(method, lpparam)
                }
            } catch (e: Throwable) {
                XpUtil.log("Error hooking triggerAlarmsLocked methods: ${e.message}")
                e.printStackTrace()
            }
        }

        /**
         * Hook a specific triggerAlarmsLocked method with parameter caching
         */
        private fun hookAlarmMethod(
            method: Method,
            lpparam: XC_LoadPackage.LoadPackageParam
        ) {
            XpUtil.log("Hooking triggerAlarmsLocked method with signature: ${method.parameterTypes.joinToString()}")

            XposedBridge.hookMethod(method, object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun afterHookedMethod(param: MethodHookParam) {
                    try {
                        // Check if we have cached positions
                        val positions = alarmPositionsRef.get()

                        if (positions != null) {
                            if(XpNSP.getInstance().getDebug()){
                                XpUtil.log("Using cached positions for triggerAlarmsLocked on Android ${Build.VERSION.SDK_INT}")
                            }
                            // Use cached positions to extract parameters
                            extractParametersFromCache(param, positions)
                        } else if (!alarmHookFailed) {
                            if (XpNSP.getInstance().getDebug()){
                                XpUtil.log("No cached positions for triggerAlarmsLocked, trying to extract parameters")
                            }
                            // Try to extract parameters using strategies
                            extractAndCacheAlarmParameters(param)
                        }
                    } catch (e: Exception) {
                        XpUtil.log("Error in triggerAlarmsLocked hook callback: ${e.message}")
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
            positions: AlarmParamPositions
        ) {
            try {
                val args = param.args

                // Extract parameters using cached position
                val triggerList = args[positions.triggerListPos] as? ArrayList<*>

                // Process the alarm list if parameters are valid
                if (triggerList != null) {
                    val context: Context = AndroidAppHelper.currentApplication().applicationContext
                    hookAlarmsLocked(triggerList, context)
                }
            } catch (e: Exception) {
                XpUtil.log("Error extracting parameters from cache: ${e.message}")
            }
        }

        /**
         * Try to extract and cache parameters for triggerAlarmsLocked
         */
        private fun extractAndCacheAlarmParameters(param: XC_MethodHook.MethodHookParam) {
            val args = param.args

            // First try the strategy for current Android version
            val androidVersionIndex = when (Build.VERSION.SDK_INT) {
                in Build.VERSION_CODES.S..Int.MAX_VALUE -> 0 // Android 12+
                in Build.VERSION_CODES.Q..Build.VERSION_CODES.R -> 1 // Android 10-11
                in Build.VERSION_CODES.N..Build.VERSION_CODES.P -> 2 // Android 7-9
                else -> 0 // Default to newest version strategy
            }

            // Try the expected strategy for current version first
            if (androidVersionIndex < alarmPositionStrategies.size) {
                val positions = alarmPositionStrategies[androidVersionIndex]
                if (tryExtractWithPositions(param, positions)) {
                    // Cache successful positions
                    alarmPositionsRef.set(positions)
                    XpUtil.log("Successfully extracted parameters for triggerAlarmsLocked on Android ${Build.VERSION.SDK_INT}")
                    return
                } else {
                    // Log that the expected strategy failed
                    XpUtil.log("Expected triggerAlarmsLocked parameter positions for Android ${Build.VERSION.SDK_INT} failed")
                }
            }

            // Try all strategies if the expected one failed
            XpUtil.log("Trying all strategies for triggerAlarmsLocked on Android ${Build.VERSION.SDK_INT}")
            for ((index, positions) in alarmPositionStrategies.withIndex()) {
                if (index != androidVersionIndex && tryExtractWithPositions(param, positions)) {
                    // Cache successful positions
                    alarmPositionsRef.set(positions)

                    // Log warning that we're using different positions than expected
                    XpUtil.log("Warning: Using unexpected parameter positions for triggerAlarmsLocked on Android ${Build.VERSION.SDK_INT}")
                    XpUtil.log("Expected index: $androidVersionIndex, Actual index: $index")
                    return
                }
            }

            // OEM fallback (Samsung/OneUI etc): triggerAlarmsLocked(long nowElapsed, ArrayList triggerList)
            // If we couldn't match any hardcoded strategies, try to locate the ArrayList argument at runtime.
            val detectedPos = detectTriggerListPos(args)
            if (detectedPos != null) {
                val positions = AlarmParamPositions(detectedPos)
                if (tryExtractWithPositions(param, positions)) {
                    alarmPositionsRef.set(positions)
                    XpUtil.log("Detected triggerAlarmsLocked triggerListPos=$detectedPos on Android ${Build.VERSION.SDK_INT}")
                    return
                }
            }

            // If all strategies failed, mark as failed
            alarmHookFailed = true
            XpUtil.log("All triggerAlarmsLocked parameter extraction strategies failed")
        }

        private fun detectTriggerListPos(args: Array<Any?>): Int? {
            // Pass 1: prefer a non-empty list that looks like an Alarm list.
            for (i in args.indices) {
                val list = args[i] as? ArrayList<*> ?: continue
                val first = list.firstOrNull { it != null } ?: continue
                if (looksLikeAlarm(first)) {
                    return i
                }
            }

            // Pass 2: fall back to the first ArrayList position (covers empty triggerList case).
            for (i in args.indices) {
                if (args[i] is ArrayList<*>) {
                    return i
                }
            }
            return null
        }

        private fun looksLikeAlarm(obj: Any): Boolean {
            return try {
                obj.javaClass.getDeclaredField("statsTag")
                obj.javaClass.getDeclaredField("packageName")
                obj.javaClass.getDeclaredField("uid")
                true
            } catch (_: Throwable) {
                false
            }
        }

        /**
         * Try to extract parameters using specific positions
         */
        private fun tryExtractWithPositions(
            param: XC_MethodHook.MethodHookParam,
            positions: AlarmParamPositions
        ): Boolean {
            val args = param.args

            // Check if positions are valid for this args array
            if (args.size <= positions.triggerListPos) {
                return false
            }

            try {
                // Extract parameters
                val triggerList = args[positions.triggerListPos] as? ArrayList<*>

                // Validate parameters
                if (triggerList != null) {
                    // Parameters are valid, process the alarm list
                    val context: Context = AndroidAppHelper.currentApplication().applicationContext
                    hookAlarmsLocked(triggerList, context)
                    return true
                }
            } catch (e: Exception) {
                // This positions strategy failed
                return false
            }

            return false
        }

        // handle alarm
        private fun hookAlarmsLocked(
            triggerList: ArrayList<*>,
            context: Context
        ) {
            // Process alarms in reverse order to safely remove items
            for (i in triggerList.size - 1 downTo 0) {
                val alarmInfo = extractAlarmInfo(triggerList[i]) ?: continue
                val (alarmName, packageName, uid) = alarmInfo
                
                val userId = getUserId(uid)
//                XpUtil.log("$packageName alarm: $alarmName uid:$uid userid:$userId")

                val now = SystemClock.elapsedRealtime()
                val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                val isScreenOff = booted and !pm.isInteractive
                
                // Determine if alarm should be blocked
                val shouldBlock = block(
                    alarmName,
                    packageName,
                    userId,
                    lastAllowTime[alarmName] ?: 0,
                    now,
                    isScreenOff
                )

                if (shouldBlock) {
                    // Remove alarm from trigger list
                    triggerList.removeAt(i)
                    XpUtil.log("$packageName alarm: $alarmName block $booted ${pm.isInteractive}")
                    
                    // Record blocked event
                    XpRecord.blockEvent(
                        alarmName, packageName, type,
                        context, userId
                    )
                } else {
                    // Update last allowed time and record event
                    lastAllowTime[alarmName] = now
                    XpRecord.newEvent(
                        alarmName, packageName, type,
                        context, userId
                    )
                }
            }
        }
        
        private fun extractAlarmInfo(alarm: Any?): Triple<String, String, Int>? {
            return try {
                val statsTag = alarm?.javaClass?.getDeclaredField("statsTag")?.get(alarm) as String
                val alarmName = statsTag.replace(Regex("\\*.*\\*:"), "")
                val packageName = alarm.javaClass.getDeclaredField("packageName").get(alarm) as String
                val uid = alarm.javaClass.getDeclaredField("uid").get(alarm) as Int
                
                Triple(alarmName, packageName, uid)
            } catch (e: Exception) {
                XpUtil.log(" alarm: hookAlarmsLocked err:$e")
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
            val xpNSP = XpNSP.getInstance()
            return xpNSP.flag(name, packageName, type, userId)
                    || isLocked && xpNSP.flagLock(name, packageName, type, userId)
                    || xpNSP.aTI(now, lastActive, name, packageName, type, userId)
                    || xpNSP.rE(name, packageName, type, userId)
        }
    }
}
