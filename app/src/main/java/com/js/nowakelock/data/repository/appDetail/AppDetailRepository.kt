package com.js.nowakelock.data.repository.appDetail

import com.js.nowakelock.data.db.entity.AppSt
import com.js.nowakelock.data.model.AppWithStats
import kotlinx.coroutines.flow.Flow

interface AppDetailRepository {
    /**
     * Gets app statistics information for the specified package and user
     */
    fun getAppsWithStat(packageName: String, userId: Int): Flow<AppWithStats>
    
    /**
     * Gets app settings data for the specified package and user
     * @return Flow that emits the AppSt or null if it doesn't exist
     */
    fun getAppSt(packageName: String, userId: Int): Flow<AppSt?>
    
    /**
     * Updates app settings in the database
     * @param appSt The app settings to update
     * @return true if update was successful, false otherwise
     */
    suspend fun updateAppSt(appSt: AppSt): Boolean
}