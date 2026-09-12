# NWL-22：API 102 模块代码热重载

任务：NWL-22。用户已批准实现；OP13 若需要重启，必须先说明，由用户同意后手动重启。实现规范见 [OpenSpec](../openspec/changes/add-api102-module-hot-reload/proposal.md)。

## 当前交付边界

本次实现官方重载生命周期及应用内「模块代码更新」入口。规则修改仍由独立的发布机制生效，不触发代码重载，也不需要应用常驻。

**自动重载元数据暂未启用。** 首个支持版作为 bootstrap 候选，先完成正常加载，再用可区分的 B、C release 验证真实 system_server 更新。宿主测试、独立 ART 测试、安装成功均不等于系统 Hook 已免重启替换成功。

OP13 在首次安装 A2 前执行的旧 Hook 没有退休回调，默认拒绝热重载。升级 APK 无法给已经加载的旧类补上该回调。关闭、重新启用或卸载模块也不保证移除系统进程里的既有 Hook。首次切换通常需要一次正常重启；保留用户规则，不把卸载作为必要步骤。用户随后已手动重启，A2 正常加载的证据见下方。

## 实现和关键约束

- 旧入口继续分派到旧适配器；API 102 回调通过公共 Java 桥分派到现代实现。API stub 仅编译使用，不能打包进 APK。
- 新版本不等待框架重放启动事件：显式恢复系统和 SettingsProvider 类加载器、开机状态、规则及统计资源，再替换 Hook。
- 使用官方 `HookHandle.replaceHook`。同一个方法的替换是原子的，多个方法之间不是事务。未匹配的旧句柄不能自动视为废弃；删除需要显式列出旧 ID，否则报告失败并保留未匹配句柄。
- 代际状态是 JDK/Android 对象图：共享有界队列、信号量、原子计数、规则快照头、活跃锁的中性数组、时间窗口表。退休前后检查对象图，不能传递模块对象、Room、线程或回调。
- 生产者继续向同一个队列提交。旧消费者停止后，新消费者接手；旧 Hook 晚到的事件不依赖旧线程继续存活。消费者停止有时限，不中断数据库事务，超时拒绝并恢复旧消费者。
- 现代 Provider 的跨进程统计入口加入同一队列。同进程消费者首次连接 Provider 时直接处理已经取出的批次，防止获取锁被重新排到释放锁之后。清空统计保持同步完成语义。
- Provider 初始化和退休互斥，退休获取锁有时限；这些锁只用于后台统计/Provider 生命周期，不进入 Hook 规则判断。Room 使用模块拥有的执行器，关闭后等待退出。
- Provider 热重载保留数据库、清空世代、活跃/重叠锁计数及时间基准；普通首次启动仍沿用现有统计初始化行为。
- 应用读取统计或写入本地缓存失败时保留原数据。移除了旧 `AppDasAR` 的异常处理里调用 Provider `ClearData` 的逻辑；两个隔离回归用例在修复前均能复现该误清空行为。本次 OP13 安装过程中没有实际触发清空，旧日志的 `finally` 曾无条件输出类似错误文字，该误导日志也已移除。
- 新版本先恢复最后接受的规则快照，再连接官方 Remote Preferences。重新连接失败不能报告成功；拒绝恢复中的监听器异常不能阻止统计消费者重新启动。
- 框架提交新代际后不保证回滚。模块单独报告恢复失败，不能只看框架 `UP_TO_DATE`、versionCode 或规则 ACK。应用核对异步结果、实际构建身份、必要进程和模块恢复完成状态。

没有修改 Vector，没有新增常驻应用服务，也没有在规则判断中加入存储访问或互斥锁。

## 验证方式

2026-09-12 最终本地结果：113 项单元测试全部通过，无跳过；minified release 和 release lint vital 检查通过；OpenSpec strict 验证通过。最终候选 A2 构建身份为 `3.0.10:87-nwl22-a2-20260912`，SHA-256 为 `f8ead755d820d0e1f3bee8837275eecc56fd11f91dce0b3ecc0edf452a86becb`。A2 包含防止统计同步异常误清空的修复，替代最初的 A 候选。

在 OP13 上独立执行 ART 夹具，旧 ABI 两参数构造及包回调通过；102 连续两次实际类加载器交接通过，旧监听器和线程退出，两份旧模块类加载器可回收。该夹具测试时系统进程 PID 仍为 3431，尚未把此候选加载进真实系统 Hook。

已备份应用数据库/偏好及先前 APK，覆盖安装 A2 成功，应用冷启动正常；安装后 requested/observed 规则 revision 同为 `1789196703103`。随后停止应用，保留现有规则。设备处于锁屏，模块检查新界面的实际点击验收留待用户重启、解锁后进行。没有自动重启，也没有启用自动更新触发元数据。

宿主测试覆盖队列晚到事件、停止超时恢复、跨代规则一致性、重叠持锁跨 Provider 重开、首次同进程连接的获取/释放顺序、官方句柄替换、明确废弃句柄与发现失败的区别、部分失败不得确认成功。

`scripts/EntryAbiProbe.java` 是独立 `app_process` 测试夹具：加载真实框架 dex 与 minified APK，旧 ABI 检查两参数构造及旧包回调；102 模拟框架的装载/退休/重载，检查连续两次交接、监听器/消费者数量以及旧类加载器是否可回收。夹具的框架 attachment 调用只属于测试驱动，产品模块不调用保留接口。该测试不安装系统 Hook，不能替代真实设备回归。

构建使用 `-PuseLocalMavenBootstrap=true --offline`。用于验收的每个 APK 必须传不同的 `-PhookBuildId`，例如 `3.0.10:87-nwl22-a-20260912`、`...-b-...`、`...-c-...`；仅比较 versionCode 87 无效。

真实设备仍需完成：

1. 保存规则和基线；安装 A 后，由用户手动重启并核对 A 的执行身份。
2. 保持受控锁跨越 A→B；检查并发/晚到事件、持续时间、规则和各类计数。B 首先通过应用按钮发起官方重载。
3. 前一次通过后才启用 `autoHotReload=true`，再测试 B→C 的 APK 更新触发路径。
4. 两次更新均保持 system_server PID 和 boot ID 不变；若 SettingsProvider 独立运行，分别核对其进程和代际。
5. 核对用户 0/10 的 Probe 与实际应用拦截，停止 NoWakeLock 应用后继续运行。记录丢弃/失败/积压计数及当前延迟。
6. 补旧框架实际运行回归及首次解锁边界。ART 旧 ABI 通过不能代替旧框架整机回归。

完成上述验收前，Plane 与 OpenSpec 保持进行中，不作为面向所有用户的正式发布。

## 用户手动重启后的实测

2026-09-12 用户确认已重启后，通过 ADB 检查 OP13：boot ID 从 `d3684fd9-9542-4197-9d29-e1eceebff3ad` 变为 `8fb070f4-ded9-49d0-90ee-dd58042f6452`，system_server/SettingsProvider PID 从 3431 变为 3377。两个实际构建身份均为 `3.0.10:87-nwl22-a2-20260912`，两个 `CodeReady=true`，两个 `ReloadFailure=null`。这证明首次正常启动已完成，不是热重载证据。

- 启动恢复规则 revision `1789196703103`；应用冷启动后的新 revision 为 `1789197754037`，随后检查 requested/observed 一致。各系统 Hook 均报告 INSTALLED，remote 后端可用。
- 微信用户 10 的真实拦截继续增长：两次采样中 `PlatformComm` blocked 从 137 增至 186，`MicroMsg.MMAutoAuth` 从 35 增至 36。这些是实际统计读数，不能扩大为所有微信事件都应被阻断。
- 后续停止 NoWakeLock 应用，执行用户 0/10 的 Probe 唤醒锁请求，各轮各用户 allowed 精确增加 1、blocked 不变、记录时长增加，释放后系统持锁列表移除对应项。不过设备处于锁屏，Android 把后台 Probe 锁标成 `DISABLED`，因此只确认用户隔离和放行记账，**不算有效持锁、跨重载持锁或完整行为验收**。已收紧脚本，不能因仅存在系统锁条目就通过有效持锁断言。
- 结束时 `recordPending=0`、`recordDropped=0`、`recordFailed=0`、`recordDurationReliable=true`，没有清空统计或修改真实规则。测试应用已安装到用户 0/10，测试后均停止。
- B release 已构建，身份 `3.0.10:87-nwl22-b-20260912`，SHA-256 `0d9ca713e9ef9dca28b4af8e60412d20289f2cbbf4bb4c9d65333ae1d490b27f`，尚未安装或请求重载。自动重载元数据仍禁用。下一步需要解锁后点击应用官方重载按钮并完成 A2→B、B→C 的实际验收。

证据保存在忽略目录 `.tmp/nwl22/`：`a2-active.txt`、`a2-status.txt`、`a2-boot.txt`、`a2-real-stats.txt`、Probe 前后 JSON 和 held 列表、规则备份及 B APK。最初脚本误把 dumpsys 的历史日志也当作未释放锁，修正后又发现锁条目可能为 DISABLED；早期脚本的 PASS 文字不能作为有效持锁通过依据，以该边界说明和收紧后的断言为准。没有命令重启设备；目前没有发现再次重启的必要。

## 官方依据

- [API 102 生命周期](https://github.com/libxposed/api/blob/102.0.0/api/src/main/java/io/github/libxposed/api/XposedModuleInterface.java)
- [HookHandle 替换语义](https://github.com/libxposed/api/blob/102.0.0/api/src/main/java/io/github/libxposed/api/XposedInterface.java)
- [Service 102 目标与异步重载](https://github.com/libxposed/service/blob/102.0.0/service/src/main/java/io/github/libxposed/service/XposedService.java)

本会话没有可用 Context7 工具，接口核对使用官方固定版本源码及 `tmp/xposed-reference` 中的参考资料。
