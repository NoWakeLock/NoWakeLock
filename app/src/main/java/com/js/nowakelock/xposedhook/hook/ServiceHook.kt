package com.js.nowakelock.xposedhook.hook

import android.app.AndroidAppHelper
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import com.js.nowakelock.data.db.Type
import com.js.nowakelock.xposedhook.XpUtil
import com.js.nowakelock.xposedhook.model.XpNSP
import com.js.nowakelock.xposedhook.model.XpRecord
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.XposedHelpers.findClass
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.lang.reflect.Method
import com.js.nowakelock.xposedhook.hook.findMethod
import com.js.nowakelock.xposedhook.hook.MethodCondition
import java.util.concurrent.atomic.AtomicReference

class ServiceHook {
    companion object {

        private val type = Type.Service
        var booted = false

        // Data class to store valid parameter positions
        private data class ServiceParamPositions(
            val servicePos: Int,      // Position of Intent parameter
            val packagePos: Int,      // Position of callingPackage parameter
            val userIdPos: Int        // Position of userId parameter
        )

        // Cache for parameter positions using AtomicReference for thread safety
        @Volatile
        private var startServicePositionsRef: AtomicReference<ServiceParamPositions?> =
            AtomicReference(null)

        @Volatile
        private var bindServicePositionsRef: AtomicReference<ServiceParamPositions?> =
            AtomicReference(null)

        // Flags to indicate if all extraction attempts have failed
        @Volatile
        private var startServiceHookFailed = false

        @Volatile
        private var bindServiceHookFailed = false

        // Predefined parameter positions for different Android versions
        private val startServicePositionStrategies = listOf(
            // Android 14-15 (API 34-35)
            ServiceParamPositions(1, 6, 8),
            // Android 12-13 (API 31-33) 
            ServiceParamPositions(1, 6, 8),
            // Android 11 (API 30)
            ServiceParamPositions(1, 6, 8),
            // Android 10 (API 29)
            ServiceParamPositions(1, 6, 7),
            // Android 8-9 (API 26-28)
            ServiceParamPositions(1, 6, 7),
            // Android 7 (API 24-25)
            ServiceParamPositions(1, 5, 6)
        )

        // Predefined parameter positions for bindServiceLocked
        private val bindServicePositionStrategies = listOf(
            // Android 14+ (API 34+)
            ServiceParamPositions(2, 11, 12),
            // Android 12-13 (API 31-33)
            ServiceParamPositions(2, 7, 8),
            // Older versions
            ServiceParamPositions(2, 7, 8)
        )

        fun hookService(lpparam: XC_LoadPackage.LoadPackageParam) {
            XpUtil.log("Hooking Service ${Build.VERSION.SDK_INT}")

            // Use unified hook approach for all Android versions
            unifiedServiceHook(lpparam)
        }

        /**
         * Unified service hook approach that works across all Android versions
         */
        private fun unifiedServiceHook(lpparam: XC_LoadPackage.LoadPackageParam) {
            try {
                // Get the ActiveServices class
                val activeServicesClass =
                    findClass("com.android.server.am.ActiveServices", lpparam.classLoader)

                // Hook startServiceLocked methods
                hookStartServiceMethods(activeServicesClass, lpparam)

                // Hook bindServiceLocked methods
                hookBindServiceMethods(activeServicesClass, lpparam)
            } catch (e: Throwable) {
                XpUtil.log("Error in unified service hook: ${e.message}")
                e.printStackTrace()
            }
        }

        /**
         * Hook all startServiceLocked methods
         */
        private fun hookStartServiceMethods(
            activeServicesClass: Class<*>,
            lpparam: XC_LoadPackage.LoadPackageParam
        ) {
            try {
                // Find all methods named startServiceLocked
                val methods =
                    activeServicesClass.declaredMethods.filter { it.name == "startServiceLocked" }

                XpUtil.log("Found ${methods.size} startServiceLocked methods")

                if (methods.isEmpty()) {
                    XpUtil.log("No startServiceLocked methods found!")
                    return
                }

                // Hook each method found
                for (method in methods) {
                    hookStartServiceLockedMethod(method, lpparam)
                }
            } catch (e: Throwable) {
                XpUtil.log("Error hooking startServiceLocked methods: ${e.message}")
                e.printStackTrace()
            }
        }

        /**
         * Hook all bindServiceLocked methods
         */
        private fun hookBindServiceMethods(
            activeServicesClass: Class<*>,
            lpparam: XC_LoadPackage.LoadPackageParam
        ) {
            try {
                // Find all methods named bindServiceLocked
                val methods =
                    activeServicesClass.declaredMethods.filter { it.name == "bindServiceLocked" }

                XpUtil.log("Found ${methods.size} bindServiceLocked methods")

                if (methods.isEmpty()) {
                    XpUtil.log("No bindServiceLocked methods found!")
                    return
                }

                // Hook each method found
                for (method in methods) {
                    hookBindServiceLockedMethod(method, lpparam)
                }
            } catch (e: Throwable) {
                XpUtil.log("Error hooking bindServiceLocked methods: ${e.message}")
                e.printStackTrace()
            }
        }

        /**
         * Hook a specific startServiceLocked method with parameter caching
         */
        private fun hookStartServiceLockedMethod(
            method: Method,
            lpparam: XC_LoadPackage.LoadPackageParam
        ) {
            XpUtil.log("Hooking startServiceLocked method with signature: ${method.parameterTypes.joinToString()}")

            XposedBridge.hookMethod(method, object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun beforeHookedMethod(param: MethodHookParam) {
                    try {
                        // Check if we have cached positions
                        val positions = startServicePositionsRef.get()

                        if (positions != null) {
                            if(XpNSP.getInstance().getDebug()){
                                XpUtil.log("Using cached positions for startServiceLocked on Android ${Build.VERSION.SDK_INT}")
                            }
                            // Use cached positions to extract parameters
                            extractParametersFromCache(param, positions)
                        } else if (!startServiceHookFailed) {
                            if (XpNSP.getInstance().getDebug()){
                                XpUtil.log("No cached positions for startServiceLocked, trying to extract parameters")
                            }
                            // Try to extract parameters using strategies
                            extractAndCacheStartServiceParameters(param)
                        }
                    } catch (e: Exception) {
                        XpUtil.log("Error in startServiceLocked hook callback: ${e.message}")
                        e.printStackTrace()
                    }
                }
            })
        }

        /**
         * Hook a specific bindServiceLocked method with parameter caching
         */
        private fun hookBindServiceLockedMethod(
            method: Method,
            lpparam: XC_LoadPackage.LoadPackageParam
        ) {
            XpUtil.log("Hooking bindServiceLocked method with signature: ${method.parameterTypes.joinToString()}")

            XposedBridge.hookMethod(method, object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun beforeHookedMethod(param: MethodHookParam) {
                    try {
                        // Check if we have cached positions
                        val positions = bindServicePositionsRef.get()

                        if (positions != null) {
                            if(XpNSP.getInstance().getDebug()){
                                XpUtil.log("Using cached positions for bindServiceLocked on Android ${Build.VERSION.SDK_INT}")
                            }
                            // Use cached positions to extract parameters
                            extractParametersFromCache(param, positions)
                        } else if (!bindServiceHookFailed) {
                            if (XpNSP.getInstance().getDebug()){
                                XpUtil.log("No cached positions for bindServiceLocked, trying to extract parameters")
                            }
                            // Try to extract parameters using strategies
                            extractAndCacheBindServiceParameters(param)
                        }
                    } catch (e: Exception) {
                        XpUtil.log("Error in bindServiceLocked hook callback: ${e.message}")
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
            positions: ServiceParamPositions
        ) {
            try {
                val args = param.args

                // Extract parameters using cached positions
                val service = args[positions.servicePos] as? Intent
                val callingPackage = args[positions.packagePos] as? String
                val userId = args[positions.userIdPos] as? Int

                // Process the service request if parameters are valid
                if (service != null && callingPackage != null && userId != null) {
                    val context: Context = AndroidAppHelper.currentApplication().applicationContext
                    hookStartServiceLocked(param, service, callingPackage, context, userId)
                }
            } catch (e: Exception) {
                XpUtil.log("Error extracting parameters from cache: ${e.message}")
            }
        }

        /**
         * Try to extract and cache parameters for startServiceLocked
         */
        private fun extractAndCacheStartServiceParameters(param: XC_MethodHook.MethodHookParam) {
            val args = param.args

            // First try the strategy for current Android version
            val androidVersionIndex = when (Build.VERSION.SDK_INT) {
                in Build.VERSION_CODES.UPSIDE_DOWN_CAKE..Int.MAX_VALUE -> 0 // Android 14+
                in Build.VERSION_CODES.S..Build.VERSION_CODES.TIRAMISU -> 1 // Android 12-13
                Build.VERSION_CODES.R -> 2 // Android 11
                Build.VERSION_CODES.Q -> 3 // Android 10
                in Build.VERSION_CODES.O..Build.VERSION_CODES.P -> 4 // Android 8-9
                in Build.VERSION_CODES.N..Build.VERSION_CODES.N_MR1 -> 5 // Android 7
                else -> 0 // Default to newest version strategy
            }

            // Try the expected strategy for current version first
            if (androidVersionIndex < startServicePositionStrategies.size) {
                val positions = startServicePositionStrategies[androidVersionIndex]
                if (tryExtractWithPositions(param, positions)) {
                    // Cache successful positions
                    startServicePositionsRef.set(positions)
                    XpUtil.log("Successfully extracted parameters for startServiceLocked on Android ${Build.VERSION.SDK_INT}")
                    return
                } else {
                    // Log that the expected strategy failed
                    XpUtil.log("Expected startServiceLocked parameter positions for Android ${Build.VERSION.SDK_INT} failed")
                }
            }

            // Try all strategies if the expected one failed
            XpUtil.log("Trying all strategies for startServiceLocked on Android ${Build.VERSION.SDK_INT}")
            for ((index, positions) in startServicePositionStrategies.withIndex()) {
                if (index != androidVersionIndex && tryExtractWithPositions(param, positions)) {
                    // Cache successful positions
                    startServicePositionsRef.set(positions)

                    // Log warning that we're using different positions than expected
                    XpUtil.log("Warning: Using unexpected parameter positions for startServiceLocked on Android ${Build.VERSION.SDK_INT}")
                    XpUtil.log("Expected index: $androidVersionIndex, Actual index: $index")
                    return
                }
            }

            // If all strategies failed, mark as failed
            startServiceHookFailed = true
            XpUtil.log("All startServiceLocked parameter extraction strategies failed")
        }

        /**
         * Try to extract and cache parameters for bindServiceLocked
         */
        private fun extractAndCacheBindServiceParameters(param: XC_MethodHook.MethodHookParam) {
            val args = param.args

            // First try the strategy for current Android version
            val androidVersionIndex = when (Build.VERSION.SDK_INT) {
                in Build.VERSION_CODES.UPSIDE_DOWN_CAKE..Int.MAX_VALUE -> 0 // Android 14+
                in Build.VERSION_CODES.S..Build.VERSION_CODES.TIRAMISU -> 1 // Android 12-13
                else -> 2 // Older versions
            }

            // Try the expected strategy for current version first
            if (androidVersionIndex < bindServicePositionStrategies.size) {
                val positions = bindServicePositionStrategies[androidVersionIndex]
                if (tryExtractWithPositions(param, positions)) {
                    // Cache successful positions
                    bindServicePositionsRef.set(positions)
                    XpUtil.log("Successfully extracted parameters for bindServiceLocked on Android ${Build.VERSION.SDK_INT}")
                    return
                } else {
                    // Log that the expected strategy failed
                    XpUtil.log("Expected bindServiceLocked parameter positions for Android ${Build.VERSION.SDK_INT} failed")
                }
            }

            // Try all strategies if the expected one failed
            XpUtil.log("Trying all strategies for bindServiceLocked on Android ${Build.VERSION.SDK_INT}")
            for ((index, positions) in bindServicePositionStrategies.withIndex()) {
                if (index != androidVersionIndex && tryExtractWithPositions(param, positions)) {
                    // Cache successful positions
                    bindServicePositionsRef.set(positions)

                    // Log warning that we're using different positions than expected
                    XpUtil.log("Warning: Using unexpected parameter positions for bindServiceLocked on Android ${Build.VERSION.SDK_INT}")
                    XpUtil.log("Expected index: $androidVersionIndex, Actual index: $index")
                    return
                }
            }

            // If all strategies failed, mark as failed
            bindServiceHookFailed = true
            XpUtil.log("All bindServiceLocked parameter extraction strategies failed")
        }

        /**
         * Try to extract parameters using specific positions
         */
        private fun tryExtractWithPositions(
            param: XC_MethodHook.MethodHookParam,
            positions: ServiceParamPositions
        ): Boolean {
            val args = param.args

            // Check if positions are valid for this args array
            if (args.size <= maxOf(
                    positions.servicePos,
                    positions.packagePos,
                    positions.userIdPos
                )
            ) {
                return false
            }

            try {
                // Extract parameters
                val service = args[positions.servicePos] as? Intent
                val callingPackage = args[positions.packagePos] as? String
                val userId = args[positions.userIdPos] as? Int

                // Validate parameters
                if (service != null && callingPackage != null && userId != null && userId >= 0 && userId <= 1000) {
                    // Parameters are valid, process the service request
                    val context: Context = AndroidAppHelper.currentApplication().applicationContext
                    hookStartServiceLocked(param, service, callingPackage, context, userId)
                    return true
                }
            } catch (e: Exception) {
                // This positions strategy failed
                return false
            }

            return false
        }

        private fun hookStartServiceLocked(
            param: XC_MethodHook.MethodHookParam,
            service: Intent?,
            packageName: String?,
            context: Context,
            userId: Int = 0
        ) {
            if (service == null || packageName == null) return
            val serviceName = service.component?.flattenToShortString() ?: return

//            XpUtil.log("$packageName service: $serviceName userid:$userId")
            val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            val block = block(serviceName, packageName, userId, booted and !pm.isInteractive)

            if (block) {
                param.result = null

                XpUtil.log("$packageName service: $serviceName block $booted ${pm.isInteractive}")
                XpRecord.blockEvent(
                    serviceName,
                    packageName,
                    type,
                    context,
                    userId
                )
            } else {
                XpRecord.newEvent(
                    serviceName,
                    packageName,
                    type,
                    context,
                    userId
                )
            }
        }

        private fun block(
            name: String,
            packageName: String,
            userId: Int,
            isLocked: Boolean
        ): Boolean {
            val xpNSP = XpNSP.getInstance()
            return xpNSP.flag(name, packageName, type, userId)
                    || isLocked && xpNSP.flag(name, packageName, type, userId)
        }
    }
}