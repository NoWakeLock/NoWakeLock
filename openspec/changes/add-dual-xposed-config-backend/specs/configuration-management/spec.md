## MODIFIED Requirements
### Requirement: Per-Item Rules Are Persisted and Synced
应用 MUST持久化按条目的规则设置（全量拦截、仅灭屏拦截、时间窗口/最小间隔），并将对应配置发布到所有可用的 Xposed 配置后端，使 Hook 侧读取的 key 与应用侧保存的规则保持一致。

#### Scenario: User updates a rule
- **WHEN** 用户在 UI 中修改某条规则
- **THEN** 规则被保存到本地数据源，且 legacy SharedPreferences 与可用的 Remote Preferences 使用相同 key 更新

#### Scenario: Modern backend unavailable
- **WHEN** 用户修改规则但 Modern Remote Preferences 服务不可用
- **THEN** 应用仍会更新 legacy SharedPreferences，且不会因为 Remote Preferences 不可用而丢失本地规则

### Requirement: App-Level Settings Are Persisted
应用 MUST持久化按应用维度的设置，包括各域开关与正则 pattern 集合，并以 packageName + userId 作为作用域发布到可用的 Xposed 配置后端。

#### Scenario: User updates app settings
- **WHEN** 用户更新某应用的应用级设置
- **THEN** 设置会保存到该 packageName 与 userId 下，并发布对应的 hook 读取 key

#### Scenario: Regex settings are published
- **WHEN** 用户更新某应用某个控制域的正则 pattern 集合
- **THEN** 应用发布 `${type}_${packageName}_${userId}_rE` 配置，使 Hook 侧正则拦截读取到最新集合

### Requirement: Backup and Restore
应用 MUST支持通过系统文件选择器导出/导入 JSON 格式的配置备份，并在导入后将恢复的配置发布到可用的 Xposed 配置后端。

#### Scenario: Export configuration
- **WHEN** 用户创建备份
- **THEN** 写出一个包含应用设置与规则设置的 JSON 文档

#### Scenario: Import configuration
- **WHEN** 用户从备份 JSON 文档恢复
- **THEN** 备份中的应用设置与规则设置会被应用到本地，并重新发布到可用配置后端

## ADDED Requirements
### Requirement: Generation-Specific Xposed Configuration Backends
应用 MUST通过同一个 APK 支持既有 legacy XSharedPreferences 路径与 Remote Preferences 路径。纯 legacy 入口保留原行为；LSPosed 1.11.0 的 API100 入口桥及 API102 入口 MUST使用 Remote Preferences，不得把不可读的 legacy 文件当作回退来源。

#### Scenario: Legacy framework reads configuration
- **GIVEN** 设备上的 Xposed 框架只支持 legacy XSharedPreferences
- **WHEN** Hook 进程读取 NoWakeLock 配置
- **THEN** Hook 进程从 legacy SharedPreferences 读取规则并继续按现有行为工作

#### Scenario: Modern framework reads configuration
- **GIVEN** 设备上的 Xposed 框架支持 Modern Remote Preferences
- **WHEN** Hook 进程读取 NoWakeLock 配置
- **THEN** Hook 进程从 Remote Preferences 读取规则，且不要求 NoWakeLock 应用进程保持运行

#### Scenario: Modern remote backend misses a key
- **GIVEN** Remote Preferences 可读但缺少某个规则 key，且 legacy XSharedPreferences 包含该 key
- **WHEN** Hook 进程读取该规则
- **THEN** Modern Hook 进程使用保守默认值，且不会读取 legacy XSharedPreferences

### Requirement: Configuration Republish From Source Of Truth
应用 MUST将 Room 中的规则和应用级设置视为配置事实来源，并在配置后端变为可用或数据恢复后重新发布配置。

#### Scenario: App starts after upgrade
- **WHEN** 用户从旧版本升级后首次启动应用
- **THEN** 应用从本地数据库重新发布当前规则到可用配置后端

#### Scenario: Remote service binds after app start
- **WHEN** Modern Remote Preferences 服务在应用启动后绑定成功
- **THEN** 应用向 Remote Preferences 发布当前完整配置快照

### Requirement: Single APK Xposed API Compatibility
应用 MUST通过同一个 APK 保留既有 legacy 行为，并支持已固定的 LSPosed 1.11.0 与官方稳定 API102 两个目标。API101 等过渡版本不属于支持范围。应用 MUST NOT通过多 APK、product flavor 或不同 applicationId 解决兼容问题。

#### Scenario: Fixed old runtime selects modern metadata
- **GIVEN** LSPosed 1.11.0 优先读取 Modern metadata 且要求 `(XposedInterface, ModuleLoadedParam)` 构造器
- **WHEN** runtime 加载单 APK
- **THEN** 窄入口桥通过旧构造器与 onSystemServerLoaded 分派到原有 legacy Hook 业务，并使用旧服务的 Remote Preferences
- **AND** 不依赖 minApiVersion 自动阻止加载或自动回退 legacy metadata

#### Scenario: API102 runtime loads modern entry
- **GIVEN** Xposed runtime 使用 API102 的无参数 `XposedModule` 构造方式
- **WHEN** runtime 加载 NoWakeLock Modern 入口
- **THEN** 入口可被实例化，并在 Remote Preferences capability 可用时安装 remote-only 配置 reader

### Requirement: Pure Modern Hook Path
Modern API102 分支 MUST使用 libxposed hook-chain API 安装 system_server 与 SettingsProvider hook，不得调用或加载 `de.robv.android.xposed` legacy hook API。联合入口的编译占位 API 类 MUST NOT打包进 APK。

#### Scenario: Modern runtime installs system hooks
- **GIVEN** API102 runtime 调用 `onSystemServerStarting` 且提供系统 Hook 能力
- **WHEN** NoWakeLock 安装 wakelock、alarm、service hook
- **THEN** hook 通过 `XposedInterface.hook(...).intercept(...)` 安装，且 Modern 入口类加载链不引用 legacy XposedBridge API

#### Scenario: Modern runtime handles module health route
- **GIVEN** API102 runtime 加载 SettingsProvider package
- **WHEN** NoWakeLock 安装模块检查路由
- **THEN** SettingsProvider hook 通过 libxposed hook chain 调用 `XProvider`，而不是通过 `XposedBridge.hookMethod`

### Requirement: Serialized Configuration Snapshots
应用 MUST串行发布从本地事实来源读取的完整配置快照，以单次后端提交同时更新 revision、现有规则及删除已移除的自有规则。发布失败 MUST保留本地数据并报告待同步。

#### Scenario: Queued publications and rule removal
- **WHEN** 多个配置修改排队且某个旧规则已删除
- **THEN** 每次发布取得锁后重新读取当前数据，清除后端中过期的自有 key，保留无关 key，并赋予递增 revision

#### Scenario: Debug configuration without framework
- **WHEN** 框架服务不可用时用户修改 debug
- **THEN** debug 保存于应用私有存储，重启后仍可恢复，服务重新连接后随完整快照发布
