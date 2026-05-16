package com.js.nowakelock.data.repository.appdas

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.js.nowakelock.BasicApp
import com.js.nowakelock.data.db.AppDatabase
import com.js.nowakelock.data.db.Type
import com.js.nowakelock.data.db.entity.AppInfo
import com.js.nowakelock.data.db.entity.Info
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDasARSortingAndroidTest {
    private lateinit var db: AppDatabase
    private lateinit var repository: AppDasAR

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
        BasicApp.context = context
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = AppDasAR(
            appInfoDao = db.appInfoDao(),
            daDao = db.dADao(),
            infoEventDao = db.infoEventDao()
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun countSortMatchesDisplayedTotalCount() = runBlocking {
        db.appInfoDao().insert(
            listOf(
                AppInfo(packageName = "pkg.alpha", label = "Alpha", userId = 0),
                AppInfo(packageName = "pkg.beta", label = "Beta", userId = 0)
            )
        )

        db.dADao().insert(
            listOf(
                Info(
                    name = "wl_alpha",
                    type = Type.Wakelock,
                    packageName = "pkg.alpha",
                    count = 3,
                    userId = 0
                ),
                Info(
                    name = "wl_beta",
                    type = Type.Wakelock,
                    packageName = "pkg.beta",
                    count = 1,
                    userId = 0
                ),
                Info(
                    name = "alarm_beta",
                    type = Type.Alarm,
                    packageName = "pkg.beta",
                    count = 9,
                    userId = 0
                )
            )
        )

        val apps = repository.getAppsWithStatsSortedByCount().first()

        assertEquals(
            "When the screen says Count, the ordering should follow the count shown to the user.",
            listOf("Beta", "Alpha"),
            apps.map { it.appInfo.label }
        )
        assertEquals(
            listOf(10, 3),
            apps.map { it.wakelockCount + it.alarmCount + it.serviceCount }
        )
    }
}
