package com.js.nowakelock.xposedhook

object ModernXposedRuntimeDetector {
    fun isAvailable(targetClassLoader: ClassLoader? = null): Boolean {
        return canLoad(MODERN_INTERFACE, targetClassLoader)
            || canLoad(MODERN_INTERFACE, ModernXposedRuntimeDetector::class.java.classLoader)
            || canLoad(MODERN_INTERFACE, ClassLoader.getSystemClassLoader())
    }

    private fun canLoad(className: String, classLoader: ClassLoader?): Boolean {
        if (classLoader == null) return false
        return try {
            Class.forName(className, false, classLoader)
            true
        } catch (_: Throwable) {
            false
        }
    }

    private const val MODERN_INTERFACE = "io.github.libxposed.api.XposedInterface"
}
