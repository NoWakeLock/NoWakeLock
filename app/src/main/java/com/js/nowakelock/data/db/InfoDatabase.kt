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
            .addMigrations(MIGRATION_10_12, MIGRATION_11_12)
            .build()
            
        /**
         * Shared migration implementation to rebuild the info_event table
         * This function encapsulates the common migration logic for all paths to version 12
         * 
         * @param db The database to migrate
         * @param fromVersion The version we're migrating from (for logging purposes)
         */
        private fun performInfoEventTableMigration(db: SupportSQLiteDatabase, fromVersion: Int) {
            // Log migration start
            android.util.Log.i("InfoDatabase", "Starting migration from version $fromVersion to 12")
            
            // Wrap the entire migration in a transaction for atomicity
            db.beginTransaction()
            try {
                // Step 1: Try to drop all existing indexes on the info_event table
                try {
                    db.execSQL("DROP INDEX IF EXISTS index_info_event_key")
                    db.execSQL("DROP INDEX IF EXISTS index_info_event_package_type_time")
                    db.execSQL("DROP INDEX IF EXISTS index_info_event_name_type_userId")
                    db.execSQL("DROP INDEX IF EXISTS index_info_event_name_type_userId_time")
                    android.util.Log.d("InfoDatabase", "Successfully dropped all indexes")
                } catch (e: Exception) {
                    // Just log the error and continue, as this is a non-critical step
                    android.util.Log.e("InfoDatabase", "Error dropping indexes: ${e.message}")
                }
                
                // Step 2: Delete all data from the table (if it exists)
                try {
                    db.execSQL("DELETE FROM info_event")
                    android.util.Log.d("InfoDatabase", "Deleted all data from info_event")
                } catch (e: Exception) {
                    // Table might not exist, just log and continue
                    android.util.Log.d("InfoDatabase", "Could not delete data: ${e.message}")
                }
                
                // Step 3: Drop the existing table entirely
                try {
                    db.execSQL("DROP TABLE IF EXISTS info_event")
                    android.util.Log.d("InfoDatabase", "Dropped the info_event table")
                } catch (e: Exception) {
                    // This should not fail with IF EXISTS, but log just in case
                    android.util.Log.e("InfoDatabase", "Error dropping table: ${e.message}")
                    // If we can't drop the table, we should not proceed
                    throw e
                }
                
                // Step 4: Create a new table with the correct structure for version 12
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
                    android.util.Log.d("InfoDatabase", "Created new info_event table")
                } catch (e: Exception) {
                    // If we can't create the table, the migration fails
                    android.util.Log.e("InfoDatabase", "Error creating table: ${e.message}")
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
                    android.util.Log.d("InfoDatabase", "Created all required indexes")
                } catch (e: Exception) {
                    // If we can't create the indexes, the migration fails
                    android.util.Log.e("InfoDatabase", "Error creating indexes: ${e.message}")
                    throw e
                }
                
                // Mark the transaction as successful
                db.setTransactionSuccessful()
                android.util.Log.i("InfoDatabase", "Migration completed successfully")
            } catch (e: Exception) {
                // If any critical step fails, the entire migration fails
                android.util.Log.e("InfoDatabase", "Migration failed: ${e.message}")
                throw e
            } finally {
                // End the transaction (will roll back if not marked successful)
                db.endTransaction()
                android.util.Log.d("InfoDatabase", "Migration transaction ended")
            }
        }

        /**
         * Migration from version 10 to 12
         * Completely rebuilds the info_event table with the new structure
         */
        private val MIGRATION_10_12 = object : Migration(10, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Use the shared implementation for migrating the info_event table
                performInfoEventTableMigration(db, 10)
            }
        }
        
        /**
         * Migration from version 11 to 12
         * Completely rebuilds the info_event table with the new structure
         */
        private val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Use the shared implementation for migrating the info_event table
                performInfoEventTableMigration(db, 11)
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