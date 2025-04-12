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
    
    // DA Detail 路由，包含必要的参数
    const val DA_DETAIL = "da_detail/{daName}?packageName={packageName}&userId={userId}&type={type}"
    
    /**
     * 生成DA详情路由路径的辅助函数
     *
     * @param daName DA项目名称
     * @param packageName 包名（可选）
     * @param userId 用户ID（默认为0）
     * @param type DA类型
     * @return 格式化的导航路由字符串
     */
    fun daDetail(
        daName: String, 
        packageName: String? = null,
        userId: Int = 0,
        type: Type
    ): String {
        val baseRoute = "da_detail/$daName"
        val packageNameParam = if (!packageName.isNullOrEmpty()) "packageName=$packageName" else null
        val userIdParam = "userId=$userId"
        val typeParam = "type=${type.value}"
        
        val params = listOfNotNull(packageNameParam, userIdParam, typeParam)
        return if (params.isEmpty()) baseRoute else "$baseRoute?${params.joinToString("&")}"
    }
} 