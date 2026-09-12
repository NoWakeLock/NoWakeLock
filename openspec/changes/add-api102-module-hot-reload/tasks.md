## 1. Investigation and review
- [x] 1.1 Verify official API/service 102 contracts and identify current lifecycle gaps.
- [x] 1.2 Review and approve this implementation proposal (user approved 2026-09-12; any required device reboot must be announced and performed manually by the user after agreement).
- [x] 1.3 Prove the bounded statistics/in-flight handover and failure semantics with an isolated two-generation harness before coding automatic reload (queue/provider/timeout tests and minified ART A-B-C fixture passed; automatic metadata remains disabled).

## 2. Implementation
- [x] 2.1 Extend the dual entry and ABI stubs with isolated API102 lifecycle dispatch; validate old runtime class loading (ART old two-argument constructor and package callback passed without API102 callback types).
- [x] 2.2 Implement runtime ownership, neutral state transfer, worker/listener retirement, and provider reopen without clearing statistics.
- [x] 2.3 Replace modern hooks through stable IDs/handles, restore readiness/loaders, and remove explicitly identified obsolete hooks; unmatched discoveries fail conservatively.
- [x] 2.4 Add service-triggered reload and per-target generation/result reporting; keep rule synchronization separate (host tests passed; real framework callbacks remain part of device acceptance).
- [ ] 2.5 Enable APK-update auto reload after controlled lifecycle tests pass.

## 3. Acceptance and documentation
- [ ] 3.1 Cover refusal/failure, late events, active and overlapping wakelocks, interval history, concurrent rule updates, repeated reloads, and worker/listener leaks.
- [ ] 3.2 Bootstrap the first capable generation, then verify two successive release APK updates on API102 with stable system PID/boot ID and changed executing build identity.
  - Bootstrap completed after the user's manual reboot: OP13 system/provider PID 3377 both execute A2 with ready=true and no lifecycle failure. B is built; A2-to-B and B-to-C remain pending an unlocked device for the official application reload action and controlled active-lock acceptance.
- [ ] 3.3 Verify real-user and Probe blocking for users 0/10, exact event deltas, and operation with the application stopped.
- [ ] 3.4 Run old-runtime regression and relevant host tests; record unsupported cases honestly.
- [x] 3.5 Update constraints, delivery checklist, user docs, Plane evidence and OpenSpec acceptance state for bootstrap delivery; append real system acceptance evidence after the remaining device tests.

2026-09-12 preparation: 113 host tests passed; final minified A2 release built and installed; independent ART fixture passed legacy ABI and API102 A-B-C lifecycle/old-classloader collection. A2 includes regression-tested prevention of clearing provider statistics on an app read/cache error. Documentation and constraints updated. Normal device bootstrap by the user's manual reboot, actual system A-B-C, legacy device regression and automatic-update acceptance remain open. See `doc/NWL-22_API102_Hot_Reload_2026-09-12.zh-CN.md`.

2026-09-12 post-reboot: normal bootstrap is now verified; real WeChat user 10 blocking continues, rules match, and queue pending/dropped/failed are zero. Probe user 0/10 count isolation passed, but locked-screen background locks were DISABLED by Android, so effective-hold and full multi-user behavior acceptance remain open. No automatic metadata enabled, no reboot command issued.
