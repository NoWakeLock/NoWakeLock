# Configuration Management

## Purpose
配置数据保存在本地，并按 Android userId 做隔离。运行时 Hook 侧通过可读的
SharedPreferences 读取规则开关与参数。

## Requirements

### Requirement: Per-Item Rules Are Persisted and Synced
应用 MUST持久化按条目的规则设置（全量拦截、仅灭屏拦截、时间窗口/最小间隔），并保持
对应的 SharedPreferences key 与 Xposed 侧读取一致。

#### Scenario: User updates a rule
- **WHEN** 用户在 UI 中修改某条规则
- **THEN** 规则被保存，且 SharedPreferences key 被更新

### Requirement: App-Level Settings Are Persisted
应用 MUST持久化按应用维度的设置，包括各域开关与正则 pattern 集合，并以 packageName +
userId 作为作用域。

#### Scenario: User updates app settings
- **WHEN** 用户更新某应用的应用级设置
- **THEN** 设置会保存到该 packageName 与 userId 下

### Requirement: Backup and Restore
应用 MUST支持通过系统文件选择器导出/导入 JSON 格式的配置备份。

#### Scenario: Export configuration
- **WHEN** 用户创建备份
- **THEN** 写出一个包含应用设置与规则设置的 JSON 文档

#### Scenario: Import configuration
- **WHEN** 用户从备份 JSON 文档恢复
- **THEN** 备份中的应用设置与规则设置会被应用到本地

### Requirement: Multi-User Scoping
应用 MUST按 Android userId 隔离设置，并允许在 UI 中切换当前用户上下文。

#### Scenario: Switching users changes visible data
- **WHEN** 用户选择了不同的 userId
- **THEN** 应用列表与设置内容随之切换为该 userId
