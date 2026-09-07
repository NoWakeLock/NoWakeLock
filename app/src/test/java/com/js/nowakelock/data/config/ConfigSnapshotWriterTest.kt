package com.js.nowakelock.data.config

import com.js.nowakelock.data.db.Type
import com.js.nowakelock.data.db.entity.AppSt
import com.js.nowakelock.data.db.entity.St
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ConfigSnapshotWriterTest {
    @Test
    fun `writes rule settings to backend`() {
        val backend = FakeBackend()
        val st = St(
            name = "JobScheduler",
            type = Type.Service,
            packageName = "android",
            fullBlock = true,
            screenOffBlock = false,
            timeWindowMs = 3000,
            userId = 0
        )

        assertTrue(ConfigSnapshotWriter.writeSt(backend, st))

        assertEquals(true, backend.booleans["JobScheduler_Service_android_0_flag"])
        assertEquals(false, backend.booleans["JobScheduler_Service_android_0_flag_lock"])
        assertEquals(3000L, backend.longs["JobScheduler_Service_android_0_aTI"])
    }

    @Test
    fun `writes app regex settings to backend`() {
        val backend = FakeBackend()
        val appSt = AppSt(
            packageName = "com.example.app",
            rE_Wakelock = setOf("wake.*"),
            rE_Alarm = setOf("alarm.*"),
            rE_Service = setOf("service.*"),
            userId = 10
        )

        assertTrue(ConfigSnapshotWriter.writeAppSt(backend, appSt))

        assertEquals(setOf("wake.*"), backend.sets["Wakelock_com.example.app_10_rE"])
        assertEquals(setOf("alarm.*"), backend.sets["Alarm_com.example.app_10_rE"])
        assertEquals(setOf("service.*"), backend.sets["Service_com.example.app_10_rE"])
    }

    @Test
    fun `writeSt reports backend write failure`() {
        val backend = FakeBackend(putLongResult = false)
        val st = St(
            name = "JobScheduler",
            type = Type.Service,
            packageName = "android",
            fullBlock = true,
            screenOffBlock = false,
            timeWindowMs = 3000,
            userId = 0
        )

        assertFalse(ConfigSnapshotWriter.writeSt(backend, st))
    }

    private class FakeBackend(
        private val putBooleanResult: Boolean = true,
        private val putLongResult: Boolean = true,
        private val putStringSetResult: Boolean = true
    ) : XposedConfigBackend {
        override val name = "fake"
        override val isReadable = true
        override fun publish(snapshot: ConfigSnapshot): Boolean = true
        val booleans = mutableMapOf<String, Boolean>()
        val longs = mutableMapOf<String, Long>()
        val sets = mutableMapOf<String, Set<String>>()

        override fun putBoolean(key: String, value: Boolean): Boolean {
            booleans[key] = value
            return putBooleanResult
        }

        override fun putLong(key: String, value: Long): Boolean {
            longs[key] = value
            return putLongResult
        }

        override fun putStringSet(key: String, value: Set<String>): Boolean {
            sets[key] = value
            return putStringSetResult
        }
    }
}
