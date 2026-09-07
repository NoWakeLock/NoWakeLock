# NWL-12：首次阻断后的唤醒锁计时虚增

## 根因与修复

`XProvider.newEvent` 先创建/更新统计行，再登记持锁区间。已有统计行的阻断请求会提前返回；但首次阻断进入 `info == null` 分支，创建统计行后仍执行 `handleAcquire`。这个从未实际持有的锁没有配对释放，后续正常锁的累计时长因而包含中间等待时间。

修复将计时登记条件限定为 `type == Type.Wakelock && !isBlocked`。阻断事件明细和 blockCount 保留，正常持锁、重叠区间和释放逻辑不变。没有修改数据库结构或重新计算旧统计；重新加载系统模块后从新事件开始正确计时。

## 回归证据

测试文件：`app/src/test/java/com/js/nowakelock/data/provider/XProviderWakelockDurationTest.kt`。测试调用真实 `XProvider.getMethod`，使用 Robolectric、Room 数据库和真实 WakelockRegistry，固定毫秒时间。

修复前的命令：

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests '*XProviderWakelockDurationTest' -PuseLocalMavenBootstrap=true --offline --no-problems-report
```

四个用例中两个失败：首次阻断、等待300000ms、实际持锁150ms，预期150而实际300150；仅首次阻断时，预期持锁中时长0而实际300000。重叠持锁与重复 acquire/release 用例通过。失败证据保存在 `.tmp/nwl12-red.xml`、`.tmp/nwl12-red.log`。

修复后完整 **83 tests，0 failures/errors/skipped**，Debug/Release 构建均成功，日志 `.tmp/nwl12-green-build.log`。新增四项回归全部通过。APK 审计确认入口、min100/target102、scope 和未打包 API stubs 均符合此前候选约束。

| 产物 | SHA256 |
|---|---|
| Debug | `3cc101acb259e7a5a28901e7031da09815bf3910b5e0ab1e5513b03d7cf95566` |
| Release | `22ce9964c653f76167b48d7313253c3225a1ecf19793fbfcad76acdf93653a45` |

产物路径仍为 `app/build/outputs/apk/{debug,release}/NoWakeLock-3.0.10.apk`，使用本地调试签名。审计结果 `.tmp/nwl12-candidate-audit.json`。修复前 Release 保留于 `.tmp/nwl12-before.apk`。

## 设备验收

K30SU / Android12 / LSPosed1.11.0(7209) 安装上述 Release 并重启，设备安装包哈希已核对一致。

- 使用全新名称 `NWL_DurationProbeWake`，避免此前异常状态影响结果。首次请求阻断后：allowed=0、blocked=1、duration=0。
- 隔一段时间删除规则并由应用发布，再请求持锁约150ms并释放：allowed=1、blocked=1、**duration=152ms**。同一序列未再把中间等待计入累计时长；闹钟恢复正常投递。
- 应用 requested/published、Provider observed、system_server hookObserved 均为 `1788768900743`，debug=false；三类 hook 的 hasData 均为 true。
- 测试规则计数0，设备临时刺激器已删除；源码仅保留于 `.tmp/NwlDurationProbe.java`。没有安装独立探针 APK。

设备证据 `.tmp/nwl12-device-blocked.txt`、`.tmp/nwl12-device-allowed.txt`、`.tmp/nwl12-final-config.txt`。NWL-12 已完成；API102 环境仍由 NWL-11 单独验收。代码未提交或推送。
