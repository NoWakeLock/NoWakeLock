package com.js.nowakelock.xposedhook.modern

import android.content.Context
import android.os.Bundle
import com.js.nowakelock.data.provider.XProvider
import com.js.nowakelock.data.provider.ProviderMethod
import com.js.nowakelock.xposedhook.model.RuntimeTransfer
import com.js.nowakelock.xposedhook.model.XpRecord
import com.js.nowakelock.xposedhook.XposedHookInstallGuard
import io.github.libxposed.api.XposedInterface

object ModernSettingsProviderHook {
    fun hook(xposed: XposedInterface, classLoader: ClassLoader?) {
        val loader = classLoader ?: return
        val cls = ModernHookSupport.loadClass(
            "com.android.providers.settings.SettingsProvider",
            loader
        ) ?: return
        val method = cls.getMethod(
            "call",
            String::class.java,
            String::class.java,
            Bundle::class.java
        )
        ModernHookSupport.hookMethod(xposed, method) { chain ->
            val args = ModernHookSupport.args(chain)
            val callMethod = args.getOrNull(0) as? String
            if (callMethod != "NoWakelock") {
                return@hookMethod chain.proceed()
            }
            val providerMethod = args.getOrNull(1) as? String
            val extras = args.getOrNull(2) as? Bundle
            val context = ModernHookSupport.findContext(chain)
            call(context, providerMethod, extras)
        }
    }

    private fun call(context: Context?, method: String?, extras: Bundle?): Bundle? {
        if (context == null || extras == null || method == null) return null
        if (method == com.js.nowakelock.data.config.ConfigPush.METHOD) {
            return com.js.nowakelock.data.config.ConfigPush.receive(context, extras)
        }
        if (method == ProviderMethod.NewEvent.value || method == ProviderMethod.EndEvent.value) {
            // Keep Binder ingress ordered with late callbacks and the new generation's consumer.
            // A retired callback may enqueue neutral data but must never reopen its old database.
            if (!RuntimeTransfer.retired) XProvider.getInstance(context)
            // On the first local attachment the consumer may already own an earlier batch.
            // Re-enqueueing it behind later events would put releases before their acquires.
            if (!RuntimeTransfer.retired && extras.getInt("__recordWorkerPid") == android.os.Process.myPid() &&
                android.os.Binder.getCallingPid() == android.os.Process.myPid()) {
                return XProvider.getInstance(context).getMethod(method, extras)
            }
            return XpRecord.receive(context, method, extras)
        }
        return XProvider.getInstance(context).getMethod(method, extras)
    }
}
