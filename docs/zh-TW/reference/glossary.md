# 術語表

NoWakeLock 相關的技術術語和概念解釋。

## 核心概念

### WakeLock（喚醒鎖）
防止 Android 裝置進入休眠狀態的機制。應用程式透過持有 WakeLock 來保持 CPU 執行或螢幕常亮。

**類型**：
- **PARTIAL_WAKE_LOCK** - 保持 CPU 執行，螢幕可以關閉
- **SCREEN_DIM_WAKE_LOCK** - 保持螢幕亮起但允許變暗
- **SCREEN_BRIGHT_WAKE_LOCK** - 保持螢幕完全亮起
- **FULL_WAKE_LOCK** - 保持 CPU 和螢幕都執行

### Alarm（定時任務）
Android 系統的定時器服務，允許應用程式在特定時間或間隔執行任務。

**類型**：
- **RTC** - 基於實際時間的定時器
- **RTC_WAKEUP** - 基於實際時間且會喚醒裝置
- **ELAPSED_REALTIME** - 基於裝置啟動時間
- **ELAPSED_REALTIME_WAKEUP** - 基於啟動時間且會喚醒裝置

### Service（服務）
在背景執行的 Android 應用程式元件，不提供使用者介面。

**類型**：
- **前景服務** - 執行使用者可感知的任務，顯示持續通知
- **背景服務** - 執行使用者不直接感知的任務
- **綁定服務** - 提供用戶端-伺服器介面

## Android 系統

### Doze Mode（打盹模式）
Android 6.0+ 引入的省電機制，裝置靜止時進入深度睡眠狀態。

### App Standby（應用程式待機）
系統對長時間未使用應用程式的省電限制。

### Background Execution Limits（背景執行限制）
Android 8.0+ 對背景服務和廣播接收器的限制。

### SELinux（安全增強的 Linux）
Android 系統的強制存取控制安全機制。

## Xposed 框架

### Xposed Framework
允許在不修改 APK 的情況下修改系統和應用程式行為的框架。

### Hook（鉤子）
攔截和修改函數呼叫的技術。

### LSPosed
基於 Riru 的現代 Xposed 實作，支援 Android 8.1+。

### EdXposed
基於 YAHFA 和 SandHook 的 Xposed 實作。

### Zygote
Android 系統中所有應用程式程序的父程序。

## NoWakeLock 術語

### 攔截模式
- **允許** - 不做任何限制，正常執行
- **限制** - 設定時間或頻率限制
- **攔截** - 完全阻止操作

### 規則系統
基於模式比對的設定機制，支援正規表示式。

### 元件
WakeLock、Alarm、Service 的統稱。

### 作用域
Xposed 模組生效的應用程式範圍。

### DA
Detection/Action 的縮寫，指 NoWakeLock 檢測到的 WakeLock、Alarm、Service 活動。

## 效能指標

### 獲取次數
WakeLock 被獲取的總數。

### 累計時長
WakeLock 被持有的總時間。

### 觸發頻率
Alarm 的平均觸發間隔。

### 啟動次數
Service 被啟動的總數。

### 攔截率
被攔截的操作佔總操作的百分比。

## 技術術語

### API Level
Android 版本對應的 API 級別號。

### Package Name
應用程式的唯一識別符，如 `com.example.app`。

### UID（使用者識別符）
系統分配給每個應用程式的唯一數字識別。

### PID（程序識別符）
系統分配給每個程序的唯一數字識別。

### ContentProvider
Android 四大元件之一，用於跨應用程式資料共享。

### IPC（程序間通訊）
不同程序之間的資料交換機制。

### JNI（Java 原生介面）
Java 程式碼呼叫本機 C/C++ 程式碼的介面。

## 資料庫術語

### Room
Google 官方的 SQLite 抽象層框架。

### DAO（資料存取物件）
封裝資料庫操作的介面。

### Entity（實體）
資料庫表的物件對應。

### Migration（遷移）
資料庫版本升級的處理機制。

## 開發術語

### Kotlin
現代的 JVM 程式語言，Android 開發首選語言。

### Jetpack Compose
Android 現代宣告式 UI 工具包。

### Coroutines（協程）
Kotlin 的非同步程式設計機制。

### Flow
Kotlin 的響應式資料流框架。

### ViewModel
Android 架構元件，管理 UI 相關資料。

### LiveData
可觀察的資料持有類，具有生命週期感知能力。

### Koin
輕量級的依賴注入框架。

## 正規表示式

### 元字元
具有特殊含義的字元，如 `.`、`*`、`+`、`?` 等。

### 字元類
用方括號括起來的字元集合，如 `[abc]`。

### 量詞
指定比對次數的符號，如 `{n}`、`{n,m}` 等。

### 分組
用圓括號建立的子表示式，如 `(abc)+`。

### 錨點
指定比對位置的符號，如 `^`（開始）、`$`（結束）。

## 設定術語

### 繼承
子級設定從父級自動獲取設定的機制。

### 優先級
多個規則衝突時的執行順序。

### 範本
預設的設定組合，可重複使用。

### 白名單
不受規則限制的應用程式或元件清單。

### 黑名單
被嚴格限制或攔截的應用程式或元件清單。

## 系統服務

### PowerManagerService
管理裝置電源狀態的系統服務。

### AlarmManagerService
管理系統定時任務的服務。

### ActivityManagerService
管理應用程式生命週期的服務。

### PackageManagerService
管理應用程式安裝和權限的服務。

### WindowManagerService
管理視窗顯示的服務。

## 權限相關

### QUERY_ALL_PACKAGES
查詢所有已安裝應用程式的權限（Android 11+）。

### WAKE_LOCK
獲取 WakeLock 的權限。

### RECEIVE_BOOT_COMPLETED
接收開機廣播的權限。

### WRITE_EXTERNAL_STORAGE
寫入外部儲存的權限。

## 偵錯術語

### ADB（Android 偵錯橋）
連接開發機和 Android 裝置的命令列工具。

### Logcat
Android 系統記錄檢視工具。

### ANR（應用程式無回應）
應用程式主執行緒阻塞超過 5 秒的錯誤。

### Crash（當機）
應用程式異常終止的錯誤。

### Memory Leak（記憶體洩漏）
程式執行中記憶體無法正常釋放的問題。

## 效能術語

### CPU 使用率
處理器的使用百分比。

### 記憶體佔用
應用程式使用的 RAM 大小。

### 電量消耗
應用程式的電池使用量。

### 網路流量
應用程式的資料傳輸量。

### 儲存 I/O
應用程式的儲存讀寫活動。

!!! info "術語更新"
    隨著專案發展，新的術語會持續新增到此清單中。如有疑問，請查閱相關文件或聯絡社群。

!!! tip "學習建議"
    建議新使用者先熟悉核心概念（WakeLock、Alarm、Service），再逐步瞭解技術細節。