# ContentProvider API

NoWakeLock 提供了 ContentProvider 介面用於應用程式與 Xposed 模組之間的資料交換，支援事件記錄、規則管理和狀態查詢。

## ContentProvider 概覽

### 授權和 URI 結構
```kotlin
const val AUTHORITY = "com.js.nowakelock.provider"
const val BASE_URI = "content://$AUTHORITY"

// 主要 URI 端點
val EVENTS_URI = Uri.parse("$BASE_URI/events")           // 事件記錄
val RULES_URI = Uri.parse("$BASE_URI/rules")             // 規則管理
val APPS_URI = Uri.parse("$BASE_URI/apps")               // 應用程式資訊
val STATS_URI = Uri.parse("$BASE_URI/stats")             // 統計資料
val STATUS_URI = Uri.parse("$BASE_URI/status")           // 系統狀態
val CONFIG_URI = Uri.parse("$BASE_URI/config")           // 設定管理
```

### 權限宣告
```xml
<!-- 讀取權限 -->
<permission
    android:name="com.js.nowakelock.permission.READ_DATA"
    android:protectionLevel="signature" />

<!-- 寫入權限 -->
<permission
    android:name="com.js.nowakelock.permission.WRITE_DATA"
    android:protectionLevel="signature" />

<!-- Provider 宣告 -->
<provider
    android:name=".provider.NoWakeLockProvider"
    android:authorities="com.js.nowakelock.provider"
    android:exported="false"
    android:readPermission="com.js.nowakelock.permission.READ_DATA"
    android:writePermission="com.js.nowakelock.permission.WRITE_DATA" />
```

## 事件記錄 API

### 插入事件
```kotlin
// 事件類型
object EventTypes {
    const val WAKELOCK = 1
    const val ALARM = 2
    const val SERVICE = 3
}

// 插入 WakeLock 事件
fun insertWakelockEvent(
    context: Context,
    packageName: String,
    tag: String,
    flags: Int,
    uid: Int,
    isBlocked: Boolean,
    instanceId: String = UUID.randomUUID().toString()
): Uri? {
    val values = ContentValues().apply {
        put("type", EventTypes.WAKELOCK)
        put("package_name", packageName)
        put("name", tag)
        put("flags", flags)
        put("uid", uid)
        put("user_id", UserHandle.getUserId(uid))
        put("start_time", System.currentTimeMillis())
        put("is_blocked", if (isBlocked) 1 else 0)
        put("instance_id", instanceId)
    }
    
    return context.contentResolver.insert(EVENTS_URI, values)
}

// 更新事件結束時間
fun updateEventEndTime(
    context: Context,
    instanceId: String,
    endTime: Long = System.currentTimeMillis()
): Int {
    val values = ContentValues().apply {
        put("end_time", endTime)
        put("duration", endTime - startTime) // 需要計算持續時間
    }
    
    val uri = Uri.withAppendedPath(EVENTS_URI, instanceId)
    return context.contentResolver.update(uri, values, null, null)
}
```

### 查詢事件
```kotlin
// 查詢應用程式的事件記錄
fun queryAppEvents(
    context: Context,
    packageName: String,
    eventType: Int? = null,
    startTime: Long? = null,
    endTime: Long? = null,
    limit: Int = 1000
): Cursor? {
    val selection = buildString {
        append("package_name = ?")
        if (eventType != null) append(" AND type = ?")
        if (startTime != null) append(" AND start_time >= ?")
        if (endTime != null) append(" AND start_time <= ?")
    }
    
    val selectionArgs = buildList {
        add(packageName)
        if (eventType != null) add(eventType.toString())
        if (startTime != null) add(startTime.toString())
        if (endTime != null) add(endTime.toString())
    }.toTypedArray()
    
    val sortOrder = "start_time DESC LIMIT $limit"
    
    return context.contentResolver.query(
        EVENTS_URI,
        null,
        selection,
        selectionArgs,
        sortOrder
    )
}

// 查詢活躍事件
fun queryActiveEvents(context: Context, eventType: Int? = null): Cursor? {
    val selection = if (eventType != null) {
        "end_time IS NULL AND type = ?"
    } else {
        "end_time IS NULL"
    }
    
    val selectionArgs = eventType?.toString()?.let { arrayOf(it) }
    
    return context.contentResolver.query(
        EVENTS_URI,
        arrayOf("instance_id", "package_name", "name", "start_time", "type"),
        selection,
        selectionArgs,
        "start_time DESC"
    )
}

// 事件統計查詢（目前會話）
fun queryCurrentSessionStatistics(
    context: Context,
    packageName: String? = null
): Cursor? {
    val uri = Uri.parse("$BASE_URI/events/current_session")
    val selection = if (packageName != null) {
        "package_name = ?"
    } else null
    
    val selectionArgs = packageName?.let { arrayOf(it) }
    
    return context.contentResolver.query(
        uri,
        null,
        selection,
        selectionArgs,
        "start_time DESC"
    )
}
```

## 規則管理 API

### 規則 CRUD 操作
```kotlin
// 規則類型
object RuleTypes {
    const val WAKELOCK = 1
    const val ALARM = 2
    const val SERVICE = 3
}

// 動作類型
object ActionTypes {
    const val ALLOW = 0
    const val LIMIT = 1
    const val BLOCK = 2
}

// 比對類型
object MatchTypes {
    const val EXACT = 0
    const val CONTAINS = 1
    const val REGEX = 2
    const val WILDCARD = 3
}

// 插入規則
fun insertRule(
    context: Context,
    packageName: String,
    target: String,
    ruleType: Int,
    action: Int,
    matchType: Int = MatchTypes.EXACT,
    timeout: Long = 60000,
    priority: Int = 0
): Uri? {
    val ruleId = UUID.randomUUID().toString()
    val currentTime = System.currentTimeMillis()
    
    val values = ContentValues().apply {
        put("id", ruleId)
        put("package_name", packageName)
        put("target", target)
        put("type", ruleType)
        put("action", action)
        put("match_type", matchType)
        put("timeout", timeout)
        put("priority", priority)
        put("enabled", 1)
        put("created_time", currentTime)
        put("last_modified_time", currentTime)
    }
    
    return context.contentResolver.insert(RULES_URI, values)
}

// 更新規則
fun updateRule(
    context: Context,
    ruleId: String,
    action: Int? = null,
    timeout: Long? = null,
    enabled: Boolean? = null,
    priority: Int? = null
): Int {
    val values = ContentValues().apply {
        action?.let { put("action", it) }
        timeout?.let { put("timeout", it) }
        enabled?.let { put("enabled", if (it) 1 else 0) }
        priority?.let { put("priority", it) }
        put("last_modified_time", System.currentTimeMillis())
    }
    
    val uri = Uri.withAppendedPath(RULES_URI, ruleId)
    return context.contentResolver.update(uri, values, null, null)
}

// 刪除規則
fun deleteRule(context: Context, ruleId: String): Int {
    val uri = Uri.withAppendedPath(RULES_URI, ruleId)
    return context.contentResolver.delete(uri, null, null)
}

// 批次刪除應用程式規則
fun deleteAppRules(context: Context, packageName: String): Int {
    return context.contentResolver.delete(
        RULES_URI,
        "package_name = ?",
        arrayOf(packageName)
    )
}
```

### 規則查詢
```kotlin
// 查詢應用程式規則
fun queryAppRules(
    context: Context,
    packageName: String,
    ruleType: Int? = null,
    enabled: Boolean? = null
): Cursor? {
    val selection = buildString {
        append("package_name = ?")
        if (ruleType != null) append(" AND type = ?")
        if (enabled != null) append(" AND enabled = ?")
    }
    
    val selectionArgs = buildList {
        add(packageName)
        if (ruleType != null) add(ruleType.toString())
        if (enabled != null) add(if (enabled) "1" else "0")
    }.toTypedArray()
    
    return context.contentResolver.query(
        RULES_URI,
        null,
        selection,
        selectionArgs,
        "priority DESC, last_modified_time DESC"
    )
}

// 查詢全域規則
fun queryGlobalRules(
    context: Context,
    ruleType: Int? = null
): Cursor? {
    val selection = buildString {
        append("package_name = '*'")
        if (ruleType != null) append(" AND type = ?")
        append(" AND enabled = 1")
    }
    
    val selectionArgs = ruleType?.toString()?.let { arrayOf(it) }
    
    return context.contentResolver.query(
        RULES_URI,
        null,
        selection,
        selectionArgs,
        "priority DESC"
    )
}

// 規則比對查詢
fun queryMatchingRules(
    context: Context,
    packageName: String,
    target: String,
    ruleType: Int
): Cursor? {
    val uri = Uri.parse("$BASE_URI/rules/match")
    val selection = "package_name IN (?, '*') AND type = ? AND enabled = 1"
    val selectionArgs = arrayOf(packageName, ruleType.toString())
    
    return context.contentResolver.query(
        uri,
        null,
        selection,
        selectionArgs,
        "priority DESC"
    )
}
```

## 應用程式資訊 API

### 應用程式資料管理
```kotlin
// 插入或更新應用程式資訊
fun insertOrUpdateApp(
    context: Context,
    packageName: String,
    uid: Int,
    label: String,
    isSystem: Boolean,
    enabled: Boolean,
    versionCode: Long,
    versionName: String
): Uri? {
    val userId = UserHandle.getUserId(uid)
    
    val values = ContentValues().apply {
        put("package_name", packageName)
        put("uid", uid)
        put("user_id", userId)
        put("label", label)
        put("system", if (isSystem) 1 else 0)
        put("enabled", if (enabled) 1 else 0)
        put("version_code", versionCode)
        put("version_name", versionName)
        put("last_update_time", System.currentTimeMillis())
    }
    
    // 使用 REPLACE 策略
    val uri = Uri.parse("$BASE_URI/apps/replace")
    return context.contentResolver.insert(uri, values)
}

// 查詢應用程式資訊
fun queryAppInfo(
    context: Context,
    packageName: String,
    userId: Int = 0
): Cursor? {
    return context.contentResolver.query(
        APPS_URI,
        null,
        "package_name = ? AND user_id = ?",
        arrayOf(packageName, userId.toString()),
        null
    )
}

// 查詢使用者的所有應用程式
fun queryUserApps(
    context: Context,
    userId: Int,
    systemApps: Boolean? = null,
    enabledOnly: Boolean = false
): Cursor? {
    val selection = buildString {
        append("user_id = ?")
        if (systemApps != null) append(" AND system = ?")
        if (enabledOnly) append(" AND enabled = 1")
    }
    
    val selectionArgs = buildList {
        add(userId.toString())
        if (systemApps != null) add(if (systemApps) "1" else "0")
    }.toTypedArray()
    
    return context.contentResolver.query(
        APPS_URI,
        null,
        selection,
        selectionArgs,
        "label ASC"
    )
}

// 搜尋應用程式
fun searchApps(
    context: Context,
    query: String,
    userId: Int
): Cursor? {
    val uri = Uri.parse("$BASE_URI/apps/search")
    return context.contentResolver.query(
        uri,
        null,
        "user_id = ? AND (label LIKE ? OR package_name LIKE ?)",
        arrayOf(userId.toString(), "%$query%", "%$query%"),
        "label ASC"
    )
}
```

## 統計資料 API

### 目前會話統計查詢
```kotlin
// 應用程式目前會話統計
fun queryCurrentAppStatistics(
    context: Context,
    packageName: String
): Cursor? {
    val uri = Uri.parse("$BASE_URI/stats/current/$packageName")
    return context.contentResolver.query(
        uri,
        null,
        null,
        null,
        null
    )
}

// 目前總體統計
fun queryCurrentOverallStatistics(
    context: Context,
    userId: Int = 0
): Cursor? {
    val uri = Uri.parse("$BASE_URI/stats/current_overall")
    return context.contentResolver.query(
        uri,
        null,
        "user_id = ?",
        arrayOf(userId.toString()),
        null
    )
}

// 活躍應用程式排行（目前會話）
fun queryActiveApps(
    context: Context,
    metric: String = "event_count", // event_count, active_wakelocks, blocked_count
    limit: Int = 10
): Cursor? {
    val uri = Uri.parse("$BASE_URI/stats/active_apps")
    return context.contentResolver.query(
        uri,
        null,
        "metric = ? LIMIT ?",
        arrayOf(metric, limit.toString()),
        "value DESC"
    )
}

```

!!! warning "資料生命週期重要提示"
    NoWakeLock 的資料儲存採用會話級別設計：
    
    - **重啟後資料清空**：BootResetManager 檢測裝置重啟後會清理所有統計資料
    - **XProvider 初始化清理**：InfoDatabase 在建立時呼叫 clearAllTables()
    - **不存在跨重啟的歷史資料**：統計 API 只回傳目前會話資料
    
    這種設計確保了統計資料的即時性和系統效能。

## 系統狀態 API

### 狀態查詢介面
```kotlin
// 模組狀態查詢
fun queryModuleStatus(context: Context): Cursor? {
    val uri = Uri.parse("$BASE_URI/status/module")
    return context.contentResolver.query(uri, null, null, null, null)
}

// Hook 狀態查詢
fun queryHookStatus(context: Context): Cursor? {
    val uri = Uri.parse("$BASE_URI/status/hooks")
    return context.contentResolver.query(uri, null, null, null, null)
}

// 效能指標查詢
fun queryPerformanceMetrics(context: Context): Cursor? {
    val uri = Uri.parse("$BASE_URI/status/performance")
    return context.contentResolver.query(uri, null, null, null, null)
}

// 規則統計查詢
fun queryRuleStatistics(context: Context): Cursor? {
    val uri = Uri.parse("$BASE_URI/status/rules")
    return context.contentResolver.query(uri, null, null, null, null)
}
```

## 設定管理 API

### 設定同步介面
```kotlin
// 同步設定到 Xposed 模組
fun syncConfiguration(context: Context): Boolean {
    val uri = Uri.parse("$BASE_URI/config/sync")
    val values = ContentValues().apply {
        put("sync_time", System.currentTimeMillis())
        put("sync_type", "full")
    }
    
    val result = context.contentResolver.insert(uri, values)
    return result != null
}

// 增量同步規則
fun syncRule(context: Context, ruleId: String): Boolean {
    val uri = Uri.parse("$BASE_URI/config/sync_rule")
    val values = ContentValues().apply {
        put("rule_id", ruleId)
        put("sync_time", System.currentTimeMillis())
    }
    
    val result = context.contentResolver.insert(uri, values)
    return result != null
}

// 查詢同步狀態
fun querySyncStatus(context: Context): Cursor? {
    val uri = Uri.parse("$BASE_URI/config/sync_status")
    return context.contentResolver.query(uri, null, null, null, null)
}

// 重設設定
fun resetConfiguration(context: Context, resetType: String = "all"): Boolean {
    val uri = Uri.parse("$BASE_URI/config/reset")
    val values = ContentValues().apply {
        put("reset_type", resetType)
        put("reset_time", System.currentTimeMillis())
    }
    
    val result = context.contentResolver.insert(uri, values)
    return result != null
}
```

## 批次操作 API

### 批次資料處理
```kotlin
// 批次插入事件
fun bulkInsertEvents(context: Context, events: List<ContentValues>): Int {
    val uri = Uri.parse("$BASE_URI/events/bulk")
    return context.contentResolver.bulkInsert(uri, events.toTypedArray())
}

// 批次更新規則
fun bulkUpdateRules(context: Context, updates: List<Pair<String, ContentValues>>): Int {
    val uri = Uri.parse("$BASE_URI/rules/bulk_update")
    
    // 使用 ContentProviderOperation 進行批次操作
    val operations = updates.map { (ruleId, values) ->
        ContentProviderOperation.newUpdate(Uri.withAppendedPath(RULES_URI, ruleId))
            .withValues(values)
            .build()
    }
    
    return try {
        val results = context.contentResolver.applyBatch(AUTHORITY, ArrayList(operations))
        results.count { it.count > 0 }
    } catch (e: Exception) {
        0
    }
}

// 批次刪除過期事件
fun cleanupOldEvents(context: Context, cutoffTime: Long): Int {
    val uri = Uri.parse("$BASE_URI/events/cleanup")
    return context.contentResolver.delete(
        uri,
        "start_time < ?",
        arrayOf(cutoffTime.toString())
    )
}
```

## 觀察者模式

### 資料變更監聽
```kotlin
// 註冊事件變更監聽器
class EventObserver(private val context: Context) : ContentObserver(Handler(Looper.getMainLooper())) {
    
    fun startObserving() {
        context.contentResolver.registerContentObserver(
            EVENTS_URI,
            true, // 監聽子 URI
            this
        )
    }
    
    fun stopObserving() {
        context.contentResolver.unregisterContentObserver(this)
    }
    
    override fun onChange(selfChange: Boolean, uri: Uri?) {
        super.onChange(selfChange, uri)
        uri?.let { handleUriChange(it) }
    }
    
    private fun handleUriChange(uri: Uri) {
        when {
            uri.toString().startsWith(EVENTS_URI.toString()) -> {
                // 處理事件變更
                onEventChanged(uri)
            }
            uri.toString().startsWith(RULES_URI.toString()) -> {
                // 處理規則變更
                onRuleChanged(uri)
            }
            uri.toString().startsWith(APPS_URI.toString()) -> {
                // 處理應用程式變更
                onAppChanged(uri)
            }
        }
    }
    
    private fun onEventChanged(uri: Uri) {
        // 實作事件變更處理邏輯
    }
    
    private fun onRuleChanged(uri: Uri) {
        // 實作規則變更處理邏輯
        // 可以觸發設定同步
        syncRule(context, uri.lastPathSegment ?: "")
    }
    
    private fun onAppChanged(uri: Uri) {
        // 實作應用程式變更處理邏輯
    }
}
```

## 錯誤處理

### 異常情況處理
```kotlin
object ContentProviderErrors {
    const val SUCCESS = 0
    const val PERMISSION_DENIED = 1
    const val INVALID_ARGUMENTS = 2
    const val DATABASE_ERROR = 3
    const val SYNC_FAILED = 4
    const val UNKNOWN_ERROR = 5
}

// 包裝查詢操作
fun safeQuery(
    context: Context,
    uri: Uri,
    projection: Array<String>? = null,
    selection: String? = null,
    selectionArgs: Array<String>? = null,
    sortOrder: String? = null
): Result<Cursor?> {
    return try {
        val cursor = context.contentResolver.query(
            uri, projection, selection, selectionArgs, sortOrder
        )
        Result.success(cursor)
    } catch (e: SecurityException) {
        Result.failure(SecurityException("Permission denied: ${e.message}"))
    } catch (e: IllegalArgumentException) {
        Result.failure(IllegalArgumentException("Invalid arguments: ${e.message}"))
    } catch (e: Exception) {
        Result.failure(Exception("Query failed: ${e.message}"))
    }
}

// 包裝插入操作
fun safeInsert(
    context: Context,
    uri: Uri,
    values: ContentValues
): Result<Uri?> {
    return try {
        val result = context.contentResolver.insert(uri, values)
        Result.success(result)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

!!! info "API 使用說明"
    ContentProvider API 提供了完整的資料存取介面，支援事件記錄、規則管理、應用程式資訊查詢和目前會話統計等功能。

!!! warning "權限要求"
    使用 ContentProvider API 需要相應的權限宣告，確保呼叫方具有讀寫權限。

!!! tip "效能和資料管理建議"
    - 對於大量資料操作，建議使用批次 API 和非同步處理，避免阻塞主執行緒
    - 使用 ContentObserver 監聽資料變更以實現即時更新
    - 注意資料僅在目前會話有效，裝置重啟後會清空所有統計資料