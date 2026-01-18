# Service Control

## Purpose
NoWakeLock 通过 Hook 系统 service 启动请求来观测服务启动，并根据用户配置选择性阻止启动。

## Requirements

### Requirement: Service Start Recording
系统 MUST记录服务启动事件的 service component name、packageName、userId，以及该启动是否被拦截。

#### Scenario: Allowed service start is recorded
- **WHEN** 服务被启动
- **THEN** 该启动会被记录用于统计

#### Scenario: Blocked service start is recorded
- **WHEN** 服务启动被拦截
- **THEN** 记录一条 blocked 事件

### Requirement: Service Blocking
系统 MUST允许用户按 service component name 与 calling package 对特定服务启动进行拦截。

#### Scenario: Block prevents service start
- **GIVEN** 目标 service component 已启用拦截
- **WHEN** 应用尝试启动该服务
- **THEN** 启动被阻止且事件标记为 blocked
