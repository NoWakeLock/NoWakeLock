# σ₄: Active Context
*v1.0 | Created: 2025-04-15 | Updated: 2025-04-29*
*Π: 🏗️DEVELOPMENT | Ω: ⚙️E*

## 🔮 Current Focus
实现了 AppTabContent 的设置界面，为 AppSt 创建了符合 Material Design 3 的 UI 组件，包括全局阻止开关和正则表达式模式管理。设置界面完美集成到现有的应用详情页面，保持一致的设计语言和用户体验。下一步将进一步优化标签页内容的 UI，改进视觉细节和交互体验。

## 🔄 Recent Changes
- [Change₂₈] 2025-04-29 ⟶ 实现 AppTabContent 的 AppSt 设置 UI，包括全局阻止开关和正则表达式模式管理
- [Change₂₃] 2025-04-27 ⟶ 实现 AppDetailScreen 的 Tab 内容与 DAsScreen 的集成
- [Change₂₄] 2025-04-27 ⟶ 实现 Tab 懒加载功能，提高性能
- [Change₂₅] 2025-04-27 ⟶ 实现搜索状态传递，仅影响当前选中的 Tab
- [Change₂₆] 2025-04-27 ⟶ 更新 RouteUtils 支持 AppDetail 路由的搜索功能
- [Change₂₇] 2025-04-27 ⟶ 实现从 Tab 内容到 DA 详情页的导航
- [Change₁] 2025-04-15 ⟶ RIPER framework initialization
- [Change₁₈] 2025-04-25 ⟶ Implemented SavedStateHandle in ViewModels for improved state management
- [Change₁₉] 2025-04-25 ⟶ Created parameter constant classes for type safety
- [Change₂₀] 2025-04-25 ⟶ Implemented type-based navigation with class objects
- [Change₂₁] 2025-04-25 ⟶ Fixed navigation issues with hybrid approach for Settings screen
- [Change₂] 2025-04-15 ⟶ Project phase changed from 🚧INITIALIZING to 🏗️DEVELOPMENT
- [Change₃] 2025-04-15 ⟶ Initial codebase research completed
- [Change₄] 2025-04-15 ⟶ Project brief updated with specific UI reconstruction and feature goals
- [Change₅] 2025-04-16 ⟶ Technical context updated with detailed requirements
- [Change₆] 2025-04-16 ⟶ Comprehensive project analysis completed
- [Change₇] 2025-04-16 ⟶ Initial progress metrics established and milestones planned
- [Change₈] 2025-04-16 ⟶ UI structure and navigation system analyzed
- [Change₉] 2025-04-17 ⟶ JSON parsing error identified in DAInfoRepositoryImpl
- [Change₁₀] 2025-04-17 ⟶ Detailed analysis of DADetailScreen UI structure completed
- [Change₁₁] 2025-04-17 ⟶ Created DADetailHeaderCard to combine header and statistics card
- [Change₁₂] 2025-04-17 ⟶ Identified card styling inconsistencies and solution approaches
- [Change₁₃] 2025-04-20 ⟶ Analyzed JSON parsing error in DAInfoRepositoryImpl and developed solution approaches
- [Change₁₄] 2025-04-21 ⟶ Analyzed TimelineChart implementation and identified gaps compared to prototype design
- [Change₁₅] 2025-04-21 ⟶ Analyzed unixTimeBoot implementation performance implications and database issues
- [Change₁₆] 2025-04-22 ⟶ Completed detailed analysis of settings interface implementation architecture
- [Change₁₇] 2025-04-22 ⟶ Designed AppDetailScreen architecture with Material Design 3 principles
- [Change₂₂] 2025-04-26 ⟶ 完成TopAppBars.kt重构，使用组件拆分、状态集中管理和更好的代码组织方式

## 🚶 Next Steps
- [Step₁₆] 优化 AppDetailScreen Tab 内容的 UI，移除不必要的元素，Medium
- [Step₁₅] Fix navigation system TopAppBar issues by updating route detection logic, High
- [Step₁] Fix JSON parsing error in DAInfoRepositoryImpl by implementing multi-language support, High
- [Step₂] Fix card styling inconsistencies by using ElevatedCard with proper parameters, High
- [Step₃] Create MD3 UI component library using existing components as reference, High
- [Step₄] Design navigation system with type-safe routes using serializable classes, High
- [Step₅] Implement multi-user interface with proper UI controls in TopAppBar, Medium
- [Step₆] Develop edge-to-edge layout system with proper content padding, Medium
- [Step₇] Improve TimelineChart with rounded bars, proper axis layout and optimized time labels, High
- [Step₈] Update database version number and implement proper migration strategy, High
- [Step₉] Implement Settings Context Provider with LocalStorage persistence, High
- [Step₁₀] Create reusable settings UI components for different input types, Medium
- [Step₁₁] Implement theme settings with dark/light mode synchronization, Medium
- [Step₁₂] Design settings search functionality for improved discoverability, Low
- [Step₁₃] Implement AppDetailScreen with tabs for app information, wakelocks, alarms, and services, High
- [Step₁₄] Design collapsible header pattern for AppDetailScreen following MD3 principles, Medium

## 🤔 Active Decisions
- [Decision₁] ✅ ⟶ Adopt RIPER framework for project organization, To improve development efficiency and knowledge management
- [Decision₁₆] ✅ ⟶ Use SavedStateHandle for managing screen parameters, Provides automatic state restoration during configuration changes
- [Decision₁₇] ✅ ⟶ Create parameter constant classes for type safety, Makes parameter access more robust and prevents typos
- [Decision₁₈] ✅ ⟶ Implement hybrid navigation approach for mixed string and type routes, Pragmatic solution to maintain backward compatibility
- [Decision₂] ✅ ⟶ Move project to development phase, Framework initialization complete
- [Decision₃] ✅ ⟶ Focus on MD3 UI reconstruction as primary goal, Based on updated project brief
- [Decision₄] ✅ ⟶ Prioritize multi-user support and backup functionality, Critical for complete feature set
- [Decision₅] ✅ ⟶ Implement balanced milestone approach with 2-6 week targets, Provides realistic timeframes while maintaining momentum
- [Decision₆] ✅ ⟶ Use existing component structure as baseline, Maintain consistent user experience while upgrading to MD3
- [Decision₇] ⏳ ⟶ Determine approach for handling multi-language descriptions in DAInfoRepositoryImpl, Options include internal JSON model, custom type adapter, or kotlinx.serialization
- [Decision₈] ✅ ⟶ Combine header and statistics cards in DADetailScreen, Improves visual design and reduces scrolling
- [Decision₉] ✅ ⟶ Use ElevatedCard for DADetailHeaderCard to match other cards, Ensures visual consistency across the UI
- [Decision₁₀] ✅ ⟶ Create internal JSON parsing model for DAInfoRepositoryImpl, Isolate multi-language handling without changing external interfaces
- [Decision₁₁] ✅ ⟶ Keep TimelineChart implementation using Canvas without third-party libraries, Limit dependencies while improving visual design
- [Decision₁₂] ✅ ⟶ Implement Room database version incrementation and proper migration strategy, Resolves database integrity errors
- [Decision₁₃] ✅ ⟶ Use React Context API approach for settings management, Provides unified state management across component tree
- [Decision₁₄] ✅ ⟶ Store settings in LocalStorage with JSON serialization, Ensures settings persistence between sessions
- [Decision₁₅] ✅ ⟶ Implement useReducer pattern for complex settings state management, Enables more structured state updates
- [Decision₁₉] ✅ ⟶ 在 AppDetailScreen 中集成已有的 DAsScreen 实现，而不是创建新组件，更好地复用代码
- [Decision₂₀] ✅ ⟶ 实现 Tab 懒加载，仅在 Tab 被选中时加载内容，提高性能
- [Decision₂₁] ✅ ⟶ 将搜索状态仅传递给当前选中的 Tab，简化状态管理

## 📎 Context References
- 📄 Active Files:
  - [app/src/main/java/com/js/nowakelock/ui/screens/appdetail/AppDetailScreen.kt] ⟶ 实现了 AppSt 设置 UI 和 Tab 内容集成
  - [app/src/main/res/values/strings.xml] ⟶ 添加了设置 UI 相关的字符串资源
  - [app/src/main/java/com/js/nowakelock/ui/screens/apps/AppsViewModel.kt] ⟶ Updated to use SavedStateHandle for parameter management
  - [app/src/main/java/com/js/nowakelock/ui/screens/das/DAsViewModel.kt] ⟶ Updated to use SavedStateHandle for parameter management
  - [app/src/main/java/com/js/nowakelock/ui/navigation/params/AppsScreenParams.kt] ⟶ Parameter constants for AppsScreen
  - [app/src/main/java/com/js/nowakelock/ui/navigation/params/DAsScreenParams.kt] ⟶ Parameter constants for DAs screens
  - [app/src/main/java/com/js/nowakelock/ui/navigation/NavRoutes.kt] ⟶ Navigation routes and type classes
  - [app/src/main/java/com/js/nowakelock/ui/navigation/NavGraph.kt] ⟶ Navigation graph with type-based navigation
  - [app/src/main/java/com/js/nowakelock/ui/components/BottomNavBar.kt] ⟶ Bottom navigation with hybrid navigation approach
  - [app/src/main/java/com/js/nowakelock/ui/components/TopAppBars.kt] ⟶ 刚完成重构的TopAppBar实现
  - [app/src/main/java/com/js/nowakelock/ui/screens/dadetail/DADetailContent.kt] ⟶ Main content component for DADetailScreen
  - [app/src/main/java/com/js/nowakelock/ui/screens/dadetail/components/DADetailHeaderCard.kt] ⟶ New combined header and stats card
  - [app/src/main/java/com/js/nowakelock/repository/DAInfoRepositoryImpl.kt] ⟶ Repository with JSON parsing error
  - [app/src/main/java/com/js/nowakelock/model/DAInfoEntry.kt] ⟶ Data model for DA information
  - [app/src/main/java/com/js/nowakelock/ui/NoWakeLockApp.kt] ⟶ Main UI composition for MD3 reconstruction
  - [app/src/main/java/com/js/nowakelock/ui/navigation/NavGraph.kt] ⟶ Navigation system with type-safe routes
  - [app/src/main/java/com/js/nowakelock/ui/components/BottomNavBar.kt] ⟶ Bottom navigation implementation
  - [app/src/main/java/com/js/nowakelock/ui/components/UserSwitcher.kt] ⟶ Multi-user UI component
  - [app/src/main/java/com/js/nowakelock/ui/screens/das/DAsScreen.kt] ⟶ Representative screen implementation
  - [app/src/main/java/com/js/nowakelock/ui/screens/dadetail/components/TimelineChart.kt] ⟶ Activity timeline visualization component
  - [app/src/main/java/com/js/nowakelock/ui/screens/dadetail/components/DATimelineCard.kt] ⟶ Card containing timeline chart
  - [app/src/main/java/com/js/nowakelock/data/db/InfoDatabase.kt] ⟶ Database configuration with versioning issue
  - [app/src/main/java/com/js/nowakelock/data/db/dao/DADao.kt] ⟶ Data access object with Map return type warnings
  - [app/src/main/java/com/js/nowakelock/ui/settings/SettingsProvider.kt] ⟶ Settings management and persistence
  - [app/src/main/java/com/js/nowakelock/ui/settings/components/SettingsUI.kt] ⟶ Reusable settings UI components
  - [app/src/main/java/com/js/nowakelock/ui/theme/Theme.kt] ⟶ Theme management including light/dark mode
  - [app/src/main/java/com/js/nowakelock/ui/screens/apps/AppsScreen.kt] ⟶ Apps list screen with navigation to app details
  - [app/src/main/java/com/js/nowakelock/data/repository/appdas/AppDasRepo.kt] ⟶ Repository for app data access
  - [app/src/main/java/com/js/nowakelock/ui/components/StatisticCard.kt] ⟶ Reusable statistics display component
  - [app/src/main/java/com/js/nowakelock/ui/screens/appdetail/AppDetailScreen.kt] ⟶ 实现了 Tab 内容集成和懒加载
  - [app/src/main/java/com/js/nowakelock/ui/components/TopAppBars.kt] ⟶ 更新以支持 AppDetail 路由的搜索功能
  - [app/src/main/java/com/js/nowakelock/ui/navigation/NavGraph.kt] ⟶ 更新以传递搜索状态给 AppDetailScreen
  - [app/src/main/java/com/js/nowakelock/ui/screens/das/DAsScreens.kt] ⟶ 提供 WakelockScreen, AlarmScreen, ServiceScreen 组件
  - [app/src/main/java/com/js/nowakelock/ui/screens/das/DAsViewModel.kt] ⟶ 处理 DA 列表的加载和过滤

## 💻 Active Code:
  - [AppDetailScreen.SettingsCard] ⟶ AppSt 设置卡片组件，展示全局设置和模式管理
  - [AppDetailScreen.BlockSettingsSection] ⟶ 全局阻止设置区域，包含唤醒锁/闹钟/服务开关
  - [AppDetailScreen.PatternSettingsSection] ⟶ 正则表达式模式设置区域，支持添加和删除模式
  - [AppDetailScreen.PatternChip] ⟶ 模式显示组件，展示已添加的正则表达式
  - [AppDetailScreen.SettingToggle] ⟶ 设置开关组件，用于启用/禁用全局阻止功能
  - [AppDetailViewModel.updateWakelockBlock] ⟶ 更新全局唤醒锁阻止设置
  - [AppDetailViewModel.addWakelockPattern] ⟶ 添加唤醒锁阻止模式
  - [AppDetailViewModel.removeWakelockPattern] ⟶ 删除唤醒锁阻止模式
  - [AppDetailViewModel.validateRegexPattern] ⟶ 验证正则表达式模式是否有效
  - [AppsViewModel.currentUserId] ⟶ SavedStateHandle property for user ID
  - [DAsViewModel.packageName] ⟶ SavedStateHandle property for package filtering
  - [AppsScreenParams] ⟶ Constants for AppsViewModel parameters
  - [DAsScreenParams] ⟶ Constants for DAsViewModel parameters
  - [NavRoutes] ⟶ String constants and class-based route definitions
  - [BottomNavItem] ⟶ Navigation item with route creation for bottom bar
  - [NoWakeLockBottomNavBar] ⟶ Bottom navigation implementation with hybrid approach
  - [NoWakeLockTopAppBar] ⟶ 重构后的主TopAppBar组件，保持接口不变但改进内部实现
  - [DADetailHeaderCard] ⟶ New combined header and statistics card component
  - [DAInfoRepositoryImpl.loadDAInfos()] ⟶ Method with JSON parsing error
  - [DAInfoEntry] ⟶ Data model expecting string description
  - [NoWakeLockNavGraph] ⟶ Main navigation structure with composable screens
  - [DAListItem] ⟶ Key UI component for list display
  - [UserSwitcher] ⟶ Multi-user selection component
  - [NoWakeLockBottomNavBar] ⟶ Bottom navigation with Material icons
  - [enableEdgeToEdge()] ⟶ Modern edge-to-edge UI implementation
  - [TimelineChart] ⟶ Canvas-based chart implementation needing visual improvements
  - [InfoDatabase] ⟶ Room database class requiring version update
  - [unixTimeBoot] ⟶ System time calculation for handling relative timestamps
  - [SettingsProvider] ⟶ Context provider for settings management
  - [useSettings] ⟶ Custom hook for accessing settings
  - [SettingsReducer] ⟶ State management for complex settings updates
  - [SettingsUI.Input] ⟶ Text input setting component
  - [SettingsUI.Switch] ⟶ Toggle setting component
  - [SettingsUI.Select] ⟶ Dropdown selection setting component
  - [SettingsUI.ColorPicker] ⟶ Color selection setting component
  - [RouteUtils] ⟶ 新增路由处理工具类，封装路由判断逻辑
  - [TopAppBarUiState] ⟶ 新增TopAppBar状态管理数据类
  - [RouteUtils.shouldShowSearch] ⟶ 更新支持 AppDetail 路由的搜索功能
  - [AppDetailScreen.selectedTabIndex] ⟶ 跟踪当前选中的 Tab
  - [AppDetailScreen.loadedTabs] ⟶ 用于懒加载的已加载 Tab 集合
  - [WakelocksTabContent] ⟶ 集成了 WakelockScreen 的 Tab 内容
  - [AlarmsTabContent] ⟶ 集成了 AlarmScreen 的 Tab 内容
  - [ServicesTabContent] ⟶ 集成了 ServiceScreen 的 Tab 内容
  - [koinViewModel(qualifier = named("..."))] ⟶ 获取特定类型的 DAsViewModel
  - [viewModel.setAppFilter(packageName, userId)] ⟶ 设置 DA 列表的包名和用户 ID 过滤器

## 📚 Active Docs:
  - [memory-bank/progress.md] ⟶ Development milestones
  - [memory-bank/systemPatterns.md] ⟶ Architecture insights
  - [memory-bank/techContext.md] ⟶ Technical implementation details

## 📁 Active Folders:
  - [app/src/main/java/com/js/nowakelock/ui/screens/dadetail/] ⟶ DADetail screen implementation
  - [app/src/main/java/com/js/nowakelock/ui/screens/dadetail/components/] ⟶ DADetail screen components
  - [app/src/main/java/com/js/nowakelock/repository/] ⟶ Repository implementations
  - [app/src/main/java/com/js/nowakelock/model/] ⟶ Data models
  - [app/src/main/java/com/js/nowakelock/ui/components/] ⟶ Reusable UI components
  - [app/src/main/java/com/js/nowakelock/ui/screens/] ⟶ Application screens by feature
  - [app/src/main/java/com/js/nowakelock/ui/navigation/] ⟶ Navigation system
  - [app/src/main/java/com/js/nowakelock/data/db/] ⟶ Database infrastructure and DAOs
  - [app/src/main/java/com/js/nowakelock/ui/settings/] ⟶ Settings system implementation
  - [app/src/main/java/com/js/nowakelock/ui/settings/components/] ⟶ Settings UI components

## 📡 Context Status
- 🟢 Active: 
  - AppDetailScreen 标签页懒加载优化
  - Tab 切换时的 UI 体验
  - Compose 中的 derivedStateOf 用法
- 🟡 Partially Relevant: 
  - DAsViewModel 性能优化
  - WakelockScreen/AlarmScreen/ServiceScreen 实现
- 🟣 Essential: 
  - Composable 重组机制理解
  - LaunchedEffect 和 derivedStateOf 的正确使用方式
- 🔴 Deprecated: N/A

## 💡 Project Patterns & Preferences
- [Pattern₁] MVVM Architecture ⟶ App follows MVVM pattern with Compose UI
- [Pattern₁₉] SavedStateHandle for ViewModel Parameters ⟶ Screen parameters stored in SavedStateHandle for automatic state restoration
- [Pattern₂₀] Parameter Constants ⟶ String constants for parameter names to prevent typos and improve type safety
- [Pattern₂₁] Hybrid Navigation ⟶ Mix of string routes and type-based routes depending on screen needs
- [Pattern₂] Dependency Injection ⟶ Uses Koin for DI with modular organization
- [Pattern₃] Screen-Based Organization ⟶ UI divided by feature screens in separate packages
- [Pattern₄] Type-Safe Navigation ⟶ Uses serializable classes for route parameters
- [Pattern₅] Composable Components ⟶ Reusable UI components in separate files
- [Pattern₆] Material Design 3 ⟶ New UI design system with edge-to-edge support
- [Pattern₇] Edge-to-Edge UI ⟶ Modern Android UI using enableEdgeToEdge() API
- [Pattern₈] Component-Based Design ⟶ Reusable UI components for consistency
- [Pattern₉] Incremental Development ⟶ Progressive implementation with milestone-based planning
- [Pattern₁₀] Multi-Language Support ⟶ Some data models need to support multiple languages
- [Pattern₁₁] Card-Based UI ⟶ Uses card components for information grouping with consistent styling
- [Pattern₁₂] ElevatedCard Pattern ⟶ Preference for ElevatedCard over basic Card for main UI components
- [Pattern₁₃] Canvas Visualization ⟶ Custom charts implemented with Compose Canvas API rather than third-party libraries
- [Pattern₁₄] Semantic MD3 Badge Colors ⟶ User badges and indicators should use semantic colors in circular badges
- [Pattern₁₅] Context-Based State Management ⟶ Uses React Context API pattern for settings and global state
- [Pattern₁₆] Persistent Settings Storage ⟶ Settings stored in LocalStorage with JSON serialization
- [Pattern₁₇] Reducer Pattern for Complex State ⟶ Complex state updates managed through reducer functions
- [Pattern₁₈] Component Composition ⟶ UI built through composition of smaller, specialized components
- [Pattern₂₂] 组件拆分与职责分离 ⟶ 将大型UI组件拆分为更小的可组合函数，每个函数负责特定功能
- [Pattern₂₃] 状态集中管理 ⟶ 使用数据类封装UI状态，基于参数派生状态而非分散的条件判断
- [Pattern₂₄] 工具类封装 ⟶ 将通用逻辑抽取到工具类中，提高代码复用性和可维护性
- [Pattern₂₅] Tab 懒加载模式 ⟶ 使用 mutableSetOf 跟踪已加载的 Tab，只在需要时创建内容
- [Pattern₂₆] 条件性搜索状态传递 ⟶ 搜索状态只传递给当前选中的 Tab，简化状态管理
- [Pattern₂₇] 复用现有组件 ⟶ 优先集成现有组件而不是创建新组件，减少代码重复

## 📚 Learnings & Insights
- 2025-04-25 ⟶ SavedStateHandle provides automatic state restoration during configuration changes and process death, making it ideal for storing ViewModel parameters.
- 2025-04-25 ⟶ Using type-based navigation (like `composable<Apps>`) changes the actual route from simple strings to complex generated paths like "com.js.nowakelock.ui.navigation.Apps?parameters..."
- 2025-04-25 ⟶ Route detection logic in UI components needs to be updated when switching from string-based to type-based navigation.
- 2025-04-25 ⟶ String comparison for routes (`route == NavRoutes.APPS`) fails with type-based navigation, requiring pattern matching (`route.contains(NavRoutes.APPS)`).
- 2025-04-25 ⟶ Mixing string-based and type-based navigation in the same application requires careful handling of different route formats.
- 2025-04-25 ⟶ Creating parameter constant classes improves code maintainability by centralizing parameter names and preventing typos.
- 2025-04-25 ⟶ Property delegation in Kotlin (`var property by savedStateHandle.stateIn()`) can simplify SavedStateHandle usage but isn't available in all versions.

- 2025-04-13 ⟶ 完成系统初始化，建立了 memory-bank 结构和文档归档规范。
- 2025-04-13 ⟶ 关键代码段应用了分级保护注释，形成了保护注册表和开发阶段的保护策略。
- 2025-04-13 ⟶ 项目阶段切换机制和进度追踪体系完善，支持开发过程的阶段性备份。
- 2025-04-13 ⟶ 统计与电池优化功能初步实现，提升了可视化和用户洞察能力。
- 2025-04-13 ⟶ 电池使用影响评估系统(BatteryImpact)通过四个级别(NONE/LOW/MEDIUM/HIGH)有效分类应用电池消耗程度。
- 2025-04-13 ⟶ DAStatistics类增强计算函数可生成阻止百分比和节省时间百分比，为用户提供更直观的统计数据。
- 2025-04-13 ⟶ 研究模式(Research Mode)下，严格限制为只读操作，专注信息收集和理解，不允许修改代码。

- 2025-04-12 ⟶ 明确了 memory-bank 结构和上下文加载规则，规范了各类上下文文件的用途和内容归档方式。
- 2025-04-12 ⟶ 梳理了 DADetailScreen 的 UI 结构、分区、数据源、样式细节，为后续 UI 统一和重构提供了基础。
- 2025-04-12 ⟶ 归纳了 UI 卡片合并、分割线处理、卡片间距与背景一致性等最佳实践。
- 2025-04-12 ⟶ 记录并分析了 JSON 解析典型错误，强调数据结构与模型定义一致性的重要性。

- 2025-04-17 ⟶ 详细分析了 DADetailScreen 的实现，了解了组件结构、数据流和样式特点。
- 2025-04-17 ⟶ 实现了 DADetailHeaderCard 组件，合并了头部区域和统计数据卡片，提高了界面紧凑性。
- 2025-04-17 ⟶ 发现并解决了卡片样式不一致问题，确定了使用 ElevatedCard 替代基础 Card 来保持统一外观。
- 2025-04-17 ⟶ 确认了包括移除卡片自身边距，让父容器控制间距的最佳实践方法。
- 2025-04-17 ⟶ 分析了 JSON 解析错误，识别出多语言支持需求与数据模型不匹配的问题。

- 2025-04-20 ⟶ 深入分析了DAInfoRepositoryImpl中的JSON解析错误，确认问题出在description和recommendation字段，实际为多语言对象而非简单字符串。
- 2025-04-20 ⟶ 探索了三种主要解决方案：1) 更新数据模型支持多语言Map；2) 创建自定义类型适配器；3) 设计内部JSON解析模型。
- 2025-04-20 ⟶ 决定使用内部JSON解析模型方案，在不改变外部接口的情况下解决多语言支持问题。
- 2025-04-20 ⟶ 评估了Kotlinx.serialization作为现代Kotlin项目的最佳实践JSON解析库，比Gson更适合Kotlin特性和Compose生态系统。
- 2025-04-20 ⟶ 制定了六步实施计划：创建内部解析模型、修改解析逻辑、实现模型转换、更新查询方法、增强错误处理、全面测试。

- 2025-04-21 ⟶ 分析了TimelineChart实现与原型图的差距，发现主要包括布局与间距、视觉呈现、标签与时间指示器、图例样式和响应式布局五个方面的不足。
- 2025-04-21 ⟶ 确定了Canvas绘制的局限性是主要原因，包括手动位置计算不精确、缺乏视觉细节和自适应能力有限。
- 2025-04-21 ⟶ 评估了第三方图表库如Vico、Compose Charts和Horologist Charts，但决定保持不引入额外依赖，继续优化现有Canvas实现。
- 2025-04-21 ⟶ 制定了改进计划，包括柱状图圆角处理、间距优化、图例移至右上角、Y轴刻度线添加和时间标签优化。
- 2025-04-21 ⟶ 确定了优先级顺序：先处理图例位置、实现柱状图圆角、优化时间标签显示、添加Y轴刻度、最后进行性能优化。

- 2025-04-21 ⟶ 分析了unixTimeBoot变量的性能影响，确认其作为一次性计算的时间转换机制不会造成明显性能问题，计算开销小且只在特定时间转换场景下使用。
- 2025-04-21 ⟶ 解析了Room数据库版本不匹配错误(ERR:ff147e6ee57246faef88268522a13064/0a7951e6409f54e7793c70042ff096b7)，确认需要增加数据库版本号并提供迁移策略。
- 2025-04-21 ⟶ 分析了fallbackToDestructiveMigration(false)配置含义，表明当找不到适当迁移路径时应抛出异常而非删除数据，这是数据安全性优先的策略。
- 2025-04-21 ⟶ 发现DADao中使用可空类型参数(Map<Info, St?>)的Room警告，确认Room不会在结果集中放入null值，应改为非空类型。
- 2025-04-21 ⟶ 诊断Logcat日志中的"no such table: info_event"错误，确认为数据库版本从5直接升级到11但缺少相应迁移路径导致。
- 2025-04-21 ⟶ 分析了"Accessing non-final property type in constructor"警告，识别为在父类构造函数中访问被子类覆盖的open属性的危险做法。
- 2025-04-21 ⟶ 评估了"OnBackInvokedCallback is not enabled"警告，确认需要在AndroidManifest.xml中添加android:enableOnBackInvokedCallback="true"支持Android 13+的新返回按钮机制。
- 2025-04-21 ⟶ 解释了tools:targetApi="33"属性的作用，它仅为开发工具提示而非运行时配置，用于抑制针对高API级别功能的lint警告。
- 2025-04-21 ⟶ 分析了隐藏API访问警告(SQLiteDatabase.getThreadSession等)，确认这些警告来自Room库内部实现，虽不会立即影响功能但表明存在长期兼容性风险。
- 2025-04-21 ⟶ 设计了符合MD3规范的用户ID徽章显示方案，推荐使用圆形徽章、语义化颜色和动态字体大小调整，以最大化信息密度并保持视觉一致性。

- [Learning₁] 2025-04-15 ⟶ RIPER framework provides structured approach to project development
- [Learning₂] 2025-04-15 ⟶ NoWakeLock uses Xposed framework to monitor wakelocks and alarms at system level
- [Learning₃] 2025-04-15 ⟶ The app has three main core functionalities: wakelock monitoring, alarm monitoring, and service monitoring
- [Learning₄] 2025-04-15 ⟶ App uses modern Android architecture components
- [Learning₅] 2025-04-15 ⟶ Different hooks are implemented for different Android API levels to ensure compatibility
- [Learning₆] 2025-04-15 ⟶ Project focus is on UI reconstruction with Material Design 3
- [Learning₇] 2025-04-16 ⟶ App uses enableEdgeToEdge() API instead of deprecated SystemUiController
- [Learning₈] 2025-04-16 ⟶ Multi-user support is implemented through userId parameters in database queries and UI
- [Learning₉] 2025-04-16 ⟶ The app has a comprehensive design for detecting and controlling system wakelocks, alarms and services
- [Learning₁₀] 2025-04-16 ⟶ Backup functionality will require careful serialization of user settings and preferences
- [Learning₁₁] 2025-04-16 ⟶ Existing functionality is largely complete with core monitoring at 85-90%, allowing focus on UI and enhancement features
- [Learning₁₂] 2025-04-16 ⟶ Navigation uses type-safe routes with Kotlin serialization for passing complex data between screens
- [Learning₁₃] 2025-04-16 ⟶ UI components use Material 3 elements but need consistent styling and modern layout patterns
- [Learning₁₄] 2025-04-16 ⟶ Multi-user support is already implemented in the UI via UserSwitcher component but needs enhancement 
- [Learning₁₅] 2025-04-17 ⟶ DAInfoRepositoryImpl has a JSON parsing error where description field is expected to be a string but actual JSON contains an object
- [Learning₁₆] 2025-04-17 ⟶ Multi-language support is needed for description fields in certain data models
- [Learning₁₇] 2025-04-17 ⟶ Three possible solutions identified: update data model, create custom type adapter, or preprocess JSON
- [Learning₁₈] 2025-04-17 ⟶ DADetailScreen structure includes header, stats, about, settings and activity timeline sections
- [Learning₁₉] 2025-04-17 ⟶ Combining UI elements improves visual consistency and reduces scrolling
- [Learning₂₀] 2025-04-17 ⟶ Material 3 card styling requires consistency; ElevatedCard provides better visual hierarchy than basic Card
- [Learning₂₁] 2025-04-17 ⟶ Parent container should control spacing between cards while cards manage internal padding
- [Learning₂₂] 2025-04-20 ⟶ JSON parsing errors mainly occur when schema mismatches exist between data format and model classes
- [Learning₂₃] 2025-04-20 ⟶ Internal JSON parsing models can isolate multi-language handling logic without changing external interfaces
- [Learning₂₄] 2025-04-20 ⟶ Kotlinx.serialization offers better Kotlin integration and performance than Gson through compile-time code generation
- [Learning₂₅] 2025-04-20 ⟶ Maintaining backwards compatibility is critical when updating repository implementations
- [Learning₂₆] 2025-04-21 ⟶ Canvas API provides powerful but low-level drawing capabilities that require careful position calculations
- [Learning₂₇] 2025-04-21 ⟶ Material Design 3 chart best practices include proper spacing, rounded corners, and strategic label placement
- [Learning₂₈] 2025-04-21 ⟶ Performance optimizations like using remember for calculations can significantly improve Compose recomposition efficiency
- [Learning₂₉] 2025-04-21 ⟶ Proper axis layout with non-overlapping labels is critical for chart readability
- [Learning₃₀] 2025-04-21 ⟶ Selective label display (showing only key points) improves chart clarity while maintaining information value
- [Learning₃₁] 2025-04-21 ⟶ Room database schema changes require version updates with proper migration strategies to prevent data loss
- [Learning₃₂] 2025-04-21 ⟶ The unixTimeBoot calculation (System.currentTimeMillis() - SystemClock.elapsedRealtime()) provides a timestamp reference point without causing performance issues
- [Learning₃₃] 2025-04-21 ⟶ Room's fallbackToDestructiveMigration(false) setting prioritizes data preservation over application functionality
- [Learning₃₄] 2025-04-21 ⟶ Accessing non-final properties in constructors is dangerous in Kotlin inheritance because parent constructors execute before child property initialization
- [Learning₃₅] 2025-04-21 ⟶ Proper MD3 badge design uses circular shapes, semantic colors and appropriate sizing to maximize information density and visual hierarchy
- [Learning₃₆] 2025-04-21 ⟶ Enable modern Android back handling with android:enableOnBackInvokedCallback="true" in the manifest for Android 13+ compatibility
- [Learning₃₇] 2025-04-21 ⟶ Library dependencies may access hidden Android APIs (like SQLiteDatabase.getThreadSession) which creates potential compatibility risks in future Android versions
- [Learning₃₈] 2025-04-22 ⟶ React Context API pattern provides effective state management for settings across the component tree
- [Learning₃₉] 2025-04-22 ⟶ LocalStorage with JSON serialization offers simple but effective settings persistence
- [Learning₄₀] 2025-04-22 ⟶ useReducer pattern helps manage complex state updates in a more structured way than useState
- [Learning₄₁] 2025-04-22 ⟶ Component composition allows for flexible and maintainable settings UI architecture
- [Learning₄₂] 2025-04-22 ⟶ Settings validation systems ensure data consistency and improve user experience
- [Learning₄₃] 2025-04-22 ⟶ Version control mechanisms for settings enable format upgrades and migrations
- [Learning₄₄] 2025-04-22 ⟶ Dynamic theme application through CSS variables allows theme changes without page refreshes
- [Learning₄₅] 2025-04-22 ⟶ Nested settings access through path parsing (e.g., "appearance.theme.mode") simplifies API design
- [Learning₄₆] 2025-04-22 ⟶ Settings search functionality significantly improves user experience for large setting collections
- [Learning₄₇] 2025-04-22 ⟶ Conditional settings dependencies allow for dynamic UI based on other setting values
- [Learning₄₈] 2025-04-22 ⟶ Material Design 3 doesn't have a direct equivalent to the old CollapsingToolbarLayout, requiring custom implementation in Compose
- [Learning₄₉] 2025-04-22 ⟶ Collapsible headers in Compose can be implemented using TopAppBarScrollBehavior and NestedScrollConnection
- [Learning₅₀] 2025-04-22 ⟶ Tab-based interfaces in Compose use TabRow with HorizontalPager for swipe navigation
- [Learning₅₁] 2025-04-22 ⟶ For large screens, ListDetailPaneScaffold provides an adaptive layout for master-detail patterns
- [Learning₅₂] 2025-04-22 ⟶ Existing DAsScreen implementations can be reused as tab content in the AppDetailScreen
- [Learning₅₃] 2025-04-22 ⟶ Koin is used for dependency injection instead of Hilt in this project
- [Learning₅₄] 2025-04-22 ⟶ The app uses a global TopAppBar that should be leveraged rather than creating screen-specific ones
- [Learning₅₅] 2025-04-22 ⟶ The TopAppBarEvent system allows setting detail screen titles from nested screens
- [Learning₅₆] 2025-04-22 ⟶ StatisticCard component needed for displaying grouped statistics in a standardized format
- [Learning₅₇] 2025-04-22 ⟶ LaunchedEffect triggering can affect the timing of UI events like title changes
- 2025-04-26 ⟶ 通过组件拆分和状态集中管理可以大幅提升代码可读性和可维护性，而不必改变外部接口
- 2025-04-26 ⟶ 工具类封装可以有效减少重复代码，提高代码质量和可测试性
- 2025-04-26 ⟶ Compose预览函数是UI开发的重要工具，可以快速验证不同状态下组件的视觉表现
- 2025-04-27 ⟶ 通过集成现有组件而不是创建新的实现，可以大幅减少代码重复并保持一致的用户体验。
- 2025-04-27 ⟶ 懒加载模式通过跟踪已加载内容的集合，可以有效减少不必要的渲染和数据加载，提高性能。
- 2025-04-27 ⟶ 条件性搜索状态传递比创建复杂的上下文映射更简单且易于理解，适合 Tab 结构的应用。
- 2025-04-27 ⟶ 为特定组件使用 named qualifier 创建 ViewModel 实例，可以在不同上下文中复用相同的数据源。

## 🧠 Knowledge Gained
- 🔍 Jetpack Compose 重组机制
  - LaunchedEffect 在重组后执行，而不是在重组过程中
  - derivedStateOf 可用于立即响应状态变化，解决 LaunchedEffect 延迟执行问题
  - 状态更新和 UI 渲染遵循单向数据流
- 🛠️ 懒加载最佳实践
  - 使用 remember { mutableStateOf(Set) } 跟踪已加载内容
  - 结合 derivedStateOf 确保当前选中项始终被视为已加载
  - 保持 LaunchedEffect 用于持久化已加载状态
- 📊 Wakelock/Alarm/Service 数据加载优化
  - 标签页各自拥有独立 ViewModel 导致数据重复加载
  - 初始化阶段同时进行数据同步和 UI 渲染影响性能
  - 未来改进方向：共享 ViewModel 或预加载数据

## 📌 Important Details
- 🔵 LaunchedEffect 执行时机
  - 不在当前重组周期内执行，而是在重组完成后调度执行
  - 这导致依赖 LaunchedEffect 更新的状态在当前重组周期内不可用
- 🔵 derivedStateOf 特性
  - 创建依赖于其他状态的派生状态
  - 只在依赖状态变化时重新计算
  - 在当前重组周期内立即可用
- 🔵 Compose 中的懒加载实现
  - 原始实现：使用 remember { mutableSetOf(0) } 和 LaunchedEffect 添加标签
  - 优化实现：使用 remember { mutableStateOf(setOf(0)) } 和 derivedStateOf 立即包含当前标签
  - 这避免了用户首次点击标签时看到加载指示器的问题

## 🔧 Current Tasks
1. ✅ 分析 AppDetailScreen 标签页切换问题
2. ✅ 理解 Compose 中 LaunchedEffect 和状态更新的执行顺序
3. ✅ 实现使用 derivedStateOf 的优化解决方案
4. ⏳ 继续优化 tab 内容的 UI 设计和布局
5. ⏳ 考虑优化 ViewModel 创建和数据加载策略

## 📚 Learning Resources
- [Compose State and State Hoisting](https://developer.android.com/jetpack/compose/state)
- [Side-effects in Compose](https://developer.android.com/jetpack/compose/side-effects)
- [LaunchedEffect API documentation](https://developer.android.com/reference/kotlin/androidx/compose/runtime/package-summary#LaunchedEffect)
- [derivedStateOf API documentation](https://developer.android.com/reference/kotlin/androidx/compose/runtime/package-summary#derivedStateOf)