# Interne API

Die interne API von NoWakeLock bietet Kernschnittstellen für Modulkommunikation, Datenverarbeitung und Systemintegration, die hauptsächlich für die tiefe Integration zwischen Xposed-Modul und Anwendung verwendet werden.

## XProvider Interne Schnittstelle

### Kernkommunikationsschnittstelle
```kotlin
// XProvider Kernschnittstelle
interface XProviderInterface {
    
    // Ereignisverwaltung
    fun insertEvent(event: InfoEvent): Boolean
    fun updateEvent(instanceId: String, endTime: Long, duration: Long): Boolean
    fun getActiveEvents(packageName: String? = null): List<InfoEvent>
    
    // Regelverwaltung
    fun getRules(packageName: String? = null, type: RuleType? = null): List<Rule>
    fun getRule(ruleId: String): Rule?
    fun upsertRule(rule: Rule): Boolean
    fun deleteRule(ruleId: String): Boolean
    
    // Konfigurationssynchronisation
    fun syncConfiguration(): Boolean
    fun getConfigurationVersion(): Long
    fun markConfigurationDirty()
    
    // Statusabfrage
    fun getModuleStatus(): ModuleStatus
    fun getHookStatus(): Map<HookType, HookStatus>
    fun getPerformanceMetrics(): PerformanceMetrics
}

// XProvider Implementierungsklasse
class XProvider private constructor() : XProviderInterface {
    
    companion object {
        @Volatile
        private var INSTANCE: XProvider? = null
        
        fun getInstance(): XProvider {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: XProvider().also { INSTANCE = it }
            }
        }
        
        // Statische Methoden für Xposed-Modul-Aufrufe
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

### Kanalauswahlstrategie
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
                // Kritische kleine Daten bevorzugen Systemeigenschaften
                if (ChannelType.SYSTEM_PROPERTIES in availableChannels) {
                    ChannelType.SYSTEM_PROPERTIES
                } else {
                    ChannelType.CONTENT_PROVIDER
                }
            }
            dataSize > 1000 -> {
                // Große Daten verwenden Dateisystem
                ChannelType.FILE_SYSTEM
            }
            reliability == ReliabilityLevel.HIGH -> {
                // Hohe Zuverlässigkeit verwendet ContentProvider
                ChannelType.CONTENT_PROVIDER
            }
            else -> {
                // Standardstrategie
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
        val isRecent = System.currentTimeMillis() - lastUpdate < 300_000 // 5 Minuten
        
        return successRate >= 0.8f && averageLatency < 1000 && isRecent
    }
    
    val latency: Long get() = averageLatency
}
```

## Rule Engine API

### Regelübereinstimmungsmodul
```kotlin
// Regelmodul-Schnittstelle
interface RuleEngine {
    fun evaluateWakeLock(info: WakeLockInfo): RuleResult
    fun evaluateAlarm(info: AlarmInfo): RuleResult
    fun evaluateService(info: ServiceInfo): RuleResult
    
    fun precompileRules(rules: List<Rule>)
    fun invalidateCache()
    fun getMatchingRules(info: ComponentInfo): List<Rule>
}

// Regelmodul-Implementierung
class RuleEngineImpl : RuleEngine {
    
    private val compiledRules = ConcurrentHashMap<String, CompiledRule>()
    private val ruleCache = LRUCache<String, RuleResult>(1000)
    
    override fun evaluateWakeLock(info: WakeLockInfo): RuleResult {
        val cacheKey = "${info.packageName}:${info.tag}:${info.flags}"
        
        // Cache prüfen
        ruleCache.get(cacheKey)?.let { cached ->
            if (!cached.isExpired()) {
                return cached
            }
        }
        
        // Regelübereinstimmung ausführen
        val result = performRuleMatching(info)
        
        // Ergebnis zwischenspeichern
        ruleCache.put(cacheKey, result)
        
        return result
    }
    
    private fun performRuleMatching(info: WakeLockInfo): RuleResult {
        val matchingRules = getMatchingRulesForWakeLock(info)
        
        // Nach Priorität sortieren
        val sortedRules = matchingRules.sortedByDescending { it.priority }
        
        // Erste übereinstimmende Regel finden
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
        // Paketname-Übereinstimmung
        if (!matchesPackage(info.packageName, rule.packageName)) {
            return false
        }
        
        // Benutzer-Übereinstimmung
        if (rule.userId != -1 && rule.userId != info.userId) {
            return false
        }
        
        // Tag-Übereinstimmung
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
            ruleName = "Standard erlauben"
        )
        
        val DEFAULT_BLOCK = RuleResult(
            action = ActionType.BLOCK,
            ruleId = "default_block",
            ruleName = "Standard blockieren"
        )
    }
}
```

## Hook Callback-Schnittstelle

### Hook-Ereignisverarbeiter
```kotlin
// Hook-Ereignisschnittstelle
interface HookEventHandler {
    fun onWakeLockAcquire(info: WakeLockInfo): HookResult
    fun onWakeLockRelease(instanceId: String, duration: Long): HookResult
    fun onAlarmTrigger(info: AlarmInfo): HookResult
    fun onServiceStart(info: ServiceInfo): HookResult
    fun onServiceStop(info: ServiceInfo): HookResult
}

// Hook-Ergebnis
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

// Hook-Ereignisverarbeiter-Implementierung
class HookEventHandlerImpl(
    private val ruleEngine: RuleEngine,
    private val xProvider: XProvider,
    private val statisticsCollector: StatisticsCollector
) : HookEventHandler {
    
    override fun onWakeLockAcquire(info: WakeLockInfo): HookResult {
        // Startzeit aufzeichnen
        val startTime = System.currentTimeMillis()
        
        try {
            // 1. Schnelle Filterung
            if (shouldSkipPackage(info.packageName)) {
                return HookResult.ALLOW
            }
            
            // 2. Regelbewertung
            val ruleResult = ruleEngine.evaluateWakeLock(info)
            
            // 3. Ereignisdatensatz erstellen
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
            
            // 4. Aktion nach Regel ausführen
            val hookResult = when (ruleResult.action) {
                ActionType.ALLOW -> {
                    event.isBlocked = false
                    HookResult.allow("Regel erlaubt: ${ruleResult.ruleName}")
                }
                ActionType.LIMIT -> {
                    event.isBlocked = false
                    HookResult.timeout(
                        timeout = ruleResult.timeout,
                        reason = "Regel limitiert: ${ruleResult.ruleName}, Timeout ${ruleResult.timeout}ms"
                    )
                }
                ActionType.BLOCK -> {
                    event.isBlocked = true
                    HookResult.block("Regel blockiert: ${ruleResult.ruleName}")
                }
            }
            
            // 5. Ereignis aufzeichnen
            xProvider.insertEvent(event)
            
            // 6. Statistiken aktualisieren
            statisticsCollector.recordWakeLockAttempt(info, ruleResult)
            
            return hookResult
            
        } catch (e: Exception) {
            XposedBridge.log("WakeLock hook handler failed: ${e.message}")
            return HookResult.ALLOW // Bei Fehlern standardmäßig erlauben, um Systemprobleme zu vermeiden
        }
    }
    
    override fun onWakeLockRelease(instanceId: String, duration: Long): HookResult {
        try {
            // Ereignis-Endzeit aktualisieren
            xProvider.updateEvent(instanceId, System.currentTimeMillis(), duration)
            
            // Statistiken aktualisieren
            statisticsCollector.recordWakeLockRelease(instanceId, duration)
            
            return HookResult.ALLOW
        } catch (e: Exception) {
            XposedBridge.log("WakeLock release handler failed: ${e.message}")
            return HookResult.ALLOW
        }
    }
    
    private fun shouldSkipPackage(packageName: String): Boolean {
        return when {
            packageName == "android" -> false // Systemkernpaket muss verarbeitet werden
            packageName.startsWith("com.android.systemui") -> true // System-UI überspringen
            packageName.startsWith("android.uid.") -> true // UID-Pakete überspringen
            else -> false
        }
    }
}
```

## Statistiksammler-API

### Statistikdatensammlungsschnittstelle
```kotlin
// Statistiksammler-Schnittstelle
interface StatisticsCollector {
    fun recordWakeLockAttempt(info: WakeLockInfo, result: RuleResult)
    fun recordWakeLockRelease(instanceId: String, duration: Long)
    fun recordAlarmTrigger(info: AlarmInfo, result: RuleResult)
    fun recordServiceStart(info: ServiceInfo, result: RuleResult)
    
    fun getRealtimeStats(packageName: String): RealtimeStats
    fun flushPendingStats()
}

// Statistiksammler-Implementierung
class StatisticsCollectorImpl(
    private val realtimeCounter: RealtimeCounter,
    private val batchProcessor: BatchProcessor
) : StatisticsCollector {
    
    private val pendingStats = ConcurrentLinkedQueue<StatisticEvent>()
    private val flushThreshold = 100
    
    override fun recordWakeLockAttempt(info: WakeLockInfo, result: RuleResult) {
        // Echtzeitanzahl
        realtimeCounter.incrementWakeLockAcquire(info.packageName, info.tag)
        
        // Stapelstatistik
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
        
        // Prüfen, ob Flush erforderlich
        if (pendingStats.size >= flushThreshold) {
            flushPendingStats()
        }
    }
    
    override fun flushPendingStats() {
        val stats = mutableListOf<StatisticEvent>()
        
        // Wartende Statistiken stapelweise abrufen
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

## Cache-Verwaltungs-API

### Mehrstufiges Cache-System
```kotlin
// Cache-Manager-Schnittstelle
interface CacheManager {
    fun <T> get(key: String, type: Class<T>): T?
    fun <T> put(key: String, value: T, ttl: Long = 300_000)
    fun invalidate(key: String)
    fun invalidateByPrefix(prefix: String)
    fun clear()
    fun getStats(): CacheStats
}

// Mehrstufige Cache-Implementierung
class MultiLevelCacheManager : CacheManager {
    
    // L1: Memory-Cache (schnellster Zugriff)
    private val l1Cache = LRUCache<String, CacheEntry>(500)
    
    // L2: Disk-Cache (persistent)
    private val l2Cache = DiskLRUCache.open(
        File("/data/system/nowakelock/cache"),
        1, 1, 10 * 1024 * 1024 // 10MB
    )
    
    // L3: Systemeigenschafts-Cache (prozessübergreifend)
    private val l3Cache = SystemPropertiesCache()
    
    override fun <T> get(key: String, type: Class<T>): T? {
        // L1 Cache-Abfrage
        l1Cache.get(key)?.let { entry ->
            if (!entry.isExpired()) {
                @Suppress("UNCHECKED_CAST")
                return entry.value as? T
            } else {
                l1Cache.remove(key)
            }
        }
        
        // L2 Cache-Abfrage
        l2Cache.get(key)?.let { snapshot ->
            try {
                val json = snapshot.getString(0)
                val entry = Gson().fromJson(json, CacheEntry::class.java)
                if (!entry.isExpired()) {
                    // Zu L1 Cache befördern
                    l1Cache.put(key, entry)
                    @Suppress("UNCHECKED_CAST")
                    return entry.value as? T
                } else {
                    l2Cache.remove(key)
                }
            } catch (e: Exception) {
                // Deserialisierungsfehler ignorieren
            }
        }
        
        // L3 Cache-Abfrage
        return l3Cache.get(key, type)
    }
    
    override fun <T> put(key: String, value: T, ttl: Long) {
        val entry = CacheEntry(
            value = value,
            expireTime = System.currentTimeMillis() + ttl,
            createTime = System.currentTimeMillis()
        )
        
        // In L1 Cache schreiben
        l1Cache.put(key, entry)
        
        // Asynchron in L2 Cache schreiben
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val editor = l2Cache.edit(key)
                editor?.let {
                    val json = Gson().toJson(entry)
                    it.set(0, json)
                    it.commit()
                }
            } catch (e: Exception) {
                // Festplattenschreibfehler ignorieren
            }
        }
        
        // Kleine Daten in L3 Cache schreiben
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
        // L1 Cache Präfix-Bereinigung
        val snapshot = l1Cache.snapshot()
        snapshot.keys.filter { it.startsWith(prefix) }.forEach { key ->
            l1Cache.remove(key)
        }
        
        // L2 und L3 Cache asynchrone Bereinigung
        CoroutineScope(Dispatchers.IO).launch {
            // Hier müssen alle Schlüssel durchlaufen werden, weniger effizient, in tatsächlicher Implementierung kann partitionierter Cache in Betracht gezogen werden
        }
    }
    
    private fun estimateSize(value: Any?): Int {
        return when (value) {
            is String -> value.length
            is Number -> 8
            is Boolean -> 1
            else -> 100 // Schätzwert
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

## Diagnose- und Debug-API

### Interne Diagnoseschnittstelle
```kotlin
// Diagnoseschnittstelle
interface DiagnosticsAPI {
    fun getSystemHealth(): SystemHealth
    fun getHookDiagnostics(): HookDiagnostics
    fun getCacheDiagnostics(): CacheDiagnostics
    fun getPerformanceDiagnostics(): PerformanceDiagnostics
    
    fun runDiagnosticTests(): DiagnosticResults
    fun exportDiagnosticData(): String
}

// Systemgesundheitsstatus
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

// Hook-Diagnoseinformationen
data class HookDiagnostics(
    val hookSuccessRate: Map<String, Float>,
    val avgHookDuration: Map<String, Long>,
    val hookErrorCounts: Map<String, Long>,
    val methodResolutionStatus: Map<String, Boolean>,
    val parameterMappingStatus: Map<String, Boolean>
)

// Diagnose-Implementierung
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
                    message = "Test-Ausnahme: ${e.message}",
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
        
        // Testen, ob verschiedene Hook-Punkte normal funktionieren
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
            message = if (failures.isEmpty()) "Alle Hooks funktionieren normal" else "Fehlgeschlagene Hooks: ${failures.joinToString()}",
            duration = duration,
            details = hookTests.keys.toList()
        )
    }
    
    private fun testWakeLockHook(): Boolean {
        // Tatsächliche WakeLock Hook-Testlogik
        return true // Vereinfachte Implementierung
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

!!! info "Interne API-Designprinzipien"
    Die interne API legt Wert auf Leistung, Zuverlässigkeit und Wartbarkeit und bietet vollständige Funktionen für Modulkommunikation, Regelverarbeitung und Diagnose.

!!! warning "Verwendungshinweise"
    Die interne API wird hauptsächlich für die Kommunikation zwischen internen NoWakeLock-Komponenten verwendet und garantiert keine Rückwärtskompatibilität. Externe Entwickler sollten die öffentliche ContentProvider-API verwenden.

!!! tip "Leistungsoptimierung"
    Die interne API verwendet umfassend Caching, Stapelverarbeitung und asynchrone Verarbeitungsmechanismen, um gute Leistung in hochfrequenten Aufrufszenarien zu gewährleisten.