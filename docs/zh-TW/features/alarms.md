# Alarm 管理

Alarm（定時任務）是 Android 系統的定時器機制，用於在特定時間觸發操作，頻繁的 Alarm 會影響裝置續航。

!!! danger "⚠️ 救援模式 - 重要提醒！"
    **如果錯誤設定 WakeLock 導致裝置無法啟動**：
    
    **情況1：LSPosed 框架問題（安裝後未設定就遇到問題）**：
    1. 長按電源鍵10秒強制重啟
    2. 螢幕變黑後立即反覆按任意硬體按鍵
    3. 感受到2次短震動後，繼續快速按4次相同按鍵
    4. 第4次按鍵後感受到長震動，表示 LSPosed 已停用
    5. 正常啟動後在 LSPosed 中停用 NoWakeLock 模組
    
    **情況2：誤操作設定問題（可進入 Recovery）**：
    1. 進入 Recovery → 檔案管理
    2. 導航到 /data/misc/xxx-xxx-xxx/prefs/com.js.nowakelock
       （xxx-xxx-xxx 是一段很長的隨機字元串，每個裝置可能都不同）
    3. 刪除整個資料夾
    4. 重啟裝置
    
    **不確定原因時**：直接清除 NoWakeLock 應用程式資料，重新設定時避免攔截系統關鍵元件。

## 功能概述

### Alarm 作用
- 定時執行任務
- 週期性操作觸發
- 系統級別的定時器
- 應用程式保活機制

### 管理目標
- 監控 Alarm 設定和觸發
- 識別過度頻繁的定時任務
- 控制 Alarm 的觸發頻率
- 減少不必要的喚醒

## 介面說明

### Alarm 清單

【需要截圖：Alarm 清單頁面】

**清單資訊**：
- **標籤** - Alarm 標識符
- **應用程式** - 來源包名
- **類型** - Alarm 類型圖示
- **狀態** - 攔截狀態
- **統計** - 觸發次數和時間資訊

### 狀態顯示

| 狀態 | 圖示 | 說明 |
|------|------|------|
| 允許 | 🟢 | 正常觸發 |
| 限制 | 🟡 | 降低觸發頻率 |
| 攔截 | 🔴 | 阻止觸發 |
| 待觸發 | ⏰ | 已設定等待觸發 |

## Alarm 類型

### 按觸發條件分類

| 類型 | 說明 | 典型用途 |
|------|------|----------|
| RTC | 絕對時間觸發 | 鬧鐘、提醒 |
| RTC_WAKEUP | 絕對時間喚醒裝置 | 重要通知 |
| ELAPSED_REALTIME | 相對時間觸發 | 定期檢查 |
| ELAPSED_REALTIME_WAKEUP | 相對時間喚醒 | 背景任務 |

### 按重複模式分類

**單次 Alarm**：
- 執行一次後自動取消
- 用於特定時間的任務

**重複 Alarm**：
- 按固定間隔重複觸發
- 常見於同步、更新任務

**精確 Alarm**：
- 準確的時間觸發
- 系統資源消耗較高

## 設定選項

### 處理模式

#### 允許模式
- Alarm 正常設定和觸發
- 不進行任何干預
- 適用於重要系統功能

#### 限制模式
- 降低觸發頻率
- 合併相近的觸發時間
- 延遲非緊急 Alarm

#### 攔截模式
- 完全阻止 Alarm 設定
- 應用程式無法建立該類型 Alarm
- 可能嚴重影響應用程式功能

### 進階選項

**智慧合併**：
- 將相近時間的 Alarm 合併
- 減少裝置喚醒次數

**批次處理模式**：
- 延遲非緊急 Alarm
- 與其他任務一起執行

## 使用方法

### 檢視 Alarm 清單

1. 點擊底部"Alarms"標籤
2. 檢視目前活動的 Alarm
3. 使用篩選器檢視特定狀態

### 設定 Alarm 規則

1. 點擊目標 Alarm 項目
2. 選擇處理模式
3. 設定具體參數：
   - 最小間隔時間
   - 延遲時間
   - 批次處理選項

【需要截圖：Alarm 設定頁面】

### 批量管理

**按應用程式批量設定**：
1. 篩選特定應用程式的 Alarm
2. 選擇批量操作
3. 應用統一規則

**按類型批量設定**：
- 所有 WAKEUP 類型限制
- 所有重複 Alarm 降頻
- 系統 Alarm 謹慎處理

## 實際應用

### 問題識別

#### 異常 Alarm 特徵

**高頻觸發**：
- 間隔小於 1 分鐘的重複 Alarm
- 深夜時段頻繁觸發
- 裝置靜止時仍在執行

## 技術實現

### Hook 機制

攔截 AlarmManagerService 的關鍵方法：
```kotlin
// 系統 Alarm 設定呼叫
setImpl(
    int type,
    long triggerAtTime,
    long windowLength,
    long interval,
    PendingIntent operation,
    IAlarmListener directReceiver,
    String listenerTag,
    WorkSource workSource,
    AlarmManager.AlarmClockInfo alarmClock,
    int callingUid,
    String callingPackage
)

// Alarm 觸發處理
triggerAlarmsLocked(ArrayList<Alarm> triggerList)
```

### 資料處理

**即時處理**：
- Alarm 設定時的規則檢查
- 觸發時的頻率控制
- 動態調整觸發時間

**歷史記錄**：
- 資料庫儲存觸發歷史
- 統計分析和走勢計算
- 自動清理過期資料

### 相容性

**版本支援**：
- Android 7.0+ 完整支援
- 不同版本的 API 適配
- 降級相容策略

**效能優化**：
- 最小化 Hook 開銷
- 高效的規則匹配演算法
- 異步處理統計資料

## 相關功能

- [應用程式管理](app-management.md) - 檢視應用程式的所有 Alarm
- [WakeLock 管理](wakelocks.md) - 配合 WakeLock 優化
- [規則系統](rules-regex.md) - 使用正則表示式批量設定

!!! tip "優化建議"
    Alarm 優化效果明顯，但需要平衡功能性。建議從非關鍵應用程式開始，逐步調整重要應用程式的設定。

!!! warning "注意事項"
    過度限制系統 Alarm 可能影響裝置正常功能，如自動時間同步、系統更新檢查等。