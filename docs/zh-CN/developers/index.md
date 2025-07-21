# 开发者文档

深入了解 NoWakeLock 的技术实现，为项目贡献代码。

## 文档导航

### 🏗️ 架构设计
- [系统概览](architecture/overview.md) - 整体架构和设计思路
- [Xposed Hooks](architecture/xposed-hooks.md) - Hook 系统的实现
- [数据流设计](architecture/data-flow.md) - 数据在系统中的流转
- [数据库设计](architecture/database.md) - 数据存储架构

### ⚙️ 实现细节
- [Hook 详解](implementation/hook-details.md) - 具体的 Hook 实现
- [计数器系统](implementation/counter-system.md) - 统计计算机制
- [进程间通信](implementation/ipc.md) - 模块与应用的通信

### 📚 API 参考
- [ContentProvider](api/content-provider.md) - 数据访问接口
- [内部 API](api/internal-api.md) - 模块内部接口

## 快速开始

### 环境要求
- **Android Studio** - Arctic Fox 或更新版本
- **JDK** - 17 或更新版本
- **Android SDK** - API 24-35
- **Git** - 版本控制工具

### 获取源码
```bash
git clone https://github.com/NoWakeLock/NoWakeLock.git
cd NoWakeLock
git checkout dev
```

### 构建项目
```bash
# 安装依赖
./gradlew clean

# 构建 Debug 版本
./gradlew assembleDebug

# 运行测试
./gradlew test
```

## 技术栈

### 核心技术
- **Kotlin** - 主要编程语言
- **Jetpack Compose** - 现代化 UI 框架
- **Room** - 数据库抽象层
- **Coroutines** - 异步编程
- **Flow** - 响应式数据流

### Xposed 集成
- **LSPosed API** - 主要 Hook 框架
- **EdXposed 兼容** - 向后兼容支持
- **反射机制** - 跨版本 API 适配

### 依赖注入
- **Koin** - 轻量级 DI 框架
- **ViewModel** - UI 状态管理
- **Repository 模式** - 数据访问抽象

## 核心模块

### XposedHook 模块
```
xposedhook/
├── XposedModule.kt      # 模块入口点
├── hook/               # Hook 实现
│   ├── WakelockHook.kt
│   ├── AlarmHook.kt
│   └── ServiceHook.kt
└── model/              # 数据模型
    └── XpNSP.kt
```

### 数据层
```
data/
├── db/                 # 数据库
│   ├── AppDatabase.kt
│   ├── InfoDatabase.kt
│   └── entity/
├── repository/         # 数据仓库
└── counter/           # 计数器系统
```

### UI 层
```
ui/
├── screens/           # 页面组件
├── components/        # 通用组件
├── theme/            # 主题样式
└── navigation/       # 导航逻辑
```

## 开发流程

### 特性开发
1. **需求分析** - 明确功能需求和技术方案
2. **分支创建** - 从 `dev` 分支创建特性分支
3. **代码实现** - 遵循编码规范实现功能
4. **单元测试** - 编写和运行测试用例
5. **集成测试** - 在真实设备上测试
6. **代码审查** - 提交 Pull Request
7. **合并发布** - 合并到 `dev` 分支

### Bug 修复
1. **问题复现** - 确认 Bug 的重现步骤
2. **根因分析** - 分析问题的根本原因
3. **修复实现** - 编写最小化的修复代码
4. **回归测试** - 确保修复不影响其他功能
5. **发布部署** - 根据严重程度选择发布时机

## 代码架构

### MVVM 架构
```kotlin
// ViewModel
class WakelocksViewModel(
    private val repository: WakelockRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(WakelocksUiState())
    val uiState: StateFlow<WakelocksUiState> = _uiState.asStateFlow()
    
    fun loadWakelocks() {
        viewModelScope.launch {
            repository.getWakelocks()
                .collect { wakelocks ->
                    _uiState.update { it.copy(wakelocks = wakelocks) }
                }
        }
    }
}

// Compose UI
@Composable
fun WakelocksScreen(viewModel: WakelocksViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    
    LazyColumn {
        items(uiState.wakelocks) { wakelock ->
            WakelockItem(wakelock = wakelock)
        }
    }
}
```

### Repository 模式
```kotlin
interface WakelockRepository {
    fun getWakelocks(): Flow<List<WakelockInfo>>
    suspend fun updateWakelock(wakelock: WakelockInfo)
}

class WakelockRepositoryImpl(
    private val dao: WakelockDao,
    private val xpProvider: XProvider
) : WakelockRepository {
    override fun getWakelocks(): Flow<List<WakelockInfo>> {
        return dao.getAllWakelocks()
            .map { entities -> entities.map { it.toDomain() } }
    }
}
```

### Hook 实现模式
```kotlin
object WakelockHook {
    fun hookWakeLocks(lpparam: LoadPackageParam) {
        findAndHookMethod(
            PowerManagerService::class.java,
            "acquireWakeLockInternal",
            *parameterTypes
        ) { param ->
            val result = processWakeLockAcquire(param.args)
            if (result.shouldBlock) {
                param.result = null // 拦截调用
                return@findAndHookMethod
            }
            // 继续原始调用
        }
    }
}
```

## 测试策略

### 单元测试
- **ViewModel 测试** - 业务逻辑测试
- **Repository 测试** - 数据层测试
- **工具类测试** - 辅助函数测试

### 集成测试
- **数据库测试** - Room 数据库操作
- **Hook 测试** - Xposed Hook 功能
- **UI 测试** - Compose 界面测试

### 设备测试
- **兼容性测试** - 多版本 Android 设备
- **性能测试** - 内存、CPU、电量消耗
- **稳定性测试** - 长时间运行测试

## 发布流程

### 版本管理
- **主版本** - 重大功能更新
- **次版本** - 新功能添加
- **修订版本** - Bug 修复

### 分支策略
- **master** - 稳定版本
- **dev** - 开发版本
- **feature/*** - 特性分支
- **hotfix/*** - 紧急修复

### CI/CD 流程
```yaml
# GitHub Actions 工作流
name: Build and Test
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Set up JDK
        uses: actions/setup-java@v4
        with:
          java-version: '17'
      - name: Run tests
        run: ./gradlew test
      - name: Build APK
        run: ./gradlew assembleDebug
```

## 调试技巧

### Xposed 调试
```kotlin
// 使用 XposedBridge.log 输出调试信息
XposedBridge.log("NoWakeLock: Hook executed")

// 使用条件编译控制调试代码
if (BuildConfig.DEBUG) {
    XposedBridge.log("Debug info: ${param.args}")
}
```

### 日志分析
```bash
# 过滤 NoWakeLock 日志
adb logcat | grep -i nowakelock

# 监控性能指标
adb shell dumpsys meminfo com.js.nowakelock
adb shell top | grep nowakelock
```

## 社区贡献

### 参与方式
- **代码贡献** - 功能开发和 Bug 修复
- **文档贡献** - 改进文档和教程
- **测试贡献** - 设备兼容性测试
- **翻译贡献** - 多语言支持

### 交流渠道
- **GitHub Issues** - 问题报告和功能请求
- **GitHub Discussions** - 技术讨论和想法交流
- **Telegram** - 实时交流和快速支持
- **Discord** - 深度技术讨论

### 代码审查
所有贡献都需要经过代码审查：
- 代码质量检查
- 安全性评估
- 性能影响分析
- 兼容性验证

!!! info "开发者协议"
    贡献代码即表示同意项目的开源协议（GPL v3.0）和贡献者协议。

!!! tip "新手友好"
    项目欢迎新手贡献者，我们有标记为 `good-first-issue` 的简单任务供新手入门。