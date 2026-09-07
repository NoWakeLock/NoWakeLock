# 旧稳定框架环境可行性：入口桥之外的确定障碍

日期：2026-09-06。续接[入口分派调查](./Xposed_Entry_Dispatch_Research_2026-09-05.zh-CN.md)。范围是 LSPosed1.9.2 将双 metadata APK 判为 Modern 后，能否不改适配就继续使用原 legacy 环境；不要求支持全部过渡版本。

## 结论

**不能说现方案只差设备验证。** 源码已经可以确定：在 LSPosed1.9.2 中，即使补齐入口构造桥，Modern 分类也不会恢复 legacy 模块注册、New XSharedPreferences 自动配置和 legacy 生命周期注册。设备实验用于验证补齐后的桥与运行时行为，不能代替这些已知缺口的设计与实现。

“同一 APK、保留旧业务判断、新框架使用纯 Modern”仍是可以继续验证的架构方向；但若旧业务依赖 LSPosed 的 New XSharedPreferences 环境，**薄桥只能解决生命周期入口，不能独自保留旧配置环境**。需要独立解决旧稳定环境下配置发布/读取，或重新界定指定支持矩阵。

## 固定证据基线

- LSPosed1.9.2：`f8927757e8704d96611e1f3d75f702e3f0ce061f`。
- Vector2.2：`88f8e1faa8b4e7ce20aefabe9c295cd746ea038e`。
- MiHealth_AmapFix：`387ba15e30ee13d0dd670e2634e73fa9d0c1bbff`。
- libxposed 2023-10-12 前的 API 源码快照：`a42f85d06eac3373d266a534ab3b31a584b30774`。其 ABI 与所读 LSPosed loader 的两参签名一致；这只是对应时期源码，不声称复原了历史发布 APK 内 API jar 的精确哈希。

本轮重新联网读取官方源码和案例源码；Context7工具不可用。没有执行构建、ART或设备实验。

## 1. Modern 分类不会注册成 legacy：确定事实

LSPosed1.9.2 的 `ConfigFileManager.loadModule` 先读取 Modern list，非空就标记 `file.legacy=false`。`LSPApplicationService` 随后用该字段把两个加载列表严格分开。[解析器][parse]、[服务列表过滤][lists]

`XposedInit` 对两代的注册也不同：

| 路径 | loadedModules 中的值 | 随后调用 |
|---|---|---|
| loadLegacyModules | `Optional.of(apkPath)` | legacy loadModule/initModule |
| loadModules（Modern） | `Optional.empty()` | LSPosedContext.loadModule |

这是源码特意保留的语义，注释也说明只有 legacy 模块有非空值。Modern入口里调用一个旧业务类，不会自动执行`loadLegacyModules`，不会把empty变成APK路径。[XposedInit][init]

## 2. New XSharedPreferences 读写两端均受影响：确定事实

### 读取端

`XSharedPreferences(packageName,prefFileName)` 先读取上述map；**仅当Optional值present时**才解析模块manifest，检查`xposedminversion>92`或`xposedsharedprefs`，并通过framework的`getPrefsPath`选择New XSP目录。

Modern模块的值是empty，因此`newModule=false`，直接选择`/data/data/<package>/shared_prefs/<name>.xml`。保留manifest metadata无法改变这条判断；它连解析metadata的分支都没有进入。[XSharedPreferences][xsp]

这确定了路径选择变化；至于退回的普通私有文件能否被某个目标进程读取，要看具体权限/SELinux/文件是否存在。本报告不把“选择旧路径”过度推成所有设备必定崩溃。但原New XSP共享契约已不成立。

### 发布端

`LoadedApkCreateCLHooker` 只在`isFirstPackage`且`loadedModules[packageName]`值present时调用`hookNewXSP`。后者才安装：

- `ContextImpl.checkMode`：允许`MODE_WORLD_READABLE`。
- `ContextImpl.getPreferencesDir`：改到framework preference目录。

Modern分类下map值empty，因此正常框架流程不会安装这两项New XSP支持。即便另行让模块App进入scope，这个present条件仍然不成立。[LoadedApkCreateCLHooker][create-cl]

**直接推论**：仅添加两参入口桥、保留旧`MODE_WORLD_READABLE`发布器与包名式`XSharedPreferences`reader，不能保持原先New XSP写读闭环。这个缺口在设备测试前已可由源码确定。不能把手动修改框架内部map或hook私有框架实现包装成公共API保证。

## 3. 自身 hook / 激活：哪些确定，哪些不能武断

legacy `initModule`负责识别并注册`IXposedHookZygoteInit`、`IXposedHookLoadPackage`、资源回调；Modern加载器不会执行这段逻辑。因而仅在APK中保留LegacyEntry类，不会让它的self-activation回调自动注册。薄桥若需要旧业务执行，必须显式适配实际收到的生命周期。[legacy注册][init]

旧daemon `ConfigManager` 的scope处理含“允许模块注入自身”的路径，`getModulesForProcess`也主要依赖cachedScope。因此本轮**没有证据支持“所有Modern模块在1.9.2中绝对不能self hook”**这种泛化结论。可确定的较窄事实是：scope、回调注册和New XSP启用是三个不同条件；满足其中一个不会自动满足另两个。[ConfigManager][config]

此外，`LoadedApkCreateCLHooker`在非first package且该包是Modern模块（map值empty）时提前return，跳过后续package回调；模块代码作为其他进程中的额外包加载，也不能被当作模块App自身启动。[package回调过滤][create-cl]

现代App侧正常通信路线是`LSPModuleService.uidStarts`识别`!file.legacy`模块，再向模块provider发送service Binder；这不是legacy self hook。该旧实现暴露`getFrameworkPrivilege`等旧接口，不能据此认为本地API101+ service wrapper可直接无差别使用。[LSPModuleService][module-service]

## 4. MiHealth 构造桥：匹配入口 ABI，但不是完整环境适配

指定旧loader用`getConstructor(XposedInterface.class, ModuleLoadedParam.class)`。2023 API快照中的真实父类`XposedModule`也只有该两参构造，内部调用`XposedInterfaceWrapper(base)`；稳定102的`XposedModule`没有显式构造，因此走无参父类构造。[旧loader][old-loader]、[旧API父类][old-api]、[API102父类][new-api]

MiHealth的compileOnly shim在编译时同时声明无参和两参`XposedModule`构造；ModernEntry分别调用`super()`和`super(base,param)`。**从描述符看，这两条执行路径各有对应时代的真实父类构造**；shim的方法体不会被打入APK代替运行时父类。[MiHealth entry][mi-entry]、[MiHealth shim][mi-module]

但一个DEX类同时包含两个super调用，其中一个在当前runtime不存在，是否在未执行时被ART验证/解析、R8如何保留，应通过实际DEX及ART验证。不能仅由Java编译通过证明两边装载成功，也不能仅因父类少一个构造就武断断言另一条未执行构造必然让所有加载失败。

### 不能原样照搬的确定差异

- MiHealth的shim没有`SystemServerLoadedParam`及`onSystemServerLoaded`，只有新`SystemServerStartingParam`及对应回调。其模块主要处理应用包，不能当作已经覆盖旧NoWakeLock system_server生命周期的模板。[MiHealth interface][mi-interface]
- LSPosed1.9.2实际在`SystemServer.startBootstrapServices`前调用`onSystemServerLoaded`，传入systemServer ClassLoader；同阶段legacy路径构造`packageName=android`的LoadPackageParam。NoWakeLock桥需要明确把这个旧事件转为内部系统hook调用；增加两参构造不会创建这个转发。[StartBootstrapServicesHooker][system-callback]
- MiHealth的shim把`getApplicationInfo`返回类型写成`Object`；2023 API对应类型是`ApplicationInfo`。返回类型属于JVM/DEX方法描述符，若新增适配代码按这个shim实际调用该方法，不能只因Java可赋给Object就认为ABI一致。案例当前入口不依赖此方法，并不能消除将来使用它的风险。[旧interface][old-interface]、[MiHealth interface][mi-interface]

## 5. 最新框架一侧：可保留旧类，不可调用旧API

Vector2.2选择Modern后，用无参入口、framework attach和新生命周期装载；target102时ClassLoader拒绝legacy API名称。单APK仍可携带未使用的旧类，但纯Modern可达路径必须不初始化/链接这些旧实现。target101与target102的限制不同，不能把保留旧类理解为新路径可以调用旧hook。[Vector loader][new-loader]、[Vector ClassLoader][new-cl]

## 6. 有源码依据的补齐方向：指定旧稳定的 Remote Preferences 适配

**可作为候选，能力确实存在，并非要求实现所有过渡API。** LSPosed1.9.2 的 `LSPosedContext.getRemotePreferences(name)` 创建 hook侧 `LSPosedRemotePreferences`；模块App侧 `LSPModuleService` 已实现 `requestRemotePreferences`、`updateRemotePreferences`和`deleteRemotePreferences`，更新后调用 injected service 的通知路径。因此可以考虑“旧loader桥 + 保留旧hook业务核心 + 指定1.9.2 Remote Preferences配置适配器”。[旧hook上下文][old-loader]、[旧App服务][module-service]

这条路线绕开New XSP注册缺失，但**是需要新增适配的路线，不是当前实现已经覆盖**。当前本地reader/backend选择和App service API101+门槛须由主代理审计；不能把1.9.2旧服务Binder交给101 wrapper就假定兼容。其服务暴露`getAPIVersion`、`getFrameworkPrivilege`，provider交付协议和AIDL transaction也必须按指定旧版本核对。旧App服务与hook服务各自工作，不等于配置更新从Room到hook已闭环。

实验应先做到旧App写一个revision、旧系统hook读同一个revision、App退出后仍可读取，再验证热更新/重启/服务重连。只有这一闭环完成，才能声称桥保留了旧业务所需配置。与两APK相比，这保留单APK目标；与修改框架内部map相比，它建立在已有公开Remote Preferences能力上，但仍需要完整的协议、生命周期和二进制验证。

## 已知缺口与下一步判定

| 事项 | 现在能否确定 | 应做的工作 |
|---|---|---|
| 双metadata在旧1.9.2选择Modern | 已确定 | 接受其实际loader并设计指定旧版本适配 |
| 仅入口桥不能恢复New XSP注册/自动hook | 已确定 | 设计旧稳定环境中的配置闭环，不能原样复用并宣称完成 |
| MiHealth不含旧system_server转发 | 已确定 | 按旧事件显式适配，不照搬应用包案例 |
| 跨代父类构造和缺失类型的ART行为 | 尚未验证 | 最小独立DEX/探针验证，无需先修改全部业务 |
| 修正后的旧配置权限/重启持久性 | 尚未验证 | 设备验证发布和读取同一revision |
| 当前NoWakeLock完整实现已完成兼容 | 无证据支持 | 结合本地代码审计，先补确定缺口，再验设备 |

本轮未证伪单APK最终可行性；已证伪的是“加薄桥即可保持全部旧环境、其余只待真机”的简化说法。保留旧业务核心仍有价值，但旧稳定框架适配边界至少包括**生命周期及配置环境**，不能只覆盖构造器。

[parse]: https://github.com/LSPosed/LSPosed/blob/f8927757e8704d96611e1f3d75f702e3f0ce061f/daemon/src/main/java/org/lsposed/lspd/service/ConfigFileManager.java
[lists]: https://github.com/LSPosed/LSPosed/blob/f8927757e8704d96611e1f3d75f702e3f0ce061f/daemon/src/main/java/org/lsposed/lspd/service/LSPApplicationService.java
[init]: https://github.com/LSPosed/LSPosed/blob/f8927757e8704d96611e1f3d75f702e3f0ce061f/core/src/main/java/de/robv/android/xposed/XposedInit.java
[xsp]: https://github.com/LSPosed/LSPosed/blob/f8927757e8704d96611e1f3d75f702e3f0ce061f/core/src/main/java/de/robv/android/xposed/XSharedPreferences.java
[create-cl]: https://github.com/LSPosed/LSPosed/blob/f8927757e8704d96611e1f3d75f702e3f0ce061f/core/src/main/java/org/lsposed/lspd/hooker/LoadedApkCreateCLHooker.java
[config]: https://github.com/LSPosed/LSPosed/blob/f8927757e8704d96611e1f3d75f702e3f0ce061f/daemon/src/main/java/org/lsposed/lspd/service/ConfigManager.java
[module-service]: https://github.com/LSPosed/LSPosed/blob/f8927757e8704d96611e1f3d75f702e3f0ce061f/daemon/src/main/java/org/lsposed/lspd/service/LSPModuleService.java
[old-loader]: https://github.com/LSPosed/LSPosed/blob/f8927757e8704d96611e1f3d75f702e3f0ce061f/core/src/main/java/org/lsposed/lspd/impl/LSPosedContext.java
[system-callback]: https://github.com/LSPosed/LSPosed/blob/f8927757e8704d96611e1f3d75f702e3f0ce061f/core/src/main/java/org/lsposed/lspd/hooker/StartBootstrapServicesHooker.java
[old-api]: https://github.com/libxposed/api/blob/a42f85d06eac3373d266a534ab3b31a584b30774/api/src/main/java/io/github/libxposed/api/XposedModule.java
[old-interface]: https://github.com/libxposed/api/blob/a42f85d06eac3373d266a534ab3b31a584b30774/api/src/main/java/io/github/libxposed/api/XposedModuleInterface.java
[new-api]: https://github.com/libxposed/api/blob/102.0.0/api/src/main/java/io/github/libxposed/api/XposedModule.java
[mi-entry]: https://github.com/JoyElliot/MiHealth_AmapFix/blob/387ba15e30ee13d0dd670e2634e73fa9d0c1bbff/app/src/main/java/io/github/mihealthamapfix/ModernEntry.java
[mi-module]: https://github.com/JoyElliot/MiHealth_AmapFix/blob/387ba15e30ee13d0dd670e2634e73fa9d0c1bbff/stubs/src/main/java/io/github/libxposed/api/XposedModule.java
[mi-interface]: https://github.com/JoyElliot/MiHealth_AmapFix/blob/387ba15e30ee13d0dd670e2634e73fa9d0c1bbff/stubs/src/main/java/io/github/libxposed/api/XposedModuleInterface.java
[new-loader]: https://github.com/JingMatrix/Vector/blob/88f8e1faa8b4e7ce20aefabe9c295cd746ea038e/xposed/src/main/kotlin/org/matrix/vector/impl/core/VectorModuleManager.kt
[new-cl]: https://github.com/JingMatrix/Vector/blob/88f8e1faa8b4e7ce20aefabe9c295cd746ea038e/xposed/src/main/kotlin/org/matrix/vector/impl/utils/VectorModuleClassLoader.kt
