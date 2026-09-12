package com.js.nowakelock.xposedhook

import android.util.Log
import com.js.nowakelock.data.config.XposedConfigKeys
import com.js.nowakelock.xposedhook.entry.EntryDelegate
import com.js.nowakelock.xposedhook.modern.ModernSettingsProviderHook
import com.js.nowakelock.xposedhook.modern.ModernXposedLog
import com.js.nowakelock.xposedhook.modern.ModernXposedSystemHookInstaller
import com.js.nowakelock.xposedhook.model.XpNSP
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedModuleInterface
import com.js.nowakelock.xposedhook.model.RuntimeTransfer
import com.js.nowakelock.xposedhook.model.XpRecord
import com.js.nowakelock.data.provider.XProvider
import android.content.Context
import com.js.nowakelock.xposedhook.modern.ModernHookSupport

/** API102-only implementation; instantiated after framework attachment. */
class ModernXposedModule(framework: Any) : EntryDelegate {
    private val xposed = framework as XposedInterface
    private var supported = false
    private var settingsLoader: ClassLoader? = null

    override fun moduleLoaded(processName: String) {
        supported = xposed.apiVersion == 102
        if (supported) RuntimeTransfer.enableModern()
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
        loader?.let { RuntimeTransfer.put("systemLoader", it) }
        installReader()
        ModernXposedSystemHookInstaller.install(xposed, loader, "modern-api102")
        RuntimeTransfer.systemInstalled = true
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
            (loader ?: settingsLoader)?.let { RuntimeTransfer.put("settingsLoader", it) }
            ModernSettingsProviderHook.hook(xposed, loader ?: settingsLoader)
            RuntimeTransfer.providerInstalled = true
            logInfo("SettingsProvider installation results:\n${HookInstallRegistry.summary()}")
        }
    }

    override fun hotReloading(param: Any): Boolean {
        if (!supported) return false
        val reload = param as XposedModuleInterface.HotReloadingParam
        try {
            // Validate ownership before changing resources. All queue entries and transferred
            // collections use framework/JDK classes, never module-defined wrapper objects.
            check(RuntimeTransfer.state()["schema"] == 1)
            RuntimeTransfer.validateNeutralState()
            if (RuntimeTransfer.state()["settingsLoader"] != null &&
                RuntimeTransfer.state()["providerContext"] == null) return false
            if (!XpRecord.stopForReload()) return false
            XpNSP.getInstance().disposeReader()
            if (!XProvider.retireForReload()) error("Database executor is still active")
            RuntimeTransfer.retired = true
            RuntimeTransfer.validateNeutralState()
            reload.setSavedInstanceState(RuntimeTransfer.state())
            return true
        } catch (e: Throwable) {
            RuntimeTransfer.retired = false
            try { XProvider.resumeAfterRefusal() } catch (restore: Throwable) {
                logInfo("Provider recovery after reload refusal failed: ${restore.message}")
            }
            try { XpNSP.getInstance().resumeReader() } catch (restore: Throwable) {
                logInfo("Rule listener recovery after refusal failed: ${restore.message}")
            } finally { XpRecord.resumeAfterReload() }
            logInfo("Hot reload refused: ${e.message}")
            return false
        }
    }

    override fun hotReloaded(param: Any) {
        val reload = param as XposedModuleInterface.HotReloadedParam
        RuntimeTransfer.adopt(reload.savedInstanceState)
        try {
            XpNSP.restoreRules()
            moduleLoaded(reload.processName)
            check(supported) { "API102 unavailable after reload" }
            // Reopen resources before replacing any callbacks. Failures still retain last rules.
            (RuntimeTransfer.state()["providerContext"] as? Context)?.let { XProvider.getInstance(it) }
            ModernHookSupport.beginReload(reload.oldHookHandles)
            if (reload.isSystemServer) systemServer(RuntimeTransfer.state()["systemLoader"] as? ClassLoader)
            (RuntimeTransfer.state()["settingsLoader"] as? ClassLoader)?.let {
                packageReady(SETTINGS_PROVIDER, it)
            }
            ModernHookSupport.finishReload()
            XpRecord.resumeAfterReload()
            logInfo("Hot reload installed: ${com.js.nowakelock.BuildConfig.HOOK_BUILD_ID}")
        } catch (e: Throwable) {
            RuntimeTransfer.reloadFailure = e.message ?: e.javaClass.name
            ModernHookSupport.abandonReload()
            // The framework cannot roll back callbacks already replaced. Keep the shared
            // consumer alive for both generations, and report failure instead of losing events.
            try {
                (RuntimeTransfer.state()["providerContext"] as? Context)?.let { XProvider.getInstance(it) }
                XpRecord.resumeAfterReload()
            } catch (recovery: Throwable) { logInfo("Statistics recovery failed: ${recovery.message}") }
            logInfo("Hot reload failed; target needs a normal restart: ${e.message}")
            throw e
        }
    }

    private fun installReader() {
        try {
            if (xposed.frameworkProperties and XposedInterface.PROP_CAP_REMOTE == 0L) {
                logInfo("Remote Preferences capability unavailable")
                check(!RuntimeTransfer.reloaded) { "Remote capability lost during reload" }
                return
            }
            XpNSP.installRemotePreferences(xposed.getRemotePreferences(XposedConfigKeys.GROUP_NAME),
                xposed.frameworkName, xposed.frameworkVersion)
        } catch (e: Exception) {
            logInfo("Unable to install Remote Preferences reader: ${e.message}")
            if (RuntimeTransfer.reloaded) throw e
        }
    }

    private fun logInfo(message: String) {
        try { xposed.log(Log.INFO, "NoWakeLockModern", message) }
        catch (_: Exception) { Log.i("NoWakeLockModern", message) }
    }

    companion object { private const val SETTINGS_PROVIDER = "com.android.providers.settings" }
}
