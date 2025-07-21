# Internal API

NoWakeLock's internal API provides core interfaces for inter-module communication, data processing, and system integration, primarily used for deep integration between the Xposed module and application.

## XProvider Internal Interface

### Core Communication Interface
```kotlin
// XProvider core interface
interface XProviderInterface {
    
    // Event management
    fun insertEvent(event: InfoEvent): Boolean
    fun updateEvent(instanceId: String, endTime: Long, duration: Long): Boolean
    fun getActiveEvents(packageName: String? = null): List<InfoEvent>
    
    // Rule management
    fun getRules(packageName: String? = null, type: RuleType? = null): List<Rule>
    fun getRule(ruleId: String): Rule?
    fun upsertRule(rule: Rule): Boolean
    fun deleteRule(ruleId: String): Boolean
    
    // Configuration synchronization
    fun syncConfiguration(): Boolean
    fun getConfigurationVersion(): Long
    fun markConfigurationDirty()
    
    // Status queries
    fun getModuleStatus(): ModuleStatus
    fun getHookStatus(): Map<HookType, HookStatus>
    fun getPerformanceMetrics(): PerformanceMetrics
}

// XProvider implementation class
class XProvider private constructor() : XProviderInterface {
    
    companion object {
        @Volatile
        private var INSTANCE: XProvider? = null
        
        fun getInstance(): XProvider {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: XProvider().also { INSTANCE = it }
            }
        }
        
        // Static methods for Xposed module calls
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

### Channel Selection Strategy
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
                // Critical small data prioritizes system properties
                if (ChannelType.SYSTEM_PROPERTIES in availableChannels) {
                    ChannelType.SYSTEM_PROPERTIES
                } else {
                    ChannelType.CONTENT_PROVIDER
                }
            }
            dataSize > 1000 -> {
                // Large data uses file system
                ChannelType.FILE_SYSTEM
            }
            reliability == ReliabilityLevel.HIGH -> {
                // High reliability uses ContentProvider
                ChannelType.CONTENT_PROVIDER
            }
            else -> {
                // Default strategy
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
        val isRecent = System.currentTimeMillis() - lastUpdate < 300_000 // 5 minutes
        
        return successRate >= 0.8f && averageLatency < 1000 && isRecent
    }
    
    val latency: Long get() = averageLatency
}
```

## Rule Engine API

### Rule Matching Engine
```kotlin
// Rule engine interface
interface RuleEngine {
    fun evaluateWakeLock(info: WakeLockInfo): RuleResult
    fun evaluateAlarm(info: AlarmInfo): RuleResult
    fun evaluateService(info: ServiceInfo): RuleResult
    
    fun precompileRules(rules: List<Rule>)
    fun invalidateCache()
    fun getMatchingRules(info: ComponentInfo): List<Rule>
}

// Rule engine implementation
class RuleEngineImpl : RuleEngine {
    
    private val compiledRules = ConcurrentHashMap<String, CompiledRule>()
    private val ruleCache = LRUCache<String, RuleResult>(1000)
    
    override fun evaluateWakeLock(info: WakeLockInfo): RuleResult {
        val cacheKey = "${info.packageName}:${info.tag}:${info.flags}"
        
        // Check cache
        ruleCache.get(cacheKey)?.let { cached ->
            if (!cached.isExpired()) {
                return cached
            }
        }
        
        // Perform rule matching
        val result = performRuleMatching(info)
        
        // Cache result
        ruleCache.put(cacheKey, result)
        
        return result
    }
    
    private fun performRuleMatching(info: WakeLockInfo): RuleResult {
        val matchingRules = getMatchingRulesForWakeLock(info)
        
        // Sort by priority
        val sortedRules = matchingRules.sortedByDescending { it.priority }
        
        // Find first matching rule
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
        // Package matching
        if (!matchesPackage(info.packageName, rule.packageName)) {
            return false
        }
        
        // User matching
        if (rule.userId != -1 && rule.userId != info.userId) {
            return false
        }
        
        // Tag matching
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
            ruleName = "Default Allow"
        )
        
        val DEFAULT_BLOCK = RuleResult(
            action = ActionType.BLOCK,
            ruleId = "default_block",
            ruleName = "Default Block"
        )
    }
}
```

## Hook Callback Interface

### Hook Event Handler
```kotlin
// Hook event interface
interface HookEventHandler {
    fun onWakeLockAcquire(info: WakeLockInfo): HookResult
    fun onWakeLockRelease(instanceId: String, duration: Long): HookResult
    fun onAlarmTrigger(info: AlarmInfo): HookResult
    fun onServiceStart(info: ServiceInfo): HookResult
    fun onServiceStop(info: ServiceInfo): HookResult
}

// Hook result
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

// Hook event handler implementation
class HookEventHandlerImpl(
    private val ruleEngine: RuleEngine,
    private val xProvider: XProvider,
    private val statisticsCollector: StatisticsCollector
) : HookEventHandler {
    
    override fun onWakeLockAcquire(info: WakeLockInfo): HookResult {
        // Record start time
        val startTime = System.currentTimeMillis()
        
        try {
            // 1. Quick filtering
            if (shouldSkipPackage(info.packageName)) {
                return HookResult.ALLOW
            }
            
            // 2. Rule evaluation
            val ruleResult = ruleEngine.evaluateWakeLock(info)
            
            // 3. Create event record
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
            
            // 4. Execute action based on rule
            val hookResult = when (ruleResult.action) {
                ActionType.ALLOW -> {
                    event.isBlocked = false
                    HookResult.allow("Rule allowed: ${ruleResult.ruleName}")
                }
                ActionType.LIMIT -> {
                    event.isBlocked = false
                    HookResult.timeout(
                        timeout = ruleResult.timeout,
                        reason = "Rule limited: ${ruleResult.ruleName}, timeout ${ruleResult.timeout}ms"
                    )
                }
                ActionType.BLOCK -> {
                    event.isBlocked = true
                    HookResult.block("Rule blocked: ${ruleResult.ruleName}")
                }
            }
            
            // 5. Record event
            xProvider.insertEvent(event)
            
            // 6. Update statistics
            statisticsCollector.recordWakeLockAttempt(info, ruleResult)
            
            return hookResult
            
        } catch (e: Exception) {
            XposedBridge.log("WakeLock hook handler failed: ${e.message}")
            return HookResult.ALLOW // Default allow on failure to avoid system issues
        }
    }
    
    override fun onWakeLockRelease(instanceId: String, duration: Long): HookResult {
        try {
            // Update event end time
            xProvider.updateEvent(instanceId, System.currentTimeMillis(), duration)
            
            // Update statistics
            statisticsCollector.recordWakeLockRelease(instanceId, duration)
            
            return HookResult.ALLOW
        } catch (e: Exception) {
            XposedBridge.log("WakeLock release handler failed: ${e.message}")
            return HookResult.ALLOW
        }
    }
    
    private fun shouldSkipPackage(packageName: String): Boolean {
        return when {
            packageName == "android" -> false // System core package needs handling
            packageName.startsWith("com.android.systemui") -> true // Skip system UI
            packageName.startsWith("android.uid.") -> true // Skip UID packages
            else -> false
        }
    }
}
```

## Statistics Collector API

### Statistics Data Collection Interface
```kotlin
// Statistics collector interface
interface StatisticsCollector {
    fun recordWakeLockAttempt(info: WakeLockInfo, result: RuleResult)
    fun recordWakeLockRelease(instanceId: String, duration: Long)
    fun recordAlarmTrigger(info: AlarmInfo, result: RuleResult)
    fun recordServiceStart(info: ServiceInfo, result: RuleResult)
    
    fun getRealtimeStats(packageName: String): RealtimeStats
    fun flushPendingStats()
}

// Statistics collector implementation
class StatisticsCollectorImpl(
    private val realtimeCounter: RealtimeCounter,
    private val batchProcessor: BatchProcessor
) : StatisticsCollector {
    
    private val pendingStats = ConcurrentLinkedQueue<StatisticEvent>()
    private val flushThreshold = 100
    
    override fun recordWakeLockAttempt(info: WakeLockInfo, result: RuleResult) {
        // Real-time counting
        realtimeCounter.incrementWakeLockAcquire(info.packageName, info.tag)
        
        // Batch statistics
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
        
        // Check if flush is needed
        if (pendingStats.size >= flushThreshold) {
            flushPendingStats()
        }
    }
    
    override fun flushPendingStats() {
        val stats = mutableListOf<StatisticEvent>()
        
        // Batch extract pending statistics
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

## Cache Management API

### Multi-Level Cache System
```kotlin
// Cache manager interface
interface CacheManager {
    fun <T> get(key: String, type: Class<T>): T?
    fun <T> put(key: String, value: T, ttl: Long = 300_000)
    fun invalidate(key: String)
    fun invalidateByPrefix(prefix: String)
    fun clear()
    fun getStats(): CacheStats
}

// Multi-level cache implementation
class MultiLevelCacheManager : CacheManager {
    
    // L1: Memory cache (fastest access)
    private val l1Cache = LRUCache<String, CacheEntry>(500)
    
    // L2: Disk cache (persistent)
    private val l2Cache = DiskLRUCache.open(
        File("/data/system/nowakelock/cache"),
        1, 1, 10 * 1024 * 1024 // 10MB
    )
    
    // L3: System properties cache (cross-process)
    private val l3Cache = SystemPropertiesCache()
    
    override fun <T> get(key: String, type: Class<T>): T? {
        // L1 cache query
        l1Cache.get(key)?.let { entry ->
            if (!entry.isExpired()) {
                @Suppress("UNCHECKED_CAST")
                return entry.value as? T
            } else {
                l1Cache.remove(key)
            }
        }
        
        // L2 cache query
        l2Cache.get(key)?.let { snapshot ->
            try {
                val json = snapshot.getString(0)
                val entry = Gson().fromJson(json, CacheEntry::class.java)
                if (!entry.isExpired()) {
                    // Promote to L1 cache
                    l1Cache.put(key, entry)
                    @Suppress("UNCHECKED_CAST")
                    return entry.value as? T
                } else {
                    l2Cache.remove(key)
                }
            } catch (e: Exception) {
                // Ignore deserialization errors
            }
        }
        
        // L3 cache query
        return l3Cache.get(key, type)
    }
    
    override fun <T> put(key: String, value: T, ttl: Long) {
        val entry = CacheEntry(
            value = value,
            expireTime = System.currentTimeMillis() + ttl,
            createTime = System.currentTimeMillis()
        )
        
        // Write to L1 cache
        l1Cache.put(key, entry)
        
        // Asynchronously write to L2 cache
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val editor = l2Cache.edit(key)
                editor?.let {
                    val json = Gson().toJson(entry)
                    it.set(0, json)
                    it.commit()
                }
            } catch (e: Exception) {
                // Ignore disk write errors
            }
        }
        
        // Write small data to L3 cache
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
        // L1 cache prefix cleanup
        val snapshot = l1Cache.snapshot()
        snapshot.keys.filter { it.startsWith(prefix) }.forEach { key ->
            l1Cache.remove(key)
        }
        
        // L2 and L3 cache asynchronous cleanup
        CoroutineScope(Dispatchers.IO).launch {
            // Need to iterate through all keys, low efficiency, consider using partitioned cache in actual implementation
        }
    }
    
    private fun estimateSize(value: Any?): Int {
        return when (value) {
            is String -> value.length
            is Number -> 8
            is Boolean -> 1
            else -> 100 // Estimated value
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

## Diagnostics and Debugging API

### Internal Diagnostics Interface
```kotlin
// Diagnostics interface
interface DiagnosticsAPI {
    fun getSystemHealth(): SystemHealth
    fun getHookDiagnostics(): HookDiagnostics
    fun getCacheDiagnostics(): CacheDiagnostics
    fun getPerformanceDiagnostics(): PerformanceDiagnostics
    
    fun runDiagnosticTests(): DiagnosticResults
    fun exportDiagnosticData(): String
}

// System health status
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

// Hook diagnostic information
data class HookDiagnostics(
    val hookSuccessRate: Map<String, Float>,
    val avgHookDuration: Map<String, Long>,
    val hookErrorCounts: Map<String, Long>,
    val methodResolutionStatus: Map<String, Boolean>,
    val parameterMappingStatus: Map<String, Boolean>
)

// Diagnostics implementation
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
                    message = "Test exception: ${e.message}",
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
        
        // Test if each hook point works normally
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
            message = if (failures.isEmpty()) "All hooks working normally" else "Failed hooks: ${failures.joinToString()}",
            duration = duration,
            details = hookTests.keys.toList()
        )
    }
    
    private fun testWakeLockHook(): Boolean {
        // Actual WakeLock hook test logic
        return true // Simplified implementation
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

!!! info "Internal API Design Principles"
    The internal API focuses on performance, reliability, and maintainability, providing complete inter-module communication, rule processing, and diagnostic functionality.

!!! warning "Usage Considerations"
    Internal APIs are primarily used for communication between NoWakeLock internal components and do not guarantee backward compatibility. External developers should use the public ContentProvider API.

!!! tip "Performance Optimization"
    Internal APIs extensively use caching, batch processing, and asynchronous processing mechanisms to ensure good performance in high-frequency call scenarios.