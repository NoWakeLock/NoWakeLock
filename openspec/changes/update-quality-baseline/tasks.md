# Tasks: Update Quality Baseline

> 原则：先评估，再动手；修 bug 只修 S1/S2；不做大重构。

## 0. Baseline Capture
- [x] 记录当前分支的构建/测试状态（命令 + 结果 + 失败原因），作为对比基线。
  - `./gradlew :app:testDebugUnitTest`
  - `./gradlew :app:assembleDebugAndroidTest`

## 1. Critical-only Code Review (No Refactor)
- [x] 建立 `review-findings.md`，只记录 S1/S2（崩溃/数据损坏/安全隐私/明显错误）。
- [x] 聚焦检查以下高风险边界（不做风格/命名调整）：
  - Xposed hooks: 反射、版本分支、异常处理、系统进程稳定性
  - ContentProvider: 权限/导出/注入/SQL 注入风险、越权读取
  - 配置读取: XSharedPreferences 失败降级、默认值安全性
  - 并发/Flow/Room: 线程切换、竞态、主线程 I/O
- [x] 为每个发现补齐最小复现路径与“最小修复方案”（避免重构）。

## 2. Apply Minimal Fixes (S1/S2 Only)
- [x] 对 S1/S2 问题逐项做最小修复（不改架构、不动大范围文件组织）。
- [x] 每修复一项都补一个回归测试（若可自动化），否则记录到真机 smoke checklist。
- [x] 重新运行 baseline 命令，确保修复没有引入新失败。
## 3. Testing Assessment (What Can We Add?)
- [x] 盘点当前可测试点与缺口（输出 `test-assessment.md`）：
  - 单元测试：纯逻辑（mapping/validation/rules/counter）
  - Robolectric：需要 Android framework 但不需要真机
  - Instrumentation：Compose UI、ContentProvider、Room 集成
  - 真机 smoke：LSPosed scope、系统进程 hooks 观测
- [x] 选定本次优先补齐的测试目标（按 ROI 排序），并标注原因/风险/预估工作量。

## 4. Implement Tests (Incremental)
### 4.1 Make Existing Tests Runnable
- [x] 修复 unit test “No runnable methods” 问题（恢复或重写最小可运行用例）。
- [x] 修复 `androidTest` Compose 测试编译阻塞（确保 `:app:assembleDebugAndroidTest` 通过）。
### 4.2 Add High-Value Unit Tests
- [x] `data/counter`：`WakelockCounter` / `WakelockRegistry` 的关键行为（正常序列、异常序列、边界条件）。
- [x] `base/calculateTime`：Wakelock 事件重叠合并逻辑。
- [x] 配置相关：JSON 备份导入导出校验（`BackupJsonTest`）。
- [x] Preferences mapping：`ThemeMode` / `LanguageMode` 字符串映射（`UserPreferencesRepositoryMappingTest`）。

### 4.3 Add Instrumentation Tests
- [x] Compose UI smoke：启动关键页面并断言基本稳定（不追求复杂交互，见 `MainActivitySmokeTest`）。
- [x] ContentProvider：基础读写/权限边界（仅在可控环境下，见 `XProviderIntegrationTest`；直接测路由 + Room 集成，不依赖 Xposed）。

### 4.4 Real Device Smoke Checklist
- [x] 输出 `device-smoke.md`：至少覆盖模块激活检查、三域事件可观测、拦截策略保守不致系统异常。
- [x] 明确测试前置条件（LSPosed 安装、作用域勾选、重启、日志收集路径）。
- [x] 将真机验证拆分为两层：自动化 app 内验证 vs 手工 Xposed smoke。

### 4.5 Connected Device Instrumentation
- [x] 输出 `connected-device-validation.md`：明确自动化范围、命令、前置条件、结果记录模板。
- [x] 升级 `androidTest` 依赖到 Android 16 兼容版本，并修复真机失败的测试断言/空测试类。
- [x] 在已连接真机上完成整套 `androidTest` 执行；`connectedDebugAndroidTest` 仍受宿主 UTP 下载链路限制，使用 `adb shell am instrument` 跑完整套件。
- [x] 提供一键真机执行脚本（`scripts/run-connected-android-tests.ps1`），固化 bootstrap / assemble / install / instrument 流程。
- [x] 记录设备信息、执行时间、结果与失败用例（如有）。

## 5. Quality Gate Confirmation
- [x] `./gradlew :app:testDebugUnitTest` 通过。
- [x] `./gradlew :app:assembleDebugAndroidTest` 通过。
- [x] 整套 `androidTest` 在已连接真机上通过（记录见 `connected-device-validation.md`）。
- [x] 所有新增测试稳定（无随机失败），并可重复执行。
