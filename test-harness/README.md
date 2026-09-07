# NoWakeLock Probe

独立 Android 测试 APK，包名 `com.js.nowakelock.probe`。原生 Java + Android 控件，无第三方依赖、网络权限、开机任务或自动重试。用于验证 NoWakeLock 已有行为，不属于模块正式 APK。

## 独立控制

| 操作 | 控制方式 | 观察证据 |
|---|---|---|
| Partial wakelock | 申请 / 释放；0 手动，正数为自动释放毫秒数 | 系统 `dumpsys power`、NoWakeLock 计数与时长 |
| 一次性精确唤醒闹钟 | 自定义延迟毫秒数 / 取消；再次安排会替换上次 | `ALARM_DELIVERED`、模块计数 |
| Started Service | 启动 / 停止；0 手动，正数自动 stopSelf | `SERVICE_CREATED/STARTED/DESTROYED` |
| Bound Service | 绑定 / 解绑；0 手动，正数自动解绑 | `SERVICE_BOUND/CONNECTED/UNBOUND/DESTROYED` |

定时范围 0..3600000ms。闹钟的0表示尽快安排，不是手动；系统可能施加最小延迟或在休眠时推迟。精确闹钟权限不足会记录 `ALARM_PERMISSION_REQUIRED`，不会假装已发出请求；页面提供授权入口。

Started 和 Bound 是同一个真实 Service，因此**已绑定时只停止 started 状态不会销毁服务，最后解绑后才销毁**。这是用来验证的实际生命周期，不做假回调。普通 Service 的后台存活仍受 Android 限制；本工具不承诺后台长期保活。测试 started/bound 最好先分别执行，再组合执行。

“全部停止”释放本应用锁、取消本应用闹钟、停止并解绑本应用服务。“清空日志”会先执行全部停止。Activity 销毁会释放锁并解绑；普通进入后台不等于销毁。进程被终止时内存中的定时器不再工作，系统会清理持锁/绑定；PendingIntent 闹钟可能仍投递，因此结束测试应先按全部停止。

日志保留最近80行，带实际毫秒时间，既显示在页面也写入 `NWLProbe` logcat。`*_REQUEST` 与 `*_API_RETURNED` 仅代表调用，不代表系统执行。尤其 WakeLock 的本地 isHeld 在被 hook 阻断时仍可能为true，必须对照系统实际锁与模块统计。

## 构建 / ADB

从仓库根目录：

```powershell
.\gradlew.bat :test-harness:assembleDebug -PuseLocalMavenBootstrap=true --offline
adb -s DEVICE install -r test-harness/build/outputs/apk/debug/test-harness-debug.apk
adb -s DEVICE shell am start -W -n com.js.nowakelock.probe/.ProbeActivity --es command wake --el duration_ms 0
adb -s DEVICE shell am start -W -n com.js.nowakelock.probe/.ProbeActivity --es command release
adb -s DEVICE logcat -s NWLProbe:I '*:S'
```

命令：`wake`、`release`、`alarm`、`cancel`、`start`、`service-stop`、`bind`、`unbind`、`stop`、`reset`、`permission`。`duration_ms` 对 wake/start/bind 表示时长，对 alarm 表示延迟；省略时使用页面当前值。每次 am start 只执行一次命令，旋转恢复不重放命令。

API102 对照测试新增 `alarm-foreground`（显式使用前台广播队列），脚本传 `--foreground-alarm` 启用，默认仍测试普通后台广播，不掩盖后台队列问题。`burst --el duration_ms N` 为独立压测命令，N 表示唤醒锁申请/释放对数，范围1..3000；输出调用总耗时，必须同时检查系统侧计数、积压与丢弃诊断，不能把它当成纯Hook耗时或电量测量。

`alarm-listener` / `--listener-alarm` 使用 AlarmManager.OnAlarmListener 直接回调，仍经过系统 Alarm Hook，名称与上表相同。该模式用来隔离广播队列延迟，不能替代 PendingIntent 广播投递验收；进程退出会失去 listener。三个闹钟模式互斥，默认仍为普通广播。

当前延迟测试命令 `latency --ei count 200 --ei interval_ms 100` 在一个有限生命周期的工作线程上申请并立即释放同一唤醒锁，逐次记录 acquire/release API 耗时。次数1..3000、间隔0..1000ms，配置的总等待时间最多120秒；0间隔表示突发。结束后将分位数和完成数量写到 `files/latency.json`，可用 `adb shell run-as com.js.nowakelock.probe cat files/latency.json` 读取。没有逐事件日志或磁盘写入；“全部停止”和 Activity 销毁会取消测试。这是应用侧完整 API 耗时，包含 Binder、框架和系统原方法，不能称为纯 NoWakeLock Hook 耗时。

可重复自动检查（需要已安装、已解锁、允许精确闹钟）：

```powershell
python test-harness/smoke.py --adb C:/Android/SDK/platform-tools/adb.exe --serial DEVICE
# 在 NoWakeLock 中给下方三个名称设置完全阻断，再运行：
python test-harness/smoke.py --adb C:/Android/SDK/platform-tools/adb.exe --serial DEVICE --blocked
# 删除测试规则后再跑第一条，检查恢复。
```

脚本操作会替换本测试应用当前请求并清空其日志，结束时执行全部停止。无须 root 即可运行；通过 debug APK 的 run-as 读取本应用日志，通过 dumpsys 验证真实持锁。脚本只检查现有规则的效果，不自行修改 NoWakeLock 规则。失败后的界面日志用于区分权限、系统限制和模块拦截，不能只靠“没有回调”断言兼容成功。

## NoWakeLock 精确规则

包名均为 `com.js.nowakelock.probe`，用户0：

| 类型 | 名称 |
|---|---|
| Wakelock | `NWLProbe:Wake` |
| Alarm | `com.js.nowakelock.probe.ALARM` |
| Service | `com.js.nowakelock.probe/.ProbeService` |

先放行记录基线，再完全阻断三项，最后移除规则恢复；测试完成恢复放行。需要测试熄屏阻断时只更改对应规则，申请锁后手动熄屏，并结合日志和模块统计检查。

参考：[WakeLock](https://developer.android.com/reference/android/os/PowerManager.WakeLock)、[AlarmManager](https://developer.android.com/reference/android/app/AlarmManager)、[Bound Service 生命周期](https://developer.android.com/develop/background-work/services/bound-services)。

## 2026-09-07 实测

NWL-19/20 后续实现与最终验收见 [原子规则与统计验证记录](../doc/NWL-19_20_Atomic_Config_Statistics_Validation_2026-09-07.zh-CN.md)。下文保留早期候选包与发现缺陷时的历史结果，不代表最终 APK 状态。

API102 后续验收见 [NWL-11 真机记录](../doc/NWL-11_API102_Device_Validation_2026-09-07.zh-CN.md)：Vector2.2 冷启动阻断通过，实时配置同步失败由 NWL-18 跟踪；后台闹钟超时在禁用 NoWakeLock 并重启后仍可复现。专项通过不代表完整套件通过。

后续更新：NWL-14 的旧端绑定返回值已修复。最新模块 APK、真实 Service 红绿回归和本轮完整恢复检查限制见 [NWL-14 记录](../doc/NWL-14_Service_Bind_Result_Fix_2026-09-07.zh-CN.md)。脚本支持 `--service-only` 单独检查 Service，不把专项 PASS 当作全部场景 PASS。以下保留发现缺陷时的历史结果。

Plane NWL-13 跟踪本工具。APK 已安装于 K30SU，SHA256 `747c33405448082a4cc270402ec296442ea44df4c097555f87ef45249069448c`。放行和规则移除后恢复测试通过：手动持锁、启动/绑定超过5秒后显式释放/停止/解绑，实际系统锁和真实服务生命周期符合控制；500ms定时操作与闹钟取消也通过。

旧端完整阻断测试**未通过**：实际 `bindService` 遭阻断后抛出 Integer.intValue 空指针。唤醒锁/闹钟阻断、Service启动阻断均有模块统计支持，但不能据此掩盖绑定异常。已登记 **NWL-14**；严格脚本会因 `ERROR bind` 失败，不将“没有连接回调”判为成功。工具可用于该缺陷后续红绿回归。

证据：`.tmp/probe-smoke-allowed.txt`、`.tmp/probe-smoke-restored.txt`、`.tmp/probe-smoke-blocked-strict.txt`、`.tmp/probe-device-events.log`、`.tmp/probe-ui.png`。早期 `.tmp/probe-smoke-blocked.txt` 的 PASS 只检查了是否执行，已由严格异常检查结果取代。实际UI已截图检查；结束后已移除测试规则，测试 APK 保留供继续测试。
