# Implémentation détaillée des Hooks Xposed

NoWakeLock utilise le framework Xposed pour intercepter et contrôler le comportement des WakeLock, Alarm et Service via des Hooks des services système.

## Architecture des Hooks

### Configuration du point d'entrée

#### Déclaration du module Xposed
```
# assets/xposed_init
com.js.nowakelock.xposedhook.XposedModule
```

#### Classe principale du module
```kotlin
class XposedModule : IXposedHookZygoteInit, IXposedHookLoadPackage {
    
    override fun initZygote(startupParam: StartupParam) {
        // Hook lors de l'initialisation Zygote
    }
    
    override fun handleLoadPackage(lpparam: LoadPackageParam) {
        when (lpparam.packageName) {
            "android" -> {
                // Hook du framework système
                WakelockHook.hook(lpparam)
                AlarmHook.hook(lpparam)
                ServiceHook.hook(lpparam)
            }
            "com.android.providers.settings" -> {
                // Hook du fournisseur de paramètres
                SettingsProviderHook.hook(lpparam)
            }
        }
    }
}
```

## Implémentation des Hooks WakeLock

### Points de Hook principaux
```kotlin
object WakelockHook {
    
    fun hook(lpparam: LoadPackageParam) {
        try {
            hookAcquireWakeLock(lpparam)
            hookReleaseWakeLock(lpparam)
        } catch (t: Throwable) {
            XposedBridge.log("WakelockHook failed: ${t.message}")
        }
    }
    
    private fun hookAcquireWakeLock(lpparam: LoadPackageParam) {
        findAndHookMethod(
            "com.android.server.power.PowerManagerService",
            lpparam.classLoader,
            "acquireWakeLockInternal"
        ) { param ->
            handleWakeLockAcquire(param)
        }
    }
}
```

### Stratégie de résolution des paramètres
```kotlin
object WakelockParamResolver {
    
    // Cache des positions de paramètres pour améliorer les performances
    private val paramPositionCache = mutableMapOf<String, IntArray>()
    
    fun resolveParams(method: Method, args: Array<Any?>): WakeLockParams? {
        val key = "${method.declaringClass.name}.${method.name}"
        val positions = paramPositionCache.getOrPut(key) {
            detectParameterPositions(method, args)
        }
        
        return try {
            WakeLockParams(
                lock = args[positions[0]] as IBinder,
                flags = args[positions[1]] as Int,
                tag = args[positions[2]] as String,
                packageName = args[positions[3]] as String,
                workSource = args.getOrNull(positions[4]) as? WorkSource,
                historyTag = args.getOrNull(positions[5]) as? String,
                uid = args[positions[6]] as Int,
                pid = args[positions[7]] as Int
            )
        } catch (e: Exception) {
            null
        }
    }
    
    private fun detectParameterPositions(method: Method, args: Array<Any?>): IntArray {
        // Détection des positions de paramètres basée sur les types et la version Android
        return when {
            Build.VERSION.SDK_INT >= 29 -> intArrayOf(0, 1, 2, 3, 4, 5, 6, 7)
            Build.VERSION.SDK_INT >= 28 -> intArrayOf(0, 1, 2, 3, 4, 5, 6, 7) 
            else -> intArrayOf(0, 1, 2, 3, 5, 6, 7, 8)
        }
    }
}
```

### Logique de traitement WakeLock
```kotlin
private fun handleWakeLockAcquire(param: MethodHookParam) {
    val wakeLockParams = WakelockParamResolver.resolveParams(
        param.method, 
        param.args
    ) ?: return
    
    // 1. Enregistrer l'événement
    val event = InfoEvent(
        instanceId = "${wakeLockParams.lock.hashCode()}_${System.currentTimeMillis()}",
        name = wakeLockParams.tag,
        type = InfoEvent.Type.WakeLock,
        packageName = wakeLockParams.packageName,
        userId = UserHandle.getUserId(wakeLockParams.uid),
        startTime = System.currentTimeMillis(),
        isBlocked = false
    )
    
    // 2. Vérifier les règles
    val action = RuleEngine.checkWakeLockRule(
        packageName = wakeLockParams.packageName,
        tag = wakeLockParams.tag,
        flags = wakeLockParams.flags,
        userId = UserHandle.getUserId(wakeLockParams.uid)
    )
    
    // 3. Exécuter l'action
    when (action.type) {
        ActionType.BLOCK -> {
            event.isBlocked = true
            param.result = null // Empêcher l'acquisition
            XposedBridge.log("Blocked WakeLock: ${wakeLockParams.tag} from ${wakeLockParams.packageName}")
        }
        ActionType.LIMIT -> {
            // Programmer la libération avec timeout
            scheduleWakeLockRelease(wakeLockParams.lock, action.timeout)
        }
        ActionType.ALLOW -> {
            // Autoriser l'exécution normale
        }
    }
    
    // 4. Enregistrer en base de données
    XProvider.insertEvent(event)
}
```

## Implémentation des Hooks Alarm

### Compatibilité multi-versions
```kotlin
object AlarmHook {
    
    fun hook(lpparam: LoadPackageParam) {
        // Android 12+ utilise un nouveau chemin de classe
        if (Build.VERSION.SDK_INT >= 31) {
            hookAlarmTrigger31Plus(lpparam)
        } else {
            hookAlarmTriggerLegacy(lpparam)
        }
    }
    
    private fun hookAlarmTrigger31Plus(lpparam: LoadPackageParam) {
        findAndHookMethod(
            "com.android.server.alarm.AlarmManagerService",
            lpparam.classLoader,
            "triggerAlarmsLocked",
            ArrayList::class.java
        ) { param ->
            handleAlarmTrigger(param)
        }
    }
    
    private fun hookAlarmTriggerLegacy(lpparam: LoadPackageParam) {
        findAndHookMethod(
            "com.android.server.AlarmManagerService",
            lpparam.classLoader,
            "triggerAlarmsLocked",
            ArrayList::class.java
        ) { param ->
            handleAlarmTrigger(param)
        }
    }
}
```

### Logique de traitement des Alarms
```kotlin
private fun handleAlarmTrigger(param: MethodHookParam) {
    @Suppress("UNCHECKED_CAST")
    val triggerList = param.args[0] as? ArrayList<Any> ?: return
    
    val blockedAlarms = mutableListOf<Any>()
    
    triggerList.forEach { alarm ->
        val alarmInfo = extractAlarmInfo(alarm) ?: return@forEach
        
        // Vérifier les règles
        val action = RuleEngine.checkAlarmRule(
            packageName = alarmInfo.packageName,
            tag = alarmInfo.tag,
            type = alarmInfo.type,
            userId = alarmInfo.userId
        )
        
        if (action.type == ActionType.BLOCK) {
            blockedAlarms.add(alarm)
            
            // Enregistrer l'événement
            val event = InfoEvent(
                instanceId = "${alarm.hashCode()}_${System.currentTimeMillis()}",
                name = alarmInfo.tag,
                type = InfoEvent.Type.Alarm,
                packageName = alarmInfo.packageName,
                userId = alarmInfo.userId,
                startTime = System.currentTimeMillis(),
                isBlocked = true
            )
            XProvider.insertEvent(event)
        }
    }
    
    // Supprimer les Alarms bloquées
    triggerList.removeAll(blockedAlarms)
}
```

## Implémentation des Hooks Service

### Stratégie multi-positions de paramètres
```kotlin
object ServiceHook {
    
    fun hook(lpparam: LoadPackageParam) {
        hookStartService(lpparam)
        hookBindService(lpparam)
    }
    
    private fun hookStartService(lpparam: LoadPackageParam) {
        val serviceClass = findClass("com.android.server.am.ActiveServices", lpparam.classLoader)
        
        // Utiliser plusieurs stratégies de positions de paramètres
        val strategies = listOf(
            ParameterStrategy(intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8)),
            ParameterStrategy(intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9)),
            ParameterStrategy(intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9))
        )
        
        strategies.forEach { strategy ->
            try {
                XposedHelpers.findAndHookMethod(
                    serviceClass,
                    "startServiceLocked",
                    *strategy.parameterTypes
                ) { param ->
                    handleServiceStart(param, strategy)
                }
            } catch (e: NoSuchMethodError) {
                // Essayer la stratégie suivante
            }
        }
    }
}

data class ParameterStrategy(
    val positions: IntArray,
    val parameterTypes: Array<Class<*>> = arrayOf(
        IApplicationThread::class.java,
        Intent::class.java,
        String::class.java,
        Int::class.javaPrimitiveType,
        Int::class.javaPrimitiveType,
        Boolean::class.javaPrimitiveType,
        String::class.java,
        Int::class.javaPrimitiveType
    )
)
```

### Logique de traitement des Services
```kotlin
private fun handleServiceStart(param: MethodHookParam, strategy: ParameterStrategy) {
    val serviceInfo = extractServiceInfo(param.args, strategy) ?: return
    
    // Vérifier les règles
    val action = RuleEngine.checkServiceRule(
        packageName = serviceInfo.packageName,
        serviceName = serviceInfo.serviceName,
        userId = serviceInfo.userId
    )
    
    when (action.type) {
        ActionType.BLOCK -> {
            param.result = null // Empêcher le démarrage
            
            // Enregistrer l'événement
            val event = InfoEvent(
                instanceId = "${serviceInfo.intent.hashCode()}_${System.currentTimeMillis()}",
                name = serviceInfo.serviceName,
                type = InfoEvent.Type.Service,
                packageName = serviceInfo.packageName,
                userId = serviceInfo.userId,
                startTime = System.currentTimeMillis(),
                isBlocked = true
            )
            XProvider.insertEvent(event)
        }
        ActionType.LIMIT -> {
            // La limitation de Service se fait généralement par démarrage différé
            scheduleDelayedServiceStart(serviceInfo, action.delay)
        }
        ActionType.ALLOW -> {
            // Autoriser le démarrage normal
        }
    }
}
```

## Moteur de règles

### Logique de correspondance des règles
```kotlin
object RuleEngine {
    
    fun checkWakeLockRule(
        packageName: String,
        tag: String,
        flags: Int,
        userId: Int
    ): Action {
        // 1. Règles de correspondance exacte
        getExactRule(packageName, tag)?.let { return it.action }
        
        // 2. Règles d'expression régulière
        getRegexRules().forEach { rule ->
            if (rule.pattern.matches(tag)) {
                return rule.action
            }
        }
        
        // 3. Règles au niveau application
        getAppRule(packageName)?.let { return it.action }
        
        // 4. Règle par défaut
        return getDefaultAction()
    }
    
    private fun getExactRule(packageName: String, tag: String): Rule? {
        return XProvider.getRule(
            packageName = packageName,
            target = tag,
            type = RuleType.EXACT
        )
    }
    
    private fun getRegexRules(): List<Rule> {
        return XProvider.getRules(type = RuleType.REGEX)
    }
}
```

## Communication inter-processus

### Implémentation XProvider
```kotlin
class XProvider private constructor() {
    
    companion object {
        private const val AUTHORITY = "com.js.nowakelock.xprovider"
        
        fun insertEvent(event: InfoEvent) {
            try {
                val contentValues = ContentValues().apply {
                    put("instanceId", event.instanceId)
                    put("name", event.name)
                    put("type", event.type.ordinal)
                    put("packageName", event.packageName)
                    put("userId", event.userId)
                    put("startTime", event.startTime)
                    put("isBlocked", event.isBlocked)
                }
                
                SystemProperties.set("sys.nowakelock.insert", contentValues.toString())
            } catch (e: Exception) {
                XposedBridge.log("XProvider insert failed: ${e.message}")
            }
        }
        
        fun getRule(packageName: String, target: String, type: RuleType): Rule? {
            return try {
                val query = "packageName=$packageName&target=$target&type=${type.ordinal}"
                val result = SystemProperties.get("sys.nowakelock.rule.$query", "")
                if (result.isNotEmpty()) {
                    parseRule(result)
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }
}
```

## Gestion de la compatibilité des versions

### Adaptation des différences d'API
```kotlin
object CompatibilityHandler {
    
    fun getParameterIndices(methodName: String): IntArray {
        return when {
            Build.VERSION.SDK_INT >= 33 -> {
                // Android 13+
                when (methodName) {
                    "acquireWakeLockInternal" -> intArrayOf(0, 1, 2, 3, 4, 5, 6, 7)
                    "startServiceLocked" -> intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
                    else -> intArrayOf()
                }
            }
            Build.VERSION.SDK_INT >= 29 -> {
                // Android 10-12
                when (methodName) {
                    "acquireWakeLockInternal" -> intArrayOf(0, 1, 2, 3, 4, 5, 6, 7)
                    "startServiceLocked" -> intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9)
                    else -> intArrayOf()
                }
            }
            else -> {
                // Android 7-9
                when (methodName) {
                    "acquireWakeLockInternal" -> intArrayOf(0, 1, 2, 3, 5, 6, 7, 8)
                    "startServiceLocked" -> intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8)
                    else -> intArrayOf()
                }
            }
        }
    }
    
    fun getClassName(baseClassName: String): String {
        return when {
            Build.VERSION.SDK_INT >= 31 -> {
                when (baseClassName) {
                    "AlarmManagerService" -> "com.android.server.alarm.AlarmManagerService"
                    else -> "com.android.server.$baseClassName"
                }
            }
            else -> "com.android.server.$baseClassName"
        }
    }
}
```

## Gestion d'erreurs et débogage

### Gestion des échecs de Hook
```kotlin
private fun safeHook(hookAction: () -> Unit) {
    try {
        hookAction()
    } catch (e: NoSuchMethodError) {
        XposedBridge.log("Method not found: ${e.message}")
    } catch (e: ClassNotFoundException) {
        XposedBridge.log("Class not found: ${e.message}")
    } catch (e: Exception) {
        XposedBridge.log("Hook failed: ${e.message}")
    }
}
```

### Logs de débogage
```kotlin
object HookLogger {
    
    private const val DEBUG = BuildConfig.DEBUG
    
    fun logHookSuccess(methodName: String, className: String) {
        if (DEBUG) {
            XposedBridge.log("Hook success: $className.$methodName")
        }
    }
    
    fun logHookFailure(methodName: String, className: String, error: Throwable) {
        XposedBridge.log("Hook failed: $className.$methodName - ${error.message}")
    }
    
    fun logRuleExecution(action: Action, target: String) {
        if (DEBUG) {
            XposedBridge.log("Rule executed: ${action.type} for $target")
        }
    }
}
```

## Optimisation des performances

### Stratégie de cache
```kotlin
object HookCache {
    
    private val methodCache = ConcurrentHashMap<String, Method>()
    private val parameterCache = ConcurrentHashMap<String, IntArray>()
    
    fun getCachedMethod(className: String, methodName: String): Method? {
        return methodCache["$className.$methodName"]
    }
    
    fun cacheMethod(className: String, methodName: String, method: Method) {
        methodCache["$className.$methodName"] = method
    }
    
    fun getCachedParameters(methodSignature: String): IntArray? {
        return parameterCache[methodSignature]
    }
    
    fun cacheParameters(methodSignature: String, parameters: IntArray) {
        parameterCache[methodSignature] = parameters
    }
}
```

### Minimisation de la surcharge des Hooks
```kotlin
private fun isHookNeeded(packageName: String): Boolean {
    // Ignorer les packages système qui n'ont pas besoin de surveillance
    return when {
        packageName.startsWith("com.android.systemui") -> false
        packageName.startsWith("android.uid.") -> false
        packageName == "android" -> true
        else -> hasActiveRules(packageName)
    }
}
```

!!! warning "Précautions pour les Hooks"
    Les modifications de Hook au niveau système doivent être gérées avec prudence, car une implémentation incorrecte peut causer une instabilité du système. Il est recommandé de tester thoroughly dans un environnement de développement.

!!! tip "Conseils de débogage"
    Utilisez `adb logcat | grep Xposed` pour visualiser les logs d'exécution des Hooks, facilitant la localisation des problèmes et l'analyse des performances.