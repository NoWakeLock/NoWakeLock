# Alarm 管理

Alarm（定时任务）是 Android 系统的定时器机制，用于在特定时间触发操作，频繁的 Alarm 会影响设备续航。

!!! danger "⚠️ 救援模式 - 重要提醒！"
    **如果错误配置 WakeLock 导致设备无法启动**：
    
    **情况1：LSPosed 框架问题（安装后未配置就遇到问题）**：
    1. 长按电源键10秒强制重启
    2. 屏幕变黑后立即反复按任意硬件按键
    3. 感受到2次短震动后，继续快速按4次相同按键
    4. 第4次按键后感受到长震动，表示 LSPosed 已禁用
    5. 正常启动后在 LSPosed 中禁用 NoWakeLock 模块
    
    **情况2：误操作配置问题（可进入 Recovery）**：
    1. 进入 Recovery → 文件管理
    2. 导航到 /data/misc/xxx-xxx-xxx/prefs/com.js.nowakelock
       （xxx-xxx-xxx 是一段很长的随机字符串，每个设备可能都不同）
    3. 删除整个文件夹
    4. 重启设备
    
    **不确定原因时**：直接清除 NoWakeLock 应用数据，重新配置时避免拦截系统关键组件。

## 功能概述

### Alarm 作用
- 定时执行任务
- 周期性操作触发
- 系统级别的定时器
- 应用保活机制

### 管理目标
- 监控 Alarm 设置和触发
- 识别过度频繁的定时任务
- 控制 Alarm 的触发频率
- 减少不必要的唤醒

## 界面说明

### Alarm 列表

【需要截图：Alarm 列表页面】

**列表信息**：
- **标签** - Alarm 标识符
- **应用** - 来源包名
- **类型** - Alarm 类型图标
- **状态** - 拦截状态
- **统计** - 触发次数和时间信息

### 状态显示

| 状态 | 图标 | 说明 |
|------|------|------|
| 允许 | 🟢 | 正常触发 |
| 限制 | 🟡 | 降低触发频率 |
| 拦截 | 🔴 | 阻止触发 |
| 待触发 | ⏰ | 已设置等待触发 |

## Alarm 类型

### 按触发条件分类

| 类型 | 说明 | 典型用途 |
|------|------|----------|
| RTC | 绝对时间触发 | 闹钟、提醒 |
| RTC_WAKEUP | 绝对时间唤醒设备 | 重要通知 |
| ELAPSED_REALTIME | 相对时间触发 | 定期检查 |
| ELAPSED_REALTIME_WAKEUP | 相对时间唤醒 | 后台任务 |

### 按重复模式分类

**单次 Alarm**：
- 执行一次后自动取消
- 用于特定时间的任务

**重复 Alarm**：
- 按固定间隔重复触发
- 常见于同步、更新任务

**精确 Alarm**：
- 准确的时间触发
- 系统资源消耗较高

## 配置选项

### 处理模式

#### 允许模式
- Alarm 正常设置和触发
- 不进行任何干预
- 适用于重要系统功能

#### 限制模式
- 降低触发频率
- 合并相近的触发时间
- 延迟非紧急 Alarm

#### 拦截模式
- 完全阻止 Alarm 设置
- 应用无法创建该类型 Alarm
- 可能严重影响应用功能

### 高级选项

**智能合并**：
- 将相近时间的 Alarm 合并
- 减少设备唤醒次数

**批处理模式**：
- 延迟非紧急 Alarm
- 与其他任务一起执行

## 使用方法

### 查看 Alarm 列表

1. 点击底部"Alarms"标签
2. 查看当前活动的 Alarm
3. 使用筛选器查看特定状态

### 配置 Alarm 规则

1. 点击目标 Alarm 项目
2. 选择处理模式
3. 设置具体参数：
   - 最小间隔时间
   - 延迟时间
   - 批处理选项

【需要截图：Alarm 配置页面】

### 批量管理

**按应用批量设置**：
1. 筛选特定应用的 Alarm
2. 选择批量操作
3. 应用统一规则

**按类型批量设置**：
- 所有 WAKEUP 类型限制
- 所有重复 Alarm 降频
- 系统 Alarm 谨慎处理

## 实际应用

### 问题识别

#### 异常 Alarm 特征

**高频触发**：
- 间隔小于 1 分钟的重复 Alarm
- 深夜时段频繁触发
- 设备静止时仍在运行

## 技术实现

### Hook 机制

拦截 AlarmManagerService 的关键方法：
```kotlin
// 系统 Alarm 设置调用
setImpl(
    int type,
    long triggerAtTime,
    long windowLength,
    long interval,
    PendingIntent operation,
    IAlarmListener directReceiver,
    String listenerTag,
    WorkSource workSource,
    AlarmManager.AlarmClockInfo alarmClock,
    int callingUid,
    String callingPackage
)

// Alarm 触发处理
triggerAlarmsLocked(ArrayList<Alarm> triggerList)
```

### 数据处理

**实时处理**：
- Alarm 设置时的规则检查
- 触发时的频率控制
- 动态调整触发时间

**历史记录**：
- 数据库存储触发历史
- 统计分析和趋势计算
- 自动清理过期数据

### 兼容性

**版本支持**：
- Android 7.0+ 完整支持
- 不同版本的 API 适配
- 降级兼容策略

**性能优化**：
- 最小化 Hook 开销
- 高效的规则匹配算法
- 异步处理统计数据

## 相关功能

- [应用管理](app-management.md) - 查看应用的所有 Alarm
- [WakeLock 管理](wakelocks.md) - 配合 WakeLock 优化
- [规则系统](rules-regex.md) - 使用正则表达式批量配置

!!! tip "优化建议"
    Alarm 优化效果明显，但需要平衡功能性。建议从非关键应用开始，逐步调整重要应用的设置。

!!! warning "注意事项"
    过度限制系统 Alarm 可能影响设备正常功能，如自动时间同步、系统更新检查等。