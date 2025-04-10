package com.js.nowakelock.data.db.dao

import androidx.room.*
import com.js.nowakelock.data.db.entity.AppDA
import com.js.nowakelock.data.db.entity.AppInfo
import com.js.nowakelock.data.db.entity.Info
import kotlinx.coroutines.flow.Flow

@Dao
interface AppInfoDao : BaseDao<AppInfo> {
//    @Transaction
//    @Query(
//        "SELECT * FROM appInfo left outer join appcount on appInfo.packageName = appcount.packageName_count and appInfo.userId = appcount.userId_count "
//    )
//    fun loadAIACs(): Flow<Map<AppInfo, AppCount?>>

//    @Transaction
//    @Query(
//        "SELECT * FROM appInfo left outer join appcount on appInfo.packageName = appcount.packageName_count and appInfo.userId = appcount.userId_count " +
//                "where userId != 0"
//    )
//    fun loadWorkApps(): Flow<Map<AppInfo, AppCount?>>

//    @Query("select * from appInfo where userId = :userId")
//    fun loadAppInfosDB(userId: Int): List<AppInfo>

    @Query("select * from appInfo")
    fun loadAppInfosDBFlow(): Flow<List<AppInfo>>

    @Query("select * from appInfo")
    fun loadAppInfosDB(): List<AppInfo>


    @Query("select * from appInfo where packageName = :packageName and userId = :userId")
    suspend fun loadAppInfo(packageName: String, userId: Int = 0): AppInfo

//    @Query("select * from AppCount")
//    suspend fun loadAppCount(): List<AppCount>

    @Query("SELECT DISTINCT userId FROM appInfo ORDER BY userId")
    suspend fun getDistinctUserIds(): List<Int>
}