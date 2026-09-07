package com.js.nowakelock.xposedhook

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ModernMetadataTest {
    @Test
    fun `modern java entry points to modern module`() {
        val entries = File("src/main/resources/META-INF/xposed/java_init.list")
            .readLines()
            .filter { it.isNotBlank() }

        assertEquals(listOf("com.js.nowakelock.xposedhook.entry.UniversalXposedEntry"), entries)
    }

    @Test
    fun `modern module targets API 102`() {
        val properties = File("src/main/resources/META-INF/xposed/module.prop")
            .readLines()
            .filter { it.contains("=") }
            .associate {
                val (key, value) = it.split("=", limit = 2)
                key to value
            }

        assertEquals("100", properties["minApiVersion"])
        assertEquals("102", properties["targetApiVersion"])
    }

    @Test
    fun `modern scope contains declared system targets`() {
        val scopes = File("src/main/resources/META-INF/xposed/scope.list")
            .readLines()
            .filter { it.isNotBlank() }
            .toSet()

        assertTrue("system scope missing", "system" in scopes)
        assertTrue("android scope missing", "android" in scopes)
        assertEquals(setOf("system", "android"), scopes)
    }
}
