package com.js.nowakelock.data.repository.daitem

import com.js.nowakelock.data.db.entity.St
import com.js.nowakelock.data.model.DAItem
import kotlinx.coroutines.flow.Flow

interface DARepository {
    /**
     * Retrieves all DAItem sorted by name
     */
    suspend fun getDAItemsSortedByName(
        packageName: String = "",
        userId: Int = -1
    ): Flow<List<DAItem>>

    /**
     * Retrieves all DAItems sorted by count
     */
    suspend fun getDAItemsSortedByCount(
        packageName: String = "",
        userId: Int = -1
    ): Flow<List<DAItem>>

    /**
     * Retrieves all DAItems sorted by time
     */
    suspend fun getDAItemsSortedByTime(
        packageName: String = "",
        userId: Int = -1
    ): Flow<List<DAItem>>

    /**
     * Updates DAItem settings (block status and time window)
     * @param setting Setting to update
     */
    suspend fun updateDAItemSettings(
        setting: St
    )

    /**
     * Synchronizes wakelock data with the system
     */
    suspend fun syncDB(packageName: String = "", userId: Int = -1)
}