package com.js.nowakelock.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.js.nowakelock.data.db.entity.AppInfo
import com.js.nowakelock.data.db.entity.AppSt
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDaDao : BaseDao<AppSt> {
    /**
     * FOR Backup
     */
    @Query("select * from appSt")
    suspend fun loadAllAppSts(): List<AppSt>
    
    /**
     * Gets the AppSt for a specific package and user
     */
    @Query("SELECT * FROM appSt WHERE packageName_st = :packageName AND userId_appSt = :userId")
    fun getAppSt(packageName: String, userId: Int): Flow<AppSt?>
}