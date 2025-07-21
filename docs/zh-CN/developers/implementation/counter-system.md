# 计数器系统

NoWakeLock 的计数器系统负责实时统计当前会话的 WakeLock、Alarm 和 Service 活动数据，为用户提供当前的使用状况和性能指标。系统采用会话级别的统计，设备重启后会重新开始计数。

## 计数器架构

### 系统概览
```mermaid
graph TD
    A[Xposed Hook] --> B[事件收集器]
    B --> C[实时计数器]
    B --> D[会话记录器]
    C --> E[内存缓存]
    D --> F[临时存储]
    E --> G[统计计算器]
    F --> G
    G --> H[当前统计]
    H --> I[Repository层]
    I --> J[UI显示]
    K[设备重启] --> L[清理统计]
    L --> C
    L --> D
```

### 核心组件
```kotlin
// 事件收集器接口
interface EventCollector {
    fun recordEvent(event: SystemEvent)
    fun getCurrentStats(): CurrentSessionStats
    fun resetStats() // 重启后清理
}

// 计数器管理器
class CounterManager(
    private val realtimeCounter: RealtimeCounter,
    private val sessionRecorder: SessionRecorder,
    private val statisticsCalculator: StatisticsCalculator
) : EventCollector {
    
    override fun recordEvent(event: SystemEvent) {
        // 实时计数
        realtimeCounter.increment(event)
        
        // 会话记录（重启后清理）
        sessionRecorder.store(event)
        
        // 触发统计更新
        if (shouldUpdateStatistics(event)) {
            statisticsCalculator.recalculate(event.packageName)
        }
    }
    
    override fun resetStats() {
        // BootResetManager 检测重启后调用
        realtimeCounter.clear()
        sessionRecorder.clear()
    }
}
```

## 实时计数器

### RealtimeCounter 实现
```kotlin
class RealtimeCounter {
    
    // 使用线程安全的数据结构
    private val wakelockCounters = ConcurrentHashMap<String, AtomicCounterData>()
    private val alarmCounters = ConcurrentHashMap<String, AtomicCounterData>()
    private val serviceCounters = ConcurrentHashMap<String, AtomicCounterData>()
    
    // 活跃状态跟踪
    private val activeWakelocks = ConcurrentHashMap<String, WakelockSession>()
    private val activeServices = ConcurrentHashMap<String, ServiceSession>()
    
    fun increment(event: SystemEvent) {
        when (event.type) {
            EventType.WAKELOCK_ACQUIRE -> handleWakelockAcquire(event)
            EventType.WAKELOCK_RELEASE -> handleWakelockRelease(event)
            EventType.ALARM_TRIGGER -> handleAlarmTrigger(event)
            EventType.SERVICE_START -> handleServiceStart(event)
            EventType.SERVICE_STOP -> handleServiceStop(event)
        }
    }
    
    private fun handleWakelockAcquire(event: SystemEvent) {
        val key = "${event.packageName}:${event.name}"
        
        // 增加获取计数
        wakelockCounters.computeIfAbsent(key) { 
            AtomicCounterData() 
        }.acquireCount.incrementAndGet()
        
        // 记录活跃状态
        activeWakelocks[event.instanceId] = WakelockSession(
            packageName = event.packageName,
            tag = event.name,
            startTime = event.timestamp,
            flags = event.flags
        )
        
        // 更新应用级统计
        updateAppLevelStats(event.packageName, EventType.WAKELOCK_ACQUIRE)
    }
    
    private fun handleWakelockRelease(event: SystemEvent) {
        val session = activeWakelocks.remove(event.instanceId) ?: return
        val key = "${session.packageName}:${session.tag}"
        
        val duration = event.timestamp - session.startTime
        
        wakelockCounters[key]?.let { counter ->
            // 更新持有时长
            counter.totalDuration.addAndGet(duration)
            counter.releaseCount.incrementAndGet()
            
            // 更新最大持有时长
            counter.updateMaxDuration(duration)
        }
        
        // 更新应用级统计
        updateAppLevelStats(session.packageName, EventType.WAKELOCK_RELEASE, duration)
    }
    
    private fun handleAlarmTrigger(event: SystemEvent) {
        val key = "${event.packageName}:${event.name}"
        
        alarmCounters.computeIfAbsent(key) {
            AtomicCounterData()
        }.let { counter ->
            counter.triggerCount.incrementAndGet()
            counter.lastTriggerTime.set(event.timestamp)
            
            // 计算触发间隔
            val interval = event.timestamp - counter.previousTriggerTime.getAndSet(event.timestamp)
            if (interval > 0) {
                counter.updateTriggerInterval(interval)
            }
        }
        
        updateAppLevelStats(event.packageName, EventType.ALARM_TRIGGER)
    }
}

// 原子计数器数据
class AtomicCounterData {
    // WakeLock 计数器
    val acquireCount = AtomicLong(0)
    val releaseCount = AtomicLong(0)
    val totalDuration = AtomicLong(0)
    val maxDuration = AtomicLong(0)
    
    // Alarm 计数器
    val triggerCount = AtomicLong(0)
    val lastTriggerTime = AtomicLong(0)
    val previousTriggerTime = AtomicLong(0)
    val minInterval = AtomicLong(Long.MAX_VALUE)
    val maxInterval = AtomicLong(0)
    val totalInterval = AtomicLong(0)
    
    // Service 计数器
    val startCount = AtomicLong(0)
    val stopCount = AtomicLong(0)
    val runningDuration = AtomicLong(0)
    
    // 通用方法
    fun updateMaxDuration(duration: Long) {
        maxDuration.accumulateAndGet(duration) { current, new -> maxOf(current, new) }
    }
    
    fun updateTriggerInterval(interval: Long) {
        minInterval.accumulateAndGet(interval) { current, new -> minOf(current, new) }
        maxInterval.accumulateAndGet(interval) { current, new -> maxOf(current, new) }
        totalInterval.addAndGet(interval)
    }
    
    fun snapshot(): CounterSnapshot {
        return CounterSnapshot(
            acquireCount = acquireCount.get(),
            releaseCount = releaseCount.get(),
            totalDuration = totalDuration.get(),
            maxDuration = maxDuration.get(),
            triggerCount = triggerCount.get(),
            avgInterval = if (triggerCount.get() > 1) {
                totalInterval.get() / (triggerCount.get() - 1)
            } else 0,
            minInterval = if (minInterval.get() == Long.MAX_VALUE) 0 else minInterval.get(),
            maxInterval = maxInterval.get()
        )
    }
}
```

### 会话跟踪
```kotlin
// WakeLock 会话数据
data class WakelockSession(
    val packageName: String,
    val tag: String,
    val startTime: Long,
    val flags: Int,
    val uid: Int = 0,
    val pid: Int = 0
) {
    val duration: Long get() = System.currentTimeMillis() - startTime
    val isLongRunning: Boolean get() = duration > 60_000 // 超过1分钟
}

// Service 会话数据
data class ServiceSession(
    val packageName: String,
    val serviceName: String,
    val startTime: Long,
    val isForeground: Boolean = false,
    val instanceCount: Int = 1
) {
    val duration: Long get() = System.currentTimeMillis() - startTime
    val isLongRunning: Boolean get() = duration > 300_000 // 超过5分钟
}

// 会话管理器
class SessionManager {
    
    private val activeWakelocks = ConcurrentHashMap<String, WakelockSession>()
    private val activeServices = ConcurrentHashMap<String, ServiceSession>()
    private val sessionHistory = LRUCache<String, List<SessionRecord>>(1000)
    
    fun startWakelockSession(instanceId: String, session: WakelockSession) {
        activeWakelocks[instanceId] = session
        scheduleSessionTimeout(instanceId, session.tag, 300_000) // 5分钟超时
    }
    
    fun endWakelockSession(instanceId: String): WakelockSession? {
        val session = activeWakelocks.remove(instanceId)
        session?.let {
            recordSessionHistory(it)
            cancelSessionTimeout(instanceId)
        }
        return session
    }
    
    fun getActiveSessions(): SessionSummary {
        return SessionSummary(
            activeWakelocks = activeWakelocks.values.toList(),
            activeServices = activeServices.values.toList(),
            totalActiveTime = calculateTotalActiveTime(),
            longestRunningSession = findLongestRunningSession()
        )
    }
    
    private fun scheduleSessionTimeout(instanceId: String, tag: String, timeout: Long) {
        // 使用协程延迟处理超时会话
        CoroutineScope(Dispatchers.IO).launch {
            delay(timeout)
            if (activeWakelocks.containsKey(instanceId)) {
                XposedBridge.log("WakeLock timeout: $tag (${timeout}ms)")
                // 强制释放超时的 WakeLock
                forceReleaseWakeLock(instanceId)
            }
        }
    }
}
```

## 会话记录器

### SessionRecorder 实现
```kotlin
class SessionRecorder(
    private val database: InfoDatabase
) {
    
    private val eventBuffer = ConcurrentLinkedQueue<InfoEvent>()
    private val bufferSize = AtomicInteger(0)
    private val maxBufferSize = 1000
    
    fun store(event: SystemEvent) {
        val infoEvent = event.toInfoEvent()
        
        // 添加到缓冲区（仅当前会话）
        eventBuffer.offer(infoEvent)
        
        // 检查是否需要批量写入
        if (bufferSize.incrementAndGet() >= maxBufferSize) {
            flushBuffer()
        }
    }
    
    private fun flushBuffer() {
        val events = mutableListOf<InfoEvent>()
        
        // 批量取出事件
        while (events.size < maxBufferSize && !eventBuffer.isEmpty()) {
            eventBuffer.poll()?.let { events.add(it) }
        }
        
        if (events.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // 注意：InfoDatabase 在初始化时会 clearAllTables()
                    // 重启后数据会被 BootResetManager 清理
                    database.infoEventDao().insertAll(events)
                    bufferSize.addAndGet(-events.size)
                } catch (e: Exception) {
                    XposedBridge.log("Failed to store session events: ${e.message}")
                    // 重新加入队列重试
                    events.forEach { eventBuffer.offer(it) }
                }
            }
        }
    }
    
    fun getCurrentSessionStats(
        packageName: String?,
        type: EventType?
    ): CurrentSessionStats {
        return runBlocking {
            // 只查询当前会话数据（重启后清空）
            val events = database.infoEventDao().getCurrentSessionEvents(
                packageName = packageName,
                type = type?.toInfoEventType()
            )
            
            calculateCurrentSessionStats(events)
        }
    }
    
    fun clear() {
        // 重启后清理数据
        eventBuffer.clear()
        bufferSize.set(0)
    }
}

// 重启检测管理器
class BootResetManager(
    private val database: InfoDatabase,
    private val realtimeCounter: RealtimeCounter
) {
    
    fun checkAndResetAfterBoot(): Boolean {
        val bootTime = SystemClock.elapsedRealtime()
        val lastBootTime = getLastBootTime()
        
        // 检测是否重启
        val isAfterReboot = bootTime < lastBootTime || lastBootTime == 0L
        
        if (isAfterReboot) {
            // 清理所有统计数据
            resetAllStatistics()
            saveLastBootTime(bootTime)
            XposedBridge.log("Device rebooted, statistics reset")
            return true
        }
        
        return false
    }
    
    private fun resetAllStatistics() {
        // 清理数据库表（InfoDatabase 本身在初始化时也会清理）
        database.infoEventDao().clearAll()
        database.infoDao().clearAll()
        
        // 清理内存计数器
        realtimeCounter.clear()
    }
}
                } catch (e: Exception) {
                    XposedBridge.log("Data cleanup failed: ${e.message}")
                }
            }
        }
    }
    
    private suspend fun performAggregation() {
        val cutoffTime = System.currentTimeMillis() - 300_000 // 5分钟前
        
        // 聚合各类型事件
        aggregateWakelockEvents(cutoffTime)
        aggregateAlarmEvents(cutoffTime)
        aggregateServiceEvents(cutoffTime)
        
        // 更新应用级统计
        updateApplicationStatistics()
    }
    
    private suspend fun aggregateWakelockEvents(cutoffTime: Long) {
        val events = database.infoEventDao().getUnprocessedWakelockEvents(cutoffTime)
        
        val aggregatedData = events
            .groupBy { "${it.packageName}:${it.name}" }
            .mapValues { (_, eventList) ->
                WakelockAggregation(
                    packageName = eventList.first().packageName,
                    tag = eventList.first().name,
                    acquireCount = eventList.count { it.endTime == null },
                    releaseCount = eventList.count { it.endTime != null },
                    totalDuration = eventList.sumOf { it.duration },
                    maxDuration = eventList.maxOfOrNull { it.duration } ?: 0,
                    avgDuration = eventList.map { it.duration }.average().toLong(),
                    blockedCount = eventList.count { it.isBlocked },
                    timeWindow = TimeWindow(cutoffTime - 300_000, cutoffTime)
                )
            }
        
        // 存储聚合数据
        database.wakelockAggregationDao().insertAll(aggregatedData.values.toList())
        
        // 标记事件为已处理
        database.infoEventDao().markEventsAsProcessed(events.map { it.instanceId })
    }
}
```

## 统计计算器

### StatisticsCalculator 实现
```kotlin
class StatisticsCalculator(
    private val realtimeCounter: RealtimeCounter,
    private val sessionRecorder: SessionRecorder,
    private val database: InfoDatabase
) {
    
    fun calculateCurrentAppStatistics(packageName: String): CurrentAppStatistics {
        val realtimeStats = realtimeCounter.getAppStats(packageName)
        val sessionStats = sessionRecorder.getCurrentSessionStats(packageName, null)
        
        return CurrentAppStatistics(
            packageName = packageName,
            sessionStartTime = getSessionStartTime(),
            wakelockStats = calculateCurrentWakelockStats(packageName),
            alarmStats = calculateCurrentAlarmStats(packageName),
            serviceStats = calculateCurrentServiceStats(packageName),
            currentMetrics = calculateCurrentMetrics(packageName)
        )
    }
    
    private fun calculateCurrentWakelockStats(packageName: String): CurrentWakelockStats {
        val events = runBlocking {
            // 只查询当前会话数据（重启后已清空）
            database.infoEventDao().getCurrentSessionWakelockEvents(packageName)
        }
        
        val activeEvents = events.filter { it.endTime == null }
        val completedEvents = events.filter { it.endTime != null }
        
        return CurrentWakelockStats(
            totalAcquires = events.size,
            totalReleases = completedEvents.size,
            currentlyActive = activeEvents.size,
            totalDuration = completedEvents.sumOf { it.duration },
            averageDuration = if (completedEvents.isNotEmpty()) {
                completedEvents.map { it.duration }.average().toLong()
            } else 0,
            maxDuration = completedEvents.maxOfOrNull { it.duration } ?: 0,
            blockedCount = events.count { it.isBlocked },
            blockRate = if (events.isNotEmpty()) {
                events.count { it.isBlocked }.toFloat() / events.size
            } else 0f,
            topTags = calculateTopWakelockTags(events),
            recentActivity = calculateRecentActivity(events)
        )
    }
    
    private fun calculateCurrentAlarmStats(packageName: String): CurrentAlarmStats {
        val events = runBlocking {
            database.infoEventDao().getCurrentSessionAlarmEvents(packageName)
        }
        
        val triggerIntervals = calculateTriggerIntervals(events)
        
        return CurrentAlarmStats(
            totalTriggers = events.size,
            blockedTriggers = events.count { it.isBlocked },
            blockRate = if (events.isNotEmpty()) {
                events.count { it.isBlocked }.toFloat() / events.size
            } else 0f,
            averageInterval = triggerIntervals.average().toLong(),
            recentTriggers = events.takeLast(10), // 最近10次触发
            topTags = calculateTopAlarmTags(events)
        )
    }
    
    private fun calculatePerformanceMetrics(packageName: String, timeRange: TimeRange): PerformanceMetrics {
        val events = runBlocking {
            database.infoEventDao().getEventsByPackage(
                packageName = packageName,
                startTime = timeRange.startTime,
                endTime = timeRange.endTime
            )
        }
        
        return PerformanceMetrics(
            totalEvents = events.size,
            eventsPerHour = calculateEventsPerHour(events, timeRange),
            averageCpuUsage = events.map { it.cpuUsage }.average().toFloat(),
            averageMemoryUsage = events.map { it.memoryUsage }.average().toLong(),
            totalBatteryDrain = events.sumOf { it.batteryDrain.toDouble() }.toFloat(),
            efficiencyRating = calculateOverallEfficiency(events),
            resourceIntensity = calculateResourceIntensity(events),
            backgroundActivityRatio = calculateBackgroundRatio(events)
        )
    }
}

// 统计数据类（当前会话）
data class CurrentAppStatistics(
    val packageName: String,
    val sessionStartTime: Long, // 当前会话开始时间（重启后重置）
    val wakelockStats: CurrentWakelockStats,
    val alarmStats: CurrentAlarmStats,
    val serviceStats: CurrentServiceStats,
    val currentMetrics: CurrentMetrics
)

data class CurrentWakelockStats(
    val totalAcquires: Int,
    val totalReleases: Int,
    val currentlyActive: Int,
    val totalDuration: Long,
    val averageDuration: Long,
    val maxDuration: Long,
    val blockedCount: Int,
    val blockRate: Float,
    val topTags: List<TagCount>,
    val recentActivity: List<RecentEvent> // 最近活动，不是历史趋势
)

data class CurrentAlarmStats(
    val totalTriggers: Int,
    val blockedTriggers: Int,
    val blockRate: Float,
    val averageInterval: Long,
    val recentTriggers: List<InfoEvent>, // 最近触发记录
    val topTags: List<TagCount>
)

data class CurrentServiceStats(
    val totalStarts: Int,
    val currentlyRunning: Int,
    val averageRuntime: Long,
    val blockedStarts: Int,
    val blockRate: Float,
    val topServices: List<ServiceCount>
)

data class CurrentMetrics(
    val totalEvents: Int,
    val eventsPerHour: Double, // 当前会话的事件频率
    val sessionDuration: Long, // 会话持续时间
    val averageResponseTime: Long, // 平均响应时间
    val systemLoad: Float // 系统负载指标
)

data class RecentEvent(
    val timestamp: Long,
    val name: String,
    val action: String, // acquire/release/block
    val duration: Long?
)

data class TagCount(
    val tag: String,
    val count: Int,
    val percentage: Float
)

data class ServiceCount(
    val serviceName: String,
    val startCount: Int,
    val runningTime: Long
)
```

## 数据清理机制

### 重启检测和清理
```kotlin
// 实际代码中的 BootResetManager 实现
class BootResetManager(
    private val context: Context,
    private val userPreferencesRepository: UserPreferencesRepository
) {
    
    suspend fun checkAndResetIfNeeded(): Boolean {
        val currentBootTime = SystemClock.elapsedRealtime()
        val lastRecordedTime = userPreferencesRepository.getLastBootTime().first()
        val resetDone = userPreferencesRepository.getResetDone().first()
        
        // 检测是否重启：当前运行时间 < 上次记录的时间
        val isAfterReboot = currentBootTime < lastRecordedTime || lastRecordedTime == 0L
        
        if (isAfterReboot || !resetDone) {
            resetTables() // 清理数据库表
            userPreferencesRepository.setLastBootTime(currentBootTime)
            userPreferencesRepository.setResetDone(true)
            return true
        }
        return false
    }
    
    private suspend fun resetTables() {
        val db = AppDatabase.getInstance(context)
        // 清空统计表和事件表
        db.infoDao().clearAll()
        db.infoEventDao().clearAll()
    }
}

// XProvider 中的自动清理
class XProvider {
    companion object {
        private var db: InfoDatabase = InfoDatabase.getInstance(context).also { 
            it.clearAllTables() // 每次初始化都清空
        }
    }
}
```

### 为什么要清理历史数据

**设计理念**：
- **实时管控不需要历史数据** - WakeLock/Alarm 的拦截决策是即时的
- **统计显示当前会话** - 用户关心的是当前电池消耗情况
- **避免数据积累** - 历史 WakeLock 事件对系统没有价值
- **保持系统清洁** - 重启后重新开始更反映实际使用情况
        val sumX = x.sum()
        val sumY = values.sum()
        val sumXY = x.zip(values) { xi, yi -> xi * yi }.sum()
    ## 性能优化

### 缓存策略
```kotlin
class SessionStatisticsCache {
    
    private val cache = LRUCache<String, CacheEntry>(50) // 减少缓存大小
    private val cacheExpiry = 60_000L // 1分钟过期（短期缓存）
    
    fun getCurrentAppStatistics(packageName: String): CurrentAppStatistics? {
        val key = "current_${packageName}"
        val entry = cache.get(key)
        
        return if (entry != null && !entry.isExpired()) {
            entry.statistics
        } else {
            null
        }
    }
    
    fun putCurrentAppStatistics(packageName: String, statistics: CurrentAppStatistics) {
        val key = "current_${packageName}"
        cache.put(key, CacheEntry(statistics, System.currentTimeMillis() + cacheExpiry))
    }
    
    fun clearOnReboot() {
        // 重启后清理所有缓存
        cache.evictAll()
    }
    
    private data class CacheEntry(
        val statistics: CurrentAppStatistics,
        val expiryTime: Long
    ) {
        fun isExpired(): Boolean = System.currentTimeMillis() > expiryTime
    }
}

// 轻量级计算管理器
class LightweightStatsManager(
    private val realtimeCounter: RealtimeCounter,
    private val cache: SessionStatisticsCache
) {
    
    // 只计算必要的实时数据
    fun getQuickStats(packageName: String): QuickStats {
        val cached = cache.getCurrentAppStatistics(packageName)
        if (cached != null) return cached.toQuickStats()
        
        // 轻量级计算
        val realtime = realtimeCounter.getAppStats(packageName)
        return QuickStats(
            activeWakelocks = realtime.activeWakelocks,
            totalEvents = realtime.totalEvents,
            blockedEvents = realtime.blockedEvents,
            sessionUptime = System.currentTimeMillis() - getSessionStartTime()
        )
    }
}

data class QuickStats(
    val activeWakelocks: Int,
    val totalEvents: Int,
    val blockedEvents: Int,
    val sessionUptime: Long
)
```

## 性能优化

### 缓存策略
```kotlin
class StatisticsCache {
    
    private val cache = LRUCache<String, CacheEntry>(100)
    private val cacheExpiry = 300_000L // 5分钟过期
    
    fun getAppStatistics(packageName: String, timeRange: TimeRange): AppStatistics? {
        val key = "${packageName}_${timeRange.hashCode()}"
        val entry = cache.get(key)
        
        return if (entry != null && !entry.isExpired()) {
            entry.statistics
        } else {
            null
        }
    }
    
    fun putAppStatistics(packageName: String, timeRange: TimeRange, statistics: AppStatistics) {
        val key = "${packageName}_${timeRange.hashCode()}"
        cache.put(key, CacheEntry(statistics, System.currentTimeMillis() + cacheExpiry))
    }
    
    private data class CacheEntry(
        val statistics: AppStatistics,
        val expiryTime: Long
    ) {
        fun isExpired(): Boolean = System.currentTimeMillis() > expiryTime
    }
}

// 预计算管理器
class PrecomputeManager(
    private val statisticsCalculator: StatisticsCalculator,
    private val cache: StatisticsCache
) {
    
    private val precomputeScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    fun schedulePrecompute(packageNames: List<String>) {
        precomputeScope.launch {
            packageNames.forEach { packageName ->
                try {
                    precomputeAppStatistics(packageName)
                } catch (e: Exception) {
                    XposedBridge.log("Precompute failed for $packageName: ${e.message}")
                }
            }
        }
    }
    
    private suspend fun precomputeAppStatistics(packageName: String) {
        val timeRanges = listOf(
            TimeRange.last24Hours(),
            TimeRange.last7Days(),
            TimeRange.last30Days()
        )
        
        timeRanges.forEach { timeRange ->
            val statistics = statisticsCalculator.calculateAppStatistics(packageName, timeRange)
            cache.putAppStatistics(packageName, timeRange, statistics)
        }
    }
}
```

!!! info "计数器设计原则"
    NoWakeLock 的计数器系统采用会话级别设计，为 Xposed 模块的实时管控功能而优化。重点在于当前状态的实时显示，而非长期数据分析。

!!! tip "为什么不保存历史数据"
    - **实时管控不需要历史数据**：WakeLock/Alarm 的拦截决策基于当前事件和规则
    - **统计只需当前会话**：用户关心的是当前电池消耗和系统状态
    - **防止数据积累**：历史 WakeLock 事件对系统管理没有实际价值
    - **保持系统轻量**：重启清理数据确保模块高效运行

!!! warning "数据生命周期"
    - **实时计数器**：内存中保存，进程重启清空
    - **会话记录器**：数据库临时存储，设备重启清空
    - **统计数据**：只存在于当前会话，不跨重启保存