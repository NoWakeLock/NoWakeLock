package com.js.nowakelock.data.broadcastreceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.js.nowakelock.BasicApp
import com.js.nowakelock.base.getCPResult
import com.js.nowakelock.data.db.AppDatabase
import com.js.nowakelock.data.db.dao.DADao
import com.js.nowakelock.data.db.dao.InfoDao
import com.js.nowakelock.data.db.dao.InfoEventDao
import com.js.nowakelock.data.provider.ProviderMethod
import com.js.nowakelock.data.repository.preferences.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class PowerConnectionReceiver : BroadcastReceiver(), KoinComponent {
    
    // 使用Koin注入UserPreferencesRepository
    private val userPreferencesRepository: UserPreferencesRepository by inject()

    override fun onReceive(context: Context, intent: Intent) {
        // 使用runBlocking获取设置值，因为BroadcastReceiver不支持挂起函数
        val powerFlag = runBlocking { userPreferencesRepository.powerFlag.first() }
        val clearFlag = runBlocking { userPreferencesRepository.clearFlag.first() }

        if (intent.action.equals(Intent.ACTION_POWER_DISCONNECTED)) {//POWER DISCONNECTED
            CoroutineScope(Dispatchers.IO).launch {
                clear(powerFlag, clearFlag)
            }
        } else if (intent.action.equals(Intent.ACTION_POWER_CONNECTED)) { //POWER CONNECTED
            CoroutineScope(Dispatchers.IO).launch {
                clear(powerFlag, clearFlag)
            }
        }
    }

    private suspend fun clear(powerFlag: Boolean, clearFlag: Boolean) =
        withContext(Dispatchers.IO) {
            if (powerFlag) {
                // Run independent operations in parallel using structured concurrency
                coroutineScope {
                    // Start independent operations in parallel
                    val clearCPAllJob = async { clearCPAll() }
                    
                    val db = AppDatabase.getInstance(BasicApp.context)
                    val clearCountJob = async { clearCount(db.infoDao()) }
                    val clearEventJob = async { clearEvent(db.infoEventDao()) }
                    
                    // Wait for all parallel operations to complete
                    clearCPAllJob.await()
                    clearCountJob.await()
                    clearEventJob.await()
                    
                    // This operation depends on previous operations and runs after they complete
                    if (clearFlag) {
                        clearNoActive(db.dADao())
                    }
                }
            }
        }

    // clear all count
    private suspend fun clearCount(infoDao: InfoDao) {
        infoDao.rstAllCount()
        infoDao.rstAllBlockCount()
        infoDao.rstAllCountTime()
    }

    private suspend fun clearEvent(eventDao: InfoEventDao){
        eventDao.clearAll()
    }

    // clear all no active(no exist in appDb.st)
    private suspend fun clearNoActive(daDao: DADao) {
        daDao.clearNoActive()
    }

    // clear infoDb
    private fun clearCPAll() {
        val args = Bundle()
        getCPResult(BasicApp.context, ProviderMethod.ClearData.value, args)
    }
}
