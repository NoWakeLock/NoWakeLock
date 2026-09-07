package com.js.nowakelock.xposedhook

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class XposedHookInstallGuardTest {
    @Before
    fun setUp() {
        XposedHookInstallGuard.clearForTest()
    }

    @Test
    fun `shared hooks are isolated by entry generation`() {
        val loader = javaClass.classLoader

        assertTrue(XposedHookInstallGuard.markSharedInstalled("legacy", "system-server", loader))
        assertTrue(XposedHookInstallGuard.markSharedInstalled("modern", "system-server", loader))
        assertFalse(XposedHookInstallGuard.markSharedInstalled("modern", "system-server", loader))
    }

    @Test
    fun `regular hooks are installed once per entry and loader`() {
        val loader = javaClass.classLoader

        assertTrue(XposedHookInstallGuard.markInstalled("legacy", "android", loader))
        assertFalse(XposedHookInstallGuard.markInstalled("legacy", "android", loader))
    }
}
