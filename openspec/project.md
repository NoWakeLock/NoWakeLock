# Project Context

## Purpose
NoWakeLock 是一个 Android 应用 + Xposed/LSPosed 模块，提供按应用粒度的
Wakelock/Alarm/Service 控制，用于减少不必要的唤醒并改善电池续航。

## Tech Stack
- Android (minSdk 24, targetSdk 35)
- Kotlin (2.1.x), Gradle/AGP, JDK 17
- UI: Jetpack Compose + Material 3, Navigation Compose
- DI: Koin（Compose 侧通过 koinViewModel 注入）
- Storage: Room（AppDatabase）、DataStore（偏好设置）
- Serialization: kotlinx.serialization（JSON）
- Image loading: Coil
- Xposed: Xposed API（XposedBridge），兼容 LSPosed/EdXposed

## Project Conventions

### Code Style
- Kotlin 优先；函数保持短小，副作用尽量集中在边界层。
- 异步与状态：优先使用 coroutines + Flow/StateFlow。
- Compose：尽量保持 Composable 无状态；状态由 ViewModel 管理。
- 反射与版本适配：集中放在 xposedhook 工具中，避免散落。

### Architecture Patterns
- UI：MVVM（ViewModel + StateFlow）。
- 数据：按功能拆分 Repository（wakelock/alarm/service/app detail 等）。
- Xposed hooks 运行在系统进程，通过以下方式与应用通信：
  - ContentProvider（事件记录、模块检查）
  - 可读 SharedPreferences（XSharedPreferences 在 Xposed 侧读取）

### Testing Strategy
- 单元测试：JUnit4、Mockito、Robolectric、coroutines-test。
- 仪器测试：AndroidX Test + Compose UI tests。
- 优先测试纯逻辑（映射、校验、repository），尽量不依赖真实 Xposed 环境。

### Git Workflow
- 主要分支：dev（beta）、feature（实验）、master（稳定）。
- PR 保持聚焦，避免把无关的文档和代码修改混在一起。

## Domain Context
- 代码中的 “DA” 指三类可控域：Wakelock、Alarm、Service。
- AppSt：按应用维度的开关与正则 pattern 集合。
- St：按条目维度的设置（全量拦截、仅灭屏拦截、时间窗口/最小间隔等）。
- 模块至少需要勾选作用域：
  - android
  - com.js.nowakelock

## Important Constraints
- 错误配置拦截可能导致无法开机；默认策略要保守。
- 某些 OEM ROM 可能不支持或仅部分支持（例如 Samsung OneUI）。
- 无遥测：除非需求明确，否则所有数据仅在本地处理与存储。
- XSharedPreferences 依赖偏好文件可读；需要对读取失败做降级处理，不能假设配置一定存在。

## External Dependencies
- Xposed 框架：LSPosed 或 EdXposed。
- XposedBridge API 通过 JitPack 镜像获取。
- 文档站点：MkDocs（多语言文档位于 `docs/`）。
