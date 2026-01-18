# Wakelock Control

## Purpose
NoWakeLock 通过 Hook PowerManagerService 的 wakelock 获取与释放来记录活动，并根据用户配置选择性拦截。

## Requirements

### Requirement: Wakelock Event Recording
系统 MUST记录 wakelock 事件的 name、packageName、userId、开始/结束时间，以及是否被拦截。

#### Scenario: Allowed wakelock is recorded
- **WHEN** wakelock 被获取并随后释放
- **THEN** 事件时长会计入该 wakelock 的统计数据

#### Scenario: Blocked wakelock is recorded
- **WHEN** wakelock 获取被拦截
- **THEN** 记录一条 blocked 事件（无 release 时间）

### Requirement: Wakelock Blocking Rules
系统 MUST支持以下针对 wakelock 获取的拦截判定：
- 按 (name, packageName, userId) 全量拦截
- 按 (name, packageName, userId) 仅灭屏拦截
- 按 (name, packageName, userId) 最小间隔拦截（使用配置的 interval）
- 按 (packageName, userId) 正则拦截（使用配置的 patterns）

#### Scenario: Full block prevents acquisition
- **GIVEN** 目标 wakelock 已启用全量拦截
- **WHEN** 应用尝试获取该 wakelock
- **THEN** 获取被阻止且事件标记为 blocked

#### Scenario: Screen-off-only block applies only when screen is off
- **GIVEN** 目标 wakelock 已启用仅灭屏拦截
- **WHEN** 在灭屏状态下获取该 wakelock
- **THEN** 获取被阻止

#### Scenario: Minimum interval blocks rapid repeats
- **GIVEN** 目标 wakelock 配置了最小间隔
- **WHEN** 在该间隔内再次获取该 wakelock
- **THEN** 第二次获取被阻止

#### Scenario: Regex rule blocks matching names
- **GIVEN** 该应用配置了正则 pattern
- **WHEN** wakelock 名称匹配该 pattern
- **THEN** 获取被阻止
