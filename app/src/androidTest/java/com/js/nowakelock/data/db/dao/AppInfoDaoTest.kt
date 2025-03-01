package com.js.nowakelock.data.db.dao

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
//import com.js.nowakelock.data.TestData
import com.js.nowakelock.data.db.AppDatabase
import com.js.nowakelock.tool.LiveDataTestUtil
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AppInfoDaoTest {
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var dao: AppInfoDao
    private lateinit var db: AppDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java
        ).allowMainThreadQueries()
            .build()
        dao = db.appInfoDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

//    @Test
//    fun load_without_Insert() {
//        val appInfos = dao.loadAppInfos()
//        assertTrue(LiveDataTestUtil.getValue(appInfos).isEmpty())
//    }

//    @Test
//    @Throws(Exception::class)
//    fun loadAll() {
//
//        runBlocking {
//            dao.insert(TestData.appInfos)
//        }
//
//        assertEquals(runBlocking {
//            LiveDataTestUtil.getValue(dao.loadAppInfos())
//        }, TestData.appInfos)
//    }

//    @Test
//    @Throws(Exception::class)
//    fun load() {
//        runBlocking {
//            dao.insert(TestData.appInfos[0])
//        }
//
//        assertEquals(
//            runBlocking { dao.loadAppInfo(TestData.appInfos[0].packageName) },
//            TestData.appInfos[0]
//        )
//    }

//    @Test
//    @Throws(Exception::class)
//    fun load_st() {
//        runBlocking {
//            dao.insert(TestData.appInfoST)
//        }
//
//        assertEquals(
//            LiveDataTestUtil.getValue(dao.loadAppSetting(TestData.appInfoST.packageName)),
//            TestData.appInfoST
//        )
//    }
}