# σ₃: Technical Context
*v1.0 | Created: 2025-04-15 | Updated: 2025-05-20*
*Π: 🏗️DEVELOPMENT | Ω: ⚙️E*

## 🛠️ Technology Stack
- 🖥️ Frontend: Jetpack Compose, Material 3, Compose Navigation
- 🗄️ Backend: Kotlin, Android SDK, Xposed Framework
- 📊 Database: Room Persistence Library
- 🧪 Testing: JUnit, Mockito, Truth
- 🚀 Deployment: Google Play Store, F-Droid
- 🔧 Settings: Context API-based state management, LocalStorage persistence

## ⚙️ Development Environment
- [E₁] Android Studio ⟶ Latest version
- [E₂] Gradle 8.x ⟶ For build automation with Kotlin DSL
- [E₃] Git ⟶ For version control
- [E₄] GitHub Actions ⟶ For CI/CD
- [E₅] Rooted Android device/emulator ⟶ For testing Xposed functionality
- [E₆] Android SDK 35 ⟶ Latest target SDK

## 📦 Dependencies
- [D₁] Kotlin Coroutines ⟶ For asynchronous programming
- [D₂] Jetpack Compose ⟶ For modern UI development
- [D₃] Room ⟶ For local database operations
- [D₄] Koin ⟶ For dependency injection
- [D₅] Xposed Framework ⟶ For system-level hooks
- [D₆] Coil ⟶ For app icon loading
- [D₇] Kotlinx Serialization ⟶ For type-safe navigation
- [D₈] Kotlinx Collections Immutable ⟶ For immutable collections
- [D₉] DataStore Preferences ⟶ For preferences storage
- [D₁₀] Navigation Compose ⟶ For navigation
- [D₁₁] Core Splashscreen ⟶ For splash screen
- [D₁₂] Material 3 ⟶ For modern UI components
- [D₁₃] ConstraintLayout Compose ⟶ For complex layouts

## 🚧 Technical Constraints
- [T₁] Root Access ⟶ Required for complete functionality with Xposed
- [T₂] Android Version Compatibility ⟶ Minimum SDK 24 (Android 7.0), Target SDK 35
- [T₃] Battery Usage ⟶ Monitoring must have minimal impact on battery
- [T₄] Permission Model ⟶ Requires QUERY_ALL_PACKAGES permission
- [T₅] Xposed Module Scope ⟶ Limited to specific packages defined in scopes array
- [T₆] Multi-user Support ⟶ Must handle data for different Android user profiles
- [T₇] Edge-to-edge UI ⟶ Modern UI requires proper edge-to-edge handling
- [T₈] Settings Persistence ⟶ LocalStorage size limitations (5-10MB based on browser)
- [T₉] Theme Consistency ⟶ All components must respect theme settings
- [T₁₀] Settings Version Control ⟶ Need versioning mechanism for settings format changes

## 🔧 Key Technical Components

### 📝 SavedStateHandle Integration
- SavedStateHandle provides automatic state restoration during configuration changes and process death
- Parameter constant classes define string keys for all SavedStateHandle parameters
- Screen parameters are passed through navigation arguments automatically
- Custom properties with get/set accessors simplify interaction with SavedStateHandle

**Implementation Pattern:**
```kotlin
// Parameter constant class
object AppsScreenParams {
    const val USER_ID = "userId"
    const val SEARCH_QUERY = "searchQuery"
    const val SORT_ORDER = "sortOrder"
}

// SavedStateHandle usage in ViewModel
class AppsViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val appRepository: AppRepository
) : ViewModel() {
    // Type-safe accessor method
    var userId: Int
        get() = savedStateHandle.get<Int>(AppsScreenParams.USER_ID) ?: 0
        set(value) { savedStateHandle[AppsScreenParams.USER_ID] = value }
}
```

### 🛠️ Navigation System Architecture
- Type-based navigation using serializable data classes for type-safety
- Hybrid approach combining type-safety with string-based navigation for legacy screens
- Route detection using pattern matching for complex paths
- NavGraph preserves SavedStateHandle functionality while supporting mixed navigation

**Implementation Pattern:**
```kotlin
// In NavGraph.kt
composable<Apps>(
    deepLinks = listOf(NavDeepLink("app://nowakelock/apps/{userId}")),
) { backStackEntry ->
    val viewModel = hiltViewModel<AppsViewModel>()
    AppsScreen(viewModel = viewModel)
}
```

### ⏱️ Wakelock Timing Implementation
- Thread-safe counter operations using AtomicInteger 
- Timestamp visibility ensured with @Volatile annotation
- Non-overlapping duration calculation for accurate statistics
- Low overhead design with minimal synchronization

**Implementation Pattern:**
```kotlin
class WakelockCounter {
    private val activeCount = AtomicInteger(0)
    
    @Volatile
    private var activeTimestamp: Long = 0
    
    @Volatile
    private var accumulatedDuration: Long = 0
    
    // Called when a wakelock is acquired
    fun increment(): Int {
        val newCount = activeCount.incrementAndGet()
        
        // Only update timestamp on transition from 0 to 1
        if (newCount == 1) {
            activeTimestamp = System.currentTimeMillis()
        }
        
        return newCount
    }
}
```

### 🚀 Data Loading and UI Performance Optimization
- Unified data loading trigger with debounce mechanism
- Flow chain optimization with conflate, distinctUntilChanged, and debounce operators
- In-memory caching with 30-second validity period
- Component lifecycle optimization with LaunchedEffect

**Implementation Pattern:**
```kotlin
fun triggerDataLoad(forceRefresh: Boolean = false, keyParams: Boolean = false) {
    val currentTime = System.currentTimeMillis()
    
    // Use immediate loading for key parameters
    if (keyParams) {
        refreshData()
        return
    }
    
    // Debounce with 300ms delay for normal loading
    loadJob?.cancel()
    loadJob = viewModelScope.launch {
        delay(300L) // Debounce delay
        refreshData(forceRefresh)
    }
}

// Flow optimization
repository.getData()
    .distinctUntilChanged { old, new -> compareEssentialFields(old, new) }
    .debounce(50L)
    .conflate()
    .collectLatest { data ->
        _uiState.update { it.copy(data = data, loading = false) }
    }
```

### 🔍 Database Migration Strategy
- Multi-path migration for supporting version jumps
- Transaction protection ensuring atomic operations
- Table rebuilding approach for resolving index conflicts
- Comprehensive logging and exception handling

**Implementation Pattern:**
```kotlin
val MIGRATION_10_13 = object : Migration(10, 13) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.beginTransaction()
        try {
            // Create temporary table
            database.execSQL("CREATE TABLE IF NOT EXISTS app_temp (...)")
            
            // Copy data
            database.execSQL("INSERT INTO app_temp SELECT * FROM app")
            
            // Drop original table
            database.execSQL("DROP TABLE app")
            
            // Rename temporary table
            database.execSQL("ALTER TABLE app_temp RENAME TO app")
            
            database.setTransactionSuccessful()
        } finally {
            database.endTransaction()
        }
    }
}
```

### 🧪 Testing Architecture 
- Singleton reset using reflection for reliable unit testing
- Test class separation for better focus and maintenance
- Test suite organization controlling execution order
- Before/After methods ensuring test isolation

**Implementation Pattern:**
```kotlin
// TestUtils.kt
fun resetWakelockRegistry() {
    val registryClass = WakelockRegistry::class.java
    val instanceField = registryClass.getDeclaredField("instance")
    instanceField.isAccessible = true
    
    val modifiersField = Field::class.java.getDeclaredField("modifiers")
    modifiersField.isAccessible = true
    modifiersField.setInt(instanceField, instanceField.modifiers and Modifier.FINAL.inv())
    
    instanceField.set(null, null)
}

// Test suite
@RunWith(Suite::class)
@Suite.SuiteClasses(
    WakelockCounterTest::class,
    WakelockRegistryBasicTest::class,
    WakelockRegistryProblemTest::class
)
class WakelockTests {}
```

### 📱 Xposed Hook System
- Unified hooking strategy across Android versions
- Parameter position caching for performance optimization
- Adaptive parameter extraction for compatibility
- Conditional logging based on debug mode settings

**Core Components:**
- XposedModule: Main entry point for Xposed framework
- WakelockHook: Handles wakelock acquisition and release
- AlarmHook: Monitors alarm setting and triggering
- ServiceHook: Captures service starting and binding

### 🔄 Boot Detection and Reset
- Reliable device reboot detection with SystemClock.elapsedRealtime()
- Database table reset on application restart after device reboot
- DataStore preferences for persistent timestamp storage

**Implementation Pattern:**
```kotlin
fun checkAndResetAfterBoot() {
    val lastShutdownTime = dataStore.data.first()[LAST_SHUTDOWN_TIME] ?: 0
    val currentTime = SystemClock.elapsedRealtime()
    
    // If current time is much less than last recorded time, device was rebooted
    if (currentTime < lastShutdownTime - REBOOT_THRESHOLD) {
        // Reset database tables
        resetInfoTables()
        
        // Update timestamp
        dataStore.edit { preferences ->
            preferences[LAST_SHUTDOWN_TIME] = currentTime
        }
    }
}
```

### 🔍 Module Check Feature
- Verification of module activation status, hook effectiveness, and configuration path
- Content Provider extension for cross-process communication
- Multi-language support for user guidance

**Key Components:**
- ModuleCheckManager: Core detection logic
- ModuleCheckRepository: Data access interface  
- ModuleCheckViewModel: UI state management
- ModuleCheckScreen: User interface

## 🔮 Future Technical Considerations
- Further optimization of large list rendering with LazyColumn recycling
- Improved TypedRoute path handling for complex navigation requirements
- Enhanced unit test coverage for core components
- Migration from Gson to Kotlinx.Serialization for better Kotlin integration
- Standardization on Material 3 components across the entire UI
- Addressing hidden API access warnings in Room database implementation

## 移动应用性能优化策略

### 数据加载与缓存优化
NoWakeLock应用采用以下数据加载与缓存策略：

#### 统一数据加载协调器
- 使用 `triggerDataLoad` 方法集中管理加载请求
- 通过 `immediate` 参数区分立即加载和延迟加载操作
- 使用 `Job` 跟踪和取消进行中的加载作业，避免冗余操作

```kotlin
private fun triggerDataLoad(source: LoadingSource, immediate: Boolean = false) {
    loadDataJob?.cancel() // 取消进行中的作业
    loadDataJob = viewModelScope.launch {
        if (!immediate && source != LoadingSource.INITIAL && source != LoadingSource.USER_PULL) {
            delay(200) // 非立即加载应用防抖延迟
        }
        loadApps(source)
    }
}
```

#### 内存缓存机制
- 使用 `Map<String, Pair<Data, Timestamp>>` 结构存储缓存数据
- 基于查询参数生成唯一缓存键
- 实现30秒过期时间的自动缓存失效策略
- 在数据变更时主动清除相关缓存（应用列表变化、数据同步等）
- 通过LRU策略管理缓存大小（最多20项，移除最旧数据）

```kotlin
// 缓存键生成
private fun generateCacheKey(sortBy: String, userId: Int? = null, filter: String? = null): String {
    val sb = StringBuilder(sortBy)
    if (userId != null) sb.append("_user").append(userId)
    if (filter != null) sb.append("_filter").append(filter)
    return sb.toString()
}

// 缓存检查
private fun getCachedData(cacheKey: String): List<Data>? {
    val cachedEntry = cache[cacheKey] ?: return null
    val (data, timestamp) = cachedEntry
    return if (System.currentTimeMillis() - timestamp <= CACHE_EXPIRATION_MS) {
        data // 缓存有效，返回数据
    } else {
        cache.remove(cacheKey) // 缓存过期，移除
        null
    }
}
```

### Flow操作符优化
- 使用 `conflate()` 确保处理最新数据，忽略处理繁忙时的中间值
- 应用 `distinctUntilChanged()` 避免无变化数据的重复处理
- 在高频更新中使用 `debounce()` 减少处理负担

### 生命周期管理
- 使用 `LaunchedEffect` 封装副作用操作
- 基于关键参数变化触发数据加载
- 实现错误追踪和日志记录

这些策略有效减少了重复加载和数据库访问，提高了UI响应速度，特别是在频繁切换筛选条件或搜索时的性能表现。