package com.js.nowakelock.data.repository.daitem

import android.os.Bundle
import androidx.lifecycle.viewModelScope
import com.js.nowakelock.BasicApp
import com.js.nowakelock.base.LogUtil
import com.js.nowakelock.base.SPTools
import com.js.nowakelock.base.getCPResult
import com.js.nowakelock.data.db.Type
import com.js.nowakelock.data.db.dao.DADao
import com.js.nowakelock.data.db.dao.InfoEventDao
import com.js.nowakelock.data.db.entity.Info
import com.js.nowakelock.data.db.entity.InfoEvent
import com.js.nowakelock.data.db.entity.InfoWithSt
import com.js.nowakelock.data.db.entity.St
import com.js.nowakelock.data.model.DAItem
import com.js.nowakelock.data.provider.ProviderMethod
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

open class DARepositoryImpl(
    private val daDao: DADao,
    private val infoEventDao: InfoEventDao
) : DARepository {
    open val type: Type = Type.UnKnow
    
    // Simple in-memory cache for frequently accessed data
    // Cache structure: [cacheKey -> Pair(data, timestamp)]
    private val cache = mutableMapOf<String, Pair<List<DAItem>, Long>>()
    
    // Cache expiration time - 30 seconds
    private val CACHE_EXPIRATION_MS = 30 * 1000L
    
    /**
     * Generates a cache key based on query parameters
     */
    private fun generateCacheKey(packageName: String, userId: Int, sortBy: String): String {
        return "${type.value}_${packageName}_${userId}_${sortBy}"
    }
    
    /**
     * Checks if cached data exists and is valid
     */
    private fun getCachedData(cacheKey: String): List<DAItem>? {
        val cachedEntry = cache[cacheKey] ?: return null
        val (data, timestamp) = cachedEntry
        
        // Check if cache is expired
        return if (System.currentTimeMillis() - timestamp <= CACHE_EXPIRATION_MS) {
            LogUtil.d("DARepositoryImpl", "Cache hit for $cacheKey")
            data
        } else {
            // Cache expired, remove it
            cache.remove(cacheKey)
            null
        }
    }
    
    /**
     * Updates the cache with new data
     */
    private fun updateCache(cacheKey: String, data: List<DAItem>) {
        cache[cacheKey] = Pair(data, System.currentTimeMillis())
        
        // Simple cache size management - if too many entries, remove oldest
        if (cache.size > 20) {
            val oldestKey = cache.entries.minByOrNull { it.value.second }?.key
            oldestKey?.let { cache.remove(it) }
        }
    }
    
    /**
     * Clears cache for a specific type
     */
    private fun clearCacheForType() {
        val keysToRemove = cache.keys.filter { it.startsWith(type.value) }
        keysToRemove.forEach { cache.remove(it) }
    }

    /**
     * Maps the database entities to DAItem domain model
     */
    private fun mapToDAItems(infoWithSts: List<InfoWithSt>): List<DAItem> {
        return infoWithSts.map {
            DAItem.fromEntities(it.info, it.st)
        }
    }

    /**
     * Helper function to compare two InfoWithSt lists for settings equality
     * Only compares fields that affect UI rendering to prevent unnecessary UI refreshes
     * @param list1 First list of InfoWithSt objects
     * @param list2 Second list of InfoWithSt objects
     * @return true if the settings fields are equal across both lists
     */
    private fun areInfoWithStsSettingsEqual(list1: List<InfoWithSt>, list2: List<InfoWithSt>): Boolean {
        if (list1.size != list2.size) return false
        
        return list1.zip(list2).all { (item1, item2) ->
            // Only compare fields that affect UI settings rendering
            item1.info.name == item2.info.name &&
            item1.info.packageName == item2.info.packageName &&
            item1.info.userId == item2.info.userId &&
            item1.st?.fullBlock == item2.st?.fullBlock &&
            item1.st?.screenOffBlock == item2.st?.screenOffBlock &&
            item1.st?.timeWindowMs == item2.st?.timeWindowMs
        }
    }

    override suspend fun getDAItemsSortedByName(
        packageName: String, userId: Int
    ): Flow<List<DAItem>> = withContext(Dispatchers.IO) {
        // Generate cache key
        val cacheKey = generateCacheKey(packageName, userId, "NAME")
        
        if (packageName != "" && userId != -1) {
            return@withContext daDao.loadISs(packageName, type, userId)
                // Use custom comparator to detect only relevant changes
                .distinctUntilChanged { old, new -> areInfoWithStsSettingsEqual(old, new) }
                .map { infoToStMap ->
                    val sortedItems = mapToDAItems(infoToStMap).sortedBy { it.name.lowercase() }
                    // Update cache with fresh data
                    updateCache(cacheKey, sortedItems)
                    sortedItems
                }
        }

        return@withContext daDao.loadISs(type)
            // Use custom comparator to detect only relevant changes
            .distinctUntilChanged { old, new -> areInfoWithStsSettingsEqual(old, new) }
            .map { infoToStMap ->
                val sortedItems = mapToDAItems(infoToStMap).sortedBy { it.name.lowercase() }
                // Update cache with fresh data
                updateCache(cacheKey, sortedItems)
                sortedItems
            }
    }

    override suspend fun getDAItemsSortedByCount(
        packageName: String,
        userId: Int
    ): Flow<List<DAItem>> =
        withContext(Dispatchers.IO) {
            // Generate cache key
            val cacheKey = generateCacheKey(packageName, userId, "COUNT")
            
            if (packageName != "" && userId != -1) {
                return@withContext daDao.loadISs(packageName, type, userId)
                    // Use custom comparator to detect only relevant changes
                    .distinctUntilChanged { old, new -> areInfoWithStsSettingsEqual(old, new) }
                    .map { infoToStMap ->
                        val sortedItems = mapToDAItems(infoToStMap).sortedByDescending { it.count }
                        // Update cache with fresh data
                        updateCache(cacheKey, sortedItems)
                        sortedItems
                    }
            }

            return@withContext daDao.loadISs(type)
                // Use custom comparator to detect only relevant changes
                .distinctUntilChanged { old, new -> areInfoWithStsSettingsEqual(old, new) }
                .map { infoToStMap ->
                    val sortedItems = mapToDAItems(infoToStMap).sortedByDescending { it.count }
                    // Update cache with fresh data
                    updateCache(cacheKey, sortedItems)
                    sortedItems
                }
        }

    override suspend fun getDAItemsSortedByTime(
        packageName: String,
        userId: Int
    ): Flow<List<DAItem>> =
        withContext(Dispatchers.IO) {
            // Generate cache key
            val cacheKey = generateCacheKey(packageName, userId, "TIME")
            
            if (packageName != "" && userId != -1) {
                return@withContext daDao.loadISs(packageName, type, userId)
                    // Use custom comparator to detect only relevant changes
                    .distinctUntilChanged { old, new -> areInfoWithStsSettingsEqual(old, new) }
                    .map { infoToStMap ->
                        val sortedItems = mapToDAItems(infoToStMap).sortedByDescending { it.countTime }
                        // Update cache with fresh data
                        updateCache(cacheKey, sortedItems)
                        sortedItems
                    }
            }

            return@withContext daDao.loadISs(type)
                // Use custom comparator to detect only relevant changes
                .distinctUntilChanged { old, new -> areInfoWithStsSettingsEqual(old, new) }
                .map { infoToStMap ->
                    val sortedItems = mapToDAItems(infoToStMap).sortedByDescending { it.countTime }
                    // Update cache with fresh data
                    updateCache(cacheKey, sortedItems)
                    sortedItems
                }
        }

    override suspend fun updateDAItemSettings(setting: St) = withContext(Dispatchers.IO) {
        daDao.insert(setting)
        // Clear cache since data has changed
        clearCacheForType()
    }

    override fun getSTs(type: Type): Flow<List<St>> {
        return daDao.loadSts(type)
    }

    override suspend fun syncDB(packageName: String, userId: Int) = withContext(Dispatchers.IO) {
        try {
            // Get all infos from content provider
            val args = Bundle().apply {
                putString("type", type.value)
                if (packageName != "") {
                    putString("packageName", packageName)
                }
                if (userId != -1) {
                    putInt("userId", userId)
                }
            }

            val result = getCPResult(BasicApp.context, ProviderMethod.LoadInfos.value, args)

            if (result != null) {
                try {
                    val infos = result.getSerializable("infos") as Array<Info>?
                    if (!infos.isNullOrEmpty()) {
                        // Insert all infos to database
                        daDao.insert(infos.toList())
                        // Clear cache since data has changed
                        clearCacheForType()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    // Clear data if error occurs
                    getCPResult(BasicApp.context, ProviderMethod.ClearData.value, Bundle())
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Sync InfoEvent data from XProvider to AppDatabase
     *
     * @param packageName optional package name filter, empty string means get all packages
     * @param userId optional user ID filter, -1 means no filter
     * @param startTime optional start time filter, 0 means no filter
     * @param endTime optional end time filter, 0 means no filter
     */
    override suspend fun syncEvents(
        packageName: String,
        userId: Int,
        startTime: Long,
        endTime: Long
    ) = withContext(Dispatchers.IO) {
        try {
            // prepare parameters
            val args = Bundle().apply {
                putString("type", type.value)
                if (packageName.isNotBlank()) {
                    putString("packageName", packageName)
                }
                if (userId != -1) {
                    putInt("userId", userId)
                }
                if (startTime > 0) {
                    putLong("startTime", startTime)
                }
                if (endTime > 0) {
                    putLong("endTime", endTime)
                }
            }

            // call the LoadEvents method of XProvider to get event data
            val result = getCPResult(BasicApp.context, ProviderMethod.LoadEvents.value, args)

            if (result != null) {
                try {
                    // parse the InfoEvent array in the result
                    val events = result.getSerializable("events") as Array<InfoEvent>?
                    if (!events.isNullOrEmpty()) {
                        // insert all events to the database
                        infoEventDao.insert(events.toList())
                        // Clear cache only if events are affecting UI data
                        if (events.size > 5) { // Only clear if significant number of events
                            clearCacheForType()
                        }
                        LogUtil.d("DARepositoryImpl", "Synced ${events.size} events")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    LogUtil.e("DARepositoryImpl", "Error syncing events: ${e.message}")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            LogUtil.e("DARepositoryImpl", "Error syncing events: ${e.message}")
        }
    }
}