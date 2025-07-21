# Xposed Hooks 實作詳解

NoWakeLock 透過 Xposed 框架 Hook 系統服務來攔截和控制 WakeLock、Alarm 和 Service 的行為。

## Hook 架構

### 進入點配置

#### Xposed 模組宣告
```
# assets/xposed_init
com.js.nowakelock.xposedhook.XposedModule
```

#### 主模組類別
```kotlin
class XposedModule : IXposedHookZygoteInit, IXposedHookLoadPackage {
    
    override fun initZygote(startupParam: StartupParam) {
        // Zygote 初始化時的 Hook
    }
    
    override fun handleLoadPackage(lpparam: LoadPackageParam) {
        when (lpparam.packageName) {
            "android" -> {
                // 系統框架 Hook
                WakelockHook.hook(lpparam)
                AlarmHook.hook(lpparam)
                ServiceHook.hook(lpparam)
            }
            "com.android.providers.settings" -> {
                // 設定提供者 Hook
                SettingsProviderHook.hook(lpparam)
            }
        }
    }
}
```

## WakeLock Hook 實作

### 核心 Hook 點
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

### 參數解析策略
```kotlin
object WakelockParamResolver {
    
    // 快取參數位置以提高效能
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
        // 基於參數類型和 Android 版本檢測參數位置
        return when {
            Build.VERSION.SDK_INT >= 29 -> intArrayOf(0, 1, 2, 3, 4, 5, 6, 7)
            Build.VERSION.SDK_INT >= 28 -> intArrayOf(0, 1, 2, 3, 4, 5, 6, 7) 
            else -> intArrayOf(0, 1, 2, 3, 5, 6, 7, 8)
        }
    }
}
```

### WakeLock 處理邏輯
```kotlin
private fun handleWakeLockAcquire(param: MethodHookParam) {
    val wakeLockParams = WakelockParamResolver.resolveParams(
        param.method, 
        param.args
    ) ?: return
    
    // 1. 記錄事件
    val event = InfoEvent(
        instanceId = "${wakeLockParams.lock.hashCode()}_${System.currentTimeMillis()}",
        name = wakeLockParams.tag,
        type = InfoEvent.Type.WakeLock,
        packageName = wakeLockParams.packageName,
        userId = UserHandle.getUserId(wakeLockParams.uid),
        startTime = System.currentTimeMillis(),
        isBlocked = false
    )
    
    // 2. 檢查規則
    val action = RuleEngine.checkWakeLockRule(
        packageName = wakeLockParams.packageName,
        tag = wakeLockParams.tag,
        flags = wakeLockParams.flags,
        userId = UserHandle.getUserId(wakeLockParams.uid)
    )
    
    // 3. 執行動作
    when (action.type) {
        ActionType.BLOCK -> {
            event.isBlocked = true
            param.result = null // 阻止取得
            XposedBridge.log("Blocked WakeLock: ${wakeLockParams.tag} from ${wakeLockParams.packageName}")
        }
        ActionType.LIMIT -> {
            // 設定逾時釋放
            scheduleWakeLockRelease(wakeLockParams.lock, action.timeout)
        }
        ActionType.ALLOW -> {
            // 允許正常執行
        }
    }
    
    // 4. 記錄到資料庫
    XProvider.insertEvent(event)
}
```

## Alarm Hook 實作

### 多版本相容
```kotlin
object AlarmHook {
    
    fun hook(lpparam: LoadPackageParam) {
        // Android 12+ 使用新的類別路徑
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

### Alarm 處理邏輯
```kotlin
private fun handleAlarmTrigger(param: MethodHookParam) {
    @Suppress("UNCHECKED_CAST")
    val triggerList = param.args[0] as? ArrayList<Any> ?: return
    
    val blockedAlarms = mutableListOf<Any>()
    
    triggerList.forEach { alarm ->
        val alarmInfo = extractAlarmInfo(alarm) ?: return@forEach
        
        // 檢查規則
        val action = RuleEngine.checkAlarmRule(
            packageName = alarmInfo.packageName,
            tag = alarmInfo.tag,
            type = alarmInfo.type,
            userId = alarmInfo.userId
        )
        
        if (action.type == ActionType.BLOCK) {
            blockedAlarms.add(alarm)
            
            // 記錄事件
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

## Service Hook 實作

### 多參數位置策略
```kotlin
object ServiceHook {
    
    fun hook(lpparam: LoadPackageParam) {
        hookStartService(lpparam)
        hookBindService(lpparam)
    }
    
    private fun hookStartService(lpparam: LoadPackageParam) {
        val serviceClass = findClass("com.android.server.am.ActiveServices", lpparam.classLoader)
        
        // 使用多種參數位置策略
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
                // 嘗試下一個策略
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

### Service 處理邏輯
```kotlin
private fun handleServiceStart(param: MethodHookParam, strategy: ParameterStrategy) {
    val serviceInfo = extractServiceInfo(param.args, strategy) ?: return
    
    // 檢查規則
    val action = RuleEngine.checkServiceRule(
        packageName = serviceInfo.packageName,
        serviceName = serviceInfo.serviceName,
        userId = serviceInfo.userId
    )
    
    when (action.type) {
        ActionType.BLOCK -> {
            param.result = null // 阻止啟動
            
            // 記錄事件
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
            // Service 限制通常透過延遲啟動實作
            scheduleDelayedServiceStart(serviceInfo, action.delay)
        }
        ActionType.ALLOW -> {
            // 允許正常啟動
        }
    }
}
```

## 規則引擎

### 規則比對邏輯
```kotlin
object RuleEngine {
    
    fun checkWakeLockRule(
        packageName: String,
        tag: String,
        flags: Int,
        userId: Int
    ): Action {
        // 1. 精確比對規則
        getExactRule(packageName, tag)?.let { return it.action }
        
        // 2. 正規表示式規則
        getRegexRules().forEach { rule ->
            if (rule.pattern.matches(tag)) {
                return rule.action
            }
        }
        
        // 3. 應用程式層級規則
        getAppRule(packageName)?.let { return it.action }
        
        // 4. 預設規則
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

## 跨程序通訊

### XProvider 實作
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

## 版本相容性處理

### API 差異適配
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

## 錯誤處理和除錯

### Hook 失敗處理
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

### 除錯日誌
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

## 效能最佳化

### 快取策略
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

### 最小化 Hook 開銷
```kotlin
private fun isHookNeeded(packageName: String): Boolean {
    // 跳過不需要監控的系統套件
    return when {
        packageName.startsWith("com.android.systemui") -> false
        packageName.startsWith("android.uid.") -> false
        packageName == "android" -> true
        else -> hasActiveRules(packageName)
    }
}
```

!!! warning "Hook 注意事項"
    系統層級 Hook 修改需要謹慎處理，錯誤的實作可能導致系統不穩定。建議在開發環境充分測試。

!!! tip "除錯建議"
    使用 `adb logcat | grep Xposed` 檢視 Hook 執行日誌，便於問題定位和效能分析。