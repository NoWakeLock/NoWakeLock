# Xposed Hooks 实现详解

NoWakeLock 通过 Xposed 框架 Hook 系统服务来拦截和控制 WakeLock、Alarm 和 Service 的行为。

## Hook 架构

### 入口点配置

#### Xposed 模块声明
```
# assets/xposed_init
com.js.nowakelock.xposedhook.XposedModule
```

#### 主模块类
```kotlin
class XposedModule : IXposedHookZygoteInit, IXposedHookLoadPackage {
    
    override fun initZygote(startupParam: StartupParam) {
        // Zygote 初始化时的 Hook
    }
    
    override fun handleLoadPackage(lpparam: LoadPackageParam) {
        when (lpparam.packageName) {
            "android" -> {
                // 系统框架 Hook
                WakelockHook.hook(lpparam)
                AlarmHook.hook(lpparam)
                ServiceHook.hook(lpparam)
            }
            "com.android.providers.settings" -> {
                // 设置提供者 Hook
                SettingsProviderHook.hook(lpparam)
            }
        }
    }
}
```

## WakeLock Hook 实现

### 核心 Hook 点
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

### 参数解析策略
```kotlin
object WakelockParamResolver {
    
    // 缓存参数位置以提高性能
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
        // 基于参数类型和 Android 版本检测参数位置
        return when {
            Build.VERSION.SDK_INT >= 29 -> intArrayOf(0, 1, 2, 3, 4, 5, 6, 7)
            Build.VERSION.SDK_INT >= 28 -> intArrayOf(0, 1, 2, 3, 4, 5, 6, 7) 
            else -> intArrayOf(0, 1, 2, 3, 5, 6, 7, 8)
        }
    }
}
```

### WakeLock 处理逻辑
```kotlin
private fun handleWakeLockAcquire(param: MethodHookParam) {
    val wakeLockParams = WakelockParamResolver.resolveParams(
        param.method, 
        param.args
    ) ?: return
    
    // 1. 记录事件
    val event = InfoEvent(
        instanceId = "${wakeLockParams.lock.hashCode()}_${System.currentTimeMillis()}",
        name = wakeLockParams.tag,
        type = InfoEvent.Type.WakeLock,
        packageName = wakeLockParams.packageName,
        userId = UserHandle.getUserId(wakeLockParams.uid),
        startTime = System.currentTimeMillis(),
        isBlocked = false
    )
    
    // 2. 检查规则
    val action = RuleEngine.checkWakeLockRule(
        packageName = wakeLockParams.packageName,
        tag = wakeLockParams.tag,
        flags = wakeLockParams.flags,
        userId = UserHandle.getUserId(wakeLockParams.uid)
    )
    
    // 3. 执行动作
    when (action.type) {
        ActionType.BLOCK -> {
            event.isBlocked = true
            param.result = null // 阻止获取
            XposedBridge.log("Blocked WakeLock: ${wakeLockParams.tag} from ${wakeLockParams.packageName}")
        }
        ActionType.LIMIT -> {
            // 设置超时释放
            scheduleWakeLockRelease(wakeLockParams.lock, action.timeout)
        }
        ActionType.ALLOW -> {
            // 允许正常执行
        }
    }
    
    // 4. 记录到数据库
    XProvider.insertEvent(event)
}
```

## Alarm Hook 实现

### 多版本兼容
```kotlin
object AlarmHook {
    
    fun hook(lpparam: LoadPackageParam) {
        // Android 12+ 使用新的类路径
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

### Alarm 处理逻辑
```kotlin
private fun handleAlarmTrigger(param: MethodHookParam) {
    @Suppress("UNCHECKED_CAST")
    val triggerList = param.args[0] as? ArrayList<Any> ?: return
    
    val blockedAlarms = mutableListOf<Any>()
    
    triggerList.forEach { alarm ->
        val alarmInfo = extractAlarmInfo(alarm) ?: return@forEach
        
        // 检查规则
        val action = RuleEngine.checkAlarmRule(
            packageName = alarmInfo.packageName,
            tag = alarmInfo.tag,
            type = alarmInfo.type,
            userId = alarmInfo.userId
        )
        
        if (action.type == ActionType.BLOCK) {
            blockedAlarms.add(alarm)
            
            // 记录事件
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
    
    // 移除被阻止的 Alarm
    triggerList.removeAll(blockedAlarms)
}
```

## Service Hook 实现

### 多参数位置策略
```kotlin
object ServiceHook {
    
    fun hook(lpparam: LoadPackageParam) {
        hookStartService(lpparam)
        hookBindService(lpparam)
    }
    
    private fun hookStartService(lpparam: LoadPackageParam) {
        val serviceClass = findClass("com.android.server.am.ActiveServices", lpparam.classLoader)
        
        // 使用多种参数位置策略
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
                // 尝试下一个策略
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

### Service 处理逻辑
```kotlin
private fun handleServiceStart(param: MethodHookParam, strategy: ParameterStrategy) {
    val serviceInfo = extractServiceInfo(param.args, strategy) ?: return
    
    // 检查规则
    val action = RuleEngine.checkServiceRule(
        packageName = serviceInfo.packageName,
        serviceName = serviceInfo.serviceName,
        userId = serviceInfo.userId
    )
    
    when (action.type) {
        ActionType.BLOCK -> {
            param.result = null // 阻止启动
            
            // 记录事件
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
            // Service 限制通常通过延迟启动实现
            scheduleDelayedServiceStart(serviceInfo, action.delay)
        }
        ActionType.ALLOW -> {
            // 允许正常启动
        }
    }
}
```

## 规则引擎

### 规则匹配逻辑
```kotlin
object RuleEngine {
    
    fun checkWakeLockRule(
        packageName: String,
        tag: String,
        flags: Int,
        userId: Int
    ): Action {
        // 1. 精确匹配规则
        getExactRule(packageName, tag)?.let { return it.action }
        
        // 2. 正则表达式规则
        getRegexRules().forEach { rule ->
            if (rule.pattern.matches(tag)) {
                return rule.action
            }
        }
        
        // 3. 应用级别规则
        getAppRule(packageName)?.let { return it.action }
        
        // 4. 默认规则
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

## 跨进程通信

### XProvider 实现
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

## 版本兼容性处理

### API 差异适配
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

## 错误处理和调试

### Hook 失败处理
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

### 调试日志
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

## 性能优化

### 缓存策略
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

### 最小化 Hook 开销
```kotlin
private fun isHookNeeded(packageName: String): Boolean {
    // 跳过不需要监控的系统包
    return when {
        packageName.startsWith("com.android.systemui") -> false
        packageName.startsWith("android.uid.") -> false
        packageName == "android" -> true
        else -> hasActiveRules(packageName)
    }
}
```

!!! warning "Hook 注意事项"
    系统级 Hook 修改需要谨慎处理，错误的实现可能导致系统不稳定。建议在开发环境充分测试。

!!! tip "调试建议"
    使用 `adb logcat | grep Xposed` 查看 Hook 执行日志，便于问题定位和性能分析。