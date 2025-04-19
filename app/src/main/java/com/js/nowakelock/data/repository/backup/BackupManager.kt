package com.js.nowakelock.data.repository.backup

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 管理备份和恢复操作的类
 */
class BackupManager(
    private val context: Context,
    private val backupRepo: BackupRepo
) {
    private val json = Json { 
        prettyPrint = true 
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    /**
     * 创建备份并写入URI指定的文件
     * @param uri 目标文件URI
     * @return 操作结果
     */
    suspend fun createBackup(uri: Uri): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            // 获取当前备份数据
            val backup = backupRepo.getBackup()
            
            // 将备份数据序列化为JSON
            val jsonString = json.encodeToString(backup)
            
            // 写入文件
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(jsonString.toByteArray())
                outputStream.flush()
            }
            
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating backup", e)
            Result.failure(e)
        }
    }
    
    /**
     * 从URI读取备份文件并恢复
     * @param uri 备份文件URI
     * @return 操作结果
     */
    suspend fun restoreBackup(uri: Uri): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            // 读取文件内容
            val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader().use { it.readText() }
            } ?: throw IOException("Unable to read backup file")
            
            // 从JSON反序列化
            val backup = json.decodeFromString<Backup>(jsonString)
            
            // 恢复到数据库
            backupRepo.restoreBackup(backup)
            
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring backup", e)
            Result.failure(e)
        }
    }
    
    /**
     * 生成当前日期格式化字符串用于文件名
     * @return 格式化的日期字符串
     */
    fun getFormattedDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd-HH-mm", Locale.getDefault())
        return sdf.format(Date())
    }
    
    companion object {
        private const val TAG = "BackupManager"
    }
} 