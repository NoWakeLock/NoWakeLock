# Review Findings (S1/S2 Only)

> 只记录最严重的问题；不记录命名/风格/架构建议。

## Severity
- S1: crash/bootloop/模块不可用、数据损坏、明显安全/隐私问题
- S2: 明显错误且容易触发，可能导致过度拦截或功能失效

## Findings
| ID | Sev | Area | Location | Symptom | Repro | Fix (Minimal) | Test/Verification |
|---|---|---|---|---|---|---|---|
| NWL-CR-001 | S1 | Hook IPC / Security | `app/src/main/java/com/js/nowakelock/xposedhook/hook/SettingsProviderHook.kt:43`, `app/src/main/java/com/js/nowakelock/data/provider/XProvider.kt:27`, `app/src/main/java/com/js/nowakelock/data/provider/XProvider.kt:84`, `app/src/main/java/com/js/nowakelock/data/provider/XProvider.kt:273`, `app/src/main/java/com/js/nowakelock/data/provider/XProvider.kt:344` | `SettingsProvider.call()` 被 Hook 后，仅凭 `method == "NoWakelock"` 就转发到 `XProvider`，没有任何 caller UID/package 校验。与此同时，`XProvider` 暴露了 `LoadInfos` / `LoadEvents` / `ClearData` 等读写能力。结果是：能打到 `Settings.System.CONTENT_URI` 的外部调用方，理论上可直接读取事件数据或清空统计。 | 写一个独立测试 app，调用 `contentResolver.call(Settings.System.CONTENT_URI, "NoWakelock", "LoadEvents", Bundle())` 或 `... "ClearData" ...`。当前代码路径没有拒绝分支。 | 在 Hook 层或 `XProvider.getMethod()` 前增加 caller 校验：只允许本 app UID/package（必要时允许 system UID）；其他调用直接 `SecurityException` 或透传原始 SettingsProvider。 | 负向 instrumentation / 手工验证：外部 app 调用必须失败；本 app 调用仍成功。 |
| NWL-CR-002 | S2 | Blocking Logic | `app/src/main/java/com/js/nowakelock/xposedhook/hook/WakelockHook.kt:33`, `app/src/main/java/com/js/nowakelock/xposedhook/hook/WakelockHook.kt:605`, `app/src/main/java/com/js/nowakelock/xposedhook/hook/WakelockHook.kt:616`, `app/src/main/java/com/js/nowakelock/xposedhook/hook/AlarmHook.kt:29`, `app/src/main/java/com/js/nowakelock/xposedhook/hook/AlarmHook.kt:317`, `app/src/main/java/com/js/nowakelock/xposedhook/hook/AlarmHook.kt:334` | Wakelock / Alarm 的 `lastAllowTime` 只按 `name` 记忆，不带 `packageName` / `userId`。这会让不同 app 但同名项目共享最小间隔状态，导致跨应用串扰。 | 准备两个包名不同、但 emit 同名 wakelock/alarm 的 app。先让 A 触发一次，再在间隔窗口内让 B 触发；B 会错误复用 A 的 `lastAllowTime`。 | 将 `lastAllowTime` 的 key 改为 `(name, packageName, userId)` 复合键；与现有规则查找维度保持一致。 | 单测覆盖：同名不同包不得互相影响；同包同名仍按窗口生效。 |
| NWL-CR-003 | S2 | Service Blocking | `app/src/main/java/com/js/nowakelock/xposedhook/hook/ServiceHook.kt:440` | Service 域的屏幕关闭拦截规则没有真正生效。`block()` 第二个分支重复调用了 `xpNSP.flag(...)`，而不是 `xpNSP.flagLock(...)`，导致 `screenOffBlock` 配置永远不会被读取。 | 给某个 Service 只配置“仅灭屏拦截”，灭屏后启动 Service；代码不会走 `flagLock` 路径。 | 将第二个条件改为 `isLocked && xpNSP.flagLock(...)`。 | 单测覆盖 `block()`：full-block、screen-off-only、no-rule 三种场景。 |
| NWL-CR-004 | S2 | Statistics Integrity | `app/src/main/java/com/js/nowakelock/data/db/entity/Info.kt:9`, `app/src/main/java/com/js/nowakelock/data/db/dao/InfoDao.kt:32`, `app/src/main/java/com/js/nowakelock/data/db/dao/InfoDao.kt:35`, `app/src/main/java/com/js/nowakelock/data/provider/XProvider.kt:145`, `app/src/main/java/com/js/nowakelock/data/provider/XProvider.kt:150`, `app/src/main/java/com/js/nowakelock/data/provider/XProvider.kt:170` | 统计表 `Info` 的主键与更新条件都不包含 `packageName`，只有 `(name, type, userId)`。同一 user 下，只要两个 app 产生了同名项，统计就会合并到同一行，导致按 app 维度展示的数据错误。 | 两个不同包发出同名 wakelock/service/alarm。第一次会插入带 `packageName=A` 的 `Info`；第二次会命中同一主键并更新 A 的统计，而不是建立 B 自己的统计行。 | 将 `packageName_info` 纳入主键和所有 `loadInfo/upCount/upBlockCount/upCountTime` 查询条件；必要时做一次定向 schema 迁移或可接受的数据重建。 | 单测 / migration test：同名不同包应产生两条独立统计。 |

## Triage Status (2026-03-08)
- `NWL-CR-001`: Deferred (accepted risk for now).
- `NWL-CR-002`: Fixed; Xposed-related automated test removed, verify on real device / manual smoke.
- `NWL-CR-003`: Fixed; Xposed-related automated test removed, verify on real device / manual smoke.
- `NWL-CR-004`: Deferred.

## Notes
- 本轮没有把“命名不好”“结构不优雅”“可读性一般”之类问题记为 findings。
- 只对确认的 S1/S2 做了最小修复与回归测试；未做架构重构。
