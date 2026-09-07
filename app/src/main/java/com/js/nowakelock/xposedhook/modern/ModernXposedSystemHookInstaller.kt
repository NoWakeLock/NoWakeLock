package com.js.nowakelock.xposedhook.modern

import com.js.nowakelock.xposedhook.XposedHookInstallGuard
import io.github.libxposed.api.XposedInterface
import java.lang.reflect.Method

object ModernXposedSystemHookInstaller {
    fun install(xposed: XposedInterface, classLoader: ClassLoader?, source: String) {
        ModernXposedLog.install { message ->
            xposed.log(android.util.Log.INFO, "NoWakeLockModern", message)
        }
        val loader = classLoader ?: run {
            ModernXposedLog.info("Skip system_server hooks from $source because classLoader is null")
            return
        }
        installSafely("boot hooks") { hookBootCompletedMethods(xposed, loader) }
        installSafely("wakelock hooks") { ModernWakelockHook.hook(xposed, loader) }
        installSafely("alarm hooks") { ModernAlarmHook.hook(xposed, loader) }
        installSafely("service hooks") { ModernServiceHook.hook(xposed, loader) }
    }

    private fun installSafely(name: String, action: () -> Unit) {
        try {
            action()
        } catch (e: Throwable) {
            ModernXposedLog.error("Unable to install $name", e)
        }
    }

    private fun markBooted() {
        ModernWakelockHook.booted = true
        ModernAlarmHook.booted = true
        ModernServiceHook.booted = true
    }

    private fun hookBootCompletedMethods(xposed: XposedInterface, classLoader: ClassLoader) {
        val candidates = listOf(
            "com.android.server.policy.keyguard.KeyguardServiceDelegate" to "onBootCompleted",
            "com.android.server.am.ActivityManagerService" to "finishBooting",
            "com.android.server.wm.WindowManagerService" to "systemReady"
        )
        for ((className, methodName) in candidates) {
            val cls = ModernHookSupport.loadClass(className, classLoader) ?: continue
            val methods = cls.declaredMethods.filter { it.name == methodName }
            for (method in methods) {
                hookBootMethod(xposed, method)
            }
        }
    }

    private fun hookBootMethod(xposed: XposedInterface, method: Method) {
        ModernHookSupport.hookMethod(xposed, method) { chain ->
            markBooted()
            chain.proceed()
        }
    }
}
