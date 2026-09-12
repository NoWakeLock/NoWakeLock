# NWL-23：API102 Service 重载重复记账

## 问题与复现

OP13 / Android 16 / Vector 2.2 API102，在 NWL-22 热重载验收期间发现：Probe 只启动一次 Service、绑定一次，Service allowed 却增加 3，预期为 2。进一步分开请求，启动使计数从 7 变为 9，绑定从 9 变为 10；Probe 日志各只有一次请求和实际生命周期回调。

现代实现对全部 `startServiceLocked` 重载安装 Hook。ROM 的同一请求在重载之间嵌套调用，每层都判断规则并记账，导致启动计数重复。旧运行路径本轮未修改；不能将本机现象扩大为所有 ROM 或所有旧框架。

单元测试通过真实 `ModernServiceHook` 注册的回调模拟系统重载嵌套，修复前同一 Intent 的一次请求得到 2 条事件；原方法抛异常后再次请求得到 3 条，预期分别是 1、2。不同用户或不同 Intent 的嵌套请求原本就应独立计数，该对照通过。

## 修复

用线程局部调用帧识别同一操作、同一 Intent 对象、同一调用包及同一 Android 用户的重载嵌套。重复层继续执行原方法，不重复判断和记账；不同请求不合并。`finally` 恢复外层帧，最外层返回时移除，原方法异常也不会留下抑制状态。

调用帧通过 NWL-22 运行状态交接：只有原生 JDK `ThreadLocal`、中性数组、字符串、整数和平台 Intent，不保存模块对象或回调。线程局部对象在 Hook 安装时取得，规则判断不新增互斥锁、存储读取或 Binder 调用。生命周期校验只接受这个明确拥有的 ThreadLocal，不能任意传递其他 ThreadLocal。

## 验证与边界

120 项主机测试全部通过，minified B3 release 和 release lint vital 检查通过。测试夹具使用暂停的统计消费者检查提交次数；测试结束清理其合成事件，避免后续 Room 测试误消费夹具数据，不涉及设备统计清空。

B3（`3.0.10:87-nwl22-b3-20260912`）已通过官方服务从 B2 热重载。OP13 system_server/SettingsProvider PID 均保持 3377，boot ID 不变，两用户跨更新持锁、计数和时长连续。完整矩阵通过：用户 0 放行、用户 10 阻断，交换规则后用户 10 恢复、用户 0 阻断，最终用户 0 恢复。每轮 Wake +1、Alarm +1、Service 启动及绑定共 +2 分别进入应有的 allowed 或 blocked，另一用户数据不变。应用每次发布后强停，测试结束移除 Probe 规则，40 条真实事件规则和 8 条应用规则保持原样，统计 pending/dropped/failed 均为 0。最终自动更新候选的结果另记入 NWL-22 验收文档。

证据位于忽略目录 `.tmp/nwl22/`：`b2-service-isolation.json`、`b2-service-isolation-events.txt`、`service-overload-red2.txt`、`b3-verified-build.txt`、`b2b3-run.txt`、`b3-*` 行为及统计记录。

最终 C（versionCode 88）通过 APK 自动重载后再次完成同样的全用户矩阵，Service 启动/绑定仍精确 +2，规则恢复与统计健康检查通过。新引入的线程局部状态也已实际跨 B3→C 交接，PID 与 boot ID 未变化。NWL-23 的修复和本机验收完成。
