# Execution evidence (2026-09-07 approved plan)

Historical checked lists are superseded by this evidence-based checklist.

## 1. NWL-5 Reference investigation
- [x] Verify user ZIP against official release and pin LSPosed1.11.0 commit.
- [x] Download fixed framework/API/service/MiHealth/GlassMic sources with licenses to tmp/xposed-reference.
- [x] Trace old/new loader, configuration wire protocol and scope; publish implementation decision report.

## 2. NWL-6 API102 entry and isolation
- [x] Update build toolchain and fixed official API102 dependencies.
- [x] Validate lifecycle, capabilities, scope, metadata, R8 and modern isolation.
- [x] Validate updated OpenSpec.

## 3. NWL-7 Configuration
- [x] Serialized current-data snapshot publishing with revision and owned-key cleanup.
- [x] Durable debug, reconnect publication and separate persistence/publication results.
- [x] Real reader observations and configuration failure tests.

## 4. NWL-8 Hooks and diagnostics
- [x] Per-method install state/retry and parameter caches.
- [x] Business callback semantics tests and event channel diagnostics.
- [x] Framework running targets and non-stuck module health UI.

## 5. NWL-9 Candidate
- [x] Relevant and full unit tests; debug/release builds.
- [x] Inspect APK/DEX/R8 and publish hashes/results.
- [x] Explicitly record API102 device acceptance as pending.

## 6. NWL-10 K30SU
- [x] Record working mainline baseline and preserve device configuration.
- [x] Implement specified old entry/configuration adaptation.
- [x] Validate old framework entry, rules, three hooks, event return, app exit and reboot.

## 7. NWL-11 API102 device
- [x] Obtain user-provided API102 environment (K30SU, Vector2.2/3080, confirmed API102).
- [ ] Run equivalent end-to-end checks and old-side regression after fixes.

API102 device evidence: doc/NWL-11_API102_Device_Validation_2026-09-07.zh-CN.md.
The initial live configuration failure (NWL-18) was resolved by authenticated snapshot activation and acknowledged revision checks. NWL-19/20 record the subsequent API102 rule, statistics, app-exit, reboot, listener-alarm and Service start/bind validation, plus current latency measurements. Wakelock duration fixes are also included.

Latest evidence: doc/NWL-19_20_Atomic_Config_Statistics_Validation_2026-09-07.zh-CN.md and doc/NWL-20_Current_Latency_2026-09-07.zh-CN.md. Legacy LSPosed 1.11 regression used its remote-preferences bridge; a pure legacy API82-only device is not covered. Ordinary PendingIntent broadcast-alarm delivery remains a device validation limitation, reproduced with NoWakeLock disabled, so NWL-11 is not marked fully accepted.

Initial implementation evidence remains in doc/Xposed_Implementation_Validation_2026-09-07.zh-CN.md; its pending-state notes describe that earlier candidate.
