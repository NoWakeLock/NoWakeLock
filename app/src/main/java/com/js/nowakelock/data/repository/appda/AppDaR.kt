package com.js.nowakelock.data.repository.appda

import com.js.nowakelock.base.LogUtil
import com.js.nowakelock.data.db.dao.AppDaDao
import com.js.nowakelock.data.db.entity.AppDA
import com.js.nowakelock.data.db.entity.AppSt
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.zip

//class AppDaR(private val appDaDao: AppDaDao) : AppDaRepo {
//    override fun getAppDa(packageName: String, userId: Int): Flow<AppDA> {
//        val appInfo = appDaDao.loadAppInfo(packageName, userId)
////        val appCount = appDaDao.loadAppCount(packageName, userId)
//        val appSt = appDaDao.loadAppSt(packageName, userId)
//
//        return appInfo.zip(appSt) { it, st ->
//            AppDA(it, st ?: AppSt(packageName = it.packageName, userId = it.userId))
//        }
//    }
//
//    override fun getAppSt(packageName: String, userId: Int): Flow<AppSt> =
//        appDaDao.loadAppSt(packageName, userId).distinctUntilChanged().map {
//            it ?: AppSt(packageName = packageName, userId = userId)
//        }
//
//    override suspend fun setAppSt(appSt: AppSt) {
//        appDaDao.insert(appSt)
//    }
//}