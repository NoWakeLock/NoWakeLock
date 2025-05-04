package com.js.nowakelock.data.repository.appDetail

import com.js.nowakelock.base.LogUtil
import com.js.nowakelock.base.calculateTime
import com.js.nowakelock.data.db.Type
import com.js.nowakelock.data.db.dao.AppDaDao
import com.js.nowakelock.data.db.dao.AppInfoDao
import com.js.nowakelock.data.db.dao.DADao
import com.js.nowakelock.data.db.dao.InfoEventDao
import com.js.nowakelock.data.db.entity.AppInfo
import com.js.nowakelock.data.db.entity.AppSt
import com.js.nowakelock.data.model.AppWithStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class AppDetailRepositoryImpl(
    private val appInfoDao: AppInfoDao,
    private val daDao: DADao,
    private val infoEventDao: InfoEventDao,
    private val appDaDao: AppDaDao
) : AppDetailRepository {
    override fun getAppsWithStat(packageName: String, userId: Int): Flow<AppWithStats> {
        return appInfoDao.loadAppInfoFw(packageName, userId).distinctUntilChanged().map { appInfo ->
            appInfoToAppWithStats(appInfo)
        }
    }

    /**
     * Gets the AppSt for the specified package and user
     * @return Flow of AppSt or null if not found
     */
    override fun getAppSt(packageName: String, userId: Int): Flow<AppSt?> {
        return appDaDao.getAppSt(packageName, userId).distinctUntilChanged().flowOn(Dispatchers.IO)
            .catch { e ->
                // Log error but don't throw to prevent UI crashes
                android.util.Log.e("AppDetailRepository", "Error loading AppSt: ${e.message}", e)
                emit(null)
            }
    }

    /**
     * Updates or inserts an AppSt entity
     * @return true if successful, false otherwise
     */
    override suspend fun updateAppSt(appSt: AppSt): Boolean = withContext(Dispatchers.IO) {
        try {
            appDaDao.insert(appSt)
            true
        } catch (e: Exception) {
            LogUtil.e("AppDetailRepository", "Error updating AppSt: ${e.message}")
            false
        }
    }

    /**
     * Helper method to convert AppInfo to AppWithStats by querying wakelock data
     */
    /**
     * Helper method to convert AppInfo to AppWithStats by querying wakelock data
     */
    private suspend fun appInfoToAppWithStats(appInfo: AppInfo): AppWithStats =
        withContext(Dispatchers.IO) {
            // Get all data for this app with the same package and user ID
            val packageName = appInfo.packageName
            val userId = appInfo.userId

            // Fetch all relevant data types
            val wakelockInfos = daDao.getInfosByPackageAndType(packageName, Type.Wakelock, userId)
            val wakelockEvents = infoEventDao.getEventsByApp(packageName, Type.Wakelock, userId)
            val alarmInfos = daDao.getInfosByPackageAndType(packageName, Type.Alarm, userId)
            val serviceInfos = daDao.getInfosByPackageAndType(packageName, Type.Service, userId)

            // Calculate statistics for each data type
            val wakelockCount = wakelockInfos.sumOf { it.count }
            val wakelockBlockedCount = wakelockInfos.sumOf { it.blockCount }
            val wakelockTime = calculateTime(wakelockEvents)

            val alarmCount = alarmInfos.sumOf { it.count }
            val alarmBlockedCount = alarmInfos.sumOf { it.blockCount }

            val serviceCount = serviceInfos.sumOf { it.count }
            val serviceBlockedCount = serviceInfos.sumOf { it.blockCount }

            AppWithStats(
                appInfo = appInfo,
                wakelockCount = wakelockCount,
                wakelockBlockedCount = wakelockBlockedCount,
                wakelockTime = wakelockTime,
                alarmCount = alarmCount,
                alarmBlockedCount = alarmBlockedCount,
                serviceCount = serviceCount,
                serviceBlockedCount = serviceBlockedCount
            )
        }
}