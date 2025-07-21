# 内部 API

NoWakeLock 内部 API 提供了模块间通信、数据处理和系统集成的核心接口，主要用于 Xposed 模块与应用之间的深度集成。

## XProvider 内部接口

### 核心通信接口
```kotlin
// XProvider 核心接口
interface XProviderInterface {
    
    // 事件管理
    fun insertEvent(event: InfoEvent): Boolean
    fun updateEvent(instanceId: String, endTime: Long, duration: Long): Boolean
    fun getActiveEvents(packageName: String? = null): List<InfoEvent>
    
    // 规则管理
    fun getRules(packageName: String? = null, type: RuleType? = null): List<Rule>
    fun getRule(ruleId: String): Rule?
    fun upsertRule(rule: Rule): Boolean
    fun deleteRule(ruleId: String): Boolean
    
    // 配置同步
    fun syncConfiguration(): Boolean
    fun getConfigurationVersion(): Long
    fun markConfigurationDirty()
    
    // 状态查询
    fun getModuleStatus(): ModuleStatus
    fun getHookStatus(): Map<HookType, HookStatus>
    fun getPerformanceMetrics(): PerformanceMetrics
}

// XProvider 实现类
class XProvider private constructor() : XProviderInterface {
    
    companion object {
        @Volatile
        private var INSTANCE: XProvider? = null
        
        fun getInstance(): XProvider {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: XProvider().also { INSTANCE = it }
            }
        }
        
        // 静态方法用于 Xposed 模块调用
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

### 通道选择策略
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
                // 紧急小数据优先使用系统属性
                if (ChannelType.SYSTEM_PROPERTIES in availableChannels) {
                    ChannelType.SYSTEM_PROPERTIES
                } else {
                    ChannelType.CONTENT_PROVIDER
                }
            }
            dataSize > 1000 -> {
                // 大数据使用文件系统
                ChannelType.FILE_SYSTEM
            }
            reliability == ReliabilityLevel.HIGH -> {
                // 高可靠性使用 ContentProvider
                ChannelType.CONTENT_PROVIDER
            }
            else -> {
                // 默认策略
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
        val isRecent = System.currentTimeMillis() - lastUpdate < 300_000 // 5分钟
        
        return successRate >= 0.8f && averageLatency < 1000 && isRecent
    }
    
    val latency: Long get() = averageLatency
}
```

## Rule Engine API

### 规则匹配引擎
```kotlin
// 规则引擎接口
interface RuleEngine {
    fun evaluateWakeLock(info: WakeLockInfo): RuleResult
    fun evaluateAlarm(info: AlarmInfo): RuleResult
    fun evaluateService(info: ServiceInfo): RuleResult
    
    fun precompileRules(rules: List<Rule>)
    fun invalidateCache()
    fun getMatchingRules(info: ComponentInfo): List<Rule>
}

// 规则引擎实现
class RuleEngineImpl : RuleEngine {
    
    private val compiledRules = ConcurrentHashMap<String, CompiledRule>()
    private val ruleCache = LRUCache<String, RuleResult>(1000)
    
    override fun evaluateWakeLock(info: WakeLockInfo): RuleResult {
        val cacheKey = "${info.packageName}:${info.tag}:${info.flags}"
        
        // 检查缓存
        ruleCache.get(cacheKey)?.let { cached ->
            if (!cached.isExpired()) {
                return cached
            }
        }
        
        // 执行规则匹配
        val result = performRuleMatching(info)
        
        // 缓存结果
        ruleCache.put(cacheKey, result)
        
        return result
    }
    
    private fun performRuleMatching(info: WakeLockInfo): RuleResult {
        val matchingRules = getMatchingRulesForWakeLock(info)
        
        // 按优先级排序
        val sortedRules = matchingRules.sortedByDescending { it.priority }
        
        // 找到第一个匹配的规则
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
        // 包名匹配
        if (!matchesPackage(info.packageName, rule.packageName)) {
            return false
        }
        
        // 用户匹配
        if (rule.userId != -1 && rule.userId != info.userId) {
            return false
        }
        
        // 标签匹配
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
            ruleName = "默认允许"
        )
        
        val DEFAULT_BLOCK = RuleResult(
            action = ActionType.BLOCK,
            ruleId = "default_block",
            ruleName = "默认阻止"
        )
    }
}
```

## Hook 回调接口

### Hook 事件处理器
```kotlin
// Hook 事件接口
interface HookEventHandler {
    fun onWakeLockAcquire(info: WakeLockInfo): HookResult
    fun onWakeLockRelease(instanceId: String, duration: Long): HookResult
    fun onAlarmTrigger(info: AlarmInfo): HookResult
    fun onServiceStart(info: ServiceInfo): HookResult
    fun onServiceStop(info: ServiceInfo): HookResult
}

// Hook 结果
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

// Hook 事件处理器实现
class HookEventHandlerImpl(
    private val ruleEngine: RuleEngine,
    private val xProvider: XProvider,
    private val statisticsCollector: StatisticsCollector
) : HookEventHandler {
    
    override fun onWakeLockAcquire(info: WakeLockInfo): HookResult {
        // 记录开始时间
        val startTime = System.currentTimeMillis()
        
        try {
            // 1. 快速过滤
            if (shouldSkipPackage(info.packageName)) {
                return HookResult.ALLOW
            }
            
            // 2. 规则评估
            val ruleResult = ruleEngine.evaluateWakeLock(info)
            
            // 3. 创建事件记录
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
            
            // 4. 根据规则执行动作
            val hookResult = when (ruleResult.action) {
                ActionType.ALLOW -> {
                    event.isBlocked = false
                    HookResult.allow("规则允许: ${ruleResult.ruleName}")
                }
                ActionType.LIMIT -> {
                    event.isBlocked = false
                    HookResult.timeout(
                        timeout = ruleResult.timeout,
                        reason = "规则限制: ${ruleResult.ruleName}, 超时${ruleResult.timeout}ms"
                    )
                }
                ActionType.BLOCK -> {
                    event.isBlocked = true
                    HookResult.block("规则阻止: ${ruleResult.ruleName}")
                }
            }
            
            // 5. 记录事件
            xProvider.insertEvent(event)
            
            // 6. 更新统计
            statisticsCollector.recordWakeLockAttempt(info, ruleResult)
            
            return hookResult
            
        } catch (e: Exception) {
            XposedBridge.log("WakeLock hook handler failed: ${e.message}")
            return HookResult.ALLOW // 失败时默认允许，避免系统问题
        }
    }
    
    override fun onWakeLockRelease(instanceId: String, duration: Long): HookResult {
        try {
            // 更新事件结束时间
            xProvider.updateEvent(instanceId, System.currentTimeMillis(), duration)
            
            // 更新统计
            statisticsCollector.recordWakeLockRelease(instanceId, duration)
            
            return HookResult.ALLOW
        } catch (e: Exception) {
            XposedBridge.log("WakeLock release handler failed: ${e.message}")
            return HookResult.ALLOW
        }
    }
    
    private fun shouldSkipPackage(packageName: String): Boolean {
        return when {
            packageName == "android" -> false // 系统核心包需要处理
            packageName.startsWith("com.android.systemui") -> true // 跳过系统UI
            packageName.startsWith("android.uid.") -> true // 跳过UID包
            else -> false
        }
    }
}
```

## 统计收集器 API

### 统计数据收集接口
```kotlin
// 统计收集器接口
interface StatisticsCollector {
    fun recordWakeLockAttempt(info: WakeLockInfo, result: RuleResult)
    fun recordWakeLockRelease(instanceId: String, duration: Long)
    fun recordAlarmTrigger(info: AlarmInfo, result: RuleResult)
    fun recordServiceStart(info: ServiceInfo, result: RuleResult)
    
    fun getRealtimeStats(packageName: String): RealtimeStats
    fun flushPendingStats()
}

// 统计收集器实现
class StatisticsCollectorImpl(
    private val realtimeCounter: RealtimeCounter,
    private val batchProcessor: BatchProcessor
) : StatisticsCollector {
    
    private val pendingStats = ConcurrentLinkedQueue<StatisticEvent>()
    private val flushThreshold = 100
    
    override fun recordWakeLockAttempt(info: WakeLockInfo, result: RuleResult) {
        // 实时计数
        realtimeCounter.incrementWakeLockAcquire(info.packageName, info.tag)
        
        // 批量统计
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
        
        // 检查是否需要刷新
        if (pendingStats.size >= flushThreshold) {
            flushPendingStats()
        }
    }
    
    override fun flushPendingStats() {
        val stats = mutableListOf<StatisticEvent>()
        
        // 批量取出待处理统计
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

## 缓存管理 API

### 多级缓存系统
```kotlin
// 缓存管理器接口
interface CacheManager {
    fun <T> get(key: String, type: Class<T>): T?
    fun <T> put(key: String, value: T, ttl: Long = 300_000)
    fun invalidate(key: String)
    fun invalidateByPrefix(prefix: String)
    fun clear()
    fun getStats(): CacheStats
}

// 多级缓存实现
class MultiLevelCacheManager : CacheManager {
    
    // L1: 内存缓存 (最快访问)
    private val l1Cache = LRUCache<String, CacheEntry>(500)
    
    // L2: 磁盘缓存 (持久化)
    private val l2Cache = DiskLRUCache.open(
        File("/data/system/nowakelock/cache"),
        1, 1, 10 * 1024 * 1024 // 10MB
    )
    
    // L3: 系统属性缓存 (跨进程)
    private val l3Cache = SystemPropertiesCache()
    
    override fun <T> get(key: String, type: Class<T>): T? {
        // L1 缓存查询
        l1Cache.get(key)?.let { entry ->
            if (!entry.isExpired()) {
                @Suppress("UNCHECKED_CAST")
                return entry.value as? T
            } else {
                l1Cache.remove(key)
            }
        }
        
        // L2 缓存查询
        l2Cache.get(key)?.let { snapshot ->
            try {
                val json = snapshot.getString(0)
                val entry = Gson().fromJson(json, CacheEntry::class.java)
                if (!entry.isExpired()) {
                    // 提升到 L1 缓存
                    l1Cache.put(key, entry)
                    @Suppress("UNCHECKED_CAST")
                    return entry.value as? T
                } else {
                    l2Cache.remove(key)
                }
            } catch (e: Exception) {
                // 忽略反序列化错误
            }
        }
        
        // L3 缓存查询
        return l3Cache.get(key, type)
    }
    
    override fun <T> put(key: String, value: T, ttl: Long) {
        val entry = CacheEntry(
            value = value,
            expireTime = System.currentTimeMillis() + ttl,
            createTime = System.currentTimeMillis()
        )
        
        // 写入 L1 缓存
        l1Cache.put(key, entry)
        
        // 异步写入 L2 缓存
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val editor = l2Cache.edit(key)
                editor?.let {
                    val json = Gson().toJson(entry)
                    it.set(0, json)
                    it.commit()
                }
            } catch (e: Exception) {
                // 忽略磁盘写入错误
            }
        }
        
        // 小数据写入 L3 缓存
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
        // L1 缓存前缀清理
        val snapshot = l1Cache.snapshot()
        snapshot.keys.filter { it.startsWith(prefix) }.forEach { key ->
            l1Cache.remove(key)
        }
        
        // L2 和 L3 缓存异步清理
        CoroutineScope(Dispatchers.IO).launch {
            // 这里需要遍历所有键，效率较低，实际实现中可以考虑使用分区缓存
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

## 诊断和调试 API

### 内部诊断接口
```kotlin
// 诊断接口
interface DiagnosticsAPI {
    fun getSystemHealth(): SystemHealth
    fun getHookDiagnostics(): HookDiagnostics
    fun getCacheDiagnostics(): CacheDiagnostics
    fun getPerformanceDiagnostics(): PerformanceDiagnostics
    
    fun runDiagnosticTests(): DiagnosticResults
    fun exportDiagnosticData(): String
}

// 系统健康状态
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

// Hook 诊断信息
data class HookDiagnostics(
    val hookSuccessRate: Map<String, Float>,
    val avgHookDuration: Map<String, Long>,
    val hookErrorCounts: Map<String, Long>,
    val methodResolutionStatus: Map<String, Boolean>,
    val parameterMappingStatus: Map<String, Boolean>
)

// 诊断实现
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
                    message = "测试异常: ${e.message}",
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
        
        // 测试各个 Hook 点是否正常工作
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
            message = if (failures.isEmpty()) "所有 Hook 正常工作" else "失败的 Hook: ${failures.joinToString()}",
            duration = duration,
            details = hookTests.keys.toList()
        )
    }
    
    private fun testWakeLockHook(): Boolean {
        // 实际的 WakeLock Hook 测试逻辑
        return true // 简化实现
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

!!! info "内部 API 设计原则"
    内部 API 注重性能、可靠性和可维护性，提供了完整的模块间通信、规则处理和诊断功能。

!!! warning "使用注意事项"
    内部 API 主要用于 NoWakeLock 内部组件间通信，不保证向后兼容性。外部开发者应使用公开的 ContentProvider API。

!!! tip "性能优化"
    内部 API 广泛使用缓存、批处理和异步处理机制，确保在高频调用场景下保持良好性能。