# Change: Update Quality Baseline (No Big Refactor)

## 背景
- 当前项目已在真实用户设备上运行，功能基本可靠，但代码主要为手搓实现，缺少系统性的测试与质量门槛。
- 时间有限，不做大规模重构；风险与收益不成正比。
- 目标是用“最小风险”换取“明显的可靠性提升”：先排掉最严重隐患，再补上关键测试。

## 目标
1. 通过一次“只盯严重问题”的 code review，识别并修复会导致崩溃/数据损坏/安全与隐私问题/明显错误的缺陷。
2. 建立可运行的自动化测试基线：单元测试 + 仪器测试（含 Compose UI 测试）能编译并通过，并可在已连接真机上执行 app 内测试。
3. 在不重构架构的前提下，为最核心的纯逻辑模块补上高价值测试，并形成后续扩展的测试策略。

## 非目标（明确不做）
- 不做架构重构、分层重排、命名规范化、风格统一、性能重写等“美化型”工作。
- 不追求全量覆盖率指标；以关键路径与高风险点优先。
- 不尝试在自动化里完整模拟 Xposed/LSPosed 系统进程环境；相关验证以真机手工 smoke 为主。

## 当前基线（已知现状）
- `:app:testDebugUnitTest` ✅ 已通过（修复历史空测试类 + 补齐关键纯逻辑单测）。
- `:app:assembleDebugAndroidTest` ✅ 已通过（升级 AndroidX Test 依赖到 Android 16 兼容版本，并修复真机失败的 Compose/Room 测试）。
- 已连接真机自动化：✅ 已在 `Pixel 6a / Android 16` 上执行整套 `androidTest` 并记录结果；当前稳定入口为 `scripts/run-connected-android-tests.ps1`，其底层通过 `adb shell am instrument` 运行整套件。`connectedDebugAndroidTest` 仍受宿主 UTP 依赖下载限制。
- Xposed API：继续通过 JitPack 镜像提供 compile-time 依赖，不提交 jar；Xposed/LSPosed 相关验证仍以手工真机 smoke 为主。
- 环境注意：当前环境下 Gradle 下载新依赖可能遇到 Permission denied，尽量避免引入新的远端依赖。

## 方法
### 1) Critical-only Code Review
- 输出：`review-findings.md`（只列 Severity-1/2）
- Severity 定义：
  - S1: 可能导致 crash/bootloop/模块不可用、数据损坏、明显安全/隐私问题
  - S2: 明显错误且容易触发（例如规则判定错误导致过度拦截）
- 原则：只做最小修复，不引入大改动

### 2) 建立“质量门槛”（Quality Gates）
- 以本 change 的 `tasks.md`、`test-assessment.md`、`connected-device-validation.md` 记录本次最小门槛。
- 重点是让测试“可运行、可维护、可扩展”，而不是一次性把覆盖率做满。
### 3) 测试补齐分两步
- 评估：盘点哪些模块适合单测、哪些适合 Robolectric、哪些必须 instrumentation、哪些只能真机手测。
- 执行：按优先级逐步补齐，保证每增加一组测试都能稳定通过并长期保留。

### 4) 真机验证分层
- 已连接真机的自动化验证，限定在 app 内可控范围：Compose UI、布局回归、Room / `XProvider` 集成。
- Xposed / LSPosed / 系统进程 hook 效果，继续使用手工 smoke checklist，不强行塞进 instrumentation 自动化。
- 所有真机自动化执行结果单独记录，避免与手工 Xposed 验证混淆。

## 验收标准（Definition of Done）
- `./gradlew :app:testDebugUnitTest` 通过。
- `./gradlew :app:assembleDebugAndroidTest` 通过（至少能编译 androidTest）。
- 在有设备连接的前提下，完成整套 `androidTest` 真机执行，并保留执行记录；当前稳定入口为 `powershell -ExecutionPolicy Bypass -File .\\scripts\\run-connected-android-tests.ps1`，若需拆步执行则使用 `adb shell am instrument` 回退路径。
- 至少补齐一组核心纯逻辑单测（优先 `data/counter` 与规则判定/事件聚合）。
- 提供一份分层的真机验证清单：自动化 app 内验证 + 手工 Xposed smoke。

## 风险与缓解
- 风险：测试需要引入/调整依赖导致构建时间上升。
  - 缓解：只引入必要依赖；优先纯逻辑测试，避免重型模拟。
- 风险：Xposed 环境不可在 CI 自动化复现。
  - 缓解：将系统进程相关验证拆成手工 smoke + app 侧/纯逻辑自动化。
