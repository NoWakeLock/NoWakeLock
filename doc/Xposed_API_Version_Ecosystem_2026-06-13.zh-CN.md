# Xposed API 版本生态与模块兼容策略调查

日期：2026-06-13
仓库：`NoWakeLock`
相关 issue：`https://github.com/NoWakeLock/NoWakeLock/issues/54`
相关 change：`add-dual-xposed-config-backend`

## 结论先行

这次调查要把几个容易混在一起的概念拆开：

- `API 93` 属于 LSPosed legacy 扩展时代，核心是 New XSharedPreferences；它不是 libxposed Modern API。
- `API 100` 是 libxposed Modern API 的过渡版本；Vector v2.0 release 明确把自己定位成 API100 时代的确定实现。
- `API 101` 是当前已发布到 Maven Central 的 libxposed 稳定基线：`api:101.0.1`、`service:101.0.0`。
- `API 102` 已经出现在 libxposed api/service 仓库默认分支 README 中，但 Maven metadata 仍显示 release/latest 是 101.x；因此它只能作为观察项，不能作为 NoWakeLock 当前基线。
- API100 与 API101 不是简单升版本；构造函数、生命周期、hook 模型、service capability 模型都有破坏性变化。

对 NoWakeLock 的直接含义：不能只把 `module.prop` 写成 `minApiVersion=101`，否则 API100 用户会在框架管理器中被直接拦截；也不能只把 `minApiVersion` 降到 100，因为当前 API101-only `XposedModule` 类在 API100 runtime 下并不二进制兼容。

## 资料来源优先级

本轮没有可用的 context7 工具入口，因此采用下面的一手来源优先级：

- 上游官方 Wiki：LSPosed Modern API、New XSharedPreferences、rovo89 XposedBridge API Wiki。
- 上游源码/Release/API：GitHub API、raw source、release body、Maven metadata。
- libxposed API/service tag 与 AIDL 源码。
- 已有模块仓库的真实 `module.prop`、entry list、entry class、compile-time shim。
- Reddit、Telegram mirror、第三方指南只作为辅助线索，不作为主要事实依据。

## API 版本线

### rovo89 legacy API 82

原始 XposedBridge 文档仍以 `de.robv.android.xposed:api:82` 为典型 Gradle 依赖示例，并强调 API jar 只能 provided/compileOnly，实际实现由框架提供。该文档还说明 Xposed API 版本只在框架 API 改动时才增长，并尽量保持旧模块兼容。

这个时代的模块入口是 `assets/xposed_init`，典型接口是 `IXposedHookLoadPackage`、`IXposedHookZygoteInit`、`XposedBridge`、`XposedHelpers`、`XC_MethodHook`。NoWakeLock 当前三类阻断 hook 仍属于这条 legacy 路线。

参考：

- `https://raw.githubusercontent.com/wiki/rovo89/xposedbridge/Using-the-Xposed-Framework-API.md`
- `https://github.com/rovo89/XposedBridge`

### LSPosed API 93 与 New XSharedPreferences

LSPosed New XSharedPreferences Wiki 明确写明：自 LSPosed API 93 起，框架为 sdk > 27 的模块提供新的 `XSharedPreferences` 支持。模块可以通过 `xposedminversion >= 93` 或 `xposedsharedprefs` metadata 启用。

这个机制的核心不是 Modern API，而是 legacy 入口的配置共享改造：

- 模块 app 侧继续用 `Context.getSharedPreferences(name, Context.MODE_WORLD_READABLE)` 写配置。
- LSPosed hook `ContextImpl.getPreferencesDir()` 和 `ContextImpl.checkMode(int)`，让 world-readable preference 在新目录模型下可用。
- hooked process 侧应该用 `XSharedPreferences(packageName, prefFileName)` 读取。
- 文档明确提醒不要用 `XSharedPreferences(File)`，因为实际文件在随机目录中。

这正好解释了 issue #54 的原始风险：NoWakeLock 健康检查不应该硬扫 `/data/misc/<uuid>/prefs/...` 路径；正确关注点是 hook 进程是否能通过 backend 读到配置。

参考：

- `https://raw.githubusercontent.com/wiki/LSPosed/LSPosed/New-XSharedPreferences.md`

### LSPosed Modern API 与 Remote Preferences

LSPosed Modern API Wiki 把现代入口改为：

- Java entry：`META-INF/xposed/java_init.list`
- module config：`META-INF/xposed/module.prop`
- scope：`META-INF/xposed/scope.list`
- Java entry class：实现/继承 `io.github.libxposed.api.XposedModule`

Modern API Wiki 的内容共享表非常关键：

- New XSharedPreferences：Legacy(ext)，从 v2.1.0 起不支持，存储位置是 `/data/misc/<random>/prefs/<module>`。
- XSharedPreferences：Legacy，从 v2.0.0 起支持，读模块 app internal storage。
- Remote Preferences：Modern，从 v1.9.0 起支持，存储在 LSPosed database，支持 change listener。
- Remote Files：Modern，从 v1.9.0 起支持，适合大内容。

因此长期方向可以是 Remote Preferences，但不能因此删除 legacy XSharedPreferences。NoWakeLock 的 hook 逻辑仍要服务大量 legacy 用户，而且 issue #54 的目标是配置 backend 可读，不是纯 Modern-only hook 迁移。

参考：

- `https://raw.githubusercontent.com/wiki/LSPosed/LSPosed/Develop-Xposed-Modules-Using-Modern-Xposed-API.md`

### libxposed API 100

API100 是 Modern API 的过渡版本。Vector v2.0 release 的说明很直接：API100 从未正式发布，Vector v2.0 是 API100 时代的 definitive implementation，并基于 API101 跳变前的提交。

API100 已经有 Remote Preferences 相关能力，但它的 runtime ABI 与 API101 不同，不能用 API101-only 模块类直接运行。

参考：

- `https://github.com/JingMatrix/Vector/releases/tag/v2.0`
- `https://api.github.com/repos/JingMatrix/Vector/releases/latest`

### libxposed API 101

API101 是当前可作为模块开发稳定基线的 Modern API。证据有三层：

- Maven Central metadata 显示 `io.github.libxposed:api` latest/release 是 `101.0.1`。
- Maven Central metadata 显示 `io.github.libxposed:service` latest/release 是 `101.0.0`。
- libxposed API PR #51 标题是 `RFC for final stable API`，合并时间为 2026-03-17，正文明确说明 API100 常量移除、runtime `getApiVersion()` 返回 101，并且这次合并后准备 Maven Central 发布。

API101 的几个破坏性变化：

- `XposedModule` 不再通过构造函数接收 `XposedInterface` 和 `ModuleLoadedParam`。
- hook 模型改成 OkHttp-style interceptor chain。
- `onSystemServerLoaded` 改为 `onSystemServerStarting`。
- `onPackageLoaded` 的 classloader 访问语义变化，并新增 `onPackageReady`。
- `getFrameworkPrivilege()` 被 `getFrameworkProperties()` bitmask 替代。

参考：

- `https://repo1.maven.org/maven2/io/github/libxposed/api/maven-metadata.xml`
- `https://repo1.maven.org/maven2/io/github/libxposed/service/maven-metadata.xml`
- `https://github.com/libxposed/api/pull/51`

### libxposed API 102

截至 2026-06-13，libxposed api/service 仓库默认分支已经是 `102`，README 中也出现了 `api:102.0.0` 和 `service:102.0.0` 的示例依赖。

但 Maven metadata 仍显示：

- `io.github.libxposed:api` latest/release 是 `101.0.1`。
- `io.github.libxposed:service` latest/release 是 `101.0.0`。

所以 API102 是上游开发线，不是当前 NoWakeLock 应该编译/发布绑定的稳定基线。文档与实现里应避免把 “GitHub 默认分支最新” 和 “Maven 可用稳定版” 混写。

参考：

- `https://raw.githubusercontent.com/libxposed/api/102/README.md`
- `https://raw.githubusercontent.com/libxposed/service/102/README.md`
- `https://repo1.maven.org/maven2/io/github/libxposed/api/maven-metadata.xml`
- `https://repo1.maven.org/maven2/io/github/libxposed/service/maven-metadata.xml`

## API100 与 API101 的具体不兼容

### 模块构造函数

API100 的 `XposedModule` 只有构造函数：

```text
XposedModule(XposedContext base, ModuleLoadedParam param)
```

API101 的 `XposedModule` 继承 `XposedInterfaceWrapper`，不再定义两参数构造，框架通过 `attachFramework()` 注入。

因此如果模块只按 API101 编译并只提供 no-arg 构造，API100 runtime 尝试按旧构造实例化时可能失败。把 `minApiVersion` 改成 100 不能自动解决这个 ABI 问题。

参考源码：

- `https://raw.githubusercontent.com/libxposed/api/100/api/src/main/java/io/github/libxposed/api/XposedModule.java`
- `https://raw.githubusercontent.com/libxposed/api/101.0.1/api/src/main/java/io/github/libxposed/api/XposedModule.java`

### 生命周期与 classloader

API100 侧生命周期更接近：

- `onPackageLoaded(PackageLoadedParam)`
- `onSystemServerLoaded(SystemServerLoadedParam)`

API101 侧改为：

- `onModuleLoaded(ModuleLoadedParam)`
- `onPackageLoaded(PackageLoadedParam)`
- `onPackageReady(PackageReadyParam)`
- `onSystemServerStarting(SystemServerStartingParam)`

API101 PR #51 明确说明 package loading lifecycle 被拆成两段，classloader 从 `PackageLoadedParam` 移到 `PackageReadyParam`。因此 API100/101 兼容 entry 需要同时处理两个生命周期模型，不能只 override API101 方法。

### Hook API 模型

API100 仍有 `hookBefore`、`hookAfter`、`hook(Method, hooker)` 等 before/after callback 风格接口。

API101 删除旧 hook overload，变为：

- `hook(Executable)` 返回 `HookBuilder`。
- Hooker 实现 `intercept(Chain)`。
- priority、exception mode 在 builder 上配置。

这说明 pure Modern hook 迁移本身也要分 API100/101 两套适配；对 issue #54 而言，不应把它扩大成三类阻断 hook 的 Modern 重写。

### Service AIDL 与 capability 风险

API100 service AIDL 的 transaction #5 是：

```text
int getFrameworkPrivilege() = 5;
```

API101 service AIDL 的 transaction #5 是：

```text
long getFrameworkProperties() = 5;
```

这意味着使用 API101 `service` wrapper 去调用 API100 framework service 时，`frameworkProperties` 可能读到 API100 的 privilege 值，并把它误判成 capability bit。Remote Preferences 的 request/update transaction 仍在 #20/#21，概念上可 try/fallback，但能力位不能直接信。

对 NoWakeLock 的约束：app 侧判断 Remote Preferences 可用时，不能只依赖 API101 `PROP_CAP_REMOTE`。如果要兼容 API100，必须按 runtime API/version 或实际 request/update 成功结果做降级。

参考源码：

- `https://raw.githubusercontent.com/libxposed/service/100/interface/src/main/aidl/io/github/libxposed/service/IXposedService.aidl`
- `https://raw.githubusercontent.com/libxposed/service/101.0.0/interface/src/main/aidl/io/github/libxposed/service/IXposedService.aidl`

## 当前框架与分支状态

### rovo89/XposedBridge

状态：archived。

GitHub API 显示仓库 archived，最近 push 在 2021-03-23。它是 legacy XposedBridge API 源头，主要价值是旧 API 语义和历史兼容说明，不是当前 Android 14/15/16 的活跃框架选择。

参考：`https://api.github.com/repos/rovo89/XposedBridge`

### ElderDrivers/EdXposed

状态：基本停止活跃维护。

GitHub API 显示仓库没有 archived，但最近 push 在 2022-04-12；latest release 是 `v0.5.2.2`，发布时间 2021-02-15。release body 仍在谈 XSharedPreferences file watcher、Android 11 resource hook 等旧时代问题。

NoWakeLock 仍应把它视为 legacy 用户来源之一，但不应围绕 EdXposed 引入 Modern API 依赖策略。

参考：

- `https://api.github.com/repos/ElderDrivers/EdXposed`
- `https://api.github.com/repos/ElderDrivers/EdXposed/releases/latest`

### LSPosed/LSPosed 官方仓库

状态：archived。

GitHub API 显示 `LSPosed/LSPosed` archived，最近 push 在 2025-03-04；latest release 是 `v1.9.2`，发布时间 2023-10-11。

v1.9.2 release body 对 NoWakeLock 有两个重要线索：

- 修复 system_server 上 Remote Preferences listener 不工作。
- 修复 New XSharedPreferences 的错误路径。

这说明 LSPosed 官方早期就同时处理 legacy New XSharedPreferences 和 Modern Remote Preferences，但官方 release 并不是 API101 最终稳定路线。

参考：

- `https://api.github.com/repos/LSPosed/LSPosed`
- `https://api.github.com/repos/LSPosed/LSPosed/releases/latest`

### JingMatrix/Vector

状态：当前最重要的活跃 root/zygisk Xposed 框架分支。

GitHub API 显示仓库未 archived，最近 push 在 2026-05-04，latest release 是 `v2.0`，发布时间 2026-03-22。

Vector v2.0 release 明确说：

- 项目从 LSPosed 过渡到 Vector。
- API101 发布后生态迁向新的 breaking standard。
- API100 从未正式发布，Vector v2.0 是 API100 时代的 definitive implementation。
- v2.0 基于 API101 跳变前的提交，用于依赖 legacy libxposed APIs 的用户。

同时，Vector master 已经不等同于 v2.0 release：

- `xposed/libxposed` 子模块指向 `libxposed/api` commit `edeb837...`，这个 commit 正是 tag `101.0.1`。
- `services/libxposed` 子模块指向 `libxposed/service` commit `11f894...`，README 与 AIDL 仍是 API101 模型。

因此文档和实现要分清：Vector release v2.0 是 API100；Vector master/后续 debug 构建已经是 API101 方向。

参考：

- `https://api.github.com/repos/JingMatrix/Vector`
- `https://api.github.com/repos/JingMatrix/Vector/releases/latest`
- `https://api.github.com/repos/JingMatrix/Vector/contents/xposed/libxposed?ref=master`
- `https://api.github.com/repos/JingMatrix/Vector/contents/services/libxposed?ref=master`

### libxposed/api 与 libxposed/service

状态：活跃维护，但它们是 API 库，不是最终用户安装的框架。

GitHub API 显示两个仓库都未 archived，默认分支都是 `102`，最近 push 均在 2026-06。但 Maven Central 仍显示稳定 release 是 101.x。

NoWakeLock 当前如果采用 Maven 依赖，应以 `api:101.0.1` 和 `service:101.0.0` 为稳定依赖；如果后续跟进 API102，需要另开调查和兼容 change。

参考：

- `https://api.github.com/repos/libxposed/api`
- `https://api.github.com/repos/libxposed/service`
- `https://raw.githubusercontent.com/libxposed/api/101.0.1/README.md`
- `https://raw.githubusercontent.com/libxposed/service/101.0.0/README.md`

### LSPosed/LSPatch 与 7723mod/NPatch

LSPatch 是 non-root Xposed framework，官方 `LSPosed/LSPatch` 仓库已 archived，最近 push 在 2023-12-13。

NPatch 是更活跃的 rootless/patch 方向，`7723mod/NPatch` GitHub API 显示未 archived，最近 push 在 2026-06-08。README 写明它是 rootless implementation，通过向目标 APK 插入 dex/so 来集成 Xposed API，支持版本理论上跟随 JingMatrix/LSPosed。

这些项目对普通 app hook 模块有参考价值，但对 NoWakeLock 的 system_server wakelock/alarm/service 阻断价值有限，因为 NoWakeLock 需要系统进程 scope 和低驻留跨进程配置。

参考：

- `https://api.github.com/repos/LSPosed/LSPatch`
- `https://api.github.com/repos/7723mod/NPatch`
- `https://raw.githubusercontent.com/7723mod/NPatch/master/README.md`

### 社区 fork：mywalkb/LSPosed_mod、LSPosed-Irena、ReLSPosed

这些 fork 需要作为用户现实存在考虑，但不应作为 NoWakeLock 的主设计基线。

已核对状态：

- `mywalkb/LSPosed_mod`：未 archived，但 latest release 是 `v1.9.3_mod`，发布时间 2024-03-08，最近 push 2024-09-30；不是 API100/101 主线。
- `re-zero001/LSPosed-Irena`：archived，最近 push 2026-05-22；社区 fork，不宜作为主目标。
- `ThePedroo/ReLSPosed`：archived，fork 自 Vector，最近 push 2026-02-09；社区 fork，不宜作为主目标。

参考：

- `https://api.github.com/repos/mywalkb/LSPosed_mod`
- `https://api.github.com/repos/mywalkb/LSPosed_mod/releases/latest`
- `https://api.github.com/repos/re-zero001/LSPosed-Irena`
- `https://api.github.com/repos/ThePedroo/ReLSPosed`

## 模块生态的兼容做法

这里记录已有模块如何面对 API100/101/legacy 分裂。它们不是都适合 NoWakeLock，但能说明社区实际有多种策略。

### 策略一：API101-only

代表：`libxposed/example`、`LSPosed/DisableFlagSecure`。

共同特征：

- `module.prop` 写 `minApiVersion=101`、`targetApiVersion=101`。
- 使用 `META-INF/xposed/java_init.list`。
- 不提供 legacy `assets/xposed_init` 作为旧框架 fallback。
- 对 API100 用户会直接显示框架 API 不满足。

参考文件：

- `https://raw.githubusercontent.com/libxposed/example/master/app/src/main/resources/META-INF/xposed/module.prop`
- `https://raw.githubusercontent.com/libxposed/example/master/app/src/main/resources/META-INF/xposed/java_init.list`
- `https://raw.githubusercontent.com/libxposed/example/master/app/build.gradle.kts`
- `https://raw.githubusercontent.com/LSPosed/DisableFlagSecure/master/app/src/main/resources/META-INF/xposed/module.prop`
- `https://raw.githubusercontent.com/LSPosed/DisableFlagSecure/master/app/src/main/resources/META-INF/xposed/java_init.list`

对 NoWakeLock 的判断：不适合直接采用。NoWakeLock 明确需要兼容旧用户和 Vector API100 用户；API101-only 会复现用户本地看到的 “模块需要 101，本地框架是 100”。

### 策略二：API100-only

代表：`CaptureSposed`。

其 `module.prop` 当前是：

```text
minApiVersion=100
targetApiVersion=100
staticScope=true
```

这类模块面向 Vector/API100 时代，能避开 API101-only 对 API100 的阻断，但如果后续框架只保留 API101，就需要迁移。

参考文件：

- `https://raw.githubusercontent.com/99keshav99/CaptureSposed/master/app/src/main/resources/META-INF/xposed/module.prop`
- `https://github.com/99keshav99/CaptureSposed/issues/78`
- `https://github.com/99keshav99/CaptureSposed/issues/77`

对 NoWakeLock 的判断：也不适合直接采用。NoWakeLock 不能倒退成 API100-only，因为后续稳定 Modern API 是 101。

### 策略三：legacy-only

代表：`Hide-My-Applist`。

它仍使用 legacy entry：

```text
xposed/src/main/assets/xposed_init
```

参考文件：

- `https://raw.githubusercontent.com/Dr-TSNG/Hide-My-Applist/master/xposed/src/main/assets/xposed_init`

对 NoWakeLock 的判断：当前 NoWakeLock 的三类阻断 hook 与 legacy-only 策略一致，但 issue #54 需要更可靠的配置 backend 和健康检查；只保留 legacy 无法覆盖 Remote Preferences 的长期方向。

### 策略四：单 APK 多入口兼容

重点参考：`ljy6-6-6/MiHealth_AmapFix`。

这是本轮找到的最有参考价值的公开案例。README 明确写明单 APK 保留三段入口兼容：

- LSPosed Modern API 101
- LSPosed Modern API 100
- 低于 100 的 Legacy 入口

该仓库不是简单把 `minApiVersion` 调低，而是用 entry 与 compile-time shim 同时覆盖 API100/101/legacy。

参考文件：

- `app/src/main/resources/META-INF/xposed/module.prop`
- `app/src/main/resources/META-INF/xposed/java_init.list`
- `app/src/main/resources/META-INF/xposed/scope.list`
- `app/src/main/assets/xposed_init`
- `app/src/main/java/io/github/mihealthamapfix/ModernEntry.java`
- `app/src/main/java/io/github/mihealthamapfix/LegacyInit.java`
- `stubs/src/main/java/io/github/libxposed/api/XposedModule.java`
- `stubs/src/main/java/io/github/libxposed/api/XposedInterface.java`
- `stubs/src/main/java/io/github/libxposed/api/XposedModuleInterface.java`
- `stubs/src/main/java/io/github/libxposed/api/XposedInterfaceWrapper.java`

原始链接：

- `https://github.com/ljy6-6-6/MiHealth_AmapFix`
- `https://raw.githubusercontent.com/ljy6-6-6/MiHealth_AmapFix/master/app/src/main/resources/META-INF/xposed/module.prop`
- `https://raw.githubusercontent.com/ljy6-6-6/MiHealth_AmapFix/master/app/src/main/java/io/github/mihealthamapfix/ModernEntry.java`
- `https://raw.githubusercontent.com/ljy6-6-6/MiHealth_AmapFix/master/app/src/main/java/io/github/mihealthamapfix/LegacyInit.java`
- `https://raw.githubusercontent.com/ljy6-6-6/MiHealth_AmapFix/master/stubs/src/main/java/io/github/libxposed/api/XposedModule.java`

#### MiHealth_AmapFix 的 metadata

`module.prop`：

```text
minApiVersion=100
targetApiVersion=101
staticScope=true
```

Modern entry：

```text
META-INF/xposed/java_init.list -> io.github.mihealthamapfix.ModernEntry
```

Legacy entry：

```text
assets/xposed_init -> io.github.mihealthamapfix.LegacyInit
```

也就是说它让 API100/101 框架都能识别 Modern metadata，同时保留低版本 legacy fallback。

#### ModernEntry 的核心做法

`ModernEntry` 继承 `XposedModule`，同时提供两个构造函数：

```java
public ModernEntry() { super(); }

public ModernEntry(XposedInterface base, XposedModuleInterface.ModuleLoadedParam param) {
    super(base, param);
    rememberModuleLoaded(param);
}
```

这正是 API101 no-arg 构造与 API100 两参数构造的兼容桥。

它还同时 override：

- `onModuleLoaded(...)`
- `onPackageLoaded(...)`
- `onPackageReady(...)`

并用 `getApiVersion()` 判断是否等待 API101 的 `onPackageReady()` 再安装 hook：

```text
frameworkApiVersion >= 101 -> 等 onPackageReady
frameworkApiVersion < 101 -> onPackageLoaded 中安装
```

这说明多版本兼容不是只靠 metadata，而是 entry class 本身要能被两个 runtime 正确实例化和回调。

#### compile-time shim 的作用

MiHealth_AmapFix 本地 `stubs/.../XposedModule.java` 同时声明了：

```java
public XposedModule() {}

public XposedModule(XposedInterface base, XposedModuleInterface.ModuleLoadedParam param) {
    attachFramework(base);
}
```

这使一个源码树能编译出同时包含 API100/101 构造形态的 entry class。运行时真实框架仍提供实际实现，stub 只是用于编译期生成兼容字节码。

对应的 `XposedModuleInterface` stub 也同时放入 API100/101 生命周期需要的 param 与 callback：

- `PackageLoadedParam.getDefaultClassLoader()`
- `PackageLoadedParam.getClassLoader()`
- `PackageReadyParam`
- `SystemServerStartingParam`
- `onModuleLoaded`
- `onPackageLoaded`
- `onPackageReady`
- `onSystemServerStarting`

#### 它的业务 hook 没有纯 Modern 重写

MiHealth_AmapFix 的一个重要现实点：业务 hook 仍使用 legacy `XposedBridge` / `XposedHelpers` / `XC_MethodHook`。

Modern entry 的主要价值是兼容加载入口与生命周期，而不是把所有 hook 都改写成 API101 interceptor chain。这一点和 NoWakeLock 当前目标相近：issue #54 需要解决配置读取/发布，不要求马上把 wakelock/alarm/service 三类 hook 全部迁移到 Modern hook API。

#### 可借鉴但不能直接照抄的地方

可借鉴：

- `minApiVersion=100`、`targetApiVersion=101` 用于覆盖 API100/101 Modern 管理器识别。
- 保留 legacy `assets/xposed_init` 覆盖旧框架。
- Modern entry 同时提供 API100/101 构造与回调。
- 关键 hook 安装有重复保护。

需要 NoWakeLock 单独评估：

- NoWakeLock 需要 Remote Preferences，不只是入口加载。
- NoWakeLock 需要 system_server scope，MiHealth 主要是普通 app scope。
- NoWakeLock app 侧还使用 `io.github.libxposed:service`，API100/101 service capability 差异必须单独处理。
- NoWakeLock 当前 Gradle 依赖官方 API101，如果要采用 shim，需要避免把真实 API101 jar 与本地 stub 冲突。

## 对 NoWakeLock 的建议兼容目标

基于 issue #54 和用户反馈，建议把当前 change 的兼容目标写成：

- Legacy API93+：继续使用 `assets/xposed_init` 与 New XSharedPreferences / XSharedPreferences 路线。
- Vector v2.0 / API100：Modern metadata 不能写死 `minApiVersion=101`；entry 需要 API100 构造兼容。
- libxposed API101：采用 Maven Central 稳定版本 `api:101.0.1`、`service:101.0.0`，并兼容 API101 lifecycle/service model。
- API102：只观察，不作为当前实现要求。

这不是 pure Modern-only 支持目标。当前目标仍是解决 #54：让配置 backend 和健康检查可靠，同时保留已有 legacy 阻断 hook。

## 对当前本地改动的风险提示

当前本地 change 中如果存在：

```text
minApiVersion=101
targetApiVersion=101
```

它会直接导致 API100 用户被管理器提示框架版本不足。用户本地 “模块需要 101，本地是 100” 就是这个现象。

如果简单改成：

```text
minApiVersion=100
targetApiVersion=101
```

仍不完整，因为 API100 runtime 可能需要两参数构造函数，而当前 API101-only `XposedModule` 子类不一定有这个构造。正确修复应同时处理：

- metadata 兼容。
- Modern entry class 的 API100/101 构造兼容。
- API100/101 lifecycle 兼容。
- app-side service capability 探测不能误读 transaction #5。
- legacy `XSharedPreferences` reader 必须继续作为 fallback。

## 和 issue #54 的关系

issue #54 的原始配置问题可以这样归因：

- New XSharedPreferences 本身仍然是 legacy 用户需要的能力。
- 但 LSPosed 文档明确说明其真实文件路径在随机目录，不能靠固定 `/data/misc/<uuid>` 扫描判断是否有效。
- 更长期的 backend 是 Modern Remote Preferences，但它需要 Modern entry/service 能力。
- 兼容用户不能只支持 API101-only；API100/legacy 用户仍要保留。

因此当前最合理的长期方案不是废除 New XSharedPreferences，而是 dual backend：legacy XSharedPreferences 保留，Remote Preferences 在可用时优先/补充，并且健康检查从 path scanning 改成 backend availability。

## 后续实现检查清单

这份文档不直接修改代码，但给后续实现设置以下检查点：

- `module.prop` 不应无条件 `minApiVersion=101`，否则 API100 用户不可用。
- 如果 `minApiVersion=100`，Modern entry 必须有 API100 构造兼容方案。
- 如果同时存在 Modern 与 legacy entry，要有重复 hook/install guard。
- Remote Preferences app-side service 不应只信 API101 `frameworkProperties` capability。
- hook-side 读配置必须保持 conservative default：backend 不可读时不崩溃、不阻断。
- legacy key 格式必须保持稳定，避免用户设置丢失。
- 真机 smoke 至少覆盖 legacy API93+、Vector API100、API101-capable 框架三类。

## 参考链接汇总

官方/上游文档：

- LSPosed Modern API Wiki：`https://raw.githubusercontent.com/wiki/LSPosed/LSPosed/Develop-Xposed-Modules-Using-Modern-Xposed-API.md`
- LSPosed New XSharedPreferences Wiki：`https://raw.githubusercontent.com/wiki/LSPosed/LSPosed/New-XSharedPreferences.md`
- rovo89 XposedBridge API Wiki：`https://raw.githubusercontent.com/wiki/rovo89/xposedbridge/Using-the-Xposed-Framework-API.md`
- libxposed API PR #51：`https://github.com/libxposed/api/pull/51`

Maven 与 API 源码：

- `https://repo1.maven.org/maven2/io/github/libxposed/api/maven-metadata.xml`
- `https://repo1.maven.org/maven2/io/github/libxposed/service/maven-metadata.xml`
- `https://raw.githubusercontent.com/libxposed/api/100/api/src/main/java/io/github/libxposed/api/XposedModule.java`
- `https://raw.githubusercontent.com/libxposed/api/101.0.1/api/src/main/java/io/github/libxposed/api/XposedModule.java`
- `https://raw.githubusercontent.com/libxposed/service/100/interface/src/main/aidl/io/github/libxposed/service/IXposedService.aidl`
- `https://raw.githubusercontent.com/libxposed/service/101.0.0/interface/src/main/aidl/io/github/libxposed/service/IXposedService.aidl`

框架仓库：

- `https://api.github.com/repos/LSPosed/LSPosed`
- `https://api.github.com/repos/LSPosed/LSPosed/releases/latest`
- `https://api.github.com/repos/JingMatrix/Vector`
- `https://api.github.com/repos/JingMatrix/Vector/releases/latest`
- `https://api.github.com/repos/libxposed/api`
- `https://api.github.com/repos/libxposed/service`
- `https://api.github.com/repos/rovo89/XposedBridge`
- `https://api.github.com/repos/ElderDrivers/EdXposed`
- `https://api.github.com/repos/LSPosed/LSPatch`
- `https://api.github.com/repos/7723mod/NPatch`

模块案例：

- libxposed example：`https://github.com/libxposed/example`
- DisableFlagSecure：`https://github.com/LSPosed/DisableFlagSecure`
- CaptureSposed：`https://github.com/99keshav99/CaptureSposed`
- Hide-My-Applist：`https://github.com/Dr-TSNG/Hide-My-Applist`
- MiHealth_AmapFix：`https://github.com/ljy6-6-6/MiHealth_AmapFix`
