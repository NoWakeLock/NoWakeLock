package com.js.nowakelock.ui.navigation.params

/**
 * AppsScreen参数常量定义
 * 用于统一管理AppsScreen相关的参数名称，确保参数访问的类型安全
 */
object AppsScreenParams {
    /**
     * 当前用户ID参数
     * 用于筛选特定用户的应用数据
     */
    const val CURRENT_USER_ID = "currentUserId"
    
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
     * 存储当前选择的应用过滤选项
     */
    const val FILTER_OPTION = "filterOption"
    
    /**
     * 排序选项参数
     * 存储当前选择的应用排序选项
     */
    const val SORT_OPTION = "sortOption"
} 