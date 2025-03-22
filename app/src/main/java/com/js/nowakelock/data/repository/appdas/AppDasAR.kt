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
import com.js.nowakelock.BasicApp
import com.js.nowakelock.BasicApp.Companion.context
import com.js.nowakelock.base.LogUtil
import com.js.nowakelock.base.getCPResult
import com.js.nowakelock.base.getUserId
import com.js.nowakelock.data.db.Type
import com.js.nowakelock.data.db.dao.AppInfoDao
import com.js.nowakelock.data.db.dao.DADao
import com.js.nowakelock.data.db.entity.*
import com.js.nowakelock.data.model.AppWithStats
import com.js.nowakelock.data.provider.ProviderMethod
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext

class AppDasAR(private val appInfoDao: AppInfoDao, private val daDao: DADao) : AppDasRepo {

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
        val dbAppInfos = getDBAppInfos()//db AppInfos
        val sysAppInfos = getSysAppInfos()//system AppInfos

        //取差集更新删除
        insertAll(sysAppInfos.keys subtract dbAppInfos.keys, sysAppInfos)
        deleteAll(dbAppInfos.keys subtract sysAppInfos.keys, dbAppInfos)
    }

    override suspend fun syncInfos(): Unit = withContext(Dispatchers.IO) {
        val args = Bundle().let {
            it.putString("type", Type.UnKnow.value)
            it.putString("packageName", "")
            it
        }

        val result = getCPResult(BasicApp.context, ProviderMethod.LoadInfos.value, args)
        result?.let {
            try {
                val infos = result.getSerializable("infos") as Array<Info>?
                infos?.toList()?.let {
                    daDao.insert(it)
                }
            }catch (e:Exception){
                getCPResult(BasicApp.context, ProviderMethod.ClearAll.value, Bundle())
                LogUtil.d("AppDasAR", "getSerializable err: $e")
            }finally {
                LogUtil.d("AppDasAR", "getSerializable err clearAll")
            }
        }
    }

    /**
     * Gets all applications with their wakelock statistics
     */
    override fun getAppsWithStats(): Flow<List<AppWithStats>> {
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
        return getAppsWithStats().map { list ->
            list.sortedBy { it.appInfo.label }
        }
    }

    /**
     * Gets applications sorted by wakelock count (descending)
     */
    override fun getAppsWithStatsSortedByCount(): Flow<List<AppWithStats>> {
        return getAppsWithStats().map { list ->
            list.sortedByDescending { it.wakelockCount }
        }
    }

    /**
     * Gets applications sorted by wakelock time (descending)
     */
    override fun getAppsWithStatsSortedByTime(): Flow<List<AppWithStats>> {
        return getAppsWithStats().map { list ->
            list.sortedByDescending { it.wakelockTime }
        }
    }

    /**
     * Gets only user (non-system) applications with their stats
     */
    override fun getUserAppsWithStats(): Flow<List<AppWithStats>> {
        return getAppsWithStats().map { list ->
            list.filter { !it.appInfo.system }
        }
    }

    /**
     * Gets only system applications with their stats
     */
    override fun getSystemAppsWithStats(): Flow<List<AppWithStats>> {
        return getAppsWithStats().map { list ->
            list.filter { it.appInfo.system }
        }
    }

    /**
     * Gets only applications that have wakelock activity
     */
    override fun getModifiedAppsWithStats(): Flow<List<AppWithStats>> {
        return getAppsWithStats().map { list ->
            list.filter { it.wakelockCount > 0 }
        }
    }

    /**
     * Helper method to convert AppInfo to AppWithStats by querying wakelock data
     */
    private suspend fun appInfoToAppWithStats(appInfo: AppInfo): AppWithStats = withContext(Dispatchers.IO) {
        // Get all wake lock infos for this app
        val wakelockInfos = daDao.getInfosByPackageAndType(
            appInfo.packageName,
            Type.Wakelock,
            appInfo.userId
        )
        
        // Calculate statistics
        val wakelockCount = wakelockInfos.sumOf { it.count }
        val wakelockBlockedCount = wakelockInfos.sumOf { it.blockCount }
        val wakelockTime = wakelockInfos.sumOf { it.countTime }
        val wakelockNames = wakelockInfos.map { it.name }
        
        return@withContext AppWithStats(
            appInfo = appInfo,
            wakelockCount = wakelockCount,
            wakelockBlockedCount = wakelockBlockedCount,
            wakelockTime = wakelockTime,
            wakelockNames = wakelockNames
        )
    }

    /**db 插入新应用*/
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

    /**db 删除卸载应用*/
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

    // 获取全部 system AppInfos
    @SuppressLint("QueryPermissionsNeeded")
    private suspend fun getSysAppInfos(): ArrayMap<String, AppInfo> =
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

    // 获取全部数据库 AppInfos
    private suspend fun getDBAppInfos(): ArrayMap<String, AppInfo> =
        withContext(Dispatchers.IO) {
            val dbAppInfos = ArrayMap<String, AppInfo>()
            appInfoDao.loadAppInfosDB().forEach {
                dbAppInfos["${it.packageName}_${it.userId}"] = it
            }
            return@withContext dbAppInfos
        }

    //获取单个 AppInfo
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
}