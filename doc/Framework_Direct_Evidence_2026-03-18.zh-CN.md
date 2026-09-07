# Framework 直接证据记录 2026-03-18

## 目标

这份文档记录本轮已经拿到的 framework / system 直接证据，重点回答两个问题：

1. 真机里出现的原始名，是否能在源码里直接找到。
2. 这些原始名更像是从哪一种 framework 机制暴露出来的：
   - `AlarmManager` 的 tag
   - `WakeLock` 的 name/tag
   - `StateMachine` / handler 的显式命名
   - `TAG + 后缀`

本文件只使用两类来源：

- `local_lineage_source`：本地 Lineage16 源码树 `\\wsl.localhost\Debian\home\js\android\lineage`
- `official_aosp_web`：`android.googlesource.com` 官方 AOSP 源码网页

本文件不使用社区帖子、论坛经验或第三方整理内容。

## 证据等级

- `direct`：源码里直接出现原始名本体，且能直接看出它被用作 alarm tag / wakelock name / 显式类名。
- `inferred_from_local_source`：需要把两个本地源码点拼起来看，但推导链条短且清晰。
- `web_only_direct`：本地 Lineage16 没直接搜到，但官方 AOSP 网页源码里能直接看到。
- `unresolved`：目前还没有足够直接的源码证据。

## 已拿到的直接证据

| 原始观测名 | 类型 | 结论 | 证据等级 | 关键证据 |
|---|---|---|---|---|
| `TIME_TICK` | Alarm | 来自 `AlarmManagerService` 的内部 time tick alarm tag | `direct` | `TIME_TICK_TAG = "TIME_TICK"`；`isTimeTickAlarm(...)` 直接比较该 tag；同时构造 `Intent.ACTION_TIME_TICK` |
| `PowerStatsHighFreqLog` | Alarm | 来自 powerstats 定时采样触发器的高频 timer 名称 | `direct` | `TimerTrigger` 里直接创建 `new PeriodicTimer("PowerStatsHighFreqLog", ...)` |
| `PowerStatsLowFreqLog` | Alarm | 来自 powerstats 定时采样触发器的低频 timer 名称 | `direct` | `TimerTrigger` 里直接创建 `new PeriodicTimer("PowerStatsLowFreqLog", ...)` |
| `*telephony-radio*` | Wakelock | 来自 `RIL` 主 radio wakelock tag | `direct` | `RILJ_WAKELOCK_TAG = "*telephony-radio*"` |
| `RILJ_ACK_WL` | Wakelock | 来自 `RIL` 的 ACK 专用 wakelock 名称 | `direct` | `RILJ_ACK_WAKELOCK_NAME = "RILJ_ACK_WL"` |
| `*CountQuotaTracker.cleanup*` | Alarm | 来自 `CountQuotaTracker` 的 cleanup alarm tag | `direct` | `ALARM_TAG_CLEANUP = "*" + TAG + ".cleanup*"` |
| `*job.delay*` | Alarm | 来自 `TimeController` 的 delay alarm tag | `direct` | `DELAY_TAG = "*job.delay*"` |
| `*job.flexibility_check*` | Alarm | 来自 `FlexibilityController.FlexibilityAlarmQueue` 的 alarm tag | `direct` | `super(..., "*job.flexibility_check*", "Flexible Constraint Check", ...)` |
| `StatsCompanionService.pull` | Alarm | 来自 `StatsCompanionService` 给 statsd 拉取用 alarm 设置的 tag | `direct` | `mAlarmManager.setExact(..., TAG + ".pull", mPullingAlarmListener, ...)` |
| `com.android.server.stats.StatsCompanionService$PullingAlarmListener` | Wakelock | 来自 `PullingAlarmListener` 使用 canonical name 创建 wakelock | `direct` | `new WakelockThread(..., PullingAlarmListener.class.getCanonicalName(), ...)` |
| `ContextHubClientBroker` | Wakelock | 来自 Context Hub broker 使用 `TAG` 创建 wakelock | `direct` | `TAG = "ContextHubClientBroker"`；`newWakeLock(..., TAG)` |
| `GsmInboundSmsHandler` | Wakelock | 高概率来自 GSM SMS handler 的显式状态机名称，经 `InboundSmsHandler` 作为 wakelock name 使用 | `inferred_from_local_source` | `GsmInboundSmsHandler` 构造函数传 `super("GsmInboundSmsHandler", ...)`；`InboundSmsHandler` 在构造阶段创建 SMS 分发 wakelock 并使用 `getName()` |
| `com.android.server.action.NETWORK_STATS_POLL` | Alarm | 官方 AOSP 中直接定义为 `NetworkStatsService` 的 poll action；当前本地 Lineage16 树未直接搜到 | `web_only_direct` | 官方 AOSP `NetworkStatsService.java` 直接定义 `ACTION_NETWORK_STATS_POLL = "com.android.server.action.NETWORK_STATS_POLL"` |

## 逐项证据

### 1. `TIME_TICK`

结论：这不是“应用自定义字符串”，而是 framework alarm 子系统内部明确维护的固定 tag。

本地直接证据：

- `frameworks/base/apex/jobscheduler/service/java/com/android/server/alarm/AlarmManagerService.java`
  - `static final String TIME_TICK_TAG = "TIME_TICK";`
  - `private static boolean isTimeTickAlarm(Alarm a) { return a.uid == Process.SYSTEM_UID && TIME_TICK_TAG.equals(a.listenerTag); }`
  - 同文件还构造 `Intent.ACTION_TIME_TICK`

说明：

- 从这里看，真机 raw name `TIME_TICK` 应按 framework alarm tag 理解，而不是按普通广播 action 原文展示来理解。

### 2. `PowerStatsHighFreqLog` / `PowerStatsLowFreqLog`

结论：这两个名字都直接来自 powerstats 定时采样触发器，不是外部 app 名。

本地直接证据：

- `frameworks/base/services/core/java/com/android/server/powerstats/TimerTrigger.java`
  - `new PeriodicTimer("PowerStatsLowFreqLog", ...)`
  - `new PeriodicTimer("PowerStatsHighFreqLog", ...)`
  - 同文件还定义：
    - `LOG_PERIOD_MS_LOW_FREQUENCY = 60 * 60 * 1000`
    - `LOG_PERIOD_MS_HIGH_FREQUENCY = 2 * 60 * 1000`

说明：

- 这意味着后续说明文案里可以把它们明确归为 powerstats 周期采样，而不是笼统写成“未知系统任务”。

### 3. `*telephony-radio*` / `RILJ_ACK_WL`

结论：这两条都属于 telephony RIL 家族，而且是源码里直接定义的两个不同 wakelock 名称。

本地直接证据：

- `frameworks/opt/telephony/src/java/com/android/internal/telephony/RIL.java`
  - `static final String RILJ_LOG_TAG = "RILJ";`
  - `static final String RILJ_WAKELOCK_TAG = "*telephony-radio*";`
  - `static final String RILJ_ACK_WAKELOCK_NAME = "RILJ_ACK_WL";`

说明：

- `*telephony-radio*` 和 `RILJ_ACK_WL` 不应该再混成一个模糊条目。
- 更合理的做法是按同一家族的两个 raw alias / sibling item 处理。

### 4. `*CountQuotaTracker.cleanup*`

结论：这条不是猜测，源码里就直接定义了这个 cleanup alarm tag。

本地直接证据：

- `frameworks/base/services/core/java/com/android/server/utils/quota/CountQuotaTracker.java`
  - `private static final String ALARM_TAG_CLEANUP = "*" + TAG + ".cleanup*";`
  - `TAG = CountQuotaTracker.class.getSimpleName()`
  - `maybeScheduleCleanupAlarmLocked()` 里用该 tag 调 `scheduleAlarm(...)`

说明：

- 这条可以直接作为 canonical raw 解释来源，无需再依赖 Amplify 时代经验文案。

### 5. `*job.delay*`

结论：这是 JobScheduler `TimeController` 的 delay alarm tag。

本地直接证据：

- `frameworks/base/apex/jobscheduler/service/java/com/android/server/job/controllers/TimeController.java`
  - `private final String DEADLINE_TAG = "*job.deadline*";`
  - `private final String DELAY_TAG = "*job.delay*";`

补充的官方网页证据：

- 官方 AOSP：
  - <https://android.googlesource.com/platform/frameworks/base/+/ea4b420bff775cb55544d8843d6befd1b4eeccf9/apex/jobscheduler/service/java/com/android/server/job/controllers/TimeController.java>

说明：

- 这类 raw 名称属于 JobScheduler 内部 timing controller 的 alarm tag，不应误归到某个 app job service 名称上。

### 6. `*job.flexibility_check*`

结论：这是 JobScheduler `FlexibilityController` 的内部 alarm tag。

本地直接证据：

- `frameworks/base/apex/jobscheduler/service/java/com/android/server/job/controllers/FlexibilityController.java`
  - `class FlexibilityAlarmQueue extends AlarmQueue<JobStatus>`
  - 构造时直接调用：
    - `super(context, looper, "*job.flexibility_check*", "Flexible Constraint Check", true, ...)`

说明：

- 这类 raw 名字说明设备上的 JobScheduler 已经包含 flexible constraints 相关逻辑。

### 7. `StatsCompanionService.pull`

结论：这是 statsd companion 的 pulling alarm tag，源码里直接拼接出来。

本地直接证据：

- `packages/modules/StatsD/service/java/com/android/server/stats/StatsCompanionService.java`
  - `static final String TAG = "StatsCompanionService";`
  - `public void setPullingAlarm(long nextPullTimeMs) { ... mAlarmManager.setExact(..., TAG + ".pull", mPullingAlarmListener, mHandler); }`

说明：

- 这条与下面的 `PullingAlarmListener` wakelock 应当视为一组配套条目。

### 8. `com.android.server.stats.StatsCompanionService$PullingAlarmListener`

结论：这不是偶然出现的 Java 类名泄漏，而是被源码直接拿去作为 wakelock name。

本地直接证据：

- `packages/modules/StatsD/service/java/com/android/server/stats/StatsCompanionService.java`
  - `public final static class PullingAlarmListener implements OnAlarmListener`
  - `new WakelockThread(mContext, PullingAlarmListener.class.getCanonicalName(), ...)`
  - `WakelockThread` 内部 `newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, wakelockName)`

说明：

- 这说明当前 raw 观测名正是 framework 直接暴露出来的 canonical class name。

### 9. `ContextHubClientBroker`

结论：这是 Context Hub broker 侧的 wakelock 名称，来源非常直接。

本地直接证据：

- `frameworks/base/services/core/java/com/android/server/location/contexthub/ContextHubClientBroker.java`
  - `public class ContextHubClientBroker extends IContextHubClient.Stub`
  - `private static final String TAG = "ContextHubClientBroker";`
  - `mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);`

说明：

- 这条适合归入 Context Hub / CHRE / sensor hub 通信家族。

### 10. `GsmInboundSmsHandler`

结论：目前最合理的理解是，raw name 不是“日志描述文本”，而是 GSM 入站短信状态机名称，经 `InboundSmsHandler` 变成 wakelock 名。

本地证据链：

- `frameworks/opt/telephony/src/java/com/android/internal/telephony/gsm/GsmInboundSmsHandler.java`
  - `public class GsmInboundSmsHandler extends InboundSmsHandler`
  - 构造函数：`super("GsmInboundSmsHandler", context, storageMonitor, phone, looper, featureFlags);`
- `frameworks/opt/telephony/src/java/com/android/internal/telephony/InboundSmsHandler.java`
  - 注释明确写 `Wake lock to ensure device stays awake while dispatching the SMS intents.`
  - 构造阶段创建 partial wakelock，并使用 `getName()`

判定：

- 这里属于 `inferred_from_local_source`，但推导链条很短，置信度仍然高。
- `CdmaInboundSmsHandler` 也应按同样机制理解。

### 11. `com.android.server.action.NETWORK_STATS_POLL`

结论：官方 AOSP 里这是 `NetworkStatsService` 直接定义的 poll action；当前本地 Lineage16 树没有直接搜到，说明它更可能是旧 branch / 旧实现 / 设备侧残留家族。

官方网页直接证据：

- 官方 AOSP：
  - <https://android.googlesource.com/platform/frameworks/base/+/9a24f337aa5459963197555f6ae83b1c23146beb/services/core/java/com/android/server/net/NetworkStatsService.java>
  - 其中直接定义：
    - `public static final String ACTION_NETWORK_STATS_POLL = "com.android.server.action.NETWORK_STATS_POLL";`

当前本地状态：

- 在本地 `frameworks/base` 与 `packages/modules/Connectivity/service/src` 的定向搜索中，没有直接搜到 `NETWORK_STATS_POLL` 或 `NetworkStatsService.java`。

说明：

- 这里要避免误写成“当前 Lineage16 本地就能直接证实”。
- 更稳妥的写法是：
  - 这是 Android framework 正式出现过的官方 action 名；
  - 但当前本地树未直接命中；
  - 需要后续结合设备实际 Android 基线 / 厂商分支再判断是否应保留为现役条目还是历史兼容 alias。

## 这轮最重要的机制结论

本轮已经能明确看到，真机里的 framework raw name 主要来自四类机制：

1. `AlarmManager` tag
- 例：`TIME_TICK`
- 例：`*job.delay*`
- 例：`*job.flexibility_check*`
- 例：`StatsCompanionService.pull`
- 例：`*CountQuotaTracker.cleanup*`
- 例：`PowerStatsHighFreqLog`

2. `WakeLock` name
- 例：`ContextHubClientBroker`
- 例：`RILJ_ACK_WL`
- 例：`*telephony-radio*`
- 例：`com.android.server.stats.StatsCompanionService$PullingAlarmListener`

3. 状态机 / handler 显式命名
- 例：`GsmInboundSmsHandler`
- 同家族：`CdmaInboundSmsHandler`

4. `TAG + 后缀`
- 例：`StatsCompanionService.pull`

这对后续数据源重构很重要，因为说明库不能只按“包名 / 类名 / 广播 action”单一维度维护，而需要允许：

- raw alarm tag
- raw wakelock tag
- raw class canonical name
- alias / sibling family

## 对后续说明库整理的直接启发

优先应考虑补齐这些 family：

- `TIME_TICK` <- 现有 `androidintentactiontime_tick` 需要接住真实 raw 名
- `RILJ` family
  - `*telephony-radio*`
  - `RILJ_ACK_WL`
- `InboundSmsHandler` family
  - `GsmInboundSmsHandler`
  - `CdmaInboundSmsHandler`
- `PowerStats` family
  - `PowerStatsHighFreqLog`
  - `PowerStatsLowFreqLog`
- `JobScheduler` internal family
  - `*job.delay*`
  - `*job.deadline*`
  - `*job.flexibility_check*`
  - `*CountQuotaTracker.cleanup*`
- `StatsCompanionService` family
  - `StatsCompanionService.pull`
  - `com.android.server.stats.StatsCompanionService$PullingAlarmListener`

## 仍待后续补查

这轮还没有完成直接证据闭环的项：

- `android/com.android.server.notification.NotificationHistoryJobService`
- `com.android.keychain/.KeyChainService`
- `AppWidgetService_reportWidgetEvents`
- `android/com.android.server.SmartStorageMaintIdler`
- `*job*r/android/com.android.server.SmartStorageMaintIdler`
- `*job*r/android/com.android.server.MountServiceIdler`
- `SatelliteController`
- 多个 SystemUI raw 名（`Doze`、`SysUI:BroadcastSender`、`RingtonePlayer` 等）

## 来源清单

### 本地源码

- `\\wsl.localhost\Debian\home\js\android\lineage\frameworks\base\apex\jobscheduler\service\java\com\android\server\alarm\AlarmManagerService.java`
- `\\wsl.localhost\Debian\home\js\android\lineage\frameworks\base\services\core\java\com\android\server\powerstats\TimerTrigger.java`
- `\\wsl.localhost\Debian\home\js\android\lineage\frameworks\base\services\core\java\com\android\server\powerstats\PowerStatsLogger.java`
- `\\wsl.localhost\Debian\home\js\android\lineage\frameworks\base\services\core\java\com\android\server\utils\quota\CountQuotaTracker.java`
- `\\wsl.localhost\Debian\home\js\android\lineage\frameworks\base\apex\jobscheduler\service\java\com\android\server\job\controllers\TimeController.java`
- `\\wsl.localhost\Debian\home\js\android\lineage\frameworks\base\apex\jobscheduler\service\java\com\android\server\job\controllers\FlexibilityController.java`
- `\\wsl.localhost\Debian\home\js\android\lineage\packages\modules\StatsD\service\java\com\android\server\stats\StatsCompanionService.java`
- `\\wsl.localhost\Debian\home\js\android\lineage\frameworks\base\services\core\java\com\android\server\location\contexthub\ContextHubClientBroker.java`
- `\\wsl.localhost\Debian\home\js\android\lineage\frameworks\opt\telephony\src\java\com\android\internal\telephony\RIL.java`
- `\\wsl.localhost\Debian\home\js\android\lineage\frameworks\opt\telephony\src\java\com\android\internal\telephony\InboundSmsHandler.java`
- `\\wsl.localhost\Debian\home\js\android\lineage\frameworks\opt\telephony\src\java\com\android\internal\telephony\gsm\GsmInboundSmsHandler.java`

### 官方 AOSP 网页

- <https://android.googlesource.com/platform/frameworks/base/+/9a24f337aa5459963197555f6ae83b1c23146beb/services/core/java/com/android/server/net/NetworkStatsService.java>
- <https://android.googlesource.com/platform/frameworks/base/+/ea4b420bff775cb55544d8843d6befd1b4eeccf9/apex/jobscheduler/service/java/com/android/server/job/controllers/TimeController.java>
