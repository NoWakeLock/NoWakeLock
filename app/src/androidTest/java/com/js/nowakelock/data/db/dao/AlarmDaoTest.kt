package com.js.nowakelock.data.db.dao

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.asLiveData
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
//import com.js.nowakelock.data.TestData
import com.js.nowakelock.data.db.AppDatabase
import com.js.nowakelock.tool.LiveDataTestUtil
import kotlinx.coroutines.runBlocking
import org.junit.*

//class AlarmDaoTest {
//    @get:Rule
//    var instantTaskExecutorRule = InstantTaskExecutorRule()
//
//    private lateinit var alDao: AlarmDao
//    private lateinit var db: AppDatabase
//
//    @Before
//    fun setUp() {
//        val context = ApplicationProvider.getApplicationContext<Context>()
//        db = Room.inMemoryDatabaseBuilder(
//            context, AppDatabase::class.java
//        ).allowMainThreadQueries()
//            .build()
//        alDao = db.alarmDao()
//    }
//
//    @After
//    fun tearDown() {
//        db.close()
//    }
//
//    @Test
//    fun testAlarm() {
//        runBlocking {
//            alDao.insert(TestData.alarms)
//        }
//        val a = runBlocking { alDao.loadAlarms().asLiveData() }
//        val b = runBlocking { alDao.loadAlarms(TestData.pN).asLiveData() }
//        val c = runBlocking { alDao.loadAlarm("a1") }
//        val d = runBlocking { alDao.loadAllAlarms() }
//
//        Assert.assertEquals(LiveDataTestUtil.getValue(a).size, 10)
//        Assert.assertEquals(LiveDataTestUtil.getValue(b).size, 10)
//        Assert.assertEquals(c!!.count, 1)
//        Assert.assertEquals(d.size, 10)
//
//        runBlocking {
//            alDao.delete(TestData.alarms[0])
//        }
//        Assert.assertEquals(LiveDataTestUtil.getValue(a).size, 9)
//
//        runBlocking {
//            alDao.delete(TestData.alarms)
//        }
//        Assert.assertEquals(LiveDataTestUtil.getValue(a).size, 0)
//    }
//
//    @Test
//    fun testAlarm_st() {
//        runBlocking {
//            alDao.insertST(TestData.alarmST)
//        }
//        val a = runBlocking { alDao.loadAlarmSt() }
//        val b = runBlocking { alDao.loadAlarmSt("test") }
//
//        Assert.assertEquals(a.size, 1)
//        Assert.assertEquals(b!!.flag, false)
//        Assert.assertEquals(b.allowTimeinterval, 10)
//    }
//}