# NoWakeLock

NoWakeLock 是一個 Android Xposed 模組，用於管理裝置的 WakeLock、Alarm 和 Service 行為，幫助優化電池續航。

!!! warning "重要免責聲明與使用建議"
    **使用風險自擔，開發者不對裝置損壞承擔責任。**
    
    **重要**：如果您的裝置沒有續航問題，不建議使用此軟體。Android 11+ 的背景管理已經過大幅優化，僅在透過 BetterBatteryStats 等工具確認存在異常耗電問題時才建議使用。
    
    NoWakeLock 是針對特定問題的解決工具，而非通用優化軟體。

!!! danger "⚠️ 重要：救援模式"
    **如果裝置啟動後卡死、無限重啟或系統異常**：
    
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
    
    **預防措施**：首次使用時謹慎設定，逐步測試規則效果。

## 核心功能

- **WakeLock 管理** - 監控和控制應用程式的喚醒鎖
- **Alarm 控制** - 管理系統定時任務
- **Service 管理** - 控制背景服務啟動
- **應用程式管理** - 按應用程式檢視和設定所有元件
- **規則系統** - 支援正規表示式的彈性設定

## 快速開始

1. [安裝指南](getting-started/installation.md) - 安裝 NoWakeLock 模組
2. [問題分析](getting-started/problem-analysis.md) - 分析耗電問題（使用前必讀）
3. [快速上手](getting-started/quick-start.md) - 5 分鐘基礎設定
4. [模組檢查](getting-started/module-check.md) - 驗證模組狀態

## 主要功能

### 📱 應用程式管理
- [應用程式管理](features/app-management.md) - 按應用程式檢視和設定

### ⚡ 系統控制
- [WakeLock 管理](features/wakelocks.md) - 防止裝置休眠的鎖機制
- [Alarm 管理](features/alarms.md) - 系統定時任務控制
- [Service 管理](features/services.md) - 背景服務控制

### 🔧 設定工具
- [規則與正規表示式](features/rules-regex.md) - 彈性的比對規則
- [應用程式管理](features/app-management.md) - 按應用程式統一管理

## 使用指南

透過應用程式主介面的五個標籤頁進行操作：
- **Apps** - 應用程式清單和整體管理
- **Wakelocks** - WakeLock 監控和控制
- **Alarms** - 定時任務管理
- **Services** - 背景服務控制
- **Settings** - 全域設定和設定

## 獲取幫助

- [常見問題](reference/faq.md) - 最常遇到的問題解答
- [故障排除](reference/troubleshooting.md) - 問題診斷與解決
- [術語表](reference/glossary.md) - 技術術語說明

## 相容性

- **Android 版本**: 7.0 (API 24) 至 15.0 (API 35)
- **Xposed 框架**: LSPosed (推薦)、EdXposed
- **架構支援**: ARM64、ARM32
- **目前版本**: 3.0.3 (正式版)

!!! error "裝置相容性限制"
    **三星裝置 OneUI 目前尚不支援**
    
    由於 OneUI 更改了 Android 原始碼，Hook 位置經過多種方法嘗試，始終無法生效。其他廠商的 Android 裝置通常可以正常使用。

## 社群與支援

- **Telegram**: [@nowakelock](https://t.me/nowakelock)
- **Discord**: [NoWakelock](https://discord.gg/kewmG5AShQ)
- **GitHub**: [NoWakeLock/NoWakeLock](https://github.com/NoWakeLock/NoWakeLock)

## 開發者

對技術實現或貢獻程式碼感興趣？

- [開發者文件](developers/) - 技術架構和實作細節
- [開發環境](developers/) - 如何參與開發

---

!!! warning "使用提醒"
    NoWakeLock 需要 Xposed 框架，使用前請備份重要資料。開發者不對裝置問題承擔責任。

!!! info "許可證"
    本專案基於 [GNU General Public License v3.0](https://github.com/NoWakeLock/NoWakeLock/blob/master/LICENSE) 開源。