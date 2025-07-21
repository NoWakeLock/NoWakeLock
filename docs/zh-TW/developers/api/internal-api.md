# 內部 API

NoWakeLock 內部 API 提供了模組間通訊、資料處理和系統整合的核心介面，主要用於 Xposed 模組與應用程式之間的深度整合。

## XProvider 內部介面

### 核心通訊介面
```kotlin
// XProvider 核心介面
interface XProviderInterface {
    
    // 事件管理
    fun insertEvent(event: InfoEvent): Boolean
    fun updateEvent(instanceId: String, endTime: Long, duration: Long): Boolean
    fun getActiveEvents(packageName: String? = null): List<InfoEvent>
    
    // 規則管理
    fun getRules(packageName: String? = null, type: RuleType? = null): List<Rule>
    fun getRule(ruleId: String): Rule?
    fun upsertRule(rule: Rule): Boolean
    fun deleteRule(ruleId: String): Boolean
    
    // 設定同步
    fun syncConfiguration(): Boolean
    fun getConfigurationVersion(): Long
    fun markConfigurationDirty()
    
    // 狀態查詢
    fun getModuleStatus(): ModuleStatus
    fun getHookStatus(): Map<HookType, HookStatus>
    fun getPerformanceMetrics(): PerformanceMetrics
}

// XProvider 實作類別
class XProvider private constructor() : XProviderInterface {
    
    companion object {
        @Volatile
        private var INSTANCE: XProvider? = null
        
        fun getInstance(): XProvider {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: XProvider().also { INSTANCE = it }
            }
        }
        
        // 靜態方法用於 Xposed 模組呼叫
        @JvmStatic
        fun insertEventStatic(event: InfoEvent): Boolean {
            return getInstance().insertEvent(event)
        }
        
        @JvmStatic
        fun getRulesStatic(packageName: String, type: Int): Array<Rule> {
            val ruleType = RuleType.values()[type]
            return getInstance().getRules(packageName, ruleType).toTypedArray()
        }
    }
    
    override fun insertEvent(event: InfoEvent): Boolean {
        return try {
            when (getAvailableChannel()) {
                ChannelType.SYSTEM_PROPERTIES -> insertEventViaProperties(event)
                ChannelType.CONTENT_PROVIDER -> insertEventViaProvider(event)
                ChannelType.FILE_SYSTEM -> insertEventViaFile(event)
                else -> false
            }
        } catch (e: Exception) {
            XposedBridge.log("Failed to insert event: ${e.message}")
            false
        }
    }
}
```

### 通道選擇策略
```kotlin
enum class ChannelType {
    SYSTEM_PROPERTIES, CONTENT_PROVIDER, FILE_SYSTEM, MEMORY_MAPPED
}

class ChannelSelector {
    
    private val channelHealth = mutableMapOf<ChannelType, ChannelHealth>()
    
    fun getOptimalChannel(
        dataSize: Int,
        priority: Priority,
        reliability: ReliabilityLevel
    ): ChannelType {
        val availableChannels = getHealthyChannels()
        
        return when {
            priority == Priority.CRITICAL && dataSize <= 80 -> {
                // 緊急小資料優先使用系統屬性
                if (ChannelType.SYSTEM_PROPERTIES in availableChannels) {
                    ChannelType.SYSTEM_PROPERTIES
                } else {
                    ChannelType.CONTENT_PROVIDER
                }
            }
            dataSize > 1000 -> {
                // 大資料使用檔案系統
                ChannelType.FILE_SYSTEM
            }
            reliability == ReliabilityLevel.HIGH -> {
                // 高可靠性使用 ContentProvider
                ChannelType.CONTENT_PROVIDER
            }
            else -> {
                // 預設策略
                availableChannels.firstOrNull() ?: ChannelType.FILE_SYSTEM
            }
        }
    }
    
    private fun getHealthyChannels(): List<ChannelType> {
        return channelHealth.entries
            .filter { it.value.isHealthy() }
            .map { it.key }
            .sortedBy { channelHealth[it]?.latency ?: Long.MAX_VALUE }
    }
    
    fun recordChannelPerformance(
        channel: ChannelType,
        success: Boolean,
        latency: Long
    ) {
        channelHealth.compute(channel) { _, health ->
            health?.update(success, latency) ?: ChannelHealth().update(success, latency)
        }
    }
}

data class ChannelHealth(
    var successCount: Long = 0,
    var failureCount: Long = 0,
    var averageLatency: Long = 0,
    var lastUpdate: Long = System.currentTimeMillis()
) {
    fun update(success: Boolean, latency: Long): ChannelHealth {
        val totalOperations = successCount + failureCount
        
        return copy(
            successCount = if (success) successCount + 1 else successCount,
            failureCount = if (!success) failureCount + 1 else failureCount,
            averageLatency = (averageLatency * totalOperations + latency) / (totalOperations + 1),
            lastUpdate = System.currentTimeMillis()
        )
    }
    
    fun isHealthy(): Boolean {
        val totalOperations = successCount + failureCount
        if (totalOperations == 0L) return true
        
        val successRate = successCount.toFloat() / totalOperations
        val isRecent = System.currentTimeMillis() - lastUpdate < 300_000 // 5分鐘
        
        return successRate >= 0.8f && averageLatency < 1000 && isRecent
    }
    
    val latency: Long get() = averageLatency
}
```

## Rule Engine API

### 規則比對引擎
```kotlin
// 規則引擎介面
interface RuleEngine {
    fun evaluateWakeLock(info: WakeLockInfo): RuleResult
    fun evaluateAlarm(info: AlarmInfo): RuleResult
    fun evaluateService(info: ServiceInfo): RuleResult
    
    fun precompileRules(rules: List<Rule>)
    fun invalidateCache()
    fun getMatchingRules(info: ComponentInfo): List<Rule>
}

// 規則引擎實作
class RuleEngineImpl : RuleEngine {
    
    private val compiledRules = ConcurrentHashMap<String, CompiledRule>()
    private val ruleCache = LRUCache<String, RuleResult>(1000)
    
    override fun evaluateWakeLock(info: WakeLockInfo): RuleResult {
        val cacheKey = "${info.packageName}:${info.tag}:${info.flags}"
        
        // 檢查快取
        ruleCache.get(cacheKey)?.let { cached ->
            if (!cached.isExpired()) {
                return cached
            }
        }
        
        // 執行規則比對
        val result = performRuleMatching(info)
        
        // 快取結果
        ruleCache.put(cacheKey, result)
        
        return result
    }
    
    private fun performRuleMatching(info: WakeLockInfo): RuleResult {
        val matchingRules = getMatchingRulesForWakeLock(info)
        
        // 按優先順序排序
        val sortedRules = matchingRules.sortedByDescending { it.priority }
        
        // 找到第一個比對的規則
        val matchedRule = sortedRules.firstOrNull { rule ->
            matchesRule(info, rule)
        }
        
        return matchedRule?.let { rule ->
            RuleResult(
                action = rule.action,
                timeout = rule.timeout,
                ruleId = rule.id,
                ruleName = rule.name,
                matchTime = System.currentTimeMillis(),
                isFromCache = false
            )
        } ?: RuleResult.DEFAULT_ALLOW
    }
    
    private fun matchesRule(info: WakeLockInfo, rule: Rule): Boolean {
        // 套件名稱比對
        if (!matchesPackage(info.packageName, rule.packageName)) {
            return false
        }
        
        // 使用者比對
        if (rule.userId != -1 && rule.userId != info.userId) {
            return false
        }
        
        // 標籤比對
        return when (rule.matchType) {
            MatchType.EXACT -> rule.target == info.tag
            MatchType.CONTAINS -> info.tag.contains(rule.target, ignoreCase = true)
            MatchType.REGEX -> {
                val compiledRule = getCompiledRule(rule)
                compiledRule.regex?.matches(info.tag) ?: false
            }
            MatchType.WILDCARD -> {
                val compiledRule = getCompiledRule(rule)
                compiledRule.pattern?.matches(info.tag) ?: false
            }
        }
    }
    
    private fun getCompiledRule(rule: Rule): CompiledRule {
        return compiledRules.getOrPut(rule.id) {
            CompileRule(rule)
        }
    }
    
    override fun precompileRules(rules: List<Rule>) {
        rules.forEach { rule ->
            compiledRules[rule.id] = CompileRule(rule)
        }
    }
    
    private fun CompileRule(rule: Rule): CompiledRule {
        return CompiledRule(
            rule = rule,
            regex = if (rule.matchType == MatchType.REGEX) {
                try {
                    rule.target.toRegex(RegexOption.IGNORE_CASE)
                } catch (e: Exception) {
                    null
                }
            } else null,
            pattern = if (rule.matchType == MatchType.WILDCARD) {
                try {
                    rule.target
                        .replace("*", ".*")
                        .replace("?", ".")
                        .toRegex(RegexOption.IGNORE_CASE)
                } catch (e: Exception) {
                    null
                }
            } else null
        )
    }
}

data class CompiledRule(
    val rule: Rule,
    val regex: Regex?,
    val pattern: Regex?,
    val compileTime: Long = System.currentTimeMillis()
)

data class RuleResult(
    val action: ActionType,
    val timeout: Long = 0,
    val delay: Long = 0,
    val maxPerHour: Int = 0,
    val ruleId: String = "",
    val ruleName: String = "",
    val matchTime: Long = System.currentTimeMillis(),
    val isFromCache: Boolean = false,
    val confidence: Float = 1.0f
) {
    fun isExpired(maxAge: Long = 60_000): Boolean {
        return System.currentTimeMillis() - matchTime > maxAge
    }
    
    companion object {
        val DEFAULT_ALLOW = RuleResult(
            action = ActionType.ALLOW,
            ruleId = "default",
            ruleName = "預設允許"
        )
        
        val DEFAULT_BLOCK = RuleResult(
            action = ActionType.BLOCK,
            ruleId = "default_block",
            ruleName = "預設阻止"
        )
    }
}
```

## Hook 回呼介面

### Hook 事件處理器
```kotlin
// Hook 事件介面
interface HookEventHandler {
    fun onWakeLockAcquire(info: WakeLockInfo): HookResult
    fun onWakeLockRelease(instanceId: String, duration: Long): HookResult
    fun onAlarmTrigger(info: AlarmInfo): HookResult
    fun onServiceStart(info: ServiceInfo): HookResult
    fun onServiceStop(info: ServiceInfo): HookResult
}

// Hook 結果
data class HookResult(
    val shouldBlock: Boolean,
    val modifiedArgs: Array<Any?>? = null,
    val timeout: Long = 0,
    val reason: String = "",
    val metadata: Map<String, Any> = emptyMap()
) {
    companion object {
        val ALLOW = HookResult(shouldBlock = false)
        val BLOCK = HookResult(shouldBlock = true)
        
        fun allow(reason: String = "") = HookResult(shouldBlock = false, reason = reason)
        fun block(reason: String = "") = HookResult(shouldBlock = true, reason = reason)
        fun timeout(timeout: Long, reason: String = "") = HookResult(
            shouldBlock = false, 
            timeout = timeout, 
            reason = reason
        )
    }
}

// Hook 事件處理器實作
class HookEventHandlerImpl(
    private val ruleEngine: RuleEngine,
    private val xProvider: XProvider,
    private val statisticsCollector: StatisticsCollector
) : HookEventHandler {
    
    override fun onWakeLockAcquire(info: WakeLockInfo): HookResult {
        // 記錄開始時間
        val startTime = System.currentTimeMillis()
        
        try {
            // 1. 快速過濾
            if (shouldSkipPackage(info.packageName)) {
                return HookResult.ALLOW
            }
            
            // 2. 規則評估
            val ruleResult = ruleEngine.evaluateWakeLock(info)
            
            // 3. 建立事件記錄
            val event = InfoEvent(
                instanceId = info.instanceId,
                name = info.tag,
                type = InfoEvent.Type.WakeLock,
                packageName = info.packageName,
                userId = info.userId,
                startTime = startTime,
                flags = info.flags,
                uid = info.uid,
                pid = info.pid
            )
            
            // 4. 根據規則執行動作
            val hookResult = when (ruleResult.action) {
                ActionType.ALLOW -> {
                    event.isBlocked = false
                    HookResult.allow("規則允許: ${ruleResult.ruleName}")
                }
                ActionType.LIMIT -> {
                    event.isBlocked = false
                    HookResult.timeout(
                        timeout = ruleResult.timeout,
                        reason = "規則限制: ${ruleResult.ruleName}, 逾時${ruleResult.timeout}ms"
                    )
                }
                ActionType.BLOCK -> {
                    event.isBlocked = true
                    HookResult.block("規則阻止: ${ruleResult.ruleName}")
                }
            }
            
            // 5. 記錄事件
            xProvider.insertEvent(event)
            
            // 6. 更新統計
            statisticsCollector.recordWakeLockAttempt(info, ruleResult)
            
            return hookResult
            
        } catch (e: Exception) {
            XposedBridge.log("WakeLock hook handler failed: ${e.message}")
            return HookResult.ALLOW // 失敗時預設允許，避免系統問題
        }
    }
    
    override fun onWakeLockRelease(instanceId: String, duration: Long): HookResult {
        try {
            // 更新事件結束時間
            xProvider.updateEvent(instanceId, System.currentTimeMillis(), duration)
            
            // 更新統計
            statisticsCollector.recordWakeLockRelease(instanceId, duration)
            
            return HookResult.ALLOW
        } catch (e: Exception) {
            XposedBridge.log("WakeLock release handler failed: ${e.message}")
            return HookResult.ALLOW
        }
    }
    
    private fun shouldSkipPackage(packageName: String): Boolean {
        return when {
            packageName == "android" -> false // 系統核心套件需要處理
            packageName.startsWith("com.android.systemui") -> true // 跳過系統UI
            packageName.startsWith("android.uid.") -> true // 跳過UID套件
            else -> false
        }
    }
}
```

## 統計收集器 API

### 統計資料收集介面
```kotlin
// 統計收集器介面
interface StatisticsCollector {
    fun recordWakeLockAttempt(info: WakeLockInfo, result: RuleResult)
    fun recordWakeLockRelease(instanceId: String, duration: Long)
    fun recordAlarmTrigger(info: AlarmInfo, result: RuleResult)
    fun recordServiceStart(info: ServiceInfo, result: RuleResult)
    
    fun getRealtimeStats(packageName: String): RealtimeStats
    fun flushPendingStats()
}

// 統計收集器實作
class StatisticsCollectorImpl(
    private val realtimeCounter: RealtimeCounter,
    private val batchProcessor: BatchProcessor
) : StatisticsCollector {
    
    private val pendingStats = ConcurrentLinkedQueue<StatisticEvent>()
    private val flushThreshold = 100
    
    override fun recordWakeLockAttempt(info: WakeLockInfo, result: RuleResult) {
        // 即時計數
        realtimeCounter.incrementWakeLockAcquire(info.packageName, info.tag)
        
        // 批次統計
        val statEvent = StatisticEvent(
            type = StatisticType.WAKELOCK_ACQUIRE,
            packageName = info.packageName,
            target = info.tag,
            timestamp = System.currentTimeMillis(),
            action = result.action,
            metadata = mapOf(
                "flags" to info.flags,
                "uid" to info.uid,
                "rule_id" to result.ruleId
            )
        )
        
        pendingStats.offer(statEvent)
        
        // 檢查是否需要重新整理
        if (pendingStats.size >= flushThreshold) {
            flushPendingStats()
        }
    }
    
    override fun flushPendingStats() {
        val stats = mutableListOf<StatisticEvent>()
        
        // 批次取出待處理統計
        while (stats.size < flushThreshold && !pendingStats.isEmpty()) {
            pendingStats.poll()?.let { stats.add(it) }
        }
        
        if (stats.isNotEmpty()) {
            batchProcessor.processBatch(stats)
        }
    }
    
    override fun getRealtimeStats(packageName: String): RealtimeStats {
        return realtimeCounter.getPackageStats(packageName)
    }
}

data class StatisticEvent(
    val type: StatisticType,
    val packageName: String,
    val target: String,
    val timestamp: Long,
    val action: ActionType,
    val metadata: Map<String, Any> = emptyMap()
)

enum class StatisticType {
    WAKELOCK_ACQUIRE, WAKELOCK_RELEASE,
    ALARM_TRIGGER, ALARM_SET,
    SERVICE_START, SERVICE_STOP
}

data class RealtimeStats(
    val packageName: String,
    val wakelockCount: Long,
    val alarmCount: Long,
    val serviceCount: Long,
    val blockedCount: Long,
    val lastActivity: Long,
    val updateTime: Long = System.currentTimeMillis()
)
```

## 快取管理 API

### 多級快取系統
```kotlin
// 快取管理器介面
interface CacheManager {
    fun <T> get(key: String, type: Class<T>): T?
    fun <T> put(key: String, value: T, ttl: Long = 300_000)
    fun invalidate(key: String)
    fun invalidateByPrefix(prefix: String)
    fun clear()
    fun getStats(): CacheStats
}

// 多級快取實作
class MultiLevelCacheManager : CacheManager {
    
    // L1: 記憶體快取 (最快存取)
    private val l1Cache = LRUCache<String, CacheEntry>(500)
    
    // L2: 磁碟快取 (持久化)
    private val l2Cache = DiskLRUCache.open(
        File("/data/system/nowakelock/cache"),
        1, 1, 10 * 1024 * 1024 // 10MB
    )
    
    // L3: 系統屬性快取 (跨程序)
    private val l3Cache = SystemPropertiesCache()
    
    override fun <T> get(key: String, type: Class<T>): T? {
        // L1 快取查詢
        l1Cache.get(key)?.let { entry ->
            if (!entry.isExpired()) {
                @Suppress("UNCHECKED_CAST")
                return entry.value as? T
            } else {
                l1Cache.remove(key)
            }
        }
        
        // L2 快取查詢
        l2Cache.get(key)?.let { snapshot ->
            try {
                val json = snapshot.getString(0)
                val entry = Gson().fromJson(json, CacheEntry::class.java)
                if (!entry.isExpired()) {
                    // 提升到 L1 快取
                    l1Cache.put(key, entry)
                    @Suppress("UNCHECKED_CAST")
                    return entry.value as? T
                } else {
                    l2Cache.remove(key)
                }
            } catch (e: Exception) {
                // 忽略反序列化錯誤
            }
        }
        
        // L3 快取查詢
        return l3Cache.get(key, type)
    }
    
    override fun <T> put(key: String, value: T, ttl: Long) {
        val entry = CacheEntry(
            value = value,
            expireTime = System.currentTimeMillis() + ttl,
            createTime = System.currentTimeMillis()
        )
        
        // 寫入 L1 快取
        l1Cache.put(key, entry)
        
        // 非同步寫入 L2 快取
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val editor = l2Cache.edit(key)
                editor?.let {
                    val json = Gson().toJson(entry)
                    it.set(0, json)
                    it.commit()
                }
            } catch (e: Exception) {
                // 忽略磁碟寫入錯誤
            }
        }
        
        // 小資料寫入 L3 快取
        if (estimateSize(value) <= 80) {
            l3Cache.put(key, value, ttl)
        }
    }
    
    override fun invalidate(key: String) {
        l1Cache.remove(key)
        l2Cache.remove(key)
        l3Cache.remove(key)
    }
    
    override fun invalidateByPrefix(prefix: String) {
        // L1 快取前綴清理
        val snapshot = l1Cache.snapshot()
        snapshot.keys.filter { it.startsWith(prefix) }.forEach { key ->
            l1Cache.remove(key)
        }
        
        // L2 和 L3 快取非同步清理
        CoroutineScope(Dispatchers.IO).launch {
            // 這裡需要遍歷所有鍵，效率較低，實際實作中可以考慮使用分割槽快取
        }
    }
    
    private fun estimateSize(value: Any?): Int {
        return when (value) {
            is String -> value.length
            is Number -> 8
            is Boolean -> 1
            else -> 100 // 估算值
        }
    }
}

data class CacheEntry(
    val value: Any?,
    val expireTime: Long,
    val createTime: Long
) {
    fun isExpired(): Boolean = System.currentTimeMillis() > expireTime
}

data class CacheStats(
    val l1Hits: Long,
    val l1Misses: Long,
    val l2Hits: Long,
    val l2Misses: Long,
    val l3Hits: Long,
    val l3Misses: Long,
    val totalRequests: Long,
    val hitRate: Float
) {
    val overallHitRate: Float
        get() = if (totalRequests > 0) {
            (l1Hits + l2Hits + l3Hits).toFloat() / totalRequests
        } else 0f
}
```

## 診斷和偵錯 API

### 內部診斷介面
```kotlin
// 診斷介面
interface DiagnosticsAPI {
    fun getSystemHealth(): SystemHealth
    fun getHookDiagnostics(): HookDiagnostics
    fun getCacheDiagnostics(): CacheDiagnostics
    fun getPerformanceDiagnostics(): PerformanceDiagnostics
    
    fun runDiagnosticTests(): DiagnosticResults
    fun exportDiagnosticData(): String
}

// 系統健康狀態
data class SystemHealth(
    val moduleLoaded: Boolean,
    val hooksActive: Map<String, Boolean>,
    val ipcChannelsStatus: Map<ChannelType, ChannelHealth>,
    val memoryUsage: MemoryUsage,
    val errorCount: Map<String, Int>,
    val lastError: String?,
    val uptime: Long
)

data class MemoryUsage(
    val heapUsed: Long,
    val heapMax: Long,
    val nativeUsed: Long,
    val cacheSize: Long
) {
    val heapUsagePercentage: Float
        get() = if (heapMax > 0) heapUsed.toFloat() / heapMax else 0f
}

// Hook 診斷資訊
data class HookDiagnostics(
    val hookSuccessRate: Map<String, Float>,
    val avgHookDuration: Map<String, Long>,
    val hookErrorCounts: Map<String, Long>,
    val methodResolutionStatus: Map<String, Boolean>,
    val parameterMappingStatus: Map<String, Boolean>
)

// 診斷實作
class DiagnosticsAPIImpl : DiagnosticsAPI {
    
    override fun runDiagnosticTests(): DiagnosticResults {
        val tests = listOf(
            ::testHookFunctionality,
            ::testIPCCommunication,
            ::testRuleEngine,
            ::testDataPersistence,
            ::testPerformance
        )
        
        val results = tests.map { test ->
            try {
                test()
            } catch (e: Exception) {
                DiagnosticResult(
                    name = test.name,
                    success = false,
                    message = "測試異常: ${e.message}",
                    duration = 0
                )
            }
        }
        
        return DiagnosticResults(
            timestamp = System.currentTimeMillis(),
            results = results,
            overallSuccess = results.all { it.success }
        )
    }
    
    private fun testHookFunctionality(): DiagnosticResult {
        val startTime = System.currentTimeMillis()
        
        // 測試各個 Hook 點是否正常工作
        val hookTests = mapOf(
            "WakeLock Acquire" to { testWakeLockHook() },
            "Alarm Trigger" to { testAlarmHook() },
            "Service Start" to { testServiceHook() }
        )
        
        val failures = hookTests.entries.mapNotNull { (name, test) ->
            try {
                if (!test()) name else null
            } catch (e: Exception) {
                "$name: ${e.message}"
            }
        }
        
        val duration = System.currentTimeMillis() - startTime
        
        return DiagnosticResult(
            name = "Hook Functionality",
            success = failures.isEmpty(),
            message = if (failures.isEmpty()) "所有 Hook 正常工作" else "失敗的 Hook: ${failures.joinToString()}",
            duration = duration,
            details = hookTests.keys.toList()
        )
    }
    
    private fun testWakeLockHook(): Boolean {
        // 實際的 WakeLock Hook 測試邏輯
        return true // 簡化實作
    }
}

data class DiagnosticResults(
    val timestamp: Long,
    val results: List<DiagnosticResult>,
    val overallSuccess: Boolean
)

data class DiagnosticResult(
    val name: String,
    val success: Boolean,
    val message: String,
    val duration: Long,
    val details: List<String> = emptyList()
)
```

!!! info "內部 API 設計原則"
    內部 API 注重效能、可靠性和可維護性，提供了完整的模組間通訊、規則處理和診斷功能。

!!! warning "使用注意事項"
    內部 API 主要用於 NoWakeLock 內部元件間通訊，不保證向後相容性。外部開發者應使用公開的 ContentProvider API。

!!! tip "效能最佳化"
    內部 API 廣泛使用快取、批次處理和非同步處理機制，確保在高頻呼叫場景下保持良好效能。