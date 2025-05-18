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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import java.lang.StringBuilder

class AppDasAR(
    private val appInfoDao: AppInfoDao,
    private val daDao: DADao,
    private val infoEventDao: InfoEventDao
) : AppDasRepo {

    private val pm: PackageManager = context.packageManager
    private val um = context.getSystemService(Context.USER_SERVICE) as UserManager
    private val launcherApps = context.getSystemService<LauncherApps>()!!
    
    // 简单的内存缓存，用于存储频繁访问的数据
    // 缓存结构: [cacheKey -> Pair(data, timestamp)]
    private val cache = mutableMapOf<String, Pair<List<AppWithStats>, Long>>()
    
    // 缓存过期时间 - 30秒
    private val CACHE_EXPIRATION_MS = 30 * 1000L
    
    /**
     * 根据查询参数生成缓存键
     */
    private fun generateCacheKey(sortBy: String, userId: Int? = null, filter: String? = null): String {
        val sb = StringBuilder(sortBy)
        if (userId != null) sb.append("_user").append(userId)
        if (filter != null) sb.append("_filter").append(filter)
        return sb.toString()
    }
    
    /**
     * 检查缓存是否存在且有效
     */
    private fun getCachedData(cacheKey: String): List<AppWithStats>? {
        val cachedEntry = cache[cacheKey] ?: return null
        val (data, timestamp) = cachedEntry
        
        // 检查缓存是否过期
        return if (System.currentTimeMillis() - timestamp <= CACHE_EXPIRATION_MS) {
            LogUtil.d("AppDasAR", "Cache hit for $cacheKey")
            data
        } else {
            // 缓存过期，移除
            cache.remove(cacheKey)
            LogUtil.d("AppDasAR", "Cache expired for $cacheKey")
            null
        }
    }
    
    /**
     * 更新缓存
     */
    private fun updateCache(cacheKey: String, data: List<AppWithStats>) {
        cache[cacheKey] = Pair(data, System.currentTimeMillis())
        LogUtil.d("AppDasAR", "Cache updated for $cacheKey, size=${data.size}")
        
        // 简单的缓存大小管理 - 如果条目过多，移除最旧的
        if (cache.size > 20) {
            val oldestKey = cache.entries.minByOrNull { it.value.second }?.key
            oldestKey?.let {
                cache.remove(it)
                LogUtil.d("AppDasAR", "Removed oldest cache entry: $it")
            }
        }
    }
    
    /**
     * 清除所有缓存
     */
    private fun clearAllCache() {
        val size = cache.size
        cache.clear()
        LogUtil.d("AppDasAR", "Cleared all cache, entries=$size")
    }

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
        val dbAppInfos = getDBAppInfos()//db AppInfos
        val sysAppInfos = getInstalledAppInfos()//system AppInfos

        // get difference set to update and delete
        val insertKeysSize = sysAppInfos.keys.subtract(dbAppInfos.keys).size
        val deleteKeysSize = dbAppInfos.keys.subtract(sysAppInfos.keys).size
        
        if (insertKeysSize > 0 || deleteKeysSize > 0) {
            // 如果应用列表发生了变化，清除所有缓存
            clearAllCache()
            LogUtil.d("AppDasAR", "Cleared cache due to app list changes: inserted=$insertKeysSize, deleted=$deleteKeysSize")
        }
        
        insertAll(sysAppInfos.keys subtract dbAppInfos.keys, sysAppInfos)
        deleteAll(dbAppInfos.keys subtract sysAppInfos.keys, dbAppInfos)
    }

    override suspend fun syncInfos(): Unit = withContext(Dispatchers.IO) {
        val args = Bundle().let {
            it.putString("type", Type.UnKnow.value)
            it.putString("packageName", "")
            it
        }

        val result = getCPResult(context, ProviderMethod.LoadInfos.value, args)
        result?.let {
            try {
                val infos = result.getSerializable("infos") as Array<Info>?
                infos?.toList()?.let {
                    if (it.isNotEmpty()) {
                        daDao.insert(it)
                        // 如果有大量信息变更，清除所有缓存
                        if (it.size > 5) {
                            clearAllCache()
                            LogUtil.d("AppDasAR", "Cleared cache due to info changes: count=${it.size}")
                        }
                    }
                }
            } catch (e: Exception) {
                getCPResult(context, ProviderMethod.ClearData.value, Bundle())
                LogUtil.d("AppDasAR", "getSerializable err: $e")
            } finally {
                LogUtil.d("AppDasAR", "getSerializable err clearAll")
            }
        }
    }

    /**
     * Gets all applications with their wakelock statistics
     */
    override fun getAppsWithStats(): Flow<List<AppWithStats>> {
        // 基础方法不使用缓存，因为它是其他方法的基础
        return appInfoDao.loadAppInfosDBFlow().distinctUntilChanged().map { appInfoList ->
            // Transform to AppWithStats by looking up wake lock info for each app
            appInfoList.map { appInfo ->
                appInfoToAppWithStats(appInfo)
            }
        }
    }

    /**
     * Gets applications sorted by application name
     */
    override fun getAppsWithStatsSortedByName(): Flow<List<AppWithStats>> {
        // 为排序后的数据创建缓存键
        val cacheKey = generateCacheKey("NAME")
        
        // 检查缓存
        val cachedData = getCachedData(cacheKey)
        if (cachedData != null) {
            // 如果缓存命中，返回包含缓存数据的Flow
            return flow { emit(cachedData) }
        }
        
        // 缓存未命中，执行正常查询并更新缓存
        return getAppsWithStats().map { list ->
            val sortedList = list.sortedBy { it.appInfo.label }
            // 更新缓存
            updateCache(cacheKey, sortedList)
            sortedList
        }
    }

    /**
     * Gets applications sorted by wakelock count (descending)
     */
    override fun getAppsWithStatsSortedByCount(): Flow<List<AppWithStats>> {
        // 为排序后的数据创建缓存键
        val cacheKey = generateCacheKey("COUNT")
        
        // 检查缓存
        val cachedData = getCachedData(cacheKey)
        if (cachedData != null) {
            // 如果缓存命中，返回包含缓存数据的Flow
            return flow { emit(cachedData) }
        }
        
        // 缓存未命中，执行正常查询并更新缓存
        return getAppsWithStats().map { list ->
            val sortedList = list.sortedByDescending { it.wakelockCount }
            // 更新缓存
            updateCache(cacheKey, sortedList)
            sortedList
        }
    }

    /**
     * Gets applications sorted by wakelock time (descending)
     */
    override fun getAppsWithStatsSortedByTime(): Flow<List<AppWithStats>> {
        // 为排序后的数据创建缓存键
        val cacheKey = generateCacheKey("TIME")
        
        // 检查缓存
        val cachedData = getCachedData(cacheKey)
        if (cachedData != null) {
            // 如果缓存命中，返回包含缓存数据的Flow
            return flow { emit(cachedData) }
        }
        
        // 缓存未命中，执行正常查询并更新缓存
        return getAppsWithStats().map { list ->
            val sortedList = list.sortedByDescending { it.wakelockTime }
            // 更新缓存
            updateCache(cacheKey, sortedList)
            sortedList
        }
    }

    /**
     * Gets only user (non-system) applications with their stats
     */
    override fun getUserAppsWithStats(): Flow<List<AppWithStats>> {
        // 为过滤后的数据创建缓存键
        val cacheKey = generateCacheKey("BASE", filter = "USER")
        
        // 检查缓存
        val cachedData = getCachedData(cacheKey)
        if (cachedData != null) {
            // 如果缓存命中，返回包含缓存数据的Flow
            return flow { emit(cachedData) }
        }
        
        // 缓存未命中，执行正常查询并更新缓存
        return getAppsWithStats().map { list ->
            val filteredList = list.filter { !it.appInfo.system }
            // 更新缓存
            updateCache(cacheKey, filteredList)
            filteredList
        }
    }

    /**
     * Gets only system applications with their stats
     */
    override fun getSystemAppsWithStats(): Flow<List<AppWithStats>> {
        // 为过滤后的数据创建缓存键
        val cacheKey = generateCacheKey("BASE", filter = "SYSTEM")
        
        // 检查缓存
        val cachedData = getCachedData(cacheKey)
        if (cachedData != null) {
            // 如果缓存命中，返回包含缓存数据的Flow
            return flow { emit(cachedData) }
        }
        
        // 缓存未命中，执行正常查询并更新缓存
        return getAppsWithStats().map { list ->
            val filteredList = list.filter { it.appInfo.system }
            // 更新缓存
            updateCache(cacheKey, filteredList)
            filteredList
        }
    }

    /**
     * Gets only applications that have wakelock activity
     */
    override fun getModifiedAppsWithStats(): Flow<List<AppWithStats>> {
        // 为过滤后的数据创建缓存键
        val cacheKey = generateCacheKey("BASE", filter = "MODIFIED")
        
        // 检查缓存
        val cachedData = getCachedData(cacheKey)
        if (cachedData != null) {
            // 如果缓存命中，返回包含缓存数据的Flow
            return flow { emit(cachedData) }
        }
        
        // 缓存未命中，执行正常查询并更新缓存
        return getAppsWithStats().map { list ->
            val filteredList = list.filter { it.wakelockCount > 0 }
            // 更新缓存
            updateCache(cacheKey, filteredList)
            filteredList
        }
    }

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

    // delete appinfos that are no longer installed
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

    // get current app data from database
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
        val easting = pm.getApplicationEnabledSetting(ai.packageName)
        val enabled = ai.enabled &&
                (easting == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT ||
                        easting == PackageManager.COMPONENT_ENABLED_STATE_ENABLED)
        val persistent =
            ai.flags and ApplicationInfo.FLAG_PERSISTENT != 0 || "android" == ai.packageName
        val system = ai.flags and
                (ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0

        return AppInfo(
            ai.packageName,
            ai.uid,
            pm.getApplicationLabel(ai) as String,
            ai.icon,
            system,
            enabled,
            persistent,
            ai.processName,
            getUserId(ai.uid)
        )
    }

    // get all installed app data from system
    private fun getInstalledAppInfos(): ArrayMap<String, AppInfo> {
        val sysAppInfos = ArrayMap<String, AppInfo>()
        val users = um.userProfiles
        users.forEach { uh: UserHandle ->
            val userId = uh::class.java.getMethod("getIdentifier").invoke(uh) as Int
            launcherApps.getActivityList(null, uh).forEach {
                val packageName = it.applicationInfo.packageName
                if ("mediatek.bluetooth" != packageName) {
                    val key = "${packageName}_${userId}"
                    if (!sysAppInfos.containsKey(key)) {
                        val sysAi = getSysAppInfo(it.applicationInfo)
                        sysAppInfos[key] = sysAi
                    }
                }
            }
        }
        return sysAppInfos
    }

    /**
     * 获取所有可用的用户ID
     */
    override suspend fun getAvailableUsers(): List<UserInfo> = withContext(Dispatchers.IO) {
        val users = mutableListOf<UserInfo>()
        
        // 添加主用户 (ID 0)
        users.add(UserInfo.createPrimaryUser())
        
        // 从数据库获取其他用户ID
        val userIds = appInfoDao.getDistinctUserIds()
        
        // 添加所有非零用户ID
        userIds.filter { it != 0 }.forEach { userId ->
            users.add(UserInfo.fromUserId(userId))
        }
        
        return@withContext users
    }
}