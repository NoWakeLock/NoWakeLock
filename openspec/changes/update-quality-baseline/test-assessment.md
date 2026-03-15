# Test Assessment

## Current State
- Unit tests: PASS (`:app:testDebugUnitTest`)
- Instrumentation tests (build): PASS (`:app:assembleDebugAndroidTest`)
- Instrumentation tests (connected device): PASS for full `androidTest` suite via `adb shell am instrument` (`Pixel 6a / Android 16`, `OK (10 tests)`)
- Known blockers / constraints:
  - 当前环境下 Gradle 下载新依赖可能出现 Permission denied（尽量不新增远端依赖）。
  - Xposed API：继续通过 JitPack 镜像提供 compile-time 依赖（不提交 jar）；Xposed 相关验证不再纳入自动化测试目标。
  - Unit test source-set 曾存在重复 FQCN（`com.js.nowakelock.data.db.Type`），会 shadow 生产类并引发序列化 `NoSuchFieldError`；已修复。

## Candidate Targets (ROI)
| Target | Test Type | Why High Value | Risk/Notes | Est. |
|---|---|---|---|---|
| Hook rule scoping (`lastAllowTime` key) | Manual / code review | 避免跨包/跨用户串扰导致误拦截 | 代码已最小修复；不再保留 Xposed 相关自动化用例 | Done |
| Service screen-off rule | Manual / code review | 避免“仅灭屏拦截”配置失效 | 代码已最小修复；不再保留 Xposed 相关自动化用例 | Done |
| `data/counter` (`WakelockCounter`/`WakelockRegistry`) | Unit | 统计/持续时长的核心逻辑，回归价值高 | 已覆盖三组单测 | Done |
| `calculateTime(events)` overlap 计算 | Unit | 事件重叠合并的关键纯逻辑 | 已覆盖：`CalculateTimeTest` | Done |
| Backup JSON roundtrip (`Backup`) | Unit | 备份恢复是用户高频风险点（数据兼容性） | 已覆盖：`BackupJsonTest` | Done |
| Preferences enum mapping (`ThemeMode`/`LanguageMode`) | Unit | 防止配置字符串变更导致设置异常 | 已覆盖：`UserPreferencesRepositoryMappingTest` | Done |
| `XProvider` routing + Room integration | Instrumentation | Hook 通信核心，且涉及 DB 写入逻辑 | 已覆盖：`XProviderIntegrationTest`（不依赖 Xposed） | Done |
| Compose UI smoke (MainActivity launch + nav) | Instrumentation | 防止 UI 直接崩溃/路由断裂 | 已覆盖：`MainActivitySmokeTest` | Done |
| Connected-device androidTest execution | Real device | 捕获真实设备上的 UI / Room / provider 差异 | 已执行整套 `androidTest`；通过升级 AndroidX Test 依赖并修复真机断言后，`OK (10 tests)` | Done |

## Proposed Plan
1. 维持质量门槛：`testDebugUnitTest` 必须持续通过；`assembleDebugAndroidTest` 必须可编译。
2. 单元测试优先：Backup JSON、Preferences mapping、counter、rules。
3. Instrumentation：`MainActivityLaunchTest` + `MainActivitySmokeTest` + `XProviderIntegrationTest` + `DAListItemControlSectionLayoutTest`。
4. 对有 Java SSL / UTP 下载限制的主机，先运行 `scripts/bootstrap-android-test-deps.ps1`，再用 `-PuseLocalMavenBootstrap=true --offline` 编译测试 APK。
5. 真机执行优先使用 `scripts/run-connected-android-tests.ps1`；其底层调用 `adb shell am instrument -w com.js.nowakelock.test/androidx.test.runner.AndroidJUnitRunner` 并校验 `OK (N tests)`。
6. Xposed/系统进程相关继续以真机 smoke 为主（见 `device-smoke.md`）。
