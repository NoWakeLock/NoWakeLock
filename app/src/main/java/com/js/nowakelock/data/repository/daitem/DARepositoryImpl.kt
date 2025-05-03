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

    /**
     * Maps the database entities to DAItem domain model
     */
    private fun mapToDAItems(infoWithSts: List<InfoWithSt>): List<DAItem> {
        return infoWithSts.map {
            DAItem.fromEntities(it.info, it.st)
        }
    }

    override suspend fun getDAItemsSortedByName(
        packageName: String, userId: Int
    ): Flow<List<DAItem>> = withContext(Dispatchers.IO) {
        if (packageName != "" && userId != -1) {
            return@withContext daDao.loadISs(packageName, type, userId)
                .distinctUntilChanged()
                .map { infoToStMap ->
                    mapToDAItems(infoToStMap).sortedBy { it.name.lowercase() }
                }
        }

        return@withContext daDao.loadISs(type).distinctUntilChanged()
            .map { infoToStMap ->
                mapToDAItems(infoToStMap).sortedBy { it.name.lowercase() }
            }
    }

    override suspend fun getDAItemsSortedByCount(
        packageName: String,
        userId: Int
    ): Flow<List<DAItem>> =
        withContext(Dispatchers.IO) {
            if (packageName != "" && userId != -1) {
                return@withContext daDao.loadISs(packageName, type, userId)
                    .distinctUntilChanged()
                    .map { infoToStMap ->
                        mapToDAItems(infoToStMap).sortedBy { it.count }
                    }
            }

            return@withContext daDao.loadISs(type).distinctUntilChanged()
                .map { infoToStMap ->
                    mapToDAItems(infoToStMap).sortedBy { it.count }
                }
        }

    override suspend fun getDAItemsSortedByTime(
        packageName: String,
        userId: Int
    ): Flow<List<DAItem>> =
        withContext(Dispatchers.IO) {
            if (packageName != "" && userId != -1) {
                return@withContext daDao.loadISs(packageName, type, userId).distinctUntilChanged()
                    .map { infoToStMap ->
                        mapToDAItems(infoToStMap).sortedBy { it.countTime }
                    }
            }

            return@withContext daDao.loadISs(type).distinctUntilChanged().map { infoToStMap ->
                mapToDAItems(infoToStMap).sortedBy { it.countTime }
            }
        }

    override suspend fun updateDAItemSettings(setting: St) = withContext(Dispatchers.IO) {
        daDao.insert(setting)
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