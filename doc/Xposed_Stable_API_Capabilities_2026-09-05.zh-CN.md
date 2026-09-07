# 新版稳定 Xposed API 能力与本地实验复核

日期：2026-09-05。问题范围：新框架上该用什么机制，以及当前未完成实现哪些仍适用。入口分派另见 [入口兼容调查](Xposed_Entry_Dispatch_Research_2026-09-05.zh-CN.md)。本轮只读源码与联网调查，没有改业务代码、升级依赖、运行构建或真机验证。

## 结论

新框架应使用完整 Modern 路径。官方稳定 API102 仍以 interceptor hook chain、进程生命周期回调、Remote Preferences/Files 为基础；本地按 API101 写的这些实验没有整体过时。新增运行目标查询、hook ID/原子替换、detach 与热重载可改进诊断/调试，但不能替代配置通信或证明实际拦截成功。[API][api]、[生命周期][lifecycle]、[service][service]

必须分清两条数据流：规则从 App 发布到框架，再由 hook 进程读取；事件统计从 hook 进程返回记录端。官方 remote preferences/files 在 hook 进程中是只读接口，不能直接承担后一条写入通道。本地 SettingsProvider/事件记录路线仍需独立验证。[API][api]、[本地 XpRecord](../app/src/main/java/com/js/nowakelock/xposedhook/model/XpRecord.kt)

## 版本依据：稳定版与开发分支分开

- Maven Central 的 api 与 service 当前 release/latest 均为 `102.0.0`。[api metadata](https://repo1.maven.org/maven2/io/github/libxposed/api/maven-metadata.xml)、[service metadata](https://repo1.maven.org/maven2/io/github/libxposed/service/maven-metadata.xml)
- api `102.0.0` 固定提交：`45e7c5cfe54725b6d828d8b7be65e22ce60c67e4`；service `102.0.0`：`3318940876192e29cf6ab07637e899e22a87ebf0`。本报告能力表依据这两个 tag，而非默认分支的名称。
- 本轮 api `102.0.0...master` 对比仅显示 `Chain.proceedWith` 的 Javadoc 澄清：改变 receiver 不等于选择另一个 override 或另一个 hook chain。没有据此发现新的公开方法；不能把开发分支注释扩展称为新 API 代际。[对比](https://github.com/libxposed/api/compare/102.0.0...79b75b49255a257d38f67bce2e928649dc2fc6c9)
- Vector v2.2 是采用 API102 的具体稳定框架；API 库版本不是所有 fork 的兼容承诺。[发布说明](https://github.com/JingMatrix/Vector/releases/tag/v2.2)
- 本地 [app/build.gradle](../app/build.gradle) 仍使用 api/service `101.0.0`，[module.prop](../app/src/main/resources/META-INF/xposed/module.prop) 仍是 min101/target101。增加102专属调用需要重新评估编译依赖与运行时保护；依赖版本、minApi、targetApi是不同维度，不应一起机械提升。

## 可以采用的能力及本地对应

| 需求 | 官方稳定接口 | 本地状态与后续判断 |
|---|---|---|
| 系统进程中安装三类拦截 | `onSystemServerStarting`，参数提供系统 ClassLoader；`PROP_CAP_SYSTEM` 表示系统进程能力 | ModernXposedModule 已接入回调，ModernXposedSystemHookInstaller 已存在。应记录实际能力与安装结果，不能仅因 API 数字满足就承诺系统 hook |
| 获取应用真正可用的 ClassLoader | `onPackageLoaded` 在默认 loader 就绪时触发；`onPackageReady` 在 AppComponentFactory 创建 loader 后触发 | 本地 SettingsProvider 入口等待 PackageReady 的方向有依据；具体 scope/ROM 生命周期需设备记录 |
| 替代 XposedBridge / XC_MethodHook | `hook(Executable).intercept(Hooker)`，通过 `chain.proceed()` 继续或直接返回阻断 | ModernHookSupport 与三类 Modern hook 已使用此机制；要逐项比较旧版阻断返回值、记录次数、异常行为 |
| 替代 XposedHelpers 查找 | 使用目标 ClassLoader 和 Java reflection 获取 Class/Method；必要时官方 `getInvoker` 调用 | 本地已用 Class.forName、Method；不是所有旧工具函数一一对应，OEM签名识别仍由模块负责 |
| 原方法调用、内联排查 | `getInvoker(...).setType(ORIGIN)`；`deoptimize(Executable)`；`hookClassInitializer` | 稳定接口可用，本地没有接入这些调用。按证据使用；不把 deoptimize 作为无数据问题通用补丁 |
| 控制 hook 异常传播 | HookBuilder exception mode 或 module.prop `exceptionMode` | 本地 protective 配置仍适用；它不吞掉原方法/后续 chain 抛出的所有异常 |
| 向 hook 进程发布规则 | App `XposedService.getRemotePreferences(group)` 写；hook `XposedInterface.getRemotePreferences(group)` 读；检查 `PROP_CAP_REMOTE` | ConfigPublisher、RemotePreferencesManager、SharedPreferencesHookConfigReader 已有实验实现；继续验证发布与读取版本一致性 |
| 较大只读配置文件 | App 管理 remote files，hook `openRemoteFile` 只读 | 可以作为大规则快照候选；当前无需求证据要求替换小配置 preferences |
| 服务绑定和框架识别 | `XposedServiceHelper.registerListener`；bind/died回调；API/name/version/properties/scope | 本地已注册与重发；官方允许多个框架多次bind，本地只缓存一个 service，需选择策略或验证单框架假设 |
| 查询模块实际运行目标（102新增） | `XposedService.getRunningTargets()` | 本地未使用；可补充状态页和实验日志，含PID/UID/进程/版本/状态，但不能证明某个业务hook成功或已有事件 |
| hook ID与原子替换（102新增） | `HookBuilder.setId`、`HookHandle.replaceHook` | 本地未使用；同模块同 Executable 同ID替换可辅助重复安装控制，但不解决框架选错入口或类加载失败 |
| 停止当前入口后续回调（102新增） | `XposedInterfaceWrapper.detach()` | 本地未使用；仅停止当前entry生命周期，不撤销已装hook，不触发Legacy回退 |
| 更新模块代码无需重启进程（102新增） | service `hotReloadModule` + `onHotReloading/onHotReloaded` | 本地未支持完整清理/重建，先不纳入兼容修复验收；官方明确禁止用热重载传播配置变更 |

能力来源：[XposedInterface][api]、[XposedModuleInterface][lifecycle]、[XposedInterfaceWrapper][wrapper]、[XposedService][service]、[XposedServiceHelper][helper]。

## 必须纠正的“旧机制被移除”表述

1. API102 文档明确是 **target102+ 的 libxposed 模块不能调用 legacy `de.robv.android.xposed` API**，不是所有新框架都删除所有Legacy模块能力。我们选择纯Modern新路径，可避免依赖这种过渡容忍；目标API101在API102框架中运行与正式target102不是一回事。[API][api]
2. Modern没有 zygote 注入，应使用被作用域覆盖的目标进程回调。不能将旧 `initZygote` 的初始化时序直接搬过去。[API][api]、[生命周期][lifecycle]
3. New XSharedPreferences 的支持会随框架改变。新路径使用Remote Preferences是明确替代方式；旧路径是否维持原样与问题1的框架分类/加载方式关联，不能只凭类存在判断旧配置完整可用。
4. Android的ContentResolver/ContentProvider并非因Modern API就自动消失。我们对SettingsProvider的hook要用Modern接口安装；接口存在不证明当前方法签名、scope及数据库所在进程正确。[本地 ModernSettingsProviderHook](../app/src/main/java/com/js/nowakelock/xposedhook/modern/ModernSettingsProviderHook.kt)

## 配置链路：API102没有推翻旧实验，但有未完成项

本地链路为 `Room → ConfigPublisher → remote group Nowakelock → hook reader`，旧后端另行保留。规则格式保持一致有利于复用业务判定；这是本地实现设计，不是官方为NoWakeLock提供的自动迁移服务。

- `ConfigPublisher.start()` 在初次启动与service bind后全量发布；单条规则、App regex、debug有单独方法。官方listener只应注册一次，本地AtomicBoolean符合这一点。[本地 publisher](../app/src/main/java/com/js/nowakelock/data/config/ConfigPublisher.kt)、[manager](../app/src/main/java/com/js/nowakelock/data/config/XposedRemotePreferencesManager.kt)、[官方helper][helper]
- 官方RemotePreferences `commit()` 先更新本地缓存，再向框架提交；提交失败可返回false，而App本地读取已是新值。**App读回成功不是hook读到成功**。`apply()` 异步提交也不能提供同步远端确认。[官方 RemotePreferences][prefs]
- 本地 `ConfigSnapshotWriter` 分key发布，`writeAll` 只遍历当前规则，不删除历史key。下一轮应实验删除规则/恢复较少规则的备份是否留下旧值，以及多个key中途失败是否形成部分配置；这是源码可见的待验证风险，不是已确认issue根因。[本地 writer](../app/src/main/java/com/js/nowakelock/data/config/ConfigSnapshotWriter.kt)
- 本地 remote reader 获得对象就把 `isReadable=true`，`refresh()`为空；应验证框架端更新通知、服务重启后的读取、快照版本/探针key，而不是把对象非空等价为配置新鲜。[本地 reader](../app/src/main/java/com/js/nowakelock/xposedhook/model/HookConfigReader.kt)
- 最新官方102仍建议配置变更使用Remote Preferences及change listener，不用hot reload。这条结论已来自稳定service，不是仅开发分支文案。[service][service]

## 事件与健康检查：官方新能力能辅助，不能直接全替换

本地 `XpRecord` 将事件以ContentResolver.call发送到Settings.System URI；ModernSettingsProviderHook拦截专用method并转交XProvider，后者写InfoDatabase。不是“hook进程写RemotePreferences”。[XpRecord](../app/src/main/java/com/js/nowakelock/xposedhook/model/XpRecord.kt)、[XProvider](../app/src/main/java/com/js/nowakelock/data/provider/XProvider.kt)

稳定API102的远程共享读接口没有提供通用的 hook→App 可写事件队列；热重载Bundle和运行目标查询也不是业务日志管道。[API][api]、[service][service] 因此本轮没有证据支持“升级102就可以删除SettingsProvider/统计通信”。可先保留并验证现有通道，若要改用自定义Binder/Provider属于另一项设计，需要考虑权限、服务寿命与系统进程开销。

`getRunningTargets()` 是值得优先评估的新诊断能力：可区分未加载/加载旧代码等框架状态；但本地ModuleCheckManager用数据库存在记录推测hook效果，二者应并列呈现。另在SettingsProvider进程读到remote配置，不代表system_server的reader也成功，必须逐进程验证。[本地检查](../app/src/main/java/com/js/nowakelock/data/manager/ModuleCheckManager.kt)

## 热重载为何应单独做实验

API102默认 `onHotReloading` 返回false；允许重载前必须清理模块拥有的线程、外部回调、JNI等引用。新代不会自动重放package生命周期，默认 `onHotReloaded` 只解除旧hook。[生命周期][lifecycle]

本地XpRecord有Timer、队列和异步coroutine，Modern installer有安装guard与静态状态。直接打开autoHotReload不构成完整实现，还需事件排空、旧线程退出、句柄替换与重新注册方案。本轮将其列为后续调试能力，不用它取代正常配置刷新或首次兼容验收。

## 下一轮实验顺序（未执行）

1. **入口成功后再测能力**：记录进程、API/runtime、framework版本及SYSTEM/REMOTE位；具体稳定目标暂以Vector2.2为可复现基线，其他fork另列明确版本。
2. **纯Modern hook探针**：无副作用方法，验证回调、proceed/阻断、异常和安装次数；再移入wakelock/alarm/service，逐项保持旧业务语义。
3. **配置一致性**：App写版本/探针key，分别从system_server及SettingsProvider读回；覆盖App退出、重启、bind/died、断连提交、恢复备份、删除规则、regex、多key部分失败。
4. **事件闭环**：制造一次已知事件，检查hook→ContentResolver→SettingsProvider→数据库→UI，避免把“无数据”只归因于配置读取。
5. **102诊断增强**：独立评估getRunningTargets，验证与模块日志对应；setId/替换、detach、hot reload另做可选实验。

既有tasks中的真机smoke未完成，不能用历史打包/单元测试通过替代以上结果。此报告只更新能力判断，不改变已批准范围或声称#54/#55已解决。

[api]: https://github.com/libxposed/api/blob/45e7c5cfe54725b6d828d8b7be65e22ce60c67e4/api/src/main/java/io/github/libxposed/api/XposedInterface.java
[lifecycle]: https://github.com/libxposed/api/blob/45e7c5cfe54725b6d828d8b7be65e22ce60c67e4/api/src/main/java/io/github/libxposed/api/XposedModuleInterface.java
[wrapper]: https://github.com/libxposed/api/blob/45e7c5cfe54725b6d828d8b7be65e22ce60c67e4/api/src/main/java/io/github/libxposed/api/XposedInterfaceWrapper.java
[service]: https://github.com/libxposed/service/blob/3318940876192e29cf6ab07637e899e22a87ebf0/service/src/main/java/io/github/libxposed/service/XposedService.java
[helper]: https://github.com/libxposed/service/blob/3318940876192e29cf6ab07637e899e22a87ebf0/service/src/main/java/io/github/libxposed/service/XposedServiceHelper.java
[prefs]: https://github.com/libxposed/service/blob/3318940876192e29cf6ab07637e899e22a87ebf0/service/src/main/java/io/github/libxposed/service/RemotePreferences.java
