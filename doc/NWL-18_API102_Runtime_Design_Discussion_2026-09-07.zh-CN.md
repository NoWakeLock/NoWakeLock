# API 102 数据流重新讨论（未实施）

约束：不修改 Vector；NoWakeLock 应用不需要常驻；旧版工作路径保留。

## 核实事实

- API 102 的 Hook 端 Remote Preferences 和 Remote Files 都只读；并非通用双向数据存储。[官方接口](https://libxposed.github.io/api/io/github/libxposed/api/XposedInterface.html)
- 官方服务将热重载限定为模块代码更新，明确不应用于配置传播。[XposedService](https://github.com/libxposed/service/blob/master/service/src/main/java/io/github/libxposed/service/XposedService.java)
- K30SU 的历史证据 `.tmp/api102-isolation-restored-provider.txt` 显示 SettingsProvider 与 system_server 报告来自同一 PID 2269。不能将此直接推广到所有 ROM，也不能仅靠同 PID 断言模块 ClassLoader/单例相同。
- 本地 `XpRecord.kt` 延迟排队后仍逐事件调用 Provider，不是真正合并成一次批量事务；`XProvider.kt` 负责事件和统计数据库，初始化时清表。当前并没有跨系统进程重建保留全部统计的保证。
- 已观察到官方 Vector 冷启动可以读取已发布规则，而持续运行时通知不更新；实验补丁仅作根因证据，已经撤销。

## 推荐候选：存储与即时传递分开

1. 规则仍从应用 Room 生成同一个完整快照。先成功写入官方远程持久化配置，再向系统侧直接推送该快照。
2. 系统侧验证调用者、用户、协议版本、大小和规则有效性后，一次切换不可变快照，并确认生效版本。错误或较旧消息不覆盖当前规则。重复请求应幂等。
3. 官方配置通知和直推最终都进入同一个规则接收入口，禁止旧缓存回调或后续 installReader 覆盖较新的直推状态。版本机制还需覆盖导入、应用数据重置和多用户，不能只假设墙钟永远递增。
4. Hook 每次决策只读取一次内存快照，不做 Binder、文件读取或数据库操作。应用关闭后系统侧继续执行。冷启动通过官方配置恢复；尚未可读时需要有界重试和明确状态。
5. 最小实验先复用 SettingsProvider 自定义调用传送受限大小的快照并返回 ACK。大数据应通过文件描述符或分页处理，不能无限塞入 Bundle。

## 统计路径可独立改进

确认同进程且同模块运行时后，Hook 事件直接进入有界、按序的记录队列，后台串行聚合及批量写入。只有 UI 查询/清空统计需要跨进程。不能把数据库操作移到系统锁内，也不能将失去详细事件的内存环形缓冲说成无损替代。

数据库归属、上下文和迁移需明确；本轮不把既有数据库搬到新路径，不承诺统计跨重启保留。若需要跨重启历史，必须另定持久化和保留策略。

## 后续可选的 Binder 简化

SettingsProvider 可仅作为发现入口，返回系统侧自有 Binder，然后由应用直接查询统计、更新规则和读取 ACK。Android 支持在 Bundle 中传递 Binder；这是我们基于 Android 构建的协议，并非 libxposed 新提供的任意消息总线。[Bundle 文档](https://developer.android.com/reference/android/os/Bundle)

这一步增加接口和重连工作，只有现有调用方案表现出实际复杂度/性能问题才值得做。不能先假设可以完全移除系统侧入口，也不应为发布 Binder 再增加需要用户安装的常驻 root 服务。

## 不优先采用

- 单纯换 Remote Files：仍需解决通知、原子发布、读写竞争与重启恢复，没有自动解决当前缺陷。
- 高频定时轮询：增加常态开销；重复读取缓存也未必取得新值。
- 用热重载传设置：违背官方接口用途且需要额外生命周期清理。
- 每个事件投递到应用 Provider/Service：可能不断拉起应用进程，与不常驻目标不符。

## 最小验证顺序

先验证官方框架下直推后的三项规则实际变化、ACK 与实际生效一致；再强制停止应用，确认拦截和统计继续；再重启且不打开应用，确认规则恢复。补测乱序/重复发布、非法调用、多用户和大快照。统计架构优化单独进行，避免与 NWL-18 混成一次全面重写。

本文件是调查后的候选设计，不是实现完成或设备验证通过的声明。
