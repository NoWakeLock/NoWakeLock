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
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

class ServiceHook {
    companion object {

        private val type = Type.Service

        fun hookService(lpparam: XC_LoadPackage.LoadPackageParam) {

            XpUtil.log("Hooking Service ${Build.VERSION.SDK_INT}")

            when (Build.VERSION.SDK_INT) {
                //Try for alarm hooks for API levels >= 31 (S)
                in Build.VERSION_CODES.S..40 -> serviceHook31to32(lpparam)
                //Try for alarm hooks for API levels = 30 (R)
                Build.VERSION_CODES.R -> serviceHook30(lpparam)
                //Try for alarm hooks for API levels = 29 (Q)
                Build.VERSION_CODES.Q -> serviceHook29(lpparam)
                //Try for alarm hooks for API levels 26 ~ 28 (O ~ P)
                in Build.VERSION_CODES.O..Build.VERSION_CODES.P -> serviceHook26to28(lpparam)
                //Try for alarm hooks for API levels 24 ~ 25 (N)
                in Build.VERSION_CODES.N..Build.VERSION_CODES.N_MR1 -> serviceHook24to25(lpparam)
            }
        }

        /**
         * https://cs.android.com/android/platform/superproject/+/master:frameworks/base/services/core/java/com/android/server/am/ActiveServices.java;l=613?q=startServiceLocked
         * @param lpparam LoadPackageParam
         * @throws Throwable
         */

        private fun serviceHook31to32(lpparam: XC_LoadPackage.LoadPackageParam) {

            XposedHelpers.findAndHookMethod("com.android.server.am.ActiveServices",
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

        //https://cs.android.com/android/platform/superproject/+/android-10.0.0_r1:frameworks/base/services/core/java/com/android/server/am/ActiveServices.java;l=184?q=ActiveServices&ss=android%2Fplatform%2Fsuperproject:
        private fun serviceHook30(lpparam: XC_LoadPackage.LoadPackageParam) {
            XpUtil.log("Hooking Service for API levels 30")

            XposedHelpers.findAndHookMethod("com.android.server.am.ActiveServices",
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
            XposedHelpers.findAndHookMethod("com.android.server.am.ActiveServices",
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
            XposedHelpers.findAndHookMethod("com.android.server.am.ActiveServices",
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
            val block = block(serviceName, packageName, userId, !pm.isInteractive)

            if (block) {
                param.result = null

                XpUtil.log("$packageName service: $serviceName block")
                XpRecord.upBlockCount(
                    serviceName, packageName, type,
                    context, userId
                )//update BlockCount
            } else {
                XpRecord.upCount(serviceName, packageName, type, context, userId)//update Count
            }
        }

        private fun block(name: String, packageName: String, userId: Int, isLocked: Boolean): Boolean {
            val xpNSP = XpNSP.getInstance()
            return xpNSP.flag(name, packageName, type, userId)
                    || isLocked && xpNSP.flag(name, packageName, type, userId)
        }

    }
}