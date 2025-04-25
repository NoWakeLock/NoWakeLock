package com.js.nowakelock.ui.navigation.params

/**
 * DAsScreen参数常量定义
 * 用于统一管理WakelockScreen、AlarmScreen、ServiceScreen相关的参数名称
 * 确保参数访问的类型安全
 */
object DAsScreenParams {
    /**
     * 包名参数
     * 用于筛选特定应用的DA数据
     */
    const val PACKAGE_NAME = "packageName"
    
    /**
     * 用户ID参数
     * 用于筛选特定用户的DA数据
     */
    const val USER_ID = "userId"
    
    /**
     * 搜索查询参数
     * 用于存储当前的搜索关键词
     */
    const val SEARCH_QUERY = "searchQuery"
    
    /**
     * 搜索激活状态参数
     * 标识搜索功能是否处于激活状态
     */
    const val IS_SEARCH_ACTIVE = "isSearchActive"
    
    /**
     * 过滤选项参数
     * 存储当前选择的DA过滤选项
     */
    const val FILTER_OPTION = "filterOption"
    
    /**
     * 排序选项参数
     * 存储当前选择的DA排序选项
     */
    const val SORT_OPTION = "sortOption"
} 