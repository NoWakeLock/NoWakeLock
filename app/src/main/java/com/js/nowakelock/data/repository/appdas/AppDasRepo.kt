package com.js.nowakelock.data.repository.appdas

import com.js.nowakelock.data.db.entity.AppDA
import com.js.nowakelock.data.db.entity.AppInfo
import com.js.nowakelock.data.model.AppWithStats
import kotlinx.coroutines.flow.Flow

interface AppDasRepo {
    fun getAppDAs(): Flow<List<AppDA>>
    suspend fun getAppInfo(packageName: String, useId: Int): AppInfo
    suspend fun syncAppInfos()
    suspend fun syncInfos()
    
    /**
     * Gets all applications with their wakelock statistics
     */
    fun getAppsWithStats(): Flow<List<AppWithStats>>
    
    /**
     * Gets applications sorted by application name
     */
    fun getAppsWithStatsSortedByName(): Flow<List<AppWithStats>>
    
    /**
     * Gets applications sorted by wakelock count (descending)
     */
    fun getAppsWithStatsSortedByCount(): Flow<List<AppWithStats>>
    
    /**
     * Gets applications sorted by wakelock time (descending)
     */
    fun getAppsWithStatsSortedByTime(): Flow<List<AppWithStats>>
    
    /**
     * Gets only user (non-system) applications with their stats
     */
    fun getUserAppsWithStats(): Flow<List<AppWithStats>>
    
    /**
     * Gets only system applications with their stats
     */
    fun getSystemAppsWithStats(): Flow<List<AppWithStats>>
    
    /**
     * Gets only applications that have wakelock activity
     */
    fun getModifiedAppsWithStats(): Flow<List<AppWithStats>>
}