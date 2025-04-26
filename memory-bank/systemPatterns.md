# σ₂: System Patterns
*v1.0 | Created: 2025-04-15 | Updated: 2025-04-26*
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