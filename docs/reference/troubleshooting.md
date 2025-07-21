# 故障排除

系统性的问题诊断和解决指南。

## 诊断流程

### 第一步：基础检查

#### 模块状态验证
1. 打开 NoWakeLock → "模块检查"
2. 确认所有项目显示 ✅
3. 如有 ❌ 项目，按提示处理

【需要截图：模块检查失败示例】

#### Xposed 框架检查
```bash
# 检查 LSPosed 状态
adb shell am start -n org.lsposed.manager/.ui.activity.MainActivity

# 查看模块列表
adb shell pm list packages | grep nowakelock
```

#### 基础权限检查
- 存储权限
- 查询所有应用权限（Android 11+）
- 无障碍服务权限（如需要）

### 第二步：功能测试

#### WakeLock 测试
1. 设置一个简单的 WakeLock 限制规则
2. 打开对应应用触发 WakeLock
3. 检查统计页面是否有拦截记录

#### 规则生效测试
1. 创建测试规则：拦截特定 WakeLock
2. 观察目标应用行为变化
3. 查看事件日志确认规则执行

## 常见问题分类

### 安装问题

#### 模块无法加载
**症状**：模块检查显示"模块未加载"

**诊断步骤**：
1. 确认 Xposed 框架正常运行
2. 检查模块是否在管理器中勾选
3. 验证应用签名是否正确

**解决方案**：
```bash
# 重新安装模块
adb uninstall com.js.nowakelock
adb install nowakelock.apk

# 清除框架缓存
# 在 LSPosed 中："设置" → "清除缓存"
```

#### Hook 功能失效
**症状**：模块加载但 Hook 不工作

**可能原因**：
- 系统版本不兼容
- 作用域配置错误
- SELinux 策略限制

**解决方案**：
1. 确认作用域包含 `android`
2. 检查 SELinux 状态：
   ```bash
   adb shell getenforce
   # 如果是 Enforcing，可能影响 Hook 功能
   ```
3. 查看 Xposed 日志：
   ```bash
   adb logcat | grep -E "(Xposed|nowakelock)"
   ```

### 功能问题

#### 规则不生效
**症状**：设置规则后没有拦截效果

**检查清单**：
- [ ] 规则是否启用
- [ ] 匹配条件是否正确
- [ ] 目标应用是否重启
- [ ] 是否有冲突的规则

**调试方法**：
1. 使用简单的精确匹配测试
2. 检查规则优先级
3. 查看匹配日志

#### 应用功能异常
**症状**：设置规则后应用无法正常工作

**立即处理**：
1. 禁用相关规则
2. 重启问题应用
3. 逐步恢复规则

**根本解决**：
1. 分析应用依赖的关键组件
2. 调整规则范围或参数
3. 使用"限制"替代"拦截"

#### 统计数据异常
**症状**：统计数据显示异常或不更新

**检查项目**：
1. 数据库状态
   ```bash
   adb shell ls -la /data/data/com.js.nowakelock/databases/
   ```
2. 存储空间
   ```bash
   adb shell df /data
   ```
3. 应用权限

**修复方法**：
```bash
# 清除数据库（注意：会丢失历史数据）
adb shell pm clear com.js.nowakelock
```

### 性能问题

#### 系统卡顿
**症状**：安装 NoWakeLock 后系统响应变慢

**性能分析**：
```bash
# CPU 使用率
adb shell top | grep nowakelock

# 内存使用
adb shell dumpsys meminfo com.js.nowakelock
```

**优化方案**：
1. 减少规则数量
2. 简化正则表达式
3. 调整统计频率

#### 电量消耗增加
**症状**：模块本身消耗电量

**诊断方法**：
1. 检查后台活动
   ```bash
   adb shell dumpsys battery
   ```
2. 分析 WakeLock 使用
   ```bash
   adb shell dumpsys power | grep nowakelock
   ```

**解决方案**：
- 检查是否有异常的循环任务
- 优化数据库查询频率
- 确认没有内存泄漏

### 兼容性问题

#### 特定应用冲突
**症状**：某些应用与 NoWakeLock 冲突

**识别方法**：
1. 系统日志分析
2. 应用崩溃报告
3. ANR（Application Not Responding）日志

**处理策略**：
```yaml
临时解决:
  - 将应用加入白名单
  - 禁用相关规则

长期解决:
  - 分析冲突原因
  - 调整 Hook 策略
  - 更新兼容性代码
```

#### 系统版本兼容
**症状**：新版本 Android 上功能异常

**适配检查**：
1. API 变更分析
2. 权限模型变化
3. 安全策略更新

**降级方案**：
- 禁用不兼容的功能
- 使用替代实现
- 等待版本更新

## 日志分析

### 收集日志

#### 系统日志
```bash
# 完整日志
adb logcat -v time > full_log.txt

# NoWakeLock 相关
adb logcat | grep -i nowakelock > nowakelock_log.txt

# Xposed 相关
adb logcat | grep -i xposed > xposed_log.txt
```

#### 应用日志
```bash
# 特定进程日志
adb logcat --pid=$(adb shell pidof com.js.nowakelock)

# 崩溃日志
adb logcat | grep -E "(FATAL|AndroidRuntime)"
```

### 日志分析

#### 关键错误标识
```
E/Xposed: Hook failed
E/NoWakeLock: Database error
W/ActivityManager: Unable to start service
```

#### 性能问题标识
```
W/Choreographer: Skipped frames
I/Timeline: Timeline: Activity_idle
W/InputDispatcher: Application is not responding
```

### 日志清理
```bash
# 清除日志
adb logcat -c

# 设置日志级别
adb shell setprop log.tag.NoWakeLock VERBOSE
```

## 数据恢复

### 配置备份
```bash
# 备份配置
adb backup -f backup.ab com.js.nowakelock

# 提取数据库
adb shell cp /data/data/com.js.nowakelock/databases/app_database /sdcard/
adb pull /sdcard/app_database ./
```

### 配置恢复
```bash
# 恢复备份
adb restore backup.ab

# 手动恢复数据库
adb push ./app_database /sdcard/
adb shell cp /sdcard/app_database /data/data/com.js.nowakelock/databases/
```

### 重置选项

#### 软重置（保留配置）
1. 应用设置 → 清除缓存
2. 重启应用

#### 硬重置（清除所有数据）
```bash
adb shell pm clear com.js.nowakelock
```

#### 完全重置（重新安装）
```bash
adb uninstall com.js.nowakelock
# 重新安装和配置
```

## 高级调试

### Hook 调试

#### 启用详细日志
在应用设置中开启"调试模式"，会输出详细的 Hook 信息。

#### Hook 测试工具
```kotlin
// 测试特定 Hook 点
fun testWakeLockHook() {
    // 手动触发 WakeLock 获取
    // 观察 Hook 是否被调用
}
```

### 性能分析

#### CPU 分析
```bash
# 性能监控
adb shell am start -n com.android.shell/.BugreportStorageProvider

# 线程分析
adb shell ps -T | grep nowakelock
```

#### 内存分析
```bash
# 内存详情
adb shell dumpsys meminfo com.js.nowakelock

# 内存泄漏检测
adb shell am dumpheap com.js.nowakelock /sdcard/heap.hprof
```

### 数据库调试

#### 数据库检查
```sql
-- 连接数据库
sqlite3 app_database

-- 检查表结构
.schema

-- 查看数据
SELECT * FROM app_info LIMIT 10;
SELECT * FROM wakelock_info LIMIT 10;
```

#### 数据一致性检查
```sql
-- 检查孤立记录
SELECT * FROM events WHERE app_id NOT IN (SELECT id FROM apps);

-- 统计数据验证
SELECT package_name, COUNT(*) FROM events GROUP BY package_name;
```

## 预防措施

### 定期维护

#### 每周检查
- 模块状态验证
- 规则效果评估
- 性能指标监控

#### 每月维护
- 清理历史数据
- 更新规则配置
- 备份重要设置

### 监控设置

#### 性能监控
设置性能阈值，超出时自动告警：
- CPU 使用率 > 5%
- 内存使用 > 100MB
- 数据库大小 > 500MB

#### 功能监控
定期测试关键功能：
- 规则匹配准确性
- 统计数据完整性
- 应用功能正常性

## 专业支持

### 社区支持
- **Telegram**: [@nowakelock](https://t.me/nowakelock)
- **Discord**: [NoWakelock Community](https://discord.gg/kewmG5AShQ)
- **GitHub**: [Issues](https://github.com/NoWakeLock/NoWakeLock/issues)

### 问题报告模板
```markdown
## 环境信息
- 设备: [品牌 型号]
- Android版本: [版本号]
- Xposed框架: [LSPosed/EdXposed 版本]
- NoWakeLock版本: [版本号]

## 问题描述
[详细描述问题现象]

## 重现步骤
1. [步骤一]
2. [步骤二]
3. [问题出现]

## 预期结果
[期望的正常行为]

## 实际结果
[实际发生的异常行为]

## 相关日志
```log
[粘贴相关日志]
```

## 其他信息
[任何其他相关信息]
```

!!! warning "数据安全"
    进行故障排除时，务必先备份重要配置。某些操作可能导致数据丢失。

!!! tip "调试建议"
    复杂问题建议逐步排查，从最简单的配置开始，逐步增加复杂度，便于定位问题根源。