# WakeLock 管理

WakeLock（唤醒锁）阻止设备进入休眠状态，是 Android 系统中影响电池续航的关键机制。

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

## WakeLock 类型

### 主要类型

| 类型 | 常量值 | 说明 | 电量影响 |
|------|--------|------|----------|
| PARTIAL_WAKE_LOCK | 1 | 保持 CPU 运行 | 高 |
| SCREEN_DIM_WAKE_LOCK | 6 | 屏幕变暗但不关闭 (已废弃) | 中等 |
| SCREEN_BRIGHT_WAKE_LOCK | 10 | 屏幕保持亮度 (已废弃) | 高 |
| FULL_WAKE_LOCK | 26 | CPU + 屏幕全亮 (已废弃) | 极高 |
| PROXIMITY_SCREEN_OFF_WAKE_LOCK | 32 | 接近传感器控制 | 低 |

!!! warning "类型说明"
    除 PARTIAL_WAKE_LOCK 和 PROXIMITY_SCREEN_OFF_WAKE_LOCK 外，其他类型在 Android API 17 后已被废弃。现代应用主要使用 PARTIAL_WAKE_LOCK。

### 特殊标识

**系统 WakeLock**：
- `PowerManagerService.WakeLocks`
- `AlarmManager`
- `AudioMix`

**网络相关**：
- `WifiManager`
- `ConnectivityService`

**定位服务**：
- `LocationManagerService`
- `GpsLocationProvider`

## 界面说明

### WakeLock 列表

【需要截图：WakeLock 列表页面】

**列表信息**：
- **名称** - WakeLock 标识符
- **应用** - 来源包名
- **类型** - WakeLock 类型图标
- **状态** - 当前状态指示器
- **统计** - 获取次数和累计时长

### 状态指示器

| 状态 | 图标 | 说明 |
|------|------|------|
| 允许 | 🟢 | 正常运行，不做限制 |
| 限制 | 🟡 | 设置了超时时间 |
| 拦截 | 🔴 | 完全阻止获取 |
| 活跃 | ⚡ | 当前正在持有 |

### 筛选和排序

**筛选选项**：
- 全部
- 允许
- 限制  
- 拦截
- 当前活跃

**排序方式**：
- 按名称
- 按应用
- 按获取次数
- 按累计时长
- 按最后活动时间

## 配置选项

### 处理模式

#### 允许模式
- 不进行任何限制
- WakeLock 正常获取和释放
- 默认模式，适用于大部分情况

#### 限制模式
- 设置最大持有时间
- 超时后强制释放
- 仅在通过 BBS 确认某个 WakeLock 持续时间过长时使用

!!! warning "超时设置原则"
    超时时间必须根据 BBS 分析的实际数据确定，而不是使用预设值。观察该 WakeLock 的正常持续时间，然后设置稍大于正常值的超时。

#### 拦截模式
- 完全阻止 WakeLock 获取
- 应用无法持有该 WakeLock
- 仅在确认该 WakeLock 完全不必要且严重影响电池时使用

## 使用方法

### 基本操作流程

!!! warning "重要：配置前必读"
    1. **先诊断，后配置** - 使用 BBS 确认问题后再进行配置
    2. **单个处理** - 针对具体问题逐个配置，避免批量操作
    3. **持续监控** - 配置后继续使用 BBS 验证效果

### 查看和分析 WakeLock

1. 点击底部"Wakelocks"标签
2. 浏览当前列表和统计数据
3. 结合 BBS 数据分析异常项目

### 针对性配置

1. **确认问题** - 基于 BBS 分析结果
2. **点击目标 WakeLock** 进入配置页面
3. **选择最小干预** - 优先选择限制模式
4. **设置参数** - 基于实际观察到的数据
5. **验证功能** - 确认应用功能正常

!!! danger "禁止批量配置"
    不要使用批量操作功能进行预设配置。每个 WakeLock 的问题都是具体的，需要个别分析和处理。

## 效果验证

### 配置后监控

**必要步骤**：
1. **继续数据监控** 配置后的效果
   - 优先使用 BBS 进行全面评估
   - 也可使用 NoWakeLock 内置统计作为参考
2. **验证应用功能** - 确认应用的所有功能仍然正常
3. **评估电池改善** - 对比配置前后的实际电池消耗

**回滚准备**：
- 如果应用功能受影响，立即取消配置
- 如果电池改善不明显，重新评估是否有必要限制

## 技术细节

### Hook 实现

NoWakeLock 拦截 PowerManagerService 中的关键方法：

```kotlin
// 主要 Hook 方法 (参数因 Android 版本而异)
acquireWakeLockInternal(...)
releaseWakeLockInternal(...)
```

**版本兼容性处理**：
- 使用参数位置缓存机制
- 支持 Android 7.0-15.0 的不同方法签名
- 自动检测和适配参数位置

### 兼容性处理

**版本适配**：
- 支持 Android 7.0-15.0
- 动态参数位置检测
- 降级策略处理

**性能优化**：
- Hook 调用开销 < 1ms
- 规则匹配使用缓存
- 异步处理统计数据

### 数据存储

**实时数据**：
- 当前活跃的 WakeLock
- 内存中缓存，重启后清除

**会话统计**：
- 当前会话的 WakeLock 活动记录
- 数据库临时存储，设备重启后清空

## 相关功能

- [应用管理](app-management.md) - 按应用查看所有 WakeLock
- [规则系统](rules-regex.md) - 使用正则表达式批量配置

!!! warning "使用建议"
    修改系统关键服务的 WakeLock 可能影响设备稳定性。建议从第三方应用开始，逐步调整系统服务。