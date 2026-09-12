# Change: API 102 module code hot reload

Plane task: NWL-22.

## Why
Users need APK updates to replace NoWakeLock's running system hooks without rebooting. Live rule publication already works after NWL-21, but the existing entry does not implement the official hot reload lifecycle. Merely installing an APK or seeing a new rule revision does not demonstrate replacement of running code.

The user approved this implementation proposal on 2026-09-12 and requested updated constraints and documentation. Any required device reboot must be announced first and performed manually by the user after agreement.

## What Changes
- Add the official API 102 retirement and reload callbacks while preserving the fixed old framework entry and legacy API isolation.
- Retain stable hook IDs, replace callbacks through official handles, and reinstall required system/provider integration explicitly.
- Transfer versioned classloader-neutral state, retire old workers/listeners, and preserve statistics, active wakelock accounting, time-window history, and current rules.
- Enable APK-update automatic reload only after the lifecycle passes controlled device tests; provide an application-side retry and truthful per-target status using official service APIs.
- Keep rule publication independent of code reload. Never reboot automatically as a reload fallback.
- Update delivery checks, operational constraints, and user documentation.

## Impact
- Affected specs: new `module-hot-reload`, existing module health and configuration concepts remain compatible.
- Affected code: `xposed-entry`, `ModernXposedModule`, modern hook registration, `XpRecord`, `XProvider`, config reader listeners, wakelock/time-window state, service manager and module-check UI.
- Initial migration: the currently loaded implementation inherits `onHotReloading=false`. It cannot bootstrap its own missing callback. The first capable version requires a normal target start (normally one reboot for system_server); later capable-to-capable updates are the no-reboot acceptance scenario.
- No changes to Vector; no general legacy hot-reload promise. OP13's current NWL-21 fix is already effective without a reboot.
