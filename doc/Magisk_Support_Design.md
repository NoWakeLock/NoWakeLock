# NoWakeLock Magisk支持方案设计文档

**文档日期**: 2024-11-07  
**文档版本**: 1.0  
**状态**: 研究阶段

## 1. 概述

本文档总结了为NoWakeLock应用添加Magisk支持的研究讨论结果。目前，NoWakeLock使用Xposed框架实现系统级Wakelock、Service和Alarm控制。添加Magisk支持将提供另一种实现方式，扩大应用的兼容性和使用场景。

## 2. 现有实现分析

### 2.1 Xposed实现架构

NoWakeLock当前通过Xposed框架实现以下功能：

- **WakelockHook**: 拦截PowerManagerService中的acquireWakeLockInternal和releaseWakeLockInternal方法
- **ServiceHook**: 拦截ActiveServices中的startServiceLocked和bindServiceLocked方法
- **AlarmHook**: 拦截AlarmManagerService相关方法
- **SettingsProviderHook**: 拦截SettingsProvider实现数据库功能

### 2.2 关键组件

- **XpNSP**: 使用XSharedPreferences读取用户配置，决定是否拦截系统操作
- **XpRecord**: 通过ContentProvider与主应用通信，记录事件数据
- **动态版本适应**: 实现了复杂的参数位置策略，适应不同Android版本

## 3. 核心技术挑战

在添加Magisk支持时，需要解决两个主要挑战：

### 3.1 配置数据读取

**挑战描述**: 
- 当前使用XSharedPreferences读取用户配置
- Magisk环境中无法使用XSharedPreferences
- 配置具有"高读取频率、低写入频率"的特性
- 需要一个文件持久化的替代方案

### 3.2 数据库功能实现

**挑战描述**:
- 当前通过Hook SettingsProvider实现数据库功能
- Magisk模块需要替代此机制
- 应用不常驻后台，需要无依赖的数据记录方式

## 4. 解决方案设计

### 4.1 配置读取替代方案

**方案概述**: 创建系统级配置文件，实现类似XSharedPreferences的功能

**实现要点**:
1. 在系统分区创建JSON格式配置文件 (例如`/data/system/nowakelock/config.json`)
2. 设置适当文件权限，确保系统模块和应用可访问
3. 实现基于内存的缓存机制，减少I/O操作
4. 定期检查文件修改时间戳，仅在配置变更时重新加载

**优势**:
- 与XSharedPreferences工作方式接近
- 保持文件持久化特性
- 高读取/低写入场景下性能良好
- 实现复杂度相对较低

### 4.2 数据库功能替代方案

**方案概述**: 在Magisk模块中实现类似当前SettingsProvider Hook的数据库功能

**实现要点**:
1. Magisk模块修改系统文件，实现与当前Hook相同的功能
2. 在系统空间创建和维护数据库
3. 保持相同的数据库结构和API
4. 复用现有数据操作逻辑

**优势**:
- 保持与现有实现的一致性
- 无需应用常驻后台
- 数据结构和处理逻辑可以保持不变

## 5. 架构设计

### 5.1 统一抽象层

为确保Xposed和Magisk实现共享相同的业务逻辑，需要创建以下抽象层:

**配置提供者接口**:
```
interface ConfigProvider {
    fun getBoolean(key: String, default: Boolean): Boolean
    fun getString(key: String, default: String): String
    fun getLong(key: String, default: Long): Long
    fun getSet(key: String): Set<String>
    // ...其他方法
}
```

**配置实现类**:
- `XposedConfigProvider`: 使用XSharedPreferences实现
- `MagiskConfigProvider`: 使用文件系统实现

**数据记录接口**:
```
interface EventRecorder {
    fun newEvent(name: String, packageName: String, type: Type, ...)
    fun endEvent(name: String, packageName: String, type: Type, ...)
    fun blockEvent(name: String, packageName: String, type: Type, ...)
    // ...其他方法
}
```

**数据记录实现类**:
- `XposedEventRecorder`: 通过ContentProvider与应用通信
- `MagiskEventRecorder`: 通过系统数据库实现

### 5.2 文件系统实现细节

**配置文件结构**:
- JSON格式，与当前XSharedPreferences存储格式兼容
- 包含所有用户配置项(屏蔽规则、正则表达式、时间间隔等)

**读取实现**:
- 启动时读取文件到内存
- 使用固定时间间隔(如30秒)检查文件修改
- 仅在文件变更时重新加载

**写入实现**:
- 应用修改配置时完整写入文件
- 使用临时文件写入后重命名，确保原子操作
- 实现简单的文件锁机制避免并发写入冲突

### 5.3 Magisk模块实现要点

**系统文件修改**:
- 修改PowerManagerService类的关键方法
- 修改SettingsProvider相关类
- 注入自定义代码实现拦截和数据记录

**权限处理**:
- 确保模块有适当的SELinux上下文
- 设置适当的文件和目录权限
- 处理可能的权限降级问题

## 6. 实现策略

### 6.1 模块化实现

1. **隔离变化点**:
   - 仅修改配置访问和数据库实现部分
   - 保持核心业务逻辑不变

2. **提取通用组件**:
   - 将XpNSP中的业务逻辑提取为通用组件
   - 建立适配器模式连接不同的底层实现

3. **统一数据格式**:
   - 确保配置文件格式和数据库结构一致
   - 允许用户无缝切换两种模式

### 6.2 实施路径

推荐采用以下阶段性实施路径:

1. **基础设施阶段**:
   - 创建ConfigProvider抽象和实现
   - 实现基本的Magisk模块文件修改功能

2. **功能整合阶段**:
   - 将配置逻辑从XSharedPreferences迁移到通用接口
   - 实现Magisk版本的数据库功能

3. **完善与优化阶段**:
   - 处理版本兼容性问题
   - 优化性能和资源使用
   - 完善用户体验和模式切换

## 7. 技术考量

### 7.1 性能考量

- **配置文件缓存**: 减少文件I/O对性能的影响
- **内存使用**: 确保不会引入过多内存开销
- **并发处理**: 处理多进程/多线程并发访问

### 7.2 兼容性考量

- **Android版本适应**: 确保兼容各种Android版本
- **设备兼容性**: 适应不同设备和ROM
- **升级策略**: 处理系统升级导致的兼容性问题

### 7.3 安全考量

- **文件权限**: 确保配置文件安全
- **数据保护**: 保护用户数据不被未授权访问
- **稳定性**: 防止模块导致系统不稳定

## 8. 结论

添加Magisk支持是NoWakeLock项目的可行扩展方向。通过创建系统配置文件和实现系统级数据库功能，可以在Magisk环境中实现与Xposed相同的功能，同时保持代码和用户体验的一致性。

此方案避免了过度设计，聚焦于解决两个核心问题，提供了一个实用且高效的实施路径。通过适当的抽象和接口设计，可以最大限度地重用现有代码，降低开发和维护成本。

---

**备注**: 本文档基于研究阶段的讨论，具体实现细节可能需要在开发过程中进一步调整。 