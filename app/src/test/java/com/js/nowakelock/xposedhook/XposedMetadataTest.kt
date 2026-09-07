package com.js.nowakelock.xposedhook

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File
import java.util.Properties

class XposedMetadataTest {
    @Test
    fun `modern module metadata targets API102 runtime compatibility`() {
        val properties = Properties().apply {
            File("src/main/resources/META-INF/xposed/module.prop").inputStream().use(::load)
        }

        assertEquals("100", properties.getProperty("minApiVersion"))
        assertEquals("102", properties.getProperty("targetApiVersion"))
        assertEquals("true", properties.getProperty("staticScope"))
    }

    @Test
    fun `legacy and modern entry files are both packaged`() {
        assertEquals(
            "com.js.nowakelock.xposedhook.XposedModule",
            File("src/main/assets/xposed_init").readText().trim()
        )
        assertEquals(
            "com.js.nowakelock.xposedhook.entry.UniversalXposedEntry",
            File("src/main/resources/META-INF/xposed/java_init.list").readText().trim()
        )
    }

    @Test
    fun `modern scope uses system server target for SettingsProvider callbacks`() {
        assertEquals(
            listOf("system", "android"),
            File("src/main/resources/META-INF/xposed/scope.list").readLines()
        )
    }
}
