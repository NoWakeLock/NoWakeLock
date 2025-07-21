# 数据库设计

NoWakeLock 采用双数据库架构设计，分离业务配置数据和运行时事件数据，确保数据管理的高效性和可维护性。

## 数据库架构概览

### 双数据库设计理念
```
┌─────────────────────────────┐    ┌─────────────────────────────┐
│         AppDatabase         │    │        InfoDatabase         │
│     (业务配置数据)           │    │      (运行时事件数据)        │
├─────────────────────────────┤    ├─────────────────────────────┤
│ • 应用信息 (AppInfo)         │    │ • 事件记录 (InfoEvent)       │
│ • WakeLock规则              │    │ • 实时统计                  │
│ • Alarm规则                 │    │ • 历史记录                  │
│ • Service规则               │    │ • 性能数据                  │
│ • 用户配置                  │    │                            │
└─────────────────────────────┘    └─────────────────────────────┘
            │                                    │
            └──────────────┬─────────────────────┘
                          │
                   ┌─────────────┐
                   │ Repository  │
                   │   统一接口   │
                   └─────────────┘
```

## AppDatabase 设计

### 数据库声明
```kotlin
@Database(
    entities = [
        AppInfo::class,
        WakelockRule::class,
        AlarmRule::class,
        ServiceRule::class,
        UserPreferences::class
    ],
    version = 13,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 11, to = 12),
        AutoMigration(from = 12, to = 13, spec = Migration12to13::class)
    ]
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun appInfoDao(): AppInfoDao
    abstract fun wakelockRuleDao(): WakelockRuleDao
    abstract fun alarmRuleDao(): AlarmRuleDao
    abstract fun serviceRuleDao(): ServiceRuleDao
    abstract fun userPreferencesDao(): UserPreferencesDao
    
    companion object {
        private const val DATABASE_NAME = "app_database"
        
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                ).addMigrations(
                    MIGRATION_10_11,
                    MIGRATION_11_12
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
```

### 核心实体设计

#### AppInfo - 应用信息表
```kotlin
@Entity(
    tableName = "appInfo",
    primaryKeys = ["packageName", "userId"],
    indices = [
        Index(value = ["packageName"], name = "idx_app_package"),
        Index(value = ["userId"], name = "idx_app_user"),
        Index(value = ["system"], name = "idx_app_system"),
        Index(value = ["enabled"], name = "idx_app_enabled"),
        Index(value = ["packageName", "userId"], name = "idx_app_composite", unique = true)
    ]
)
data class AppInfo(
    var packageName: String = "",
    var uid: Int = 0,
    var label: String = "",
    var icon: Int = 0,
    var system: Boolean = false,
    var enabled: Boolean = false,
    var persistent: Boolean = false,
    var processName: String = "",
    var userId: Int = 0,
    var firstInstallTime: Long = 0,
    var lastUpdateTime: Long = 0,
    var targetSdkVersion: Int = 0,
    var versionCode: Long = 0,
    var versionName: String = "",
    var dataDir: String = "",
    var publicSourceDir: String = "",
    var sharedUserId: String? = null
) {
    // 计算属性
    val isSystemApp: Boolean get() = system
    val isUserApp: Boolean get() = !system
    val displayName: String get() = label.ifEmpty { packageName }
}
```

#### WakelockRule - WakeLock 规则表
```kotlin
@Entity(
    tableName = "wakelock_rules",
    indices = [
        Index(value = ["packageName", "userId"], name = "idx_wakelock_app"),
        Index(value = ["tag"], name = "idx_wakelock_tag"),
        Index(value = ["action"], name = "idx_wakelock_action"),
        Index(value = ["enabled"], name = "idx_wakelock_enabled"),
        Index(value = ["priority"], name = "idx_wakelock_priority")
    ],
    foreignKeys = [
        ForeignKey(
            entity = AppInfo::class,
            parentColumns = ["packageName", "userId"],
            childColumns = ["packageName", "userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class WakelockRule(
    @PrimaryKey var id: String = UUID.randomUUID().toString(),
    var packageName: String = "",
    var userId: Int = 0,
    var tag: String = "",
    var action: ActionType = ActionType.ALLOW,
    var timeout: Long = 60000, // 超时时间（毫秒）
    var enabled: Boolean = true,
    var priority: Int = 0, // 规则优先级
    var matchType: MatchType = MatchType.EXACT,
    var pattern: String = "", // 正则表达式
    var description: String = "",
    var createdTime: Long = System.currentTimeMillis(),
    var lastModifiedTime: Long = System.currentTimeMillis(),
    var appliedCount: Long = 0, // 应用次数统计
    var conditions: RuleConditions? = null // 应用条件
) {
    enum class ActionType {
        ALLOW, LIMIT, BLOCK
    }
    
    enum class MatchType {
        EXACT, CONTAINS, REGEX, WILDCARD
    }
}
```

#### AlarmRule - Alarm 规则表
```kotlin
@Entity(
    tableName = "alarm_rules",
    indices = [
        Index(value = ["packageName", "userId"], name = "idx_alarm_app"),
        Index(value = ["action"], name = "idx_alarm_action"),
        Index(value = ["enabled"], name = "idx_alarm_enabled"),
        Index(value = ["alarmType"], name = "idx_alarm_type")
    ],
    foreignKeys = [
        ForeignKey(
            entity = AppInfo::class,
            parentColumns = ["packageName", "userId"],
            childColumns = ["packageName", "userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class AlarmRule(
    @PrimaryKey var id: String = UUID.randomUUID().toString(),
    var packageName: String = "",
    var userId: Int = 0,
    var tag: String = "",
    var action: ActionType = ActionType.ALLOW,
    var minInterval: Long = 60000, // 最小间隔（毫秒）
    var maxPerHour: Int = 60, // 每小时最大触发次数
    var enabled: Boolean = true,
    var priority: Int = 0,
    var alarmType: AlarmType = AlarmType.ALL,
    var timeWindow: TimeWindow? = null, // 时间窗口限制
    var description: String = "",
    var createdTime: Long = System.currentTimeMillis(),
    var lastModifiedTime: Long = System.currentTimeMillis(),
    var appliedCount: Long = 0
) {
    enum class ActionType {
        ALLOW, LIMIT, BLOCK, DELAY
    }
    
    enum class AlarmType {
        ALL, RTC, RTC_WAKEUP, ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP
    }
}
```

#### ServiceRule - Service 规则表
```kotlin
@Entity(
    tableName = "service_rules",
    indices = [
        Index(value = ["packageName", "userId"], name = "idx_service_app"),
        Index(value = ["serviceName"], name = "idx_service_name"),
        Index(value = ["action"], name = "idx_service_action"),
        Index(value = ["serviceType"], name = "idx_service_type")
    ],
    foreignKeys = [
        ForeignKey(
            entity = AppInfo::class,
            parentColumns = ["packageName", "userId"],
            childColumns = ["packageName", "userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ServiceRule(
    @PrimaryKey var id: String = UUID.randomUUID().toString(),
    var packageName: String = "",
    var userId: Int = 0,
    var serviceName: String = "",
    var action: ActionType = ActionType.ALLOW,
    var maxRuntime: Long = 300000, // 最大运行时间（毫秒）
    var startDelay: Long = 0, // 启动延迟
    var maxInstanceCount: Int = 1, // 最大实例数
    var enabled: Boolean = true,
    var priority: Int = 0,
    var serviceType: ServiceType = ServiceType.ALL,
    var resourceLimits: ResourceLimits? = null,
    var description: String = "",
    var createdTime: Long = System.currentTimeMillis(),
    var lastModifiedTime: Long = System.currentTimeMillis(),
    var appliedCount: Long = 0
) {
    enum class ActionType {
        ALLOW, LIMIT, BLOCK, DELAY
    }
    
    enum class ServiceType {
        ALL, FOREGROUND, BACKGROUND, BOUND
    }
}
```

### DAO 接口设计

#### AppInfoDao
```kotlin
@Dao
interface AppInfoDao {
    
    @Query("SELECT * FROM appInfo WHERE userId = :userId ORDER BY label ASC")
    fun getAppsByUser(userId: Int): Flow<List<AppInfo>>
    
    @Query("SELECT * FROM appInfo WHERE packageName = :packageName AND userId = :userId")
    suspend fun getApp(packageName: String, userId: Int): AppInfo?
    
    @Query("SELECT * FROM appInfo WHERE system = :isSystem AND userId = :userId")
    fun getAppsByType(isSystem: Boolean, userId: Int): Flow<List<AppInfo>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(appInfo: AppInfo)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAll(apps: List<AppInfo>)
    
    @Delete
    suspend fun delete(appInfo: AppInfo)
    
    @Query("DELETE FROM appInfo WHERE packageName = :packageName AND userId = :userId")
    suspend fun deleteByPackage(packageName: String, userId: Int)
    
    @Query("SELECT COUNT(*) FROM appInfo WHERE userId = :userId")
    suspend fun getAppCount(userId: Int): Int
    
    @Query("SELECT DISTINCT userId FROM appInfo ORDER BY userId")
    suspend fun getAllUserIds(): List<Int>
    
    // 搜索功能
    @Query("""
        SELECT * FROM appInfo 
        WHERE userId = :userId 
        AND (label LIKE '%' || :query || '%' OR packageName LIKE '%' || :query || '%')
        ORDER BY 
            CASE WHEN label LIKE :query || '%' THEN 1 ELSE 2 END,
            label ASC
    """)
    fun searchApps(userId: Int, query: String): Flow<List<AppInfo>>
    
    // 批量操作
    @Transaction
    suspend fun refreshAppList(userId: Int, newApps: List<AppInfo>) {
        // 删除不存在的应用
        val existingPackages = newApps.map { it.packageName }.toSet()
        deleteRemovedApps(userId, existingPackages)
        
        // 插入或更新应用
        insertOrUpdateAll(newApps.filter { it.userId == userId })
    }
    
    @Query("""
        DELETE FROM appInfo 
        WHERE userId = :userId 
        AND packageName NOT IN (:keepPackages)
    """)
    suspend fun deleteRemovedApps(userId: Int, keepPackages: Set<String>)
}
```

#### 规则 DAO 基类
```kotlin
// 通用规则操作接口
interface BaseRuleDao<T> {
    
    fun getAllRules(): Flow<List<T>>
    
    fun getRulesByApp(packageName: String, userId: Int): Flow<List<T>>
    
    fun getEnabledRules(): Flow<List<T>>
    
    suspend fun insertOrUpdate(rule: T)
    
    suspend fun delete(rule: T)
    
    suspend fun deleteByApp(packageName: String, userId: Int)
    
    suspend fun toggleRule(ruleId: String, enabled: Boolean)
    
    suspend fun getRuleCount(): Int
}

@Dao
interface WakelockRuleDao : BaseRuleDao<WakelockRule> {
    
    @Query("SELECT * FROM wakelock_rules ORDER BY priority DESC, lastModifiedTime DESC")
    override fun getAllRules(): Flow<List<WakelockRule>>
    
    @Query("""
        SELECT * FROM wakelock_rules 
        WHERE packageName = :packageName AND userId = :userId
        ORDER BY priority DESC
    """)
    override fun getRulesByApp(packageName: String, userId: Int): Flow<List<WakelockRule>>
    
    @Query("SELECT * FROM wakelock_rules WHERE enabled = 1 ORDER BY priority DESC")
    override fun getEnabledRules(): Flow<List<WakelockRule>>
    
    @Query("""
        SELECT * FROM wakelock_rules 
        WHERE enabled = 1 
        AND (packageName = :packageName OR packageName = '*')
        ORDER BY priority DESC
    """)
    suspend fun getMatchingRules(packageName: String): List<WakelockRule>
    
    @Query("""
        SELECT * FROM wakelock_rules 
        WHERE tag = :tag AND packageName = :packageName AND userId = :userId
    """)
    suspend fun getExactRule(packageName: String, tag: String, userId: Int): WakelockRule?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun insertOrUpdate(rule: WakelockRule)
    
    @Delete
    override suspend fun delete(rule: WakelockRule)
    
    @Query("DELETE FROM wakelock_rules WHERE packageName = :packageName AND userId = :userId")
    override suspend fun deleteByApp(packageName: String, userId: Int)
    
    @Query("UPDATE wakelock_rules SET enabled = :enabled WHERE id = :ruleId")
    override suspend fun toggleRule(ruleId: String, enabled: Boolean)
    
    @Query("SELECT COUNT(*) FROM wakelock_rules")
    override suspend fun getRuleCount(): Int
    
    // 统计相关查询
    @Query("""
        SELECT action, COUNT(*) as count 
        FROM wakelock_rules 
        WHERE enabled = 1 
        GROUP BY action
    """)
    suspend fun getActionStatistics(): Map<WakelockRule.ActionType, Int>
    
    @Query("""
        SELECT packageName, COUNT(*) as ruleCount
        FROM wakelock_rules 
        WHERE enabled = 1 
        GROUP BY packageName 
        ORDER BY ruleCount DESC 
        LIMIT :limit
    """)
    suspend fun getTopAppsWithRules(limit: Int): Map<String, Int>
}
```

## InfoDatabase 设计

### 事件记录数据库
```kotlin
@Database(
    entities = [InfoEvent::class],
    version = 12,
    exportSchema = true
)
abstract class InfoDatabase : RoomDatabase() {
    
    abstract fun infoEventDao(): InfoEventDao
    
    companion object {
        private const val DATABASE_NAME = "info_database"
        
        @Volatile
        private var INSTANCE: InfoDatabase? = null
        
        fun getInstance(context: Context): InfoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    InfoDatabase::class.java,
                    DATABASE_NAME
                ).addMigrations(
                    INFO_MIGRATION_11_12
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
```

### InfoEvent 实体详解
```kotlin
@Entity(
    tableName = "info_event",
    indices = [
        Index(value = ["packageName", "userId"], name = "idx_event_app"),
        Index(value = ["type"], name = "idx_event_type"),
        Index(value = ["startTime"], name = "idx_event_start_time"),
        Index(value = ["endTime"], name = "idx_event_end_time"),
        Index(value = ["isBlocked"], name = "idx_event_blocked"),
        Index(value = ["packageName", "type", "startTime"], name = "idx_event_composite"),
        Index(value = ["startTime", "endTime"], name = "idx_event_duration")
    ]
)
data class InfoEvent(
    @PrimaryKey 
    var instanceId: String = "", // IBinder.hashCode() + timestamp
    
    var name: String = "", // WakeLock tag / Alarm tag / Service name
    var type: Type = Type.UnKnow,
    var packageName: String = "",
    var userId: Int = 0,
    var startTime: Long = 0,
    var endTime: Long? = null,
    var isBlocked: Boolean = false,
    
    // 扩展信息
    var flags: Int = 0, // WakeLock flags / Alarm type
    var duration: Long = 0, // 持续时间（毫秒）
    var pid: Int = 0, // 进程ID
    var uid: Int = 0, // 用户ID
    var workSource: String? = null, // WorkSource 信息
    var stackTrace: String? = null, // 调用栈（调试用）
    var ruleId: String? = null, // 应用的规则ID
    var actionTaken: String? = null, // 执行的动作描述
    
    // 性能指标
    var cpuUsage: Float = 0f, // CPU 使用率
    var memoryUsage: Long = 0, // 内存使用（字节）
    var batteryDrain: Float = 0f, // 电量消耗估算
    
    // 上下文信息
    var screenOn: Boolean = true, // 屏幕状态
    var charging: Boolean = false, // 充电状态
    var networkType: String? = null, // 网络类型
    var deviceIdle: Boolean = false // 设备空闲状态
) {
    enum class Type {
        UnKnow, WakeLock, Alarm, Service
    }
    
    // 计算属性
    val actualDuration: Long
        get() = endTime?.let { it - startTime } ?: duration
        
    val isActive: Boolean
        get() = endTime == null && startTime > 0
        
    val efficiency: Float
        get() = if (actualDuration > 0) (1000f / actualDuration) else 0f
}
```

### InfoEventDao
```kotlin
@Dao
interface InfoEventDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: InfoEvent)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<InfoEvent>)
    
    @Update
    suspend fun update(event: InfoEvent)
    
    // 按应用查询事件
    @Query("""
        SELECT * FROM info_event 
        WHERE packageName = :packageName AND userId = :userId
        ORDER BY startTime DESC
        LIMIT :limit
    """)
    suspend fun getEventsByApp(
        packageName: String, 
        userId: Int, 
        limit: Int = 1000
    ): List<InfoEvent>
    
    // 按类型查询事件
    @Query("""
        SELECT * FROM info_event 
        WHERE type = :type AND userId = :userId
        AND startTime >= :startTime
        ORDER BY startTime DESC
    """)
    fun getEventsByType(
        type: InfoEvent.Type, 
        userId: Int, 
        startTime: Long
    ): Flow<List<InfoEvent>>
    
    // 获取活跃事件
    @Query("""
        SELECT * FROM info_event 
        WHERE endTime IS NULL AND startTime > 0
        ORDER BY startTime DESC
    """)
    fun getActiveEvents(): Flow<List<InfoEvent>>
    
    // 统计查询
    @Query("""
        SELECT 
            packageName,
            type,
            COUNT(*) as eventCount,
            SUM(CASE WHEN isBlocked THEN 1 ELSE 0 END) as blockedCount,
            AVG(duration) as avgDuration,
            MAX(startTime) as lastActivity
        FROM info_event 
        WHERE userId = :userId 
        AND startTime >= :startTime
        GROUP BY packageName, type
    """)
    suspend fun getEventStatistics(userId: Int, startTime: Long): List<EventStatistic>
    
    // 性能分析查询
    @Query("""
        SELECT 
            packageName,
            AVG(cpuUsage) as avgCpuUsage,
            AVG(memoryUsage) as avgMemoryUsage,
            SUM(batteryDrain) as totalBatteryDrain,
            COUNT(*) as eventCount
        FROM info_event 
        WHERE userId = :userId 
        AND startTime >= :startTime
        AND type = :type
        GROUP BY packageName
        ORDER BY totalBatteryDrain DESC
    """)
    suspend fun getPerformanceStats(
        userId: Int, 
        startTime: Long, 
        type: InfoEvent.Type
    ): List<PerformanceStatistic>
    
    // 清理过期数据
    @Query("DELETE FROM info_event WHERE startTime < :cutoffTime")
    suspend fun deleteOldEvents(cutoffTime: Long)
    
    @Query("DELETE FROM info_event WHERE packageName = :packageName AND userId = :userId")
    suspend fun deleteEventsByApp(packageName: String, userId: Int)
    
    // 数据完整性检查
    @Query("""
        SELECT * FROM info_event 
        WHERE packageName NOT IN (SELECT packageName FROM appInfo WHERE userId = info_event.userId)
        LIMIT 100
    """)
    suspend fun getOrphanEvents(): List<InfoEvent>
    
    @Query("DELETE FROM info_event WHERE instanceId IN (:instanceIds)")
    suspend fun deleteOrphanEvents(instanceIds: List<String>)
}

// 统计数据类
data class EventStatistic(
    val packageName: String,
    val type: InfoEvent.Type,
    val eventCount: Int,
    val blockedCount: Int,
    val avgDuration: Long,
    val lastActivity: Long
)

data class PerformanceStatistic(
    val packageName: String,
    val avgCpuUsage: Float,
    val avgMemoryUsage: Long,
    val totalBatteryDrain: Float,
    val eventCount: Int
)
```

## 数据迁移策略

### 版本迁移管理
```kotlin
// 自动迁移规格
@DeleteColumn(
    tableName = "appInfo",
    columnName = "obsoleteField"
)
@RenameColumn(
    tableName = "wakelock_rules",
    fromColumnName = "oldName",
    toColumnName = "newName"
)
class Migration12to13 : AutoMigrationSpec

// 手动迁移
val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // 添加新字段
        database.execSQL("""
            ALTER TABLE appInfo 
            ADD COLUMN targetSdkVersion INTEGER NOT NULL DEFAULT 0
        """)
        
        // 创建新索引
        database.execSQL("""
            CREATE INDEX idx_app_target_sdk 
            ON appInfo(targetSdkVersion)
        """)
        
        // 数据转换
        database.execSQL("""
            UPDATE wakelock_rules 
            SET priority = 0 
            WHERE priority IS NULL
        """)
    }
}

val INFO_MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // 添加性能监控字段
        database.execSQL("""
            ALTER TABLE info_event 
            ADD COLUMN cpuUsage REAL NOT NULL DEFAULT 0.0
        """)
        
        database.execSQL("""
            ALTER TABLE info_event 
            ADD COLUMN memoryUsage INTEGER NOT NULL DEFAULT 0
        """)
        
        database.execSQL("""
            ALTER TABLE info_event 
            ADD COLUMN batteryDrain REAL NOT NULL DEFAULT 0.0
        """)
        
        // 创建复合索引以提升查询性能
        database.execSQL("""
            CREATE INDEX idx_event_performance 
            ON info_event(packageName, type, startTime, batteryDrain)
        """)
    }
}
```

### 数据库维护工具
```kotlin
class DatabaseMaintenanceManager(
    private val appDatabase: AppDatabase,
    private val infoDatabase: InfoDatabase
) {
    
    // 定期清理过期数据
    suspend fun performMaintenance() {
        val cutoffTime = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30)
        
        // 清理过期事件
        infoDatabase.infoEventDao().deleteOldEvents(cutoffTime)
        
        // 清理孤立数据
        cleanupOrphanData()
        
        // 优化数据库
        optimizeDatabase()
        
        // 更新统计信息
        updateStatistics()
    }
    
    private suspend fun cleanupOrphanData() {
        val orphanEvents = infoDatabase.infoEventDao().getOrphanEvents()
        if (orphanEvents.isNotEmpty()) {
            val instanceIds = orphanEvents.map { it.instanceId }
            infoDatabase.infoEventDao().deleteOrphanEvents(instanceIds)
        }
    }
    
    private suspend fun optimizeDatabase() {
        // 重建索引
        appDatabase.query("REINDEX", null)
        infoDatabase.query("REINDEX", null)
        
        // 分析表结构
        appDatabase.query("ANALYZE", null)
        infoDatabase.query("ANALYZE", null)
        
        // 清理碎片
        appDatabase.query("VACUUM", null)
        infoDatabase.query("VACUUM", null)
    }
}
```

## 类型转换器

### 复杂数据类型处理
```kotlin
class Converters {
    
    @TypeConverter
    fun fromRuleConditions(conditions: RuleConditions?): String? {
        return conditions?.let { Gson().toJson(it) }
    }
    
    @TypeConverter
    fun toRuleConditions(json: String?): RuleConditions? {
        return json?.let { 
            try {
                Gson().fromJson(it, RuleConditions::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }
    
    @TypeConverter
    fun fromTimeWindow(timeWindow: TimeWindow?): String? {
        return timeWindow?.let { Gson().toJson(it) }
    }
    
    @TypeConverter
    fun toTimeWindow(json: String?): TimeWindow? {
        return json?.let {
            try {
                Gson().fromJson(it, TimeWindow::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }
    
    @TypeConverter
    fun fromResourceLimits(limits: ResourceLimits?): String? {
        return limits?.let { Gson().toJson(it) }
    }
    
    @TypeConverter
    fun toResourceLimits(json: String?): ResourceLimits? {
        return json?.let {
            try {
                Gson().fromJson(it, ResourceLimits::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }
}

// 复杂类型定义
data class RuleConditions(
    val timeWindows: List<TimeWindow> = emptyList(),
    val batteryLevel: IntRange? = null,
    val chargingState: Boolean? = null,
    val networkType: NetworkType? = null,
    val deviceIdle: Boolean? = null
)

data class TimeWindow(
    val startHour: Int, // 0-23
    val startMinute: Int, // 0-59
    val endHour: Int,
    val endMinute: Int,
    val daysOfWeek: Set<DayOfWeek> = setOf() // 空表示每天
)

data class ResourceLimits(
    val maxCpuUsage: Float = 100f, // 百分比
    val maxMemoryUsage: Long = Long.MAX_VALUE, // 字节
    val maxNetworkUsage: Long = Long.MAX_VALUE, // 字节/秒
    val maxStorageIO: Long = Long.MAX_VALUE // 字节/秒
)

enum class NetworkType {
    WIFI, MOBILE, ETHERNET, BLUETOOTH, VPN, ANY
}
```

## 性能优化

### 查询优化策略
```kotlin
// 使用视图简化复杂查询
@DatabaseView("""
    SELECT 
        a.packageName,
        a.userId,
        a.label,
        a.system,
        COUNT(e.instanceId) as eventCount,
        MAX(e.startTime) as lastActivity,
        SUM(CASE WHEN e.isBlocked THEN 1 ELSE 0 END) as blockedCount
    FROM appInfo a
    LEFT JOIN info_event e ON a.packageName = e.packageName AND a.userId = e.userId
    WHERE e.startTime >= :startTime OR e.startTime IS NULL
    GROUP BY a.packageName, a.userId
""")
data class AppSummaryView(
    val packageName: String,
    val userId: Int,
    val label: String,
    val system: Boolean,
    val eventCount: Int,
    val lastActivity: Long?,
    val blockedCount: Int
)

// 分页查询优化
@Query("""
    SELECT * FROM info_event 
    WHERE packageName = :packageName 
    ORDER BY startTime DESC 
    LIMIT :pageSize OFFSET :offset
""")
suspend fun getEventsPaged(
    packageName: String, 
    pageSize: Int, 
    offset: Int
): List<InfoEvent>

// 预计算统计数据
@Entity(tableName = "event_statistics_cache")
data class EventStatisticsCache(
    @PrimaryKey val key: String, // packageName_userId_type_date
    val packageName: String,
    val userId: Int,
    val type: InfoEvent.Type,
    val date: String, // yyyy-MM-dd
    val eventCount: Int,
    val blockedCount: Int,
    val totalDuration: Long,
    val avgDuration: Long,
    val maxDuration: Long,
    val lastUpdated: Long
)
```

!!! info "设计原则"
    数据库设计遵循第三范式，避免数据冗余，同时考虑查询性能，通过合理的索引设计和视图优化提升访问效率。

!!! warning "维护注意"
    定期执行数据库维护任务，包括清理过期数据、重建索引和优化查询计划，确保数据库性能保持最优状态。