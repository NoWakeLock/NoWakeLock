# NoWakeLock #54 Xposed 配置后端调查与实现现状

> 2026-09-05 复核提示：本文为历史调查和未提交实现记录；“已完成”条目是当时记录，本轮未重新运行构建或真机验证。最新框架变化、兼容方案证据和下一轮实验建议见 [2026-09-05 兼容性复核](Xposed_Dual_API_Compatibility_Update_2026-09-05.zh-CN.md)。#54 的配置异常、安全模式和 #55 的无数据现象仍需分别核实，不能仅凭相似症状认定同一根因。

日期：2026-06-13
分支：`feature/dual-xposed-config-backend`
OpenSpec change：`add-dual-xposed-config-backend`

## 结论先行

#54 现在要拆成两个问题看：

1. 原始问题是模块状态页依赖 `/data/misc/<uuid>/prefs/...` 路径扫描，导致 New XSharedPreferences 配置路径被判 invalid。
2. 后续评论新增了 LSPosed safe mode 现象，这更像模块加载或 system_server hook 崩溃，不是单纯配置读取失败。

当前 change 主要解决第 1 类问题：配置发布、配置读取、模块健康检查。它不能承诺修复 safe mode，除非拿到 LSPosed / logcat 崩溃栈并确认 safe mode 根因也在配置读取链路。

## 2026-06-25 实现更新

本轮复盘后，把 change 从「同一 APK 兼容 API100/101/102」收敛为「同一 APK 保留 legacy 入口，并让 API101/API102 完全走 Modern 机制」。API100 不再作为支持目标。

- 保留 legacy `assets/xposed_init`，同时保留 Modern `META-INF/xposed/java_init.list`。
- Modern `module.prop` 为 `minApiVersion=101`、`targetApiVersion=101`，不 target 102。
- `ModernXposedModule` 只暴露 API101+ 无参数构造，不再保留 API100 的 `(XposedContext, ModuleLoadedParam)` 两参数构造。
- App 侧 `XposedRemotePreferencesManager` 要求 service API101+ 且 `PROP_CAP_REMOTE` 可用，API100 直接视为不支持 Remote Preferences。
- Hook 侧配置 reader 改成 generation-specific：legacy 入口使用 XSharedPreferences-only，Modern 入口使用 Remote Preferences-only。remote 可读但缺 key 时使用保守默认值，不回退 legacy。
- Modern `scope.list` 只保留 `system` 与 `android`。SettingsProvider 不是有效 Modern scope target，应通过 `system` scope 等待 `com.android.providers.settings` package loading event。
- API101/API102 的 SettingsProvider/system_server hook 使用 libxposed hook chain 安装；Modern path 不再调用 `XposedBridge`、`XposedHelpers`、`XC_MethodHook`。
- legacy `XposedModule` 检测到 Modern libxposed runtime 时跳过，避免 101/102 框架仍走 legacy 主 hook。

这个实现仍保持单 APK，不拆 flavor、不换 applicationId。旧框架走 legacy，新框架走 Modern。

## 2026-06-25 release/R8 验证目标

当前 release/R8 验证目标已经变更：

1. R8 必须保留 API101+ Modern 入口无参数构造器。
2. R8 不应保留 API100 Modern 入口两参数构造器。
3. release APK 不应打入 `io.github.libxposed.api.*` compileOnly API 类。
4. Modern entry 与 `xposedhook/modern` 包不应引用 legacy `de.robv.android.xposed` API。

已在 `app/proguard-rules.pro` 为 `ModernXposedModule` 明确保留：

- `public <init>()`，供 API101+ runtime 加载。

当前已完成验证：

- `openspec validate add-dual-xposed-config-backend --strict` 通过。
- `./gradlew :app:compileDebugKotlin -PuseLocalMavenBootstrap=true` 通过。
- `./gradlew :app:testDebugUnitTest -PuseLocalMavenBootstrap=true --offline` 通过。
- `./gradlew :app:assembleDebug -PuseLocalMavenBootstrap=true --offline` 通过。
- `./gradlew :app:assembleRelease -PuseLocalMavenBootstrap=true --offline` 通过。
- debug/release APK 均确认包含 `assets/xposed_init`、`META-INF/xposed/java_init.list`、`META-INF/xposed/module.prop`、`META-INF/xposed/scope.list`。
- debug/release `module.prop` 均为 `minApiVersion=101`、`targetApiVersion=101`、`staticScope=true`、`exceptionMode=protective`。
- release DEX 确认 `ModernXposedModule` 只有 `()V` no-arg 构造器，没有 API100 `(XposedContext, ModuleLoadedParam)` 构造器。
- release DEX 确认没有打入 `io.github.libxposed.api.*` 或 `de.robv.android.xposed.*` class definitions。

## Issue 最新时间线

来自 GitHub issue comments API，最后核对时间是 2026-06-13：

- 2026-06-01：maintainer 询问 Xposed 版本。
- 2026-06-01：用户回复 `LSPosed-v2.0.4-7741`。
- 2026-06-04：maintainer 回复 `XPOSED update, will fix on next version.`
- 2026-06-05：新用户补充 `This version of LSPosed will run in safe mode with NoWakeLock enabled.`

## 背景：为什么需要跨进程配置

NoWakeLock 的 UI 进程和被 hook 的系统/应用进程不是同一个进程。用户设置保存在 App 侧，但 wakelock、alarm、service 的判断发生在 hooked process 内。

因此旧实现使用 New XSharedPreferences：App 不需要常驻后台，hook 进程在需要判断规则时直接读取配置。这个方向仍然正确，不能简单废弃，否则会破坏 Xposed 模块的低驻留设计。

旧链路大致是：

- `SPTools` 用 `Context.MODE_WORLD_READABLE` 写 `Nowakelock` SharedPreferences。
- `XpNSP` 在 hook 进程用 `XSharedPreferences(BuildConfig.APPLICATION_ID, "Nowakelock")` 读取。
- 规则 key 形如 `${name}_${type}_${packageName}_${userId}_flag`、`_flag_lock`、`_aTI`，regex key 形如 `${type}_${packageName}_${userId}_rE`。

原始 #54 的 config path invalid 更像健康检查实现的问题：`ModuleCheckManager` 调 `CheckSharedPreferencesPath`，`XProvider` 扫 `/data/misc/<uuid>/prefs/<applicationId>`。LSPosed 文档本来就说明 New XSharedPreferences 的实际文件目录可能是随机目录，不应该靠硬编码路径判断。

## 已核对的官方/上游事实

LSPosed New XSharedPreferences 文档要点：

- LSPosed API 93 起提供 New XSharedPreferences。
- 模块可通过 `xposedminversion >= 93` 或 `xposedsharedprefs` metadata 启用。
- 模块侧仍用 `Context.MODE_WORLD_READABLE` 写偏好。
- hooked process 侧应使用 `XSharedPreferences(packageName, prefFileName)` 读，不应直接依赖文件路径。

LSPosed Modern API 文档要点：

- Modern metadata 使用 `META-INF/xposed/module.prop`、`java_init.list`、`scope.list`。
- `assets/xposed_init` 在 Modern API 文档中属于旧入口，但旧框架/兼容框架仍可能加载。
- Modern API 的 hook 模型不是 legacy `XposedHelpers/XC_MethodHook` 的原样替代。

libxposed 要点：

- `XposedInterface.getRemotePreferences(group)` 可在 Modern entry 中获取 Remote Preferences。
- App 侧通过 `io.github.libxposed:service` 与 framework service 交互，`XposedService.getRemotePreferences(group)` 返回 `SharedPreferences`。
- Maven Central 当前已验证基线：`io.github.libxposed:api:101.0.1`，`io.github.libxposed:service:101.0.0`。
- `service:101.0.0` 要求 compileSdk 36；其 AAR minSdk 是 26，因此当前实现使用 manifest override 保持 App minSdk 24。

## 当前 scope 决策

本 change 的目标已经包含 API101/API102 的 pure Modern hook path，但仍保留 legacy path 给旧框架。

当前目标是：

- 保留 legacy Xposed users。
- 保留 wakelock/alarm/service 现有阻断语义。
- legacy hook 进程从 XSharedPreferences 读取用户设置。
- Modern hook 进程从 Remote Preferences 读取用户设置。
- 模块健康检查改成 backend availability，而不是扫描 `/data/misc`。

明确非目标：

- 不删除 `assets/xposed_init`。
- 不删除 legacy `XSharedPreferences`。
- 不要求 App 常驻后台。
- 不支持 API100 Modern runtime。
- 不把 APK 拆成多个 flavor 或 package。

这点已经在 OpenSpec 的 `proposal.md`、`design.md`、`tasks.md` 里修正。

## 当前实现状态

已新增配置模型与发布链路：

- `data/config/XposedConfigKeys.kt`：集中定义旧 key 格式，避免 app/hook 两侧继续散落拼字符串。
- `data/config/ConfigPublisher.kt`：Room 是 source of truth，发布到 legacy backend 和 remote backend。
- `data/config/XposedRemotePreferencesManager.kt`：注册 libxposed service listener，service bind 后 republish。
- `ConfigSnapshotWriter`：把 `St` 和 `AppSt` 写入 backend，已覆盖 app regex `${type}_${packageName}_${userId}_rE`。

已接入发布触发点：

- App 启动后 `BasicApp` 调 `ConfigPublisher.start()`。
- `DARepositoryImpl` / `DADetailRepositoryImpl` 写 `St` 后发布单条规则。
- `AppDetailRepositoryImpl` 写 `AppSt` 后发布 regex 设置。
- `BackupManager.restoreBackup()` 成功后全量发布。
- `SettingsViewModel.updateDebugMode()` 通过 publisher 写 `debug`。

旧的 `DAsViewModel.syncSt()` UI 副作用已移除，避免只有进入列表页才写配置。

## Hook 侧当前状态

`XpNSP` 仍保留原有公开方法：

- `flag(...)`
- `flagLock(...)`
- `aTI(...)`
- `rE(...)`
- `getDebug()`

但内部改成 `HookConfigReader` facade：

- 默认 reader 是 `EmptyHookConfigReader`，backend 不可用时使用保守默认值。
- legacy entry 显式安装 `LegacyHookConfigReader`，继续读 `XSharedPreferences`。
- Modern entry 调用 `XpNSP.installRemotePreferences(...)` 安装 `SharedPreferencesHookConfigReader`，且不安装 legacy fallback。

`ModernXposedModule` 当前职责：

- 在 `onModuleLoaded()` / `onSystemServerStarting()` / `onPackageReady()` 尝试安装 Remote Preferences reader。
- 在 `onSystemServerStarting()` 通过 libxposed hook chain 安装 wakelock/alarm/service hook。
- 在 `com.android.providers.settings` 中用 libxposed hook chain hook `SettingsProvider.call(...)`，用于 module check / backend status route。

legacy `XposedModule` 仍负责旧框架上的三类拦截：`WakelockHook`、`AlarmHook`、`ServiceHook`。检测到 Modern libxposed runtime 时，legacy entry 会跳过。

## 模块健康检查现状

已把 `CheckSharedPreferencesPath` 的路径扫描替换为 backend status。

当前方向：

- `XProvider` 通过 `CheckConfigBackendStatus` 返回 backend 信息。
- 保留旧 `CheckSharedPreferencesPath` 作为 alias，避免 app/hook 版本不一致时直接断。
- `ModuleCheckResult` 新增 `ConfigBackendStatus`。
- UI 文案从 “New XSharedPreferences path” 改成 “Configuration backend”。
- overall status 判定改成：模块未激活或没有可读 backend 才 ERROR；hook 无数据是 WARNING。

这个变化直接对应 #54 原始 config path invalid 问题。

## 构建与打包现状

已新增：

- `compileOnly io.github.libxposed:api:101.0.0`
- `implementation io.github.libxposed:service:101.0.0`
- `META-INF/xposed/module.prop`
- `META-INF/xposed/java_init.list`
- `META-INF/xposed/scope.list`
- R8 keep：legacy entry、modern entry、install guard

构建调整：

- `compileSdk` 升到 36。
- `targetSdk` 保持 35。
- `minSdk` 保持 24。
- manifest 使用 `tools:overrideLibrary` 覆盖 libxposed service/interface 的 minSdk 26 声明。

## 已跑过的验证

上一轮实现后已通过：

- `openspec validate add-dual-xposed-config-backend --strict`
- `./gradlew :app:compileDebugKotlin -PuseLocalMavenBootstrap=true`
- `./gradlew :app:testDebugUnitTest -PuseLocalMavenBootstrap=true --offline`
- `./gradlew :app:assembleDebug -PuseLocalMavenBootstrap=true --offline`
- `./gradlew :app:assembleRelease -PuseLocalMavenBootstrap=true --offline`
- APK 内容检查：同时存在 `assets/xposed_init` 和 `META-INF/xposed/*`，`scope.list` 为 `system`/`android`。
- merged manifest 检查：`io.github.libxposed.service.XposedProvider` 已合入，authority 为 `com.js.nowakelock.XposedService`。

新增测试覆盖：

- key mapping 保持旧格式。
- `ConfigSnapshotWriter` 写 `St` 和 `AppSt`。
- app-level regex key 发布。
- backend status bundle round-trip。
- module health status 映射。

仍未完成：

- legacy/modern-capable 框架真机 smoke。

## 关于 LSPosed safe mode 的判断

safe mode 不能直接归因于配置路径问题。

如果只是配置读不到，通常表现为：

- 规则不生效。
- debug 开关不生效。
- 模块状态页显示 backend/path invalid。

LSPosed safe mode 更像：

- module load 阶段抛了未捕获异常。
- system_server hook 安装阶段崩溃。
- 某个 ROM / Android 版本的方法签名或 classloader 与现有 hook 假设不一致。
- SettingsProvider hook 或 module check route 在新框架行为下触发异常。

因此当前结论是：

- dual backend change 可以解决 #54 的 config-health/read path 分支。
- safe mode 需要单独看 LSPosed log / logcat stack。
- 没有崩溃栈前，不能承诺 safe mode 已被这个 change 修复。

这不改变 dual backend 的必要性，但会影响 issue 回复和 release note 表述。

## 下一步建议

优先级 1：把 safe mode 证据补齐。

需要向 issue 用户要：

- LSPosed log / module log。
- safe mode 前后的 `logcat` crash stack。
- Android 版本、ROM、设备型号。
- NoWakeLock 版本。
- 是否只启用 NoWakeLock 就复现。
- safe mode 是开机后立即触发，还是打开模块状态页时触发。

优先级 2：完成当前 change 的缺口。

- 补 Remote unavailable fallback 单测。
- 在 legacy LSPosed/EdXposed 场景 smoke：旧拦截仍工作，legacy backend 可读。
- 在 LSPosed v2 / Vector modern-capable 场景 smoke：Remote Preferences 能发布/读取，module check backend status 正常。
- 验证 backup restore、debug toggle、app regex 设置都会 republish。

优先级 3：如果 safe mode log 指向 hook 崩溃，再开单独 bugfix。

可能需要查：

- `WakelockHook` 的 `PowerManagerService` 方法签名。
- `AlarmHook` 的 `AlarmManagerService` 类名/方法签名。
- `ServiceHook` 的 `ActiveServices` 方法签名。
- `AndroidAppHelper.currentApplication()` 是否为空。
- `SettingsProviderHook` 在新 LSPosed 下是否触发异常。

## Issue 回复草稿

可以考虑在 #54 回复：

```text
Thanks for the update. The original invalid config path report and the new LSPosed safe mode report may be two different failure paths.

The config path check is being changed to report actual configuration backend availability instead of scanning /data/misc paths, and we are adding a dual backend so legacy XSharedPreferences remains supported while Remote Preferences can be used on modern-capable frameworks.

For the LSPosed safe mode part, we need LSPosed/module logs or logcat around the safe mode trigger. Safe mode usually indicates a module load or hook crash, so we should not assume it is fixed by the config backend change without a stack trace.
```

## 重要提醒

不要把这个 change 描述成 “支持 API100”。

更准确的 release / PR 表述：

- Add dual Xposed configuration backend.
- Preserve legacy XSharedPreferences.
- Add libxposed Remote Preferences publishing/reading for API101/API102 Modern path.
- Port API101/API102 system_server and SettingsProvider hooks to libxposed hook chain.
- Replace fragile New XSharedPreferences path scanning with backend status.
- Keep one APK with legacy and Modern entries.

如果后续真的要 target API102，需要另开 change 评估 API102-only hook id、detach、hot reload 等能力。

## 参考链接

- NoWakeLock #54：https://github.com/NoWakeLock/NoWakeLock/issues/54
- Issue comments API：https://api.github.com/repos/NoWakeLock/NoWakeLock/issues/54/comments
- LSPosed New XSharedPreferences：https://github.com/LSPosed/LSPosed/wiki/New-XSharedPreferences
- LSPosed Modern API：https://github.com/LSPosed/LSPosed/wiki/Develop-Xposed-Modules-Using-Modern-Xposed-API
- libxposed `XposedInterface`：https://libxposed.github.io/api/io/github/libxposed/api/XposedInterface.html
- libxposed service `XposedService`：https://libxposed.github.io/service/io/github/libxposed/service/XposedService.html
- Maven Central libxposed api：https://central.sonatype.com/artifact/io.github.libxposed/api
- Maven Central libxposed service：https://central.sonatype.com/artifact/io.github.libxposed/service

## 当前相关文件速览

- `openspec/changes/add-dual-xposed-config-backend/`
- `app/src/main/java/com/js/nowakelock/data/config/`
- `app/src/main/java/com/js/nowakelock/xposedhook/model/HookConfigReader.kt`
- `app/src/main/java/com/js/nowakelock/xposedhook/model/XpNSP.kt`
- `app/src/main/java/com/js/nowakelock/xposedhook/ModernXposedModule.kt`
- `app/src/main/java/com/js/nowakelock/data/provider/XProvider.kt`
- `app/src/main/java/com/js/nowakelock/data/manager/ModuleCheckManager.kt`
- `app/src/main/resources/META-INF/xposed/`
