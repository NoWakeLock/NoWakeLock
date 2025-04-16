package com.js.nowakelock.data.db

import android.content.Context
import androidx.room.*
import androidx.room.migration.AutoMigrationSpec
import com.js.nowakelock.data.db.converters.SetConvert
import com.js.nowakelock.data.db.converters.TypeConvert
import com.js.nowakelock.data.db.dao.AppInfoDao
import com.js.nowakelock.data.db.dao.AppDaDao
import com.js.nowakelock.data.db.dao.DADao
import com.js.nowakelock.data.db.dao.InfoDao
import com.js.nowakelock.data.db.dao.InfoEventDao
import com.js.nowakelock.data.db.entity.*

@Database(
    entities = [
        AppInfo::class, AppSt::class, St::class, Info::class, InfoEvent::class
    ],
    version = 12,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4),
        AutoMigration(from = 4, to = 5, spec = AppDatabase.C4To5::class),
        AutoMigration(from = 9, to = 10),
        AutoMigration(from = 10, to = 11, spec = AppDatabase.C10To11::class),
        AutoMigration(from = 11, to = 12),
    ]
)
@TypeConverters(SetConvert::class, TypeConvert::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appInfoDao(): AppInfoDao
    abstract fun appDaDao(): AppDaDao
    abstract fun dADao(): DADao
    abstract fun infoDao(): InfoDao
    abstract fun infoEventDao(): InfoEventDao

    companion object {
        private const val DATABASE_NAME = "noWakelock_db"

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: buildInstance(context).also {
                    instance = it
                }
            }

        private fun buildInstance(context: Context) = Room.databaseBuilder(
            context.applicationContext, AppDatabase::class.java,
            DATABASE_NAME
        )
//            .addMigrations()
            .fallbackToDestructiveMigration(true) //if version change, it will delete all data.
            .build()
    }

    @RenameColumn(
        tableName = "info", fromColumnName = "userId", toColumnName = "userid_info"
    )
    @RenameColumn(
        tableName = "st", fromColumnName = "userId", toColumnName = "userid_st"
    )
    @RenameColumn(
        tableName = "appSt", fromColumnName = "userId", toColumnName = "userId_appSt"
    )
    class C4To5 : AutoMigrationSpec

    @RenameColumn(
        tableName = "st", fromColumnName = "flag", toColumnName = "fullBlock"
    )
    @RenameColumn(
        tableName = "st", fromColumnName = "flagLock", toColumnName = "screenOffBlock"
    )
    @RenameColumn(
        tableName = "st", fromColumnName = "allowTimeInterval", toColumnName = "timeWindowMs"
    )
    class C10To11 : AutoMigrationSpec
}