package com.js.nowakelock.data.model

/**
 * 应用统计信息类，聚合应用的唤醒锁、闹钟和服务数据
 * 用于应用详情页显示
 */
data class AppStatistics(
    val wakelockCount: Int = 0,
    val wakelockBlockedCount: Int = 0,
    val alarmCount: Int = 0,
    val alarmBlockedCount: Int = 0,
    val serviceCount: Int = 0,
    val serviceBlockedCount: Int = 0,
    val totalTime: Long = 0,
    val savedTime: Long = 0
) {
    /**
     * 返回格式化的总时间字符串
     */
    fun getFormattedTotalTime(): String {
        if (totalTime <= 0) return "0s"
        
        val seconds = totalTime / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        
        return when {
            hours > 0 -> "${hours}h ${minutes % 60}m"
            minutes > 0 -> "${minutes}m ${seconds % 60}s" 
            else -> "${seconds}s"
        }
    }

    /**
     * 返回格式化的节省时间字符串
     */
    fun getFormattedSavedTime(): String {
        if (savedTime <= 0) return "0s"
        
        val seconds = savedTime / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        
        return when {
            hours > 0 -> "${hours}h ${minutes % 60}m"
            minutes > 0 -> "${minutes}m ${seconds % 60}s" 
            else -> "${seconds}s"
        }
    }
} 