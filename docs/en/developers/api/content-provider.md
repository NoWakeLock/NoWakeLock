# ContentProvider API

NoWakeLock provides a ContentProvider interface for data exchange between the application and Xposed module, supporting event recording, rule management, and status queries.

## ContentProvider Overview

### Authority and URI Structure
```kotlin
const val AUTHORITY = "com.js.nowakelock.provider"
const val BASE_URI = "content://$AUTHORITY"

// Main URI endpoints
val EVENTS_URI = Uri.parse("$BASE_URI/events")           // Event recording
val RULES_URI = Uri.parse("$BASE_URI/rules")             // Rule management
val APPS_URI = Uri.parse("$BASE_URI/apps")               // Application info
val STATS_URI = Uri.parse("$BASE_URI/stats")             // Statistics data
val STATUS_URI = Uri.parse("$BASE_URI/status")           // System status
val CONFIG_URI = Uri.parse("$BASE_URI/config")           // Configuration management
```

### Permission Declaration
```xml
<!-- Read permission -->
<permission
    android:name="com.js.nowakelock.permission.READ_DATA"
    android:protectionLevel="signature" />

<!-- Write permission -->
<permission
    android:name="com.js.nowakelock.permission.WRITE_DATA"
    android:protectionLevel="signature" />

<!-- Provider declaration -->
<provider
    android:name=".provider.NoWakeLockProvider"
    android:authorities="com.js.nowakelock.provider"
    android:exported="false"
    android:readPermission="com.js.nowakelock.permission.READ_DATA"
    android:writePermission="com.js.nowakelock.permission.WRITE_DATA" />
```

## Event Recording API

### Insert Events
```kotlin
// Event types
object EventTypes {
    const val WAKELOCK = 1
    const val ALARM = 2
    const val SERVICE = 3
}

// Insert WakeLock event
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

// Update event end time
fun updateEventEndTime(
    context: Context,
    instanceId: String,
    endTime: Long = System.currentTimeMillis()
): Int {
    val values = ContentValues().apply {
        put("end_time", endTime)
        put("duration", endTime - startTime) // Need to calculate duration
    }
    
    val uri = Uri.withAppendedPath(EVENTS_URI, instanceId)
    return context.contentResolver.update(uri, values, null, null)
}
```

### Query Events
```kotlin
// Query application event records
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

// Query active events
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

// Event statistics query (current session)
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

## Rule Management API

### Rule CRUD Operations
```kotlin
// Rule types
object RuleTypes {
    const val WAKELOCK = 1
    const val ALARM = 2
    const val SERVICE = 3
}

// Action types
object ActionTypes {
    const val ALLOW = 0
    const val LIMIT = 1
    const val BLOCK = 2
}

// Match types
object MatchTypes {
    const val EXACT = 0
    const val CONTAINS = 1
    const val REGEX = 2
    const val WILDCARD = 3
}

// Insert rule
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

// Update rule
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

// Delete rule
fun deleteRule(context: Context, ruleId: String): Int {
    val uri = Uri.withAppendedPath(RULES_URI, ruleId)
    return context.contentResolver.delete(uri, null, null)
}

// Batch delete app rules
fun deleteAppRules(context: Context, packageName: String): Int {
    return context.contentResolver.delete(
        RULES_URI,
        "package_name = ?",
        arrayOf(packageName)
    )
}
```

### Rule Queries
```kotlin
// Query app rules
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

// Query global rules
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

// Rule matching query
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

## Application Info API

### App Data Management
```kotlin
// Insert or update app info
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
    
    // Use REPLACE strategy
    val uri = Uri.parse("$BASE_URI/apps/replace")
    return context.contentResolver.insert(uri, values)
}

// Query app info
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

// Query user's all apps
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

// Search apps
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

## Statistics Data API

### Current Session Statistics Query
```kotlin
// App current session statistics
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

// Current overall statistics
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

// Active app ranking (current session)
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

!!! warning "Data Lifecycle Important Notice"
    NoWakeLock's data storage adopts session-level design:
    
    - **Data cleared after reboot**: BootResetManager clears all statistics after detecting device reboot
    - **XProvider initialization cleanup**: InfoDatabase calls clearAllTables() on creation
    - **No cross-reboot historical data**: Statistics API only returns current session data
    
    This design ensures real-time nature of statistics and system performance.

## System Status API

### Status Query Interface
```kotlin
// Module status query
fun queryModuleStatus(context: Context): Cursor? {
    val uri = Uri.parse("$BASE_URI/status/module")
    return context.contentResolver.query(uri, null, null, null, null)
}

// Hook status query
fun queryHookStatus(context: Context): Cursor? {
    val uri = Uri.parse("$BASE_URI/status/hooks")
    return context.contentResolver.query(uri, null, null, null, null)
}

// Performance metrics query
fun queryPerformanceMetrics(context: Context): Cursor? {
    val uri = Uri.parse("$BASE_URI/status/performance")
    return context.contentResolver.query(uri, null, null, null, null)
}

// Rule statistics query
fun queryRuleStatistics(context: Context): Cursor? {
    val uri = Uri.parse("$BASE_URI/status/rules")
    return context.contentResolver.query(uri, null, null, null, null)
}
```

## Configuration Management API

### Configuration Sync Interface
```kotlin
// Sync configuration to Xposed module
fun syncConfiguration(context: Context): Boolean {
    val uri = Uri.parse("$BASE_URI/config/sync")
    val values = ContentValues().apply {
        put("sync_time", System.currentTimeMillis())
        put("sync_type", "full")
    }
    
    val result = context.contentResolver.insert(uri, values)
    return result != null
}

// Incremental sync rule
fun syncRule(context: Context, ruleId: String): Boolean {
    val uri = Uri.parse("$BASE_URI/config/sync_rule")
    val values = ContentValues().apply {
        put("rule_id", ruleId)
        put("sync_time", System.currentTimeMillis())
    }
    
    val result = context.contentResolver.insert(uri, values)
    return result != null
}

// Query sync status
fun querySyncStatus(context: Context): Cursor? {
    val uri = Uri.parse("$BASE_URI/config/sync_status")
    return context.contentResolver.query(uri, null, null, null, null)
}

// Reset configuration
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

## Batch Operations API

### Bulk Data Processing
```kotlin
// Bulk insert events
fun bulkInsertEvents(context: Context, events: List<ContentValues>): Int {
    val uri = Uri.parse("$BASE_URI/events/bulk")
    return context.contentResolver.bulkInsert(uri, events.toTypedArray())
}

// Bulk update rules
fun bulkUpdateRules(context: Context, updates: List<Pair<String, ContentValues>>): Int {
    val uri = Uri.parse("$BASE_URI/rules/bulk_update")
    
    // Use ContentProviderOperation for bulk operations
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

// Bulk delete expired events
fun cleanupOldEvents(context: Context, cutoffTime: Long): Int {
    val uri = Uri.parse("$BASE_URI/events/cleanup")
    return context.contentResolver.delete(
        uri,
        "start_time < ?",
        arrayOf(cutoffTime.toString())
    )
}
```

## Observer Pattern

### Data Change Listening
```kotlin
// Register event change listener
class EventObserver(private val context: Context) : ContentObserver(Handler(Looper.getMainLooper())) {
    
    fun startObserving() {
        context.contentResolver.registerContentObserver(
            EVENTS_URI,
            true, // Monitor child URIs
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
                // Handle event changes
                onEventChanged(uri)
            }
            uri.toString().startsWith(RULES_URI.toString()) -> {
                // Handle rule changes
                onRuleChanged(uri)
            }
            uri.toString().startsWith(APPS_URI.toString()) -> {
                // Handle app changes
                onAppChanged(uri)
            }
        }
    }
    
    private fun onEventChanged(uri: Uri) {
        // Implement event change handling logic
    }
    
    private fun onRuleChanged(uri: Uri) {
        // Implement rule change handling logic
        // Can trigger configuration sync
        syncRule(context, uri.lastPathSegment ?: "")
    }
    
    private fun onAppChanged(uri: Uri) {
        // Implement app change handling logic
    }
}
```

## Error Handling

### Exception Handling
```kotlin
object ContentProviderErrors {
    const val SUCCESS = 0
    const val PERMISSION_DENIED = 1
    const val INVALID_ARGUMENTS = 2
    const val DATABASE_ERROR = 3
    const val SYNC_FAILED = 4
    const val UNKNOWN_ERROR = 5
}

// Wrap query operations
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

// Wrap insert operations
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

!!! info "API Usage Instructions"
    ContentProvider API provides complete data access interface, supporting event recording, rule management, application info query and current session statistics.

!!! warning "Permission Requirements"
    Using ContentProvider API requires appropriate permission declarations to ensure the caller has read/write permissions.

!!! tip "Performance and Data Management Recommendations"
    - For large data operations, recommend using batch APIs and async processing to avoid blocking main thread
    - Use ContentObserver to monitor data changes for real-time updates
    - Note that data is only valid for current session, all statistics will be cleared after device reboot