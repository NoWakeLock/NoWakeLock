# Framework 来源补查待办 2026-03-18

## 状态

- 目的：先固定一份 framework / system 层面的待查清单，后续再逐条去 AOSP、官方文档或系统 APK 中找定义与上下文
- 当前阶段：只建账，不在这份文档里直接下最终解释结论
- 本轮直接证据整理见：[`Framework_Direct_Evidence_2026-03-18.zh-CN.md`](./Framework_Direct_Evidence_2026-03-18.zh-CN.md)
- 数据基础：
  - 真机导出数据库：`C:\Android\tmp\device_nowakelock_db_raw.sqlite`
  - 当前内置说明库：[`app/src/main/assets/da_info.json`](../app/src/main/assets/da_info.json)
  - 当前维护底稿：[`doc/DA_Info_Sourcebook_2026-03-16.zh-CN.md`](./DA_Info_Sourcebook_2026-03-16.zh-CN.md)

## 方法说明

本文件只使用本地证据，不使用新的网络搜索结果。

本轮筛选逻辑：

- 先用当前运行时匹配语义对真机 `info` 表做对照
- 只关注 framework / system 范围的候选：
  - `packageName_info = android`
  - `packageName_info = com.android.phone`
  - `packageName_info = com.android.systemui`
  - `packageName_info = audioserver`
- 明显属于“系统调度外壳包装出的三方 app job/service”先排除，不放进 framework backlog 主清单

## 当前观察摘要

- 真机 `info` 表共有 `1070` 条观测名
- 当前内置说明库共有 `45` 条 canonical entry
- 在 framework / system 范围内，当前真正命中的只有 `6` 个观测名，总 `count = 739`
- 这 `6` 个已命中观测名是：
  - `NetworkStats` `548`
  - `android.appwidget.action.APPWIDGET_UPDATE` `124`
  - `AudioMix` `56`
  - `NfcService:mRoutingWakeLock` `9`
  - `SyncLoopWakeLock` `1`
  - `CdmaInboundSmsHandler` `1`

这意味着 framework 侧还有大量高频名字没有被当前说明库吸收，或者虽然已有相关概念条目，但缺少能命中真机原始名的 alias。

## 第一组：高优先级 framework 待查项

这些名字满足至少一个条件：

- 频次高
- 看起来直接来自 framework / system server / telephony / systemui
- 或者已经明显能看出与当前条目同家族，但还没有被当前说明库命中

| 优先级 | 原始观测名 | 类型 | 包名 | 真机 count | 当前状态 | 建议主查路径 |
|---|---|---|---|---:|---|---|
| P0 | `TIME_TICK` | Alarm | `android` | 2203 | 与现有 `AndroidIntentActionTime_tick` 同家族，但当前未命中 | Android 官方文档 + AOSP 广播常量 |
| P0 | `PowerStatsHighFreqLog` | Alarm | `android` | 1225 | 新候选，无当前条目 | AOSP `powerstats` / `statsd` / `alarm` 相关源码 |
| P0 | `*telephony-radio*` | Wakelock | `com.android.phone` | 1074 | 新候选，明显属于电话栈 | AOSP Telephony / RIL / Phone 相关源码 |
| P0 | `*job.delay*` | Alarm | `android` | 855 | 新候选，像 JobScheduler 内部调度痕迹 | AOSP JobScheduler / AlarmQueue / controllers |
| P0 | `StatsCompanionService.pull` | Alarm | `android` | 652 | 新候选，stats 拉取调度 | AOSP `StatsCompanionService` / stats pull |
| P0 | `com.android.server.stats.StatsCompanionService$PullingAlarmListener` | Wakelock | `android` | 652 | 新候选，与上一条同家族 | AOSP `StatsCompanionService` 内部 listener |
| P1 | `*CountQuotaTracker.cleanup*` | Alarm | `android` | 348 | 新候选，像 framework quota 清理 | AOSP `CountQuotaTracker` / quota / alarm 机制 |
| P1 | `android/com.android.server.notification.NotificationHistoryJobService` | Service | `android` | 189 | 新候选，system server job service | AOSP NotificationManager / NotificationHistory |
| P1 | `ContextHubClientBroker` | Wakelock | `android` | 177 | 新候选，Context Hub/CHRE 方向 | AOSP Context Hub / location / sensor stack |
| P1 | `com.android.keychain/.KeyChainService` | Service | `android` | 144 | 新候选，系统 app/service | AOSP KeyChain app / framework binder 入口 |
| P1 | `*job.flexibility_check*` | Alarm | `android` | 130 | 新候选，像 JobScheduler flexibility controller | AOSP JobScheduler flexibility 相关源码 |
| P1 | `com.android.server.action.NETWORK_STATS_POLL` | Alarm | `android` | 126 | 新候选，和 `NetworkStats` 家族接近 | AOSP NetworkStats / services / action constant |
| P1 | `RILJ_ACK_WL` | Wakelock | `com.android.phone` | 94 | 与现有 `RILJ` 同电话栈家族，但当前未命中 | AOSP RILJ / telephony ack wakelock |
| P1 | `PowerStatsLowFreqLog` | Alarm | `android` | 61 | 与 `PowerStatsHighFreqLog` 同家族 | AOSP `powerstats` / `statsd` |
| P1 | `AppWidgetService_reportWidgetEvents` | Alarm | `android` | 58 | 与 app widget 框架相关，当前未覆盖 | AOSP AppWidgetService / widget event reporting |
| P1 | `android/com.android.server.SmartStorageMaintIdler` | Service | `android` | 60 | 新候选，明显 framework storage maint 方向 | AOSP Storage / Idle / SmartStorage |
| P1 | `*job*r/android/com.android.server.SmartStorageMaintIdler` | Wakelock | `android` | 120 | 与上一条同家族 | AOSP JobScheduler + SmartStorageMaintIdler |
| P1 | `*job*r/android/com.android.server.MountServiceIdler` | Wakelock | `android` | 56 | 新候选，存储维护类 job | AOSP MountService / storage idler |
| P2 | `GsmInboundSmsHandler` | Wakelock | `com.android.phone` | 1 | 与现有 `CDMAInboundSMSHandler` 同家族，但当前未命中 | AOSP inbound SMS handler 家族 |
| P2 | `SatelliteController` | Alarm | `com.android.phone` | 2 | 新候选，现代 telephony 卫星子系统 | AOSP Telephony satellite 模块 |

## 第二组：SystemUI / UI 子系统待查项

这一组频次不高，但名字足够像系统组件，后续适合单独做一轮 AOSP 或 SystemUI APK 补查。

| 优先级 | 原始观测名 | 类型 | 包名 | 真机 count | 当前状态 | 建议主查路径 |
|---|---|---|---|---:|---|---|
| P2 | `Doze` | Wakelock | `com.android.systemui` | 4 | 新候选 | AOSP/SystemUI Doze |
| P2 | `Doze:KeyguardIndication` | Wakelock | `com.android.systemui` | 2 | 新候选 | AOSP/SystemUI Doze + Keyguard |
| P2 | `SysUI:BroadcastSender` | Wakelock | `com.android.systemui` | 5 | 新候选 | SystemUI broadcast sender |
| P2 | `RingtonePlayer` | Wakelock | `com.android.systemui` | 5 | 新候选 | SystemUI audio/ringtone player |
| P2 | `Scrims` | Wakelock | `com.android.systemui` | 2 | 新候选 | SystemUI scrim / display overlay |
| P2 | `AmbientIndication` | Wakelock | `com.android.systemui` | 1 | 新候选 | SystemUI ambient indication |
| P2 | `show keyguard` | Wakelock | `com.android.systemui` | 3 | 新候选 | SystemUI keyguard state machine |
| P2 | `ReverseChargingControl` | Alarm | `com.android.systemui` | 1 | 新候选 | Pixel / SystemUI reverse charging |

## 第三组：当前库里已有概念，但需要官方补强或 alias 补齐的 framework 条目

这一组不是“完全没有条目”，而是目前至少存在以下问题之一：

- 验证状态仍偏弱
- 文案仍大量继承 Amplify 时代经验
- 真实设备上出现了同家族新名字，但当前 match 规则没接住

| canonical key | 标题 | 当前 verification | 真机是否观测到 | 当前问题 |
|---|---|---|---|---|
| `androidintentactiontime_tick` | `AndroidIntentActionTime_tick` | `verified` | 否，真机出现的是 `TIME_TICK` | 需要补 raw alias，把真实设备原始名接住 |
| `networkstats` | `NetworkStats` | `partially_verified` | 是，`548` | 需要补 AOSP 级定义与相关 action/service 背景 |
| `androidappwidgetactionappwidget_update` | `AndroidAppwidgetActionAppwidget_update` | `partially_verified` | 是，`124` | 需要补 framework 定义，可能还要扩 widget 家族相关项 |
| `rilj` | `RILJ` | `community_unverified` | 否，但真机出现 `RILJ_ACK_WL` | 需要扩 telephony 家族 alias / sibling item |
| `cdmainboundsmshandler` | `CDMAInboundSMSHandler` | `community_unverified` | 是，`1` | 需要补 SMS handler 家族，至少把 `GsmInboundSmsHandler` 纳入 backlog |
| `audiomix` | `AudioMix` | `community_unverified` | 是，`56` | 需要判断应查 AOSP audio stack，还是更偏系统服务实现 |
| `nfcservicemroutingwakelock` | `NfcServiceMroutingWakeLock` | `community_unverified` | 是，`9` | 需要官方/NFC 源码补强，不宜继续只靠社区经验 |
| `syncloopwakelock` | `SyncLoopWakeLock` | `partially_verified` | 是，`1` | 需要 framework sync 调度定义补强 |
| `activitymanagerlauncher` | `ActivityManagerLauncher` | `community_unverified` | 否 | 仍属于 framework 高风险解释项，需要 AOSP 核实定义 |
| `windowmanager` | `WindowManager` | `partially_verified` | 否 | 需要从 framework 角度补定义，不应只保留历史经验 |
| `startingalertservice` | `StartingAlertService` | `community_unverified` | 否 | 仍需确认是 framework 还是历史 app/service 影子名 |
| `timedeventqueue` | `TimedEventQueue` | `community_unverified` | 否 | 含义过于模糊，需要源码级确认 |
| `locationmanagerservice` | `LocationManagerService` | `partially_verified` | 否 | 需要系统服务级官方定义补强 |
| `AndroidcontentSyncManagerSYNC_ALARM` | `AndroidcontentSyncManagerSYNC_ALARM` | `partially_verified` | 否 | 需要 sync manager / alarm 官方定义补强 |
| `AndroidappbackupintentRUN` | `AndroidappbackupintentRUN` | `partially_verified` | 否 | 需要 backup manager / broadcast/action 背景补强 |
| `comandroidserverIdleMaintenanceServiceactionUPDATE_IDLE_MAINTENANCE_STATE` | `ComandroisserverIdleMaintenanceServiceactionUPDATE_IDLE_MAINTENANCE_STATE` | `partially_verified` | 否 | 需要在 AOSP 中确认规范名字和真正 action |
| `comandroidserverUPDATE_TWILIGHT_STATE` | `ComandroisserverUPDATE_TWILIGHT_STATE` | `partially_verified` | 否 | 需要确认 twilight 相关真实 action / service 名 |
| `comandroidinternalpolicyimplPhoneWindowManagerDELAYED_KEYGUARD` | `ComandroisinternalpolicyimplPhoneWindowManagerDELAYED_KEYGUARD` | `partially_verified` | 否 | 需要确认 keyguard 调度真实名字与触发点 |

## 第四组：这轮先排除，不纳入 framework 主 backlog

这类名字虽然 `packageName_info = android`，但从原始字符串上已经能看出它们主要是“系统调度器包装出来的 app 侧 job/service”，不应直接当作 framework 定义项处理。

典型例子：

- `*job*r/com.google.android.as.oss/com.google.android.gms.learning.internal.training.InAppJobService`
- `com.google.android.googlequicksearchbox/androidx.work.impl.background.systemjob.SystemJobService`
- `com.google.android.apps.weather/androidx.work.impl.background.systemjob.SystemJobService`
- `com.google.android.gms/.backup.BackupTransportService`
- `com.google.android.vending/com.google.android.finsky.scheduler.process.backgroundimpl.PhoneskyJobServiceBackground`

处理原则：

- 这些名字后续应进入 “GMS / 系统 app / 三方 app 来源补查” 的另一条线
- 不要在 framework backlog 里混查，否则会把 AOSP 方向和 app/APK 方向搅在一起

## 当前结论

这轮先记账后，可以把 framework 侧后续来源补查拆成三条明确路线：

1. 先补 alias / sibling family
- `TIME_TICK`
- `RILJ_ACK_WL`
- `GsmInboundSmsHandler`

2. 再查高频 framework system server 项
- `PowerStatsHighFreqLog`
- `StatsCompanionService.pull`
- `com.android.server.stats.StatsCompanionService$PullingAlarmListener`
- `*CountQuotaTracker.cleanup*`
- `ContextHubClientBroker`
- `com.android.server.action.NETWORK_STATS_POLL`

3. 最后查 scheduler / SystemUI 边界项
- `*alarm*`
- `*job.delay*`
- `*job.flexibility_check*`
- `Doze`
- `SysUI:BroadcastSender`
- `RingtonePlayer`

这份文档的作用是先把 framework 范围内真正值得查的名字固定下来。下一步如果继续推进，建议就按这三组顺序，逐条去做 AOSP / 官方文档 / 系统 APK 的定向补查。
