package com.js.nowakelock.data.repository.backup

import com.js.nowakelock.data.db.Type
import com.js.nowakelock.data.db.entity.AppSt
import com.js.nowakelock.data.db.entity.St
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class BackupJsonTest {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @Test
    fun roundtrip_preservesBackupData() {
        val backup = Backup(
            appSts = listOf(
                AppSt(
                    packageName = "com.example",
                    wakelock = true,
                    alarm = false,
                    service = true,
                    rE_Wakelock = setOf(".*sync.*"),
                    rE_Alarm = emptySet(),
                    rE_Service = setOf("com.example/.FooService"),
                    userId = 0
                )
            ),
            sts = listOf(
                St(
                    name = "TAG",
                    type = Type.Wakelock,
                    packageName = "com.example",
                    fullBlock = false,
                    screenOffBlock = true,
                    timeWindowMs = 60_000L,
                    userId = 0
                ),
                St(
                    name = "SVC",
                    type = Type.Service,
                    packageName = "com.example",
                    fullBlock = true,
                    screenOffBlock = null,
                    timeWindowMs = 0L,
                    userId = 0
                )
            )
        )

        val encoded = json.encodeToString(backup)
        val decoded = json.decodeFromString<Backup>(encoded)

        assertEquals(backup, decoded)
    }

    @Test
    fun decode_ignoresUnknownKeys() {
        val raw = """
            {
              "appSts": [],
              "sts": [],
              "unknownField": 123
            }
        """.trimIndent()

        val decoded = json.decodeFromString<Backup>(raw)

        assertEquals(0, decoded.appSts.size)
        assertEquals(0, decoded.sts.size)
    }
}
