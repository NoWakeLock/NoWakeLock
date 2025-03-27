package com.js.nowakelock.data.repository.wakelock

import com.js.nowakelock.BasicApp
import com.js.nowakelock.base.getCPResult
import com.js.nowakelock.data.db.Type
import com.js.nowakelock.data.db.dao.DADao
import com.js.nowakelock.data.db.entity.Info
import com.js.nowakelock.data.db.entity.St
import com.js.nowakelock.data.model.WakelockItem
import com.js.nowakelock.data.provider.ProviderMethod
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import android.os.Bundle

/**
 * Implementation of WakelockRepository
 * Manages wakelock data using Room database and content provider
 */
class WakelockRepositoryImpl(
    private val daDao: DADao
) : WakelockRepository {

    /**
     * Maps the database entities to WakelockItem domain model
     */
    private fun mapToWakelockItems(infoToStMap: Map<Info, St?>): List<WakelockItem> {
        return infoToStMap.map { (info, st) ->
            WakelockItem.fromEntities(info, st)
        }
    }
    
    override fun getWakelocksSortedByName(): Flow<List<WakelockItem>> {
        return daDao.loadISs(Type.Wakelock)
            .distinctUntilChanged()
            .map { infoToStMap ->
                mapToWakelockItems(infoToStMap)
                    .sortedBy { it.name.lowercase() }
            }
    }
    
    override fun getWakelocksSortedByCount(): Flow<List<WakelockItem>> {
        return daDao.loadISs(Type.Wakelock)
            .distinctUntilChanged()
            .map { infoToStMap ->
                mapToWakelockItems(infoToStMap)
                    .sortedByDescending { it.count }
            }
    }
    
    override fun getWakelocksSortedByTime(): Flow<List<WakelockItem>> {
        return daDao.loadISs(Type.Wakelock)
            .distinctUntilChanged()
            .map { infoToStMap ->
                mapToWakelockItems(infoToStMap)
                    .sortedByDescending { it.countTime }
            }
    }
    
    override suspend fun updateWakelockSettings(
        name: String,
        packageName: String,
        userId: Int,
        isBlocked: Boolean,
        timeWindow: Int?
    ) = withContext(Dispatchers.IO) {
        // Create St entity with updated settings
        val st = St(
            name = name,
            type = Type.Wakelock,
            packageName = packageName,
            userId = userId,
            flag = isBlocked,
            // If isBlocked is false or timeWindow is null, set flag to false (fully blocked)
            // Otherwise, use the specified time window
            allowTimeInterval = if (isBlocked && timeWindow != null) {
                TimeUnit.SECONDS.toMillis(timeWindow.toLong())
            } else {
                0
            }
        )
        
        // Save to database
        daDao.insert(st)
    }
    
    override suspend fun syncWakelocks() = withContext(Dispatchers.IO) {
        try {
            // Get all wakelocks from content provider
            val args = Bundle().apply {
                putString("type", Type.Wakelock.value)
            }
            
            val result = getCPResult(BasicApp.context, ProviderMethod.LoadInfos.value, args)
            
            if (result != null) {
                try {
                    val infos = result.getSerializable("infos") as Array<Info>?
                    if (!infos.isNullOrEmpty()) {
                        // Insert all wakelocks to database
                        daDao.insert(infos.toList())
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
} 