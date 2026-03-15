# Real Device Validation Checklist

## Scope Split
- 自动化真机验证：仅覆盖 app 内可控范围，如 Compose UI、窄宽布局、Room / `XProvider` 集成。
- 手工真机 smoke：覆盖 LSPosed / Xposed / 系统进程 hook 的真实效果。

## Automated Checks on Connected Device
### Preconditions
- 已开启 USB 调试，`adb devices` 可看到目标设备。
- 设备已解锁，允许安装与运行测试 APK。
- 先完成一次 `./gradlew :app:assembleDebugAndroidTest`。

### Command
- 推荐一键执行：`powershell -ExecutionPolicy Bypass -File .\scripts\run-connected-android-tests.ps1`
- 拆步执行：
  - 依赖预热：`powershell -ExecutionPolicy Bypass -File .\scripts\bootstrap-android-test-deps.ps1`
  - 编译测试 APK：`./gradlew -PuseLocalMavenBootstrap=true --offline :app:assembleDebug :app:assembleDebugAndroidTest`
  - 真机执行：`adb shell am instrument -w com.js.nowakelock.test/androidx.test.runner.AndroidJUnitRunner`
- 说明：`./gradlew :app:connectedDebugAndroidTest` 在当前主机上仍受 AGP 8.9 UTP 宿主依赖下载链路限制。

### Expected Coverage
1. `MainActivityLaunchTest`：应用启动且 Activity 保持存活，不发生启动即崩溃。
2. `MainActivitySmokeTest`：主界面与设置页导航稳定。
3. `DAListItemControlSectionLayoutTest`：窄宽场景下 control section 不裁切、能换行。
4. `XProviderIntegrationTest`：`XProvider` 路由、数据写入、清理与 `Room` 集成。
5. 其他 instrumentation 历史测试：不应再出现 `No runnable methods` 一类初始化错误。

## Manual Xposed Smoke
### Preconditions
- 已安装并启用 LSPosed/EdXposed。
- 作用域至少勾选：`android`、`com.js.nowakelock`。
- 完整重启一次（确保模块在系统进程加载）。

### Checklist
1. 打开应用 -> 模块检查页：模块状态显示为已激活。
2. 分别在 Wakelock/Alarm/Service 域触发至少一条可观测事件：检查页对应域显示为 OK 或能看到数据增长。
3. 配置一条“保守”的拦截规则（例如仅灭屏拦截或最小间隔），验证不会导致系统异常（无 bootloop、无系统 UI 崩溃）。
4. 关闭规则或恢复默认：确认系统行为回归正常。

## Logs / Evidence
- 自动化：记录设备型号、Android 版本、测试命令、Gradle 结果、失败类名（如有）。
- 手工：记录设备型号、Android 版本、ROM、LSPosed 版本、NoWakeLock 版本。
- 若失败：附上相关日志（Logcat / LSPosed 日志）与复现步骤。

## Pass/Fail
- PASS: `run-connected-android-tests.ps1` 或等价的 `adb shell am instrument` 整套 `androidTest` 通过，且手工 smoke 满足预期、无崩溃/卡死。
- FAIL: 自动化或手工任一步骤不符合预期，或出现系统级异常。
