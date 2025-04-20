package com.js.nowakelock.data.repository.appDetail

import com.js.nowakelock.data.db.Type
import com.js.nowakelock.data.db.dao.AppInfoDao
import com.js.nowakelock.data.db.dao.DADao
import com.js.nowakelock.data.db.dao.InfoEventDao
import com.js.nowakelock.data.db.entity.AppInfo
import com.js.nowakelock.data.model.AppWithStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class AppDetailRepositoryImpl(
    private val appInfoDao: AppInfoDao,
    private val daDao: DADao,
    private val infoEventDao: InfoEventDao
) : AppDetailRepository {
    override fun getAppsWithStat(packageName: String, userId: Int): Flow<AppWithStats> {
        return appInfoDao.loadAppInfoFw(packageName, userId).distinctUntilChanged().map { appInfo ->
            appInfoToAppWithStats(appInfo)
        }
    }

    /**
     * Helper method to convert AppInfo to AppWithStats by querying wakelock data
     */
    private suspend fun appInfoToAppWithStats(appInfo: AppInfo): AppWithStats = withContext(
        Dispatchers.IO
    ) {
        // Get all wake lock infos for this app
        val wakelockInfos = daDao.getInfosByPackageAndType(
            appInfo.packageName,
            Type.Wakelock,
            appInfo.userId
        )

        val alarmInfos = daDao.getInfosByPackageAndType(
            appInfo.packageName,
            Type.Alarm,
            appInfo.userId
        )

        val serviceInfos = daDao.getInfosByPackageAndType(
            appInfo.packageName,
            Type.Service,
            appInfo.userId
        )

        // Calculate statistics
        val wakelockCount = wakelockInfos.sumOf { it.count }
        val wakelockBlockedCount = wakelockInfos.sumOf { it.blockCount }
        val wakelockTime = wakelockInfos.sumOf { it.countTime }
        val wakelockNames = wakelockInfos.map { it.name }

        val alarmCount = alarmInfos.sumOf { it.count }
        val alarmBlockedCount = alarmInfos.sumOf { it.blockCount }

        val serviceCount = serviceInfos.sumOf { it.count }
        val serviceBlockedCount = serviceInfos.sumOf { it.blockCount }
        

        return@withContext AppWithStats(
            appInfo = appInfo,
            wakelockCount = wakelockCount,
            wakelockBlockedCount = wakelockBlockedCount,
            wakelockTime = wakelockTime,
            wakelockNames = wakelockNames,
            alarmCount = alarmCount,
            alarmBlockedCount = alarmBlockedCount,
            serviceCount = serviceCount,
            serviceBlockedCount = serviceBlockedCount
        )
    }
}