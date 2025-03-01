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

//class ServiceDaoTest {
//    @get:Rule
//    var instantTaskExecutorRule = InstantTaskExecutorRule()
//
//    private lateinit var dao: ServiceDao
//    private lateinit var db: AppDatabase
//
//    @Before
//    fun setUp() {
//        val context = ApplicationProvider.getApplicationContext<Context>()
//        db = Room.inMemoryDatabaseBuilder(
//            context, AppDatabase::class.java
//        ).allowMainThreadQueries()
//            .build()
//        dao = db.serviceDao()
//    }
//
//    @After
//    fun tearDown() {
//        db.close()
//    }
//
//    @Test
//    fun testInfo() {
//        runBlocking {
//            dao.insert(TestData.serviceStm)
//        }
//        val a = runBlocking { dao.loadServices().asLiveData() }
//        val b = runBlocking { dao.loadServices(TestData.pN).asLiveData() }
//        val c = runBlocking { dao.loadService("s1") }
//        val d = runBlocking { dao.loadAllServices() }
//
//        Assert.assertEquals(LiveDataTestUtil.getValue(a).size, 10)
//        Assert.assertEquals(LiveDataTestUtil.getValue(b).size, 10)
//        Assert.assertEquals(c!!.count, 1)
//        Assert.assertEquals(d.size, 10)
//
//        runBlocking {
//            dao.delete(TestData.serviceStm[0])
//        }
//        Assert.assertEquals(LiveDataTestUtil.getValue(a).size, 9)
//
//        runBlocking {
//            dao.delete(TestData.serviceStm)
//        }
//        Assert.assertEquals(LiveDataTestUtil.getValue(a).size, 0)
//    }
//
//    @Test
//    fun testST() {
//        runBlocking {
//            dao.insertST(TestData.serviceSt)
//        }
//        val a = runBlocking { dao.loadServiceSt() }
//        val b = runBlocking { dao.loadServiceSt("test") }
//
//        Assert.assertEquals(a.size, 1)
//        Assert.assertEquals(b!!.flag, false)
//        Assert.assertEquals(b.allowTimeinterval, 10)
//    }
//}