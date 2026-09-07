package com.js.nowakelock.xposedhook

import android.content.SharedPreferences
import android.util.Log
import com.js.nowakelock.data.config.XposedConfigKeys
import com.js.nowakelock.xposedhook.entry.EntryDelegate
import com.js.nowakelock.xposedhook.hook.SettingsProviderHook
import com.js.nowakelock.xposedhook.model.XpNSP

/** Fixed LSPosed1.11.0 bridge: retain legacy hooks, use its remote preference transport. */
class OldModernEntryAdapter(private val framework: Any) : EntryDelegate {
    override fun moduleLoaded(processName: String) {
        Log.i(TAG, "Old modern entry loaded: $processName")
        installReader()
    }

    override fun systemServer(loader: ClassLoader?) {
        installReader()
        XposedSystemHookInstaller.install(loader, "lsposed-1.11.0-bridge")
    }

    override fun packageLoaded(packageName: String, loader: ClassLoader?) {
        installReader()
        if (packageName == "com.android.providers.settings") SettingsProviderHook.hook(loader)
    }

    override fun packageReady(packageName: String, loader: ClassLoader?) = Unit

    private fun installReader() {
        try {
            val preferences = framework.javaClass.getMethod("getRemotePreferences", String::class.java)
                .invoke(framework, XposedConfigKeys.GROUP_NAME) as SharedPreferences
            XpNSP.installRemotePreferences(preferences, "LSPosed", "1.11.0")
        } catch (e: Exception) {
            Log.e(TAG, "Old remote preference reader unavailable", e)
        }
    }

    companion object { private const val TAG = "NoWakeLockOldBridge" }
}
