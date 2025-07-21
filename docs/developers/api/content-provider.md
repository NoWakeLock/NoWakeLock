# ContentProvider API

NoWakeLock 提供了 ContentProvider 接口用于应用与 Xposed 模块之间的数据交换，支持事件记录、规则管理和状态查询。

## ContentProvider 概览

### 授权和 URI 结构
```kotlin
const val AUTHORITY = "com.js.nowakelock.provider"
const val BASE_URI = "content://$AUTHORITY"

// 主要 URI 端点
val EVENTS_URI = Uri.parse("$BASE_URI/events")           // 事件记录
val RULES_URI = Uri.parse("$BASE_URI/rules")             // 规则管理
val APPS_URI = Uri.parse("$BASE_URI/apps")               // 应用信息
val STATS_URI = Uri.parse("$BASE_URI/stats")             // 统计数据
val STATUS_URI = Uri.parse("$BASE_URI/status")           // 系统状态
val CONFIG_URI = Uri.parse("$BASE_URI/config")           // 配置管理
```

### 权限声明
```xml
<!-- 读取权限 -->
<permission
    android:name="com.js.nowakelock.permission.READ_DATA"
    android:protectionLevel="signature" />

<!-- 写入权限 -->
<permission
    android:name="com.js.nowakelock.permission.WRITE_DATA"
    android:protectionLevel="signature" />

<!-- Provider 声明 -->
<provider
    android:name=".provider.NoWakeLockProvider"
    android:authorities="com.js.nowakelock.provider"
    android:exported="false"
    android:readPermission="com.js.nowakelock.permission.READ_DATA"
    android:writePermission="com.js.nowakelock.permission.WRITE_DATA" />
```

## 事件记录 API

### 插入事件
```kotlin
// 事件类型
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

// 更新事件结束时间
fun updateEventEndTime(
    context: Context,
    instanceId: String,
    endTime: Long = System.currentTimeMillis()
): Int {
    val values = ContentValues().apply {
        put("end_time", endTime)
        put("duration", endTime - startTime) // 需要计算持续时间
    }
    
    val uri = Uri.withAppendedPath(EVENTS_URI, instanceId)
    return context.contentResolver.update(uri, values, null, null)
}
```

### 查询事件
```kotlin
// 查询应用的事件记录
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

// 查询活跃事件
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

// 事件统计查询（当前会话）
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

## 规则管理 API

### 规则 CRUD 操作
```kotlin
// 规则类型
object RuleTypes {
    const val WAKELOCK = 1
    const val ALARM = 2
    const val SERVICE = 3
}

// 动作类型
object ActionTypes {
    const val ALLOW = 0
    const val LIMIT = 1
    const val BLOCK = 2
}

// 匹配类型
object MatchTypes {
    const val EXACT = 0
    const val CONTAINS = 1
    const val REGEX = 2
    const val WILDCARD = 3
}

// 插入规则
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

// 更新规则
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

// 删除规则
fun deleteRule(context: Context, ruleId: String): Int {
    val uri = Uri.withAppendedPath(RULES_URI, ruleId)
    return context.contentResolver.delete(uri, null, null)
}

// 批量删除应用规则
fun deleteAppRules(context: Context, packageName: String): Int {
    return context.contentResolver.delete(
        RULES_URI,
        "package_name = ?",
        arrayOf(packageName)
    )
}
```

### 规则查询
```kotlin
// 查询应用规则
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

// 查询全局规则
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

// 规则匹配查询
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

## 应用信息 API

### 应用数据管理
```kotlin
// 插入或更新应用信息
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

// 查询应用信息
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

// 查询用户的所有应用
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

// 搜索应用
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

## 统计数据 API

### 当前会话统计查询
```kotlin
// 应用当前会话统计
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

// 当前总体统计
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

// 活跃应用排行（当前会话）
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

!!! warning "数据生命周期重要提示"
    NoWakeLock 的数据存储采用会话级别设计：
    
    - **重启后数据清空**：BootResetManager 检测设备重启后会清理所有统计数据
    - **XProvider 初始化清理**：InfoDatabase 在创建时调用 clearAllTables()
    - **不存在跨重启的历史数据**：统计 API 只返回当前会话数据
    
    这种设计确保了统计数据的实时性和系统性能。

## 系统状态 API

### 状态查询接口
```kotlin
// 模块状态查询
fun queryModuleStatus(context: Context): Cursor? {
    val uri = Uri.parse("$BASE_URI/status/module")
    return context.contentResolver.query(uri, null, null, null, null)
}

// Hook 状态查询
fun queryHookStatus(context: Context): Cursor? {
    val uri = Uri.parse("$BASE_URI/status/hooks")
    return context.contentResolver.query(uri, null, null, null, null)
}

// 性能指标查询
fun queryPerformanceMetrics(context: Context): Cursor? {
    val uri = Uri.parse("$BASE_URI/status/performance")
    return context.contentResolver.query(uri, null, null, null, null)
}

// 规则统计查询
fun queryRuleStatistics(context: Context): Cursor? {
    val uri = Uri.parse("$BASE_URI/status/rules")
    return context.contentResolver.query(uri, null, null, null, null)
}
```

## 配置管理 API

### 配置同步接口
```kotlin
// 同步配置到 Xposed 模块
fun syncConfiguration(context: Context): Boolean {
    val uri = Uri.parse("$BASE_URI/config/sync")
    val values = ContentValues().apply {
        put("sync_time", System.currentTimeMillis())
        put("sync_type", "full")
    }
    
    val result = context.contentResolver.insert(uri, values)
    return result != null
}

// 增量同步规则
fun syncRule(context: Context, ruleId: String): Boolean {
    val uri = Uri.parse("$BASE_URI/config/sync_rule")
    val values = ContentValues().apply {
        put("rule_id", ruleId)
        put("sync_time", System.currentTimeMillis())
    }
    
    val result = context.contentResolver.insert(uri, values)
    return result != null
}

// 查询同步状态
fun querySyncStatus(context: Context): Cursor? {
    val uri = Uri.parse("$BASE_URI/config/sync_status")
    return context.contentResolver.query(uri, null, null, null, null)
}

// 重置配置
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

## 批量操作 API

### 批量数据处理
```kotlin
// 批量插入事件
fun bulkInsertEvents(context: Context, events: List<ContentValues>): Int {
    val uri = Uri.parse("$BASE_URI/events/bulk")
    return context.contentResolver.bulkInsert(uri, events.toTypedArray())
}

// 批量更新规则
fun bulkUpdateRules(context: Context, updates: List<Pair<String, ContentValues>>): Int {
    val uri = Uri.parse("$BASE_URI/rules/bulk_update")
    
    // 使用 ContentProviderOperation 进行批量操作
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

// 批量删除过期事件
fun cleanupOldEvents(context: Context, cutoffTime: Long): Int {
    val uri = Uri.parse("$BASE_URI/events/cleanup")
    return context.contentResolver.delete(
        uri,
        "start_time < ?",
        arrayOf(cutoffTime.toString())
    )
}
```

## 观察者模式

### 数据变更监听
```kotlin
// 注册事件变更监听器
class EventObserver(private val context: Context) : ContentObserver(Handler(Looper.getMainLooper())) {
    
    fun startObserving() {
        context.contentResolver.registerContentObserver(
            EVENTS_URI,
            true, // 监听子 URI
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
                // 处理事件变更
                onEventChanged(uri)
            }
            uri.toString().startsWith(RULES_URI.toString()) -> {
                // 处理规则变更
                onRuleChanged(uri)
            }
            uri.toString().startsWith(APPS_URI.toString()) -> {
                // 处理应用变更
                onAppChanged(uri)
            }
        }
    }
    
    private fun onEventChanged(uri: Uri) {
        // 实现事件变更处理逻辑
    }
    
    private fun onRuleChanged(uri: Uri) {
        // 实现规则变更处理逻辑
        // 可以触发配置同步
        syncRule(context, uri.lastPathSegment ?: "")
    }
    
    private fun onAppChanged(uri: Uri) {
        // 实现应用变更处理逻辑
    }
}
```

## 错误处理

### 异常情况处理
```kotlin
object ContentProviderErrors {
    const val SUCCESS = 0
    const val PERMISSION_DENIED = 1
    const val INVALID_ARGUMENTS = 2
    const val DATABASE_ERROR = 3
    const val SYNC_FAILED = 4
    const val UNKNOWN_ERROR = 5
}

// 包装查询操作
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

// 包装插入操作
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

!!! info "API 使用说明"
    ContentProvider API 提供了完整的数据访问接口，支持事件记录、规则管理、应用信息查询和当前会话统计等功能。

!!! warning "权限要求"
    使用 ContentProvider API 需要相应的权限声明，确保调用方具有读写权限。

!!! tip "性能和数据管理建议"
    - 对于大量数据操作，建议使用批量 API 和异步处理，避免阻塞主线程
    - 使用 ContentObserver 监听数据变更以实现实时更新
    - 注意数据仅在当前会话有效，设备重启后会清空所有统计数据