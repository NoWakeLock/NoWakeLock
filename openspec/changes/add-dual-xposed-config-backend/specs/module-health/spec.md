## RENAMED Requirements
- FROM: `### Requirement: Shared Preferences Path Validation`
- TO: `### Requirement: Configuration Backend Availability Validation`

## MODIFIED Requirements
### Requirement: Configuration Backend Availability Validation
应用 MUST校验 Hook 侧实际可用的配置后端，而不是仅校验某个猜测的 SharedPreferences 文件系统路径。配置后端检查 MUST区分 legacy XSharedPreferences 与 Modern Remote Preferences 的可读状态，并保持二者为 generation-specific 状态。

#### Scenario: Legacy backend is readable
- **WHEN** 执行模块检查且 legacy XSharedPreferences 可读
- **THEN** 配置后端检查通过，并报告 legacy backend 可用

#### Scenario: Modern backend is readable
- **WHEN** 执行模块检查且 Modern Remote Preferences 可读
- **THEN** 配置后端检查通过，并报告 remote backend 可用

#### Scenario: Modern backend is unavailable
- **WHEN** Modern 入口已加载但 Remote Preferences 不可读
- **THEN** 配置后端检查报告 remote backend 不可用，且不会把 legacy backend 当作 Modern fallback

#### Scenario: No backend is readable
- **WHEN** legacy XSharedPreferences 与 Modern Remote Preferences 均不可读
- **THEN** 总体状态为 error，并显示配置后端不可用的排查信息

## ADDED Requirements
### Requirement: Configuration Backend Status Reporting
应用 MUST在模块检查结果中报告配置后端类型、可读状态，以及可获得的最近发布或读取状态，供 UI 区分 legacy、remote 与不可用状态。

#### Scenario: Multiple backends are available
- **WHEN** legacy 与 remote 配置后端都可用
- **THEN** UI 显示配置后端正常，并可区分 legacy 与 remote 后端类型

#### Scenario: Only legacy backend is available
- **WHEN** 模块处于激活状态且只有 legacy 配置后端可读
- **THEN** UI 显示模块可用，并将配置后端状态标记为 legacy-only 或等价提示

### Requirement: Overall Status Uses Backend Availability
应用 MUST使用模块激活状态、Hook 观测状态与配置后端可用性共同计算总体状态。模块已激活且至少一个配置后端可读时，不得因为无法定位 `/data/misc` 路径而直接报 error。

#### Scenario: Module active with readable backend
- **WHEN** 模块已激活且至少一个配置后端可读
- **THEN** 总体状态不会因为旧的路径扫描失败而变为 error

#### Scenario: Module inactive
- **WHEN** 模块未激活
- **THEN** 总体状态为 error，即使本地应用侧存在配置数据

### Requirement: Evidence-Based Synchronization And Hook Diagnostics
模块检查 MUST区分应用发布成功、Hook 进程实际读取 revision、各方法安装结果及历史事件记录；空结果或失败结果 MUST结束加载状态。API102 框架运行目标信息仅作为诊断证据，不能替代端到端设备验证。

#### Scenario: Pending or outdated reader revision
- **WHEN** 最新配置尚未发布成功，或 Hook 读取 revision 与应用已发布 revision 不一致
- **THEN** UI 显示待同步或警告，不报告配置已经同步

#### Scenario: Method installation fails and retries
- **WHEN** 某个实际方法的 Hook 安装失败后再次触发安装
- **THEN** 该方法可重试，已成功的相同方法不会重复安装，结果按方法与类加载器隔离

#### Scenario: API102 device unavailable
- **WHEN** 主机检查通过但尚无用户提供的 API102 设备
- **THEN** 候选版本与任务记录明确保留 API102 设备验收待完成
