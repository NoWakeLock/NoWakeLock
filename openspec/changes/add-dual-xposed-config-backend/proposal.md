# Change: Single APK for LSPosed 1.11.0 and stable API102

## Why
NoWakeLock's working legacy behavior must coexist with a pure API102 implementation in one APK. A nonempty Modern entry list takes precedence in the specified old framework; keeping two metadata files alone is insufficient. Existing configuration, installation and diagnostic code also contains incomplete failure handling.

## What Changes
- **BREAKING**: Modern runtime support targets stable API102, with a narrow compatibility adapter for JingMatrix LSPosed 1.11.0 (7209); no promise for every intermediate API.
- Complete Modern hook, snapshot publication, per-method installation and diagnostic behavior.
- Preserve legacy business semantics and existing SettingsProvider event transport.
- Validate old framework on K30SU after an API102 candidate is built; API102 device acceptance follows when supplied.
- Upgrade compile SDK/build tooling as required by official service102 AAR metadata, retaining minSdk24 and targetSdk35.

## Impact
- Affected specs: configuration-management, module-health.
- User approved this execution order on 2026-09-07. No hot reload, dynamic unload, new APK flavor or automatic commit/push.
- Plane NWL-5 through NWL-11 track evidence and acceptance separately.
- Fixed references: doc/Xposed_Implementation_Reference_Audit_2026-09-07.zh-CN.md; source archives in tmp/xposed-reference.
