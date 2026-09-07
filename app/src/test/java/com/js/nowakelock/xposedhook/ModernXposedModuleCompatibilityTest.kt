package com.js.nowakelock.xposedhook

import com.js.nowakelock.xposedhook.entry.UniversalXposedEntry
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedModuleInterface
import org.junit.Assert.assertNotNull
import org.junit.Test

class ModernXposedModuleCompatibilityTest {
    @Test fun `entry has both exact framework constructor descriptors`() {
        assertNotNull(UniversalXposedEntry::class.java.getDeclaredConstructor())
        assertNotNull(UniversalXposedEntry::class.java.getDeclaredConstructor(
            XposedInterface::class.java, XposedModuleInterface.ModuleLoadedParam::class.java))
    }
}
