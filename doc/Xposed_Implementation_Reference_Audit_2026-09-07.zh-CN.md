# 指定旧端与 API 102 实施参考核证

日期：2026-09-07。目标旧端是用户 K30SU 的 JingMatrix LSPosed v1.11.0 (7209)，不是官方 LSPosed 1.9.2。新端实现使用官方稳定 API/service 102；框架装载行为以 Vector 2.2 固定源码核证。本报告不修改业务代码，不声称设备实验完成。

## 实施决定

单 APK 的可执行方向是：保留 legacy 入口用于真正选择 legacy 的框架，同时提供兼容旧 Modern 构造与新 Modern 无参构造的入口；指定 1.11.0 通过旧 Modern 回调调度旧业务，Vector 2.2 通过新 Modern 回调进入纯 API 102 业务。**指定旧端不会因 modern 不兼容而自动回退 assets/xposed_init。** 桥接还必须补旧配置协议和系统回调，不能只增加一个构造函数。[旧解析器]、[旧上下文]

建议 metadata 为 `minApiVersion=100`、`targetApiVersion=102`，前提是确实实现本报告指定的旧协议桥。它表示桥支持该旧 ABI，不表示承诺所有曾经叫 API 100 的分支。旧管理器会对 target 高于框架显示警告；源码不支持把该警告说成必然拒绝加载。Vector 2.2 按 target >=101 优先 modern，不会因另有 legacy entry 而选择旧实现。[旧模块识别]、[旧模块列表]、[新解析器]

## 来源固定与用户 ZIP

用户 ZIP SHA256 `6b8455e08dfa13a8121619fe013a79b40e3e5a1291555c91cc7ee1870a56b65c` 与 GitHub 官方 v1.11.0 release asset 的 digest 完全相同；解压 module.prop 确认 v1.11.0 (7209)。发布 commit 为 `aed70305b9251404b9b900f3d8b65b9918d55097`。[官方发布](https://github.com/JingMatrix/Vector/releases/tag/v1.11.0)

已下载八个固定 source archive，保存原始 LICENSE；位置和完整 SHA 见 [参考清单](../tmp/xposed-reference/README.md)。框架 source archive 不含子模块，已补下载这两个版本实际引用的 API/service gitlink。此处确认的是官方发布物身份以及对应源码结构，没有反编译全套 APK/DEX，也没有证明可重复构建。

## 1.11.0 的确定事实

| 环节 | 本版实际行为 | 实施影响 |
|---|---|---|
| 模块分类 | 非空 META-INF/xposed/java_init.list 优先；仅为空才读 assets/xposed_init | 双 metadata 不能保住本版的 legacy 装载路径 |
| 构造 | 精确查找 `(XposedInterface, ModuleLoadedParam)` | 无参-only 入口确定不能实例化 |
| 父类 | 本版 gitlink 的 XposedModule 只有上述二参构造，内部 super(base) | 编译 shim 必须提供两个真实 descriptor；不能打包替代框架 API |
| 系统回调 | onSystemServerLoaded(SystemServerLoadedParam)，classLoader 来自 system server | 不能只实现新 onSystemServerStarting；转接旧业务需要 android/system_server 语义明确 |
| 普通包 | onPackageLoaded；旧 param.getClassLoader | 不等 onPackageReady；不能链接只有新 API 才有的方法 |
| 类加载 | 本版没有 target102 的 legacy 包名前缀禁令；禁止模块内置框架 API | 旧桥可调用 legacy hooks；compileOnly shim 不得进 DEX |

依据：[旧解析器]、[旧上下文]、[旧 API 父类]、[旧系统回调]、[旧类加载器]。

min/target 的检查位置必须区分：daemon ConfigFileManager 不读取 module.prop；manager ModuleUtil 读取 min/target，ModulesFragment 显示过高版本 warning。本轮未发现 daemon 根据 min102 自动拒绝并转 legacy 的代码。无论 warning 如何显示，二参构造要求不变。[旧解析器]、[旧模块识别]、[旧模块列表]

旧 Modern 装载时 XposedInit 把模块记录成 Optional.empty；New XSP 的模块自身 Context 改道和 XSharedPreferences(package,name) 特殊路径均要求值 present。因此“旧桥直接调用原 legacy 业务，并继续原样使用 New XSP”缺少必要条件。这里不等同所有 ROM 上旧 XML 文件绝对不可读，而是本框架不会为这个分类自动提供 New XSP 路径。激活检测也不能再只依赖 legacy 自身 hook。[旧注册]、[旧自身配置]、[旧 XSP]

## 旧 service 是否需要独立适配

**需要协议分派；不必把远程 prefs 的全部实现复制一份。** 旧 LSPModuleService 对 modern 模块 App 投递 `.XposedService` / `SendBinder`，实现远程 prefs 读取、diff 更新和注入侧通知；旧 LSPosedContext 已有 getRemotePreferences。因此旧业务读取可改为注入的配置接口，由旧 modern adapter 提供 SharedPreferences。[旧模块服务]、[旧上下文]

固定旧 service AIDL 与 Vector 2.2 引用的 service AIDL 对照：

| AIDL 显式 transaction 编号 | 旧协议 | 新协议 | 结论 |
|---|---|---|---|
| 1 | getAPIVersion → int | getApiVersion → int | 可用作最先进行的版本握手；Java 名变而 wire 形状相同 |
| 5 | getFrameworkPrivilege → int | getFrameworkProperties → long | 不能混用，旧 root=0 不代表新 capability mask=0 |
| 10 | getScope → List<String> | 相同 | 只读 scope wire 可复用 |
| 11 | requestScope(String,callback) | requestScope(List<String>,callback) | 参数不同，需独立适配 |
| 12 | removeScope(String) → String | removeScope(List<String>) → void | 参数及结果不同 |
| 20/21/22 | prefs request/update/delete | 同编号同签名 | 可复用这部分 wire；仍应版本门控并验证 Bundle 格式 |
| 30/31/32 | files list/open/delete | 同编号同签名 | 能力相同不等于已验证生命周期行为 |

依据：[旧服务 AIDL]、[新服务 AIDL]。上述数字是 AIDL 显式编号；若手写 transact 必须遵循生成 Stub 的 FIRST_CALL_TRANSACTION 偏移，不能直接把表中数字当绝对 Binder code。

实施建议：先读 API 版本，再为指定旧版走旧能力模型及旧 scope 方法；102 走正式稳定库接口。若沿用新库接收 provider/binder，只使用已核对 wire 兼容的方法，不提前调用 properties 或新 scope；需要旧 scope 时将旧接口放入隔离适配器，保留正确 interface token。禁止在同 APK 打包两个同名 IXposedService 类型。新旧 prefs 都要检查首启全量发布、diff 删除、debug、重启恢复与 Binder 重连。

## API 102 的类隔离和 scope

Vector 2.2 target>=102 的模块 loader 拒绝 legacy API 前缀访问，包括反射。旧业务可存在 APK 的不活动类中，新分支不能引用或初始化旧 hook 类；入口公共字段、日志、静态初始化也必须避免 legacy 类型。新入口用无参构造、框架 attach 后再分派。[新模块管理器]、[新类加载器]

现代 scope 中 `system` 明确映射到 `system_server` / UID 1000；`android` 是另一个普通包标识，不可当同义词。旧管理器仅针对 legacy metadata 做 android/system 名称反转，modern scope.list 原样读取。因此现代列出 system 是系统业务的必要项；是否保留 android 和 SettingsProvider 取决于实际 hook 目标，而非 API102 要求的固定三件套。[新 scope 缓存]、[旧模块识别]

scope.list 是声明/推荐，不等于该目标此刻已获授权和已装载。API102 的 running targets 可补运行诊断，不能以声明列表替代真实加载证据。新稳定库 API 细节以 [稳定能力报告](Xposed_Stable_API_Capabilities_2026-09-05.zh-CN.md) 及主代理固定的 102.0.0 artifact 为准；本参考目录 new-api/new-service 是 Vector 2.2 的 gitlink，不应以它们的历史类型名覆盖稳定 artifact。

## 案例能够支持什么

MiHealth 固定源码是一个无 flavor 的 APK，ModernEntry 同时提供双构造，compileOnly stubs 编译；实际 hooks 仍调用 XposedHelpers/XC_MethodHook。它提供旧构造桥的源代码参考，**不提供 target102 纯 modern 的实现样板**。它也没有覆盖 NoWakeLock 所需旧系统回调；stub 的返回 descriptor 要按上述指定框架校准，不可整包照搬。[MiHealth 入口]、[MiHealth stub]、[MiHealth 构建]

GlassMic 固定源码一个 APK 引入 :xposed library，保留 legacy entry，同时 ModernEntry 通过 hook().intercept 安装实际业务 hook、读取 remote prefs；可借鉴两套业务 API 隔离。其 modern 入口只有无参构造，不能直接满足本次 1.11.0。源码关于 zygote 全局继承 hook 的注释不能作为事实依据。两案例均不是我们已在 K30SU 上确认的发布二进制兼容证明。[GlassMic 入口]、[GlassMic 构建]

## 实施和验证边界

1. 固定旧二参 ABI + 新102无参 ABI 的 compileOnly 入口 shim；增加旧系统 callback，旧实现只经旧分派可达。逐 descriptor 检查，不依赖“类存在”判断分支成功。
2. old-modern 与 new-modern 都提供配置接口；旧 service 精确版本握手，禁止新 properties/scope 误调用旧协议。legacy 环境仍可保留既有 XSP backend。
3. metadata min100/target102，列出真正需要的现代目标；统一成功后才记账的业务安装 guard，入口日志分别报告实际回调和配置来源。
4. 主机可确认：两个构造/父类调用 descriptor、旧 callback descriptor、API shim 未进 APK、两入口 metadata、102 可达类不引用 legacy、旧新 Binder 序列化合同。
5. ART/设备仍需确认：双构造类遇到另一端缺失父类构造是否验证通过、R8 后旧新分派可达性、system_server 时序、首启/升级/重启远程配置、wake lock/alarm/service 实际规则以及单次 hook。先构建可诊断最小桥，再在 K30SU 验证旧端；新端必须另有 API102 环境。

因此不是“已完成只差真机”：源码已经确定需要入口、旧回调、配置及协议适配；完成这些修改和主机验证后，才进入设备阶段。没有证据要求重写所有旧业务，但也不能保留旧框架环境假设不变。

[旧解析器]: https://github.com/JingMatrix/Vector/blob/aed70305b9251404b9b900f3d8b65b9918d55097/daemon/src/main/java/org/lsposed/lspd/service/ConfigFileManager.java#L432
[旧上下文]: https://github.com/JingMatrix/Vector/blob/aed70305b9251404b9b900f3d8b65b9918d55097/core/src/main/java/org/lsposed/lspd/impl/LSPosedContext.java#L114
[旧模块识别]: https://github.com/JingMatrix/Vector/blob/aed70305b9251404b9b900f3d8b65b9918d55097/app/src/main/java/org/lsposed/manager/util/ModuleUtil.java
[旧模块列表]: https://github.com/JingMatrix/Vector/blob/aed70305b9251404b9b900f3d8b65b9918d55097/app/src/main/java/org/lsposed/manager/ui/fragment/ModulesFragment.java#L560
[旧 API 父类]: https://github.com/libxposed/api/blob/54582730315ba4a3d7cfaf9baf9d23c419e07006/api/src/main/java/io/github/libxposed/api/XposedModule.java
[旧系统回调]: https://github.com/JingMatrix/Vector/blob/aed70305b9251404b9b900f3d8b65b9918d55097/core/src/main/java/org/lsposed/lspd/hooker/StartBootstrapServicesHooker.java#L56
[旧类加载器]: https://github.com/JingMatrix/Vector/blob/aed70305b9251404b9b900f3d8b65b9918d55097/core/src/main/java/org/lsposed/lspd/util/LspModuleClassLoader.java
[旧注册]: https://github.com/JingMatrix/Vector/blob/aed70305b9251404b9b900f3d8b65b9918d55097/core/src/main/java/de/robv/android/xposed/XposedInit.java#L242
[旧自身配置]: https://github.com/JingMatrix/Vector/blob/aed70305b9251404b9b900f3d8b65b9918d55097/core/src/main/java/org/lsposed/lspd/hooker/LoadedApkCreateCLHooker.java#L126
[旧 XSP]: https://github.com/JingMatrix/Vector/blob/aed70305b9251404b9b900f3d8b65b9918d55097/core/src/main/java/de/robv/android/xposed/XSharedPreferences.java#L164
[旧模块服务]: https://github.com/JingMatrix/Vector/blob/aed70305b9251404b9b900f3d8b65b9918d55097/daemon/src/main/java/org/lsposed/lspd/service/LSPModuleService.java
[旧服务 AIDL]: https://github.com/libxposed/service/blob/496b76fa3e5af87958ebef97bd160319e05da79b/interface/src/main/aidl/io/github/libxposed/service/IXposedService.aidl
[新服务 AIDL]: https://github.com/libxposed/service/blob/3318940876192e29cf6ab07637e899e22a87ebf0/interface/src/main/aidl/io/github/libxposed/service/IXposedService.aidl
[新解析器]: https://github.com/JingMatrix/Vector/blob/88f8e1faa8b4e7ce20aefabe9c295cd746ea038e/daemon/src/main/kotlin/org/matrix/vector/daemon/data/FileSystem.kt#L274
[新模块管理器]: https://github.com/JingMatrix/Vector/blob/88f8e1faa8b4e7ce20aefabe9c295cd746ea038e/xposed/src/main/kotlin/org/matrix/vector/impl/core/VectorModuleManager.kt
[新类加载器]: https://github.com/JingMatrix/Vector/blob/88f8e1faa8b4e7ce20aefabe9c295cd746ea038e/xposed/src/main/kotlin/org/matrix/vector/impl/utils/VectorModuleClassLoader.kt
[新 scope 缓存]: https://github.com/JingMatrix/Vector/blob/88f8e1faa8b4e7ce20aefabe9c295cd746ea038e/daemon/src/main/kotlin/org/matrix/vector/daemon/data/ConfigCache.kt#L346
[MiHealth 入口]: https://github.com/JoyElliot/MiHealth_AmapFix/blob/387ba15e30ee13d0dd670e2634e73fa9d0c1bbff/app/src/main/java/io/github/mihealthamapfix/ModernEntry.java
[MiHealth stub]: https://github.com/JoyElliot/MiHealth_AmapFix/blob/387ba15e30ee13d0dd670e2634e73fa9d0c1bbff/stubs/src/main/java/io/github/libxposed/api/XposedModule.java
[MiHealth 构建]: https://github.com/JoyElliot/MiHealth_AmapFix/blob/387ba15e30ee13d0dd670e2634e73fa9d0c1bbff/app/build.gradle
[GlassMic 入口]: https://github.com/lm060719/io.mo.glassmic/blob/200a903a8dd1b4dcb9b4148555971d91278b61c6/xposed/src/main/java/io/mo/glassmic/xposed/GlassMicXposedModule.kt
[GlassMic 构建]: https://github.com/lm060719/io.mo.glassmic/blob/200a903a8dd1b4dcb9b4148555971d91278b61c6/app/build.gradle.kts
