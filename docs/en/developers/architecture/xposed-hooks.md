# Xposed Hooks Implementation Guide

NoWakeLock uses the Xposed framework to hook system services and intercept and control WakeLock, Alarm, and Service behaviors.

## Hook Architecture

### Entry Point Configuration

#### Xposed Module Declaration
```
# assets/xposed_init
com.js.nowakelock.xposedhook.XposedModule
```

#### Main Module Class
```kotlin
class XposedModule : IXposedHookZygoteInit, IXposedHookLoadPackage {
    
    override fun initZygote(startupParam: StartupParam) {
        // Hook during Zygote initialization
    }
    
    override fun handleLoadPackage(lpparam: LoadPackageParam) {
        when (lpparam.packageName) {
            "android" -> {
                // System framework hooks
                WakelockHook.hook(lpparam)
                AlarmHook.hook(lpparam)
                ServiceHook.hook(lpparam)
            }
            "com.android.providers.settings" -> {
                // Settings provider hooks
                SettingsProviderHook.hook(lpparam)
            }
        }
    }
}
```

## WakeLock Hook Implementation

### Core Hook Points
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

### Parameter Resolution Strategy
```kotlin
object WakelockParamResolver {
    
    // Cache parameter positions for performance
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
        // Detect parameter positions based on parameter types and Android version
        return when {
            Build.VERSION.SDK_INT >= 29 -> intArrayOf(0, 1, 2, 3, 4, 5, 6, 7)
            Build.VERSION.SDK_INT >= 28 -> intArrayOf(0, 1, 2, 3, 4, 5, 6, 7) 
            else -> intArrayOf(0, 1, 2, 3, 5, 6, 7, 8)
        }
    }
}
```

### WakeLock Processing Logic
```kotlin
private fun handleWakeLockAcquire(param: MethodHookParam) {
    val wakeLockParams = WakelockParamResolver.resolveParams(
        param.method, 
        param.args
    ) ?: return
    
    // 1. Record event
    val event = InfoEvent(
        instanceId = "${wakeLockParams.lock.hashCode()}_${System.currentTimeMillis()}",
        name = wakeLockParams.tag,
        type = InfoEvent.Type.WakeLock,
        packageName = wakeLockParams.packageName,
        userId = UserHandle.getUserId(wakeLockParams.uid),
        startTime = System.currentTimeMillis(),
        isBlocked = false
    )
    
    // 2. Check rules
    val action = RuleEngine.checkWakeLockRule(
        packageName = wakeLockParams.packageName,
        tag = wakeLockParams.tag,
        flags = wakeLockParams.flags,
        userId = UserHandle.getUserId(wakeLockParams.uid)
    )
    
    // 3. Execute action
    when (action.type) {
        ActionType.BLOCK -> {
            event.isBlocked = true
            param.result = null // Block acquisition
            XposedBridge.log("Blocked WakeLock: ${wakeLockParams.tag} from ${wakeLockParams.packageName}")
        }
        ActionType.LIMIT -> {
            // Schedule timeout release
            scheduleWakeLockRelease(wakeLockParams.lock, action.timeout)
        }
        ActionType.ALLOW -> {
            // Allow normal execution
        }
    }
    
    // 4. Record to database
    XProvider.insertEvent(event)
}
```

## Alarm Hook Implementation

### Multi-Version Compatibility
```kotlin
object AlarmHook {
    
    fun hook(lpparam: LoadPackageParam) {
        // Android 12+ uses new class paths
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

### Alarm Processing Logic
```kotlin
private fun handleAlarmTrigger(param: MethodHookParam) {
    @Suppress("UNCHECKED_CAST")
    val triggerList = param.args[0] as? ArrayList<Any> ?: return
    
    val blockedAlarms = mutableListOf<Any>()
    
    triggerList.forEach { alarm ->
        val alarmInfo = extractAlarmInfo(alarm) ?: return@forEach
        
        // Check rules
        val action = RuleEngine.checkAlarmRule(
            packageName = alarmInfo.packageName,
            tag = alarmInfo.tag,
            type = alarmInfo.type,
            userId = alarmInfo.userId
        )
        
        if (action.type == ActionType.BLOCK) {
            blockedAlarms.add(alarm)
            
            // Record event
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
    
    // Remove blocked alarms
    triggerList.removeAll(blockedAlarms)
}
```

## Service Hook Implementation

### Multi-Parameter Position Strategy
```kotlin
object ServiceHook {
    
    fun hook(lpparam: LoadPackageParam) {
        hookStartService(lpparam)
        hookBindService(lpparam)
    }
    
    private fun hookStartService(lpparam: LoadPackageParam) {
        val serviceClass = findClass("com.android.server.am.ActiveServices", lpparam.classLoader)
        
        // Use multiple parameter position strategies
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
                // Try next strategy
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

### Service Processing Logic
```kotlin
private fun handleServiceStart(param: MethodHookParam, strategy: ParameterStrategy) {
    val serviceInfo = extractServiceInfo(param.args, strategy) ?: return
    
    // Check rules
    val action = RuleEngine.checkServiceRule(
        packageName = serviceInfo.packageName,
        serviceName = serviceInfo.serviceName,
        userId = serviceInfo.userId
    )
    
    when (action.type) {
        ActionType.BLOCK -> {
            param.result = null // Block start
            
            // Record event
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
            // Service limiting is usually implemented through delayed start
            scheduleDelayedServiceStart(serviceInfo, action.delay)
        }
        ActionType.ALLOW -> {
            // Allow normal start
        }
    }
}
```

## Rule Engine

### Rule Matching Logic
```kotlin
object RuleEngine {
    
    fun checkWakeLockRule(
        packageName: String,
        tag: String,
        flags: Int,
        userId: Int
    ): Action {
        // 1. Exact match rules
        getExactRule(packageName, tag)?.let { return it.action }
        
        // 2. Regular expression rules
        getRegexRules().forEach { rule ->
            if (rule.pattern.matches(tag)) {
                return rule.action
            }
        }
        
        // 3. Application-level rules
        getAppRule(packageName)?.let { return it.action }
        
        // 4. Default rules
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

## Cross-Process Communication

### XProvider Implementation
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

## Version Compatibility Handling

### API Difference Adaptation
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

## Error Handling and Debugging

### Hook Failure Handling
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

### Debug Logging
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

## Performance Optimization

### Caching Strategy
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

### Minimizing Hook Overhead
```kotlin
private fun isHookNeeded(packageName: String): Boolean {
    // Skip system packages that don't need monitoring
    return when {
        packageName.startsWith("com.android.systemui") -> false
        packageName.startsWith("android.uid.") -> false
        packageName == "android" -> true
        else -> hasActiveRules(packageName)
    }
}
```

!!! warning "Hook Considerations"
    System-level Hook modifications require careful handling; incorrect implementations may cause system instability. Thorough testing in development environments is recommended.

!!! tip "Debugging Tips"
    Use `adb logcat | grep Xposed` to view Hook execution logs for problem diagnosis and performance analysis.