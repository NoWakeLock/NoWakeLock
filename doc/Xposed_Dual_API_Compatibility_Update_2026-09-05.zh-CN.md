# 单 APK 跨代 Xposed 兼容调查更新

调查日期：2026-09-05。范围：联网核对框架加载、API ABI、配置共享及公开模块源码；不修改实现，不包含本轮构建或真机实验。

后续按两个独立问题细化：[旧/新框架入口分派与开源案例](Xposed_Entry_Dispatch_Research_2026-09-05.zh-CN.md)；[稳定 API102 能力与本地未完成实验](Xposed_Stable_API_Capabilities_2026-09-05.zh-CN.md)。前者讨论如何选择旧/新实现，后者讨论新实现具体使用什么接口。

## 结论先行

**单 APK 携带两套入口有真实源码案例，但“保留 legacy 入口，所以旧 LSPosed 自动走 legacy”不成立。当前方案首先要重新验证加载选择，而不是继续扩大业务 hook 迁移。**

最关键的新证据是：LSPosed v1.9.2 和 Vector v2.0 都优先读取非空的 Modern `java_init.list`，仅当它为空才读 `assets/xposed_init`；Vector v2.2 则按 `targetApiVersion >= 101` 优先选择 Modern。三者都不是“Modern 实例化失败后自动回退 legacy”。因此当前 NoWakeLock 的 API101-only Modern 入口可能抢占旧框架的 legacy 路线。源码能确认选择规则；具体 APK 的错误类型和设备表现仍需实验。[旧 LSPosed 解析器][old-parser]、[Vector 2.0 解析器][v20-parser]、[Vector 2.2 解析器][v22-parser]

Vector 稳定版已经从旧报告中的 v2.0 前进到 **v2.2**：v2.1 于 8 月 2 日首次稳定采用 API101，v2.2 于 8 月 4 日采用 API102，并修复 v2.1 Release 的 R8 问题导致 `system_server` 中 `XposedHelpers.findClass` 持续失败。这提供了“模块加载了但没有 hook”的框架侧新排查项，不能反向认定它就是 #54/#55 的根因。[v2.1 发布说明](https://github.com/JingMatrix/Vector/releases/tag/v2.1)、[v2.2 发布说明](https://github.com/JingMatrix/Vector/releases/tag/v2.2)

## 证据标准与边界

- **源码直接证据**：固定提交、tag 的实际分支或 ABI；证明实现机制，不证明发布二进制与真机行为。
- **官方发布/文档证据**：发布说明、Maven 元数据、官方 API；不把默认分支等同于已发布版本。
- **推断/待实验**：由上游与本地实现拼出的风险，明确标识；没有设备日志不归因 issue。

本会话工具清单没有 Context7；已用联网搜索发现资料，再直接读取 GitHub 官方源码、发布页及 Maven Central。没有采用第三方教程作为结论依据。旧 `Framework_Direct_Evidence` 与 `Framework_Source_Hunt_Backlog` 是 Android 原始事件名称调查，不能作为 Xposed 跨代兼容实验记录。

## 框架实际选择入口

| 固定版本 | 源码直接行为 | 对当前单 APK 的影响 |
|---|---|---|
| LSPosed v1.9.2，`f8927757e8704d96611e1f3d75f702e3f0ce061f` | `loadModule` 先读 Modern list；非空设 `legacy=false`，否则读 legacy list | 不能因它是旧版就假定 legacy 入口会执行 |
| Vector v2.0，`76141fed151f49b818144d54f2ebb6ab9a2df11c` | 同上；该解析器没有按 `minApiVersion` 做 legacy fallback | API101-only 入口仍可能进入旧 Modern loader |
| Vector v2.2，`88f8e1faa8b4e7ce20aefabe9c295cd746ea038e` | `target>=101` → Modern；否则有 legacy file → Legacy；否则 `target==100` → Unsupported | 单包选一条路径；Modern 失败不自动重试 legacy |

来源：[LSPosed ConfigFileManager][old-parser]、[Vector 2.0 ConfigFileManager][v20-parser]、[Vector 2.2 FileSystem][v22-parser]。这些规则只对已核对版本成立，不能直接推广到全部 fork。

Vector v2.0 的 `LSPosedContext.loadModule` 用 `(XposedInterface, ModuleLoadedParam)` 精确查找构造器；单个入口实例化异常被记录后继续，方法末尾仍可返回成功。它没有在该方法中重新解析 legacy list。因而“Loaded module”日志本身也不足以证明入口/业务 hook 成功。[v2.0 loader][v20-loader]

Vector v2.2 使用无参数构造后 `attachFramework`，如果所有入口都无法实例化则返回失败。`target>=102` 会令模块 ClassLoader 拒绝 legacy API 类；这不等于整个框架删除了 legacy 模块支持。[v2.2 loader][v22-loader]、[v2.2 ClassLoader][v22-cl]

## API 与配置：保留什么，修正什么

1. **保留：API93 扩展不等于 Modern API100。** New XSharedPreferences 的官方说明要求模块 metadata、`MODE_WORLD_READABLE`，读取使用包名和 preference 名称；随机路径不应成为健康检查的固定假设。[官方 New XSharedPreferences](https://github.com/LSPosed/LSPosed/wiki/New-XSharedPreferences)
2. **修正：API100 不能只看一个分支的 ABI。** `libxposed/api` 的 `100` 分支当前 `XposedModule` 构造参数是 `XposedContext`，而 Vector v2.0 实际加载器查找的是 `XposedInterface`。旧报告将前者写成所有 API100 框架的唯一 ABI，证据不足。必须锁定目标框架及其 API 子模块提交。[API100 分支源码](https://github.com/libxposed/api/blob/100/api/src/main/java/io/github/libxposed/api/XposedModule.java)、[Vector 2.0 loader][v20-loader]
3. **保留：API101 生命周期/构造/hook 改动不能靠降低 minApi 自动兼容。** API101 稳定 RFC 改为无参入口、分阶段 package callback 和 interceptor chain。兼容旧 Modern 需要实际适配其 ABI，或明确放弃对应框架，不能只放一个 legacy 文件。[API101 RFC](https://github.com/libxposed/api/pull/51)
4. **更新：Maven api/service 最新发布均为 102.0.0。** api 版本列表为 101.0.0、101.0.1、102.0.0；service 为 101.0.0、102.0.0。此次 metadata 的 `lastUpdated` 分别为 `20260613123532`、`20260613123536`；这不是每个版本的独立发布日期，旧报告的“截至6月23”也不应被转述为发布日期。[api metadata](https://repo1.maven.org/maven2/io/github/libxposed/api/maven-metadata.xml)、[service metadata](https://repo1.maven.org/maven2/io/github/libxposed/service/maven-metadata.xml)
5. **更新：api/service 默认分支均已是 master。** 旧 `/102/README.md` 链接本轮返回404。当前官方集成示例仍强调 api 用 `compileOnly`、service 用 `implementation`，并给出 R8 入口保留规则。[API README](https://github.com/libxposed/api/blob/79b75b49255a257d38f67bce2e928649dc2fc6c9/README.md)、[Service README](https://github.com/libxposed/service/blob/3318940876192e29cf6ab07637e899e22a87ebf0/README.md)
6. **保留：Remote Preferences 与服务能力检查是正确研究方向。** `getRemotePreferences` 可因服务死亡或不支持 remote capability 失败；官方明确配置更新应使用 preferences/listener，不应使用 hot reload。API102 hot reload 是代码替换功能，本轮无需引入。[官方 XposedService 源码](https://github.com/libxposed/service/blob/3318940876192e29cf6ab07637e899e22a87ebf0/service/src/main/java/io/github/libxposed/service/XposedService.java)

不要把 Wiki 的“New XSharedPreferences 自某版取消”跨 fork 当绝对结论：Vector v2.2 源码仍有 metadata 分支，经 `getPrefsPath(packageName)` 取路径，也保留 file watcher。代码存在不等于 UI 发布、权限、读取完整链路已成功；需以具体框架验证。[Vector v2.2 XSharedPreferences](https://github.com/JingMatrix/Vector/blob/88f8e1faa8b4e7ce20aefabe9c295cd746ea038e/legacy/src/main/java/de/robv/android/xposed/XSharedPreferences.java)

## 公开模块案例重新核证

### MiHealth_AmapFix：真实单 APK 双入口源码，成功范围需降级表述

原 `ljy6-6-6/MiHealth_AmapFix` 现重定向到 `JoyElliot/MiHealth_AmapFix`；本轮固定提交 `387ba15e30ee13d0dd670e2634e73fa9d0c1bbff`。

- `app/build.gradle` 没有 productFlavors，两套入口都在 main，stub 与 legacy API 均是 compileOnly；这是单 APK 打包设计，不是两个 flavor。[构建文件][mi-build]
- `module.prop` 为 min100/target101；Modern list 指向 `ModernEntry`，legacy list 指向 `LegacyInit`。[metadata][mi-prop]、[Modern list][mi-list]、[legacy list][mi-legacy-list]
- `ModernEntry` 同时有无参和 `(XposedInterface, ModuleLoadedParam)` 构造，覆盖 onPackageLoaded/onPackageReady；业务仍调用 XposedBridge/XposedHelpers。[ModernEntry][mi-entry]、[stub][mi-stub]
- 两参签名与 Vector v2.0 loader 匹配；不能因此宣布与所有名为 API100 的框架匹配。target101 在 Vector v2.2 不触发 target102 的 legacy ClassLoader 禁令，也不能据此把 target 改102。[v2.0 loader][v20-loader]、[v2.2 ClassLoader][v22-cl]
- 本轮未构建该模块、未检查发布 APK、未真机重放；已读入口/build 未提供与 NoWakeLock 等价的 Room→Remote Preferences→system_server 配置闭环。**它证明有人采用该设计，不能证明 NoWakeLock 完整双后端兼容已解决。**

### universal-template：检索摘要已经过时

搜索结果仍把 `Jordan231111/lsposed-universal-template` 描述为 legacy/modern 两个 flavor，但当前提交 `d89234bfe16b66e1ed3c1c95f41776dbb8b4f380` 的 app Gradle 已无该 flavor 定义，依赖注释说 Vector 和 LSPatch1.0 都提供 Modern API102。它只能作为现代框架之间共享 API 的线索，不能作为 legacy+modern 单包成功例证。[当前构建源码](https://github.com/Jordan231111/lsposed-universal-template/blob/d89234bfe16b66e1ed3c1c95f41776dbb8b4f380/app/build.gradle.kts)

## 与本地未完成工作的对应

本地 [design](../openspec/changes/add-dual-xposed-config-backend/design.md) 当前选择 legacy API93+、Modern101/102、单 APK、排除 API100；[tasks](../openspec/changes/add-dual-xposed-config-backend/tasks.md) 中真机 smoke 5.7 未完成。旧报告记载构建通过只是历史记录，本轮没有重新执行，也不等于跨框架验证完成。

需要优先重查的三个假设：

1. **支持矩阵与加载策略冲突**：LSPosed1.9.2 已识别 Modern list，因此“排除API100但仍覆盖所有旧LSPosed”的承诺不能凭当前 metadata 成立。先验证目标旧框架，再决定是否需要旧 Modern 适配；本报告不擅自变更 OpenSpec。
2. **类存在不是入口成功**：[ModernXposedRuntimeDetector](../app/src/main/java/com/js/nowakelock/xposedhook/ModernXposedRuntimeDetector.kt) 只做 Class.forName，既不确认API101，也不确认Modern入口实例化成功。若某 fork 执行 legacy 回调而 detector 判真，可能形成两条路径都不工作；这是待实验风险，不是已证实 issue 根因。
3. **防重复安装需要实际共享语义**：按主代理本轮代码核对，本地 guard key 包含 entry/source，不能只因名字有 shared 就推断两代能相互排重。Vector2.2正常解析只选一代，但其他fork及同代多生命周期仍应记录安装次数。

旧报告中仍残留“API100用户仍要保留”“现代业务仍复用legacy”等历史段落，与6月25日策略不一致。它们应保留作演变记录，不再作为当前已验证事实。#54 配置路径异常、后续 safe mode、#55 无数据，以及 #56 SwiftKey 崩溃仍需分别取证；没有堆栈不能合并根因。

## 下一轮最小实验矩阵（建议，尚未执行）

先做只记日志、只 hook 无副作用探针方法的最小 APK；确认入口链路后再加载 NoWakeLock 的三类系统拦截。每份 APK 保存 SHA256、两套 metadata、构造器 DEX 描述符、是否包含 compileOnly 类定义；每台设备记录框架仓库/commit/build type、Android/ROM、scope。

| 实验 | 框架 | 必须回答的问题 |
|---|---|---|
| A | 一个实际使用的 legacy-only 框架 | 双入口 APK 是否走 legacy；配置写入后退出 App，规则仍可读？ |
| B | LSPosed v1.9.2 | 当前双入口是否抢入 Modern；入口构造是否失败；同一业务的 legacy-only 对照是否成功？ |
| C | Vector v2.0 | 记录实际构造查找与无参失败；验证是否无 fallback，不能只看“Loaded”日志 |
| D | Vector v2.1 Debug（诊断对照） | API101入口及Remote配置闭环；Release已知R8问题单独标识，不用它作为唯一验收基线 |
| E | Vector v2.2 Release + 对应Debug | target101纯Modern入口、system_server/SettingsProvider生命周期各安装几次，Remote发布/读取是否一致？ |

配置闭环至少包括：首次启动发布、修改一个规则、regex、恢复备份、服务重连、App退出、重启设备、缺 key 默认放行、后端不可用状态。若设备有工作资料/多用户，再核对同一包配置与规则中的 userId 语义；Vector2.2发布说明明确调整了跨用户模块运行规则。

每个阶段分别记录 `entry instantiated → callback reached → hook installed → configuration read → event observed`，不以“管理器已启用”或“状态页全绿”替代链路证据。最先验证 B/C；如果入口都没进入，业务 hook 和配置发布的更多改动无法证明兼容。

## 尚未确认

- 任一公开模块发布 APK 在全部目标旧/新框架上的真实成功矩阵。
- NoWakeLock 当前 APK 在目标设备上的具体加载错误、重复 hook 次数和实际配置一致性。
- #54 用户所用 `LSPosed-v2.0.4-7741` 的确切仓库/commit；不能直接等同 Vector v2.0。
- #54 safe mode、#55 无数据、#56 SwiftKey 崩溃是否共享原因。
- LSPosed1.9.2 之外的所有 legacy fork 是否同样优先 Modern；不得从“API93+”数字推断实现一致。

[old-parser]: https://github.com/LSPosed/LSPosed/blob/f8927757e8704d96611e1f3d75f702e3f0ce061f/daemon/src/main/java/org/lsposed/lspd/service/ConfigFileManager.java
[v20-parser]: https://github.com/JingMatrix/Vector/blob/76141fed151f49b818144d54f2ebb6ab9a2df11c/daemon/src/main/java/org/lsposed/lspd/service/ConfigFileManager.java
[v20-loader]: https://github.com/JingMatrix/Vector/blob/76141fed151f49b818144d54f2ebb6ab9a2df11c/core/src/main/java/org/lsposed/lspd/impl/LSPosedContext.java
[v22-parser]: https://github.com/JingMatrix/Vector/blob/88f8e1faa8b4e7ce20aefabe9c295cd746ea038e/daemon/src/main/kotlin/org/matrix/vector/daemon/data/FileSystem.kt
[v22-loader]: https://github.com/JingMatrix/Vector/blob/88f8e1faa8b4e7ce20aefabe9c295cd746ea038e/xposed/src/main/kotlin/org/matrix/vector/impl/core/VectorModuleManager.kt
[v22-cl]: https://github.com/JingMatrix/Vector/blob/88f8e1faa8b4e7ce20aefabe9c295cd746ea038e/xposed/src/main/kotlin/org/matrix/vector/impl/utils/VectorModuleClassLoader.kt
[mi-build]: https://github.com/JoyElliot/MiHealth_AmapFix/blob/387ba15e30ee13d0dd670e2634e73fa9d0c1bbff/app/build.gradle
[mi-prop]: https://github.com/JoyElliot/MiHealth_AmapFix/blob/387ba15e30ee13d0dd670e2634e73fa9d0c1bbff/app/src/main/resources/META-INF/xposed/module.prop
[mi-list]: https://github.com/JoyElliot/MiHealth_AmapFix/blob/387ba15e30ee13d0dd670e2634e73fa9d0c1bbff/app/src/main/resources/META-INF/xposed/java_init.list
[mi-legacy-list]: https://github.com/JoyElliot/MiHealth_AmapFix/blob/387ba15e30ee13d0dd670e2634e73fa9d0c1bbff/app/src/main/assets/xposed_init
[mi-entry]: https://github.com/JoyElliot/MiHealth_AmapFix/blob/387ba15e30ee13d0dd670e2634e73fa9d0c1bbff/app/src/main/java/io/github/mihealthamapfix/ModernEntry.java
[mi-stub]: https://github.com/JoyElliot/MiHealth_AmapFix/blob/387ba15e30ee13d0dd670e2634e73fa9d0c1bbff/stubs/src/main/java/io/github/libxposed/api/XposedModule.java
