package com.js.nowakelock.data.repository.daitem

import android.os.Bundle
import com.js.nowakelock.BasicApp
import com.js.nowakelock.base.LogUtil
import com.js.nowakelock.base.getCPResult
import com.js.nowakelock.data.db.Type
import com.js.nowakelock.data.db.dao.DADao
import com.js.nowakelock.data.db.entity.Info
import com.js.nowakelock.data.db.entity.St
import com.js.nowakelock.data.model.DAItem
import com.js.nowakelock.data.provider.ProviderMethod
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

open class DARepositoryImpl(
    private val daDao: DADao
) : DARepository {
    open val type: Type = Type.UnKnow

    init {
        LogUtil.e("DARepositoryImpl", "type: $type")
    }

    /**
     * Maps the database entities to DAItem domain model
     */
    private fun mapToDAItems(infoToStMap: Map<Info, St?>): List<DAItem> {
        return infoToStMap.map { (info, st) ->
            DAItem.fromEntities(info, st)
        }
    }

    override suspend fun getDAItemsSortedByName(
        packageName: String, userId: Int
    ): Flow<List<DAItem>> = withContext(Dispatchers.IO) {
        if (packageName != "" && userId != -1) {
            return@withContext daDao.loadISs(packageName, type, userId).distinctUntilChanged()
                .map { infoToStMap ->
                    mapToDAItems(infoToStMap).sortedBy { it.name.lowercase() }
                }
        }

        return@withContext daDao.loadISs(type).distinctUntilChanged().map { infoToStMap ->
            mapToDAItems(infoToStMap).sortedBy { it.name.lowercase() }
        }
    }

    override suspend fun getDAItemsSortedByCount(
        packageName: String,
        userId: Int
    ): Flow<List<DAItem>> =
        withContext(Dispatchers.IO) {
            if (packageName != "" && userId != -1) {
                return@withContext daDao.loadISs(packageName, type, userId).distinctUntilChanged()
                    .map { infoToStMap ->
                        mapToDAItems(infoToStMap).sortedByDescending { it.count }
                    }
            }

            return@withContext daDao.loadISs(type).distinctUntilChanged().map { infoToStMap ->
                mapToDAItems(infoToStMap).sortedByDescending { it.count }
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
                        mapToDAItems(infoToStMap).sortedByDescending { it.countTime }
                    }
            }

            return@withContext daDao.loadISs(type).distinctUntilChanged().map { infoToStMap ->
                mapToDAItems(infoToStMap).sortedByDescending { it.countTime }
            }
        }

    override suspend fun updateDAItemSettings(setting: St) = withContext(Dispatchers.IO) {
        daDao.insert(setting)
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
}