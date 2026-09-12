# API 102 hot reload design and constraints

## Official contracts

- [API 102 lifecycle](https://github.com/libxposed/api/blob/102.0.0/api/src/main/java/io/github/libxposed/api/XposedModuleInterface.java): retirement runs in old code, reload runs in new code, ordinary startup/package callbacks are not replayed, and retirement defaults to refusal.
- [Hook replacement](https://github.com/libxposed/api/blob/102.0.0/api/src/main/java/io/github/libxposed/api/XposedInterface.java): stable IDs / `replaceHook` replace a single hook atomically. In-flight calls retain their existing chain; this is not an atomic transaction across all hooked methods.
- [Service 102](https://github.com/libxposed/service/blob/102.0.0/service/src/main/java/io/github/libxposed/service/XposedService.java): obtain targets from `getRunningTargets`, call `hotReloadModule`, and await its asynchronous result. Submitting a request is not success.

## Baseline gaps before implementation

1. `UniversalXposedEntry` and its ABI stubs lack hot-reload callbacks. Adding API 102 descriptors must be tested against the fixed old runtime; modern-only types cannot leak through the common delegate.
2. `ModernHookSupport` already assigns method-based IDs, but does not retain handles for explicit retirement/replacement accounting.
3. `ModernXposedSystemHookInstaller` sets readiness only from boot callbacks. Reload after boot must restore readiness and system/provider loaders explicitly.
4. `XpRecord` starts an immortal worker. Its queue holds module-defined Event objects. `SharedPreferencesHookConfigReader` registers a listener without a disposal path.
5. `XProvider` clears statistics on construction. Reload is not a boot and must not clear the database. Room resources, active wakelock counters, and hook interval history require explicit lifecycle ownership.

## Implementation sequence

First implement a versioned runtime state boundary and a controlled two-generation reload test. Validate refusal, draining, late in-flight events, classloader retention, and recovery before enabling automatic reload metadata.

State crossing generations must consist of framework/JDK objects with a documented schema: rule values/revision, boot state, event sequence and clear generation, counters, active wakelock instance data, and time-window timestamps keyed by package/type/user. Never pass Room instances, Kotlin collection singletons, callbacks, module-defined event objects, or worker threads. Reacquire framework resources and construct new module objects from neutral data.

The first investigation task must settle the transition between live old callbacks and the statistics consumer. Draining a queue and stopping its worker is insufficient: old in-flight callbacks can submit after the apparent drain. Prove a bounded handover that retains ordering and avoids duplicate/lost events without adding a mutex or storage operation to the hook decision path. If no safe handover can be established, refuse before destructive retirement and document the limitation; do not ship a successful-looking partial reload.

Before accepting retirement, validate state schema and resource readiness. Define bounded worker termination and listener disposal, with refusal leaving the old runtime functional. Once the framework commits a new generation, failures must be reported accurately; do not promise rollback the framework does not provide.

On reload, restore runtime state, atomically replace each matching hook, install additions, remove obsolete handles, and attach the provider without clearing statistics. Handle system_server and a separately hosted SettingsProvider as independent targets. An overall success requires all required target generations to be verified.

Implementation review clarified that obsolete handles require an explicit list of old IDs. Missing discovery is not deletion intent. A module-local lifecycle failure/ready indication must accompany framework state because a framework may have committed a generation before its reload callback fails.

The implemented handover shares a bounded neutral queue plus a Java semaphore; it does not rely on an instantaneous drain. Remote provider ingress joins that queue; an already-popped batch on first local provider attachment is processed directly to preserve acquire/release order. Provider clear remains synchronous. The provider time-normalization offset is part of transferred state.

## Update behavior

- Once validated, set `autoHotReload=true` for the API102 entry. Existing old entries keep their current behavior.
- Use official service targets and callbacks for explicit retry and diagnostics; avoid background polling or a persistent application service.
- Identify actual loaded generations. Release versionCode alone is insufficient for local test APKs that share versionCode 87: use distinguishable A/B build identities in device validation.
- Report unsupported, refused, failed, and partially updated targets distinctly. Do not mark code updated merely because configuration ACK succeeds.

## Migration and device safety

The current OP13 generation lacks the retirement callback and cannot be retrofitted through the supported API. Do not attempt to bypass that refusal or alter Vector. Establish the first capable generation through an explicitly scheduled normal target start, then prove A-to-B and B-to-C updates without changing system_server PID or boot ID.

Preserve real user rules and backups. Use controlled Probe stimuli for update-time wakelocks/alarms/service events, including Android users 0 and 10. No claim of old-runtime device compatibility without actual old-runtime validation.
