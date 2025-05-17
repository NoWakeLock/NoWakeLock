# σ₆: Protection Registry
*v1.0 | Created: 2025-04-25 | Updated: 2025-05-20*
*Π: 🏗️DEVELOPMENT | Ω: ⚙️E*

## 🛡️ Protected Regions

### 测试基础设施
- **[GUARDED]** `app/src/test/java/com/js/nowakelock/data/counter/TestUtils.kt`
  - 保护理由: 提供重置单例对象和模拟日志的核心测试功能，被多个测试类依赖
  - 最后修改: 2025-05-01

- **[GUARDED]** `app/src/test/java/com/js/nowakelock/data/counter/WakelockTests.kt`
  - 保护理由: 控制测试执行顺序，确保测试间隔离，解决状态污染问题
  - 最后修改: 2025-05-01

### 关键功能测试
- **[GUARDED]** `app/src/test/java/com/js/nowakelock/data/counter/WakelockRegistryBasicTest.kt`
  - 保护理由: 验证WakelockRegistry核心功能的测试，确保单例模式和基本操作正确性
  - 最后修改: 2025-05-01

- **[GUARDED]** `app/src/test/java/com/js/nowakelock/data/counter/WakelockRegistryProblemTest.kt`
  - 保护理由: 验证WakelockRegistry复杂情况的测试，确保状态维护和边界条件处理
  - 最后修改: 2025-05-01

- **[GUARDED]** `app/src/test/java/com/js/nowakelock/data/counter/WakelockCounterTest.kt`
  - 保护理由: 验证WakelockCounter非重叠持续时间计算的正确性，核心算法测试
  - 最后修改: 2025-05-01

### 计时系统核心组件
- **[CRITICAL]** `app/src/main/java/com/js/nowakelock/data/counter/WakelockRegistry.kt`
  - 保护理由: 核心单例类，管理所有唤醒锁计数器并提供统一接口，全应用共享实例
  - 最后修改: 2025-04-30

- **[CRITICAL]** `app/src/main/java/com/js/nowakelock/data/counter/WakelockCounter.kt`
  - 保护理由: 核心计时算法实现，确保非重叠持续时间计算准确
  - 最后修改: 2025-04-30

### 核心基础设施
- **[CRITICAL]** `app/src/main/java/com/js/nowakelock/KoinDSL.kt`
  - 保护理由: 核心依赖注入模块，影响整个应用的组件实例化
  - 最后修改: 2025-04-25

- **[CRITICAL]** `app/src/main/java/com/js/nowakelock/xposedhook/hook/ServiceHook.kt`
  - 保护理由: 核心Xposed Hook组件，使用参数位置缓存优化，影响整个应用的性能
  - 最后修改: 2025-05-05

- **[CRITICAL]** `app/src/main/java/com/js/nowakelock/xposedhook/hook/AlarmHook.kt`
  - 保护理由: 核心Xposed Hook组件，使用统一钩子策略和参数缓存优化
  - 最后修改: 2025-05-07

- **[CRITICAL]** `app/src/main/java/com/js/nowakelock/xposedhook/hook/WakelockHook.kt`
  - 保护理由: 核心Xposed Hook组件，包含关键唤醒锁控制方法，对系统唤醒锁监控至关重要
  - 最后修改: 2025-05-08

### 导航系统
- **[GUARDED]** `app/src/main/java/com/js/nowakelock/ui/navigation/NavRoutes.kt`
  - 保护理由: 定义应用导航结构，需谨慎修改
  - 最后修改: 2025-04-25

- **[GUARDED]** `app/src/main/java/com/js/nowakelock/ui/navigation/NavGraph.kt`
  - 保护理由: 核心导航实现，影响所有页面跳转
  - 最后修改: 2025-05-17

### 核心UI组件
- **[GUARDED]** `app/src/main/java/com/js/nowakelock/ui/components/TopAppBars.kt`
  - 保护理由: 全局UI组件，出现在所有页面
  - 当前问题: 路由检测逻辑需更新以支持类型导航
  - 最后修改: 2025-04-27

- **[GUARDED]** `app/src/main/java/com/js/nowakelock/ui/components/BottomNavBar.kt`
  - 保护理由: 全局UI组件，负责主要页面跳转
  - 最后修改: 2025-04-25

### 重要屏幕实现
- **[GUARDED]** `app/src/main/java/com/js/nowakelock/ui/screens/appdetail/AppDetailScreen.kt`
  - 保护理由: 复杂的标签页界面实现，集成多个功能组件
  - 最后修改: 2025-05-17

- **[GUARDED]** `app/src/main/java/com/js/nowakelock/ui/screens/das/DAsScreen.kt`
  - 保护理由: 核心功能屏幕，被多处复用
  - 最后修改: 2025-04-15

- **[GUARDED]** `app/src/main/java/com/js/nowakelock/ui/screens/apps/AppsScreen.kt`
  - 保护理由: 关键多用户UI同步逻辑，涉及配置变更下的状态恢复与循环同步风险
  - 最后修改: 2025-04-29

### 参数与视图模型
- **[GUARDED]** `app/src/main/java/com/js/nowakelock/ui/navigation/params/AppsScreenParams.kt`
  - 保护理由: 定义与Apps相关的所有参数名称，确保类型安全
  - 最后修改: 2025-04-25

- **[GUARDED]** `app/src/main/java/com/js/nowakelock/ui/navigation/params/DAsScreenParams.kt`
  - 保护理由: 定义与DAs相关的所有参数名称，确保类型安全
  - 最后修改: 2025-04-25

- **[GUARDED]** `app/src/main/java/com/js/nowakelock/ui/screens/apps/AppsViewModel.kt`
  - 保护理由: 包含关键业务逻辑，处理用户筛选、排序和应用选择
  - 最后修改: 2025-04-25

- **[GUARDED]** `app/src/main/java/com/js/nowakelock/ui/screens/das/DAsViewModel.kt`
  - 保护理由: 处理唤醒锁、闹钟和服务的数据管理
  - 最后修改: 2025-04-25

## 📜 Protection History
- 2025-05-20 ⟶ 更新性能优化相关组件的保护状态，添加DARepositoryImpl和Flow链处理优化
- 2025-05-17 ⟶ 更新AppDetailScreen和NavGraph，添加页面状态持久性改进保护
- 2025-05-08 ⟶ 添加WakelockHook.kt到关键保护列表，实现统一钩子方法和参数位置自适应功能
- 2025-05-07 ⟶ 添加AlarmHook.kt到关键保护列表，完成统一钩子策略和参数缓存优化
- 2025-05-05 ⟶ 添加ServiceHook.kt到关键保护列表，完成参数位置缓存优化实现
- 2025-05-01 ⟶ 添加测试基础设施和关键功能测试到保护列表
- 2025-05-01 ⟶ 将WakelockRegistry和WakelockCounter核心计时系统组件提升为CRITICAL保护级别
- 2025-04-29 ⟶ AppsScreen.kt加入保护列表，保护单向同步解决方案
- 2025-04-27 ⟶ 添加AppDetailScreen到保护列表，实现Tab内容集成
- 2025-04-25 ⟶ 添加新创建的参数常量类到保护列表
- 2025-04-25 ⟶ 更新导航相关组件的保护状态

## ✅ Approvals
- 2025-05-20 ⟶ 批准对DARepositoryImpl.kt的缓存机制改进，优化数据加载性能
- 2025-05-17 ⟶ 批准对AppDetailScreen的rememberSaveable改进，解决状态持久性问题
- 2025-05-11 ⟶ 批准移除ModuleCheckScreen中的重复TopAppBar，统一使用全局导航栏

## ⚠️ Permission Violations
尚无权限违规记录
