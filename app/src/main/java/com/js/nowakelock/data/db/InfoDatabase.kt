package com.js.nowakelock.data.db

import android.content.Context
import androidx.room.*
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 12,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4),
        AutoMigration(from = 4, to = 5, spec = InfoDatabase.C4To5::class),
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
            .addMigrations(MIGRATION_11_12)
            .build()
            
        private val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DELETE FROM info_event")
                
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS info_event_new (" +
                    "instanceId TEXT PRIMARY KEY NOT NULL DEFAULT '', " +
                    "name_event TEXT NOT NULL DEFAULT '', " +
                    "type_event TEXT NOT NULL DEFAULT '', " +
                    "packageName_event TEXT NOT NULL DEFAULT '', " +
                    "userId_event INTEGER NOT NULL DEFAULT 0, " +
                    "startTime INTEGER NOT NULL DEFAULT 0, " +
                    "endTime INTEGER, " +
                    "isBlocked INTEGER NOT NULL DEFAULT 0)"
                )
                
                db.execSQL("DROP TABLE info_event")
                
                db.execSQL("ALTER TABLE info_event_new RENAME TO info_event")
                
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_info_event_package_type_time ON info_event (" +
                    "packageName_event, type_event, startTime)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_info_event_name_type_userId ON info_event (" +
                    "name_event, type_event, userId_event)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_info_event_name_type_userId_time ON info_event (" +
                    "name_event, type_event, userId_event, startTime)"
                )
            }
        }
    }

    @RenameColumn(
        tableName = "info", fromColumnName = "userId", toColumnName = "userid_info"
    )
    class C4To5 : AutoMigrationSpec

//    @DeleteColumn.Entries(
//        DeleteColumn(tableName = "info_event", columnName = "id"),
//        DeleteColumn(tableName = "info_event", columnName = "eventKey")
//    )
//    class C11To12 : AutoMigrationSpec
}