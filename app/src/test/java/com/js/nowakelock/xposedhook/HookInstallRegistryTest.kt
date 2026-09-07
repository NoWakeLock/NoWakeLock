package com.js.nowakelock.xposedhook

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class HookInstallRegistryTest {
    @Before fun reset() = HookInstallRegistry.clearForTest()

    @Test fun `failed installation retries but successful installation is not duplicated`() {
        val method = String::class.java.getMethod("length")
        runCatching { HookInstallRegistry.install(method) { error("installation failed") } }
        assertTrue(HookInstallRegistry.summary().contains("FAILED"))
        var installed = 0
        assertTrue(HookInstallRegistry.install(method) { installed++ })
        assertFalse(HookInstallRegistry.install(method) { installed++ })
        assertEquals(1, installed)
    }

    @Test fun `overloaded methods have independent installation state`() {
        val one = String::class.java.getMethod("substring", Int::class.javaPrimitiveType)
        val two = String::class.java.getMethod("substring", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
        assertTrue(HookInstallRegistry.install(one) {})
        assertTrue(HookInstallRegistry.install(two) {})
    }
}
