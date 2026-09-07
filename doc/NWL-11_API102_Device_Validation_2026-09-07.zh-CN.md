# NWL-11：K30SU API102 真机验收

> 历史候选包记录。NWL-18 后续由 NWL-19/20 的模块侧实现处理，最终 APK 与复验结果见 [原子规则与统计验证记录](NWL-19_20_Atomic_Config_Statistics_Validation_2026-09-07.zh-CN.md)。以下失败与当时的任务状态保留用于追溯。

## 环境与候选包

K30SU（ADB 8ae208b6）、Android12 / API32、APatch11021、Zygisk Next1.2.7。升级到官方 Vector v2.2 Debug（3080，88f8e1fa），框架 CLI 实际返回 API Version=102。旧 LSPosed 已禁用，NoWakeLock 已启用且保留 system 作用域。

框架下载包 SHA256：`d0c1bf2ca0f1b32e80eb1979e86316656137ac74dff9fe7d25ab624c83715ad7`。

NoWakeLock Release SHA256：`4f79a42a5edd6fb5cf34ea1ef9feba6baa0f6d6588bd64bf643b7f8145a11948`，本地调试签名。含 NWL-15/16/17 修复，89项主机测试通过。Debug SHA256：`db0901f6244fefaf62980ea5a8a5055da9b2afc8ac32e26364ab2e74f085d0cd`。

两处临时入口日志已移除，重建后 APK 哈希与原候选包一致；最终测试安装该包并重启，不把带诊断日志的中间包当成最终产物。

## 入口和检查命令

实机日志确认 API102 无参入口、ModernXposedModule、system hooks 和 SettingsProvider hook 都安装成功。没有发现入口混淆丢失问题。

正确的检查命令是将 `NoWakelock` 作为 method，实际查询名作为 arg，并传入非空 Bundle：

```powershell
adb -s 8ae208b6 shell "su -c 'content call --uri content://settings/system --method NoWakelock --arg CheckConfigBackendStatus --extra probe:s:api102'"
```

早期误将 CheckConfigBackendStatus 直接作为 method，返回 null；那是测试命令错误，不能作为模块缺陷证据。模块自己的日志写入 logcat，不保证出现在 Vector 的 modules 日志文件中。

## 已确认的实时配置同步失败（NWL-18）

应用持久化的 requested/published revision 为 `1788773945717`，系统进程仍读取 `1788773761198`。正确 Provider 查询重复超过15次不追上，之后重启则读到已发布的新版本。冷启动对照中的配置版本 `1788774154953`（放行）、`1788774242224`（阻断）均成功对齐。

这意味着该设备与 Vector 2.2 组合下，保存规则后不能假定规则已在 system_server 实时生效。本轮没有用“每次重启”掩盖这个失败；重启对照只验证冷启动规则和 hooks 的功能。

框架源码提供了明确的根因线索，但未修改或重新构建框架来完成因果回归，因此以下是源码归因：

1. `ConfigCache.getModulesForSystemServer()` 在正式缓存未建立时创建 `InjectedModuleService`，并明确不把 LoadedModule 写入 `state.modules`。
2. system_server 的 VectorRemotePreferences 向该实例注册变更回调。
3. 随后的 `performCacheUpdate()` 查不到 oldModule，创建另一个 InjectedModuleService。
4. ModuleAppService 的更新通知发给正式缓存中的实例；其回调集合不包含开机阶段的订阅者。

设备日志支持这个顺序：17:38:38加载 NoWakeLock，17:38:44才建立正式模块缓存，17:39:05向模块应用发送服务。参考官方 [ConfigCache](https://github.com/JingMatrix/Vector/blob/v2.2/daemon/src/main/kotlin/org/matrix/vector/daemon/data/ConfigCache.kt)、[InjectedModuleService](https://github.com/JingMatrix/Vector/blob/v2.2/daemon/src/main/kotlin/org/matrix/vector/daemon/ipc/InjectedModuleService.kt)、[ModuleAppService](https://github.com/JingMatrix/Vector/blob/v2.2/daemon/src/main/kotlin/org/matrix/vector/daemon/ipc/ModuleAppService.kt)。对应源码已实际保存在 `tmp/xposed-reference/vector-2.2/`。

证据：`.tmp/api102-live-sync-red.txt`、`.tmp/api102-live-sync-verbose.txt`。NWL-18 保持未完成，NWL-11 不能标为完整验收通过。

## 冷启动功能测试

使用独立 Probe APK，先发布放行规则，再发布三项完全阻断规则，最后删除三项测试规则；每次发布后重启做独立对照。

放行：真实 Service 启动/绑定、显式停止/解绑、500ms自动结束均通过；500ms唤醒锁释放通过，手动持锁超过6秒后释放也通过。统计显示 Service allowed=4，Wakelock allowed=2、累计7059ms。正常闹钟7秒内未收到广播，模块记录 allowed=1。

完全阻断：严格完整脚本 PASS。系统没有实际 Probe 持锁；启动服务返回null、绑定返回false；没有异常或实际服务生命周期回调。统计为 Wakelock blocked=1/countTime=0，Alarm blocked=1，Service blocked=2，三类 allowed 均为0。

证据：`.tmp/api102-final-allowed-service.txt`、`.tmp/api102-final-timed-wake.txt`、`.tmp/api102-final-allowed-all.txt`、`.tmp/api102-final-allowed-stats.txt`、`.tmp/api102-final-blocked-all.txt`、`.tmp/api102-final-blocked-stats.txt`。

恢复：删除三项测试规则并重启，配置版本 `1788774321873` 在应用和系统进程一致。Service 专项再次通过，手动持锁释放后累计6654ms、blocked=0；Alarm allowed=1，但仍未在7秒内送达。因此放行和恢复的完整脚本均保留 FAIL，不能被阻断脚本 PASS 覆盖。

最终分项结果：`.tmp/api102-final-results.json`，恢复证据 `.tmp/api102-final-restored-service.txt`、`.tmp/api102-final-restored-all.txt`、`.tmp/api102-final-restored-stats.txt`、`.tmp/api102-final-end-config.txt`。测试没有清空用户统计或修改用户其他规则。

## 闹钟隔离对照

为排除模块运行的影响，通过 Vector CLI 暂时禁用 NoWakeLock，并重启。CLI 状态为 disabled，加载日志没有 NoWakeLock，正确 Provider 检查返回 null。相同 Probe 完整脚本仍因7秒内没有 ALARM_DELIVERED 而失败；ActivityManager 显示 Probe 闹钟已进入后台有序广播队列，dispatchTime 未设置。

因此**该超时可在 NoWakeLock 未加载时独立复现**，不应继续把此现象当成 NoWakeLock 阻断逻辑的缺陷证据。尚未定位设备队列延迟的具体原因，也未修改其他模块或系统广播策略来掩盖失败。这个对照不能把未送达的闹钟验收改写为 PASS。

证据：`.tmp/api102-isolation-disabled-modules.txt`、`.tmp/api102-isolation-disabled-load-log.txt`、`.tmp/api102-isolation-disabled-provider.txt`、`.tmp/api102-isolation-smoke.txt`、`.tmp/api102-isolation-broadcasts.txt`。

隔离结束已重新启用 NoWakeLock 并完成重启，配置状态再次返回 Vector/remote 可用。测试精确规则已移除，测试应用没有遗留持锁、已启动服务或待执行闹钟。最终恢复证据 `.tmp/api102-isolation-restored-modules.txt`、`.tmp/api102-isolation-restored-provider.txt`。

## 热重载的能力边界

热重载是正式 API102 能力，Vector 2.2 宣布支持在系统进程运行中替换模块代码。但 NoWakeLock 尚未实现 `onHotReloading` / `onHotReloaded` 的资源清理与状态交接，默认拒绝热重载，更新 APK 后仍需重启。不能只开启 autoHotReload 就宣称支持。

参考 [官方API102接口](https://libxposed.github.io/api/io/github/libxposed/api/XposedModuleInterface.html) 与 [Vector v2.2发布说明](https://github.com/JingMatrix/Vector/releases/tag/v2.2)。本轮未执行热重载验收。
