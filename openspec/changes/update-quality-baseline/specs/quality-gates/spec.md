# Quality Gates

## Purpose
定义本项目最低的“可验证质量门槛”：在不进行大规模重构的前提下，保证最基本的自动化检查可运行，并能阻止明显回归。

## ADDED Requirements

### Requirement: Unit Tests Are Runnable
项目 MUST提供可运行的单元测试套件，用于验证关键纯逻辑行为（不依赖真机或 Xposed 环境）。

#### Scenario: Run unit tests locally
- **WHEN** 开发者运行 `./gradlew :app:testDebugUnitTest`
- **THEN** 任务执行成功
- **AND** 所有单元测试通过

### Requirement: Instrumentation Tests Are Buildable
项目 MUST提供可编译的 instrumentation 测试源码（包括 Compose UI tests），以支持后续真机/模拟器自动化验证。

#### Scenario: Assemble androidTest APKs
- **WHEN** 开发者运行 `./gradlew :app:assembleDebugAndroidTest`
- **THEN** 任务执行成功
- **AND** 生成 `androidTest` APK

### Requirement: Smoke Test Checklist Exists
项目 MUST提供一份可重复执行的真机 smoke checklist，用于验证 LSPosed/EdXposed 场景下的基础健康度。

#### Scenario: Validate on a real device
- **GIVEN** 已安装并启用 LSPosed/EdXposed
- **WHEN** 按 checklist 完成操作
- **THEN** 模块激活状态与三域观测结果符合预期
