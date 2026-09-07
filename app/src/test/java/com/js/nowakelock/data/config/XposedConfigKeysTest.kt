package com.js.nowakelock.data.config

import com.js.nowakelock.data.db.Type
import com.js.nowakelock.data.db.entity.AppSt
import com.js.nowakelock.data.db.entity.St
import org.junit.Assert.assertEquals
import org.junit.Test

class XposedConfigKeysTest {
    @Test
    fun `rule keys keep legacy format`() {
        val st = St(
            name = "PowerManagerService.WakeLocks",
            type = Type.Wakelock,
            packageName = "android",
            userId = 10
        )

        assertEquals(
            "PowerManagerService.WakeLocks_Wakelock_android_10_flag",
            XposedConfigKeys.flag(st)
        )
        assertEquals(
            "PowerManagerService.WakeLocks_Wakelock_android_10_flag_lock",
            XposedConfigKeys.flagLock(st)
        )
        assertEquals(
            "PowerManagerService.WakeLocks_Wakelock_android_10_aTI",
            XposedConfigKeys.allowTimeInterval(st)
        )
    }

    @Test
    fun `regex keys keep legacy app-level format`() {
        assertEquals(
            "Alarm_com.example.app_0_rE",
            XposedConfigKeys.regex(Type.Alarm, "com.example.app", 0)
        )
    }

    @Test
    fun `regex values map all hook types`() {
        val appSt = AppSt(
            packageName = "com.example.app",
            rE_Wakelock = setOf("wl.*"),
            rE_Alarm = setOf("alarm.*"),
            rE_Service = setOf("svc.*")
        )

        val values = XposedConfigKeys.regexValues(appSt)

        assertEquals(setOf("wl.*"), values[Type.Wakelock])
        assertEquals(setOf("alarm.*"), values[Type.Alarm])
        assertEquals(setOf("svc.*"), values[Type.Service])
    }
}
