# 安装指南

!!! danger "⚠️ 救援模式 - 最重要！"
    **如果安装后设备启动异常、卡死或无限重启**：
    
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

## 前置要求

### 系统要求
- Android 7.0 (API 24) 或更高版本

!!! error "设备兼容性限制"
    **三星设备 OneUI 目前尚不支持**
    
    由于 OneUI 更改了 Android 源码，Hook 位置经过多种方法尝试，始终无法生效。我们正在研究解决方案，但目前无法在三星 OneUI 设备上正常工作。
    
    其他厂商的 Android 设备通常可以正常使用。

### Xposed 框架
安装以下框架之一：

| 框架 | 适用版本 | 推荐程度 |
|------|----------|----------|
| LSPosed | Android 8.1+ | ⭐⭐⭐⭐⭐ |
| EdXposed | Android 8.0-11 | ⭐⭐⭐ |

!!! info "框架选择"
    推荐使用 LSPosed，兼容性和稳定性更好。

## 下载 NoWakeLock

### 官方渠道

[![GitHub](https://img.shields.io/badge/GitHub-Releases-blue)](https://github.com/NoWakeLock/NoWakeLock/releases)
[![IzzyOnDroid](https://img.shields.io/badge/IzzyOnDroid-F-Droid-green)](https://apt.izzysoft.de/fdroid/index/apk/com.js.nowakelock)

**下载方式**：
- **GitHub Releases** - 直接下载 APK 文件
- **IzzyOnDroid** - 在 F-Droid 中添加 IzzyOnDroid 源后安装
- **F-Droid 官方** - 计划中

!!! tip "F-Droid 源设置"
    要通过 IzzyOnDroid 安装：
    1. 在 F-Droid 应用中添加源：`https://apt.izzysoft.de/fdroid/repo`
    2. 搜索 NoWakeLock 进行安装

### 版本选择

- **稳定版** - 从 GitHub Releases 或 IzzyOnDroid 下载
- **测试版** - 从 dev 分支构建

!!! warning "仅支持官方版本"
    仅对官方渠道下载的版本提供支持。

## 安装步骤

### 1. 下载 APK
从官方渠道下载最新版本的 APK 文件。

### 2. 安装应用
```bash
# 使用 ADB 安装（可选）
adb install nowakelock-v3.x.x.apk
```

或直接在设备上安装 APK 文件。

【需要截图：安装界面】

### 3. 启用模块
1. 打开 Xposed 管理器 (LSPosed/EdXposed)
2. 进入"模块"页面
3. 勾选 NoWakeLock
4. 重启设备

【需要截图：LSPosed 模块列表】

### 4. 配置作用域
在 LSPosed 中设置模块作用域：

**必需的作用域**：
- `android` (系统框架)

!!! tip "作用域说明"
    NoWakeLock 只需要 `android` 系统框架作用域即可正常工作。

【需要截图：作用域配置】

## 验证安装

### 检查模块状态
1. 打开 NoWakeLock 应用
2. 进入"模块检查"页面
3. 确认所有项目显示绿色✅

【需要截图：模块检查页面】

### 验证项目

| 检查项目 | 说明 |
|----------|------|
| Xposed 框架激活 | 框架正常运行 |
| 模块已加载 | NoWakeLock 模块被识别 |
| Hook 正常工作 | 系统调用拦截成功 |
| 配置读取成功 | 应用可以读取配置 |

### 测试功能
1. 查看"应用"页面是否显示已安装应用
2. 检查"WakeLocks"页面是否有数据
3. 尝试设置一个简单规则

## 常见问题

### 模块未激活
**症状**: 模块检查显示❌  
**解决方案**:
1. 确认 Xposed 框架正常运行
2. 检查模块是否被勾选
3. 重启设备后再次检查

### Hook 不工作
**症状**: 没有 WakeLock/Alarm 数据  
**解决方案**:
1. 确认作用域包含 `android`
2. 检查 SELinux 策略
3. 查看 Xposed 日志

### 应用闪退
**症状**: 打开应用立即崩溃  
**解决方案**:
1. 检查 Android 版本兼容性
2. 清除应用数据
3. 重新安装模块

## 卸载模块

### 完整卸载步骤
1. 在 Xposed 管理器中取消勾选模块
2. 重启设备
3. 卸载 NoWakeLock 应用

## 下一步

安装完成后：

1. [快速开始](quick-start.md) - 5 分钟上手配置
2. [模块检查](module-check.md) - 详细验证模块状态
3. [WakeLock 管理](../features/wakelocks.md) - 开始管理 WakeLock