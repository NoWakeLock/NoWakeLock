package com.js.nowakelock.xposedhook

import android.util.Log
import com.js.nowakelock.data.config.XposedConfigKeys
import com.js.nowakelock.xposedhook.entry.EntryDelegate
import com.js.nowakelock.xposedhook.modern.ModernSettingsProviderHook
import com.js.nowakelock.xposedhook.modern.ModernXposedLog
import com.js.nowakelock.xposedhook.modern.ModernXposedSystemHookInstaller
import com.js.nowakelock.xposedhook.model.XpNSP
import io.github.libxposed.api.XposedInterface

/** API102-only implementation; instantiated after framework attachment. */
class ModernXposedModule(framework: Any) : EntryDelegate {
    private val xposed = framework as XposedInterface
    private var supported = false
    private var settingsLoader: ClassLoader? = null

    override fun moduleLoaded(processName: String) {
        supported = xposed.apiVersion == 102
        ModernXposedLog.install { logInfo(it) }
        logInfo("Loaded process=$processName framework=${xposed.frameworkName} api=${xposed.apiVersion}")
        if (supported) installReader()
    }

    override fun systemServer(loader: ClassLoader?) {
        if (!supported || xposed.frameworkProperties and XposedInterface.PROP_CAP_SYSTEM == 0L) {
            logInfo("System hook capability unavailable")
            return
        }
        XpNSP.getInstance().api102SystemRuntime = true
        installReader()
        ModernXposedSystemHookInstaller.install(xposed, loader, "modern-api102")
        logInfo("System hook installation results:\n${HookInstallRegistry.summary()}")
    }

    override fun packageLoaded(packageName: String, loader: ClassLoader?) {
        if (!supported) return
        installReader()
        if (packageName == SETTINGS_PROVIDER) settingsLoader = loader
    }

    override fun packageReady(packageName: String, loader: ClassLoader?) {
        if (!supported) return
        installReader()
        if (packageName == SETTINGS_PROVIDER) {
            ModernSettingsProviderHook.hook(xposed, loader ?: settingsLoader)
            logInfo("SettingsProvider installation results:\n${HookInstallRegistry.summary()}")
        }
    }

    private fun installReader() {
        try {
            if (xposed.frameworkProperties and XposedInterface.PROP_CAP_REMOTE == 0L) {
                logInfo("Remote Preferences capability unavailable")
                return
            }
            XpNSP.installRemotePreferences(xposed.getRemotePreferences(XposedConfigKeys.GROUP_NAME),
                xposed.frameworkName, xposed.frameworkVersion)
        } catch (e: Exception) {
            logInfo("Unable to install Remote Preferences reader: ${e.message}")
        }
    }

    private fun logInfo(message: String) {
        try { xposed.log(Log.INFO, "NoWakeLockModern", message) }
        catch (_: Exception) { Log.i("NoWakeLockModern", message) }
    }

    companion object { private const val SETTINGS_PROVIDER = "com.android.providers.settings" }
}
