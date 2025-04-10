package com.js.nowakelock.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.js.nowakelock.data.db.entity.AppInfo
import com.js.nowakelock.data.db.entity.AppSt
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDaDao : BaseDao<AppSt> {

// todo: clean up
//    @Query("select * from appInfo where packageName = :packageName and userId = :userId")
//    fun loadAppInfo(packageName: String, userId: Int): Flow<AppInfo>
//
//    @Query("select * from appSt where packageName_st = :packageName and userId_appSt = :userId")
//    fun loadAppSt(packageName: String, userId: Int): Flow<AppSt?>

//    @Query("select * from AppCount where packageName_count = :packageName and userId_count = :userId")
//    fun loadAppCount(packageName: String, userId: Int): Flow<AppCount?>

    /**
     * FOR Backup
     */
    @Query("select * from appSt")
    suspend fun loadAllAppSts(): List<AppSt>
}