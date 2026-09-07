# NWL-19/20：原子规则、异步统计与真机验证

> 后续用户已明确取消历史性能对比要求，改为评估当前绝对延迟与开销。当前测量已完成，NWL-20 已标为 Done，见 [NWL-20 当前延迟验收](NWL-20_Current_Latency_2026-09-07.zh-CN.md)。下方关于缺少历史基线的未完成状态保留为当时记录，不再构成当前验收的前置条件。

## 实现范围

NWL-19 把规则编译为不可变快照，每次 Hook 判断只取一次快照。写入端校验、复制并编译正则后，用 volatile 引用一次替换；读端不加互斥锁，不读取文件，也不等待 Binder。API102 先用官方 Remote Preferences 持久化，再通过已有 SettingsProvider Hook 推送同一版本，检查系统实际激活版本的 ACK。只接受主用户 NoWakeLock UID，限制载荷 192 KiB，拒绝旧版本、同版本不同内容、非法类型和无效正则。Provider 启动时未确认的推送最多尝试三次，间隔100ms和300ms；复用同一快照版本，收到匹配 ACK 即停止，不建立常驻重试任务。

旧端保留原有入口及配置传输。XSharedPreferences 的文件刷新改由专用线程完成，Hook 活动最多每30秒触发一次刷新；空闲时线程停放。旧 LSPosed 的 Remote Preferences 桥仍用原有通知更新，不进入 API102 推送路径。

NWL-20 使用容量4096的有界队列与单消费者；Hook 提交不等待消费者，后台最多256事件一批写入同一个 Room 事务。SettingsProvider 实际初始化后才绑定数据库处理器，同进程直接批处理；不同进程保留现有 Provider 后备路径。没有更改数据库位置、模式或保留周期。

清空统计同时推进代次与单调时间截止线，拒绝清空前滞留事件。队列满或争用超限会明确计入 dropped；数据库失败计入 failed。出现丢失或批处理失败会停止累加不可靠的持锁时长并清空活动注册表，清空统计后恢复时长计算。过载时不承诺无损统计，也不以虚构时长掩盖丢失。

未修改 Vector，没有增加 Binder 服务发现、应用后台服务或空闲轮询。上述原子化只针对规则数据；更新模块 APK 仍需重启，未启用代码热重载。

## 产物与主机检查

- 最终 APK：`app/build/outputs/apk/release/NoWakeLock-3.0.10.apk`。
- SHA256：`ac187ddbde425361d675c53bf85a641ea126c90cf216d13cc9f691154d95b824`。
- 最终构建日志：`.tmp/nwl20-final-retry-build-verified.txt`；98项测试、35个 suite，零失败和错误。重试补丁前候选包为 `554ce1cb06dd6cfef63976535be37b258253fb35725d6818ef248adeaeabb380`，其构建日志 `.tmp/nwl20-reviewed-build.txt` 为96项测试通过。
- 覆盖快照并发与防御复制、旧/冲突版本拒绝、载荷边界、Hook 不访问 preferences、并发队列顺序和容量、重叠持锁时长、清空代次屏障。
- `openspec.cmd validate update-runtime-config-and-statistics --strict`：通过。

## 设备环境与范围

K30SU / M2007J3SC，序列号8ae208b6，Android12/API32，APatch11021，Zygisk Next1.2.7。API102 环境为官方 Vector2.2（3080，88f8e1fa），NoWakeLock 已启用 system 作用域；旧框架对照使用设备原有 LSPosed1.11.0（7209）。

Probe 的三个精确名称为 `NWLProbe:Wake`、`com.js.nowakelock.probe.ALARM`、`com.js.nowakelock.probe/.ProbeService`。测试只修改 Probe 用户0的规则，并在结束时删除测试规则。清空屏障测试会重置当前统计计数，执行前已导出数据库目录；这份运行中目录备份不是经过 SQLite 一致性校验的备份。

旧 LSPosed1.11.0 对重试补丁前候选包（554ce1cb…）的完整 listener 套件六项全部通过：Service专项、定时唤醒锁、完整放行、完全阻断、Service恢复、完整恢复。三次规则版本为1788780225255、1788780296594、1788780328718，system_server PID2238始终不变，发布后 NoWakeLock 被强行停止。阻断阶段新增计数为唤醒锁1、闹钟1、Service2，正常计数和持锁时长不增加；移除规则后正常计数和持锁时长恢复，拦截数不再增长。pending/dropped/failed均为0。证据 `.tmp/nwl20-old-*`。最后补丁只改变 supportsPush=true 的 API102 发布路径，旧框架不进入该分支；没有把不同 APK 哈希的证据混为一轮。

该旧框架实测走 Remote Preferences 桥（backend=remote），不能扩大为所有原版 Xposed/XSharedPreferences 设备均已真机验证。纯 XSharedPreferences 路径完成代码检查，本轮没有另一台纯旧框架设备证据。

切换回 Vector 后 CLI 确认2.2/3080/API102。首次最终包放行测试时，19:27:56 的 vold 日志明确因 `/storage/emulated/0/Android/obb` 挂载前缀向 Probe PID6591 发出 Interrupt，随后系统报告该进程死亡；该次请求在 NoWakeLock 统计中为放行，阻断为0。该轮完整结果保留 FAIL，后续恢复轮次通过，system_server PID2251未变化。证据 `.tmp/nwl20-final102-run.txt`、`nwl20-final102-allowed-all.txt`、`nwl20-final102-logcat.txt`。脚本增加探针进程存活断言，设备稳定后另跑整轮，使用独立证据前缀，未覆盖失败记录。

设备稳定后，554ce1cb…候选包的 API102 整轮六项均通过，system_server PID2251始终不变，NoWakeLock 应用被强行停止，规则版本1788780682886→1788780746333→1788780778329。统计增量断言也通过，阻断期间持锁时长不增加，恢复后增加；队列无丢弃、失败或积压。证据 `.tmp/nwl20-final102b-*`。重试补丁最终 APK 的独立交付复验使用 `.tmp/nwl20-delivery-*` 前缀。

最终 ac187ddb… APK 交付复验通过，日志 `.tmp/nwl20-delivery-run.txt`：

| 验证项 | 实际结果 |
|---|---|
| 冷启动，NoWakeLock 未运行 | 恢复版本1788781220185，唤醒锁/闹钟/Service启动与绑定均被阻断 |
| 同 PID 实时放行→阻断→恢复 | 六项全部 PASS；PID2143不变；版本1788781297548→1788781361777→1788781393982 |
| 阻断计数增量 | 唤醒锁+1、闹钟+1、Service+2；正常计数不变，持锁时长保持7867ms |
| 恢复后的计数与时长 | 正常计数继续增加，拦截数不增；持锁时长增加至15244ms |
| 消费者健康 | pending=0、dropped=0、failed=0、durationReliable=true；direct=1011、Provider calls=1 |
| 扩展规则与权限 | 正则、熄屏、时间窗口、非法发布拒绝均 PASS；PID2143仍未变 |
| 应用不常驻 | 每次发布后强停 NoWakeLock，再执行行为验证；冷启动也不启动应用 |

最终状态核对见 `.tmp/nwl20-delivery-final-state.txt`，已恢复 Probe 放行规则并停止两款应用，保留官方 Vector/API102 和禁用的旧 LSPosed。设备安装 APK 哈希与交付 APK 一致。

## 冷启动与扩展规则证据

554ce1cb…候选包的冷启动测试通过：发布完全阻断规则后强行停止 NoWakeLock，再重启设备，应用没有运行，系统仍恢复版本 `1788779736915`；三类阻断严格测试全部通过，system_server PID2244。证据 `.tmp/nwl20-cold-run.txt`、`nwl20-cold-status.txt`、`nwl20-cold-smoke.txt`。

较早的完整候选包（SHA256 `18b4f30ca40e243b996346531f3b619de2887c6b90fbde87c7ff7747e5e4d239`）已通过 listener 模式的放行、阻断、移除规则恢复，以及正则、熄屏、时间窗口规则。最终包相对此候选包补充了统计批处理失败时的时长失效处理；不能把前一产物的证据冒充最终包测试。候选证据保存在 `.tmp/nwl20-listener-*`、`.tmp/nwl20-extra-run.txt`。

554ce1cb…候选包通过真机清空屏障：标准 content 命令提交清空前时间戳的滞留事件，计数仍为0；提交清空后新事件，计数为1，并正常结束该事件。非法 UID 的发布请求被拒绝，日志明确 `Unauthorized configuration publisher`，无 ACK，版本仍为1788780520077。框架会将 hook 拒绝异常处理为空返回，这不是 system_server 崩溃。证据 `.tmp/nwl20-fence-shell-run.txt`、`nwl20-fence-status.txt`。最初 Java 辅助进程被终止，没有作为通过依据；shell 测试也修正了 content 参数不能带冒号的问题后才通过。

## 闹钟测试边界

普通 PendingIntent 广播闹钟在放行/恢复时仍会超过脚本7秒窗口；显式前台广播模式也复现超时。此前禁用 NoWakeLock 并重启仍能复现普通广播超时，详见 NWL-11 历史隔离记录，因此不能据此归因为 NoWakeLock。此次 listener 模式绕开广播队列，仍经过同一个系统 Alarm Hook，可验证放行投递、拦截、恢复和取消。listener 通过不等于广播模式通过。

## 性能观察

在同一设备上，各执行3组300次立即申请/释放唤醒锁，以下为一次前后采样，不是电量、纯 Hook 延迟或统计显著性基准：

| 观察项 | NWL-19/旧统计实现 | NWL-20 候选实现 |
|---|---:|---:|
| 约13秒窗口 system_server CPU ticks | 1173 | 290 |
| 固定观察窗口内新增计数 | 862 | 900 |
| 最终新增计数 | 900 | 900 |
| 累计时长增量 | 160315ms（旧实现异常） | 373ms |
| 三组应用 API 总耗时 | 225/307/313ms | 318/307/283ms |

旧实现稍后补齐900条，不能说永久丢了38条；旧时长明显超过实际操作窗口。新实现诊断 pending=0、dropped=0、failed=0、direct=2778、Provider calls=1（初始化后备调用），批量数473。这些数字包含同一进程之前的活动，不是900次探针操作的独立总量。整个 system_server CPU 受其他活动影响，前后跨重启，不能据此承诺省电比例。

证据 `.tmp/nwl20-perf-before.json`、`.tmp/nwl20-before-settled.txt`、`.tmp/nwl20-perf-after.json`。

## 尚未完成的性能验收

NWL-20 原任务要求的完整 Hook 延迟分布、分配/GC 对比尚未采集，不能用应用批量 API 耗时或整个 system_server 的 CPU ticks 替代。目前留存的性能基线只有以上采样，没有可直接重放的 NWL-19 Release APK；另一个早期 baseline-debug APK 也不是同一基线，不能混用。因此 NWL-20 应保留 In Progress，功能验证通过不代表原任务全部性能验收完成。设备强制队列溢出与独立进程 Provider 的 ROM 场景也未做真机注入；队列满、高并发和重叠时长由主机测试覆盖。

Plane 已同步：NWL-19、NWL-18 标为 Done；NWL-20 保持 In Progress，记录剩余性能验证；NWL-11 已追加本轮结果并保持 In Progress。没有提交或推送 Git，当前分支为 `feature/dual-xposed-config-backend`。
