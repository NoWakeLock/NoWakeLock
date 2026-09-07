package com.js.nowakelock.xposedhook

import com.js.nowakelock.xposedhook.hook.AlarmHook
import com.js.nowakelock.xposedhook.hook.ServiceHook
import com.js.nowakelock.xposedhook.hook.WakelockHook
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers

object XposedSystemHookInstaller {
    fun install(classLoader: ClassLoader?, source: String) {
        val loader = classLoader ?: run {
            XpUtil.log("Skip system_server hooks from $source because classLoader is null")
            return
        }
        if (!XposedHookInstallGuard.markSharedInstalled(source, "system-server", loader)) {
            XpUtil.log("Skip duplicate system_server hooks from $source")
            return
        }

        try {
            hookBootCompletedMethods(loader)
        } catch (e: Throwable) {
            XpUtil.log("${e.message}")
            XpUtil.log("${e.stackTrace}")
        }
        try {
            WakelockHook.hookWakeLocks(loader)
        } catch (e: Throwable) {
            XpUtil.log("${e.message}")
            XpUtil.log("${e.stackTrace}")
        }
        try {
            AlarmHook.hookAlarm(loader)
        } catch (e: Throwable) {
            XpUtil.log("${e.message}")
            XpUtil.log("${e.stackTrace}")
        }
        try {
            ServiceHook.hookService(loader)
        } catch (e: Throwable) {
            XpUtil.log("${e.message}")
            XpUtil.log("${e.stackTrace}")
        }
    }

    private fun markBooted() {
        WakelockHook.booted = true
        ServiceHook.booted = true
        AlarmHook.booted = true
    }

    private fun hookBootCompletedMethods(classLoader: ClassLoader) {
        try {
            XposedHelpers.findAndHookMethod(
                "com.android.server.policy.keyguard.KeyguardServiceDelegate",
                classLoader,
                "onBootCompleted",
                object : XC_MethodHook() {
                    @Throws(Throwable::class)
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        markBooted()
                    }
                })
        } catch (e: Throwable) {
            XpUtil.log("${e.message}")
            XpUtil.log("${e.stackTrace}")

            try {
                XposedHelpers.findAndHookMethod(
                    "com.android.server.am.ActivityManagerService",
                    classLoader,
                    "finishBooting",
                    object : XC_MethodHook() {
                        @Throws(Throwable::class)
                        override fun beforeHookedMethod(param: MethodHookParam) {
                            markBooted()
                        }
                    })
            } catch (e: Throwable) {
                XpUtil.log("${e.message}")
                XpUtil.log("${e.stackTrace}")

                XposedHelpers.findAndHookMethod(
                    "com.android.server.wm.WindowManagerService",
                    classLoader,
                    "systemReady",
                    object : XC_MethodHook() {
                        @Throws(Throwable::class)
                        override fun beforeHookedMethod(param: MethodHookParam) {
                            markBooted()
                        }
                    })
            }
        }
    }
}
