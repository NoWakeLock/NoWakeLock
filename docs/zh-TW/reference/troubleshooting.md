# 故障排除

系統性的問題診斷和解決指南。

## 診斷流程

### 第一步：基礎檢查

#### 模組狀態驗證
1. 開啟 NoWakeLock → "模組檢查"
2. 確認所有項目顯示 ✅
3. 如有 ❌ 項目，按提示處理

【需要截圖：模組檢查失敗示例】

#### Xposed 框架檢查
```bash
# 檢查 LSPosed 狀態
adb shell am start -n org.lsposed.manager/.ui.activity.MainActivity

# 檢視模組清單
adb shell pm list packages | grep nowakelock
```

#### 基礎權限檢查
- 儲存權限
- 查詢所有應用程式權限（Android 11+）
- 無障礙服務權限（如需要）

### 第二步：功能測試

#### WakeLock 測試
1. 設定一個簡單的 WakeLock 限制規則
2. 開啟對應應用程式觸發 WakeLock
3. 檢查統計頁面是否有攔截記錄

#### 規則生效測試
1. 建立測試規則：攔截特定 WakeLock
2. 觀察目標應用程式行為變化
3. 檢視事件記錄確認規則執行

## 常見問題分類

### 安裝問題

#### 模組無法載入
**症狀**：模組檢查顯示"模組未載入"

**診斷步驟**：
1. 確認 Xposed 框架正常執行
2. 檢查模組是否在管理器中勾選
3. 驗證應用程式簽名是否正確

**解決方案**：
```bash
# 重新安裝模組
adb uninstall com.js.nowakelock
adb install nowakelock.apk

# 清除框架快取
# 在 LSPosed 中："設定" → "清除快取"
```

#### Hook 功能失效
**症狀**：模組載入但 Hook 不工作

**可能原因**：
- 系統版本不相容
- 作用域設定錯誤
- SELinux 策略限制

**解決方案**：
1. 確認作用域包含 `android`
2. 檢查 SELinux 狀態：
   ```bash
   adb shell getenforce
   # 如果是 Enforcing，可能影響 Hook 功能
   ```
3. 檢視 Xposed 記錄：
   ```bash
   adb logcat | grep -E "(Xposed|nowakelock)"
   ```

### 功能問題

#### 規則不生效
**症狀**：設定規則後沒有攔截效果

**檢查清單**：
- [ ] 規則是否啟用
- [ ] 比對條件是否正確
- [ ] 目標應用程式是否重啟
- [ ] 是否有衝突的規則

**偵錯方法**：
1. 使用簡單的精確比對測試
2. 檢查規則優先級
3. 檢視比對記錄

#### 應用程式功能異常
**症狀**：設定規則後應用程式無法正常工作

**立即處理**：
1. 停用相關規則
2. 重啟問題應用程式
3. 逐步恢復規則

**根本解決**：
1. 分析應用程式依賴的關鍵元件
2. 調整規則範圍或參數
3. 使用"限制"替代"攔截"

#### 統計資料異常
**症狀**：統計資料顯示異常或不更新

**檢查項目**：
1. 資料庫狀態
   ```bash
   adb shell ls -la /data/data/com.js.nowakelock/databases/
   ```
2. 儲存空間
   ```bash
   adb shell df /data
   ```
3. 應用程式權限

**修復方法**：
```bash
# 清除資料庫（注意：會遺失歷史資料）
adb shell pm clear com.js.nowakelock
```

### 效能問題

#### 系統卡頓
**症狀**：安裝 NoWakeLock 後系統回應變慢

**效能分析**：
```bash
# CPU 使用率
adb shell top | grep nowakelock

# 記憶體使用
adb shell dumpsys meminfo com.js.nowakelock
```

**優化方案**：
1. 減少規則數量
2. 簡化正規表示式
3. 調整統計頻率

#### 電量消耗增加
**症狀**：模組本身消耗電量

**診斷方法**：
1. 檢查背景活動
   ```bash
   adb shell dumpsys battery
   ```
2. 分析 WakeLock 使用
   ```bash
   adb shell dumpsys power | grep nowakelock
   ```

**解決方案**：
- 檢查是否有異常的循環任務
- 優化資料庫查詢頻率
- 確認沒有記憶體洩漏

### 相容性問題

#### 特定應用程式衝突
**症狀**：某些應用程式與 NoWakeLock 衝突

**識別方法**：
1. 系統記錄分析
2. 應用程式當機報告
3. ANR（Application Not Responding）記錄

**處理策略**：
```yaml
臨時解決:
  - 將應用程式加入白名單
  - 停用相關規則

長期解決:
  - 分析衝突原因
  - 調整 Hook 策略
  - 更新相容性程式碼
```

#### 系統版本相容
**症狀**：新版本 Android 上功能異常

**適配檢查**：
1. API 變更分析
2. 權限模型變化
3. 安全策略更新

**降級方案**：
- 停用不相容的功能
- 使用替代實作
- 等待版本更新

## 記錄分析

### 收集記錄

#### 系統記錄
```bash
# 完整記錄
adb logcat -v time > full_log.txt

# NoWakeLock 相關
adb logcat | grep -i nowakelock > nowakelock_log.txt

# Xposed 相關
adb logcat | grep -i xposed > xposed_log.txt
```

#### 應用程式記錄
```bash
# 特定程序記錄
adb logcat --pid=$(adb shell pidof com.js.nowakelock)

# 當機記錄
adb logcat | grep -E "(FATAL|AndroidRuntime)"
```

### 記錄分析

#### 關鍵錯誤識別
```
E/Xposed: Hook failed
E/NoWakeLock: Database error
W/ActivityManager: Unable to start service
```

#### 效能問題識別
```
W/Choreographer: Skipped frames
I/Timeline: Timeline: Activity_idle
W/InputDispatcher: Application is not responding
```

### 記錄清理
```bash
# 清除記錄
adb logcat -c

# 設定記錄級別
adb shell setprop log.tag.NoWakeLock VERBOSE
```

## 資料恢復

### 設定備份
```bash
# 備份設定
adb backup -f backup.ab com.js.nowakelock

# 提取資料庫
adb shell cp /data/data/com.js.nowakelock/databases/app_database /sdcard/
adb pull /sdcard/app_database ./
```

### 設定恢復
```bash
# 恢復備份
adb restore backup.ab

# 手動恢復資料庫
adb push ./app_database /sdcard/
adb shell cp /sdcard/app_database /data/data/com.js.nowakelock/databases/
```

### 重設選項

#### 軟重設（保留設定）
1. 應用程式設定 → 清除快取
2. 重啟應用程式

#### 硬重設（清除所有資料）
```bash
adb shell pm clear com.js.nowakelock
```

#### 完全重設（重新安裝）
```bash
adb uninstall com.js.nowakelock
# 重新安裝和設定
```

## 進階偵錯

### Hook 偵錯

#### 啟用詳細記錄
在應用程式設定中開啟"偵錯模式"，會輸出詳細的 Hook 資訊。

#### Hook 測試工具
```kotlin
// 測試特定 Hook 點
fun testWakeLockHook() {
    // 手動觸發 WakeLock 獲取
    // 觀察 Hook 是否被呼叫
}
```

### 效能分析

#### CPU 分析
```bash
# 效能監控
adb shell am start -n com.android.shell/.BugreportStorageProvider

# 執行緒分析
adb shell ps -T | grep nowakelock
```

#### 記憶體分析
```bash
# 記憶體詳情
adb shell dumpsys meminfo com.js.nowakelock

# 記憶體洩漏檢測
adb shell am dumpheap com.js.nowakelock /sdcard/heap.hprof
```

### 資料庫偵錯

#### 資料庫檢查
```sql
-- 連接資料庫
sqlite3 app_database

-- 檢查表結構
.schema

-- 檢視資料
SELECT * FROM app_info LIMIT 10;
SELECT * FROM wakelock_info LIMIT 10;
```

#### 資料一致性檢查
```sql
-- 檢查孤立記錄
SELECT * FROM events WHERE app_id NOT IN (SELECT id FROM apps);

-- 統計資料驗證
SELECT package_name, COUNT(*) FROM events GROUP BY package_name;
```

## 預防措施

### 定期維護

#### 每週檢查
- 模組狀態驗證
- 規則效果評估
- 效能指標監控

#### 每月維護
- 清理歷史資料
- 更新規則設定
- 備份重要設定

### 監控設定

#### 效能監控
設定效能閾值，超出時自動告警：
- CPU 使用率 > 5%
- 記憶體使用 > 100MB
- 資料庫大小 > 500MB

#### 功能監控
定期測試關鍵功能：
- 規則比對準確性
- 統計資料完整性
- 應用程式功能正常性

## 專業支援

### 社群支援
- **Telegram**: [@nowakelock](https://t.me/nowakelock)
- **Discord**: [NoWakelock Community](https://discord.gg/kewmG5AShQ)
- **GitHub**: [Issues](https://github.com/NoWakeLock/NoWakeLock/issues)

### 問題報告範本
```markdown
## 環境資訊
- 裝置: [品牌 型號]
- Android版本: [版本號]
- Xposed框架: [LSPosed/EdXposed 版本]
- NoWakeLock版本: [版本號]

## 問題描述
[詳細描述問題現象]

## 重現步驟
1. [步驟一]
2. [步驟二]
3. [問題出現]

## 預期結果
[期望的正常行為]

## 實際結果
[實際發生的異常行為]

## 相關記錄
```log
[貼上相關記錄]
```

## 其他資訊
[任何其他相關資訊]
```

!!! warning "資料安全"
    進行故障排除時，務必先備份重要設定。某些操作可能導致資料遺失。

!!! tip "偵錯建議"
    複雜問題建議逐步排查，從最簡單的設定開始，逐步增加複雜度，便於定位問題根源。