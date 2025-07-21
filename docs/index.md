# NoWakeLock

NoWakeLock 是一个 Android Xposed 模块，用于管理设备的 WakeLock、Alarm 和 Service 行为，帮助优化电池续航。

!!! warning "重要免责声明与使用建议"
    **使用风险自担，开发者不对设备损坏承担责任。**
    
    **重要**：如果您的设备没有续航问题，不建议使用此软件。Android 11+ 的后台管理已经过大幅优化，仅在通过 BetterBatteryStats 等工具确认存在异常耗电问题时才建议使用。
    
    NoWakeLock 是针对特定问题的解决工具，而非通用优化软件。

!!! danger "⚠️ 重要：救援模式"
    **如果设备启动后卡死、无限重启或系统异常**：
    
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
    
    **预防措施**：首次使用时谨慎配置，逐步测试规则效果。

## 核心功能

- **WakeLock 管理** - 监控和控制应用的唤醒锁
- **Alarm 控制** - 管理系统定时任务
- **Service 管理** - 控制后台服务启动
- **应用管理** - 按应用查看和配置所有组件
- **规则系统** - 支持正则表达式的灵活配置

## 快速开始

1. [安装指南](getting-started/installation.md) - 安装 NoWakeLock 模块
2. [问题分析](getting-started/problem-analysis.md) - 分析耗电问题（使用前必读）
3. [快速上手](getting-started/quick-start.md) - 5 分钟基础配置
4. [模块检查](getting-started/module-check.md) - 验证模块状态

## 主要功能

### 📱 应用管理
- [应用管理](features/app-management.md) - 按应用查看和配置

### ⚡ 系统控制
- [WakeLock 管理](features/wakelocks.md) - 防止设备休眠的锁机制
- [Alarm 管理](features/alarms.md) - 系统定时任务控制
- [Service 管理](features/services.md) - 后台服务控制

### 🔧 配置工具
- [规则与正则](features/rules-regex.md) - 灵活的匹配规则
- [应用管理](features/app-management.md) - 按应用统一管理

## 使用指南

通过应用主界面的五个标签页进行操作：
- **Apps** - 应用列表和整体管理
- **Wakelocks** - WakeLock 监控和控制
- **Alarms** - 定时任务管理
- **Services** - 后台服务控制
- **Settings** - 全局设置和配置

## 获取帮助

- [常见问题](reference/faq.md) - 最常遇到的问题解答
- [故障排除](reference/troubleshooting.md) - 问题诊断与解决
- [术语表](reference/glossary.md) - 技术术语说明

## 兼容性

- **Android 版本**: 7.0 (API 24) 至 15.0 (API 35)
- **Xposed 框架**: LSPosed (推荐)、EdXposed
- **架构支持**: ARM64、ARM32
- **当前版本**: 3.0.3 (正式版)

!!! error "设备兼容性限制"
    **三星设备 OneUI 目前尚不支持**
    
    由于 OneUI 更改了 Android 源码，Hook 位置经过多种方法尝试，始终无法生效。其他厂商的 Android 设备通常可以正常使用。

## 社区与支持

- **Telegram**: [@nowakelock](https://t.me/nowakelock)
- **Discord**: [NoWakelock](https://discord.gg/kewmG5AShQ)
- **GitHub**: [NoWakeLock/NoWakeLock](https://github.com/NoWakeLock/NoWakeLock)

## 开发者

对技术实现或贡献代码感兴趣？

- [开发者文档](developers/) - 技术架构和实现细节
- [开发环境](developers/) - 如何参与开发

---

!!! warning "使用提醒"
    NoWakeLock 需要 Xposed 框架，使用前请备份重要数据。开发者不对设备问题承担责任。

!!! info "许可证"
    本项目基于 [GNU General Public License v3.0](https://github.com/NoWakeLock/NoWakeLock/blob/master/LICENSE) 开源。