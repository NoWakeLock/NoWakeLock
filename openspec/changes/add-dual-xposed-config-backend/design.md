# Design: API102 candidate followed by specified legacy compatibility

## Context
The approved targets are K30SU/JingMatrix LSPosed1.11.0(7209) and stable API102. The old framework selects the old Modern loader for a dual-entry APK. Both constructor ABI and configuration environment require explicit adaptation; do not infer fallback from minApi metadata.

## Decisions
- Reuse current working-tree implementation and business semantics, one applicationId and APK.
- First build against official api/service102.0.0, min102/target102 candidate. After the specified old ABI bridge exists, declare its truthful minimum (100) and retain target102.
- The compatibility bridge is compileOnly and never packaged as framework API. Old two-argument construction/old system callback and new no-argument lifecycle dispatch to isolated adapters.
- Modern scope includes system and android; SettingsProvider receives its package callback in the scoped system UID process. Device checks must confirm actual dispatch.
- Room remains rule source of truth. Publication takes a mutex and reads current data; publish complete per-backend snapshots in one commit with a revision. Remove only NoWakeLock-owned keys. Debug/revisions use a private app preference store.
- Backup retains merge semantics. Persisted data and successful framework publication are separate results.
- Hook registration is keyed by actual Executable, records installation outcome and permits failed installs to retry. Runtime parameter caches are per method.
- Remote reader reports observed revision rather than assuming an object is synchronized. Diagnostic reports identify PID, observation time and actual installation outcomes; framework running targets are separate evidence.
- Old API100 service handshake may use wire-compatible remote preference methods, but must not call API102 properties or scope methods on the old service.
- No hot reload, detach, or arbitrary transitional API support.

## Verification
Use offline Gradle tests/builds with local bootstrap. Verify release metadata, DEX constructors, no packaged API classes and modern call-path isolation. K30SU tests follow host tests. API102 physical validation is explicitly pending until user supplies the environment.

## Build requirement discovered during implementation
Official service/interface102 AARs require compileSdk37. Use AGP9.1.1 with Gradle9.3.1 and JDK17, preserving minSdk24 and targetSdk35. Any necessary plugin migration is build compatibility work, not a runtime support expansion.
