# σ₆: Protection Registry
*v1.0 | Created: 2025-04-25 | Updated: 2025-05-08*
*Π: 🏗️DEVELOPMENT | Ω: ⚙️E*

## 🛡️ Protected Regions

### 测试基础设施
- **[GUARDED]** `app/src/test/java/com/js/nowakelock/data/counter/TestUtils.kt` - 测试工具类
  - 保护理由: 提供重置单例对象和模拟日志的核心测试功能，被多个测试类依赖
  - 最后修改: 2025-05-01 (实现单例重置和反射访问功能)

- **[GUARDED]** `app/src/test/java/com/js/nowakelock/data/counter/WakelockTests.kt` - 测试套件
  - 保护理由: 控制测试执行顺序，确保测试间隔离，解决状态污染问题
  - 最后修改: 2025-05-01 (创建测试套件)

### 关键功能测试
- **[GUARDED]** `app/src/test/java/com/js/nowakelock/data/counter/WakelockRegistryBasicTest.kt` - 注册表基础测试
  - 保护理由: 验证WakelockRegistry核心功能的测试，确保单例模式和基本操作正确性
  - 最后修改: 2025-05-01 (创建独立测试类)

- **[GUARDED]** `app/src/test/java/com/js/nowakelock/data/counter/WakelockRegistryProblemTest.kt` - 注册表边缘情况测试
  - 保护理由: 验证WakelockRegistry复杂情况的测试，确保状态维护和边界条件处理
  - 最后修改: 2025-05-01 (创建独立测试类)

- **[GUARDED]** `app/src/test/java/com/js/nowakelock/data/counter/WakelockCounterTest.kt` - 计数器测试
  - 保护理由: 验证WakelockCounter非重叠持续时间计算的正确性，核心算法测试
  - 最后修改: 2025-05-01 (实现计数器功能测试)

### 计时系统核心组件
- **[CRITICAL]** `app/src/main/java/com/js/nowakelock/data/counter/WakelockRegistry.kt` - 唤醒锁注册表
  - 保护理由: 核心单例类，管理所有唤醒锁计数器并提供统一接口，全应用共享实例
  - 最后修改: 2025-04-30 (实现非重叠计时系统)

- **[CRITICAL]** `app/src/main/java/com/js/nowakelock/data/counter/WakelockCounter.kt` - 唤醒锁计数器
  - 保护理由: 核心计时算法实现，确保非重叠持续时间计算准确
  - 最后修改: 2025-04-30 (实现非重叠持续时间计算)

### Critical Infrastructure
- **[CRITICAL]** `app/src/main/java/com/js/nowakelock/KoinDSL.kt` - 依赖注入配置
  - 保护理由: 核心依赖注入模块，影响整个应用的组件实例化
  - 最后修改: 2025-04-25 (添加SavedStateHandle注入)

- **[CRITICAL]** `app/src/main/java/com/js/nowakelock/xposedhook/hook/ServiceHook.kt` - 服务Hook实现
  - 保护理由: 核心Xposed Hook组件，使用参数位置缓存优化，影响整个应用的性能
  - 最后修改: 2025-10-20 (实现参数位置缓存优化)

- **[CRITICAL]** `app/src/main/java/com/js/nowakelock/xposedhook/hook/AlarmHook.kt` - 闹钟Hook实现
  - 保护理由: 核心Xposed Hook组件，使用统一钩子策略和参数缓存优化，影响应用功能和性能
  - 最后修改: 2025-05-07 (实现统一钩子策略和参数缓存优化)

- **[CRITICAL]** `app/src/main/java/com/js/nowakelock/xposedhook/hook/WakelockHook.kt` - 唤醒锁Hook实现
  - 保护理由: 核心Xposed Hook组件，包含handleWakeLockAcquire、handleWakeLockRelease和block等受保护方法，对系统唤醒锁监控至关重要
  - 最后修改: 2025-05-08 (添加统一钩子方法和参数位置自适应功能，保持所有受保护代码不变)

### 导航系统
- **[GUARDED]** `app/src/main/java/com/js/nowakelock/ui/navigation/NavRoutes.kt` - 路由定义
  - 保护理由: 定义应用导航结构，需谨慎修改
  - 最后修改: 2025-04-25 (添加类型路由类)

- **[GUARDED]** `app/src/main/java/com/js/nowakelock/ui/navigation/NavGraph.kt` - 导航图
  - 保护理由: 核心导航实现，影响所有页面跳转
  - 最后修改: 2025-04-25 (支持类型导航)

### UI组件
- **[GUARDED]** `app/src/main/java/com/js/nowakelock/ui/components/TopAppBars.kt` - 顶部应用栏
  - 保护理由: 全局UI组件，出现在所有页面
  - 当前问题: 路由检测逻辑需更新以支持类型导航
  - 最后修改: 2025-04-27 (更新 shouldShowSearch 方法支持 AppDetail 路由)

- **[GUARDED]** `app/src/main/java/com/js/nowakelock/ui/components/BottomNavBar.kt` - 底部导航栏
  - 保护理由: 全局UI组件，负责主要页面跳转
  - 最后修改: 2025-04-25 (添加混合导航支持)

### 屏幕实现
- **[GUARDED]** `app/src/main/java/com/js/nowakelock/ui/screens/appdetail/AppDetailScreen.kt` - 应用详情屏幕
  - 保护理由: 复杂的标签页界面实现，集成多个功能组件
  - 最后修改: 2025-04-27 (实现 Tab 内容集成和懒加载)

- **[GUARDED]** `app/src/main/java/com/js/nowakelock/ui/screens/das/DAsScreen.kt` - 设备自动化列表屏幕
  - 保护理由: 核心功能屏幕，被多处复用
  - 最后修改: 2025-04-15 (初始实现)

### 模型和参数定义
- **[GUARDED]** `app/src/main/java/com/js/nowakelock/ui/navigation/params/AppsScreenParams.kt` - Apps参数常量
  - 保护理由: 定义与Apps相关的所有参数名称，确保类型安全
  - 最后修改: 2025-04-25 (新创建)

- **[GUARDED]** `app/src/main/java/com/js/nowakelock/ui/navigation/params/DAsScreenParams.kt` - DAs参数常量
  - 保护理由: 定义与DAs相关的所有参数名称，确保类型安全
  - 最后修改: 2025-04-25 (新创建)

### 视图模型
- **[GUARDED]** `app/src/main/java/com/js/nowakelock/ui/screens/apps/AppsViewModel.kt` - Apps视图模型
  - 保护理由: 包含关键业务逻辑，处理用户筛选、排序和应用选择
  - 最后修改: 2025-04-25 (改用SavedStateHandle)

- **[GUARDED]** `app/src/main/java/com/js/nowakelock/ui/screens/das/DAsViewModel.kt` - DAs视图模型
  - 保护理由: 处理唤醒锁、闹钟和服务的数据管理
  - 最后修改: 2025-04-25 (改用SavedStateHandle，添加应用筛选)

- **[GUARDED]** `app/src/main/java/com/js/nowakelock/ui/screens/apps/AppsScreen.kt` - AppsScreen
  - 保护理由: 关键多用户UI同步逻辑，涉及配置变更下的状态恢复与循环同步风险，需严格遵循单向同步和保护规则
  - 最后修改: 2025-04-29 (修复语言切换后用户切换无限循环问题，采用单向同步方案3)

## 📜 Protection History
- 2025-05-08 ⟶ 添加WakelockHook.kt到关键保护列表，实现统一钩子方法和参数位置自适应功能，同时保持所有受保护代码不变
- 2025-05-07 ⟶ 添加AlarmHook.kt到关键保护列表，完成统一钩子策略和参数缓存优化实现
- 2025-05-01 ⟶ 添加测试基础设施(TestUtils, WakelockTests)和关键功能测试到保护列表
- 2025-05-01 ⟶ 将WakelockRegistry和WakelockCounter核心计时系统组件提升为CRITICAL保护级别
- 2025-04-29 ⟶ AppsScreen.kt 加入保护列表，修复语言切换后用户切换无限循环问题，采用单向同步方案3
- 2025-04-27 ⟶ 添加 AppDetailScreen 到保护列表，实现了 Tab 内容集成
- 2025-04-27 ⟶ 更新 TopAppBars.kt 的保护记录，添加最新修改
- 2025-04-27 ⟶ 添加 DAsScreen.kt 到保护列表，被 AppDetailScreen 集成使用
- 2025-04-25 ⟶ 添加新创建的参数常量类到保护列表
- 2025-04-25 ⟶ 更新导航相关组件的保护状态
- 2025-04-25 ⟶ 将TopAppBars.kt标记为需要进一步调整的组件
- 2025-10-20 ⟶ 添加ServiceHook.kt到关键保护列表，完成参数位置缓存优化实现

## ✅ Approvals
*尚无修改批准记录*

## ⚠️ Permission Violations
*尚无权限违规记录*
