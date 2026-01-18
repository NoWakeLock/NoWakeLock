# Module Health

## Purpose
NoWakeLock 依赖 Xposed/LSPosed 对 Android 系统服务进行 Hook。应用提供“模块检查”
页面，用于验证安装状态、作用域配置与基础运行健康度。

## Requirements

### Requirement: Module Active Status
应用 MUST报告 NoWakeLock 的 Hook 是否在系统进程中处于激活状态。

#### Scenario: Module is active
- **WHEN** 用户打开模块检查页面
- **THEN** UI 显示模块已激活

#### Scenario: Module is inactive
- **WHEN** 模块未加载或未配置到正确作用域
- **THEN** UI 显示模块未激活，并提供排查/修复提示

### Requirement: Hook Effectiveness Checks
应用 MUST针对每个域 Hook（Wakelock/Alarm/Service）报告在当前开机周期内是否观测到数据。

#### Scenario: Hook has observed data
- **WHEN** 某个域至少记录过一条事件
- **THEN** 该域显示为 OK

#### Scenario: Hook has not observed data
- **WHEN** 某个域没有记录任何事件
- **THEN** 该域显示为 warning

### Requirement: Shared Preferences Path Validation
应用 MUST校验 Xposed 侧读取配置所依赖的 SharedPreferences 路径是否存在且可读。

#### Scenario: Config path exists
- **WHEN** 执行模块检查
- **THEN** 配置路径检查通过

#### Scenario: Config path missing
- **WHEN** 无法定位或读取偏好路径
- **THEN** 总体状态为 error

### Requirement: Per-Boot Initialization
应用 MUST按开机周期执行初始化，避免跨重启复用上一次开机的状态。

#### Scenario: First launch after reboot
- **WHEN** 设备重启后首次启动应用
- **THEN** 与开机周期相关的表与标记仅在该次开机内重置一次
