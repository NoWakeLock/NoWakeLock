# Connected Device Instrumentation Validation

## Purpose
- 记录本次 change 在已连接真机上的 `androidTest` 自动化执行情况。
- 仅覆盖 app 内逻辑，不覆盖 Xposed / LSPosed / 系统进程 hook 效果。

## Command
- `adb devices`
- 推荐一键：`powershell -ExecutionPolicy Bypass -File .\scripts\run-connected-android-tests.ps1`
- 拆步：
  - `powershell -ExecutionPolicy Bypass -File .\scripts\bootstrap-android-test-deps.ps1`
  - `./gradlew -PuseLocalMavenBootstrap=true --offline :app:assembleDebug :app:assembleDebugAndroidTest`
  - `adb shell am instrument -w com.js.nowakelock.test/androidx.test.runner.AndroidJUnitRunner`
- 说明：`./gradlew :app:connectedDebugAndroidTest` 在当前 Windows 主机上仍受 AGP 8.9 UTP 宿主依赖解析限制。

## Included Tests
- `com.js.nowakelock.ExampleInstrumentedTest`
- `com.js.nowakelock.data.db.AppDatabaseTest`
- `com.js.nowakelock.data.provider.XProviderIntegrationTest`
- `com.js.nowakelock.ui.MainActivityLaunchTest`
- `com.js.nowakelock.ui.MainActivitySmokeTest`
- `com.js.nowakelock.ui.screens.das.components.DAListItemControlSectionLayoutTest`

## Preconditions
- 已连接一台启用 USB 调试的 Android 设备。
- 设备已解锁，并允许通过 ADB 安装测试 APK。
- 当前代码已满足 `:app:testDebugUnitTest` 和 `:app:assembleDebugAndroidTest` 通过。

## Execution Record
- Date: 2026-03-10
- Device: Pixel 6a (`28201JEGR0XPAJ`)
- Android: 16
- Command: `powershell -ExecutionPolicy Bypass -File .\scripts\run-connected-android-tests.ps1`
- Raw instrumentation command: `adb shell am instrument -w com.js.nowakelock.test/androidx.test.runner.AndroidJUnitRunner`
- Result: PASS (`OK (10 tests)`)
- Failed Tests: None in full suite
- Notes: 为兼容 Android 16，已升级 AndroidX Test 到 `core 1.7.0` / `monitor 1.8.0` / `runner 1.7.0` / `rules 1.7.0` / `espresso-core 3.7.0`，并修复真机失败的断言与空测试类。`connectedDebugAndroidTest` 在当前主机上仍会因 UTP 宿主依赖解析触发 Maven Central SSL/permission failure，因此稳定执行路径仍是脚本 + `adb shell am instrument`。
