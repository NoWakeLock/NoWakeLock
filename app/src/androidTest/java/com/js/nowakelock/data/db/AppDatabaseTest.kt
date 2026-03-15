package com.js.nowakelock.data.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {

    @Test
    fun inMemoryDatabase_opensAndExposesDaos() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        try {
            assertNotNull(db.appInfoDao())
            assertNotNull(db.appDaDao())
            assertNotNull(db.dADao())
            assertNotNull(db.infoDao())
            assertNotNull(db.infoEventDao())
        } finally {
            db.close()
        }
    }
}
