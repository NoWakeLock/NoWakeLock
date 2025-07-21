# 系統概覽

NoWakeLock 採用現代 Android 開發架構，結合 Xposed 框架的系統級 Hook 能力和 Jetpack Compose 的宣告式 UI 設計。

## 整體架構

### 架構圖

```
┌─────────────────────────────────────────────────────────┐
│                    Android 系統                          │
├─────────────────────────────────────────────────────────┤
│  PowerManagerService │ AlarmManagerService │ ActiveServices │
│         ↓ Hook             ↓ Hook               ↓ Hook     │
├─────────────────────────────────────────────────────────┤
│                 Xposed 模組層                            │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐        │
│  │WakelockHook │ │ AlarmHook   │ │ ServiceHook │        │
│  └─────────────┘ └─────────────┘ └─────────────┘        │
├─────────────────────────────────────────────────────────┤
│                   應用程式層                                 │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐        │
│  │ Presentation│ │  Domain     │ │    Data     │        │
│  │   (UI)      │ │ (Business)  │ │ (Storage)   │        │
│  └─────────────┘ └─────────────┘ └─────────────┘        │
└─────────────────────────────────────────────────────────┘
```

## 技術棧

### 核心技術
- **Kotlin** 1.9.25 - 主要程式語言
- **Jetpack Compose** 2025.04.01 - 宣告式 UI 框架
- **Room** 2.7.1 - 資料庫 ORM 框架
- **Coroutines** - 非同步程式設計
- **Flow** - 響應式資料流

### Xposed 整合
- **Xposed API** 82 - 系統級 Hook 框架
- **LSPosed** - 主要目標框架
- **EdXposed** - 向後相容支援
- **反射機制** - 跨版本 API 適配

### 相依性注入
- **Koin** 4.0.4 - 輕量級 DI 框架
- **模組化設定** - 按功能分組注入
- **ViewModel 管理** - 自動生命週期繫定

## 架構分層

### 1. Xposed 模組層
```kotlin
// 入口點
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

**職責**：
- 系統服務 Hook 放截
- 跨程序資料通訊
- 版本相容性處理

### 2. 資料層 (Data Layer)

#### 雙資料庫架構
```kotlin
// 主業務資料庫
@Database(
    entities = [AppInfo::class, WakelockRule::class],
    version = 13
)
abstract class AppDatabase : RoomDatabase()

// 事件記錄資料庫  
@Database(
    entities = [InfoEvent::class],
    version = 12
)
abstract class InfoDatabase : RoomDatabase()
```

**職責**：
- 應用程式資訊管理
- 事件資料記錄
- 規則設定儲存
- 使用者偏好設定

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
    // 實作資料存取邏輯
}
```

**特點**：
- 統一的資料存取介面
- 本機資料庫 + Xposed 資料來源
- 響應式資料流

### 3. 業務層 (Domain Layer)

#### ViewModel 架構
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

**特點**：
- MVVM 架構模式
- StateFlow 狀態管理
- 響應式資料繫定

### 4. 表現層 (Presentation Layer)

#### Compose UI 架構
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

**特點**：
- 宣告式 UI 元件
- 類型安全導航
- Material Design 3

## 核心特性

### 1. 多使用者支援
```kotlin
class UserManager {
    fun getCurrentUsers(): List<User> {
        return UserManagerService.getUsers()
    }
    
    fun switchUser(userId: Int) {
        // 切換使用者上下文
    }
}
```

### 2. 版本相容性
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

### 3. 效能最佳化
- **參數位置快取** - 減少反射開銷
- **Flow distinctUntilChanged** - 避免重複更新
- **資料庫索引** - 最佳化查詢效能
- **懶加載** - 按需加載資料

## 資料流設計

### 事件處理流程
```
系統呼叫 → Hook放截 → 規則比對 → 執行動作 → 記錄事件 → 更新UI
    ↓         ↓         ↓         ↓         ↓        ↓
PowerManager → WakelockHook → RuleEngine → Block/Allow → InfoEvent → Flow更新
```

### 狀態管理
```kotlin
data class DAsUiState(
    val apps: List<AppDas> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val filterOption: FilterOption = FilterOption.ALL,
    val sortOption: SortOption = SortOption.NAME
)
```

## 通訊機制

### 1. Xposed ↔ 應用程式通訊
- **XProvider** - ContentProvider 跨程序通訊
- **SharedPreferences** - 設定資料共享
- **檔案系統** - 臨時資料交換

### 2. 元件間通訊
- **Repository 模式** - 資料層抽象
- **StateFlow** - 響應式狀態共享
- **Navigation** - 頁面間參數傳遞

## 擴展點

### 1. 新 Hook 類型添加
```kotlin
// 建立新的 Hook 類別
object NewFeatureHook {
    fun hook(lpparam: LoadPackageParam) {
        // Hook 實作
    }
}

// 在 XposedModule 中註冊
NewFeatureHook.hook(lpparam)
```

### 2. 新 UI 頁面添加
```kotlin
// 添加新路由
@Serializable
data class NewFeature(val param: String = "")

// 添加導航和頁面
composable<NewFeature> { NewFeatureScreen() }
```

### 3. 新資料來源整合
```kotlin
// 擴展 Repository 介面
interface ExtendedRepository : DARepository {
    fun getNewData(): Flow<List<NewData>>
}
```

## 設計原則

### 1. 單一職責
每個類別和模組都有明確的職責邊界，便於維護和測試。

### 2. 相依性倒置
高層模組不相依低層模組，都相依於抽象介面。

### 3. 開闉原則
對擴展開放，對修改關閉，便於添加新功能。

### 4. 響應式設計
使用 Flow 和 StateFlow 實現響應式資料流，確保 UI 與資料同步。

!!! info "架構優勢"
    這種分層架構的設計使得 NoWakeLock 既能處理複雜的系統級操作，又保持了良好的程式碼組織和可維護性。

!!! warning "注意事項"
    系統級 Hook 的修改需要謹慎處理版本相容性，建議在多個 Android 版本上充分測試。