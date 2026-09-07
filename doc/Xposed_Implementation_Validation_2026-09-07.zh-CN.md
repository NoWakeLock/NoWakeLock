# 单 APK 双端兼容实现与验证记录

日期：2026-09-07。分支：`feature/dual-xposed-config-backend`，基点 `7d2d417`。工作区含此前尚未提交的实现，本记录不把全部差异归为本次新增。未提交或推送。

后续更新：NWL-12、NWL-14 已修复。当前 APK 哈希、83项测试及最新 Service 验证见 [NWL-14 修复记录](NWL-14_Service_Bind_Result_Fix_2026-09-07.zh-CN.md)，其中明确记录完整恢复检查遇到的后台广播延迟限制；计时修复见 [NWL-12](NWL-12_Wakelock_Duration_Fix_2026-09-07.zh-CN.md)。下文79项测试与哈希保留为首次兼容候选的历史证据。

## 结论与边界

已完成固定旧端 LSPosed 1.11.0 (7209) 与官方 API102 的单 APK 入口、配置、Modern hooks 和诊断实现，以及主机验证、K30SU 旧端回归。**API102 真机验收尚未完成**，不能据此宣布所有新版框架或关联 GitHub 问题已修复。

参考来源、固定 SHA、许可证和实现决策见 [参考审计](Xposed_Implementation_Reference_Audit_2026-09-07.zh-CN.md)，实际源码在 `tmp/xposed-reference`。不支持任意中间 API。旧框架因优先选择 Modern 元数据而需要窄范围旧 ABI 桥，业务 hooks 仍复用原有实现。

## 实现

- `xposed-entry` 提供旧双参数构造与新无参数生命周期分派；API union stubs 仅参与编译。旧适配器与 API102 适配器隔离，最终元数据 min100/target102，作用域 system/android。
- 配置在互斥锁内读取最新数据库事实，按后端一次提交完整快照与 revision，仅清除模块自有旧 key。debug 独立持久化；保存成功与发布成功分开表达，重连重发。
- Modern hooks 按实际方法记录安装结果、去重并允许失败重试；参数缓存按方法分离。SettingsProvider 继续承载事件，诊断分别显示应用发布、Provider 读取及 system_server 上报版本。
- 修复旧框架自注入时应用服务重复初始化造成的 Koin 启动异常。已初始化时复用进程服务；候选应用实际可打开、完成模块检查。
- 两路代码审查指出的读取热路径全量拷贝、系统读取确认缺口和 debug 保存错误表达已修正。

## 主机验证

工具链：JDK17、Gradle9.3.1、AGP9.1.1、Kotlin2.3.21、KSP2.3.6、compileSdk37、buildTools36.0.0。官方 service/interface102 AAR 要求 compileSdk37；minSdk24、targetSdk35 不变。

通过命令：

```powershell
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug :app:assembleRelease -PuseLocalMavenBootstrap=true --offline --no-problems-report
.\gradlew.bat :app:testDebugUnitTest -PuseLocalMavenBootstrap=true --offline --no-problems-report
openspec.cmd validate add-dual-xposed-config-backend --strict
```

第二次单测包含追加的并发旧请求与离线 debug 后续发布用例，最终 **79 tests，0 failures/errors/skipped**。日志 `.tmp/xposed-build.log`、`.tmp/xposed-tests.log`。两次之间主代码未改动。

APK/DEX 审计：两种构建均存在 UniversalXposedEntry，未打包 `io.github.libxposed.api` 类；元数据和 scope 一致。构造 ABI、R8 保留规则和 Modern 隔离已检查。可重复审计脚本 `.tmp/audit_xposed_candidate.py`，结果 `.tmp/xposed-candidate-audit.json`。

| 产物 | 字节数 | SHA256 |
|---|---:|---|
| Debug `app/build/outputs/apk/debug/NoWakeLock-3.0.10.apk` | 23934665 | `3038ac653ad5877aea8cde1c8907327a0386319d6c2772786ca60e8a73b70628` |
| Release `app/build/outputs/apk/release/NoWakeLock-3.0.10.apk` | 2462319 | `721c07ce77840a826dce5f0a59ee447df8ad07bb6ba8e1250e5bcb7291789b13` |

Release 使用现有本地调试签名配置，并非正式发布签名。K30SU 安装包 SHA256 与上述 Release 完全一致。

## K30SU 验证

设备 Android12 / LineageOS，LSPosed1.11.0(7209)，APatch + ZygiskNext。未启用设备上处于禁用状态的 Vector。按用户授权，通过管理器启用 NoWakeLock 与 system 作用域，并自行重启设备。

基线使用官方 v3.0.10 二进制（原包 SHA256 `43be2538cd3298dc7b62b2c4e8199e45cc534db29e5ccf3c5bca1ee37362bac7`），重新签为同一本地调试证书以便覆盖升级，不改变业务代码。基线激活与三类事件均正常；数据备份 `.tmp/k30-baseline-data.tar`，原始证据 `.tmp/k30-baseline-modules.log`、`.tmp/nwl-baseline-enabled.png`。

| 场景 | 实际观察 |
|---|---|
| 旧端加载候选 | 旧 ABI 桥加载；应用启动并完成模块检查，无候选应用 Koin 重复初始化崩溃 |
| 规则实时发布 | 添加临时精确规则后，系统读取新 revision，无须每次修改规则都重启 |
| 应用退出 | force-stop NoWakeLock 后，测试 wakelock/alarm 仍被拦截 |
| 重启持久性 | 带规则重启且尚未启动应用，wakelock blocked=2、alarm blocked=1，allowed 均为0 |
| Service 请求拦截 | `am startservice` 请求不存在的测试组件；添加规则后 blocked 从0增为1，移除后 allowed 从3增为4 |
| 删除规则恢复 | wakelock allowed 从0增为1，alarm allowed 从0增为1，实际打印 ALARM_DELIVERED |
| 配置确认 | 应用 requested/published、Provider observed、system_server hookObserved 最终均为 `1788743056946` |
| 检查界面 | 整体正常、模块激活、三类 hook 有数据，配置后端 remote 可读；截图 `.tmp/nwl-final-check.png` |
| 清理 | 临时规则数据库计数0，debug 恢复 false；未安装独立探针 APK |

临时刺激器源码 `.tmp/NwlCompatibilityProbe.java`，通过 app_process 运行。Service 用不存在的组件验证启动请求分派与阻断，**不是一个真实服务完整生命周期测试**。刺激器结束时自行 kill，因此输出 Killed 不代表模块崩溃。早期刺激器直接 startService 因未注册 IApplicationThread 抛错；已改为 adb am，不能把这条日志算作 system_server 崩溃。

最终记录：`.tmp/k30-final-probe.txt`、`.tmp/k30-final-config.txt`、`.tmp/k30-final-modules.log`、`.tmp/k30-final-app.log`。Provider 单独返回的 requested/published 为0，由应用端补入本地持久化值；截图与私有状态 XML 已交叉核对。该设备 SettingsProvider 与 system_server 同进程 PID2346，跨进程场景仍需新端环境覆盖。

覆盖安装候选后旧系统 hooks 尚未重载时，曾出现 remote reader 版本停留在旧值；重启后消失，随后配置更新正常。因此**升级/启用模块后需要重启**，不承诺热替换。

## 新发现的原有统计问题

阻断后恢复放行的测试中，150ms 唤醒锁显示累计337745ms。原有 `XProvider.newEvent` 在 info 首次创建且 isBlocked=true 时仍进入 `wakelockRegistry.handleAcquire`，后续累计可能包含被阻断区间。`git diff` 确认该事件计时分支本次未修改；这是代码层面原有缺陷，尚未用官方基线重复同一序列证实运行表现完全相同。

本轮确认拦截与事件计数，不声称所有计时统计正确。该问题已登记为 Plane NWL-12，不在兼容实现中静默扩大改动范围。

## 后续验收

Plane NWL-5 至 NWL-10 已完成并附验收说明。NWL-11 保持待办：用户提供实际 API102 环境后，以同一候选执行入口、配置持久化/重连、三类 hooks、事件回传及运行目标诊断验证；涉及修复再回归旧端。GitHub #54/#55/#56 不因本记录自动关闭。
