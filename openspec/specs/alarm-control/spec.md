# Alarm Control

## Purpose
NoWakeLock 通过 Hook Android 的 alarm 服务来观测 alarm 触发，并根据用户配置选择性拦截。

## Requirements

### Requirement: Alarm Trigger Recording
系统 MUST记录 alarm 触发事件的 name、packageName、userId，以及该触发是否被拦截。

#### Scenario: Allowed alarm is recorded
- **WHEN** alarm 被触发
- **THEN** 该触发会被记录用于统计

#### Scenario: Blocked alarm is recorded
- **WHEN** alarm 触发被拦截
- **THEN** 记录一条 blocked 事件

### Requirement: Alarm Blocking Rules
系统 MUST支持以下针对 alarm 触发的拦截判定：
- 按 (name, packageName, userId) 全量拦截
- 按 (name, packageName, userId) 仅灭屏拦截
- 按 (name, packageName, userId) 最小间隔拦截（使用配置的 interval）
- 按 (packageName, userId) 正则拦截（使用配置的 patterns）

#### Scenario: Full block prevents trigger delivery
- **GIVEN** 目标 alarm 已启用全量拦截
- **WHEN** 该 alarm 将被投递
- **THEN** 该 alarm 会从触发列表中移除并标记为 blocked

#### Scenario: Screen-off-only block applies only when screen is off
- **GIVEN** 目标 alarm 已启用仅灭屏拦截
- **WHEN** 在灭屏状态下该 alarm 将被触发
- **THEN** 该 alarm 不会被投递

#### Scenario: Minimum interval blocks rapid repeats
- **GIVEN** 目标 alarm 配置了最小间隔
- **WHEN** 在该间隔内该 alarm 再次触发
- **THEN** 第二次触发被拦截

#### Scenario: Regex rule blocks matching names
- **GIVEN** 该应用配置了正则 pattern
- **WHEN** alarm 名称匹配该 pattern
- **THEN** 该触发被拦截
