package com.js.nowakelock.ui.navigation

import com.js.nowakelock.data.db.Type
import kotlinx.serialization.Serializable

/**
 * 定义应用导航路由
 */
object NavRoutes {
    const val APPS = "apps"
    const val WAKELOCKS = "wakelocks"
    const val ALARMS = "alarms"
    const val SERVICES = "services"
    const val SETTINGS = "settings"

    const val DADETAIL = "DADetail"
}

/**
 * Serializable navigation destination for the DA detail screen
 * 
 * @param daName DA项目名称
 * @param packageName 包名（可选）
 * @param userId 用户ID（默认为0）
 * @param type DA类型
 */
@Serializable
data class DADetail(
    val daName: String,
    val packageName: String? = null,
    val userId: Int = 0,
    val type: String = Type.UnKnow.value
) 