package com.js.nowakelock.xposedhook

import java.util.concurrent.ConcurrentHashMap

object XposedHookInstallGuard {
    private val installed = ConcurrentHashMap.newKeySet<String>()

    fun markInstalled(entry: String, target: String, classLoader: ClassLoader?): Boolean {
        val key = "$entry:$target:${System.identityHashCode(classLoader)}"
        return installed.add(key)
    }

    fun markSharedInstalled(target: String, classLoader: ClassLoader?): Boolean {
        val key = "shared:$target:${System.identityHashCode(classLoader)}"
        return installed.add(key)
    }

    fun markSharedInstalled(entry: String, target: String, classLoader: ClassLoader?): Boolean {
        val key = "$entry:shared:$target:${System.identityHashCode(classLoader)}"
        return installed.add(key)
    }

    internal fun clearForTest() {
        installed.clear()
    }
}
