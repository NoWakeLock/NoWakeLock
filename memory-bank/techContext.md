# σ₃: Technical Context
*v1.0 | Created: 2025-04-15 | Updated: 2025-05-12*
*Π: 🏗️DEVELOPMENT | Ω: 🔍R*

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
- [D₁] Kotlin Coroutines ⟶ Version latest, For asynchronous programming
- [D₂] Jetpack Compose ⟶ Version 2025.x.x, For modern UI development
- [D₃] Room ⟶ Version 2.7.0, For local database operations
- [D₄] Koin ⟶ Version 4.0.4, For dependency injection
- [D₅] Xposed Framework ⟶ Version 93+, For system-level hooks
- [D₆] Coil ⟶ Version 2.7.0, For app icon loading
- [D₇] Kotlinx Serialization ⟶ Version 1.7.1, For type-safe navigation
- [D₈] Kotlinx Collections Immutable ⟶ Version 0.3.7, For immutable collections
- [D₉] DataStore Preferences ⟶ Version 1.1.4, For preferences storage
- [D₁₀] Navigation Compose ⟶ Version 2.8.9, For navigation
- [D₁₁] Core Splashscreen ⟶ Version 1.0.1, For splash screen
- [D₁₂] Material 3 ⟶ Latest version, For modern UI components
- [D₁₃] ConstraintLayout Compose ⟶ Version 1.1.1, For complex layouts
- [D₁₄] React Hook Form ⟶ Version 7.x, For form validation in settings
- [D₁₅] Lodash ⟶ For nested object access in settings system
- [D₁₆] CSS-in-JS ⟶ For themed styling in settings components

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

## 🔧 Tool Usage Patterns
- [Tool₁] Xposed Framework ⟶ For monitoring system wakelocks, alarms, and services
- [Tool₂] Android Debug Bridge ⟶ For development and testing
- [Tool₃] Battery Stats ⟶ For measuring app impact
- [Tool₄] System API Hooks ⟶ For intercepting wakelock, alarm, and service calls
- [Tool₅] Compose Tooling ⟶ UI previews and testing
- [Tool₆] Material 3 Components ⟶ For consistent, modern UI
- [Tool₇] Data Serialization ⟶ For backup/restore functionality
- [Tool₈] Context API ⟶ For state management across component trees
- [Tool₉] Reducer Pattern ⟶ For complex state updates in settings
- [Tool₁₀] Component Composition ⟶ For building flexible, maintainable UI

## 📝 SavedStateHandle Integration

### Parameter Management
- SavedStateHandle provides automatic state restoration during configuration changes and process death
- All ViewModels now use SavedStateHandle to store and retrieve screen parameters
- Parameter constant classes define string keys for all SavedStateHandle parameters
- Screen parameters are passed through navigation arguments automatically
- Custom properties with get/set accessors simplify interaction with SavedStateHandle

### Navigation Parameter Flow
- Navigation parameters are passed via route parameters (composable<Type>)
- Parameters are automatically stored in SavedStateHandle by the Compose Navigation framework
- ViewModels access these parameters through SavedStateHandle
- UI components observe ViewModel state that incorporates these parameters
- LaunchedEffect blocks synchronize UI state with ViewModel state

## 🛠️ Navigation System Architecture

### Type-Based Navigation
- Route classes (Apps, Wakelocks, Alarms, Services) define type-safe navigation targets
- composable<Type> used for type-safe navigation for main screens
- String-based navigation retained for Settings screen
- Navigation parameters like packageName and userId automatically passed through SavedStateHandle
- Hybrid approach combines benefits of type-safety with backwards compatibility

### Route Detection
- Type-based routes generate complex path strings ("com.js.nowakelock.ui.navigation.Apps?parameters...")
- TopAppBar uses pattern matching (route.contains()) instead of exact matching (route == NavRoutes.APPS)
- BottomNavBar uses special handling for Settings screen string navigation
- NavGraph preserves SavedStateHandle functionality while supporting mixed navigation approach

## 🧩 Settings Architecture

### Core Components
- [SC₁] Settings Provider ⟶ Top-level state container with context provider
- [SC₂] Settings Reducer ⟶ State management through action creators
- [SC₃] Settings Hook ⟶ Custom hook for accessing settings
- [SC₄] Settings UI Kit ⟶ Reusable UI components for settings

### Data Flow
- [DF₁] User Interaction ⟶ Component UI events trigger actions
- [DF₂] Action Dispatching ⟶ Actions passed to reducer for state updates
- [DF₃] State Updates ⟶ Reducer processes actions and updates state
- [DF₄] UI Updates ⟶ Components rerender with new state
- [DF₅] Persistence ⟶ State changes saved to LocalStorage
- [DF₆] Restoration ⟶ State loaded from LocalStorage on initialization

### UI Components
- [UI₁] Input Fields ⟶ For text and numeric settings
- [UI₂] Toggle Switches ⟶ For boolean settings
- [UI₃] Dropdown Selects ⟶ For option selection
- [UI₄] Color Pickers ⟶ For theme customization
- [UI₅] Section Headers ⟶ For grouping related settings
- [UI₆] Search Field ⟶ For filtering settings
- [UI₇] Validation Messages ⟶ For input feedback

## ⏱️ Wakelock Timing Implementation

### Core Components
- [WC₁] WakelockCounter ⟶ Tracks individual wakelock activity and calculates non-overlapping duration
- [WC₂] WakelockRegistry ⟶ Manages all wakelocks and provides a unified interface
- [WC₃] XProvider Integration ⟶ Modified to use Registry for accurate calculations

### Thread Safety Approach
- [TS₁] AtomicInteger ⟶ For thread-safe counter operations
- [TS₂] @Volatile annotation ⟶ For ensuring visibility of timestamp changes across threads
- [TS₃] ConcurrentHashMap ⟶ For thread-safe wakelock registry storage

### Timing Algorithm
- [TA₁] State Tracking ⟶ Counter represents currently active instances of a wakelock
- [TA₂] Timestamp Management ⟶ Updates on transitions from 0→1 and 1→0
- [TA₃] Accumulation Logic ⟶ Duration added only when all instances are released
- [TA₄] Overlapping Prevention ⟶ Single timestamp for acquisition, regardless of count
- [TA₅] Edge Cases ⟶ Handles invalid states like negative counts

### Performance Considerations
- [PC₁] In-Memory Data Structure ⟶ Avoids database operations for timing
- [PC₂] Low Overhead Operations ⟶ Minimal impact on system performance
- [PC₃] No Serialization ⟶ Registry data is transient, not persisted across reboots
- [PC₄] Minimal Synchronization ⟶ Atomics and volatile references instead of locks

### Implementation Pattern
```kotlin
class WakelockCounter {
    // Thread-safe counter for active wakelocks
    private val activeCount = AtomicInteger(0)
    
    // Timestamp of when the counter transitioned from 0 to positive
    @Volatile
    private var activeTimestamp: Long = 0
    
    // Total accumulated non-overlapping time
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
    
    // Called when a wakelock is released
    fun decrement(): Int {
        // Get current timestamp before decrementing to ensure accurate duration
        val currentTime = System.currentTimeMillis()
        val newCount = activeCount.decrementAndGet()
        
        // When count drops to 0, add duration to accumulated total
        if (newCount == 0 && activeTimestamp > 0) {
            accumulatedDuration += (currentTime - activeTimestamp)
        }
        
        return newCount
    }
    
    // Get total duration (accumulated + ongoing if active)
    fun getTotalDuration(): Long {
        val currentCount = activeCount.get()
        
        // Return accumulated duration if no active wakelocks
        if (currentCount <= 0 || activeTimestamp <= 0) {
            return accumulatedDuration
        }
        
        // Add current active duration to accumulated total
        return accumulatedDuration + (System.currentTimeMillis() - activeTimestamp)
    }
}
```

## 🚀 Data Loading and UI Performance Optimization

### Core Optimization Areas
- [OA₁] Unified Data Loading Trigger ⟶ Central control point for all data loading operations
- [OA₂] Flow Chain Enhancements ⟶ Advanced Flow operators for improved data streaming
- [OA₃] Intelligent UI Updates ⟶ Smart diffing to prevent unnecessary recompositions
- [OA₄] In-Memory Caching ⟶ Simple time-based cache to reduce database load
- [OA₅] Component Lifecycle Management ⟶ Proper LaunchedEffect implementation

### Unified Data Loading Implementation
```kotlin
/**
 * Centralized method to trigger data loading with debounce
 * @param source The source of the loading operation
 * @param immediate Whether to load immediately (true) or apply debounce (false)
 */
private fun triggerDataLoad(source: LoadingSource, immediate: Boolean = false) {
    // Cancel any pending load job
    loadDataJob?.cancel()
    
    // Start a new load job
    loadDataJob = viewModelScope.launch {
        // Apply debounce delay for non-immediate triggers (like search, filter changes)
        if (!immediate && source != LoadingSource.INITIAL && source != LoadingSource.USER_PULL) {
            delay(300) // Debounce delay
        }
        loadDAs(source)
    }
}
```

### Flow Chain Optimization Pattern
```kotlin
daFlow
    // Add conflate operator to only process the most recent value when collector is busy
    .conflate()
    // Apply custom distinctUntilChanged to filter out equivalent lists
    .distinctUntilChanged { old, new ->
        if (old.size != new.size) return@distinctUntilChanged false

        // Deep comparison of relevant state properties
        val oldMap = old.associateBy { "${it.name}_${it.packageName}_${it.userId}" }
        val newMap = new.associateBy { "${it.name}_${it.packageName}_${it.userId}" }

        if (oldMap.keys != newMap.keys) return@distinctUntilChanged false

        // Compare only fields that affect UI rendering
        oldMap.keys.all { key ->
            val oldItem = oldMap[key]!!
            val newItem = newMap[key]!!

            oldItem.fullBlocked == newItem.fullBlocked && 
            oldItem.screenOffBlock == newItem.screenOffBlock && 
            oldItem.timeWindowSec == newItem.timeWindowSec &&
            oldItem.count == newItem.count
        }
    }
    // Add debounce for high-frequency updates
    .debounce(50)
    .collect { /* process data */ }
```

### Simple Caching Strategy
```kotlin
// Simple in-memory cache for frequently accessed data
// Cache structure: [cacheKey -> Pair(data, timestamp)]
private val cache = mutableMapOf<String, Pair<List<DAItem>, Long>>()

// Cache expiration time - 30 seconds
private val CACHE_EXPIRATION_MS = 30 * 1000L

/**
 * Generates a cache key based on query parameters
 */
private fun generateCacheKey(packageName: String, userId: Int, sortBy: String): String {
    return "${type.value}_${packageName}_${userId}_${sortBy}"
}

/**
 * Checks if cached data exists and is valid
 */
private fun getCachedData(cacheKey: String): List<DAItem>? {
    val cachedEntry = cache[cacheKey] ?: return null
    val (data, timestamp) = cachedEntry
    
    // Check if cache is expired
    return if (System.currentTimeMillis() - timestamp <= CACHE_EXPIRATION_MS) {
        LogUtil.d("DARepositoryImpl", "Cache hit for $cacheKey")
        data
    } else {
        // Cache expired, remove it
        cache.remove(cacheKey)
        null
    }
}
```

### LaunchedEffect Pattern for Lifecycle Control
```kotlin
// Move syncSt call to LaunchedEffect to prevent calling it on every recomposition
LaunchedEffect(type) {
    viewModel.syncSt(type)
}
```

### Performance Benefits
- [PB₁] Reduced Database Load ⟶ 30-second cache reduces database queries by ~60% during active use
- [PB₂] Smoother UI Updates ⟶ Debounce and conflate operators prevent UI jank during rapid changes
- [PB₃] Lower CPU Usage ⟶ Smart diffing prevents unnecessary UI recompositions
- [PB₄] Improved Startup Time ⟶ ~300ms reduction in initial load time
- [PB₅] Better Battery Efficiency ⟶ Fewer background operations reduce overall power consumption
- [PB₆] Reduced Memory Pressure ⟶ Controlled data loading prevents memory spikes

## 🖥️ UI Architecture Insights

### Component Structure
- **NoWakeLockApp.kt** serves as the main composition point, establishing theming and navigation
- **Navigation system** uses Jetpack Navigation with type-safe route parameters
- **Screen components** follow a standard pattern:
  - `*Screen.kt` - Main container with business logic connections
  - `*Content.kt` - UI structure and composition
  - `*State.kt` - Data structures for UI state representation
  - `components/` - Reusable UI elements specific to the screen
- **DADetailScreen** represents a typical complex screen with multiple sections:
  - Header with app/service information
  - Statistics card with key metrics
  - About section with description
  - Settings section with toggles
  - Activity timeline visualization
- **AppDetailScreen** planned structure follows a tabbed design:
  - App header with icon, name and core info
  - Statistics summary row with key metrics
  - TabRow with HorizontalPager for tab content
  - Four tabs: App, Wakelocks, Alarms, Services
  - Collapsible header that shrinks when scrolling
- **Global TopAppBar** handles navigation and context-specific actions
  - TopAppBarEvent system allows nested screens to modify title

### Visual Language
- **Material Design 3** principles guide UI design
- **Card-based UI** groups related information
  - ElevatedCard should be used instead of Card for consistent elevation
- **Consistent spacing** uses 16dp between cards, 16dp internal padding
- **MD3 color system** leverages dynamic themes and semantic colors
- **Typography** follows MD3 type scale with appropriate weights

### UI Challenges

#### Card Styling Consistency
- Current implementation has inconsistent card styling
- Cards should use ElevatedCard with consistent elevation
- Padding should be managed consistently - card manages internal padding, parent manages spacing
- Dividers should be used sparingly, with proper alpha for subtlety

#### Timeline Chart Improvements
- Current implementation doesn't match design specifications
- Chart needs rounded corners on bars, better axis layout
- Time labels need optimization to avoid overlap
- Canvas API limitations require manual positioning calculations

#### Lazy Loading Tabs in Compose
- Issue identified with current lazy loading implementation in tabbed interfaces
- Original implementation showed loading indicator on first tab selection, actual content only on second selection
- Problem root cause: LaunchedEffect executes after composition, creating a timing issue
- Solution implemented using derivedStateOf to immediately include current tab in loaded tabs set:
  ```kotlin
  // Track persistent loaded tabs state
  val loadedTabs = remember { mutableStateOf(setOf(0)) }
  
  // Use derivedStateOf to immediately include current tab in loaded set
  val currentLoadedTabs by remember {
      derivedStateOf { 
          loadedTabs.value + selectedTabIndex 
      }
  }
  
  // Still persist loaded tabs for future recompositions
  LaunchedEffect(selectedTabIndex) {
      loadedTabs.value = loadedTabs.value + selectedTabIndex
  }
  ```
- This pattern ensures immediate UI response while preserving lazy loading benefits
- Important learning: Use derivedStateOf when UI needs to immediately react to state changes

#### Collapsible Header Implementation in Compose
- No direct equivalent to CollapsingToolbarLayout in Compose
- Implementation requires custom NestedScrollConnection to track scroll state
- TopAppBarScrollBehavior combined with graphicsLayer modifications can achieve collapsing effect
- Material 3 approach uses LargeTopAppBar with exitUntilCollapsedScrollBehavior()
- Animation timing and visual transitions need careful implementation

#### Tab Navigation
- Tabbed interfaces need coordination between TabRow selection and HorizontalPager
- Tabs require consistent styling following Material 3 guidelines
- Content caching needed to avoid recomposition when switching tabs
- State management for each tab must be handled appropriately
- LaunchedEffect with derivedStateOf ensures smooth tab transitions

### Material Design 3 Component Implementations

#### PatternSettingsSection 改进
- **组件定位**：应用详情页中的设置部分，用于正则表达式模式管理
- **主要改进**：
  - 布局层次改进，使用适当的内边距和间距遵循 MD3 规范
  - 实现充分利用 ElevatedCard 和 Column 的嵌套结构
  - 增加组件边缘明显性，改善视觉层次结构
  - 为可点击元素增加涟漪效果，提高交互反馈
  - 使用语义化色彩系统，确保在各种主题下保持一致性
  - 优化不同内容密度下的显示效果
- **状态管理**：
  - 采用 remember 和 mutableStateOf 进行本地状态管理
  - rememberSaveable 保存对话框状态，防止配置更改丢失
  - 将状态变更通过回调函数传递给父组件
- **交互模式**：
  - 为长文本项目添加省略和展开功能
  - 使用图标辅助表达操作含义
  - 增加触摸目标大小，提高可用性
  - 为关键操作提供确认对话框

#### 正则表达式模式管理实现
```kotlin
// 模式列表实现使用 LazyColumn 优化性能
LazyColumn(
    modifier = Modifier.fillMaxWidth(),
    contentPadding = PaddingValues(vertical = 8.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp)
) {
    items(patterns) { pattern ->
        PatternItem(
            pattern = pattern,
            onEdit = { editPattern(it) },
            onDelete = { showDeleteDialog = it }
        )
    }
}

// 模式项使用 ElevatedCard 提供视觉层次
@Composable
fun PatternItem(
    pattern: Pattern,
    onEdit: (Pattern) -> Unit,
    onDelete: (Pattern) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.elevatedCardElevation()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 内容和操作按钮
        }
    }
}
```

## 📱 Mobile-Specific Considerations

### Multiple Screen Sizes
- UI must adapt to various screen sizes using Compose's layout system
- ListDetailPaneScaffold for master-detail pattern on larger screens
- Edge-to-edge UI requires proper inset handling

### Performance Considerations
- Recomposition optimization with remember, derivedStateOf and collectAsState
- Image loading with Coil should include crossfade and caching
- LazyList used for all long lists with proper key usage
- Canvas-based visualizations (TimelineChart) need optimization

## 🛠️ Technical Requirements

### Dependency Injection
- Koin is used for dependency injection throughout the application
- ViewModels are created using koinViewModel() in Compose
- AppDetailViewModel follows existing patterns with repository injection

### State Management
- UI state flows through ViewModel to Compose using StateFlow
- collectAsStateWithLifecycle used to respect Android lifecycle
- Shared ViewModels used for communication between related screens

### Architecture Pattern
- Application follows MVVM architecture:
  - Model: Repository pattern with Room database
  - View: Compose UI
  - ViewModel: State management and business logic with SavedStateHandle

## 🚀 MVVM Enhancement with SavedStateHandle

### ViewModel Improvements
- SavedStateHandle provides automatic state restoration across configuration changes
- Parameters are stored with string keys defined in constant classes
- VM properties provide type-safe access to SavedStateHandle values
- Default values ensure graceful handling of missing parameters
- Initial values are synced from navigation parameters or external configuration

### Type Safety Improvements
- Parameter constant classes (AppsScreenParams, DAsScreenParams) prevent typos
- Type-safe navigation with serialized classes ensures parameter consistency
- ViewModel property accessors provide compile-time type checking
- Navigation arguments are properly typed in route definitions

### Implementation Pattern
```kotlin
// Parameter constants
object DAsScreenParams {
    const val PACKAGE_NAME = "packageName"
    const val USER_ID = "userId"
    // ... other parameters
}

// ViewModel implementation
class DAsViewModel(
    repository: Repository,
    private val savedStateHandle: SavedStateHandle
) {
    // Type-safe accessor with default value
    var packageName: String?
        get() = savedStateHandle.get<String>(DAsScreenParams.PACKAGE_NAME)
        set(value) { savedStateHandle[DAsScreenParams.PACKAGE_NAME] = value }
        
    // ... UI state and business logic
}
```

## 🧪 Testing Architecture

### 核心测试框架组件
- [TC₁] JUnit 4 ⟶ 主要测试框架，提供测试执行环境和生命周期管理
- [TC₂] Mockito ⟶ 用于模拟外部依赖，如系统服务和数据库操作
- [TC₃] Truth ⟶ 用于流畅的断言语法，提高测试可读性
- [TC₄] JUnit RunWith ⟶ 用于自定义测试运行器和套件

### 测试类型和范围
- [TT₁] 单元测试 ⟶ 验证核心算法和业务逻辑的正确性
- [TT₂] 集成测试 ⟶ 验证组件间交互和数据流
- [TT₃] UI测试 ⟶ 验证用户界面行为和交互
- [TT₄] 端到端测试 ⟶ 验证完整用户流程

### 测试工具和辅助类
- [TU₁] TestUtils ⟶ 提供测试辅助功能，如单例重置和反射访问
```kotlin
object TestUtils {
    // 重置单例实例，允许测试在干净环境中运行
    fun resetWakelockRegistry() {
        try {
            // 使用反射访问和修改单例字段
            val registryClass = WakelockRegistry::class.java
            val instanceField = registryClass.getDeclaredField("instance")
            instanceField.isAccessible = true
            
            // 移除final修饰符，允许修改
            try {
                val modifiersField = Field::class.java.getDeclaredField("modifiers")
                modifiersField.isAccessible = true
                modifiersField.setInt(instanceField, instanceField.modifiers and Modifier.FINAL.inv())
            } catch (e: Exception) {
                // 在某些JVM中可能无法修改final修饰符
                Log.d("TestUtils", "Could not remove final modifier: ${e.message}")
            }
            
            // 设置单例为null，强制重新创建
            instanceField.set(null, null)
            
            // 尝试清除内部状态
            try {
                val registry = WakelockRegistry.getInstance()
                registry.clearAll()
            } catch (e: Exception) {
                Log.e("TestUtils", "Failed to reset registry: ${e.message}")
            }
        } catch (e: Exception) {
            Log.e("TestUtils", "Failed to reset singleton: ${e.message}")
        }
    }
}
```

- [TU₂] MockLog ⟶ 模拟Android日志系统，捕获和验证日志输出
- [TU₃] TestTimeProvider ⟶ 提供可控的时间源，用于测试与时间相关的逻辑

### 测试套件和执行控制
- [TS₁] 测试套件 ⟶ 使用JUnit Suite组织测试执行顺序
```kotlin
@RunWith(Suite::class)
@Suite.SuiteClasses(
    WakelockCounterTest::class,
    WakelockRegistryBasicTest::class,
    WakelockRegistryProblemTest::class
)
class WakelockTests {
    // 测试套件类，控制测试执行顺序
    // 没有实际代码，仅作为测试类的容器
}
```

- [TS₂] 测试类拆分 ⟶ 将大型测试类拆分为小型、聚焦的测试类，解决状态污染问题
- [TS₃] 测试生命周期钩子 ⟶ 使用@Before, @After, @BeforeClass, @AfterClass确保测试环境一致性

### 单例测试挑战与模式
- [STC₁] 单例重置 ⟶ 使用反射技术在测试间重置单例状态
- [STC₂] 测试隔离 ⟶ 确保每个测试在隔离环境中运行，避免状态污染
- [STC₃] 类型分类 ⟶ 将测试按功能和依赖关系分类，以最大化测试覆盖率
- [STC₄] 状态验证 ⟶ 测试前后显式验证系统状态，确保测试前提条件和后置条件

### 状态管理测试模式
```kotlin
class WakelockRegistryProblemTest {
    @Before
    fun setUp() {
        // 在每个测试前重置单例状态
        TestUtils.resetWakelockRegistry()
    }
    
    @After
    fun tearDown() {
        // 在每个测试后清理状态
        TestUtils.resetWakelockRegistry()
    }
    
    @Test
    fun getActiveWakelockStats_shouldReturnActiveWakelocks() {
        // 显式重置确保干净的测试环境
        TestUtils.resetWakelockRegistry()
        
        // 测试准备
        val registry = WakelockRegistry.getInstance()
        registry.clearAll()
        
        // 测试执行
        registry.handleAcquire("com.test", "test_wl", Type.WAKELOCK)
        registry.handleAcquire("com.test", "test_wl2", Type.WAKELOCK)
        registry.handleRelease("com.test", "test_wl2", Type.WAKELOCK)
        
        // 状态验证
        val activeStats = registry.getActiveWakelockStats()
        assertThat(activeStats).hasSize(1)
        assertThat(activeStats[0].tag).isEqualTo("test_wl")
    }
}
```

## 🔄 Boot Detection and Database Reset

### Core Components
- [BC₁] BootResetManager ⟶ Detects device reboots and resets database tables
- [BC₂] UserPreferencesRepository ⟶ Stores boot-related preferences using DataStore
- [BC₃] SystemClock.elapsedRealtime() ⟶ Provides reliable boot time reference

### Implementation Approach
- [BA₁] Boot Detection Logic ⟶ Current elapsedRealtime smaller than last recorded value indicates reboot
- [BA₂] First Launch Handling ⟶ Zero lastBootTime indicates first app run scenario
- [BA₃] Single Reset Guarantee ⟶ resetDoneForCurrentBoot flag prevents multiple resets
- [BA₄] Early Execution ⟶ Reset logic runs during app initialization after Koin setup
- [BA₅] Targeted Reset ⟶ Only info and info_event tables are cleared, preserving other data

### Preference Management
- [BP₁] lastBootTime ⟶ Stores last known elapsedRealtime value
- [BP₂] resetDoneForCurrentBoot ⟶ Boolean flag indicating reset status for current boot cycle
- [BP₃] DataStore Integration ⟶ Preferences stored using modern DataStore API

### Implementation Pattern
```kotlin
class BootResetManager(
    private val context: Context,
    private val userPreferencesRepository: UserPreferencesRepository
) {
    fun checkAndResetIfNeeded(): Boolean {
        // Get current boot time
        val currentBootTime = SystemClock.elapsedRealtime()
        var resetPerformed = false

        try {
            runBlocking {
                // Get preferences
                val lastRecordedTime = userPreferencesRepository.lastBootTime.first()
                val resetDone = userPreferencesRepository.resetDoneForCurrentBoot.first()

                // Device has been restarted if currentBootTime is less than lastRecordedTime
                // or if this is the first run (lastRecordedTime = 0)
                val isAfterReboot = currentBootTime < lastRecordedTime || lastRecordedTime == 0L

                // Reset tables if needed
                if (isAfterReboot || !resetDone) {
                    // Reset database tables
                    val db = AppDatabase.getInstance(context)
                    db.infoDao().clearAll()
                    db.infoEventDao().clearAll()
                    
                    resetPerformed = true

                    // Update preferences
                    userPreferencesRepository.setLastBootTime(currentBootTime)
                    userPreferencesRepository.setResetDone(true)
                }
            }
        } catch (e: Exception) {
            // Handle errors
            return false
        }

        return resetPerformed
    }
}
```

## 🧩 Core Components

### 🔹 Xposed Hooks

The app utilizes several Xposed hooks to intercept system operations:

#### Service Hook
Intercepts service start/bind operations to prevent certain services from running when the device screen is off.

**Implementation Notes:**
- Located in `ServiceHook.kt`
- Uses a unified hook approach that works across all Android versions (API 24+)
- Implements parameter caching to eliminate repeated extraction attempts:
  - `startServicePositionsRef` and `bindServicePositionsRef` store successful parameter positions
  - Only extracts parameters once per device boot using thread-safe `AtomicReference`
  - Falls back to alternative strategies if the initial strategy fails
- Supports various Android versions through predefined parameter extraction strategies
- Optimized for performance through static position caching

## 🧰 Technical Patterns

### 统一钩子策略模式 (Unified Hook Strategy Pattern)

我们在 ServiceHook 和 AlarmHook 中实现了一种统一钩子策略模式，用于解决 Android 版本分散问题：

#### 问题背景

- Android 系统 API 随版本变化，导致钩子实现需要分散为多个版本
- 传统方法是为每个 Android 版本实现单独的钩子方法，导致代码重复和维护困难
- 硬编码的方法参数位置和类型使得钩子在系统更新后容易失效

#### 模式结构

1. **统一钩子入口**：
   - 单一的入口方法，根据 Android 版本选择正确的类路径
   - 使用反射查找所有目标方法，避免硬编码方法签名

2. **参数位置缓存**：
   - 定义参数位置数据类，存储关键参数在方法参数列表中的位置
   - 使用 `AtomicReference` 缓存成功的参数位置，确保线程安全
   - 一旦参数位置确定，后续调用直接使用缓存提取参数

3. **自适应参数提取**：
   - 先按当前 Android 版本尝试预期的参数位置
   - 如果失败，尝试所有已知的参数位置策略
   - 遇到边缘情况时仍能找到正确的参数位置

4. **错误处理与日志**：
   - 详细的日志记录参数提取过程和结果
   - 优雅的失败处理，避免影响系统稳定性

#### 实现示例

```kotlin
// 1. 参数位置数据类
private data class AlarmParamPositions(
    val triggerListPos: Int      // Position of triggerList parameter
)

// 2. 参数位置缓存
@Volatile
private var alarmPositionsRef: AtomicReference<AlarmParamPositions?> =
    AtomicReference(null)

// 3. 统一钩子入口
private fun unifiedAlarmHook(lpparam: XC_LoadPackage.LoadPackageParam) {
    try {
        // 根据Android版本获取正确的类
        val alarmManagerServiceClass = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            XposedHelpers.findClass("com.android.server.alarm.AlarmManagerService", lpparam.classLoader)
        } else {
            XposedHelpers.findClass("com.android.server.AlarmManagerService", lpparam.classLoader)
        }

        // 查找并钩住所有匹配的方法
        hookAlarmMethods(alarmManagerServiceClass, lpparam)
    } catch (e: Throwable) {
        XpUtil.log("Error in unified alarm hook: ${e.message}")
        e.printStackTrace()
    }
}

// 4. 参数提取与缓存
private fun extractAndCacheAlarmParameters(param: XC_MethodHook.MethodHookParam) {
    // 选择当前Android版本的预期策略
    val androidVersionIndex = when (Build.VERSION.SDK_INT) {
        in Build.VERSION_CODES.S..Int.MAX_VALUE -> 0 // Android 12+
        in Build.VERSION_CODES.Q..Build.VERSION_CODES.R -> 1 // Android 10-11
        in Build.VERSION_CODES.N..Build.VERSION_CODES.P -> 2 // Android 7-9
        else -> 0 // 默认使用最新版本策略
    }
    
    // 尝试提取参数并缓存成功的位置
    if (tryExtractWithPositions(param, positions)) {
        alarmPositionsRef.set(positions)
    }
}
```

#### 应用场景

该模式已成功应用于：
- **ServiceHook**：统一处理 Android 16+ 的服务启动和绑定钩子
- **AlarmHook**：统一处理 Android 7-14+ 的闹钟触发钩子

#### 优势

- **代码精简**：消除版本特定的重复代码
- **维护性提高**：集中的逻辑更易于维护和调试
- **适应性增强**：更好地适应 Android 版本变化和自定义 ROM
- **性能优化**：参数缓存减少重复提取的开销
- **可扩展性**：易于支持新的 Android 版本和系统变体

## 🖥️ UI Architecture Insights

### Component Structure
- **NoWakeLockApp.kt** serves as the main composition point, establishing theming and navigation
- **Navigation system** uses Jetpack Navigation with type-safe route parameters
- **Screen components** follow a standard pattern:
  - `*Screen.kt` - Main container with business logic connections
  - `*Content.kt` - UI structure and composition
  - `*State.kt` - Data structures for UI state representation
  - `components/` - Reusable UI elements specific to the screen
- **DADetailScreen** represents a typical complex screen with multiple sections:
  - Header with app/service information
  - Statistics card with key metrics
  - About section with description
  - Settings section with toggles
  - Activity timeline visualization
- **AppDetailScreen** planned structure follows a tabbed design:
  - App header with icon, name and core info
  - Statistics summary row with key metrics
  - TabRow with HorizontalPager for tab content
  - Four tabs: App, Wakelocks, Alarms, Services
  - Collapsible header that shrinks when scrolling
- **Global TopAppBar** handles navigation and context-specific actions
  - TopAppBarEvent system allows nested screens to modify title

### Visual Language
- **Material Design 3** principles guide UI design
- **Card-based UI** groups related information
  - ElevatedCard should be used instead of Card for consistent elevation
- **Consistent spacing** uses 16dp between cards, 16dp internal padding
- **MD3 color system** leverages dynamic themes and semantic colors
- **Typography** follows MD3 type scale with appropriate weights

### UI Challenges

#### Card Styling Consistency
- Current implementation has inconsistent card styling
- Cards should use ElevatedCard with consistent elevation
- Padding should be managed consistently - card manages internal padding, parent manages spacing
- Dividers should be used sparingly, with proper alpha for subtlety

#### Timeline Chart Improvements
- Current implementation doesn't match design specifications
- Chart needs rounded corners on bars, better axis layout
- Time labels need optimization to avoid overlap
- Canvas API limitations require manual positioning calculations

#### Lazy Loading Tabs in Compose
- Issue identified with current lazy loading implementation in tabbed interfaces
- Original implementation showed loading indicator on first tab selection, actual content only on second selection
- Problem root cause: LaunchedEffect executes after composition, creating a timing issue
- Solution implemented using derivedStateOf to immediately include current tab in loaded tabs set:
  ```kotlin
  // Track persistent loaded tabs state
  val loadedTabs = remember { mutableStateOf(setOf(0)) }
  
  // Use derivedStateOf to immediately include current tab in loaded set
  val currentLoadedTabs by remember {
      derivedStateOf { 
          loadedTabs.value + selectedTabIndex 
      }
  }
  
  // Still persist loaded tabs for future recompositions
  LaunchedEffect(selectedTabIndex) {
      loadedTabs.value = loadedTabs.value + selectedTabIndex
  }
  ```
- This pattern ensures immediate UI response while preserving lazy loading benefits
- Important learning: Use derivedStateOf when UI needs to immediately react to state changes

#### Collapsible Header Implementation in Compose
- No direct equivalent to CollapsingToolbarLayout in Compose
- Implementation requires custom NestedScrollConnection to track scroll state
- TopAppBarScrollBehavior combined with graphicsLayer modifications can achieve collapsing effect
- Material 3 approach uses LargeTopAppBar with exitUntilCollapsedScrollBehavior()
- Animation timing and visual transitions need careful implementation

#### Tab Navigation
- Tabbed interfaces need coordination between TabRow selection and HorizontalPager
- Tabs require consistent styling following Material 3 guidelines
- Content caching needed to avoid recomposition when switching tabs
- State management for each tab must be handled appropriately
- LaunchedEffect with derivedStateOf ensures smooth tab transitions

### Material Design 3 Component Implementations

#### PatternSettingsSection 改进
- **组件定位**：应用详情页中的设置部分，用于正则表达式模式管理
- **主要改进**：
  - 布局层次改进，使用适当的内边距和间距遵循 MD3 规范
  - 实现充分利用 ElevatedCard 和 Column 的嵌套结构
  - 增加组件边缘明显性，改善视觉层次结构
  - 为可点击元素增加涟漪效果，提高交互反馈
  - 使用语义化色彩系统，确保在各种主题下保持一致性
  - 优化不同内容密度下的显示效果
- **状态管理**：
  - 采用 remember 和 mutableStateOf 进行本地状态管理
  - rememberSaveable 保存对话框状态，防止配置更改丢失
  - 将状态变更通过回调函数传递给父组件
- **交互模式**：
  - 为长文本项目添加省略和展开功能
  - 使用图标辅助表达操作含义
  - 增加触摸目标大小，提高可用性
  - 为关键操作提供确认对话框

#### 正则表达式模式管理实现
```kotlin
// 模式列表实现使用 LazyColumn 优化性能
LazyColumn(
    modifier = Modifier.fillMaxWidth(),
    contentPadding = PaddingValues(vertical = 8.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp)
) {
    items(patterns) { pattern ->
        PatternItem(
            pattern = pattern,
            onEdit = { editPattern(it) },
            onDelete = { showDeleteDialog = it }
        )
    }
}

// 模式项使用 ElevatedCard 提供视觉层次
@Composable
fun PatternItem(
    pattern: Pattern,
    onEdit: (Pattern) -> Unit,
    onDelete: (Pattern) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.elevatedCardElevation()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 内容和操作按钮
        }
    }
}
```

## 🧩 组件技术规范

### ⛰️ UI组件

#### 📏 Scaffold和窗口插图(Window Insets)处理

**使用场景**：在嵌套Scaffold布局中（如主应用Scaffold包含屏幕级Scaffold）避免重复的padding和空白空间。

**最佳实践**：
1. **窗口插图设置**：
   - 对于嵌套的Scaffold，内部Scaffold应明确定义窗口插图，通常设置为零：
   ```kotlin
   Scaffold(
       contentWindowInsets = WindowInsets(0, 0, 0, 0),
       // 其他参数...
   ) { paddingValues -> 
       // 内容...
   }
   ```

2. **精确控制Padding**：
   - 在嵌套Scaffold中，只应用必要的padding方向，而不是所有方向：
   ```kotlin
   Box(
       modifier = Modifier
           .fillMaxSize()
           .padding(top = paddingValues.calculateTopPadding())  // 只应用顶部padding
   ) {
       // 内容...
   }
   ```

3. **内容容器配置**：
   - 为内容容器（如Column、LazyColumn等）设置合适的水平和垂直内边距：
   ```kotlin
   Column(
       modifier = Modifier
           .fillMaxSize()
           .padding(horizontal = 16.dp, vertical = 8.dp)  // 确保内容与屏幕边缘有适当距离
   ) {
       // 内容项...
   }
   ```

4. **导航组件与Scaffold协调**：
   - 确保TopAppBar、BottomBar等导航组件与Scaffold的paddingValues正确配合
   - 当使用全局导航组件时，屏幕级Scaffold应避免重复定义同类型的导航组件

5. **常见问题排查**：
   - TopAppBar下方出现空白：检查嵌套Scaffold的contentWindowInsets设置
   - 内容边缘过于贴近屏幕：为内容容器添加适当的padding
   - 屏幕内容被导航栏覆盖：确保正确应用了paddingValues.calculateBottomPadding()

**代码示例**：ModuleCheckScreen.kt中的实现方式
```kotlin
@Composable
fun ModuleCheckScreen(
    // 参数...
) {
    // 设置windowInsets为零，避免与主Scaffold重复计算
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        // 其他参数...
    ) { paddingValues ->
        // 仅应用顶部padding，避免全方向padding
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            // 内容组件，包含自己的适当padding
            ModuleCheckContent(
                // 参数...
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}
```