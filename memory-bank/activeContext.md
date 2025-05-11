# σ₄: Active Context
*v1.0 | Created: 2025-04-15 | Updated: 2025-05-05*
*Π: 🏗️DEVELOPMENT | Ω: 🔍R*

## 🔮 Current Focus

### Performance Optimization

We've recently implemented a significant performance optimization in the ServiceHook component:

- **Unified Hook Approach**: Replaced version-specific hooks with a unified approach
- **Parameter Position Caching**: Implemented caching to eliminate repeated parameter extraction
- **Adaptive Strategy**: Created a fallback mechanism for parameter extraction that works across all Android versions
- **Debug Logging Control**: Added conditional logging based on debug mode setting

### Relevant Files

- 💻 [ServiceHook.kt](app/src/main/java/com/js/nowakelock/xposedhook/hook/ServiceHook.kt) - Core service hooking implementation
- 📄 [XpNSP.kt](app/src/main/java/com/js/nowakelock/xposedhook/model/XpNSP.kt) - NSP model handling service flags

### Next Steps

- Evaluate performance impact of ServiceHook optimization
- Consider applying similar caching patterns to other Xposed hooks
- Add telemetry to measure hook success rates on different Android versions

Material Design 3 UI 组件标准化，XPosed设置与日志控制问题研究，唤醒锁系统重构，Android 16+ 的ServiceHook适配

## 🔄 Recent Changes
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

## 🚶 Next Steps
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
  - [app/src/main/java/com/js/nowakelock/xposedhook/hook/ServiceHook.kt] ⟶ 重构后的ServiceHook实现，包含对Android 16+的灵活Hook支持
  - [app/src/main/java/com/js/nowakelock/ui/screens/appdetail/components/PatternSettingsSection.kt] ⟶ 修改的 PatternSettingsSection 组件，实现更好的 Material Design 3 样式
  - [app/src/test/java/com/js/nowakelock/data/counter/WakelockCounterTest.kt] ⟶ WakelockCounter类的单元测试
  - [app/src/test/java/com/js/nowakelock/data/counter/WakelockRegistryBasicTest.kt] ⟶ WakelockRegistry基本功能测试
  - [app/src/test/java/com/js/nowakelock/data/counter/WakelockRegistryProblemTest.kt] ⟶ WakelockRegistry边缘情况测试
  - [app/src/test/java/com/js/nowakelock/data/counter/TestUtils.kt] ⟶ 测试工具类，提供单例重置等功能
  - [app/src/test/java/com/js/nowakelock/data/counter/WakelockTests.kt] ⟶ 测试套件，控制测试执行顺序
  - [app/src/main/java/com/js/nowakelock/data/counter/WakelockCounter.kt] ⟶ 计算单个唤醒锁非重叠时间的核心类
  - [app/src/main/java/com/js/nowakelock/data/counter/WakelockRegistry.kt] ⟶ 管理所有唤醒锁并提供统一接口
  - [app/src/main/java/com/js/nowakelock/data/provider/XProvider.kt] ⟶ 使用WakelockRegistry进行准确的countTime计算
  - [app/src/main/java/com/js/nowakelock/ui/screens/apps/AppsScreen.kt] ⟶ AppsScreen 语言切换后用户切换无限循环问题修复，采用单向同步方案3
  - [app/src/main/java/com/js/nowakelock/xposedhook/XpUtil.kt]
  - [app/src/main/java/com/js/nowakelock/xposedhook/model/XpNSP.kt]
  - [app/src/main/java/com/js/nowakelock/ui/screens/settings/SettingsViewModel.kt]
  - [app/src/main/java/com/js/nowakelock/base/SPTools.kt]
- 💻 Active Code: Material Design 3 UI 组件实现，XPosed模块对宿主应用设置的读取流程，唤醒锁计算系统实现，Android 16+ 的灵活ServiceHook机制
- 📚 Active Docs: 
  - memory-bank/progress.md
  - memory-bank/techContext.md
- 📁 Active Folders: 
  - app/src/main/java/com/js/nowakelock/xposedhook/
  - app/src/main/java/com/js/nowakelock/data/counter/
- 🔄 Git References: 最新提交中对唤醒锁计算系统的优化

## 📡 Context Status
- 🟢 Active: XPosed设置问题分析，唤醒锁计数系统优化
- 🟡 Partially Relevant: 数据库迁移问题
- 🟣 Essential: 核心业务逻辑保护，Xposed集成
- 🔴 Deprecated: 旧的唤醒锁计时方法

## 🔬 Research Findings

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

### XPosed设置读取流程
```kotlin
// 宿主应用中设置debug模式
fun updateDebugMode(enabled: Boolean) {
    SPTools.setBoolean("debug", enabled)
    _debugMode.value = enabled
}

// XSharedPreferences在Xposed模块中读取设置
fun makePref(): XSharedPreferences? {
    val p = XSharedPreferences(BuildConfig.APPLICATION_ID, SPTools.SP_NAME)
    pref = if (p.file.canRead()) p else null
    return pref
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

1. **Xposed模块限制**：需要适应Android版本限制，特别是在高版本系统上
2. **跨进程通信挑战**：宿主应用与Xposed模块间通信需要考虑权限和同步问题
3. **线程安全性**：处理系统级钩子需要全面考虑线程安全，使用适当的原子操作
4. **错误处理**：在Android系统集成中，完善的错误处理和日志记录至关重要
5. **用户体验**：技术限制难以克服时，清晰的用户指导和文档可以提高用户体验

## 🎯 Next Actions

1. 在设置页面添加关于XPosed设置需要重启系统的提示
2. 完善唤醒锁计数系统的实例跟踪机制
3. 改进错误处理，添加更详细的日志记录
4. 研究长期解决方案，如ContentProvider替代XSharedPreferences

## 🧩 Related Context Items

- Xposed模块和Android权限模型
- 线程安全编程模式
- Android跨进程通信机制
- 电池优化和唤醒锁管理
