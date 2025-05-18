# σ₂: System Patterns
*v1.0 | Created: 2025-04-15 | Updated: 2025-05-20*
*Π: 🏗️DEVELOPMENT | Ω: ⚙️E*

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

## 🔐 Critical Implementation Paths
- [Path₁] Wakelock Detection ⟶ WakelockHook → Record → Repository → Database
- [Path₂] Wakelock Display ⟶ Database → Repository → ViewModel → UI
- [Path₃] Wakelock Control ⟶ UI → ViewModel → Repository → XpNSP → System
- [Path₄] Alarm Detection ⟶ AlarmHook → Record → Repository → Database
- [Path₅] Service Detection ⟶ ServiceHook → Record → Repository → Database
- [Path₆] User Switching ⟶ UI → ViewModel → Repositories → Database Queries
- [Path₇] Data Backup ⟶ UI → ViewModel → Repository → Serialization → Storage
- [Path₈] Activity Visualization ⟶ Database → Repository → ViewModel → TimelineChart → Canvas

## 🧩 Core Architectural Insights
- [Insight₁] The application uses different hook implementations based on Android version for compatibility
- [Insight₂] Multi-user support is implemented through userId parameters in database queries
- [Insight₃] The app uses Koin for dependency injection with modular organization
- [Insight₄] Edge-to-edge UI implementation uses the new enableEdgeToEdge() API
- [Insight₅] Data visualization components use Canvas API for minimal dependencies
- [Insight₆] Settings management uses a Context API approach for global state access
- [Insight₇] The app uses LocalStorage for persistent settings with JSON serialization

## 🖼️ UI Architecture
### Screen Organization
- Feature-based organization in separate screen packages
- Shared component library in dedicated components package
- Type-safe navigation using Kotlin serialization
- Material 3 component integration with system theming
- Screen state management using collectAsState with Flow

### Navigation System
- Bottom Navigation for main app navigation with tab-based structure
- Central NavHost in main app composable manages all navigation
- Type-Safe Parameters using serializable data classes
- Detail Navigation with back navigation and shared data
- State Preservation during tab switching

### Data Visualization
- Canvas-Based Charts using Compose Canvas API
- Material Design 3 Styling for visual consistency
- Responsive Layout adapting to different screen sizes
- Data Transformation in ViewModel before visualization
- Remember Optimization caching calculations

## 🧪 Testing Architecture

### 单例测试挑战与解决方案
- **单例重置模式**：使用反射技术在测试间重置单例状态，确保测试隔离
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

- **测试类拆分策略**：将大型测试类拆分为小型、聚焦的测试类，提高可维护性
  ```
  优化结构:
  ├── WakelockRegistryBasicTest (基本功能)
  └── WakelockRegistryProblemTest (复杂边缘情况)
  ```

- **测试套件组织**：使用JUnit Suite控制测试执行顺序，解决测试间依赖
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
- **测试环境准备与清理**：使用@Before和@After确保测试环境一致性
- **显式状态重置**：在测试方法内部显式重置状态，确保独立性
- **流畅断言语法**：使用Truth库提供更具可读性的断言

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

## 🧠 Performance Optimization Patterns

### 🔄 Initialization-Time Caching

**Pattern**: Perform expensive operations once at initialization and cache the results for future use.

**Application in NoWakeLock**:
- **ServiceHook Parameter Caching**: The app caches parameter positions for hooked methods after first successful extraction
  - Implemented with `AtomicReference<ServiceParamPositions?>` for thread safety
  - Performs parameter extraction only once per device boot
  - Significantly reduces CPU usage for frequently called service operations

**Implementation Example**:
```kotlin
// Check cached positions before attempting extraction
val positions = startServicePositionsRef.get()
if (positions != null) {
    // Use cached positions to extract parameters
    extractParametersFromCache(param, positions)
} else if (!startServiceHookFailed) {
    // First-time extraction and caching
    extractAndCacheStartServiceParameters(param)
}
```

**Benefits**:
- Reduces repeated computation overhead
- Improves response time for frequent operations
- Decreases battery consumption 

## 🔄 加载与缓存模式

### 统一数据加载模式 (Universal Data Loading Pattern)

```
┌───────────┐     ┌──────────────┐     ┌───────────┐     ┌─────────────┐
│ UI 事件   │────▶│ 统一加载方法 │────▶│ 加载逻辑  │────▶│ 数据仓库    │
└───────────┘     └──────────────┘     └───────────┘     └─────────────┘
                        │                                       │
                        ▼                                       ▼
                  ┌──────────┐                           ┌─────────────┐
                  │ 状态更新 │◀─────────────────────────│ 缓存检查    │
                  └──────────┘                           └─────────────┘
```

**特点**：
- 集中管理所有加载请求
- 防抖处理，取消进行中的请求
- 区分关键数据和非关键数据
- 错误处理和恢复机制

**实现**：
- `triggerDataLoad(source: LoadingSource, immediate: Boolean)` 方法
- 使用 Job 跟踪和取消任务
- 适当设置防抖延迟（200ms）

### 多级缓存模式 (Multi-tier Caching Pattern)

```
┌──────────┐   ┌──────────┐   ┌──────────┐
│ UI 组件   │──▶│ 内存缓存 │──▶│ 数据库   │
└──────────┘   └──────────┘   └──────────┘
                     │              │
                     │              ▼
                     │         ┌──────────┐
                     └────────▶│ API/系统 │
                               └──────────┘
```

**特点**：
- 多层次缓存设计
- 自动过期机制
- 缓存失效触发条件
- 缓存大小管理

**实现**：
- 内存缓存：`Map<String, Pair<Data, Timestamp>>`
- 缓存键生成：`generateCacheKey(sortBy, userId, filter)`
- 过期时间：30秒
- 在数据变更时主动清除
- 最多保留20条记录，LRU淘汰策略

### 响应式数据流模式 (Reactive Data Flow Pattern)

```
┌──────────┐   ┌──────────┐   ┌──────────┐   ┌──────────┐
│ 数据源   │──▶│ 转换操作 │──▶│ 过滤操作 │──▶│ 收集点   │
└──────────┘   └──────────┘   └──────────┘   └──────────┘
                                                  │
┌──────────┐                                      │
│ UI 更新  │◀─────────────────────────────────────┘
└──────────┘
```

**特点**：
- 使用 Flow API 构建响应式数据流
- 应用适当的操作符优化流处理
- 数据流终点与UI状态更新关联

**实现**：
- `conflate()` - 确保处理最新数据
- `distinctUntilChanged()` - 避免处理相同数据
- `debounce()` - 高频更新场景
- 映射和转换操作连接数据源和UI

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

## 🧠 Performance Optimization Patterns

### 🔄 Initialization-Time Caching

**Pattern**: Perform expensive operations once at initialization and cache the results for future use.

**Application in NoWakeLock**:
- **ServiceHook Parameter Caching**: The app caches parameter positions for hooked methods after first successful extraction
  - Implemented with `AtomicReference<ServiceParamPositions?>` for thread safety
  - Performs parameter extraction only once per device boot
  - Significantly reduces CPU usage for frequently called service operations

**Implementation Example**:
```kotlin
// Check cached positions before attempting extraction
val positions = startServicePositionsRef.get()
if (positions != null) {
    // Use cached positions to extract parameters
    extractParametersFromCache(param, positions)
} else if (!startServiceHookFailed) {
    // First-time extraction and caching
    extractAndCacheStartServiceParameters(param)
}
```

**Benefits**:
- Reduces repeated computation overhead
- Improves response time for frequent operations
- Decreases battery consumption 