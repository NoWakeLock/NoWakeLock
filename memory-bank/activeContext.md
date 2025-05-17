# σ₄: Active Context
*v1.0 | Created: 2025-04-15 | Updated: 2025-05-17*
*Π: 🏗️DEVELOPMENT | Ω: ⚙️E*

## 🔮 Current Focus

### AppDetailScreen页面状态持久性优化

我们已成功修复了AppDetailScreen页面从DADetail返回时状态不保留的问题：

- **问题**: 从AppDetailScreen导航到DADetail页面并返回时，AppDetailScreen会重新刷新，丢失之前的选项卡状态。
- **根本原因**: AppDetailScreen中的状态 (selectedTabIndex 和 loadedTabs) 使用的是普通的 remember，它不会在导航过程中保存状态。
- **解决方案**:
  1. 将关键UI状态从`remember`改为`rememberSaveable`，确保导航返回时可以恢复状态
  2. 派生状态继续使用`remember`，避免序列化错误（`derivedStateOf`结果无法被序列化到Bundle）
  3. 在导航配置中为AppDetail路由添加`launchSingleTop=true`和`restoreState=true`
- **效果**: 用户现在可以在导航返回后看到之前选择的标签页和内容，无需等待重新加载

这个修复遵循了Compose的最佳实践，对状态管理和导航配置进行了有针对性的改进，同时避免了过度设计和复杂化代码结构。

### ModuleCheckScreen布局优化

我们已成功修复了ModuleCheckScreen中TopAppBar下方出现空白的问题：

- **Scaffold窗口插图设置**：添加了`contentWindowInsets = WindowInsets(0, 0, 0, 0)`，禁用默认的窗口插图，避免空间重复计算
- **精确控制Padding**：修改了padding应用方式，只对Box容器应用顶部padding: `.padding(top = paddingValues.calculateTopPadding())`
- **内容边距调整**：为ModuleCheckContent添加了适当的水平和垂直内边距，确保内容与屏幕边缘保持合理距离
- **解决双重空白问题**：通过这些更改解决了主应用Scaffold和ModuleCheckScreen中Scaffold的嵌套导致的双重空白问题

这些更改确保了ModuleCheckScreen的布局更加紧凑和美观，与应用其他部分保持一致的外观。

### 模块检测功能与导航栏修复

我们成功实现了模块检测功能并修复了导航栏重复问题：

- **模块状态检测**：完整实现了模块激活状态、Hook工作状态和配置路径有效性的检测机制。
- **UI与逻辑分离**：创建了`ModuleCheckManager`、`ModuleCheckRepository`、`ModuleCheckViewModel`和`ModuleCheckScreen`，实现了清晰的关注点分离。
- **ContentProvider扩展**：在`XProvider`中添加了新方法以支持模块检测。
- **数据库DAO更新**：在`InfoDao`中增加了`getCountByType`方法。
- **多语言支持**：为模块检测功能添加了英文、中文和法文资源。
- **依赖注入**：更新了Koin配置，加入了新的Repository和ViewModel。
- **启动时检测**：在`BasicApp`中初始化`ModuleCheckManager`以实现重启后自动检测。
- **导航栏修复**：移除了`ModuleCheckScreen`的自定义`TopAppBar`，统一使用全局导航栏，并确保刷新功能正常。

### Room数据库迁移策略优化

我们已完成Room数据库迁移策略的全面优化：

- **多路径迁移**：为AppDatabase和InfoDatabase实现了完整的多路径迁移策略
- **事务保护**：使用事务包装迁移过程，确保原子性操作
- **彻底重建策略**：通过完全重建表结构解决索引冲突问题
- **详细日志**：添加全面的日志记录，便于诊断迁移过程
- **异常处理**：实现了全面的异常处理，确保迁移过程稳定可靠

### Xposed Hook 系统优化

我们正在进行 Xposed Hook 系统的全面重构和优化：

- **统一钩子策略**：替换特定版本的钩子方法为统一实现
- **参数位置缓存**：实现缓存机制消除重复参数提取
- **自适应参数提取**：创建适用于所有 Android 版本的灵活提取策略
- **调试日志控制**：基于调试模式设置添加条件日志记录
- **错误处理增强**：添加详细的错误处理和日志记录，提高稳定性
- **Boot 检测重构**：重构系统启动检测逻辑，提高模块化和错误处理能力

### 最新进展

- 已完成 XposedModule 中 Boot 检测逻辑重构，将其提取到专用方法中，保持了原有功能，提高了代码可维护性
- 已完成Room数据库迁移策略优化，解决了版本跳跃问题和"no such column: eventKey"错误
- 已完成 ServiceHook 重构，实现了灵活的参数提取策略
- 已完成 AlarmHook 重构，添加了参数位置缓存和错误处理
- 已完成 WakelockHook 重构，应用了统一钩子方法并保留受保护代码
- 所有三个核心系统现在都使用统一的自适应参数提取策略

### 相关文件

- 💻 [ModuleCheckManager.kt](app/src/main/java/com/js/nowakelock/data/manager/ModuleCheckManager.kt) - 模块检测核心逻辑
- 💻 [ModuleCheckRepository.kt](app/src/main/java/com/js/nowakelock/data/repository/ModuleCheckRepository.kt) - 模块检测数据仓库接口
- 💻 [ModuleCheckRepositoryImpl.kt](app/src/main/java/com/js/nowakelock/data/repository/ModuleCheckRepositoryImpl.kt) - 模块检测数据仓库实现
- 💻 [ModuleCheckViewModel.kt](app/src/main/java/com/js/nowakelock/ui/screens/modulecheck/ModuleCheckViewModel.kt) - 模块检测ViewModel
- 💻 [ModuleCheckScreen.kt](app/src/main/java/com/js/nowakelock/ui/screens/modulecheck/ModuleCheckScreen.kt) - 模块检测UI Composable
- 💻 [XProvider.kt](app/src/main/java/com/js/nowakelock/data/provider/XProvider.kt) - ContentProvider，添加了模块检测相关方法
- 💻 [InfoDao.kt](app/src/main/java/com/js/nowakelock/data/db/dao/InfoDao.kt) - 数据库DAO，添加了`getCountByType`方法
- 💻 [KoinDSL.kt](app/src/main/java/com/js/nowakelock/KoinDSL.kt) - Koin依赖注入配置
- 💻 [BasicApp.kt](app/src/main/java/com/js/nowakelock/BasicApp.kt) - 应用Application类，初始化ModuleCheckManager
- 💻 [NavGraph.kt](app/src/main/java/com/js/nowakelock/ui/navigation/NavGraph.kt) - 导航图，更新了模块检测页面的导航逻辑
- 💻 [TopAppBars.kt](app/src/main/java/com/js/nowakelock/ui/components/TopAppBars.kt) - TopAppBar组件，确保模块检测页面的刷新按钮
- 📄 [strings.xml](app/src/main/res/values/strings.xml) - 默认语言字符串资源
- 📄 [strings.xml (zh)](app/src/main/res/values-zh/strings.xml) - 中文语言字符串资源
- 📄 [strings.xml (fr)](app/src/main/res/values-fr/strings.xml) - 法文语言字符串资源
- 💻 [XposedModule.kt](app/src/main/java/com/js/nowakelock/xposedhook/XposedModule.kt) - Xposed 模块主类，实现了启动检测和钩子初始化
- 💻 [AppDatabase.kt](app/src/main/java/com/js/nowakelock/data/db/AppDatabase.kt) - 应用数据库类及多路径迁移实现
- 💻 [InfoDatabase.kt](app/src/main/java/com/js/nowakelock/data/db/InfoDatabase.kt) - 信息数据库类及多路径迁移实现
- 💻 [WakelockHook.kt](app/src/main/java/com/js/nowakelock/xposedhook/hook/WakelockHook.kt) - 唤醒锁钩子实现
- 💻 [ServiceHook.kt](app/src/main/java/com/js/nowakelock/xposedhook/hook/ServiceHook.kt) - 服务钩子实现
- 💻 [AlarmHook.kt](app/src/main/java/com/js/nowakelock/xposedhook/hook/AlarmHook.kt) - 闹钟钩子实现
- 📄 [XpNSP.kt](app/src/main/java/com/js/nowakelock/xposedhook/model/XpNSP.kt) - NSP 模型处理标志

### 下一步计划

- 评估 ServiceHook、AlarmHook 和 WakelockHook 重构的性能影响
- 对重构后的 Xposed 钩子系统进行全面测试
- 添加遥测来测量不同 Android 版本上的钩子成功率
- 检查数据库迁移在各种真实设备上的效果

Material Design 3 UI 组件标准化，XPosed设置与日志控制问题研究，唤醒锁系统重构

## 🔄 Recent Changes
- [Change₅₄] 2025-05-17 ⟶ 修复AppDetailScreen页面状态持久性问题：将selectedTabIndex和loadedTabs从remember改为rememberSaveable，确保导航返回时保留状态；将currentLoadedTabs保留为remember，避免序列化错误；在NavGraph中为AppDetail路由添加launchSingleTop=true和restoreState=true配置
- [Change₅₃] 2025-05-11 ⟶ 修复模块检测页面导航栏重复问题：移除ModuleCheckScreen中的自定义TopAppBar，统一使用全局导航栏，并确保刷新功能正常，更新NavGraph和TopAppBars
- [Change₅₂] 2025-05-11 ⟶ 实现模块检测功能：包括UI (ModuleCheckScreen), ViewModel (ModuleCheckViewModel), Repository (ModuleCheckRepository/Impl), Manager (ModuleCheckManager), ContentProvider方法扩展 (XProvider), 数据库查询更新 (InfoDao), 多语言支持 (strings.xml en/zh/fr), Koin配置 (KoinDSL.kt), 和应用启动逻辑 (BasicApp.kt)
- [Change₅₁] 2025-05-10 ⟶ 重构 XposedModule.kt 中的 Boot 检测逻辑，将代码提取到专用的 hookBootCompletedMethods 方法中，提高可维护性和模块化
- [Change₄₈] 2025-05-09 ⟶ 为 AppDatabase 实现多路径迁移策略，添加 MIGRATION_10_13 和 MIGRATION_11_13，实现事务保护
- [Change₄₉] 2025-05-09 ⟶ 为 InfoDatabase 实现多路径迁移策略，添加 MIGRATION_10_12，优化现有的 MIGRATION_11_12
- [Change₅₀] 2025-05-09 ⟶ 为两个数据库类实现共享的表重建函数，确保迁移逻辑一致性
- [Change₄₅] 2025-05-08 ⟶ 重构 WakelockHook 实现，应用统一钩子方法和参数位置自适应策略
- [Change₄₆] 2025-05-08 ⟶ 为 WakelockHook 添加参数位置缓存机制，提高性能
- [Change₄₇] 2025-05-08 ⟶ 改进 WakelockHook 错误处理，确保在参数提取失败时能够回退到原有实现
- [Change₄₂] 2025-05-07 ⟶ 重构 AlarmHook 实现，用统一钩子策略替代按版本分拆的三个方法，提高代码可维护性和适应性
- [Change₄₃] 2025-05-07 ⟶ 为 AlarmHook 添加参数缓存机制，避免重复提取参数的开销，提高性能
- [Change₄₄] 2025-05-07 ⟶ 实现 AlarmHook 的自适应参数提取策略，支持 Android 7-14 及以上版本，改进日志记录以跟踪参数提取
- [Change₃₉] 2025-05-05 ⟶ 重构ServiceHook实现，将flexibleServiceHook拆分为三个独立方法：flexibleServiceHooks、flexibleStartServiceHook和flexibleBindServiceHook，提高代码模块化和可维护性
- [Change₄₀] 2025-05-05 ⟶ 为bindServiceLocked添加灵活Hook机制，使用与startServiceLocked类似的参数提取策略，统一处理逻辑
- [Change₄₁] 2025-05-05 ⟶ 调整Hook参数提取策略，适应Android 16及更高版本可能的API变化，改进日志记录以更好地追踪参数提取
- [Change₃₄] 2025-05-01 ⟶ 创建 WakelockCounterTest 类，验证单个唤醒锁计数器的功能和非重叠持续时间计算
- [Change₃₅] 2025-05-01 ⟶ 创建 WakelockRegistryBasicTest 和 WakelockRegistryProblemTest 类，分别测试基本功能和复杂边缘情况
- [Change₃₆] 2025-05-01 ⟶ 实现 TestUtils 工具类，提供测试辅助功能如单例重置和日志模拟
- [Change₃₇] 2025-05-01 ⟶ 创建测试套件 WakelockTests，控制测试执行顺序确保测试间隔离
- [Change₃₁] 2025-04-30 ⟶ 实现了WakelockCounter类，用于追踪单个唤醒锁的活跃实例并计算非重叠持续时间
- [Change₃₂] 2025-04-30 ⟶ 实现了WakelockRegistry类，管理所有唤醒锁计数器并提供统一接口
- [Change₃₃] 2025-04-30 ⟶ 修改XProvider类，使用WakelockRegistry计算准确的countTime
- [Change₃₀] 2025-04-29 ⟶ AppsScreen 语言切换后用户切换无限循环问题修复：采用方案3，仅在 UI 为默认值(0)且 ViewModel 有非默认值时同步，避免循环，保证用户选择能正确恢复
- [Change₂₉] 2025-04-29 ⟶ 修复 AppsScreen 语言切换后用户切换无限循环问题：采用方案3，仅在 UI 为默认值(0)且 ViewModel 有非默认值时同步，避免循环，保证用户选择能正确恢复
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
- [Change₃₈] 2025-05-04 ⟶ 改进 AppDetailScreen 中的 PatternSettingsSection 组件，使其更符合 Material Design 3 原则，改进了布局、状态管理和用户体验

## 🔄 Next Steps
- [Step₁₉] 测试Android 16设备上ServiceHook的灵活Hook机制，验证对startServiceLocked和bindServiceLocked的适配效果，High
- [Step₁₈] 为其他核心组件添加单元测试，如XProvider和数据访问层，Medium
- [Step₁₇] 解决唤醒锁countTime与util显示时间计算不一致问题，Medium
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
- [Decision₃₃] ✅ ⟶ 对于AppDetailScreen状态持久性问题，采用rememberSaveable并结合导航配置改进(launchSingleTop=true, restoreState=true)的解决方案，确保从DADetail页面返回时保留用户选择的标签
- [Decision₃₁] ✅ ⟶ 修复模块检测页面导航栏重复问题，采用移除局部TopAppBar，统一使用全局导航栏的方案
- [Decision₃₀] ✅ ⟶ 实现完整的模块检测功能，覆盖模块激活、Hook有效性、配置路径检查，并提供多语言UI和清晰的用户指引
- [Decision₂₉] ✅ ⟶ 对WakelockHook进行重构，应用与AlarmHook和ServiceHook类似的统一钩子策略和参数自适应提取机制，同时保持受保护代码不变
- [Decision₂₈] ✅ ⟶ 对AlarmHook进行重构，采用与ServiceHook类似的统一钩子策略和参数缓存机制，提高代码适应性
- [Decision₂₆] ✅ ⟶ 对ServiceHook进行重构，将单一方法拆分为多个独立方法，提高代码可维护性并支持Android 16+
- [Decision₂₅] ✅ ⟶ 将大型测试类拆分为小型、聚焦的测试类，并使用测试套件控制执行顺序，解决测试间的状态干扰问题
- [Decision₂₃] ✅ ⟶ 唤醒锁countTime计算使用内存数据结构而非数据库操作，通过AtomicInteger和@Volatile确保线程安全，保证实时准确的统计
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
- [Decision₂₂] 2025-04-29 ⟶ AppsScreen 语言切换后用户切换无限循环问题采用方案3修复：单向同步，避免循环，保证用户选择恢复，Status: ✅ Accepted

## 📎 Context References
- 📄 Active Files:
  - [app/src/main/java/com/js/nowakelock/ui/screens/modulecheck/ModuleCheckScreen.kt] ⟶ 模块检测UI界面，已修复TopAppBar下方空白问题
  - [app/src/main/java/com/js/nowakelock/data/manager/ModuleCheckManager.kt]
  - [app/src/main/java/com/js/nowakelock/data/repository/ModuleCheckRepository.kt]
  - [app/src/main/java/com/js/nowakelock/data/repository/ModuleCheckRepositoryImpl.kt]
  - [app/src/main/java/com/js/nowakelock/ui/screens/modulecheck/ModuleCheckViewModel.kt]
  - [app/src/main/java/com/js/nowakelock/data/provider/XProvider.kt] ⟶ ContentProvider，添加了模块检测相关方法
  - [app/src/main/java/com/js/nowakelock/data/db/dao/InfoDao.kt] ⟶ 数据库DAO，添加了getCountByType方法
  - [app/src/main/java/com/js/nowakelock/KoinDSL.kt] ⟶ Koin依赖注入配置
  - [app/src/main/java/com/js/nowakelock/BasicApp.kt] ⟶ 应用Application类，初始化ModuleCheckManager
  - [app/src/main/java/com/js/nowakelock/ui/screens/appdetail/AppDetailScreen.kt] - 应用详情屏幕，使用rememberSaveable改进状态持久性
  - [app/src/main/java/com/js/nowakelock/ui/navigation/NavGraph.kt] - 导航图配置，添加了AppDetail路由的状态保留配置
- 💻 Active Code: 模块检测功能实现，导航栏重复问题修复，统一的Xposed钩子策略实现，Room数据库多路径迁移策略，Material Design 3 UI 组件实现，XPosed模块对宿主应用设置的读取流程，唤醒锁计算系统实现
- 📚 Active Docs: 
  - memory-bank/progress.md
  - memory-bank/techContext.md
- 📁 Active Folders: 
  - app/src/main/java/com/js/nowakelock/data/db/
  - app/src/main/java/com/js/nowakelock/xposedhook/
  - app/src/main/java/com/js/nowakelock/data/counter/
- 🔄 Git References: 最新提交中对唤醒锁计算系统的优化，Room数据库迁移策略改进

## 📡 Context Status
- 🟢 Active: 模块检测功能，导航栏修复，Room数据库迁移优化，Xposed钩子系统优化，XPosed设置问题分析，唤醒锁计数系统优化
- 🟡 Partially Relevant: 
- 🟣 Essential: 核心业务逻辑保护，Xposed集成，数据库稳定性
- 🔴 Deprecated: 旧的唤醒锁计时方法，旧的数据库迁移策略

## 🔬 Research Findings

### Room数据库迁移问题
1. **问题描述**：数据库版本从10直接升级到13时出现"no such column: eventKey"错误，导致应用崩溃
2. **根本原因**：
   - 版本12中info_event表包含eventKey列并定义了唯一索引
   - 版本13中重新设计了表结构，不再包含eventKey列
   - 从版本10直接跳跃到版本13时，Room尝试创建版本12中的索引，但表结构已经变化
   - 缺乏直接从10到13的迁移路径，导致Room尝试应用中间版本的schema
3. **解决策略**：
   - 实现完整的多路径迁移策略，覆盖所有可能的升级路径
   - 使用事务包装迁移操作，确保原子性
   - 采用彻底重建表的策略，而不是尝试保留或迁移数据
   - 在迁移开始时显式删除所有可能的索引，避免索引冲突
   - 添加全面的错误处理和日志记录
4. **技术实现**：
   - 创建共享的迁移实现函数，确保迁移逻辑一致性
   - 为AppDatabase添加MIGRATION_10_13、MIGRATION_11_13和MIGRATION_12_13
   - 为InfoDatabase添加MIGRATION_10_12和优化MIGRATION_11_12
   - 所有迁移使用事务保护，确保原子性操作
   - 集中处理异常，防止迁移过程中断

### Xposed钩子优化
1. **问题描述**：原有钩子实现按Android版本分拆，导致代码重复和维护困难
2. **优化思路**：
   - 使用统一钩子策略代替特定版本的实现
   - 实现参数位置缓存，避免重复提取参数
   - 创建自适应参数提取策略，适用于各种ROM版本
   - 优化日志记录，减少生产环境中的日志噪音
3. **成果**：
   - ServiceHook重构完成，支持Android 16+所有版本
   - AlarmHook重构完成，支持Android 7-14+所有版本
   - WakelockHook重构完成，支持Android 7-14+所有版本
   - 代码更加模块化和可维护
   - 提高了在不同Android版本上的兼容性和适应性
4. **下一步优化**：
   - 添加全面的错误处理和恢复机制
   - 实现更详细的遥测和性能监控

### XPosed模块设置问题
1. **问题描述**：设置中的debug模式切换不能控制XPosed日志输出；重启应用后设置未被保存
2. **根本原因**：
   - XpUtil.log = BuildConfig.DEBUG 静态初始化，缺乏动态更新机制
   - XSharedPreferences在Android高版本中加载受限，需要重启系统才能正确读取新设置
   - MODE_WORLD_READABLE权限在新版Android中导致SecurityException
   - SPTools错误处理方式导致失败时静默返回，没有错误提示
3. **核心问题链**：
   - 宿主应用中通过SPTools保存设置
   - Xposed模块通过XSharedPreferences读取设置
   - 由于权限限制，模块无法读取新安装后的设置文件
   - 需要重启系统才能解决权限问题
4. **决策**：采用文档指导方案，在设置界面添加提示，告知用户重新安装后需要重启系统

### 唤醒锁计数系统优化
1. **问题描述**：原有计时系统无法正确处理重叠的唤醒锁
2. **新实现**：
   - 使用WakelockCounter和WakelockRegistry两层结构
   - 通过AtomicInteger和volatile变量确保线程安全
   - 实现非重叠计时算法，只有0→1和最后一个1→0转换时更新时间
3. **性能考虑**：
   - 使用内存数据结构而非数据库操作，降低性能损耗
   - 最小化同步开销，使用原子变量而非锁
4. **进一步优化**：
   - 添加实例ID跟踪，防止重复计数
   - 使用ConcurrentHashMap.newKeySet确保线程安全的集合操作

## 📝 Code Insights

### Xposed钩子统一策略实现
```kotlin
// 统一的钩子方法替代特定版本钩子
private fun unifiedAlarmHook(lpparam: XC_LoadPackage.LoadPackageParam) {
    try {
        // 根据Android版本获取正确的类
        val alarmManagerServiceClass = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            XposedHelpers.findClass("com.android.server.alarm.AlarmManagerService", lpparam.classLoader)
        } else {
            XposedHelpers.findClass("com.android.server.AlarmManagerService", lpparam.classLoader)
        }

        // 钩住所有匹配的方法
        hookAlarmMethods(alarmManagerServiceClass, lpparam)
    } catch (e: Throwable) {
        XpUtil.log("Error in unified alarm hook: ${e.message}")
        e.printStackTrace()
    }
}

// 参数位置缓存和提取
private fun extractParametersFromCache(param: XC_MethodHook.MethodHookParam, positions: AlarmParamPositions) {
    try {
        val args = param.args
        val triggerList = args[positions.triggerListPos] as? ArrayList<*>
        
        if (triggerList != null) {
            val context: Context = AndroidAppHelper.currentApplication().applicationContext
            hookAlarmsLocked(triggerList, context)
        }
    } catch (e: Exception) {
        XpUtil.log("Error extracting parameters from cache: ${e.message}")
    }
}
```

### XPosed设置读取流程
```kotlin
// 宿主应用中设置debug模式
fun updateDebugMode(enabled: Boolean) {
    SPTools.setBoolean("debug", enabled)
    _debugMode.value = enabled
}
```

### 唤醒锁计数器实现
```kotlin
// 唤醒锁计数器实现，确保线程安全
class WakelockCounter {
    private val activeCount = AtomicInteger(0)
    
    @Volatile
    private var intervalStartTime: Long = 0
    
    private val trackedInstances = ConcurrentHashMap.newKeySet<String>()
    
    // 增加计数，仅在首次调用时计时
    fun increment(now: Long, instanceId: String): Long {
        if (!trackedInstances.add(instanceId)) {
            return 0 // 已跟踪的实例不再计数
        }
        
        if (activeCount.compareAndSet(0, 1)) {
            intervalStartTime = now
            return 0
        }
        
        val duration = max(0, now - intervalStartTime)
        intervalStartTime = now
        activeCount.incrementAndGet()
        return duration
    }
}
```

## 🔍 Insights & Patterns

1. **统一钩子策略**：使用反射动态查找方法，避免硬编码版本特定的参数结构
2. **参数缓存**：一旦成功提取参数位置，缓存它们以避免重复提取的开销
3. **灵活的参数提取**：实现自适应策略，先尝试预期参数位置，失败后尝试其他可能的参数位置
4. **错误恢复**：当钩子失败时有优雅的降级机制，避免系统不稳定
5. **线程安全**：使用 AtomicReference 和 @Volatile 确保线程安全的参数缓存
6. **日志控制**：根据调试模式状态优化日志记录，减少生产环境中的噪音

## 🎯 Next Actions

1. 考虑重构 WakelockHook，应用统一钩子策略
2. 为钩子系统添加更详细的遥测和性能监控
3. 评估ServiceHook和AlarmHook重构的性能影响
4. 设计更全面的错误处理和恢复机制

## 🧩 Related Context Items

- Xposed模块和Android权限模型
- Android版本适配策略
- 反射API和系统服务钩子技术
- 参数缓存和性能优化模式
