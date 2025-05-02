# σ₂: System Patterns
*v1.0 | Created: 2025-04-15 | Updated: 2025-05-01*
*Π: 🏗️DEVELOPMENT | Ω: 🔍R*

## 🏛️ Architecture Overview
NoWakeLock采用现代化Android应用架构，整合了MVVM架构模式、Compose UI框架、Koin依赖注入和Room数据库。项目关注点分离清晰，上层UI通过ViewModel与底层数据源交互，同时通过Xposed框架实现系统级钩子功能。

## 🧱 Component Structure
- [C₁] UI Layer ⟶ Compose-based screens, navigation, and components with MD3 styling
- [C₂] ViewModel Layer ⟶ Screen-specific ViewModels for data transformation and business logic
- [C₃] Repository Layer ⟶ Data access abstraction for wakelocks, alarms, and services
- [C₄] Database Layer ⟶ Room entities, DAOs, and converters for local storage
- [C₅] Xposed Layer ⟶ System-level hooks to monitor wakelocks, alarms, and services
- [C₆] Model Layer ⟶ Data classes representing wakelocks, alarms, and application information
- [C₇] Utility Layer ⟶ Helper functions, extensions, and shared tools
- [C₈] Visualization Layer ⟶ Custom Canvas-based charts and data visualization components
- [C₉] Settings Layer ⟶ Context-based state management with persistence and UI components

## 🔄 Design Patterns
- [P₁] MVVM ⟶ For separation of UI and business logic
- [P₂] Repository Pattern ⟶ For data access abstraction
- [P₃] Dependency Injection ⟶ Using Koin for service locator pattern
- [P₄] Observer Pattern ⟶ State management with Compose state and Flow
- [P₅] Factory Pattern ⟶ For creating instances of repositories and databases
- [P₆] Adapter Pattern ⟶ For transforming data between layers
- [P₇] Strategy Pattern ⟶ Different hooking strategies for different Android versions
- [P₈] Composition Pattern ⟶ Building complex UI components from smaller specialized components
- [P₉] Provider Pattern ⟶ Context-based state management for settings
- [P₁₀] Reducer Pattern ⟶ Structured state updates for complex settings
- [P₁₁] Hook Pattern ⟶ Custom hooks for simplified state access
- [P₁₂] Command Pattern ⟶ Action creators for settings state modifications

## 🔌 Key Interfaces
- [I₁] XposedModule ⟶ Entry point for Xposed framework integration
- [I₂] Hook Implementations ⟶ Classes for monitoring specific system components
- [I₃] Repository Interfaces ⟶ Data access contracts
- [I₄] ViewModel Factories ⟶ For creating ViewModels with dependencies
- [I₅] Navigation Routes ⟶ For screen navigation
- [I₆] DAOs ⟶ Data Access Objects for database operations
- [I₇] UI Components ⟶ Reusable UI elements for consistency
- [I₈] Visualization Components ⟶ Chart and data display interfaces
- [I₉] Settings Provider ⟶ Context provider for settings management
- [I₁₀] Settings Hook ⟶ Custom hook for accessing settings state
- [I₁₁] Settings Reducer ⟶ Action-based state management
- [I₁₂] Settings UI Components ⟶ Reusable settings controls

## 🔐 Critical Implementation Paths
- [Path₁] Wakelock Detection ⟶ WakelockHook → Record → Repository → Database
- [Path₂] Wakelock Display ⟶ Database → Repository → ViewModel → UI
- [Path₃] Wakelock Control ⟶ UI → ViewModel → Repository → XpNSP → System
- [Path₄] Alarm Detection ⟶ AlarmHook → Record → Repository → Database
- [Path₅] Service Detection ⟶ ServiceHook → Record → Repository → Database
- [Path₆] User Switching ⟶ UI → ViewModel → Repositories → Database Queries
- [Path₇] Data Backup ⟶ UI → ViewModel → Repository → Serialization → Storage
- [Path₈] Activity Visualization ⟶ Database → Repository → ViewModel → TimelineChart → Canvas
- [Path₉] Settings Management ⟶ UI → Settings Hook → Settings Reducer → Context Provider → LocalStorage

## 🧩 Architectural Insights
- [Insight₁] The application uses different hook implementations based on Android version to maintain compatibility across devices
- [Insight₂] The UI is being reconstructed with Material Design 3 while preserving core functionality
- [Insight₃] Multi-user support is implemented through userId parameters in database queries
- [Insight₄] The app uses Koin for dependency injection with modular organization
- [Insight₅] Edge-to-edge UI implementation uses the new enableEdgeToEdge() API instead of deprecated SystemUiController
- [Insight₆] The architecture follows modern Android development patterns with Compose and viewModels
- [Insight₇] Data visualization components use Canvas API instead of third-party libraries to minimize dependencies
- [Insight₈] Settings management uses a Context API approach for global state access
- [Insight₉] The app uses LocalStorage for persistent settings with JSON serialization
- [Insight₁₀] Settings UI is built through component composition for flexibility and maintenance

## 🖼️ UI Architecture
- [UI₁] Screen Organization ⟶ UI is organized by feature in separate screen packages (apps, wakelocks, alarms, services, settings)
- [UI₂] Component Library ⟶ Reusable components in dedicated components package (UserSwitcher, BottomNavBar, EmptyView, etc.)
- [UI₃] Type-Safe Navigation ⟶ Uses Kotlin serialization for passing complex data between screens
- [UI₄] Nested Components ⟶ Complex screens are built from smaller composable components in screen-specific packages
- [UI₅] Material 3 Integration ⟶ Uses Material 3 components with system theming
- [UI₆] Screen State Management ⟶ Uses collectAsState with Flow for reactive UI updates
- [UI₇] Card-Based Organization ⟶ Information is grouped into distinct ElevatedCard components for clear visual hierarchy

## 🧭 Navigation System
- [Nav₁] Bottom Navigation ⟶ Main app navigation with tab-based structure
- [Nav₂] Central NavHost ⟶ Single NavHost in main app composable manages all navigation
- [Nav₃] Type-Safe Parameters ⟶ Serializable data classes for passing complex data between screens
- [Nav₄] Detail Navigation ⟶ Detail screens with back navigation and shared data
- [Nav₅] State Preservation ⟶ Navigation preserves state during tab switching
- [Nav₆] Multi-level Navigation ⟶ Supports navigation to detail screens while maintaining tab structure 

## 📊 Data Visualization Patterns
- [Viz₁] Canvas-Based Charts ⟶ Custom drawing with Compose Canvas API instead of third-party libraries
- [Viz₂] Material Design 3 Styling ⟶ Charts follow MD3 color scheme and design principles
- [Viz₃] Responsive Layout ⟶ Visualizations adapt to different screen sizes and orientations
- [Viz₄] Data Transformation ⟶ Raw data is transformed in ViewModel before visualization
- [Viz₅] Remember Optimization ⟶ Calculations are cached using remember to improve performance
- [Viz₆] Component Encapsulation ⟶ Visualization logic is encapsulated in dedicated components
- [Viz₇] State-Driven Rendering ⟶ Chart appearance dynamically responds to data state changes 

## ⚙️ Settings Architecture Patterns
- [Set₁] Context Provider ⟶ Top-level state container using React Context API pattern
- [Set₂] Settings Hook ⟶ Custom hook for accessing settings state throughout the application
- [Set₃] Reducer Pattern ⟶ Action-based state updates for structured management
- [Set₄] JSON Serialization ⟶ Settings stored as JSON in LocalStorage
- [Set₅] Versioned Storage ⟶ Schema versioning for handling format changes
- [Set₆] Nested Access ⟶ Path-based access to nested settings (e.g., "appearance.theme.mode")
- [Set₇] Default Fallback ⟶ Default settings provided when storage is empty or corrupted
- [Set₈] UI Component Library ⟶ Reusable settings control components (Input, Switch, Select, etc.)
- [Set₉] Validation System ⟶ Type-specific validation rules for settings values
- [Set₁₀] Settings Categorization ⟶ Logical grouping of related settings
- [Set₁₁] Search Functionality ⟶ Dynamic filtering of settings for improved discovery
- [Set₁₂] Conditional Dependencies ⟶ Settings that show/hide based on other setting values 

## 🧪 Testing Architecture Patterns

### 单例测试挑战与解决方案
- [Test₁] **单例重置模式** ⟶ 使用反射技术在测试间重置单例状态，确保测试隔离
  ```kotlin
  // TestUtils.kt
  fun resetWakelockRegistry() {
      // 使用反射访问和修改单例字段
      val registryClass = WakelockRegistry::class.java
      val instanceField = registryClass.getDeclaredField("instance")
      instanceField.isAccessible = true
      
      // 移除final修饰符
      val modifiersField = Field::class.java.getDeclaredField("modifiers")
      modifiersField.isAccessible = true
      modifiersField.setInt(instanceField, instanceField.modifiers and Modifier.FINAL.inv())
      
      // 设置单例为null，强制重新创建
      instanceField.set(null, null)
  }
  ```

- [Test₂] **测试类拆分策略** ⟶ 将大型测试类拆分为小型、聚焦的测试类，提高测试可维护性和隔离性
  ```
  原始结构:
  WakelockRegistryTest
  ├── 基本功能测试
  └── 复杂边缘情况测试
  
  优化结构:
  ├── WakelockRegistryBasicTest (基本功能)
  └── WakelockRegistryProblemTest (复杂边缘情况)
  ```

- [Test₃] **测试套件组织** ⟶ 使用JUnit Suite控制测试执行顺序，解决测试间依赖问题
  ```kotlin
  @RunWith(Suite::class)
  @Suite.SuiteClasses(
      WakelockCounterTest::class,
      WakelockRegistryBasicTest::class,
      WakelockRegistryProblemTest::class
  )
  class WakelockTests {}
  ```

### 测试生命周期模式
- [Test₄] **测试环境准备与清理** ⟶ 使用@Before和@After确保测试环境一致性
  ```kotlin
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
  ```

- [Test₅] **显式状态重置** ⟶ 在测试方法内部显式重置状态，确保测试独立性
  ```kotlin
  @Test
  fun someTest() {
      // 显式重置确保干净的测试环境
      TestUtils.resetWakelockRegistry()
      
      // 测试逻辑...
  }
  ```

### 断言模式
- [Test₆] **流畅断言语法** ⟶ 使用Truth库提供更具可读性的断言
  ```kotlin
  // 传统JUnit断言
  assertEquals(1, activeStats.size)
  assertEquals("test_wl", activeStats[0].tag)
  
  // Truth流畅断言
  assertThat(activeStats).hasSize(1)
  assertThat(activeStats[0].tag).isEqualTo("test_wl")
  ```

- [Test₇] **多重断言分组** ⟶ 对相关属性进行分组断言，提高测试可读性
  ```kotlin
  @Test
  fun handleAcquire_firstAcquisition_shouldReturnZero() {
      val registry = WakelockRegistry.getInstance()
      val duration = registry.handleAcquire("com.test", "test_wl", Type.WAKELOCK)
      
      // 分组断言相关状态
      assertThat(duration).isEqualTo(0L)
      assertThat(registry.getActiveWakelockStats()).hasSize(1)
      assertThat(registry.getTotalTrackedWakelocks()).isEqualTo(1)
  }
  ```

### 测试命名与组织模式
- [Test₈] **BDD风格命名** ⟶ 使用given_when_then或feature_scenario_expected结构命名测试方法
  ```kotlin
  // 方法命名: 功能_条件_预期结果
  @Test
  fun handleAcquire_firstAcquisition_shouldReturnZero() { ... }
  
  @Test
  fun getActiveWakelockStats_shouldReturnActiveWakelocks() { ... }
  ```

- [Test₉] **测试分类与归类** ⟶ 将相关测试归类到专门的测试类中
  ```
  测试类组织:
  ├── WakelockCounterTest (单个计数器功能)
  ├── WakelockRegistryBasicTest (注册表基本功能)
  └── WakelockRegistryProblemTest (注册表边缘情况)
  ```

### 时间依赖测试模式
- [Test₁₀] **时间依赖隔离** ⟶ 使用可控时间源代替System.currentTimeMillis()
  ```kotlin
  // 定义时间提供者接口
  interface TimeProvider {
      fun currentTimeMillis(): Long
  }
  
  // 测试实现
  class TestTimeProvider : TimeProvider {
      var currentTime: Long = 0
      
      override fun currentTimeMillis(): Long = currentTime
      
      fun advanceBy(millis: Long) {
          currentTime += millis
      }
  }
  ```

- [Test₁₁] **时间跳转测试** ⟶ 通过控制时间跳转测试与时间相关的功能
  ```kotlin
  @Test
  fun wakelockDuration_shouldCalculateCorrectly() {
      val timeProvider = TestTimeProvider()
      val counter = WakelockCounter(timeProvider)
      
      // 设置初始时间
      timeProvider.currentTime = 1000L
      counter.increment()
      
      // 时间跳转
      timeProvider.advanceBy(5000L)
      counter.decrement()
      
      // 验证持续时间计算
      assertThat(counter.getTotalDuration()).isEqualTo(5000L)
  }
  ```

## 🧩 Component Design Patterns

### 组件拆分与组合
- **大型组件拆分**: 将复杂UI组件分解为小型、专一职责的可组合函数
  - 示例: TopAppBar拆分为SearchModeTopBar和StandardModeTopBar
  - 优势: 提高可读性、可维护性和可测试性
  
- **组件层次结构**:
  ```
  ParentComponent
  ├── ControlComponent
  │   ├── SubControlA
  │   └── SubControlB
  └── ContentComponent
      ├── ContentSectionA
      └── ContentSectionB
  ```
  
- **组件命名约定**:
  - 使用描述性命名，反映组件功能
  - 相关组件使用共同前缀
  - 私有/内部组件使用private修饰
  
### 状态管理模式
- **状态集中化**:
  - 使用数据类封装组件状态
  - 从路由和参数派生UI状态，而非分散的条件判断
  - 示例: `TopAppBarUiState`封装TopAppBar的所有UI状态
  
- **有状态/无状态组件分离**:
  - 顶层组件管理状态
  - 子组件设计为无状态，通过参数接收所需数据
  
- **状态记忆化**:
  - 使用`remember`缓存计算结果
  - 仅在依赖项变化时重新计算

### 工具类模式
- **功能封装**:
  - 将通用逻辑抽取为工具类或辅助函数
  - 示例: `RouteUtils`封装路由判断逻辑
  
- **命名空间组织**:
  - 相关工具函数组织到同一对象或文件中
  - 使用描述性命名表达功能意图

### 样式与主题模式
- **样式集中管理**:
  - 将颜色、形状等样式属性抽取为可重用函数
  - 示例: `standardTopAppBarColors()`提供一致的颜色设置
  
- **主题一致性**:
  - 组件使用MaterialTheme属性而非硬编码值
  - 通过样式函数确保跨组件视觉一致性 