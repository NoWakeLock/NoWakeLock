package com.js.nowakelock.data.db

import android.content.Context
import androidx.room.*
import androidx.room.migration.AutoMigrationSpec
import com.js.nowakelock.data.db.converters.SetConvert
import com.js.nowakelock.data.db.converters.TypeConvert
import com.js.nowakelock.data.db.dao.InfoDao
import com.js.nowakelock.data.db.dao.InfoEventDao
import com.js.nowakelock.data.db.entity.Info
import com.js.nowakelock.data.db.entity.InfoEvent

@Database(
    entities = [
        Info::class,
        InfoEvent::class
    ],
    version = 11,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4),
        AutoMigration(from = 4, to = 5, spec = InfoDatabase.C4To5::class)
    ]
)
@TypeConverters(SetConvert::class, TypeConvert::class)
abstract class InfoDatabase : RoomDatabase() {
    abstract fun infoDao(): InfoDao
    abstract fun infoEventDao(): InfoEventDao

    companion object {
        private const val DATABASE_NAME = "info_db"

        @Volatile
        private var instance: InfoDatabase? = null

        fun getInstance(context: Context): InfoDatabase =
            instance ?: synchronized(this) {
                instance ?: buildInstance(context).also {
                    instance = it
                }
            }

        private fun buildInstance(context: Context) = Room.databaseBuilder(
            context, InfoDatabase::class.java,
            DATABASE_NAME
        )
            .fallbackToDestructiveMigration(true)
            .build()
    }

    @RenameColumn(
        tableName = "info", fromColumnName = "userId", toColumnName = "userid_info"
    )
    class C4To5 : AutoMigrationSpec
}