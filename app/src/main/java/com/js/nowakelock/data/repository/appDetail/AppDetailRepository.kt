package com.js.nowakelock.data.repository.appDetail

import com.js.nowakelock.data.model.AppWithStats
import kotlinx.coroutines.flow.Flow

interface AppDetailRepository {
    fun getAppsWithStat(packageName: String, userId: Int):Flow<AppWithStats>
}