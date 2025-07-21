# 系统概览

NoWakeLock 采用现代 Android 开发架构，结合 Xposed 框架的系统级 Hook 能力和 Jetpack Compose 的声明式 UI 设计。

## 整体架构

### 架构图

```
┌─────────────────────────────────────────────────────────┐
│                    Android 系统                          │
├─────────────────────────────────────────────────────────┤
│  PowerManagerService │ AlarmManagerService │ ActiveServices │
│         ↓ Hook             ↓ Hook               ↓ Hook     │
├─────────────────────────────────────────────────────────┤
│                 Xposed 模块层                            │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐        │
│  │WakelockHook │ │ AlarmHook   │ │ ServiceHook │        │
│  └─────────────┘ └─────────────┘ └─────────────┘        │
├─────────────────────────────────────────────────────────┤
│                   应用层                                 │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐        │
│  │ Presentation│ │  Domain     │ │    Data     │        │
│  │   (UI)      │ │ (Business)  │ │ (Storage)   │        │
│  └─────────────┘ └─────────────┘ └─────────────┘        │
└─────────────────────────────────────────────────────────┘
```

## 技术栈

### 核心技术
- **Kotlin** 1.9.25 - 主要编程语言
- **Jetpack Compose** 2025.04.01 - 声明式 UI 框架
- **Room** 2.7.1 - 数据库 ORM 框架
- **Coroutines** - 异步编程
- **Flow** - 响应式数据流

### Xposed 集成
- **Xposed API** 82 - 系统级 Hook 框架
- **LSPosed** - 主要目标框架
- **EdXposed** - 向后兼容支持
- **反射机制** - 跨版本 API 适配

### 依赖注入
- **Koin** 4.0.4 - 轻量级 DI 框架
- **模块化配置** - 按功能分组注入
- **ViewModel 管理** - 自动生命周期绑定

## 架构分层

### 1. Xposed 模块层
```kotlin
// 入口点
class XposedModule : IXposedHookZygoteInit, IXposedHookLoadPackage {
    override fun handleLoadPackage(lpparam: LoadPackageParam) {
        when (lpparam.packageName) {
            "android" -> {
                WakelockHook.hook(lpparam)
                AlarmHook.hook(lpparam)
                ServiceHook.hook(lpparam)
            }
        }
    }
}
```

**职责**：
- 系统服务 Hook 拦截
- 跨进程数据通信
- 版本兼容性处理

### 2. 数据层 (Data Layer)

#### 双数据库架构
```kotlin
// 主业务数据库
@Database(
    entities = [AppInfo::class, WakelockRule::class],
    version = 13
)
abstract class AppDatabase : RoomDatabase()

// 事件记录数据库  
@Database(
    entities = [InfoEvent::class],
    version = 12
)
abstract class InfoDatabase : RoomDatabase()
```

**职责**：
- 应用信息管理
- 事件数据记录
- 规则配置存储
- 用户偏好设置

#### Repository 模式
```kotlin
interface DARepository {
    fun getApps(userId: Int): Flow<List<AppDas>>
    suspend fun updateRule(rule: Rule)
}

class DARepositoryImpl(
    private val appInfoDao: AppInfoDao,
    private val xProvider: XProvider
) : DARepository {
    // 实现数据访问逻辑
}
```

**特点**：
- 统一的数据访问接口
- 本地数据库 + Xposed 数据源
- 响应式数据流

### 3. 业务层 (Domain Layer)

#### ViewModel 架构
```kotlin
class DAsViewModel(
    private val repository: DARepository,
    private val userRepository: UserPreferencesRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DAsUiState())
    val uiState: StateFlow<DAsUiState> = _uiState.asStateFlow()
    
    fun loadData() {
        viewModelScope.launch {
            repository.getApps()
                .distinctUntilChanged()
                .collect { apps ->
                    _uiState.update { it.copy(apps = apps) }
                }
        }
    }
}
```

**特点**：
- MVVM 架构模式
- StateFlow 状态管理
- 响应式数据绑定

### 4. 表现层 (Presentation Layer)

#### Compose UI 架构
```kotlin
@Composable
fun NoWakeLockApp() {
    val navController = rememberNavController()
    
    NoWakeLockTheme {
        NavHost(navController = navController) {
            composable<Apps> { AppsScreen() }
            composable<Wakelocks> { WakelocksScreen() }
            composable<Services> { ServicesScreen() }
        }
    }
}
```

**特点**：
- 声明式 UI 组件
- 类型安全导航
- Material Design 3

## 核心特性

### 1. 多用户支持
```kotlin
class UserManager {
    fun getCurrentUsers(): List<User> {
        return UserManagerService.getUsers()
    }
    
    fun switchUser(userId: Int) {
        // 切换用户上下文
    }
}
```

### 2. 版本兼容性
```kotlin
object VersionCompat {
    fun getParameterIndices(method: Method): IntArray {
        return when (Build.VERSION.SDK_INT) {
            in 24..28 -> intArrayOf(0, 1, 2)
            in 29..30 -> intArrayOf(1, 2, 3)
            else -> intArrayOf(2, 3, 4)
        }
    }
}
```

### 3. 性能优化
- **参数位置缓存** - 减少反射开销
- **Flow distinctUntilChanged** - 避免重复更新
- **数据库索引** - 优化查询性能
- **懒加载** - 按需加载数据

## 数据流设计

### 事件处理流程
```
系统调用 → Hook拦截 → 规则匹配 → 执行动作 → 记录事件 → 更新UI
    ↓         ↓         ↓         ↓         ↓        ↓
PowerManager → WakelockHook → RuleEngine → Block/Allow → InfoEvent → Flow更新
```

### 状态管理
```kotlin
data class DAsUiState(
    val apps: List<AppDas> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val filterOption: FilterOption = FilterOption.ALL,
    val sortOption: SortOption = SortOption.NAME
)
```

## 通信机制

### 1. Xposed ↔ 应用通信
- **XProvider** - ContentProvider 跨进程通信
- **SharedPreferences** - 配置数据共享
- **文件系统** - 临时数据交换

### 2. 组件间通信
- **Repository 模式** - 数据层抽象
- **StateFlow** - 响应式状态共享
- **Navigation** - 页面间参数传递

## 扩展点

### 1. 新 Hook 类型添加
```kotlin
// 创建新的 Hook 类
object NewFeatureHook {
    fun hook(lpparam: LoadPackageParam) {
        // Hook 实现
    }
}

// 在 XposedModule 中注册
NewFeatureHook.hook(lpparam)
```

### 2. 新 UI 页面添加
```kotlin
// 添加新路由
@Serializable
data class NewFeature(val param: String = "")

// 添加导航和页面
composable<NewFeature> { NewFeatureScreen() }
```

### 3. 新数据源集成
```kotlin
// 扩展 Repository 接口
interface ExtendedRepository : DARepository {
    fun getNewData(): Flow<List<NewData>>
}
```

## 设计原则

### 1. 单一职责
每个类和模块都有明确的职责边界，便于维护和测试。

### 2. 依赖倒置
高层模块不依赖低层模块，都依赖于抽象接口。

### 3. 开闭原则
对扩展开放，对修改关闭，便于添加新功能。

### 4. 响应式设计
使用 Flow 和 StateFlow 实现响应式数据流，确保 UI 与数据同步。

!!! info "架构优势"
    这种分层架构的设计使得 NoWakeLock 既能处理复杂的系统级操作，又保持了良好的代码组织和可维护性。

!!! warning "注意事项"
    系统级 Hook 的修改需要谨慎处理版本兼容性，建议在多个 Android 版本上充分测试。