# 单 APK 保留旧实现、在新框架切换 Modern 的入口分派调查

日期：2026-09-05。回答范围：同一 APK 如何在指定旧稳定框架保留 legacy 业务实现，在最新稳定 Vector 2.2 使用纯 Modern 业务实现。**不要求支持所有中间 API，也不把旧业务全面重写作为前提。** 本报告不修改代码，不包含真机实验。新 API 的配置/统计替代机制见[独立能力调查](./Xposed_Stable_API_Capabilities_2026-09-05.zh-CN.md)。

## 结论与可执行方向

有两种已有源码构件可以组合：**MiHealth 的跨 loader 构造桥**，以及 **GlassMic 的单 APK 两套实际 hook 实现**。但本轮未找到已经公开验证“LSPosed1.9.2 旧业务 + Vector2.2 纯Modern业务 + 单APK”完整矩阵的模块，不能把组合方案写成现成成功方案。

针对这两个指定稳定框架，最值得先做的小实验是：一个仅负责生命周期分派的兼容入口，在旧 LSPosed 的两参构造/旧回调中进入旧业务适配器，在 Vector2.2 无参入口/onModuleLoaded/新回调中进入独立 Modern 适配器。`assets/xposed_init` 仍留给真正只支持 legacy 入口的框架。这个桥服务的是旧稳定框架的真实 loader；不意味着要把所有 API100 过渡版本加入产品支持矩阵。

**这不是仅增加两个构造器即可完成的改动**：必须验证运行时父类 ABI、ART/R8 对未执行分支的链接、两套生命周期、旧配置读取可用性及相互隔离。这里提出的是有源码依据的最小候选，不是承诺必然可行。

## 为什么指定旧稳定框架需要一层桥

| 指定框架 | APK 选择与实例化规则 | 推论 |
|---|---|---|
| LSPosed1.9.2，`f8927757e8704d96611e1f3d75f702e3f0ce061f` | 非空 `META-INF/xposed/java_init.list` 优先；Modern loader 查找 `(XposedInterface, ModuleLoadedParam)` | 旧业务可以保留，但双 metadata 不会自动选择旧入口；若要求同包，需要适配这条实际到达的生命周期 |
| Vector2.2，`88f8e1faa8b4e7ce20aefabe9c295cd746ea038e` | `target>=101` 选择 Modern；无参构造后 framework attach；`target>=102` 阻止 legacy API 类访问 | Modern分派路径应完全不触碰 legacy；APK保留未加载的旧类，不等于新路径调用旧API |

来源：[旧解析器][old-parse]、[旧实例化器][old-load]、[新解析器][new-parse]、[新实例化器][new-load]、[新ClassLoader][new-cl]。这些规则是具体版本源码事实，不推广到所有 fork。

旧管理器自身也以 Modern marker 判别模块，并从 `module.prop` 读取 min/target；因此桥接候选不能一面声明 `minApiVersion=101`，一面宣称支持旧 Modern loader。若实验成功，元数据必须如实表示实际最低支持版本；不能只为消除提示降低 min。[旧 ModuleUtil][old-util]

## 各种方法的实际可用性

| 方法 | 保旧稳定 legacy 业务 | 新稳定纯 Modern 业务 | 判断 |
|---|---|---|---|
| 只保留 legacy 入口 | 有现有路线可用，但配置另验 | 不会自动获得 Modern 实例/服务上下文 | 可以作为旧版对照，不满足新分支目标 |
| 两套 metadata + API101-only 无参入口 | LSPosed1.9.2会先选Modern，旧路线被抢占 | 可以走Modern | 不足以满足指定矩阵 |
| 仅改 min/target/scope | 无法改变1.9.2非空Modern list优先逻辑 | target在2.2影响加载选择 | 没找到官方提供按框架分别选择 list 的 metadata 开关 |
| 兼容 loader 的薄入口 + 两业务适配器 | 旧稳定回调转入旧业务，需验证 | 新稳定回调转入纯Modern，需隔离 | **推荐先验证的单APK候选** |
| legacy入口中自己 new/attach Modern实例 | 不是正常框架提供的Modern装载路径 | 官方禁止模块主动调用内部attach桥 | 不推荐冒充已支持方案 |
| 两个flavor APK | 能明确分开 | 能明确分开 | 真实生态策略，但不满足此次单APK要求 |

最后两项依据：[官方入口生命周期说明](https://libxposed.github.io/api/io/github/libxposed/api/package-summary.html)、[DPIS构建][dpis-build]。没有找到框架提供的 metadata 开关，不是数学意义上证明任何技巧都不可能；本轮不建议依靠 ZIP 重复条目、解析差异或私有框架调用来建立发布兼容性。

## 开源案例核证

### 1. MiHealth_AmapFix：有单 APK 跨 loader 桥，Modern业务仍使用 legacy

固定 `JoyElliot/MiHealth_AmapFix@387ba15e30ee13d0dd670e2634e73fa9d0c1bbff`。已核对 app Gradle、main 两入口列表、module.prop、ModernEntry、LegacyInit 和 XposedModule stub。

- Gradle没有flavor，stub与legacy API为compileOnly，两入口在同一main source set；metadata为min100/target101。[build][mi-build]、[metadata][mi-meta]
- ModernEntry同时声明无参和`(XposedInterface, ModuleLoadedParam)`构造；在onPackageLoaded/onPackageReady按运行时版本选择时机。两参签名与**指定的LSPosed1.9.2 loader**匹配。[entry][mi-entry]、[stub][mi-stub]、[旧loader][old-load]
- 其实际业务仍使用`XposedHelpers.findAndHookMethod`、`XC_MethodHook`；所以它是loader桥参考，**不是最新框架纯Modern业务参考**。target101允许访问legacy这一点也不能推广到target102。[entry][mi-entry]、[新ClassLoader][new-cl]
- 不根据该源码宣称运行成功；未下载/反编译其发布APK，未对目标设备复测。

### 2. GlassMic：新找到的单 APK 两套业务实现参考

固定 `lm060719/io.mo.glassmic@200a903a8dd1b4dcb9b4148555971d91278b61c6`。

- app以`implementation(project(":xposed"))`把模块库打入同一APK；两套Xposed API都为compileOnly，无legacy/modern flavor。两入口位于app/main。[app build][glass-build]、[xposed build][glass-xbuild]、[legacy list][glass-old-list]、[modern list][glass-new-list]
- legacy入口是`LegacyXposedEntry`，实际通过`XposedHelpers.findAndHookMethod`和`XC_MethodHook`安装Application.attach hook，再转LegacyAudioRecordHook。[legacy entry][glass-old]
- Modern入口`GlassMicXposedModule : XposedModule()`调用`hook(...).intercept(...)`，其`AudioRecordHook`同样以`api.hook(method).intercept`安装具体read/release hook。**已核证这两处采用新机制，不只是把旧hook换一个入口调用。**[modern entry][glass-new]、[audio hook][glass-audio]
- 它有共享AtomicBoolean安装门，提供进程内双实现排重思路；但门在成功安装前置位的重试语义仍需自己设计，不宜无条件照搬。[gate][glass-gate]
- metadata是min101/target101；Modern只有无参构造。因此仍不能证明它在LSPosed1.9.2恢复旧业务，面对该指定旧loader也有前述障碍。[metadata][glass-meta]、[旧loader][old-load]
- 源码注释把Modern onModuleLoaded等同zygote全局覆盖；本报告**不采纳这个生命周期宣称**。框架向每个目标进程创建实例，scope与系统/应用回调应按框架事实验证，而非复制案例注释。[新loader][new-load]

该案例证明“两套实际hook实现放在同包”有源码可参考；尚未对整个可达调用图进行无legacy引用审计，也未验证发布二进制，不能称已认证的纯Modern完整产品。

### 3. DPIS：明确分 APK，防止误算为单包成功案例

固定 `Kwensiu/DPIS@50f5f1a38c2abc06e04497a624d6c9361e230f8c`。Gradle明确定义`xposedApi`维度、modern/legacy两个flavor，并从不同目录输出正常APK及`_legacy.apk`。入口分别为modern `ModuleMain`与legacy `LegacyModuleHook`；Modern metadata为min101/target102。它能作为分离source set组织参考，不能算本任务的同一APK分派案例。[build][dpis-build]、[modern list][dpis-new]、[legacy list][dpis-old]、[metadata][dpis-meta]

## 推荐候选的边界与支持矩阵

候选结构：`assets/xposed_init → LegacyEntry → 旧业务`；`java_init.list → DispatchEntry → 指定旧loader适配器或Modern适配器`。共享层只放普通业务数据和接口，API类型留在各自适配层；新分支禁止初始化旧适配器、禁止借旧XposedBridge记录日志或安装hook。

桥接只负责把真实框架生命周期转为内部调用：旧稳定两参构造标识旧装载，再将onPackageLoaded/onSystemServerLoaded转入旧业务；新稳定无参构造等待framework attach与onModuleLoaded，再处理onPackageReady/onSystemServerStarting。不能在新构造器里手动attach，不能因Class.forName成功就宣布Modern接管完成。

| 环境 | 候选预期 | 当前证据状态 |
|---|---|---|
| 实际选定的legacy-only框架 | assets旧入口直接旧业务 | 打包设计合理，需设备验证具体框架 |
| LSPosed1.9.2 | Modern list进入兼容桥，再调用旧业务 | loader ABI与公开桥相符；ART/R8、生命周期和旧配置条件未验证 |
| Vector2.2 | 无参Modern生命周期进入纯Modern业务 | 框架规则与两路源码构件支持该设计，端到端未验证 |
| 其他过渡框架/API100变体 | 不作承诺 | 明确不要求全部支持，不把单个API100分支ABI当统一标准 |

关键限制：**“旧业务函数不重写”与“旧框架原先给予legacy模块的全部环境自动保留”不同。** 在旧LSPosed中若包已被标为Modern，legacy模块注册、模块自身hook、XSharedPreferences位置等条件未必仍相同。入口桥成功后，必须验证旧业务所依赖的环境；入口报告不把这部分归为已经解决。

## 最小验证顺序（尚未执行）

1. 保留一份当前旧主线APK作为旧LSPosed对照；新建独立最小探针验证薄桥，暂不迁移业务代码。核对两套真实父类构造描述符、入口和R8产物；compileOnly stub不可打入APK。
2. 在LSPosed1.9.2和Vector2.2分别记录构造路径、运行时API、每种回调及进程/classloader；确认同一APK两边都实例化成功。若class verification失败，先查未执行的构造分支和缺失回调类型，不继续堆功能。
3. 旧侧只转发一个原有无副作用hook，新侧只安装同一探针的Modern hook；每进程每方法恰好一次。新路径检查完整可达代码无legacy依赖。
4. 通过后再接旧配置/统计依赖和新API替代机制，做功能等价验证。若旧side的“Modern标记环境”无法保持旧业务依赖，再评估如何补那一层；不得把失败解释成必须兼容所有过渡API。

本轮仅完成一手源码调查和可验证方案，不把候选写入既有OpenSpec目标、不开始实现。Context7工具不可用，使用GitHub官方源码、官方Javadoc与开源模块源码；搜索摘要只用于发现候选。

[old-parse]: https://github.com/LSPosed/LSPosed/blob/f8927757e8704d96611e1f3d75f702e3f0ce061f/daemon/src/main/java/org/lsposed/lspd/service/ConfigFileManager.java
[old-load]: https://github.com/LSPosed/LSPosed/blob/f8927757e8704d96611e1f3d75f702e3f0ce061f/core/src/main/java/org/lsposed/lspd/impl/LSPosedContext.java
[old-util]: https://github.com/LSPosed/LSPosed/blob/f8927757e8704d96611e1f3d75f702e3f0ce061f/app/src/main/java/org/lsposed/manager/util/ModuleUtil.java
[new-parse]: https://github.com/JingMatrix/Vector/blob/88f8e1faa8b4e7ce20aefabe9c295cd746ea038e/daemon/src/main/kotlin/org/matrix/vector/daemon/data/FileSystem.kt
[new-load]: https://github.com/JingMatrix/Vector/blob/88f8e1faa8b4e7ce20aefabe9c295cd746ea038e/xposed/src/main/kotlin/org/matrix/vector/impl/core/VectorModuleManager.kt
[new-cl]: https://github.com/JingMatrix/Vector/blob/88f8e1faa8b4e7ce20aefabe9c295cd746ea038e/xposed/src/main/kotlin/org/matrix/vector/impl/utils/VectorModuleClassLoader.kt
[mi-build]: https://github.com/JoyElliot/MiHealth_AmapFix/blob/387ba15e30ee13d0dd670e2634e73fa9d0c1bbff/app/build.gradle
[mi-meta]: https://github.com/JoyElliot/MiHealth_AmapFix/blob/387ba15e30ee13d0dd670e2634e73fa9d0c1bbff/app/src/main/resources/META-INF/xposed/module.prop
[mi-entry]: https://github.com/JoyElliot/MiHealth_AmapFix/blob/387ba15e30ee13d0dd670e2634e73fa9d0c1bbff/app/src/main/java/io/github/mihealthamapfix/ModernEntry.java
[mi-stub]: https://github.com/JoyElliot/MiHealth_AmapFix/blob/387ba15e30ee13d0dd670e2634e73fa9d0c1bbff/stubs/src/main/java/io/github/libxposed/api/XposedModule.java
[glass-build]: https://github.com/lm060719/io.mo.glassmic/blob/200a903a8dd1b4dcb9b4148555971d91278b61c6/app/build.gradle.kts
[glass-xbuild]: https://github.com/lm060719/io.mo.glassmic/blob/200a903a8dd1b4dcb9b4148555971d91278b61c6/xposed/build.gradle.kts
[glass-old-list]: https://github.com/lm060719/io.mo.glassmic/blob/200a903a8dd1b4dcb9b4148555971d91278b61c6/app/src/main/assets/xposed_init
[glass-new-list]: https://github.com/lm060719/io.mo.glassmic/blob/200a903a8dd1b4dcb9b4148555971d91278b61c6/app/src/main/resources/META-INF/xposed/java_init.list
[glass-meta]: https://github.com/lm060719/io.mo.glassmic/blob/200a903a8dd1b4dcb9b4148555971d91278b61c6/app/src/main/resources/META-INF/xposed/module.prop
[glass-old]: https://github.com/lm060719/io.mo.glassmic/blob/200a903a8dd1b4dcb9b4148555971d91278b61c6/xposed/src/main/java/io/mo/glassmic/xposed/LegacyXposedEntry.kt
[glass-new]: https://github.com/lm060719/io.mo.glassmic/blob/200a903a8dd1b4dcb9b4148555971d91278b61c6/xposed/src/main/java/io/mo/glassmic/xposed/GlassMicXposedModule.kt
[glass-audio]: https://github.com/lm060719/io.mo.glassmic/blob/200a903a8dd1b4dcb9b4148555971d91278b61c6/xposed/src/main/java/io/mo/glassmic/xposed/AudioRecordHook.kt
[glass-gate]: https://github.com/lm060719/io.mo.glassmic/blob/200a903a8dd1b4dcb9b4148555971d91278b61c6/xposed/src/main/java/io/mo/glassmic/xposed/XposedHookGate.kt
[dpis-build]: https://github.com/Kwensiu/DPIS/blob/50f5f1a38c2abc06e04497a624d6c9361e230f8c/app/build.gradle.kts
[dpis-new]: https://github.com/Kwensiu/DPIS/blob/50f5f1a38c2abc06e04497a624d6c9361e230f8c/app/src/modern/resources/META-INF/xposed/java_init.list
[dpis-old]: https://github.com/Kwensiu/DPIS/blob/50f5f1a38c2abc06e04497a624d6c9361e230f8c/app/src/legacy/assets/xposed_init
[dpis-meta]: https://github.com/Kwensiu/DPIS/blob/50f5f1a38c2abc06e04497a624d6c9361e230f8c/app/src/modern/resources/META-INF/xposed/module.prop
