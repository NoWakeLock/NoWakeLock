package com.js.nowakelock.data.db

import android.content.Context
import androidx.room.*
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 13,
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
            .addMigrations(MIGRATION_10_13, MIGRATION_11_13, MIGRATION_12_13)
            .build()

        /**
         * Shared migration implementation to rebuild the info_event table
         * This function encapsulates the common migration logic for all paths to version 13
         * 
         * @param db The database to migrate
         * @param fromVersion The version we're migrating from (for logging purposes)
         */
        private fun performInfoEventTableMigration(db: SupportSQLiteDatabase, fromVersion: Int) {
            // Log migration start
            android.util.Log.i("AppDatabase", "Starting migration from version $fromVersion to 13")
            
            // Wrap the entire migration in a transaction for atomicity
            db.beginTransaction()
            try {
                // Step 1: Try to drop all existing indexes on the info_event table
                try {
                    db.execSQL("DROP INDEX IF EXISTS index_info_event_key")
                    db.execSQL("DROP INDEX IF EXISTS index_info_event_package_type_time")
                    db.execSQL("DROP INDEX IF EXISTS index_info_event_name_type_userId")
                    db.execSQL("DROP INDEX IF EXISTS index_info_event_name_type_userId_time")
                    android.util.Log.d("AppDatabase", "Successfully dropped all indexes")
                } catch (e: Exception) {
                    // Just log the error and continue, as this is a non-critical step
                    android.util.Log.e("AppDatabase", "Error dropping indexes: ${e.message}")
                }
                
                // Step 2: Delete all data from the table (if it exists)
                try {
                    db.execSQL("DELETE FROM info_event")
                    android.util.Log.d("AppDatabase", "Deleted all data from info_event")
                } catch (e: Exception) {
                    // Table might not exist, just log and continue
                    android.util.Log.d("AppDatabase", "Could not delete data: ${e.message}")
                }
                
                // Step 3: Drop the existing table entirely
                try {
                    db.execSQL("DROP TABLE IF EXISTS info_event")
                    android.util.Log.d("AppDatabase", "Dropped the info_event table")
                } catch (e: Exception) {
                    // This should not fail with IF EXISTS, but log just in case
                    android.util.Log.e("AppDatabase", "Error dropping table: ${e.message}")
                    // If we can't drop the table, we should not proceed
                    throw e
                }
                
                // Step 4: Create a new table with the correct structure for version 13
                try {
                    db.execSQL(
                        "CREATE TABLE IF NOT EXISTS info_event (" +
                                "instanceId TEXT PRIMARY KEY NOT NULL DEFAULT '', " +
                                "name_event TEXT NOT NULL DEFAULT '', " +
                                "type_event TEXT NOT NULL DEFAULT '', " +
                                "packageName_event TEXT NOT NULL DEFAULT '', " +
                                "userId_event INTEGER NOT NULL DEFAULT 0, " +
                                "startTime INTEGER NOT NULL DEFAULT 0, " +
                                "endTime INTEGER, " +
                                "isBlocked INTEGER NOT NULL DEFAULT 0)"
                    )
                    android.util.Log.d("AppDatabase", "Created new info_event table")
                } catch (e: Exception) {
                    // If we can't create the table, the migration fails
                    android.util.Log.e("AppDatabase", "Error creating table: ${e.message}")
                    throw e
                }
                
                // Step 5: Create all the indexes for the new table
                try {
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
                    android.util.Log.d("AppDatabase", "Created all required indexes")
                } catch (e: Exception) {
                    // If we can't create the indexes, the migration fails
                    android.util.Log.e("AppDatabase", "Error creating indexes: ${e.message}")
                    throw e
                }
                
                // Mark the transaction as successful
                db.setTransactionSuccessful()
                android.util.Log.i("AppDatabase", "Migration completed successfully")
            } catch (e: Exception) {
                // If any critical step fails, the entire migration fails
                android.util.Log.e("AppDatabase", "Migration failed: ${e.message}")
                throw e
            } finally {
                // End the transaction (will roll back if not marked successful)
                db.endTransaction()
                android.util.Log.d("AppDatabase", "Migration transaction ended")
            }
        }

        /**
         * Migration from version 10 to 13
         * Completely rebuilds the info_event table with the new structure
         */
        private val MIGRATION_10_13 = object : Migration(10, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Use the shared implementation for migrating the info_event table
                performInfoEventTableMigration(db, 10)
            }
        }
        
        /**
         * Migration from version 11 to 13
         * Completely rebuilds the info_event table with the new structure
         */
        private val MIGRATION_11_13 = object : Migration(11, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Use the shared implementation for migrating the info_event table
                performInfoEventTableMigration(db, 11)
            }
        }
        
        /**
         * Migration from version 12 to 13
         * Completely rebuilds the info_event table with the new structure
         */
        private val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Use the shared implementation for migrating the info_event table
                performInfoEventTableMigration(db, 12)
            }
        }
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

//    @DeleteColumn.Entries(
//        DeleteColumn(tableName = "info_event", columnName = "id"),
//        DeleteColumn(tableName = "info_event", columnName = "eventKey")
//    )
//    class C12To13 : AutoMigrationSpec
}