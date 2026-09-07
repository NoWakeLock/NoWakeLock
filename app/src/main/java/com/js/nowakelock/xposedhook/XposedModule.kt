package com.js.nowakelock.xposedhook

import android.app.AndroidAppHelper
import com.js.nowakelock.xposedhook.hook.SettingsProviderHook
import com.js.nowakelock.xposedhook.model.LegacyHookConfigReader
import com.js.nowakelock.xposedhook.model.XpNSP
import de.robv.android.xposed.*
import de.robv.android.xposed.IXposedHookZygoteInit.StartupParam
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam


// GUARDED - ASK BEFORE MODIFYING
open class XposedModule : IXposedHookZygoteInit, IXposedHookLoadPackage {
    override fun initZygote(startupParam: StartupParam?) {
        XpUtil.log(": initZygote")
    }

    // CRITICAL - BUSINESS LOGIC
    override fun handleLoadPackage(lpparam: LoadPackageParam) {
//        val pN = lpparam.packageName
//        XposedBridge.log("$TAG $pN: handleLoadPackage ,mypid ${Process.myUid()}")
        if (!XposedHookInstallGuard.markInstalled("legacy", lpparam.packageName, lpparam.classLoader)) {
            return
        }
        XpNSP.installReader(LegacyHookConfigReader())

        when (lpparam.packageName) {
            "android" -> {//hook Android system
                XposedBridge.log("handleLoadPackage ${AndroidAppHelper.currentApplication()}")

                XposedSystemHookInstaller.install(lpparam.classLoader, "legacy")
            }

            "com.android.providers.settings" -> {//hook SettingsProvider
                SettingsProviderHook.hook(lpparam)
            }
        }
    }
}
