# 開發者文件

深入瞭解 NoWakeLock 的技術實作，為專案貢獻程式碼。

## 文件導航

### 🏗️ 架構設計
- [系統概覽](architecture/overview.md) - 整體架構和設計思路
- [Xposed Hooks](architecture/xposed-hooks.md) - Hook 系統的實作
- [資料流設計](architecture/data-flow.md) - 資料在系統中的流轉
- [資料庫設計](architecture/database.md) - 資料儲存架構

### ⚙️ 實作細節
- [Hook 詳解](implementation/hook-details.md) - 具體的 Hook 實作
- [計數器系統](implementation/counter-system.md) - 統計計算機制
- [程序間通訊](implementation/ipc.md) - 模組與應用程式的通訊

### 📚 API 參考
- [ContentProvider](api/content-provider.md) - 資料存取介面
- [內部 API](api/internal-api.md) - 模組內部介面

### 🤝 貢獻指南
- [開發環境](contributing/setup.md) - 搭建開發環境
- [編碼規範](contributing/guidelines.md) - 程式碼風格和約定
- [測試指南](contributing/testing.md) - 測試框架和方法

## 快速開始

### 環境要求
- **Android Studio** - Arctic Fox 或更新版本
- **JDK** - 17 或更新版本
- **Android SDK** - API 24-35
- **Git** - 版本控制工具

### 獲取原始碼
```bash
git clone https://github.com/NoWakeLock/NoWakeLock.git
cd NoWakeLock
git checkout dev
```

### 建置專案
```bash
# 安裝相依性
./gradlew clean

# 建置 Debug 版本
./gradlew assembleDebug

# 執行測試
./gradlew test
```

## 技術棧

### 核心技術
- **Kotlin** - 主要程式語言
- **Jetpack Compose** - 現代化 UI 框架
- **Room** - 資料庫抽象層
- **Coroutines** - 非同步程式設計
- **Flow** - 響應式資料流

### Xposed 整合
- **LSPosed API** - 主要 Hook 框架
- **EdXposed 相容** - 向後相容支援
- **反射機制** - 跨版本 API 適配

### 相依性注入
- **Koin** - 輕量級 DI 框架
- **ViewModel** - UI 狀態管理
- **Repository 模式** - 資料存取抽象

## 核心模組

### XposedHook 模組
```
xposedhook/
├── XposedModule.kt      # 模組入口點
├── hook/               # Hook 實作
│   ├── WakelockHook.kt
│   ├── AlarmHook.kt
│   └── ServiceHook.kt
└── model/              # 資料模型
    └── XpNSP.kt
```

### 資料層
```
data/
├── db/                 # 資料庫
│   ├── AppDatabase.kt
│   ├── InfoDatabase.kt
│   └── entity/
├── repository/         # 資料倉庫
└── counter/           # 計數器系統
```

### UI 層
```
ui/
├── screens/           # 頁面元件
├── components/        # 通用元件
├── theme/            # 主題樣式
└── navigation/       # 導航邏輯
```

## 開發流程

### 特性開發
1. **需求分析** - 明確功能需求和技術方案
2. **分支建立** - 從 `dev` 分支建立特性分支
3. **程式碼實作** - 遵循編碼規範實作功能
4. **單元測試** - 編寫和執行測試案例
5. **整合測試** - 在真實裝置上測試
6. **程式碼審查** - 提交 Pull Request
7. **合併發布** - 合併到 `dev` 分支

### Bug 修復
1. **問題重現** - 確認 Bug 的重現步驟
2. **根因分析** - 分析問題的根本原因
3. **修復實作** - 編寫最小化的修復程式碼
4. **回歸測試** - 確保修復不影響其他功能
5. **發布部署** - 根據嚴重程度選擇發布時機

## 程式碼架構

### MVVM 架構
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

### Hook 實作模式
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
                param.result = null // 攔截呼叫
                return@findAndHookMethod
            }
            // 繼續原始呼叫
        }
    }
}
```

## 測試策略

### 單元測試
- **ViewModel 測試** - 業務邏輯測試
- **Repository 測試** - 資料層測試
- **工具類測試** - 輔助函數測試

### 整合測試
- **資料庫測試** - Room 資料庫操作
- **Hook 測試** - Xposed Hook 功能
- **UI 測試** - Compose 介面測試

### 裝置測試
- **相容性測試** - 多版本 Android 裝置
- **效能測試** - 記憶體、CPU、電量消耗
- **穩定性測試** - 長時間執行測試

## 發布流程

### 版本管理
- **主版本** - 重大功能更新
- **次版本** - 新功能新增
- **修訂版本** - Bug 修復

### 分支策略
- **master** - 穩定版本
- **dev** - 開發版本
- **feature/*** - 特性分支
- **hotfix/*** - 緊急修復

### CI/CD 流程
```yaml
# GitHub Actions 工作流程
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

## 偵錯技巧

### Xposed 偵錯
```kotlin
// 使用 XposedBridge.log 輸出偵錯資訊
XposedBridge.log("NoWakeLock: Hook executed")

// 使用條件編譯控制偵錯程式碼
if (BuildConfig.DEBUG) {
    XposedBridge.log("Debug info: ${param.args}")
}
```

### 記錄分析
```bash
# 過濾 NoWakeLock 記錄
adb logcat | grep -i nowakelock

# 監控效能指標
adb shell dumpsys meminfo com.js.nowakelock
adb shell top | grep nowakelock
```

## 社群貢獻

### 參與方式
- **程式碼貢獻** - 功能開發和 Bug 修復
- **文件貢獻** - 改進文件和教學
- **測試貢獻** - 裝置相容性測試
- **翻譯貢獻** - 多語言支援

### 交流管道
- **GitHub Issues** - 問題報告和功能請求
- **GitHub Discussions** - 技術討論和想法交流
- **Telegram** - 即時交流和快速支援
- **Discord** - 深度技術討論

### 程式碼審查
所有貢獻都需要經過程式碼審查：
- 程式碼品質檢查
- 安全性評估
- 效能影響分析
- 相容性驗證

!!! info "開發者協議"
    貢獻程式碼即表示同意專案的開源協議（GPL v3.0）和貢獻者協議。

!!! tip "新手友好"
    專案歡迎新手貢獻者，我們有標記為 `good-first-issue` 的簡單任務供新手入門。