# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## ⚡ Claude Code Configuration

## 🧠 THINKING LEVELS - TASK-BASED STRATEGY

**Use appropriate thinking levels based on task complexity:**

### 📝 **THINK1/THINK (Basic Level)**
just think
**For routine tasks:**
- Reading documentation
- Simple file operations
- Basic status checks
- General questions
- Simple configurations
- And other step

**Manual Triggers:** `think` or `think1`

### 🏗️ **THINK2/MEGATHINK (Code Generation Level)**
think harder
**For all code generation and writing tasks:**
- Writing new functions/classes
- Code refactoring
- Implementing features
- Creating tests
- Code optimization
- Algorithm implementation

**Manual Triggers:** `think2`
**Auto-trigger when:** Any request involving code generation, writing, or implementation

### 🧠 **THINK3/ULTRATHINK (Architecture Level)**
ultrathink
**For complex design and problem-solving:**
- Bug investigation and debugging
- System architecture design
- Project planning and roadmaps
- Third-party API integration design
- Performance troubleshooting
- Complex system migrations
- Security vulnerability analysis

**Manual Triggers:** `think3`  
**Auto-trigger when:** Debugging, designing solutions, or planning complex integrations

### 💡 **Usage Guidelines:**
- **Budget unlimited** - use appropriate level freely
- **Default progression:** Start with think1, escalate as needed
- **Code tasks automatically use think2**
- **Complex problems automatically use think3**

---

## 🔧 Maintenance Phase Guidelines

### Current Project Status
- **Phase**: Version 3.x maintenance and bug fixing
- **Focus**: Stability improvements, compatibility fixes
- **Restrictions**: NO major architectural changes without approval

### Bug Fix Workflow
1. **Investigation Phase** (ULTRATHINK): Root cause analysis
2. **Solution Design** (MEGATHINK): Minimal, targeted fix implementation  
3. **Testing**: Always run full test suite before commits
4. **Verification**: Use subagents for code review validation

### Multi-Instance Strategy
- **Instance 1**: Bug investigation and log analysis
- **Instance 2**: Code implementation and testing
- **Instance 3**: Documentation updates and verification
- **Use git worktrees**: For parallel bug fixes without context switching

## 🛡️ Enhanced Code Protection

### Code Protection Levels

#### CRITICAL_SYSTEM_HOOK
- **Files**: `xposedhook/*.kt`
- **Rule**: Zero tolerance - no modifications without team lead approval
- **Verification**: Must test on multiple Android versions (API 24, 30, 31, 34, 35)

#### PROTECTED_DATABASE  
- **Files**: `data/database/*.kt`, `schemas/*`
- **Rule**: Migration-only changes, no schema breaking changes
- **Verification**: Run migration tests mandatory

#### GUARDED_UI
- **Files**: `ui/screens/*.kt`
- **Rule**: Bug fixes only, no new features in maintenance phase
- **Verification**: UI tests must pass

### Legacy Protection Comments (Still Valid)
- `PROTECTED - DO NOT MODIFY` - Absolute no-change zones
- `GUARDED - ASK BEFORE MODIFYING` - Requires confirmation
- `CRITICAL - BUSINESS LOGIC` - Core functionality protection

## ⚡ Bug Fix Workflow

### Pre-Commit Checklist
- [ ] No new TODO comments added
- [ ] All existing tests pass  
- [ ] No new compiler warnings
- [ ] Hook code changes tested on real device
- [ ] Memory leaks checked for UI changes
- [ ] No breaking changes to ContentProvider
- [ ] Xposed module still loads correctly

### Emergency Hotfix Protocol
For critical bugs affecting device stability:

1. **Immediate Response** (ULTRATHINK)
   - Identify affected Android versions
   - Assess blast radius and impact
   - Document reproduction steps

2. **Hotfix Development** (MEGATHINK)  
   - Minimal code changes only
   - Direct fix without refactoring
   - Comprehensive testing on affected versions

3. **Emergency Release**
   ```bash
   ./gradlew clean
   ./gradlew assembleRelease
   # Emergency testing protocol
   # Direct release to affected users
   ```

## 项目概述

NoWakeLock 是一个先进的 Android Xposed 模块，用于管理设备的 WakeLocks、Alarms 和 Services，以优化电池续航和设备性能。项目采用现代化的 Android 开发技术栈，包括 Kotlin、Jetpack Compose、Room 数据库和 Koin 依赖注入。

## 核心架构

### 1. Xposed Hook 系统
- **入口点**: `XposedModule.kt` - 主要的 Xposed Hook 入口
- **核心 Hook 类**:
  - `WakelockHook.kt` - 管理 WakeLock 的获取和释放
  - `AlarmHook.kt` - 拦截和管理系统 Alarm
  - `ServiceHook.kt` - 控制 Service 的启动和绑定
  - `SettingsProviderHook.kt` - Hook 系统设置提供者

**重要**：Hook 相关代码标记有 `PROTECTED` 和 `CRITICAL` 注释，这些代码不应随意修改。

### 2. 数据库架构
项目使用双数据库设计：

#### AppDatabase (主数据库)
- **版本**: 13
- **实体类**: 
  - `AppInfo` - 应用程序信息
  - `AppSt`, `St` - 应用和全局设置
  - `Info` - DA (Detection/Action) 信息统计
  - `InfoEvent` - 事件记录

#### InfoDatabase (事件数据库)
- **版本**: 12
- **实体类**: 
  - `Info` - 简化的 DA 信息
  - `InfoEvent` - 详细事件记录

**数据库迁移**: 项目具有完善的数据库迁移策略，支持从旧版本无缝升级。

### 3. UI 架构 (Jetpack Compose)
- **主入口**: `NoWakeLockApp.kt` - 应用程序的根 Composable
- **导航**: `NavGraph.kt` - 类型安全的导航图
- **主要屏幕**:
  - `AppsScreen` - 应用列表
  - `DAsScreen` (WakelockScreen, AlarmScreen, ServiceScreen) - DA 管理界面
  - `DADetailScreen` - DA 详情页面
  - `AppDetailScreen` - 应用详情页面
  - `SettingsScreen` - 设置页面
  - `ModuleCheckScreen` - 模块状态检查

### 4. 依赖注入 (Koin)
- **配置文件**: `KoinDSL.kt`
- **主要依赖**:
  - 数据库实例和 DAO
  - Repository 层
  - ViewModel 层
  - 备份管理器

## 🧪 Enhanced Testing Strategy

### Test-Driven Bug Fixing
```bash
# Before any code changes
./gradlew test
./gradlew connectedAndroidTest

# After changes  
./gradlew test --tests="*.$AFFECTED_CLASS*"
./gradlew lint
./gradlew assembleDebug

# Integration verification
adb install app/build/outputs/apk/debug/*.apk
# Manual module check in app
```

### Bug-Specific Test Categories
- **Hook Tests**: `./gradlew test --tests="*Hook*"`
- **Counter Logic**: `./gradlew test --tests="*Counter*"`  
- **Database Operations**: `./gradlew test --tests="*Database*"`
- **UI Components**: `./gradlew test --tests="*Screen*"`

## 常用开发命令

### Bug Investigation Commands
```bash
# Create bug fix branch
git checkout -b bugfix/issue-$ISSUE_NUMBER

# Investigation helpers
./gradlew kspDebugKotlin  # Refresh generated code
adb logcat | grep -i nowakelock  # Live debugging
adb shell dumpsys power | grep -i wake  # System state
adb shell dumpsys alarm | grep -i "$PACKAGE"  # Alarm state

# Database inspection
adb shell run-as com.js.nowakelock cp /data/data/com.js.nowakelock/databases/app_database /sdcard/
adb pull /sdcard/app_database ./debug/

# Memory analysis  
adb shell dumpsys meminfo com.js.nowakelock
```

### Build and Test Commands
```bash
# Build Debug version
./gradlew assembleDebug

# Build Release version  
./gradlew assembleRelease

# Run unit tests
./gradlew test

# Run Android tests
./gradlew connectedAndroidTest

# Run specific test class
./gradlew test --tests="com.js.nowakelock.data.counter.WakelockCounterTest"

# Clean project
./gradlew clean

# Generate test reports
./gradlew testDebugUnitTest --continue
```

### Database Operations
```bash
# Export database schema
./gradlew kspDebugKotlin

# Validate migrations  
./gradlew validateMigrations
```

### Code Quality Checks
```bash
# Kotlin code check (if configured)
./gradlew ktlintCheck

# Android Lint check
./gradlew lint
```

## 项目结构说明

### 核心模块
- `app/src/main/java/com/js/nowakelock/`
  - `xposedhook/` - Xposed Hook 核心逻辑 (**关键模块**)
  - `data/` - 数据层 (数据库、Repository、模型)
  - `ui/` - UI 层 (Compose UI、ViewModel、导航)
  - `base/` - 基础工具类

### Hook 系统工作原理
1. **XposedModule.kt** 在系统启动时被加载
2. Hook 三个关键系统调用：
   - `acquireWakeLockInternal` / `releaseWakeLockInternal` (PowerManagerService)
   - `triggerAlarmsLocked` (AlarmManagerService) 
   - `startServiceLocked` / `bindServiceLocked` (ActiveServices)
3. **跨版本兼容性**: 使用参数位置缓存策略支持 Android 7-15
4. **事件记录**: 通过 `XpRecord.kt` 记录所有拦截和允许的事件

### 数据流
1. **Hook 拦截** → **XpNSP 规则判断** → **XpRecord 事件记录** → **数据库存储**
2. **UI 查询** → **Repository 层** → **Room 数据库** → **事件统计和展示**

### 多用户支持
- 项目原生支持 Android 多用户环境
- 每个用户的数据独立存储和管理
- UI 提供用户切换功能

## 重要开发注意事项

### 1. 保护的代码区域
项目中有以下类型的保护注释，**严禁修改**：
- `PROTECTED - DO NOT MODIFY` - 绝对不可修改的核心逻辑
- `GUARDED - ASK BEFORE MODIFYING` - 修改前需要明确确认
- `CRITICAL - BUSINESS LOGIC` - 关键业务逻辑

### 2. 版本兼容性
- 支持 Android 7.0 (API 24) 到 Android 15 (API 35)
- Hook 参数位置可能在不同 Android 版本间变化
- 使用缓存策略确保跨版本兼容性

### 3. 性能考虑
- Hook 代码运行在系统进程中，必须极其高效
- 数据库操作使用异步处理
- UI 使用 Flow 和 StateFlow 进行响应式更新

### 4. 测试策略
- 单元测试：`app/src/test/` - WakelockCounter、WakelockRegistry 等核心逻辑
- 集成测试：`app/src/androidTest/` - 数据库和 DAO 测试
- UI 测试：基于 Compose 测试框架

### 5. 数据库最佳实践
- 所有数据库操作都应该异步执行
- 使用 Room 的类型转换器处理复杂数据类型
- 数据迁移时保持向后兼容性

## 特殊文件说明

### 关键配置文件
- `app/src/main/assets/xposed_init` - Xposed 模块入口类声明
- `app/src/main/AndroidManifest.xml` - 包含 Xposed 模块元数据
- `app/schemas/` - Room 数据库 Schema 文件

### 构建配置
- 支持 ProGuard/R8 代码混淆和压缩
- 使用 KSP (Kotlin Symbol Processing) 进行 Room 和 Koin 代码生成
- 自动生成版本化 APK 文件名

### 依赖关系
- **核心**: Kotlin 2.1.20, Compose BOM 2025.04.01
- **数据库**: Room 2.7.1
- **DI**: Koin 4.0.4
- **导航**: Navigation Compose 2.8.9
- **Xposed**: API 82 (compileOnly)

## 调试和故障排除

### 1. 模块检查
使用应用内的"模块检查"功能验证：
- Xposed 框架是否激活
- Hook 是否正常工作
- 配置是否正确加载

### 2. 日志调试
- Hook 日志通过 `XpUtil.log()` 输出到 Xposed 日志
- 应用日志使用 Android Log 系统
- 调试模式可在设置中启用

### 3. 数据库问题
- 数据库损坏时会自动使用 `fallbackToDestructiveMigration`
- 可通过设置页面清除应用数据

## 发布流程

### 版本号管理
- 在 `app/build.gradle` 中更新 `versionCode` 和 `versionName`
- 版本号遵循语义化版本控制

### 签名配置
- Release 版本使用调试签名 (开发阶段)
- 生产环境需要配置正式签名

### 兼容性测试
发布前必须在以下环境测试：
- 不同 Android 版本 (特别是 API 24, 30, 31, 34, 35)
- 不同 Xposed 框架 (EdXposed, LSPosed)
- 多用户环境

## 获取帮助

- **项目文档**: README.md 和 Wiki
- **问题反馈**: GitHub Issues
- **社区支持**: Telegram 和 Discord 群组
- **代码审查**: 所有 PR 都需要经过代码审查

---

这个项目是一个复杂的系统级应用，涉及 Android 底层机制。修改时请格外谨慎，特别是 Hook 相关代码。建议在开发环境充分测试后再部署到生产环境。