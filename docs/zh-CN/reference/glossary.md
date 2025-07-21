# 术语表

NoWakeLock 相关的技术术语和概念解释。

## 核心概念

### WakeLock（唤醒锁）
防止 Android 设备进入休眠状态的机制。应用通过持有 WakeLock 来保持 CPU 运行或屏幕常亮。

**类型**：
- **PARTIAL_WAKE_LOCK** - 保持 CPU 运行，屏幕可以关闭
- **SCREEN_DIM_WAKE_LOCK** - 保持屏幕亮起但允许变暗
- **SCREEN_BRIGHT_WAKE_LOCK** - 保持屏幕完全亮起
- **FULL_WAKE_LOCK** - 保持 CPU 和屏幕都运行

### Alarm（定时任务）
Android 系统的定时器服务，允许应用在特定时间或间隔执行任务。

**类型**：
- **RTC** - 基于实际时间的定时器
- **RTC_WAKEUP** - 基于实际时间且会唤醒设备
- **ELAPSED_REALTIME** - 基于设备启动时间
- **ELAPSED_REALTIME_WAKEUP** - 基于启动时间且会唤醒设备

### Service（服务）
在后台运行的 Android 应用组件，不提供用户界面。

**类型**：
- **前台服务** - 执行用户可感知的任务，显示持续通知
- **后台服务** - 执行用户不直接感知的任务
- **绑定服务** - 提供客户端-服务器接口

## Android 系统

### Doze Mode（打盹模式）
Android 6.0+ 引入的省电机制，设备静止时进入深度睡眠状态。

### App Standby（应用待机）
系统对长时间未使用应用的省电限制。

### Background Execution Limits（后台执行限制）
Android 8.0+ 对后台服务和广播接收器的限制。

### SELinux（安全增强的 Linux）
Android 系统的强制访问控制安全机制。

## Xposed 框架

### Xposed Framework
允许在不修改 APK 的情况下修改系统和应用行为的框架。

### Hook（钩子）
拦截和修改函数调用的技术。

### LSPosed
基于 Riru 的现代 Xposed 实现，支持 Android 8.1+。

### EdXposed
基于 YAHFA 和 SandHook 的 Xposed 实现。

### Zygote
Android 系统中所有应用进程的父进程。

## NoWakeLock 术语

### 拦截模式
- **允许** - 不做任何限制，正常运行
- **限制** - 设置时间或频率限制
- **拦截** - 完全阻止操作

### 规则系统
基于模式匹配的配置机制，支持正则表达式。

### 组件
WakeLock、Alarm、Service 的统称。

### 作用域
Xposed 模块生效的应用范围。

### DA
Detection/Action 的缩写，指 NoWakeLock 检测到的 WakeLock、Alarm、Service 活动。

## 性能指标

### 获取次数
WakeLock 被获取的总数。

### 累计时长
WakeLock 被持有的总时间。

### 触发频率
Alarm 的平均触发间隔。

### 启动次数
Service 被启动的总数。

### 拦截率
被拦截的操作占总操作的百分比。

## 技术术语

### API Level
Android 版本对应的 API 级别号。

### Package Name
应用的唯一标识符，如 `com.example.app`。

### UID（用户标识符）
系统分配给每个应用的唯一数字标识。

### PID（进程标识符）
系统分配给每个进程的唯一数字标识。

### ContentProvider
Android 四大组件之一，用于跨应用数据共享。

### IPC（进程间通信）
不同进程之间的数据交换机制。

### JNI（Java 原生接口）
Java 代码调用本地 C/C++ 代码的接口。

## 数据库术语

### Room
Google 官方的 SQLite 抽象层框架。

### DAO（数据访问对象）
封装数据库操作的接口。

### Entity（实体）
数据库表的对象映射。

### Migration（迁移）
数据库版本升级的处理机制。

## 开发术语

### Kotlin
现代的 JVM 编程语言，Android 开发首选语言。

### Jetpack Compose
Android 现代声明式 UI 工具包。

### Coroutines（协程）
Kotlin 的异步编程机制。

### Flow
Kotlin 的响应式数据流框架。

### ViewModel
Android 架构组件，管理 UI 相关数据。

### LiveData
可观察的数据持有类，具有生命周期感知能力。

### Koin
轻量级的依赖注入框架。

## 正则表达式

### 元字符
具有特殊含义的字符，如 `.`、`*`、`+`、`?` 等。

### 字符类
用方括号括起来的字符集合，如 `[abc]`。

### 量词
指定匹配次数的符号，如 `{n}`、`{n,m}` 等。

### 分组
用圆括号创建的子表达式，如 `(abc)+`。

### 锚点
指定匹配位置的符号，如 `^`（开始）、`$`（结束）。

## 配置术语

### 继承
子级配置从父级自动获取设置的机制。

### 优先级
多个规则冲突时的执行顺序。

### 模板
预设的配置组合，可重复使用。

### 白名单
不受规则限制的应用或组件列表。

### 黑名单
被严格限制或拦截的应用或组件列表。

## 系统服务

### PowerManagerService
管理设备电源状态的系统服务。

### AlarmManagerService
管理系统定时任务的服务。

### ActivityManagerService
管理应用生命周期的服务。

### PackageManagerService
管理应用安装和权限的服务。

### WindowManagerService
管理窗口显示的服务。

## 权限相关

### QUERY_ALL_PACKAGES
查询所有已安装应用的权限（Android 11+）。

### WAKE_LOCK
获取 WakeLock 的权限。

### RECEIVE_BOOT_COMPLETED
接收开机广播的权限。

### WRITE_EXTERNAL_STORAGE
写入外部存储的权限。

## 调试术语

### ADB（Android 调试桥）
连接开发机和 Android 设备的命令行工具。

### Logcat
Android 系统日志查看工具。

### ANR（应用无响应）
应用主线程阻塞超过 5 秒的错误。

### Crash（崩溃）
应用异常终止的错误。

### Memory Leak（内存泄漏）
程序运行中内存无法正常释放的问题。

## 性能术语

### CPU 使用率
处理器的使用百分比。

### 内存占用
应用使用的 RAM 大小。

### 电量消耗
应用的电池使用量。

### 网络流量
应用的数据传输量。

### 存储 I/O
应用的存储读写活动。

!!! info "术语更新"
    随着项目发展，新的术语会持续添加到此列表中。如有疑问，请查阅相关文档或联系社区。

!!! tip "学习建议"
    建议新用户先熟悉核心概念（WakeLock、Alarm、Service），再逐步了解技术细节。