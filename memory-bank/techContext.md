# σ₃: Technical Context
*v1.0 | Created: 2025-04-15 | Updated: 2025-05-05*
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

## 🔄 XPosed设置与日志控制机制

### 问题分析
- [XP₁] **静态初始化问题** ⟶ XpUtil.log在初始化时设置为BuildConfig.DEBUG，之后没有动态更新
- [XP₂] **XSharedPreferences限制** ⟶ Android高版本对XSharedPreferences权限限制更严格
- [XP₃] **不同进程间通信挑战** ⟶ 宿主应用和Xposed模块运行在不同进程空间
- [XP₄] **调试模式切换无效** ⟶ 设置切换后模块无法实时读取新设置

### 技术根源
- **SharedPreferences权限变更** ⟶ 从Android 7.0开始，跨进程文件访问受到限制
- **文件系统权限** ⟶ MODE_WORLD_READABLE标记在新版本Android中可能导致SecurityException
- **加载时机问题** ⟶ XSharedPreferences在模块加载时读取，之后需要显式刷新
- **错误处理不足** ⟶ SPTools中的异常被捕获但未记录，导致问题难以诊断

### XPosed设置读取流程
```kotlin
// 宿主应用中设置debug模式
fun updateDebugMode(enabled: Boolean) {
    SPTools.setBoolean("debug", enabled)
    _debugMode.value = enabled
}

// SPTools在宿主应用中保存设置
fun setBoolean(key: String, value: Boolean) {
    with(prefs?.edit() ?: return) {
        putBoolean(key, value)
        commit()
    }
}

// Xposed模块中读取设置
fun getDebug(): Boolean {
    return getBool("debug")
}

// XSharedPreferences实现
fun makePref(): XSharedPreferences? {
    return pref ?: synchronized(this) {
        val p = XSharedPreferences(BuildConfig.APPLICATION_ID, SPTools.SP_NAME)
        pref = if (p.file.canRead()) p else null
        pref
    }
}
```

### 解决方案
- [XPS₁] **用户指导** ⟶ 告知用户在重新安装应用后需要重启设备
- [XPS₂] **手动重载机制** ⟶ 添加主动重载XSharedPreferences的功能
- [XPS₃] **动态日志控制** ⟶ 修改XpUtil.log为动态检查而非静态初始化
- [XPS₄] **增强错误报告** ⟶ 改进错误处理以便识别设置加载失败
- [XPS₅] **提高刷新频率** ⟶ 降低XSharedPreferences的刷新间隔

### 最终决策
基于对问题的分析，决定采用文档引导方法而非对代码进行大幅改动：
1. 在设置页面添加提示，告知用户重新安装后需要重启系统
2. 在故障排除文档中添加这一限制的说明
3. 保留当前的XSharedPreferences机制，接受其局限性

这一决策基于以下考虑：
- 现代Android系统对XSharedPreferences本身的限制难以绕过
- 实现更复杂的跨进程通信机制（如ContentProvider）工作量大
- 大多数用户不会频繁重新安装应用，仅需重启一次即可解决问题

## 🛠️ Flexible ServiceHook Architecture

### Core Components
- [SC₁] FlexibleServiceHooks ⟶ 主协调器，管理startServiceLocked和bindServiceLocked的Hook
- [SC₂] FlexibleStartServiceHook ⟶ 处理所有startServiceLocked方法的Hook
- [SC₃] FlexibleBindServiceHook ⟶ 处理所有bindServiceLocked方法的Hook
- [SC₄] 参数提取策略 ⟶ 基于位置和类型的参数识别方法

### Architecture Pattern
- 模块化设计：将Hook逻辑拆分为独立、可维护的单元
- 自适应参数提取：支持不同Android版本的API变化
- 降级处理：当主要策略失败时，提供备选提取策略
- 丰富的日志记录：跟踪参数提取的每个步骤，便于调试

### Implementation Approach
- 反射发现：使用反射API查找目标方法，无需硬编码的完整方法签名
- 多种提取策略：尝试多种已知的参数位置模式，适应不同Android版本
- 类型匹配：当位置策略失败时，基于参数类型进行智能匹配
- 统一Hook接口：不同方法的Hook都使用相同的hookStartServiceLocked实现

### Key Strategies
- [KS₁] 针对startServiceLocked的提取策略：
  ```kotlin
  val strategies = listOf(
      Triple(1, 6, 8),  // Common in recent versions
      Triple(1, 6, 7),  // Common in some older versions
      Triple(1, 5, 6)   // Common in even older versions
  )
  ```

- [KS₂] 针对bindServiceLocked的提取策略：
  ```kotlin
  val strategies = listOf(
      Triple(2, 11, 12),  // Android 14+
      Triple(2, 7, 8)     // Android 12 and below
  )
  ```

### Implementation Example
```kotlin
/**
 * Flexible service hooks that handle both startServiceLocked and bindServiceLocked methods
 */
private fun flexibleServiceHooks(lpparam: XC_LoadPackage.LoadPackageParam) {
    // Hook startServiceLocked methods
    flexibleStartServiceHook(lpparam)
    
    // Hook bindServiceLocked methods
    flexibleBindServiceHook(lpparam)
}

/**
 * Flexible hook approach that hooks all methods named "startServiceLocked"
 * and extracts parameters based on common positions observed across Android versions.
 */
private fun flexibleStartServiceHook(lpparam: XC_LoadPackage.LoadPackageParam) {
    try {
        // Get the ActiveServices class
        val activeServicesClass =
            findClass("com.android.server.am.ActiveServices", lpparam.classLoader)

        // Find all methods named startServiceLocked
        val methods =
            activeServicesClass.declaredMethods.filter { it.name == "startServiceLocked" }

        XpUtil.log("Found ${methods.size} startServiceLocked methods")

        if (methods.isEmpty()) {
            XpUtil.log("No startServiceLocked methods found! Trying to discover methods...")
            discoverPotentialServiceMethods(activeServicesClass)
            return
        }

        // Hook each method found
        for (method in methods) {
            hookStartServiceLockedMethod(method, lpparam)
        }
    } catch (e: Throwable) {
        XpUtil.log("Error in flexible startServiceLocked hook: ${e.message}")
        e.printStackTrace()
    }
}

/**
 * Attempts to extract the required parameters using various strategies
 */
private fun tryExtractParameters(args: Array<Any?>): Triple<Intent, String, Int>? {
    // Common parameter positions observed across Android versions
    val strategies = listOf(
        Triple(1, 6, 8),  // Common in recent versions
        Triple(1, 6, 7),  // Common in some older versions
        Triple(1, 5, 6)   // Common in even older versions
    )

    // Try each position strategy
    for ((servicePos, packagePos, userIdPos) in strategies) {
        if (args.size > maxOf(servicePos, packagePos, userIdPos)) {
            try {
                val service = args[servicePos] as? Intent
                val callingPackage = args[packagePos] as? String
                val userId = args[userIdPos] as? Int

                if (service != null && callingPackage != null && userId != null) {
                    XpUtil.log("Successfully extracted parameters using positions: $servicePos, $packagePos, $userIdPos")
                    return Triple(service, callingPackage, userId)
                }
            } catch (e: Exception) {
                // This strategy failed, try the next one
                continue
            }
        }
    }

    // If position-based strategies failed, try type-based extraction
    return tryExtractParametersByType(args)
}
```

### Advantages
- [Adv₁] 适应性：无需预先知道确切的方法签名，可以适应未公开的Android版本
- [Adv₂] 可维护性：分离的代码单元更易于理解和修改
- [Adv₃] 可扩展性：新的参数提取策略可以轻松添加
- [Adv₄] 故障恢复：当主要提取策略失败时，提供备选方案

### Limitations
- [Lim₁] 性能开销：反射和多重尝试可能带来轻微的性能成本
- [Lim₂] 不确定性：复杂的提取策略可能在特定条件下失败
- [Lim₃] 维护复杂性：需要持续更新知识库以支持新版本的Android

### Future Improvements
- [Imp₁] 发现策略完善：添加更多启发式方法以找到可能被重命名的方法
- [Imp₂] 参数缓存：为成功的提取策略实现缓存，避免重复尝试
- [Imp₃] 自适应学习：基于成功率动态调整尝试策略的顺序
- [Imp₄] 添加单元测试：为复杂的提取逻辑创建全面的测试套件