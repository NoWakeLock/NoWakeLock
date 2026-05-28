package com.js.nowakelock.data.repository.appdas

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.UserHandle
import android.os.UserManager
import androidx.collection.ArrayMap
import androidx.core.content.getSystemService
import com.js.nowakelock.BasicApp.Companion.context
import com.js.nowakelock.base.LogUtil
import com.js.nowakelock.base.calculateTime
import com.js.nowakelock.base.getCPResult
import com.js.nowakelock.base.getUserId
import com.js.nowakelock.data.db.Type
import com.js.nowakelock.data.db.dao.AppInfoDao
import com.js.nowakelock.data.db.dao.DADao
import com.js.nowakelock.data.db.dao.InfoEventDao
import com.js.nowakelock.data.db.entity.*
import com.js.nowakelock.data.model.AppWithStats
import com.js.nowakelock.data.model.UserInfo
import com.js.nowakelock.data.provider.ProviderMethod
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

class AppDasAR(
    private val appInfoDao: AppInfoDao,
    private val daDao: DADao,
    private val infoEventDao: InfoEventDao
) : AppDasRepo {
    private val appNameComparator =
        compareBy<AppWithStats>({ it.appInfo.label.lowercase() }, { it.appInfo.packageName }, { it.appInfo.userId })
    private val appCountComparator =
        compareByDescending<AppWithStats> { it.totalDisplayedCount() }
            .then(appNameComparator)
    private val appTimeComparator =
        compareByDescending<AppWithStats> { it.wakelockTime }
            .then(appNameComparator)

    private val pm: PackageManager = context.packageManager
    private val um = context.getSystemService(Context.USER_SERVICE) as UserManager
    private val launcherApps = context.getSystemService<LauncherApps>()!!

    override fun getAppDAs(): Flow<List<AppDA>> {
        return appInfoDao.loadAppInfosDBFlow().distinctUntilChanged().map { appInfos ->
            appInfos.map { appInfo ->
                AppDA(
                    appInfo,
                    AppSt(packageName = appInfo.packageName, userId = appInfo.userId)
                )
            }
        }
    }

    override suspend fun getAppInfo(packageName: String, useId: Int): AppInfo =
        appInfoDao.loadAppInfo(packageName, useId)

    override suspend fun syncAppInfos() = withContext(Dispatchers.Default) {
        // Launch both operations in parallel using async coroutines
        val dbAppInfosDeferred = async { getDBAppInfos() }
        val sysAppInfosDeferred = async { getInstalledAppInfos() }
        
        // Wait for both operations to complete and get results
        val dbAppInfos = dbAppInfosDeferred.await()
        val sysAppInfos = sysAppInfosDeferred.await()

        // Process the results - insert new app infos and delete removed ones
        insertAll(sysAppInfos.keys subtract dbAppInfos.keys, sysAppInfos)
        deleteAll(dbAppInfos.keys subtract sysAppInfos.keys, dbAppInfos)
    }

    override suspend fun syncInfos(): Unit = withContext(Dispatchers.IO) {
        val args = Bundle().let {
            it.putString("type", Type.UnKnow.value)
            it.putString("packageName", "")
            it
        }

        try {
            val result = getCPResult(context, ProviderMethod.LoadInfos.value, args)
            result?.let {
                try {
                    val infos = result.getSerializable("infos") as Array<Info>?
                    infos?.toList()?.let {
                        daDao.insert(it)
                    }
                } catch (e: Exception) {
                    LogUtil.e("AppDasAR", "getSerializable err: $e")
                } finally {
                    LogUtil.d("AppDasAR", "syncInfos attempt finished")
                }
            }
        } catch (e: Exception) {
            LogUtil.e("AppDasAR", "syncInfos failed: ${e.message}")
        }
    }

    // Base flow for app statistics that's shared by sorting/filtering methods
    private val baseAppsWithStatsFlow = appInfoDao.loadAppInfosDBFlow()
        .distinctUntilChanged()
        .map { appInfoList ->
            // Process all apps in parallel using coroutineScope
            coroutineScope {
                val semaphore = kotlinx.coroutines.sync.Semaphore(10) // Limit concurrency to prevent OOM
                // Create async tasks for each app processing
                appInfoList.map { appInfo ->
                    // Each app is processed in a separate async coroutine
                    async {
                        semaphore.withPermit {
                            appInfoToAppWithStats(appInfo)
                        }
                    }
                }.awaitAll() // Wait for all apps to be processed
            }
        }
        .buffer() // Add buffer to optimize producer-consumer relationship
        .flowOn(Dispatchers.IO) // Ensure background thread processing
        
    /**
     * Gets all applications with their wakelock statistics
     * Optimized to process applications in parallel and with Flow optimizations
     */
    override fun getAppsWithStats(): Flow<List<AppWithStats>> {
        return baseAppsWithStatsFlow
    }

    /**
     * Gets applications sorted by application name
     * Uses the optimized base flow
     */
    override fun getAppsWithStatsSortedByName(): Flow<List<AppWithStats>> {
        return baseAppsWithStatsFlow.map { list ->
            list.sortedWith(appNameComparator)
        }
    }

    /**
     * Gets applications sorted by wakelock count (descending)
     * Uses the optimized base flow
     */
    override fun getAppsWithStatsSortedByCount(): Flow<List<AppWithStats>> {
        return baseAppsWithStatsFlow.map { list ->
            list.sortedWith(appCountComparator)
        }
    }

    /**
     * Gets applications sorted by wakelock time (descending)
     * Uses the optimized base flow
     */
    override fun getAppsWithStatsSortedByTime(): Flow<List<AppWithStats>> {
        return baseAppsWithStatsFlow.map { list ->
            list.sortedWith(appTimeComparator)
        }
    }

    /**
     * Gets only user (non-system) applications with their stats
     * Uses the optimized base flow
     */
    override fun getUserAppsWithStats(): Flow<List<AppWithStats>> {
        return baseAppsWithStatsFlow.map { list ->
            list.filter { !it.appInfo.system }
        }
    }

    /**
     * Gets only system applications with their stats
     * Uses the optimized base flow
     */
    override fun getSystemAppsWithStats(): Flow<List<AppWithStats>> {
        return baseAppsWithStatsFlow.map { list ->
            list.filter { it.appInfo.system }
        }
    }

    /**
     * Gets only applications that have wakelock activity
     * Uses the optimized base flow
     */
    override fun getModifiedAppsWithStats(): Flow<List<AppWithStats>> {
        return baseAppsWithStatsFlow.map { list ->
            list.filter { it.wakelockCount > 0 }
        }
    }

    /**
     * Helper method to convert AppInfo to AppWithStats by querying wakelock data
     * Optimized to perform parallel queries for better performance
     */
    private suspend fun appInfoToAppWithStats(appInfo: AppInfo): AppWithStats =
        withContext(Dispatchers.IO) {
            val packageName = appInfo.packageName
            val userId = appInfo.userId

            // Direct database query bypassing sync failures
            val infoDb = com.js.nowakelock.data.db.InfoDatabase.getInstance(context)

            // Parallelize the database queries using async coroutines
            val wakelockInfosDeferred = async { 
                infoDb.infoDao().loadInfos(packageName, Type.Wakelock, userId) 
            }
            val wakelockEventsDeferred = async { 
                infoDb.infoEventDao().loadEvents(packageName, Type.Wakelock, userId) 
            }
            val alarmInfosDeferred = async { 
                infoDb.infoDao().loadInfos(packageName, Type.Alarm, userId) 
            }
            val serviceInfosDeferred = async { 
                infoDb.infoDao().loadInfos(packageName, Type.Service, userId) 
            }

            // Await the results of all queries
            val wakelockInfos = wakelockInfosDeferred.await()
            val wakelockEvents = wakelockEventsDeferred.await()
            val alarmInfos = alarmInfosDeferred.await()
            val serviceInfos = serviceInfosDeferred.await()

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

    // insert all new appinfos
    private suspend fun insertAll(
        packageNames: Set<String>,
        sysAppInfos: ArrayMap<String, AppInfo>
    ) = withContext(Dispatchers.IO) {
        if (packageNames.isNotEmpty()) {
            sysAppInfos.filter { it.key in packageNames }.let {
                appInfoDao.insert(it.values as MutableCollection<AppInfo>)
            }
        }
    }

    // delete all uninstalled appinfos
    private suspend fun deleteAll(
        packageNames: Set<String>,
        dbAppInfos: ArrayMap<String, AppInfo>
    ) = withContext(Dispatchers.IO) {
        if (packageNames.isNotEmpty()) {
            dbAppInfos.filter { it.key in packageNames }.let {
                appInfoDao.delete(it.values as MutableCollection<AppInfo>)
            }
        }
    }

    // get all system appinfos
    @SuppressLint("QueryPermissionsNeeded")
    private suspend fun getInstalledAppInfos(): ArrayMap<String, AppInfo> =
        withContext(Dispatchers.IO) {
            val sysAppInfo = ArrayMap<String, AppInfo>()

            val userList: List<UserHandle> = um.userProfiles

            for (user in userList) {

                if (user.hashCode() == 0) {
                    pm.getInstalledApplications(0).forEach {
                        sysAppInfo["${it.packageName}_${getUserId(it.uid)}"] = getSysAppInfo(it)
                    }
                } else {
                    launcherApps.getActivityList(null, user).map {
                        sysAppInfo["${it.applicationInfo.packageName}_${getUserId(it.applicationInfo.uid)}"] =
                            getSysAppInfo(it.applicationInfo)
                    }
                }
            }

            return@withContext sysAppInfo
        }

    // get all AppInfos
    private suspend fun getDBAppInfos(): ArrayMap<String, AppInfo> =
        withContext(Dispatchers.IO) {
            val dbAppInfos = ArrayMap<String, AppInfo>()
            appInfoDao.loadAppInfosDB().forEach {
                dbAppInfos["${it.packageName}_${it.userId}"] = it
            }
            return@withContext dbAppInfos
        }

    //get single AppInfo
    private fun getSysAppInfo(ai: ApplicationInfo): AppInfo {
        // Try to get enabled setting, fall back to ai.enabled for cross-user apps on Android 16+
        val enabled = try {
            val easting = pm.getApplicationEnabledSetting(ai.packageName)
            ai.enabled &&
                    (easting == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT ||
                            easting == PackageManager.COMPONENT_ENABLED_STATE_ENABLED)
        } catch (e: IllegalArgumentException) {
            ai.enabled
        }

        val persistent =
            ai.flags and ApplicationInfo.FLAG_PERSISTENT != 0 || "android" == ai.packageName
        val system = ai.flags and
                (ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0

        // Try to get label, fall back for cross-user apps on Android 16+
        val label = try {
            pm.getApplicationLabel(ai) as String
        } catch (e: IllegalArgumentException) {
            try {
                ai.loadLabel(pm).toString()
            } catch (e: Exception) {
                ai.packageName
            }
        }

        return AppInfo(
            ai.packageName,
            ai.uid,
            label,
            ai.icon,
            system,
            enabled,
            persistent,
            ai.processName,
            getUserId(ai.uid)
        )
    }

    override suspend fun getAvailableUsers(): List<UserInfo> = withContext(Dispatchers.IO) {
        try {
            // get all userid
            val userIds = appInfoDao.getDistinctUserIds()

            // if no user, return primary user
            if (userIds.isEmpty()) {
                return@withContext listOf(UserInfo.createPrimaryUser())
            }

            // convert userid to UserInfo object
            return@withContext userIds.map { userId ->
                UserInfo.fromUserId(userId)
            }
        } catch (e: Exception) {
            LogUtil.e("AppDasAR", "Error getting available users: ${e.message}")
            // if error, return primary user
            return@withContext listOf(UserInfo.createPrimaryUser())
        }
    }

    private fun AppWithStats.totalDisplayedCount(): Int {
        return wakelockCount + alarmCount + serviceCount
    }
}
