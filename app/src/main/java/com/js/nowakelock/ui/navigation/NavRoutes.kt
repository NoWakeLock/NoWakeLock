package com.js.nowakelock.ui.navigation

import com.js.nowakelock.data.db.Type
import com.js.nowakelock.ui.screens.apps.FilterOption
import com.js.nowakelock.ui.screens.apps.SortOption
import com.js.nowakelock.ui.screens.das.DAFilterOption
import com.js.nowakelock.ui.screens.das.DASortOption
import kotlinx.serialization.Serializable

/**
 * Navigation routes
 */
object NavRoutes {
    const val APPS = "Apps"
    const val WAKELOCKS = "Wakelocks"
    const val ALARMS = "Alarms"
    const val SERVICES = "Services"
    const val SETTINGS = "Settings"
    const val MODULE_CHECK = "ModuleCheck"

    const val DADETAIL = "DADetail"
    const val APPDETAIL = "AppDetail"
}

/**
 * 应用屏幕的类型化导航目标
 *
 * @param currentUserId 当前用户ID
 * @param searchQuery 搜索查询
 * @param filterOption 过滤选项
 * @param sortOption 排序选项
 */
@Serializable
data class Apps(
    val currentUserId: Int = 0,
    val searchQuery: String = "",
    val filterOption: FilterOption = FilterOption.ALL,
    val sortOption: SortOption = SortOption.NAME
)

/**
 * Wakelock屏幕的类型化导航目标
 *
 * @param packageName 包名过滤器
 * @param userId 用户ID过滤器
 * @param searchQuery 搜索查询
 * @param filterOption 过滤选项
 * @param sortOption 排序选项
 */
@Serializable
data class Wakelocks(
    val packageName: String? = null,
    val userId: Int? = null,
    val searchQuery: String = "",
    val filterOption: DAFilterOption = DAFilterOption.ALL,
    val sortOption: DASortOption = DASortOption.NAME
)

/**
 * Alarm屏幕的类型化导航目标
 *
 * @param packageName 包名过滤器
 * @param userId 用户ID过滤器
 * @param searchQuery 搜索查询
 * @param filterOption 过滤选项
 * @param sortOption 排序选项
 */
@Serializable
data class Alarms(
    val packageName: String? = null,
    val userId: Int? = null,
    val searchQuery: String = "",
    val filterOption: DAFilterOption = DAFilterOption.ALL,
    val sortOption: DASortOption = DASortOption.NAME
)

/**
 * Service屏幕的类型化导航目标
 *
 * @param packageName 包名过滤器
 * @param userId 用户ID过滤器
 * @param searchQuery 搜索查询
 * @param filterOption 过滤选项
 * @param sortOption 排序选项
 */
@Serializable
data class Services(
    val packageName: String? = null,
    val userId: Int? = null,
    val searchQuery: String = "",
    val filterOption: DAFilterOption = DAFilterOption.ALL,
    val sortOption: DASortOption = DASortOption.NAME
)

/**
 * Serializable navigation destination for the DA detail screen
 *
 * @param daName da name
 * @param packageName package name
 * @param userId user id
 * @param type da type
 */
@Serializable
data class DADetail(
    val daName: String,
    val packageName: String? = null,
    val userId: Int = 0,
    val type: String = Type.UnKnow.value
)

/**
 * Serializable navigation destination for the App detail screen
 *
 * @param packageName 应用包名
 * @param userId 用户ID
 */
@Serializable
data class AppDetail(
    val packageName: String,
    val userId: Int = 0,
    val label: String
)