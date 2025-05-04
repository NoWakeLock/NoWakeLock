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

class ServiceHook {
    companion object {

        private val type = Type.Service
        var booted = false

        fun hookService(lpparam: XC_LoadPackage.LoadPackageParam) {

            XpUtil.log("Hooking Service ${Build.VERSION.SDK_INT}")

            when (Build.VERSION.SDK_INT) {
                //Try for service hooks for API levels > 35 (Android 16+)
                in (Build.VERSION_CODES.VANILLA_ICE_CREAM)..Int.MAX_VALUE -> {
                    XpUtil.log("Using flexible hook for Android 16+")
                    flexibleServiceHook(lpparam)
                }
                //Try for service hooks for API levels 34 ~ 35 (U), Android 14 15
//                Build.VERSION_CODES.VANILLA_ICE_CREAM -> serviceHook34to35(lpparam)
                Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> serviceHook34to35(lpparam)
                //Try for service hooks for API 31 ~ 33 (T), Android 12 13
                Build.VERSION_CODES.TIRAMISU -> serviceHook31to33(lpparam)
                Build.VERSION_CODES.S_V2 -> serviceHook31to33(lpparam)
                Build.VERSION_CODES.S -> serviceHook31to33(lpparam)
                //Try for service hooks for API levels = 30 (R)
                Build.VERSION_CODES.R -> serviceHook30(lpparam)
                //Try for service hooks for API levels = 29 (Q)
                Build.VERSION_CODES.Q -> serviceHook29(lpparam)
                //Try for service hooks for API levels 26 ~ 28 (O ~ P)
                in Build.VERSION_CODES.O..Build.VERSION_CODES.P -> serviceHook26to28(lpparam)
                //Try for service hooks for API levels 24 ~ 25 (N)
                in Build.VERSION_CODES.N..Build.VERSION_CODES.N_MR1 -> serviceHook24to25(lpparam)
                //For unknown versions, try flexible hook as fallback
                else -> {
                    XpUtil.log("Unknown Android version, trying flexible hook as fallback")
                    flexibleServiceHook(lpparam)
                }
            }
        }

        /**
         * Flexible hook approach that hooks all methods named "startServiceLocked"
         * and extracts parameters based on common positions observed across Android versions.
         */
        private fun flexibleServiceHook(lpparam: XC_LoadPackage.LoadPackageParam) {
            try {
                // Get the ActiveServices class
                val activeServicesClass = findClass("com.android.server.am.ActiveServices", lpparam.classLoader)
                
                // Find all methods named startServiceLocked
                val methods = activeServicesClass.declaredMethods.filter { it.name == "startServiceLocked" }
                
                XpUtil.log("Found ${methods.size} startServiceLocked methods")
                
                if (methods.isEmpty()) {
                    XpUtil.log("No startServiceLocked methods found! Trying to discover methods...")
                    discoverPotentialServiceMethods(activeServicesClass)
                    return
                }
                
                // Hook each method found
                for (method in methods) {
                    hookStartServiceLockedMethod(method, lpparam)
                }
            } catch (e: Throwable) {
                XpUtil.log("Error in flexible hook: ${e.message}")
                e.printStackTrace()
            }
        }

        /**
         * Hooks a specific startServiceLocked method and tries to extract parameters
         */
        private fun hookStartServiceLockedMethod(method: Method, lpparam: XC_LoadPackage.LoadPackageParam) {
            XpUtil.log("Hooking method with signature: ${method.parameterTypes.joinToString()}")
            
            XposedBridge.hookMethod(method, object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun beforeHookedMethod(param: MethodHookParam) {
                    try {
                        // Extract parameters with adaptive strategy
                        val extractionResult = tryExtractParameters(param.args)
                        
                        if (extractionResult != null) {
                            val (service, callingPackage, userId) = extractionResult
                            val context: Context = AndroidAppHelper.currentApplication().applicationContext
                            
                            XpUtil.log("Successfully extracted parameters: service=${service.component}, " +
                                       "package=$callingPackage, userId=$userId")
                            
                            hookStartServiceLocked(param, service, callingPackage, context, userId)
                        } else {
                            XpUtil.log("Failed to extract required parameters from args: ${param.args.joinToString { it?.javaClass?.simpleName ?: "null" }}")
                        }
                    } catch (e: Exception) {
                        XpUtil.log("Error in hook callback: ${e.message}")
                        e.printStackTrace()
                    }
                }
            })
        }

        /**
         * Attempts to extract the required parameters using various strategies
         */
        private fun tryExtractParameters(args: Array<Any?>): Triple<Intent, String, Int>? {
            // Common parameter positions observed across Android versions
            val strategies = listOf(
                Triple(1, 6, 8),  // Common in recent versions
                Triple(1, 6, 7),  // Common in some older versions
                Triple(1, 5, 6)   // Common in even older versions
            )
            
            // Try each position strategy
            for ((servicePos, packagePos, userIdPos) in strategies) {
                if (args.size > maxOf(servicePos, packagePos, userIdPos)) {
                    try {
                        val service = args[servicePos] as? Intent
                        val callingPackage = args[packagePos] as? String
                        val userId = args[userIdPos] as? Int
                        
                        if (service != null && callingPackage != null && userId != null) {
                            XpUtil.log("Successfully extracted parameters using positions: $servicePos, $packagePos, $userIdPos")
                            return Triple(service, callingPackage, userId)
                        }
                    } catch (e: Exception) {
                        // This strategy failed, try the next one
                        continue
                    }
                }
            }
            
            // If position-based strategies failed, try type-based extraction
            return tryExtractParametersByType(args)
        }

        /**
         * Attempts to extract parameters based on their types
         */
        private fun tryExtractParametersByType(args: Array<Any?>): Triple<Intent, String, Int>? {
            var service: Intent? = null
            var callingPackage: String? = null
            var userId: Int? = null
            var stringParams = mutableListOf<Pair<Int, String>>()
            var intParams = mutableListOf<Pair<Int, Int>>()
            
            // First pass: identify all parameters by type
            for (i in args.indices) {
                when (val arg = args[i]) {
                    is Intent -> service = arg
                    is String -> stringParams.add(Pair(i, arg))
                    is Int -> intParams.add(Pair(i, arg))
                }
            }
            
            // If we have an Intent, look for the likely package name and userId
            if (service != null) {
                // For package name: typically it's after the 5th parameter
                // and is one of the first few Strings after the Intent
                val packageCandidates = stringParams.filter { it.first > 3 }
                if (packageCandidates.isNotEmpty()) {
                    // Take the first or second String after initial params, depending on API pattern
                    callingPackage = if (packageCandidates.size > 1) packageCandidates[1].second else packageCandidates[0].second
                }
                
                // For userId: typically it's an Int parameter after the 6th position
                val userIdCandidates = intParams.filter { it.first > 5 }
                if (userIdCandidates.isNotEmpty()) {
                    // Usually the first Int that could represent a userId (small positive number)
                    userId = userIdCandidates.firstOrNull { it.second in 0..1000 }?.second
                        ?: userIdCandidates[0].second
                }
            }
            
            return if (service != null && callingPackage != null && userId != null) {
                XpUtil.log("Extracted parameters by type analysis")
                Triple(service, callingPackage, userId)
            } else {
                null
            }
        }

        /**
         * Discovers potential service-related methods when startServiceLocked cannot be found
         */
        private fun discoverPotentialServiceMethods(activeServicesClass: Class<*>) {
            val serviceMethods = activeServicesClass.declaredMethods.filter { 
                it.name.contains("service", ignoreCase = true) ||
                it.name.contains("Service", ignoreCase = true)
            }
            
            XpUtil.log("Potential service methods: ${serviceMethods.joinToString { it.name }}")
            
            // Look specifically for methods that might be renamed versions of startServiceLocked
            val potentialStartMethods = serviceMethods.filter {
                it.name.contains("start", ignoreCase = true) && 
                it.parameterTypes.any { param -> param == Intent::class.java }
            }
            
            if (potentialStartMethods.isNotEmpty()) {
                XpUtil.log("Potential start service methods: ${potentialStartMethods.joinToString { 
                    "${it.name}(${it.parameterTypes.joinToString { param -> param.simpleName }})" 
                }}")
            }
        }

        private fun serviceHook(lpparam: XC_LoadPackage.LoadPackageParam) {
            findMethod(
                findClass(
                    "com.android.server.am.ActiveServices",
                    lpparam.classLoader
                )
            ) { name == "bindServiceLocked" }
                .hookBefore {
                    val service = it.args[2] as Intent?
                    val callingPackage = it.args[11] as String
                    val userId: Int = it.args[12] as Int
//                    XpUtil.log("bindServiceLocked " + service + " " + callingPackage + " " + userId)
                    val context: Context =
                        AndroidAppHelper.currentApplication().applicationContext
                    hookStartServiceLocked(it, service, callingPackage, context, userId)
                }
        }

        // android 14 15
        // https://cs.android.com/android/platform/superproject/+/android-14.0.0_r59:frameworks/base/services/core/java/com/android/server/am/ActiveServices.java;l=909
        private fun serviceHook34to35(lpparam: XC_LoadPackage.LoadPackageParam) {
            XposedHelpers.findAndHookMethod(
                "com.android.server.am.ActiveServices",
                lpparam.classLoader,
                "startServiceLocked",
                "android.app.IApplicationThread",
                Intent::class.java,//service
                String::class.java,//resolvedType
                Int::class.javaPrimitiveType,//callingPid
                Int::class.javaPrimitiveType,//callingUid
                Boolean::class.java,//fgRequired
                String::class.java,//callingPackage
                String::class.java,//callingFeatureId
                Int::class.javaPrimitiveType,//userId
                Int::class.javaPrimitiveType,//sdkSandboxClientAppUid
                String::class.java,//sdkSandboxClientAppPackage
                String::class.java,//instanceName
                object : XC_MethodHook() {
                    @Throws(Throwable::class)
                    override fun beforeHookedMethod(param: MethodHookParam) {
//                        XpUtil.log("serviceHook31to32")
                        val service = param.args[1] as Intent?
                        val callingPackage = param.args[6] as String
                        val userId: Int = param.args[8] as Int
                        val context: Context =
                            AndroidAppHelper.currentApplication().applicationContext
                        hookStartServiceLocked(param, service, callingPackage, context, userId)
                    }
                })
        }

        // android 12-13
        // https://cs.android.com/android/platform/superproject/+/android-12.1.0_r27:frameworks/base/services/core/java/com/android/server/am/ActiveServices.java;l=621
        private fun serviceHook31to33(lpparam: XC_LoadPackage.LoadPackageParam) {

            XposedHelpers.findAndHookMethod(
                "com.android.server.am.ActiveServices",
                lpparam.classLoader,
                "startServiceLocked",
                "android.app.IApplicationThread",
                Intent::class.java,//service
                String::class.java,//resolvedType
                Int::class.javaPrimitiveType,//callingPid
                Int::class.javaPrimitiveType,//callingUid
                Boolean::class.java,//fgRequired
                String::class.java,//callingPackage
                String::class.java,//callingFeatureId
                Int::class.javaPrimitiveType,//userId
                Boolean::class.java,//allowBackgroundActivityStarts
                IBinder::class.java,
                object : XC_MethodHook() {
                    @Throws(Throwable::class)
                    override fun beforeHookedMethod(param: MethodHookParam) {
//                        XpUtil.log("serviceHook31to32")
                        val service = param.args[1] as Intent?
                        val callingPackage = param.args[6] as String
                        val userId: Int = param.args[8] as Int
                        val context: Context =
                            AndroidAppHelper.currentApplication().applicationContext
                        hookStartServiceLocked(param, service, callingPackage, context, userId)
                    }
                })
        }

        //https://cs.android.com/android/platform/superproject/+/android-10.0.0_r1:frameworks/base/services/core/java/com/android/server/am/ActiveServices.java;l=408?q=ActiveServices&ss=android%2Fplatform%2Fsuperproject
        private fun serviceHook30(lpparam: XC_LoadPackage.LoadPackageParam) {
            XpUtil.log("Hooking Service for API levels 30")

            XposedHelpers.findAndHookMethod(
                "com.android.server.am.ActiveServices",
                lpparam.classLoader,
                "startServiceLocked",
                "android.app.IApplicationThread",
                Intent::class.java,//service
                String::class.java,//resolvedType
                Int::class.javaPrimitiveType,//callingPid
                Int::class.javaPrimitiveType,//callingUid
                Boolean::class.java,//fgRequired
                String::class.java,//callingPackage
                String::class.java,//callingFeatureId
                Int::class.javaPrimitiveType,//userId
                Boolean::class.java,//allowBackgroundActivityStarts
                object : XC_MethodHook() {
                    @Throws(Throwable::class)
                    override fun beforeHookedMethod(param: MethodHookParam) {
//                        XpUtil.log("serviceHook30")
                        val service = param.args[1] as Intent?
                        val callingPackage = param.args[6] as String
                        val userId: Int = param.args[8] as Int
                        val context: Context =
                            AndroidAppHelper.currentApplication().applicationContext
                        hookStartServiceLocked(param, service, callingPackage, context, userId)
                    }
                })
        }

        private fun serviceHook29(lpparam: XC_LoadPackage.LoadPackageParam) {
            XposedHelpers.findAndHookMethod(
                "com.android.server.am.ActiveServices",
                lpparam.classLoader,
                "startServiceLocked",
                "android.app.IApplicationThread",
                Intent::class.java,//service
                String::class.java,//resolvedType
                Int::class.javaPrimitiveType,//callingPid
                Int::class.javaPrimitiveType,//callingUid
                Boolean::class.java,//fgRequired
                String::class.java,//callingPackage
                Int::class.javaPrimitiveType,//userId
                Boolean::class.java,//allowBackgroundActivityStarts
                object : XC_MethodHook() {
                    @Throws(Throwable::class)
                    override fun beforeHookedMethod(param: MethodHookParam) {
//                        XpUtil.log("serviceHook29")
                        val service = param.args[1] as Intent?
                        val callingPackage = param.args[6] as String
                        val userId: Int = param.args[7] as Int
                        val context: Context =
                            AndroidAppHelper.currentApplication().applicationContext
                        hookStartServiceLocked(param, service, callingPackage, context, userId)
                    }
                })
        }

        private fun serviceHook26to28(lpparam: XC_LoadPackage.LoadPackageParam) {
            XposedHelpers.findAndHookMethod(
                "com.android.server.am.ActiveServices",
                lpparam.classLoader,
                "startServiceLocked",
                "android.app.IApplicationThread",
                Intent::class.java,//service
                String::class.java,//resolvedType
                Int::class.javaPrimitiveType,//callingPid
                Int::class.javaPrimitiveType,//callingUid
                Boolean::class.java,//fgRequired
                String::class.java,//callingPackage
                Int::class.javaPrimitiveType,//userId
                object : XC_MethodHook() {
                    @Throws(Throwable::class)
                    override fun beforeHookedMethod(param: MethodHookParam) {
//                        XpUtil.log("serviceHook26to28")
                        val service = param.args[1] as Intent?
                        val callingPackage = param.args[6] as String
                        val userId: Int = param.args[7] as Int
                        val context: Context =
                            AndroidAppHelper.currentApplication().applicationContext
                        hookStartServiceLocked(param, service, callingPackage, context, userId)
                    }
                })
        }

        private fun serviceHook24to25(lpparam: XC_LoadPackage.LoadPackageParam) {
            XposedHelpers.findAndHookMethod(
                "com.android.server.am.ActiveServices",
                lpparam.classLoader,
                "startServiceLocked",
                "android.app.IApplicationThread",
                Intent::class.java,//service
                String::class.java,//resolvedType
                Int::class.javaPrimitiveType,//callingPid
                Int::class.javaPrimitiveType,//callingUid
                String::class.java,//callingPackage
                Int::class.javaPrimitiveType,//userId
                object : XC_MethodHook() {
                    @Throws(Throwable::class)
                    override fun beforeHookedMethod(param: MethodHookParam) {
//                        XpUtil.log("serviceHook24to25")
                        val service = param.args[1] as Intent?
                        val callingPackage = param.args[5] as String
                        val userId: Int = param.args[6] as Int
                        val context: Context =
                            AndroidAppHelper.currentApplication().applicationContext
                        hookStartServiceLocked(param, service, callingPackage, context, userId)
                    }
                })
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