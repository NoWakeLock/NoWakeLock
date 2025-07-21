# Détails d'implémentation des Hooks

Analyse approfondie du mécanisme d'implémentation des Hooks de NoWakeLock, incluant la logique spécifique d'interception des WakeLock, Alarm et Service.

## Principe d'implémentation des Hooks

### Mécanisme des Hooks Xposed
```kotlin
// Processus de Hook de base
fun hookMethod(
    className: String,
    methodName: String,
    parameterTypes: Array<Class<*>>,
    hookCallback: (MethodHookParam) -> Unit
) {
    try {
        val targetClass = XposedHelpers.findClass(className, classLoader)
        XposedHelpers.findAndHookMethod(
            targetClass,
            methodName,
            *parameterTypes,
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    hookCallback(param)
                }
            }
        )
    } catch (e: Exception) {
        XposedBridge.log("Hook failed: ${e.message}")
    }
}
```

### Stratégie de Hook générique
```kotlin
abstract class BaseHook {
    
    protected abstract val targetClass: String
    protected abstract val targetMethods: List<HookTarget>
    
    fun hook(lpparam: LoadPackageParam) {
        targetMethods.forEach { target ->
            hookWithFallback(lpparam, target)
        }
    }
    
    private fun hookWithFallback(lpparam: LoadPackageParam, target: HookTarget) {
        target.strategies.forEach { strategy ->
            try {
                performHook(lpparam, target.methodName, strategy)
                HookLogger.logSuccess(target.methodName, targetClass)
                return // Sortir après succès
            } catch (e: Exception) {
                HookLogger.logAttemptFailed(target.methodName, strategy, e)
            }
        }
        HookLogger.logAllFailed(target.methodName, targetClass)
    }
    
    protected abstract fun performHook(
        lpparam: LoadPackageParam,
        methodName: String,
        strategy: HookStrategy
    )
}

data class HookTarget(
    val methodName: String,
    val strategies: List<HookStrategy>
)

data class HookStrategy(
    val parameterTypes: Array<Class<*>>,
    val parameterIndices: IntArray,
    val description: String
)
```

## Implémentation des Hooks WakeLock

### Points de Hook principaux
```kotlin
object WakelockHook : BaseHook() {
    
    override val targetClass = "com.android.server.power.PowerManagerService"
    
    override val targetMethods = listOf(
        HookTarget(
            methodName = "acquireWakeLockInternal",
            strategies = listOf(
                // Stratégie Android 13+
                HookStrategy(
                    parameterTypes = arrayOf(
                        IBinder::class.java,           // lock
                        String::class.java,            // tag
                        Int::class.javaPrimitiveType,  // displayId
                        Int::class.javaPrimitiveType,  // flags
                        String::class.java,            // packageName
                        WorkSource::class.java,        // ws
                        String::class.java,            // historyTag
                        Int::class.javaPrimitiveType,  // uid
                        Int::class.javaPrimitiveType,  // pid
                        Boolean::class.javaPrimitiveType // unimportantForLogging
                    ),
                    parameterIndices = intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9),
                    description = "Android 13+ API 33"
                ),
                // Stratégie Android 10-12
                HookStrategy(
                    parameterTypes = arrayOf(
                        IBinder::class.java,
                        String::class.java,
                        Int::class.javaPrimitiveType,
                        Int::class.javaPrimitiveType,
                        String::class.java,
                        WorkSource::class.java,
                        String::class.java,
                        Int::class.javaPrimitiveType,
                        Int::class.javaPrimitiveType
                    ),
                    parameterIndices = intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8),
                    description = "Android 10-12 API 29-32"
                ),
                // Stratégie Android 7-9
                HookStrategy(
                    parameterTypes = arrayOf(
                        IBinder::class.java,
                        String::class.java,
                        Int::class.javaPrimitiveType,
                        String::class.java,
                        WorkSource::class.java,
                        String::class.java,
                        Int::class.javaPrimitiveType,
                        Int::class.javaPrimitiveType
                    ),
                    parameterIndices = intArrayOf(0, 1, 2, 3, 4, 5, 6, 7),
                    description = "Android 7-9 API 24-28"
                )
            )
        ),
        HookTarget(
            methodName = "releaseWakeLockInternal",
            strategies = listOf(
                HookStrategy(
                    parameterTypes = arrayOf(
                        IBinder::class.java,
                        Int::class.javaPrimitiveType
                    ),
                    parameterIndices = intArrayOf(0, 1),
                    description = "Méthode universelle de libération"
                )
            )
        )
    )
    
    override fun performHook(
        lpparam: LoadPackageParam,
        methodName: String,
        strategy: HookStrategy
    ) {
        val powerManagerClass = XposedHelpers.findClass(targetClass, lpparam.classLoader)
        
        XposedHelpers.findAndHookMethod(
            powerManagerClass,
            methodName,
            *strategy.parameterTypes,
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    when (methodName) {
                        "acquireWakeLockInternal" -> handleAcquireWakeLock(param, strategy)
                        "releaseWakeLockInternal" -> handleReleaseWakeLock(param, strategy)
                    }
                }
            }
        )
    }
}
```

### Analyse des paramètres WakeLock
```kotlin
object WakelockParamResolver {
    
    private val cache = ConcurrentHashMap<String, ParameterMapping>()
    
    fun extractWakeLockInfo(param: MethodHookParam, strategy: HookStrategy): WakeLockInfo? {
        val key = "${param.method.name}_${strategy.description}"
        val mapping = cache.getOrPut(key) {
            detectParameterMapping(param.method, strategy)
        }
        
        return try {
            val args = param.args
            val indices = strategy.parameterIndices
            
            WakeLockInfo(
                lock = args[indices[mapping.lockIndex]] as IBinder,
                tag = args[indices[mapping.tagIndex]] as String,
                displayId = if (mapping.displayIdIndex >= 0) 
                    args[indices[mapping.displayIdIndex]] as Int else 0,
                flags = args[indices[mapping.flagsIndex]] as Int,
                packageName = args[indices[mapping.packageNameIndex]] as String,
                workSource = args.getOrNull(indices.getOrNull(mapping.workSourceIndex) ?: -1) as? WorkSource,
                historyTag = args.getOrNull(indices.getOrNull(mapping.historyTagIndex) ?: -1) as? String,
                uid = args[indices[mapping.uidIndex]] as Int,
                pid = args[indices[mapping.pidIndex]] as Int,
                unimportantForLogging = if (mapping.unimportantIndex >= 0)
                    args[indices[mapping.unimportantIndex]] as Boolean else false
            )
        } catch (e: Exception) {
            XposedBridge.log("Failed to extract WakeLock info: ${e.message}")
            null
        }
    }
    
    private fun detectParameterMapping(
        method: Method,
        strategy: HookStrategy
    ): ParameterMapping {
        // Inférer mapping paramètres basé sur types et positions
        val paramTypes = method.parameterTypes
        
        return when (strategy.description) {
            "Android 13+ API 33" -> ParameterMapping(
                lockIndex = 0, tagIndex = 1, displayIdIndex = 2, flagsIndex = 3,
                packageNameIndex = 4, workSourceIndex = 5, historyTagIndex = 6,
                uidIndex = 7, pidIndex = 8, unimportantIndex = 9
            )
            "Android 10-12 API 29-32" -> ParameterMapping(
                lockIndex = 0, tagIndex = 1, displayIdIndex = -1, flagsIndex = 2,
                packageNameIndex = 3, workSourceIndex = 4, historyTagIndex = 5,
                uidIndex = 6, pidIndex = 7, unimportantIndex = -1
            )
            "Android 7-9 API 24-28" -> ParameterMapping(
                lockIndex = 0, tagIndex = 1, displayIdIndex = -1, flagsIndex = 2,
                packageNameIndex = 3, workSourceIndex = 4, historyTagIndex = 5,
                uidIndex = 6, pidIndex = 7, unimportantIndex = -1
            )
            else -> throw IllegalArgumentException("Stratégie inconnue : ${strategy.description}")
        }
    }
}

data class ParameterMapping(
    val lockIndex: Int,
    val tagIndex: Int,
    val displayIdIndex: Int,
    val flagsIndex: Int,
    val packageNameIndex: Int,
    val workSourceIndex: Int,
    val historyTagIndex: Int,
    val uidIndex: Int,
    val pidIndex: Int,
    val unimportantIndex: Int
)

data class WakeLockInfo(
    val lock: IBinder,
    val tag: String,
    val displayId: Int,
    val flags: Int,
    val packageName: String,
    val workSource: WorkSource?,
    val historyTag: String?,
    val uid: Int,
    val pid: Int,
    val unimportantForLogging: Boolean
)
```

### Logique de traitement WakeLock
```kotlin
private fun handleAcquireWakeLock(param: MethodHookParam, strategy: HookStrategy) {
    val wakeLockInfo = WakelockParamResolver.extractWakeLockInfo(param, strategy) ?: return
    
    // 1. Filtrage rapide
    if (shouldSkipPackage(wakeLockInfo.packageName)) {
        return
    }
    
    // 2. Correspondance des règles
    val ruleResult = WakelockRuleEngine.evaluate(
        packageName = wakeLockInfo.packageName,
        tag = wakeLockInfo.tag,
        flags = wakeLockInfo.flags,
        userId = UserHandle.getUserId(wakeLockInfo.uid),
        context = WakelockContext(
            pid = wakeLockInfo.pid,
            workSource = wakeLockInfo.workSource,
            historyTag = wakeLockInfo.historyTag
        )
    )
    
    // 3. Exécution de l'action
    when (ruleResult.action) {
        ActionType.BLOCK -> {
            param.result = null // Empêcher l'acquisition
            recordEvent(wakeLockInfo, ruleResult, isBlocked = true)
            XposedBridge.log("Blocked WakeLock: ${wakeLockInfo.tag} from ${wakeLockInfo.packageName}")
        }
        ActionType.LIMIT -> {
            recordEvent(wakeLockInfo, ruleResult, isBlocked = false)
            scheduleWakeLockTimeout(wakeLockInfo.lock, ruleResult.timeout)
        }
        ActionType.ALLOW -> {
            recordEvent(wakeLockInfo, ruleResult, isBlocked = false)
        }
    }
    
    // 4. Mise à jour des statistiques
    WakelockStatistics.recordAttempt(wakeLockInfo.packageName, ruleResult.action)
}

private fun handleReleaseWakeLock(param: MethodHookParam, strategy: HookStrategy) {
    val lock = param.args[0] as IBinder
    val flags = param.args[1] as Int
    
    // Enregistrer événement de libération
    WakelockTracker.recordRelease(lock, System.currentTimeMillis())
    
    // Annuler tâche de timeout
    WakelockTimeoutManager.cancelTimeout(lock)
}
```

## Implémentation des Hooks Alarm

### Stratégie Hook Alarm
```kotlin
object AlarmHook : BaseHook() {
    
    override val targetClass: String
        get() = if (Build.VERSION.SDK_INT >= 31) {
            "com.android.server.alarm.AlarmManagerService"
        } else {
            "com.android.server.AlarmManagerService"
        }
    
    override val targetMethods = listOf(
        HookTarget(
            methodName = "triggerAlarmsLocked",
            strategies = listOf(
                HookStrategy(
                    parameterTypes = arrayOf(ArrayList::class.java),
                    parameterIndices = intArrayOf(0),
                    description = "Méthode universelle de déclenchement"
                )
            )
        ),
        HookTarget(
            methodName = "setImpl",
            strategies = listOf(
                HookStrategy(
                    parameterTypes = arrayOf(
                        Int::class.javaPrimitiveType,    // type
                        Long::class.javaPrimitiveType,   // triggerAtTime
                        Long::class.javaPrimitiveType,   // windowLength
                        Long::class.javaPrimitiveType,   // interval
                        PendingIntent::class.java,       // operation
                        IAlarmListener::class.java,      // directReceiver
                        String::class.java,              // listenerTag
                        WorkSource::class.java,          // workSource
                        AlarmManager.AlarmClockInfo::class.java, // alarmClock
                        Int::class.javaPrimitiveType,    // callingUid
                        String::class.java               // callingPackage
                    ),
                    parameterIndices = intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10),
                    description = "Méthode de configuration alarm"
                )
            )
        )
    )
    
    override fun performHook(
        lpparam: LoadPackageParam,
        methodName: String,
        strategy: HookStrategy
    ) {
        val alarmManagerClass = XposedHelpers.findClass(targetClass, lpparam.classLoader)
        
        XposedHelpers.findAndHookMethod(
            alarmManagerClass,
            methodName,
            *strategy.parameterTypes,
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    when (methodName) {
                        "triggerAlarmsLocked" -> handleAlarmTrigger(param)
                        "setImpl" -> handleAlarmSet(param, strategy)
                    }
                }
            }
        )
    }
}
```

### Logique de traitement Alarm
```kotlin
private fun handleAlarmTrigger(param: MethodHookParam) {
    @Suppress("UNCHECKED_CAST")
    val triggerList = param.args[0] as? ArrayList<Any> ?: return
    
    val originalSize = triggerList.size
    val blockedAlarms = mutableListOf<Any>()
    
    triggerList.forEach { alarm ->
        val alarmInfo = extractAlarmInfo(alarm) ?: return@forEach
        
        // Évaluation des règles
        val ruleResult = AlarmRuleEngine.evaluate(
            packageName = alarmInfo.packageName,
            tag = alarmInfo.tag,
            type = alarmInfo.type,
            userId = alarmInfo.userId,
            triggerTime = alarmInfo.triggerTime,
            interval = alarmInfo.interval
        )
        
        when (ruleResult.action) {
            ActionType.BLOCK -> {
                blockedAlarms.add(alarm)
                recordAlarmEvent(alarmInfo, ruleResult, isBlocked = true)
            }
            ActionType.DELAY -> {
                // Déclenchement différé
                scheduleDelayedAlarm(alarm, ruleResult.delay)
                blockedAlarms.add(alarm)
                recordAlarmEvent(alarmInfo, ruleResult, isBlocked = false, isDelayed = true)
            }
            ActionType.LIMIT -> {
                // Vérification limitation fréquence
                if (AlarmFrequencyLimiter.shouldLimit(alarmInfo, ruleResult.maxPerHour)) {
                    blockedAlarms.add(alarm)
                    recordAlarmEvent(alarmInfo, ruleResult, isBlocked = true, reason = "Limitation fréquence")
                } else {
                    recordAlarmEvent(alarmInfo, ruleResult, isBlocked = false)
                }
            }
            ActionType.ALLOW -> {
                recordAlarmEvent(alarmInfo, ruleResult, isBlocked = false)
            }
        }
    }
    
    // Supprimer Alarms bloquées
    triggerList.removeAll(blockedAlarms)
    
    val blockedCount = originalSize - triggerList.size
    if (blockedCount > 0) {
        XposedBridge.log("Blocked $blockedCount alarms from triggering")
    }
}

private fun extractAlarmInfo(alarm: Any): AlarmInfo? {
    return try {
        // Utiliser réflexion pour extraire informations objet Alarm
        val alarmClass = alarm::class.java
        
        AlarmInfo(
            type = getFieldValue(alarm, "type") as Int,
            triggerTime = getFieldValue(alarm, "triggerAtTime") as Long,
            windowLength = getFieldValue(alarm, "windowLength") as Long,
            interval = getFieldValue(alarm, "repeatInterval") as Long,
            packageName = extractPackageFromPendingIntent(alarm),
            tag = extractTagFromAlarm(alarm),
            userId = extractUserIdFromAlarm(alarm),
            flags = getFieldValue(alarm, "flags") as? Int ?: 0
        )
    } catch (e: Exception) {
        XposedBridge.log("Failed to extract alarm info: ${e.message}")
        null
    }
}
```

## Implémentation des Hooks Service

### Stratégie Hook Service
```kotlin
object ServiceHook : BaseHook() {
    
    override val targetClass = "com.android.server.am.ActiveServices"
    
    override val targetMethods = listOf(
        HookTarget(
            methodName = "startServiceLocked",
            strategies = generateStartServiceStrategies()
        ),
        HookTarget(
            methodName = "bindServiceLocked", 
            strategies = generateBindServiceStrategies()
        ),
        HookTarget(
            methodName = "stopServiceLocked",
            strategies = listOf(
                HookStrategy(
                    parameterTypes = arrayOf(
                        ServiceRecord::class.java,
                        Boolean::class.javaPrimitiveType
                    ),
                    parameterIndices = intArrayOf(0, 1),
                    description = "Méthode arrêt service"
                )
            )
        )
    )
    
    private fun generateStartServiceStrategies(): List<HookStrategy> {
        return listOf(
            // Stratégie Android 13+
            HookStrategy(
                parameterTypes = arrayOf(
                    IApplicationThread::class.java,  // caller
                    Intent::class.java,              // service
                    String::class.java,              // resolvedType
                    Boolean::class.javaPrimitiveType,// requireForeground
                    String::class.java,              // callingPackage
                    String::class.java,              // callingFeatureId
                    Int::class.javaPrimitiveType,    // userId
                    Boolean::class.javaPrimitiveType,// allowBackgroundActivityStarts
                    IBinder::class.java,             // backgroundActivityStartsToken
                    Boolean::class.javaPrimitiveType // packageFrozen
                ),
                parameterIndices = intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9),
                description = "Android 13+ startService"
            ),
            // Stratégie Android 10-12
            HookStrategy(
                parameterTypes = arrayOf(
                    IApplicationThread::class.java,
                    Intent::class.java,
                    String::class.java,
                    Boolean::class.javaPrimitiveType,
                    String::class.java,
                    String::class.java,
                    Int::class.javaPrimitiveType,
                    Boolean::class.javaPrimitiveType,
                    IBinder::class.java
                ),
                parameterIndices = intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8),
                description = "Android 10-12 startService"
            ),
            // Stratégie Android 7-9
            HookStrategy(
                parameterTypes = arrayOf(
                    IApplicationThread::class.java,
                    Intent::class.java,
                    String::class.java,
                    Boolean::class.javaPrimitiveType,
                    String::class.java,
                    Int::class.javaPrimitiveType
                ),
                parameterIndices = intArrayOf(0, 1, 2, 3, 4, 5),
                description = "Android 7-9 startService"
            )
        )
    }
    
    override fun performHook(
        lpparam: LoadPackageParam,
        methodName: String,
        strategy: HookStrategy
    ) {
        val activeServicesClass = XposedHelpers.findClass(targetClass, lpparam.classLoader)
        
        XposedHelpers.findAndHookMethod(
            activeServicesClass,
            methodName,
            *strategy.parameterTypes,
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    when (methodName) {
                        "startServiceLocked" -> handleServiceStart(param, strategy)
                        "bindServiceLocked" -> handleServiceBind(param, strategy)
                        "stopServiceLocked" -> handleServiceStop(param)
                    }
                }
            }
        )
    }
}
```

### Logique de traitement Service
```kotlin
private fun handleServiceStart(param: MethodHookParam, strategy: HookStrategy) {
    val serviceInfo = extractServiceInfo(param, strategy) ?: return
    
    // Filtrage rapide services système critiques
    if (isCriticalSystemService(serviceInfo.serviceName)) {
        return
    }
    
    // Évaluation des règles
    val ruleResult = ServiceRuleEngine.evaluate(
        packageName = serviceInfo.packageName,
        serviceName = serviceInfo.serviceName,
        userId = serviceInfo.userId,
        foreground = serviceInfo.requireForeground,
        intent = serviceInfo.intent
    )
    
    when (ruleResult.action) {
        ActionType.BLOCK -> {
            param.result = null // Empêcher démarrage
            recordServiceEvent(serviceInfo, ruleResult, isBlocked = true)
            XposedBridge.log("Blocked service: ${serviceInfo.serviceName} from ${serviceInfo.packageName}")
        }
        ActionType.DELAY -> {
            param.result = null // Empêcher temporairement
            scheduleDelayedServiceStart(serviceInfo, ruleResult.delay)
            recordServiceEvent(serviceInfo, ruleResult, isBlocked = false, isDelayed = true)
        }
        ActionType.LIMIT -> {
            // Limitation nombre d'instances
            if (ServiceInstanceManager.hasReachedLimit(serviceInfo, ruleResult.maxInstances)) {
                param.result = null
                recordServiceEvent(serviceInfo, ruleResult, isBlocked = true, reason = "Limitation nombre instances")
            } else {
                recordServiceEvent(serviceInfo, ruleResult, isBlocked = false)
                ServiceInstanceManager.trackStart(serviceInfo)
            }
        }
        ActionType.ALLOW -> {
            recordServiceEvent(serviceInfo, ruleResult, isBlocked = false)
            ServiceInstanceManager.trackStart(serviceInfo)
        }
    }
}

private fun extractServiceInfo(param: MethodHookParam, strategy: HookStrategy): ServiceInfo? {
    return try {
        val args = param.args
        val indices = strategy.parameterIndices
        
        val intent = args[indices[1]] as Intent
        val packageName = intent.`package` ?: extractPackageFromComponent(intent.component)
        val serviceName = intent.component?.className ?: intent.action ?: "unknown"
        
        ServiceInfo(
            caller = args[indices[0]] as? IApplicationThread,
            intent = intent,
            resolvedType = args[indices[2]] as String,
            requireForeground = args[indices[3]] as Boolean,
            callingPackage = args[indices[4]] as String,
            callingFeatureId = if (indices.size > 5) args[indices[5]] as? String else null,
            userId = args[indices[if (indices.size > 6) 6 else 5]] as Int,
            packageName = packageName,
            serviceName = serviceName
        )
    } catch (e: Exception) {
        XposedBridge.log("Failed to extract service info: ${e.message}")
        null
    }
}
```

## Intégration du moteur de règles

### Évaluateur de règles générique
```kotlin
abstract class BaseRuleEngine<T : RuleInfo> {
    
    abstract fun loadRules(): List<Rule>
    abstract fun matchRule(info: T, rule: Rule): Boolean
    abstract fun createDefaultResult(info: T): RuleResult
    
    fun evaluate(info: T): RuleResult {
        val rules = loadRules().filter { it.enabled }
        
        // Trier par priorité
        val sortedRules = rules.sortedByDescending { it.priority }
        
        // Chercher règle correspondante
        val matchedRule = sortedRules.firstOrNull { rule ->
            matchRule(info, rule)
        }
        
        return matchedRule?.let { rule ->
            RuleResult(
                action = rule.action,
                timeout = rule.timeout,
                delay = rule.delay,
                maxPerHour = rule.maxPerHour,
                maxInstances = rule.maxInstances,
                ruleId = rule.id,
                ruleName = rule.name,
                matchedBy = rule.matchType
            )
        } ?: createDefaultResult(info)
    }
}

// Moteur de règles WakeLock
object WakelockRuleEngine : BaseRuleEngine<WakelockRuleInfo>() {
    
    override fun loadRules(): List<Rule> {
        return XProvider.getWakelockRules()
    }
    
    override fun matchRule(info: WakelockRuleInfo, rule: Rule): Boolean {
        // Correspondance nom package
        if (rule.packageName != "*" && rule.packageName != info.packageName) {
            return false
        }
        
        // Correspondance utilisateur
        if (rule.userId != -1 && rule.userId != info.userId) {
            return false
        }
        
        // Correspondance étiquette
        return when (rule.matchType) {
            MatchType.EXACT -> rule.target == info.tag
            MatchType.CONTAINS -> info.tag.contains(rule.target, ignoreCase = true)
            MatchType.REGEX -> {
                try {
                    rule.target.toRegex().matches(info.tag)
                } catch (e: Exception) {
                    false
                }
            }
            MatchType.WILDCARD -> {
                val pattern = rule.target
                    .replace("*", ".*")
                    .replace("?", ".")
                try {
                    pattern.toRegex().matches(info.tag)
                } catch (e: Exception) {
                    false
                }
            }
        }
    }
    
    override fun createDefaultResult(info: WakelockRuleInfo): RuleResult {
        return RuleResult(
            action = ActionType.ALLOW,
            ruleId = "default",
            ruleName = "Règle par défaut"
        )
    }
}
```

## Optimisation des performances et surveillance

### Surveillance performance Hook
```kotlin
object HookPerformanceMonitor {
    
    private val methodStats = ConcurrentHashMap<String, MethodStats>()
    
    fun recordMethodCall(methodName: String, duration: Long) {
        methodStats.compute(methodName) { _, stats ->
            stats?.let {
                it.copy(
                    callCount = it.callCount + 1,
                    totalDuration = it.totalDuration + duration,
                    avgDuration = (it.totalDuration + duration) / (it.callCount + 1),
                    maxDuration = maxOf(it.maxDuration, duration),
                    lastCall = System.currentTimeMillis()
                )
            } ?: MethodStats(
                callCount = 1,
                totalDuration = duration,
                avgDuration = duration,
                maxDuration = duration,
                lastCall = System.currentTimeMillis()
            )
        }
    }
    
    fun getPerformanceReport(): String {
        return methodStats.entries.joinToString("\n") { (method, stats) ->
            "$method: calls=${stats.callCount}, avg=${stats.avgDuration}ms, max=${stats.maxDuration}ms"
        }
    }
    
    fun resetStats() {
        methodStats.clear()
    }
}

data class MethodStats(
    val callCount: Long,
    val totalDuration: Long,
    val avgDuration: Long,
    val maxDuration: Long,
    val lastCall: Long
)

// Décorateur surveillance performance
inline fun <T> monitoredHook(methodName: String, block: () -> T): T {
    val startTime = System.nanoTime()
    try {
        return block()
    } finally {
        val duration = (System.nanoTime() - startTime) / 1_000_000
        HookPerformanceMonitor.recordMethodCall(methodName, duration)
        
        if (duration > 10) { // Enregistrer avertissement si > 10ms
            XposedBridge.log("Slow hook execution: $methodName took ${duration}ms")
        }
    }
}
```

### Gestion mémoire
```kotlin
object HookMemoryManager {
    
    private val MAX_CACHE_SIZE = 1000
    private val activeWakeLocks = LRUCache<IBinder, WakeLockTracker>(MAX_CACHE_SIZE)
    private val pendingAlarms = LRUCache<String, AlarmTracker>(MAX_CACHE_SIZE)
    private val runningServices = LRUCache<String, ServiceTracker>(MAX_CACHE_SIZE)
    
    fun trackWakeLock(lock: IBinder, info: WakeLockInfo) {
        activeWakeLocks.put(lock, WakeLockTracker(info, System.currentTimeMillis()))
    }
    
    fun releaseWakeLock(lock: IBinder): WakeLockTracker? {
        return activeWakeLocks.remove(lock)
    }
    
    fun cleanup() {
        val now = System.currentTimeMillis()
        val cutoff = now - TimeUnit.HOURS.toMillis(1)
        
        // Nettoyer trackers expirés
        cleanupExpiredTrackers(activeWakeLocks, cutoff)
        cleanupExpiredTrackers(pendingAlarms, cutoff)
        cleanupExpiredTrackers(runningServices, cutoff)
    }
    
    private fun <T : Tracker> cleanupExpiredTrackers(
        cache: LRUCache<*, T>, 
        cutoff: Long
    ) {
        val snapshot = cache.snapshot()
        snapshot.entries.forEach { (key, tracker) ->
            if (tracker.startTime < cutoff) {
                cache.remove(key)
            }
        }
    }
}

interface Tracker {
    val startTime: Long
}

data class WakeLockTracker(
    val info: WakeLockInfo,
    override val startTime: Long
) : Tracker

data class AlarmTracker(
    val info: AlarmInfo,
    override val startTime: Long,
    val triggerCount: Int = 0
) : Tracker

data class ServiceTracker(
    val info: ServiceInfo,
    override val startTime: Long,
    val instanceCount: Int = 1
) : Tracker
```

!!! info "Principes d'implémentation Hook"
    L'implémentation des Hooks doit considérer la compatibilité multi-versions, l'impact performance et la stabilité système. Prioriser l'utilisation de cache et chemins rapides pour réduire la surcharge système.

!!! warning "Considérations de sécurité"
    Lors du Hook de méthodes système critiques, gérer les exceptions avec prudence pour éviter l'instabilité système due aux erreurs du module. Recommander programmation défensive et logs d'erreur détaillés.