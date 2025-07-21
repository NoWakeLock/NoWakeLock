# API Interne

L'API interne de NoWakeLock fournit les interfaces principales pour la communication inter-modules, le traitement des données et l'intégration système, principalement utilisées pour l'intégration approfondie entre le module Xposed et l'application.

## Interface interne XProvider

### Interface de communication principale
```kotlin
// Interface principale XProvider
interface XProviderInterface {
    
    // Gestion des événements
    fun insertEvent(event: InfoEvent): Boolean
    fun updateEvent(instanceId: String, endTime: Long, duration: Long): Boolean
    fun getActiveEvents(packageName: String? = null): List<InfoEvent>
    
    // Gestion des règles
    fun getRules(packageName: String? = null, type: RuleType? = null): List<Rule>
    fun getRule(ruleId: String): Rule?
    fun upsertRule(rule: Rule): Boolean
    fun deleteRule(ruleId: String): Boolean
    
    // Synchronisation de configuration
    fun syncConfiguration(): Boolean
    fun getConfigurationVersion(): Long
    fun markConfigurationDirty()
    
    // Requête d'état
    fun getModuleStatus(): ModuleStatus
    fun getHookStatus(): Map<HookType, HookStatus>
    fun getPerformanceMetrics(): PerformanceMetrics
}

// Classe d'implémentation XProvider
class XProvider private constructor() : XProviderInterface {
    
    companion object {
        @Volatile
        private var INSTANCE: XProvider? = null
        
        fun getInstance(): XProvider {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: XProvider().also { INSTANCE = it }
            }
        }
        
        // Méthodes statiques pour l'appel du module Xposed
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
            XposedBridge.log("Échec d'insertion d'événement : ${e.message}")
            false
        }
    }
}
```

### Stratégie de sélection de canal
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
                // Données urgentes de petite taille prioritaires avec propriétés système
                if (ChannelType.SYSTEM_PROPERTIES in availableChannels) {
                    ChannelType.SYSTEM_PROPERTIES
                } else {
                    ChannelType.CONTENT_PROVIDER
                }
            }
            dataSize > 1000 -> {
                // Grandes données utilisant le système de fichiers
                ChannelType.FILE_SYSTEM
            }
            reliability == ReliabilityLevel.HIGH -> {
                // Haute fiabilité utilisant ContentProvider
                ChannelType.CONTENT_PROVIDER
            }
            else -> {
                // Stratégie par défaut
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

## API Rule Engine

### Moteur de correspondance de règles
```kotlin
// Interface du moteur de règles
interface RuleEngine {
    fun evaluateWakeLock(info: WakeLockInfo): RuleResult
    fun evaluateAlarm(info: AlarmInfo): RuleResult
    fun evaluateService(info: ServiceInfo): RuleResult
    
    fun precompileRules(rules: List<Rule>)
    fun invalidateCache()
    fun getMatchingRules(info: ComponentInfo): List<Rule>
}

// Implémentation du moteur de règles
class RuleEngineImpl : RuleEngine {
    
    private val compiledRules = ConcurrentHashMap<String, CompiledRule>()
    private val ruleCache = LRUCache<String, RuleResult>(1000)
    
    override fun evaluateWakeLock(info: WakeLockInfo): RuleResult {
        val cacheKey = "${info.packageName}:${info.tag}:${info.flags}"
        
        // Vérifier le cache
        ruleCache.get(cacheKey)?.let { cached ->
            if (!cached.isExpired()) {
                return cached
            }
        }
        
        // Exécuter la correspondance de règles
        val result = performRuleMatching(info)
        
        // Mettre en cache le résultat
        ruleCache.put(cacheKey, result)
        
        return result
    }
    
    private fun performRuleMatching(info: WakeLockInfo): RuleResult {
        val matchingRules = getMatchingRulesForWakeLock(info)
        
        // Trier par priorité
        val sortedRules = matchingRules.sortedByDescending { it.priority }
        
        // Trouver la première règle correspondante
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
        // Correspondance du nom de package
        if (!matchesPackage(info.packageName, rule.packageName)) {
            return false
        }
        
        // Correspondance d'utilisateur
        if (rule.userId != -1 && rule.userId != info.userId) {
            return false
        }
        
        // Correspondance d'étiquette
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
            ruleName = "Autorisation par défaut"
        )
        
        val DEFAULT_BLOCK = RuleResult(
            action = ActionType.BLOCK,
            ruleId = "default_block",
            ruleName = "Blocage par défaut"
        )
    }
}
```

## Interface de callback Hook

### Gestionnaire d'événements Hook
```kotlin
// Interface d'événements Hook
interface HookEventHandler {
    fun onWakeLockAcquire(info: WakeLockInfo): HookResult
    fun onWakeLockRelease(instanceId: String, duration: Long): HookResult
    fun onAlarmTrigger(info: AlarmInfo): HookResult
    fun onServiceStart(info: ServiceInfo): HookResult
    fun onServiceStop(info: ServiceInfo): HookResult
}

// Résultat Hook
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

// Implémentation du gestionnaire d'événements Hook
class HookEventHandlerImpl(
    private val ruleEngine: RuleEngine,
    private val xProvider: XProvider,
    private val statisticsCollector: StatisticsCollector
) : HookEventHandler {
    
    override fun onWakeLockAcquire(info: WakeLockInfo): HookResult {
        // Enregistrer l'heure de début
        val startTime = System.currentTimeMillis()
        
        try {
            // 1. Filtrage rapide
            if (shouldSkipPackage(info.packageName)) {
                return HookResult.ALLOW
            }
            
            // 2. Évaluation des règles
            val ruleResult = ruleEngine.evaluateWakeLock(info)
            
            // 3. Créer un enregistrement d'événement
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
            
            // 4. Exécuter l'action selon la règle
            val hookResult = when (ruleResult.action) {
                ActionType.ALLOW -> {
                    event.isBlocked = false
                    HookResult.allow("Règle autorisée : ${ruleResult.ruleName}")
                }
                ActionType.LIMIT -> {
                    event.isBlocked = false
                    HookResult.timeout(
                        timeout = ruleResult.timeout,
                        reason = "Règle limitée : ${ruleResult.ruleName}, timeout ${ruleResult.timeout}ms"
                    )
                }
                ActionType.BLOCK -> {
                    event.isBlocked = true
                    HookResult.block("Règle bloquée : ${ruleResult.ruleName}")
                }
            }
            
            // 5. Enregistrer l'événement
            xProvider.insertEvent(event)
            
            // 6. Mettre à jour les statistiques
            statisticsCollector.recordWakeLockAttempt(info, ruleResult)
            
            return hookResult
            
        } catch (e: Exception) {
            XposedBridge.log("Échec du gestionnaire WakeLock hook : ${e.message}")
            return HookResult.ALLOW // Autoriser par défaut en cas d'échec pour éviter les problèmes système
        }
    }
    
    override fun onWakeLockRelease(instanceId: String, duration: Long): HookResult {
        try {
            // Mettre à jour l'heure de fin de l'événement
            xProvider.updateEvent(instanceId, System.currentTimeMillis(), duration)
            
            // Mettre à jour les statistiques
            statisticsCollector.recordWakeLockRelease(instanceId, duration)
            
            return HookResult.ALLOW
        } catch (e: Exception) {
            XposedBridge.log("Échec du gestionnaire de libération WakeLock : ${e.message}")
            return HookResult.ALLOW
        }
    }
    
    private fun shouldSkipPackage(packageName: String): Boolean {
        return when {
            packageName == "android" -> false // Le package système principal doit être traité
            packageName.startsWith("com.android.systemui") -> true // Ignorer l'interface système
            packageName.startsWith("android.uid.") -> true // Ignorer les packages UID
            else -> false
        }
    }
}
```

## API Collecteur de statistiques

### Interface de collecte de données statistiques
```kotlin
// Interface du collecteur de statistiques
interface StatisticsCollector {
    fun recordWakeLockAttempt(info: WakeLockInfo, result: RuleResult)
    fun recordWakeLockRelease(instanceId: String, duration: Long)
    fun recordAlarmTrigger(info: AlarmInfo, result: RuleResult)
    fun recordServiceStart(info: ServiceInfo, result: RuleResult)
    
    fun getRealtimeStats(packageName: String): RealtimeStats
    fun flushPendingStats()
}

// Implémentation du collecteur de statistiques
class StatisticsCollectorImpl(
    private val realtimeCounter: RealtimeCounter,
    private val batchProcessor: BatchProcessor
) : StatisticsCollector {
    
    private val pendingStats = ConcurrentLinkedQueue<StatisticEvent>()
    private val flushThreshold = 100
    
    override fun recordWakeLockAttempt(info: WakeLockInfo, result: RuleResult) {
        // Comptage en temps réel
        realtimeCounter.incrementWakeLockAcquire(info.packageName, info.tag)
        
        // Statistiques en lot
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
        
        // Vérifier s'il faut rafraîchir
        if (pendingStats.size >= flushThreshold) {
            flushPendingStats()
        }
    }
    
    override fun flushPendingStats() {
        val stats = mutableListOf<StatisticEvent>()
        
        // Extraire en lot les statistiques en attente
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

## API Gestionnaire de cache

### Système de cache à plusieurs niveaux
```kotlin
// Interface du gestionnaire de cache
interface CacheManager {
    fun <T> get(key: String, type: Class<T>): T?
    fun <T> put(key: String, value: T, ttl: Long = 300_000)
    fun invalidate(key: String)
    fun invalidateByPrefix(prefix: String)
    fun clear()
    fun getStats(): CacheStats
}

// Implémentation du cache à plusieurs niveaux
class MultiLevelCacheManager : CacheManager {
    
    // L1 : Cache mémoire (accès le plus rapide)
    private val l1Cache = LRUCache<String, CacheEntry>(500)
    
    // L2 : Cache disque (persistant)
    private val l2Cache = DiskLRUCache.open(
        File("/data/system/nowakelock/cache"),
        1, 1, 10 * 1024 * 1024 // 10MB
    )
    
    // L3 : Cache propriétés système (inter-processus)
    private val l3Cache = SystemPropertiesCache()
    
    override fun <T> get(key: String, type: Class<T>): T? {
        // Requête cache L1
        l1Cache.get(key)?.let { entry ->
            if (!entry.isExpired()) {
                @Suppress("UNCHECKED_CAST")
                return entry.value as? T
            } else {
                l1Cache.remove(key)
            }
        }
        
        // Requête cache L2
        l2Cache.get(key)?.let { snapshot ->
            try {
                val json = snapshot.getString(0)
                val entry = Gson().fromJson(json, CacheEntry::class.java)
                if (!entry.isExpired()) {
                    // Promouvoir au cache L1
                    l1Cache.put(key, entry)
                    @Suppress("UNCHECKED_CAST")
                    return entry.value as? T
                } else {
                    l2Cache.remove(key)
                }
            } catch (e: Exception) {
                // Ignorer les erreurs de désérialisation
            }
        }
        
        // Requête cache L3
        return l3Cache.get(key, type)
    }
    
    override fun <T> put(key: String, value: T, ttl: Long) {
        val entry = CacheEntry(
            value = value,
            expireTime = System.currentTimeMillis() + ttl,
            createTime = System.currentTimeMillis()
        )
        
        // Écrire dans le cache L1
        l1Cache.put(key, entry)
        
        // Écrire de manière asynchrone dans le cache L2
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val editor = l2Cache.edit(key)
                editor?.let {
                    val json = Gson().toJson(entry)
                    it.set(0, json)
                    it.commit()
                }
            } catch (e: Exception) {
                // Ignorer les erreurs d'écriture disque
            }
        }
        
        // Écrire les petites données dans le cache L3
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
        // Nettoyage de préfixe cache L1
        val snapshot = l1Cache.snapshot()
        snapshot.keys.filter { it.startsWith(prefix) }.forEach { key ->
            l1Cache.remove(key)
        }
        
        // Nettoyage asynchrone des caches L2 et L3
        CoroutineScope(Dispatchers.IO).launch {
            // Ici il faut parcourir toutes les clés, efficacité plus faible, dans l'implémentation réelle on peut considérer utiliser un cache partitionné
        }
    }
    
    private fun estimateSize(value: Any?): Int {
        return when (value) {
            is String -> value.length
            is Number -> 8
            is Boolean -> 1
            else -> 100 // Valeur estimée
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

## API Diagnostic et débogage

### Interface de diagnostic interne
```kotlin
// Interface de diagnostic
interface DiagnosticsAPI {
    fun getSystemHealth(): SystemHealth
    fun getHookDiagnostics(): HookDiagnostics
    fun getCacheDiagnostics(): CacheDiagnostics
    fun getPerformanceDiagnostics(): PerformanceDiagnostics
    
    fun runDiagnosticTests(): DiagnosticResults
    fun exportDiagnosticData(): String
}

// État de santé du système
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

// Informations de diagnostic Hook
data class HookDiagnostics(
    val hookSuccessRate: Map<String, Float>,
    val avgHookDuration: Map<String, Long>,
    val hookErrorCounts: Map<String, Long>,
    val methodResolutionStatus: Map<String, Boolean>,
    val parameterMappingStatus: Map<String, Boolean>
)

// Implémentation de diagnostic
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
                    message = "Exception de test : ${e.message}",
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
        
        // Tester si chaque point Hook fonctionne normalement
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
            message = if (failures.isEmpty()) "Tous les Hooks fonctionnent normalement" else "Hooks en échec : ${failures.joinToString()}",
            duration = duration,
            details = hookTests.keys.toList()
        )
    }
    
    private fun testWakeLockHook(): Boolean {
        // Logique de test réelle du Hook WakeLock
        return true // Implémentation simplifiée
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

!!! info "Principes de conception de l'API interne"
    L'API interne met l'accent sur les performances, la fiabilité et la maintenabilité, fournissant une communication inter-modules complète, un traitement de règles et des fonctions de diagnostic.

!!! warning "Notes d'utilisation"
    L'API interne est principalement utilisée pour la communication entre les composants internes de NoWakeLock, ne garantit pas la compatibilité ascendante. Les développeurs externes doivent utiliser l'API ContentProvider publique.

!!! tip "Optimisation des performances"
    L'API interne utilise largement des mécanismes de cache, de traitement par lots et de traitement asynchrone pour assurer de bonnes performances dans des scénarios d'appels haute fréquence.