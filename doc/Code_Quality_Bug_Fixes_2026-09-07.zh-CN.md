# 现有代码明确问题修复：NWL-15、16、17

本轮扩大检查现有业务代码，不限于 Xposed 兼容迁移。以下三个问题均有修复前失败、修复后通过的回归证据。

## NWL-15：统计重置遗漏拦截次数

`XProvider.clearData(false)` 原先仅清空 count/countTime，遗漏 blockCount。默认统计重置后远端拦截次数残留，后续同步可能带回旧值。

修复增加 `dao.rstAllBlockCount()`。真实 Provider + Room 测试覆盖允许持锁、阻断、重置及再次持锁：重置后的次数、拦截次数、时间均为零，事件清空、统计条目保留，后续持锁重新累计。修复前断言预期0、实际1。

回归文件：`app/src/test/java/com/js/nowakelock/data/provider/XProviderWakelockDurationTest.kt`。

## NWL-16：未写入备份却返回成功

`BackupManager.createBackup` 原先使用 `openOutputStream(uri)?.use`，返回 null 时跳过写入，仍返回 `Result.success(true)`。

修复将无法取得输出流作为 IOException 失败返回。三项测试覆盖空输出流、写入失败时关闭流、成功时实际输出可反序列化的备份。空流测试修复前失败。

回归文件：`app/src/test/java/com/js/nowakelock/data/repository/backup/BackupManagerWriteTest.kt`。

## NWL-17：最近活动时长计算和显示错误

`DADetailRepositoryImpl` 原先对所有事件计算结束时间（或当前时间）减开始时间，而 `DARecentActivitiesCard` 仅给非 Wakelock 显示时长。结果是实际持锁时长隐藏，Alarm/Service 和阻断记录被计算出没有对应持有过程的时长。

修复为仅对未阻断 Wakelock 计算并显示时长；其他记录时长为零且不展示，负值截为零。真实仓库 Flow 测试覆盖阻断 Wakelock、Alarm、Service，以及已结束 Wakelock 的1500毫秒时长。修复前阻断事件预期0、实际88772194165毫秒。

回归文件：`app/src/test/java/com/js/nowakelock/data/repository/daDetail/RecentEventDurationTest.kt`。UI 条件通过代码检查和编译验证，未新增设备 UI 自动化测试。

## 验证与产物

执行 `:app:testDebugUnitTest :app:assembleDebug :app:assembleRelease -PuseLocalMavenBootstrap=true --offline --no-problems-report`，89项测试全部通过，无失败、错误或跳过；Debug/Release 构建成功。

日志：`.tmp/general-audit-red-reset-backup.log`、`.tmp/general-audit-red-duration.log`、`.tmp/general-audit-green.log`。产物审计：`.tmp/general-audit-candidate-audit.json`。

| 产物 | SHA256 |
|---|---|
| Debug | `db0901f6244fefaf62980ea5a8a5055da9b2afc8ac32e26364ab2e74f085d0cd` |
| Release | `4f79a42a5edd6fb5cf34ea1ef9feba6baa0f6d6588bd64bf643b7f8145a11948` |

路径 `app/build/outputs/apk/{debug,release}/NoWakeLock-3.0.10.apk`。Release 仍使用本地调试签名。APK 保留 UniversalXposedEntry、min100/target102、system/android scope，未打包 libxposed API stub 类。

本轮验证在主机完成，尚未将本轮新包安装到设备。统计清空测试使用隔离 Room 数据库，没有清空用户真机统计。K30SU 上一轮 NWL-14 的设备验证不能当作本轮新包验证；后台闹钟广播延迟仍未确定根因，API102 真机验收仍由 NWL-11 跟踪。这三项修复不代表现有代码已无其他问题。
