package com.js.nowakelock.ui.navigation

import com.js.nowakelock.data.db.Type

/**
 * 定义应用导航路由
 */
object NavRoutes {
    val ARG_DA_TYPE: Type = Type.UnKnow
    val ARG_PACKAGE_NAME: String = "packageName"
    val ARG_USER_ID: String = "userId"
    val ARG_DA_NAME: String = "daName"
    const val APPS = "apps"
    const val WAKELOCKS = "wakelocks"
    const val ALARMS = "alarms"
    const val SERVICES = "services"
    const val SETTINGS = "settings"
} 