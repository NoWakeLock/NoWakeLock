# NWL-14：旧端 bindService 阻断返回值

## 根因与修复

旧 `ServiceHook` 对 `startServiceLocked` 与 `bindServiceLocked` 共用处理逻辑，阻断时统一设置 `param.result = null`。前者返回 ComponentName，可用 null 表达未启动；后者返回 primitive int，框架把 null 拆箱时抛出空指针。

修复根据被 hook 的实际 Method.returnType 选择 int 的0或引用类型的null。Android12 的 [ContextImpl.bindServiceCommon](https://raw.githubusercontent.com/aosp-mirror/platform_frameworks_base/android-12.0.0_r1/core/java/android/app/ContextImpl.java) 将返回0转换为false。ModernServiceHook 已使用 ModernHookSupport.defaultReturn 按实际类型返回默认值，本次保留该路径。

## 回归入口

已有真实测试 APK `test-harness` 是本问题的回归入口：真实 Activity 调用真实 Service，不用假组件或仅测试一个返回值函数。严格脚本既检查未收到启动/连接回调，也检查没有 ERROR，以及启动返回null、绑定返回false。

```powershell
python test-harness/smoke.py --adb C:/Android/SDK/platform-tools/adb.exe --serial 8ae208b6 --blocked
```

前置条件：NoWakeLock 中 Probe 的三项精确规则完全阻断，名称见 test-harness/README.md。

修复前已重复失败，证据 `.tmp/nwl14-device-red.txt`：`ERROR bind java.lang.NullPointerException: Integer.intValue() on a null object reference`。测试不是通过不执行回调掩盖调用异常。

## 验收

83项单测全部通过，Debug/Release 构建成功（`.tmp/nwl14-build.log`）。现有 ModernHookSupportTest 已覆盖 int 阻断返回0且不调用原方法。最终包仍为 min100/target102、system/android scope，API stubs 未打包（`.tmp/nwl14-candidate-audit.json`）。

| APK | SHA256 |
|---|---|
| Debug | `2fc31128be5851f159dec153374ae47cc9e492b246cd991801f9267f70d83c96` |
| Release | `0f976a4662eb3e8e9d58ebf7336a68bfd2d87669a14d49d2fc46df0cb3d7a528` |

产物 `app/build/outputs/apk/{debug,release}/NoWakeLock-3.0.10.apk`，继续使用本地调试签名。K30SU 安装包哈希已核对一致，安装后已重启加载系统 hooks。API102 真机仍由 NWL-11 单独追踪。

修复后完整阻断脚本通过（`.tmp/nwl14-device-blocked.txt`）：startService 返回 null，bindService 返回 false，没有 ERROR，也没有实际启动或连接回调。数据库 Service allowed=0 / blocked=2，分别对应启动与绑定；wakelock 和 alarm 各 blocked=1。

恢复规则后，真实 Service 启动、绑定、连接、停止、解绑、销毁以及500ms自动结束均通过。专项命令增加 `--service-only`，输出明确标记专项，避免其他系统机制影响本问题反馈环。证据 `.tmp/nwl14-service-restored.txt`、`.tmp/nwl14-service-blocked.txt`、`.tmp/nwl14-service-final-restored.txt`。

## 本轮完整恢复检查的限制

完整恢复脚本曾因7秒内未收到 ALARM_DELIVERED 而失败，**没有将这次完整套件标为通过**。模块数据库记录该闹钟 allowed=1，AlarmManager 将其列为 in-flight，ActivityManager 显示对应广播已入后台队列但 dispatchTime 未设置。证据 `.tmp/nwl14-device-restored.txt`、`.tmp/nwl14-broadcast-delay.txt`。尚未确定后台队列延迟的根因，不能仅据此归因为模块或系统；未通过延长等待、修改广播优先级或改变设备设置掩盖该失败。

这不影响已经验证的 bindService 返回值修复结论，但仍限制“整套旧端测试全部通过”的表述。新 API102 真机也未在本轮验证。
