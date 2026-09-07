# NWL-18：API 102 通信能力与模块参考

日期：2026-09-07。此文件是讨论依据，不是已实现方案；没有修改 NoWakeLock、Vector 或设备。Context7 工具不可用，使用官方 API 源码、稳定版本 AIDL、框架源码及实际模块源码核实。

## 已核实的官方能力边界

1. Hook 侧 `XposedInterface.getRemotePreferences()` 明确只读；`openRemoteFile()` 返回只读文件描述符。二者需要框架具备 `PROP_CAP_REMOTE`。因此不能把远程文件直接当成 Hook 侧可写的统计数据库。[API 102 文档](https://libxposed.github.io/api/io/github/libxposed/api/XposedInterface.html)
2. 应用侧 `XposedService` 提供远程配置编辑及远程文件创建、打开、删除。Vector v2.2 实际实现中，`ModuleAppService.openRemoteFile()` 使用 `MODE_CREATE | MODE_READ_WRITE`，`InjectedModuleService.openRemoteFile()` 使用 `MODE_READ_ONLY`。这是应用端与注入端权限不同，不是调用同名接口就获得相同权限。[应用端实现](https://github.com/JingMatrix/Vector/blob/88f8e1faa8b4e7ce20aefabe9c295cd746ea038e/daemon/src/main/kotlin/org/matrix/vector/daemon/ipc/ModuleAppService.kt)、[注入端实现](https://github.com/JingMatrix/Vector/blob/88f8e1faa8b4e7ce20aefabe9c295cd746ea038e/daemon/src/main/kotlin/org/matrix/vector/daemon/ipc/InjectedModuleService.kt)
3. 稳定 `102.0.0` 的 `IXposedService` 完整方法表包含框架信息、作用域、运行目标、热重载、远程配置和文件操作，没有通用的自定义消息发送或任意模块 Binder 中继接口。文件中的 `SEND_BINDER` 常量不等于提供了应用与 Hook 间自定义服务注册 API。[稳定 AIDL](https://github.com/libxposed/service/blob/102.0.0/interface/src/main/aidl/io/github/libxposed/service/IXposedService.aidl)
4. 官方明确热重载用于模块代码更新，配置变化应使用远程配置及监听器。不能因为热重载参数有 Bundle 就把它当成普通规则通知。[XposedService 源码](https://github.com/libxposed/service/blob/master/service/src/main/java/io/github/libxposed/service/XposedService.java)

这意味着 API 102 可以提供不依赖应用常驻的配置持久化与 Hook 侧读取，但没有现成的“双向业务数据库服务”。统计写回和自定义通信仍需要模块自行设计。也不能以 API 版本号代替框架能力检查与真机验证。

## 实际模块参考及局限

### CustoMIUIzer-A13

核实提交 `8d46dc29851d70ef5e67071ba48db7c43ff20b15`，元数据明确 `minApiVersion=101`、`targetApiVersion=102`。`MainModule` 使用 `this::getRemotePreferences` 构造 `PreferenceBootstrap`，在 `onSystemServerStarting()` 中依次初始化配置、安装系统 Hook、建立监听。这是实际存在的 API 102/system_server 配置路径，仍选择官方 Remote Preferences。[模块元数据](https://github.com/tomthenpc/customiuizer-a13/blob/8d46dc29851d70ef5e67071ba48db7c43ff20b15/app/src/main/resources/META-INF/xposed/module.prop)、[MainModule](https://github.com/tomthenpc/customiuizer-a13/blob/8d46dc29851d70ef5e67071ba48db7c43ff20b15/app/src/main/java/tv/withaibuild/customiuizer/MainModule.java)

其 `PrefsProvider` 名称容易误导：核实的当前实现仅提供测试资源文件，query 返回 null，不能据此声称该模块使用 ContentProvider 同步规则。[PrefsProvider](https://github.com/tomthenpc/customiuizer-a13/blob/8d46dc29851d70ef5e67071ba48db7c43ff20b15/app/src/main/java/tv/withaibuild/customiuizer/PrefsProvider.kt)

上述源码已实际下载到 `tmp/nwl18-MainModule.java`、`tmp/nwl18-PrefsProvider.kt`、`tmp/nwl18-customiuizer-module.prop`，源码树索引为 `tmp/nwl18-customiuizer-tree.json`。本次没有完成其 PreferenceBootstrap 内部实现审计，也没有运行该模块；不能声称它解决了 NWL-18 的通知丢失问题。

### HLocation

发布仓库自述 API 102、system_server、Binder 控制与 SharedMemory 同步，但同时明确未公开源代码。因此仅是存在另一种设计思路的作者声明，不能作为可直接复用、已审计的开源实现。[项目说明](https://github.com/Xposed-Modules-Repo/com.hlocation)

本次没有找到足以证明“API 102 官方新机制已经完整解决应用不常驻、双向统计、开机恢复、跨框架兼容”的可直接套用模块。

## 对 NoWakeLock 的设计判断（尚待验证）

- NWL-18 当前证据是更新通知失效，而冷启动可读取已保存配置。优先保留官方 Remote Preferences 持久化，另以受验证的入口直接推送完整快照并返回实际生效版本，比同时更换存储、通知、统计更容易定位结果。
- Remote Files 适合较大或结构化的静态数据；不会自动补齐通知、原子发布、统计写回。仅将小规则从 Preferences 换成文件，未必得到更好架构。
- SettingsProvider 可以缩减为建立通信连接的入口，而非每个事件都走一次的记录路径；连接后使用模块自有 Binder 是 Android 层设计，不是 API 102 新增保证。必须核实调用者 UID、用户归属、进程与 ClassLoader、断线恢复。
- 系统 Hook 内存聚合统计、应用打开时读取，可以不依赖应用常驻。但若只保留内存，重启会丢统计；若要求持续保留历史，需要独立验证系统侧持久化目录、权限、写入批次和损坏恢复。官方 Hook 侧只读文件接口本身不能承担该写入。
- 继续复用已有持久化统计组件，先优化同进程调用或真正批量记录，是比立即自建常驻服务更小的变更；K30SU 的具体进程关系应以本地实测记录为准，不推广到所有 ROM。

建议与 [运行架构讨论](NWL-18_API102_Runtime_Design_Discussion_2026-09-07.zh-CN.md) 一并阅读。以上候选方案均没有因此获得“所有 API 102 框架可用”的结论。
