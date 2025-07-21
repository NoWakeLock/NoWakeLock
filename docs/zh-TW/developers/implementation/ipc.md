# 程序間通訊 (IPC)

NoWakeLock 的 IPC 系統負責 Xposed 模組與應用程式程序之間的資料交換，確保設定同步、事件傳輸和狀態查詢的可靠性。

## IPC 架構概覽

### 通訊模型
```mermaid
graph TB
    A[NoWakeLock 應用程式程序] --> B[XProvider]
    B --> C[SystemProperties]
    B --> D[ContentProvider]
    B --> E[共享檔案]
    
    F[Xposed 模組] --> G[System 程序]
    G --> C
    G --> H[Binder IPC]
    G --> I[記憶體對映]
    
    C --> J[設定同步]
    D --> K[事件傳輸]
    E --> L[狀態查詢]
    
    M[Hook 事件] --> F
    F --> N[規則比對]
    N --> O[動作執行]
    O --> P[結果回饋]
    P --> B
```

### 核心元件
```kotlin
// IPC 管理器介面
interface IPCManager {
    fun sendEvent(event: IPCEvent): Boolean
    fun queryConfiguration(query: ConfigQuery): ConfigResult?
    fun updateRule(rule: Rule): Boolean
    fun getSystemStatus(): SystemStatus
}

// 主要實作類別
class XProviderIPCManager : IPCManager {
    
    private val systemPropertiesChannel = SystemPropertiesChannel()
    private val contentProviderChannel = ContentProviderChannel()
    private val fileSystemChannel = FileSystemChannel()
    
    override fun sendEvent(event: IPCEvent): Boolean {
        return when (event.priority) {
            Priority.CRITICAL -> systemPropertiesChannel.send(event)
            Priority.HIGH -> contentProviderChannel.send(event)
            Priority.NORMAL -> fileSystemChannel.send(event)
            Priority.LOW -> fileSystemChannel.sendBatched(event)
        }
    }
}
```

## SystemProperties 通道

### 高優先順序即時通訊
```kotlin
class SystemPropertiesChannel {
    
    companion object {
        private const val PROP_PREFIX = "sys.nowakelock."
        private const val MAX_PROP_SIZE = 92 // Android 系統限制
        private const val EVENT_PROP = "${PROP_PREFIX}event"
        private const val CONFIG_PROP = "${PROP_PREFIX}config"
        private const val STATUS_PROP = "${PROP_PREFIX}status"
        private const val SYNC_PROP = "${PROP_PREFIX}sync"
    }
    
    // 傳送事件到系統屬性
    fun sendEvent(event: IPCEvent): Boolean {
        return try {
            val serialized = serializeEvent(event)
            if (serialized.length <= MAX_PROP_SIZE) {
                SystemProperties.set(EVENT_PROP, serialized)
                true
            } else {
                // 大資料使用分段傳輸
                sendChunkedData(EVENT_PROP, serialized)
            }
        } catch (e: Exception) {
            XposedBridge.log("Failed to send event via SystemProperties: ${e.message}")
            false
        }
    }
    
    // 分段傳輸大資料
    private fun sendChunkedData(property: String, data: String): Boolean {
        val chunks = data.chunked(MAX_PROP_SIZE - 10) // 預留分段識別空間
        val chunkCount = chunks.size
        
        try {
            // 設定分段數量
            SystemProperties.set("${property}.chunks", chunkCount.toString())
            
            // 傳送各分段
            chunks.forEachIndexed { index, chunk ->
                SystemProperties.set("${property}.$index", chunk)
            }
            
            // 設定完成識別
            SystemProperties.set("${property}.complete", System.currentTimeMillis().toString())
            return true
        } catch (e: Exception) {
            XposedBridge.log("Failed to send chunked data: ${e.message}")
            return false
        }
    }
    
    // 接收事件
    fun receiveEvent(): IPCEvent? {
        return try {
            val data = SystemProperties.get(EVENT_PROP, "")
            if (data.isNotEmpty()) {
                // 清除已讀資料
                SystemProperties.set(EVENT_PROP, "")
                deserializeEvent(data)
            } else {
                // 檢查分段資料
                receiveChunkedData(EVENT_PROP)
            }
        } catch (e: Exception) {
            XposedBridge.log("Failed to receive event: ${e.message}")
            null
        }
    }
    
    private fun receiveChunkedData(property: String): IPCEvent? {
        val chunkCountStr = SystemProperties.get("${property}.chunks", "")
        if (chunkCountStr.isEmpty()) return null
        
        val chunkCount = chunkCountStr.toIntOrNull() ?: return null
        val chunks = mutableListOf<String>()
        
        // 收集所有分段
        for (i in 0 until chunkCount) {
            val chunk = SystemProperties.get("${property}.$i", "")
            if (chunk.isEmpty()) return null // 分段缺失
            chunks.add(chunk)
        }
        
        // 檢查完成識別
        val completeTime = SystemProperties.get("${property}.complete", "")
        if (completeTime.isEmpty()) return null
        
        // 清理分段資料
        SystemProperties.set("${property}.chunks", "")
        for (i in 0 until chunkCount) {
            SystemProperties.set("${property}.$i", "")
        }
        SystemProperties.set("${property}.complete", "")
        
        // 重組資料
        val fullData = chunks.joinToString("")
        return deserializeEvent(fullData)
    }
}
```

### 事件序列化
```kotlin
object EventSerializer {
    
    // 緊湊型序列化格式
    fun serializeEvent(event: IPCEvent): String {
        return when (event.type) {
            IPCEventType.WAKELOCK_EVENT -> serializeWakelockEvent(event as WakelockIPCEvent)
            IPCEventType.ALARM_EVENT -> serializeAlarmEvent(event as AlarmIPCEvent)
            IPCEventType.SERVICE_EVENT -> serializeServiceEvent(event as ServiceIPCEvent)
            IPCEventType.RULE_UPDATE -> serializeRuleUpdate(event as RuleUpdateEvent)
            IPCEventType.CONFIG_SYNC -> serializeConfigSync(event as ConfigSyncEvent)
        }
    }
    
    private fun serializeWakelockEvent(event: WakelockIPCEvent): String {
        // 使用緊湊格式: 類型|時間戳記|套件名稱|標籤|動作|標識|...
        return buildString {
            append("WL") // 類型識別
            append("|${event.timestamp}")
            append("|${event.packageName}")
            append("|${event.tag}")
            append("|${event.action.ordinal}")
            append("|${event.flags}")
            append("|${event.uid}")
            append("|${if (event.isBlocked) 1 else 0}")
            if (event.instanceId.isNotEmpty()) {
                append("|${event.instanceId}")
            }
        }
    }
    
    fun deserializeEvent(data: String): IPCEvent? {
        val parts = data.split("|")
        if (parts.size < 3) return null
        
        return when (parts[0]) {
            "WL" -> deserializeWakelockEvent(parts)
            "AL" -> deserializeAlarmEvent(parts)
            "SV" -> deserializeServiceEvent(parts)
            "RU" -> deserializeRuleUpdate(parts)
            "CS" -> deserializeConfigSync(parts)
            else -> null
        }
    }
    
    private fun deserializeWakelockEvent(parts: List<String>): WakelockIPCEvent? {
        if (parts.size < 8) return null
        
        return try {
            WakelockIPCEvent(
                timestamp = parts[1].toLong(),
                packageName = parts[2],
                tag = parts[3],
                action = ActionType.values()[parts[4].toInt()],
                flags = parts[5].toInt(),
                uid = parts[6].toInt(),
                isBlocked = parts[7] == "1",
                instanceId = if (parts.size > 8) parts[8] else ""
            )
        } catch (e: Exception) {
            XposedBridge.log("Failed to deserialize WakeLock event: ${e.message}")
            null
        }
    }
}
```

## ContentProvider 通道

### 中等優先順序資料傳輸
```kotlin
class ContentProviderChannel {
    
    companion object {
        private const val AUTHORITY = "com.js.nowakelock.provider"
        private const val BASE_URI = "content://$AUTHORITY"
        private val EVENTS_URI = Uri.parse("$BASE_URI/events")
        private val RULES_URI = Uri.parse("$BASE_URI/rules")
        private val APPS_URI = Uri.parse("$BASE_URI/apps")
        private val STATS_URI = Uri.parse("$BASE_URI/stats")
    }
    
    fun sendEvent(event: IPCEvent): Boolean {
        return try {
            val contentValues = eventToContentValues(event)
            val uri = when (event.type) {
                IPCEventType.WAKELOCK_EVENT,
                IPCEventType.ALARM_EVENT,
                IPCEventType.SERVICE_EVENT -> EVENTS_URI
                IPCEventType.RULE_UPDATE -> RULES_URI
                else -> return false
            }
            
            // 使用 ContentResolver 插入資料
            val context = getSystemContext()
            val result = context.contentResolver.insert(uri, contentValues)
            result != null
        } catch (e: Exception) {
            XposedBridge.log("ContentProvider send failed: ${e.message}")
            false
        }
    }
    
    fun queryRules(packageName: String? = null): List<Rule> {
        return try {
            val context = getSystemContext()
            val selection = packageName?.let { "package_name = ?" }
            val selectionArgs = packageName?.let { arrayOf(it) }
            
            val cursor = context.contentResolver.query(
                RULES_URI,
                null,
                selection,
                selectionArgs,
                "priority DESC"
            )
            
            cursor?.use { 
                cursorToRules(it)
            } ?: emptyList()
        } catch (e: Exception) {
            XposedBridge.log("Failed to query rules: ${e.message}")
            emptyList()
        }
    }
    
    fun updateRule(rule: Rule): Boolean {
        return try {
            val context = getSystemContext()
            val contentValues = ruleToContentValues(rule)
            val uri = Uri.withAppendedPath(RULES_URI, rule.id)
            
            val count = context.contentResolver.update(
                uri,
                contentValues,
                null,
                null
            )
            count > 0
        } catch (e: Exception) {
            XposedBridge.log("Failed to update rule: ${e.message}")
            false
        }
    }
    
    private fun eventToContentValues(event: IPCEvent): ContentValues {
        return ContentValues().apply {
            put("type", event.type.ordinal)
            put("timestamp", event.timestamp)
            put("priority", event.priority.ordinal)
            
            when (event) {
                is WakelockIPCEvent -> {
                    put("package_name", event.packageName)
                    put("name", event.tag)
                    put("action", event.action.ordinal)
                    put("flags", event.flags)
                    put("uid", event.uid)
                    put("is_blocked", event.isBlocked)
                    put("instance_id", event.instanceId)
                }
                is AlarmIPCEvent -> {
                    put("package_name", event.packageName)
                    put("name", event.tag)
                    put("alarm_type", event.alarmType)
                    put("trigger_time", event.triggerTime)
                    put("is_blocked", event.isBlocked)
                }
                // 其他事件類型...
            }
        }
    }
}

// ContentProvider 實作
class NoWakeLockProvider : ContentProvider() {
    
    private lateinit var database: AppDatabase
    private lateinit var infoDatabase: InfoDatabase
    
    private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
        addURI(AUTHORITY, "events", EVENTS_CODE)
        addURI(AUTHORITY, "events/#", EVENT_CODE)
        addURI(AUTHORITY, "rules", RULES_CODE)
        addURI(AUTHORITY, "rules/#", RULE_CODE)
        addURI(AUTHORITY, "apps", APPS_CODE)
        addURI(AUTHORITY, "stats/*", STATS_CODE)
    }
    
    override fun onCreate(): Boolean {
        context?.let { ctx ->
            database = AppDatabase.getInstance(ctx)
            infoDatabase = InfoDatabase.getInstance(ctx)
        }
        return true
    }
    
    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return when (uriMatcher.match(uri)) {
            EVENTS_CODE -> {
                values?.let { 
                    insertEvent(it)
                    Uri.withAppendedPath(uri, values.getAsString("instance_id"))
                }
            }
            RULES_CODE -> {
                values?.let {
                    insertRule(it)
                    Uri.withAppendedPath(uri, values.getAsString("id"))
                }
            }
            else -> null
        }
    }
    
    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? {
        return when (uriMatcher.match(uri)) {
            RULES_CODE -> queryRules(selection, selectionArgs, sortOrder)
            APPS_CODE -> queryApps(selection, selectionArgs, sortOrder)
            STATS_CODE -> queryStats(uri.lastPathSegment!!, selection, selectionArgs)
            else -> null
        }
    }
    
    private fun insertEvent(values: ContentValues) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val event = contentValuesToEvent(values)
                infoDatabase.infoEventDao().insert(event)
            } catch (e: Exception) {
                Log.e("NoWakeLockProvider", "Failed to insert event", e)
            }
        }
    }
}
```

## 檔案系統通道

### 低優先順序批次傳輸
```kotlin
class FileSystemChannel {
    
    companion object {
        private val DATA_DIR = File("/data/system/nowakelock")
        private val EVENTS_DIR = File(DATA_DIR, "events")
        private val CONFIG_DIR = File(DATA_DIR, "config")
        private val TEMP_DIR = File(DATA_DIR, "temp")
        private val BATCH_FILE = File(EVENTS_DIR, "batch_events.json")
        private val CONFIG_FILE = File(CONFIG_DIR, "current_config.json")
    }
    
    init {
        ensureDirectoryStructure()
    }
    
    private fun ensureDirectoryStructure() {
        try {
            listOf(DATA_DIR, EVENTS_DIR, CONFIG_DIR, TEMP_DIR).forEach { dir ->
                if (!dir.exists()) {
                    dir.mkdirs()
                    // 設定權限允許 system 程序存取
                    Runtime.getRuntime().exec("chmod 755 ${dir.absolutePath}")
                }
            }
        } catch (e: Exception) {
            XposedBridge.log("Failed to create directory structure: ${e.message}")
        }
    }
    
    fun send(event: IPCEvent): Boolean {
        return try {
            val eventFile = File(EVENTS_DIR, "event_${System.currentTimeMillis()}.json")
            val json = Gson().toJson(event)
            eventFile.writeText(json)
            
            // 設定檔案權限
            Runtime.getRuntime().exec("chmod 644 ${eventFile.absolutePath}")
            true
        } catch (e: Exception) {
            XposedBridge.log("Failed to write event file: ${e.message}")
            false
        }
    }
    
    fun sendBatched(event: IPCEvent): Boolean {
        synchronized(BATCH_FILE) {
            return try {
                val events = if (BATCH_FILE.exists()) {
                    val existingJson = BATCH_FILE.readText()
                    val type = object : TypeToken<MutableList<IPCEvent>>() {}.type
                    Gson().fromJson<MutableList<IPCEvent>>(existingJson, type) ?: mutableListOf()
                } else {
                    mutableListOf()
                }
                
                events.add(event)
                
                // 限制批次大小
                if (events.size > 100) {
                    events.removeAt(0) // 移除最舊的事件
                }
                
                val json = Gson().toJson(events)
                BATCH_FILE.writeText(json)
                true
            } catch (e: Exception) {
                XposedBridge.log("Failed to write batch file: ${e.message}")
                false
            }
        }
    }
    
    fun readBatchedEvents(): List<IPCEvent> {
        synchronized(BATCH_FILE) {
            return try {
                if (!BATCH_FILE.exists()) {
                    return emptyList()
                }
                
                val json = BATCH_FILE.readText()
                val type = object : TypeToken<List<IPCEvent>>() {}.type
                val events = Gson().fromJson<List<IPCEvent>>(json, type) ?: emptyList()
                
                // 清空批次檔案
                BATCH_FILE.delete()
                
                events
            } catch (e: Exception) {
                XposedBridge.log("Failed to read batch file: ${e.message}")
                emptyList()
            }
        }
    }
    
    fun writeConfiguration(config: Configuration): Boolean {
        return try {
            val json = Gson().toJson(config)
            CONFIG_FILE.writeText(json)
            Runtime.getRuntime().exec("chmod 644 ${CONFIG_FILE.absolutePath}")
            true
        } catch (e: Exception) {
            XposedBridge.log("Failed to write configuration: ${e.message}")
            false
        }
    }
    
    fun readConfiguration(): Configuration? {
        return try {
            if (!CONFIG_FILE.exists()) return null
            
            val json = CONFIG_FILE.readText()
            Gson().fromJson(json, Configuration::class.java)
        } catch (e: Exception) {
            XposedBridge.log("Failed to read configuration: ${e.message}")
            null
        }
    }
}
```

## 設定同步機制

### 設定管理器
```kotlin
class ConfigurationSynchronizer(
    private val ipcManager: IPCManager,
    private val database: AppDatabase
) {
    
    private val syncScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val configCache = mutableMapOf<String, CachedConfig>()
    
    fun startSynchronization() {
        // 啟動定期同步任務
        syncScope.launch {
            while (isActive) {
                try {
                    performFullSync()
                    delay(60_000) // 每分鐘同步一次
                } catch (e: Exception) {
                    XposedBridge.log("Sync failed: ${e.message}")
                    delay(10_000) // 錯誤時等待10秒重試
                }
            }
        }
        
        // 監聽設定變更
        syncScope.launch {
            database.wakelockRuleDao().getAllRules()
                .distinctUntilChanged()
                .collect { rules ->
                    syncRulesToXposed(rules)
                }
        }
    }
    
    private suspend fun performFullSync() {
        // 1. 同步所有規則
        val allRules = database.wakelockRuleDao().getAllRules().first() +
                      database.alarmRuleDao().getAllRules().first() +
                      database.serviceRuleDao().getAllRules().first()
        
        syncRulesToXposed(allRules)
        
        // 2. 同步使用者偏好
        val preferences = database.userPreferencesDao().getUserPreferences().first()
        syncPreferencesToXposed(preferences)
        
        // 3. 同步應用程式資訊
        val apps = database.appInfoDao().getAllApps().first()
        syncAppsToXposed(apps)
    }
    
    private fun syncRulesToXposed(rules: List<Rule>) {
        val config = Configuration(
            version = System.currentTimeMillis(),
            rules = rules,
            updateTime = System.currentTimeMillis()
        )
        
        val event = ConfigSyncEvent(
            timestamp = System.currentTimeMillis(),
            configuration = config,
            syncType = SyncType.FULL_RULES
        )
        
        ipcManager.sendEvent(event)
    }
    
    fun onRuleUpdated(rule: Rule) {
        // 增量同步單一規則
        val event = RuleUpdateEvent(
            timestamp = System.currentTimeMillis(),
            rule = rule,
            operation = RuleOperation.UPDATE
        )
        
        ipcManager.sendEvent(event)
        
        // 更新本機快取
        updateConfigCache(rule)
    }
    
    fun onRuleDeleted(ruleId: String) {
        val event = RuleUpdateEvent(
            timestamp = System.currentTimeMillis(),
            ruleId = ruleId,
            operation = RuleOperation.DELETE
        )
        
        ipcManager.sendEvent(event)
        
        // 從快取中移除
        configCache.remove(ruleId)
    }
    
    private fun updateConfigCache(rule: Rule) {
        configCache[rule.id] = CachedConfig(
            rule = rule,
            cacheTime = System.currentTimeMillis(),
            version = rule.lastModifiedTime
        )
    }
}

// 設定資料類別
data class Configuration(
    val version: Long,
    val rules: List<Rule>,
    val preferences: UserPreferences? = null,
    val apps: List<AppInfo>? = null,
    val updateTime: Long
)

data class CachedConfig(
    val rule: Rule,
    val cacheTime: Long,
    val version: Long
) {
    fun isExpired(maxAge: Long = 300_000): Boolean {
        return System.currentTimeMillis() - cacheTime > maxAge
    }
}

enum class SyncType {
    FULL_RULES, INCREMENTAL_RULE, PREFERENCES, APPS
}

enum class RuleOperation {
    CREATE, UPDATE, DELETE
}
```

## 狀態查詢系統

### 狀態管理器
```kotlin
class SystemStatusManager(
    private val ipcManager: IPCManager
) {
    
    fun getModuleStatus(): ModuleStatus {
        val statusQuery = StatusQuery(
            type = StatusType.MODULE_STATUS,
            timestamp = System.currentTimeMillis()
        )
        
        val result = ipcManager.queryConfiguration(statusQuery)
        return result?.let {
            parseModuleStatus(it.data)
        } ?: ModuleStatus.UNKNOWN
    }
    
    fun getHookStatus(): Map<HookType, HookStatus> {
        val statusQuery = StatusQuery(
            type = StatusType.HOOK_STATUS,
            timestamp = System.currentTimeMillis()
        )
        
        val result = ipcManager.queryConfiguration(statusQuery)
        return result?.let {
            parseHookStatus(it.data)
        } ?: emptyMap()
    }
    
    fun getPerformanceMetrics(): PerformanceMetrics {
        val statusQuery = StatusQuery(
            type = StatusType.PERFORMANCE_METRICS,
            timestamp = System.currentTimeMillis()
        )
        
        val result = ipcManager.queryConfiguration(statusQuery)
        return result?.let {
            parsePerformanceMetrics(it.data)
        } ?: PerformanceMetrics.DEFAULT
    }
    
    private fun parseModuleStatus(data: String): ModuleStatus {
        return try {
            val parts = data.split("|")
            ModuleStatus(
                isActive = parts[0] == "1",
                version = parts[1],
                loadTime = parts[2].toLong(),
                hookCount = parts[3].toInt(),
                errorCount = parts[4].toInt(),
                lastActivity = parts[5].toLong()
            )
        } catch (e: Exception) {
            ModuleStatus.UNKNOWN
        }
    }
}

// 狀態資料類別
data class ModuleStatus(
    val isActive: Boolean,
    val version: String,
    val loadTime: Long,
    val hookCount: Int,
    val errorCount: Int,
    val lastActivity: Long
) {
    companion object {
        val UNKNOWN = ModuleStatus(
            isActive = false,
            version = "unknown",
            loadTime = 0,
            hookCount = 0,
            errorCount = 0,
            lastActivity = 0
        )
    }
}

data class HookStatus(
    val isHooked: Boolean,
    val hookTime: Long,
    val callCount: Long,
    val errorCount: Long,
    val avgDuration: Long
)

enum class HookType {
    WAKELOCK_ACQUIRE, WAKELOCK_RELEASE,
    ALARM_TRIGGER, ALARM_SET,
    SERVICE_START, SERVICE_STOP, SERVICE_BIND
}

enum class StatusType {
    MODULE_STATUS, HOOK_STATUS, PERFORMANCE_METRICS, RULE_COUNT
}
```

## 錯誤處理和重試機制

### 可靠性保障
```kotlin
class ReliableIPCManager(
    private val primaryChannel: SystemPropertiesChannel,
    private val secondaryChannel: ContentProviderChannel,
    private val fallbackChannel: FileSystemChannel
) : IPCManager {
    
    private val retryPolicy = RetryPolicy(
        maxRetries = 3,
        baseDelay = 1000,
        maxDelay = 10000,
        backoffMultiplier = 2.0
    )
    
    override fun sendEvent(event: IPCEvent): Boolean {
        return withRetry(retryPolicy) {
            when (event.priority) {
                Priority.CRITICAL -> {
                    primaryChannel.send(event) || 
                    secondaryChannel.send(event)
                }
                Priority.HIGH -> {
                    secondaryChannel.send(event) ||
                    primaryChannel.send(event)
                }
                Priority.NORMAL -> {
                    fallbackChannel.send(event)
                }
                Priority.LOW -> {
                    fallbackChannel.sendBatched(event)
                }
            }
        }
    }
    
    private fun <T> withRetry(policy: RetryPolicy, operation: () -> T): T {
        var lastException: Exception? = null
        var delay = policy.baseDelay
        
        repeat(policy.maxRetries) { attempt ->
            try {
                return operation()
            } catch (e: Exception) {
                lastException = e
                XposedBridge.log("IPC attempt ${attempt + 1} failed: ${e.message}")
                
                if (attempt < policy.maxRetries - 1) {
                    Thread.sleep(delay)
                    delay = minOf(delay * policy.backoffMultiplier.toLong(), policy.maxDelay)
                }
            }
        }
        
        throw lastException ?: Exception("All retry attempts failed")
    }
}

data class RetryPolicy(
    val maxRetries: Int,
    val baseDelay: Long,
    val maxDelay: Long,
    val backoffMultiplier: Double
)
```

!!! info "IPC 設計原則"
    NoWakeLock 的 IPC 系統採用多通道冗餘設計，確保在各種系統環境下都能可靠地進行程序間通訊。

!!! warning "權限注意事項"
    跨程序通訊需要合適的權限設定，特別是檔案系統通道需要確保 system 程序能夠存取共享目錄。

!!! tip "效能最佳化"
    根據資料的優先順序和大小選擇合適的通訊通道，高頻小資料使用 SystemProperties，大資料使用 ContentProvider 或檔案系統。