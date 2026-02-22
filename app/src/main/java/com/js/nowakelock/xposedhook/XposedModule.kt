package com.js.nowakelock.xposedhook

import android.app.AndroidAppHelper
import android.content.IntentFilter
import com.js.nowakelock.BuildConfig
import com.js.nowakelock.xposedhook.hook.AlarmHook
import com.js.nowakelock.xposedhook.hook.ServiceHook
import com.js.nowakelock.xposedhook.hook.SettingsProviderHook
import com.js.nowakelock.xposedhook.hook.WakelockHook
import com.js.nowakelock.xposedhook.hook.WakelockHook.Companion.booted
import de.robv.android.xposed.*
import de.robv.android.xposed.IXposedHookZygoteInit.StartupParam
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam


// GUARDED - ASK BEFORE MODIFYING
open class XposedModule : IXposedHookZygoteInit, IXposedHookLoadPackage {
    private var booted = false

    override fun initZygote(startupParam: StartupParam?) {
        XpUtil.log(": initZygote")
    }

    // CRITICAL - BUSINESS LOGIC
    override fun handleLoadPackage(lpparam: LoadPackageParam) {
//        val pN = lpparam.packageName
//        XposedBridge.log("$TAG $pN: handleLoadPackage ,mypid ${Process.myUid()}")

        when (lpparam.packageName) {
            "android" -> {//hook Android system
                XposedBridge.log("handleLoadPackage ${AndroidAppHelper.currentApplication()}")

                hookBootCompletedMethods(lpparam)

                try {
                    WakelockHook.hookWakeLocks(lpparam)
                } catch (e: Throwable) {
                    XpUtil.log("${e.message}")
                    XpUtil.log("${e.stackTrace}")
                }
                try {
                    AlarmHook.hookAlarm(lpparam)
                } catch (e: Throwable) {
                    XpUtil.log("${e.message}")
                    XpUtil.log("${e.stackTrace}")
                }
                try {
                    ServiceHook.hookService(lpparam)
                } catch (e: Throwable) {
                    XpUtil.log("${e.message}")
                    XpUtil.log("${e.stackTrace}")
                }
            }

            "com.android.providers.settings" -> {//hook SettingsProvider
                SettingsProviderHook.hook(lpparam)
            }
        }
    }

    private fun hookBootCompletedMethods(lpparam: LoadPackageParam) {
        try {
            // PROTECTED - DO NOT MODIFY
            XposedHelpers.findAndHookMethod(
                "com.android.server.policy.keyguard.KeyguardServiceDelegate",
                lpparam.classLoader,
                "onBootCompleted",
                object : XC_MethodHook() {
                    @Throws(Throwable::class)
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        WakelockHook.booted = true
                        ServiceHook.booted = true
                        AlarmHook.booted = true
                    }
                })
        } catch (e: Throwable) {
            XpUtil.log("${e.message}")
            XpUtil.log("${e.stackTrace}")

            try {
                // PROTECTED - DO NOT MODIFY
                XposedHelpers.findAndHookMethod(
                    "com.android.server.am.ActivityManagerService",
                    lpparam.classLoader,
                    "finishBooting",
                    object : XC_MethodHook() {
                        @Throws(Throwable::class)
                        override fun beforeHookedMethod(param: MethodHookParam) {
                            WakelockHook.booted = true
                            ServiceHook.booted = true
                            AlarmHook.booted = true
                        }
                    })
            } catch (e: Throwable) {
                XpUtil.log("${e.message}")
                XpUtil.log("${e.stackTrace}")

                try {
                    // PROTECTED - DO NOT MODIFY
                    XposedHelpers.findAndHookMethod(
                        "com.android.server.wm.WindowManagerService",
                        lpparam.classLoader,
                        "systemReady",
                        object : XC_MethodHook() {
                            @Throws(Throwable::class)
                            override fun beforeHookedMethod(param: MethodHookParam) {
                                WakelockHook.booted = true
                                ServiceHook.booted = true
                                AlarmHook.booted = true
                            }
                        })
                } catch (e: Throwable) {
                    XpUtil.log("${e.message}")
                    XpUtil.log("${e.stackTrace}")
                }
            }
        }
    }
}
