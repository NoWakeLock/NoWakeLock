package com.js.nowakelock.data.db.network

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.js.nowakelock.data.db.AppDatabase
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test

//class DescriptionDaoTest {
//
//    private lateinit var db: AppDatabase
//    private lateinit var dd: DescriptionDao
//
//    private var ds: List<Description> =
//        listOf(
//            Description("d0", "zh", "p0", true, "i0"),
//            Description("d0", "en", "p0", true, "i1"),
//            Description("d2", "zh", "p0", false, "i3"),
//            Description("d3", "en", "p1", true, "i4")
//        )
//
//    @Before
//    fun setUp() {
//        val context = ApplicationProvider.getApplicationContext<Context>()
//        db = Room.inMemoryDatabaseBuilder(
//            context, AppDatabase::class.java
//        ).allowMainThreadQueries()
//            .build()
//        dd = db.descriptionDao()
//        runBlocking {
//            dd.insert(ds)
//        }
//    }
//
//    @After
//    fun tearDown() {
//        db.close()
//    }
//
//    @Test
//    fun get() {
//        val get = runBlocking {
//            dd.loadDescriptions()
//        }
//        assert(get.size == 4)
//
//        val get2 = runBlocking {
//            dd.loadDescription("d0")
//        }
//        assert(get2.size == 2)
//
//        val get3 = runBlocking {
//            dd.loadDescription("d3", "en")
//        }
//        assert(get3 == ds[3])
//    }
//
//    @Test
//    fun delete() {
//        runBlocking {
//            dd.delete(ds)
//        }
//
//        val get = runBlocking {
//            dd.loadDescriptions()
//        }
//
//        assert(get.isEmpty())
//    }
//
//}