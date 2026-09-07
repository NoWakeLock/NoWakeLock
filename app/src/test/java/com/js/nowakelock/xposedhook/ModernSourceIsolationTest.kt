package com.js.nowakelock.xposedhook

import org.junit.Assert.assertFalse
import org.junit.Test
import java.io.File

class ModernSourceIsolationTest {
    @Test
    fun `modern entry and modern hooks do not reference legacy xposed api`() {
        val sources = buildList {
            add(File("src/main/java/com/js/nowakelock/xposedhook/ModernXposedModule.kt"))
            add(File("src/main/java/com/js/nowakelock/xposedhook/ModernXposedRuntimeDetector.kt"))
            add(File("src/main/java/com/js/nowakelock/xposedhook/XposedHookInstallGuard.kt"))
            add(File("src/main/java/com/js/nowakelock/xposedhook/model/HookConfigReader.kt"))
            add(File("src/main/java/com/js/nowakelock/xposedhook/model/XpNSP.kt"))
            addAll(
                File("src/main/java/com/js/nowakelock/xposedhook/modern")
                    .walkTopDown()
                    .filter { it.isFile && it.extension == "kt" }
                    .toList()
            )
        }

        val offenders = sources.filter { file ->
            val text = file.readText()
            text.contains("de.robv.android.xposed") || text.contains("XSharedPreferences")
        }

        assertFalse(
            "Modern path must not reference legacy Xposed API: ${offenders.joinToString { it.name }}",
            offenders.isNotEmpty()
        )
    }
}
