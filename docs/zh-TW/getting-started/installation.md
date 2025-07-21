# 安裝指南

!!! danger "⚠️ 救援模式 - 最重要！"
    **如果安裝後裝置啟動異常、卡死或無限重啟**：
    
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

## 前置要求

### 系統要求
- Android 7.0 (API 24) 或更高版本

!!! error "裝置相容性限制"
    **三星裝置 OneUI 目前尚不支援**
    
    由於 OneUI 更改了 Android 原始碼，Hook 位置經過多種方法嘗試，始終無法生效。我們正在研究解決方案，但目前無法在三星 OneUI 裝置上正常工作。
    
    其他廠商的 Android 裝置通常可以正常使用。

### Xposed 框架
安裝以下框架之一：

| 框架 | 適用版本 | 推薦程度 |
|------|----------|----------|
| LSPosed | Android 8.1+ | ⭐⭐⭐⭐⭐ |
| EdXposed | Android 8.0-11 | ⭐⭐⭐ |

!!! info "框架選擇"
    推薦使用 LSPosed，相容性和穩定性更好。

## 下載 NoWakeLock

### 官方管道

[![GitHub](https://img.shields.io/badge/GitHub-Releases-blue)](https://github.com/NoWakeLock/NoWakeLock/releases)
[![IzzyOnDroid](https://img.shields.io/badge/IzzyOnDroid-F-Droid-green)](https://apt.izzysoft.de/fdroid/index/apk/com.js.nowakelock)

**下載方式**：
- **GitHub Releases** - 直接下載 APK 檔案
- **IzzyOnDroid** - 在 F-Droid 中新增 IzzyOnDroid 源後安裝
- **F-Droid 官方** - 計劃中

!!! tip "F-Droid 源設定"
    要透過 IzzyOnDroid 安裝：
    1. 在 F-Droid 應用程式中新增源：`https://apt.izzysoft.de/fdroid/repo`
    2. 搜尋 NoWakeLock 進行安裝

### 版本選擇

- **穩定版** - 從 GitHub Releases 或 IzzyOnDroid 下載
- **測試版** - 從 dev 分支建置

!!! warning "僅支援官方版本"
    僅對官方管道下載的版本提供支援。

## 安裝步驟

### 1. 下載 APK
從官方管道下載最新版本的 APK 檔案。

### 2. 安裝應用程式
```bash
# 使用 ADB 安裝（可選）
adb install nowakelock-v3.x.x.apk
```

或直接在裝置上安裝 APK 檔案。

【需要截圖：安裝介面】

### 3. 啟用模組
1. 開啟 Xposed 管理器 (LSPosed/EdXposed)
2. 進入"模組"頁面
3. 勾選 NoWakeLock
4. 重啟裝置

【需要截圖：LSPosed 模組清單】

### 4. 設定作用域
在 LSPosed 中設定模組作用域：

**必需的作用域**：
- `android` (系統框架)

!!! tip "作用域說明"
    NoWakeLock 只需要 `android` 系統框架作用域即可正常工作。

【需要截圖：作用域設定】

## 驗證安裝

### 檢查模組狀態
1. 開啟 NoWakeLock 應用程式
2. 進入"模組檢查"頁面
3. 確認所有項目顯示綠色✅

【需要截圖：模組檢查頁面】

### 驗證項目

| 檢查項目 | 說明 |
|----------|------|
| Xposed 框架啟用 | 框架正常執行 |
| 模組已載入 | NoWakeLock 模組被識別 |
| Hook 正常工作 | 系統呼叫攔截成功 |
| 設定讀取成功 | 應用程式可以讀取設定 |

### 測試功能
1. 檢視"應用程式"頁面是否顯示已安裝應用程式
2. 檢查"WakeLocks"頁面是否有資料
3. 嘗試設定一個簡單規則

## 常見問題

### 模組未啟用
**症狀**: 模組檢查顯示❌  
**解決方案**:
1. 確認 Xposed 框架正常執行
2. 檢查模組是否被勾選
3. 重啟裝置後再次檢查

### Hook 不工作
**症狀**: 沒有 WakeLock/Alarm 資料  
**解決方案**:
1. 確認作用域包含 `android`
2. 檢查 SELinux 策略
3. 查看 Xposed 記錄

### 應用程式當機
**症狀**: 開啟應用程式立即崩潰  
**解決方案**:
1. 檢查 Android 版本相容性
2. 清除應用程式資料
3. 重新安裝模組

## 卸載模組

### 完整卸載步驟
1. 在 Xposed 管理器中取消勾選模組
2. 重啟裝置
3. 卸載 NoWakeLock 應用程式

## 下一步

安裝完成後：

1. [快速開始](quick-start.md) - 5 分鐘上手設定
2. [模組檢查](module-check.md) - 詳細驗證模組狀態
3. [WakeLock 管理](../features/wakelocks.md) - 開始管理 WakeLock