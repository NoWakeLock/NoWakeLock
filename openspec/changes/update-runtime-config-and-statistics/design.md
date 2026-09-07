## Context
Stock Vector API102 can retain stale system-server remote preferences after application writes. Statistics currently share the existing hooked SettingsProvider with the application's queries.

## Decisions
- Keep official Remote Preferences as durable bootstrap data; add authenticated snapshot push through the existing Provider only for API102. A matching runtime revision is the activation acknowledgement.
- Build and validate immutable rules on the writer. Each Hook decision captures one reference. Legacy file reads move to an activity-triggered worker; old remote notifications retain their transport.
- Retry an unacknowledged API102 push at most three times, reusing the exact revision, with two coroutine delays (100ms and 300ms). No recurring background retry job is added. Framework reconnection and later application publications remain independent recovery triggers.
- Bound statistics at 4096 queued events. A single background worker performs ordered transactions of at most256 events. Attach the local database processor only from the actual Provider initialization; keep Provider calls for separate-process configurations.
- Serialize clear and database work outside Hook threads. A generation and monotonic timestamp fence prevent pre-clear events from repopulating statistics.

## Trade-offs and validation limits
- Existing decisions may finish with their captured old snapshot. Successful ACK guarantees activation for subsequent captures, not retroactive reevaluation of an in-flight decision.
- Queue overload rejects events explicitly. Duration tracking is invalidated after loss or transaction failure; it resumes after clearing statistics. Counts are not advertised as lossless under overload.
- There is no application background service or idle polling. APK code replacement still requires a framework/device restart.
- K30SU confirms same-process direct batching. Other ROM process arrangements retain a compatibility path but require their own device validation.
- Existing database lifecycle behavior remains unchanged, including reset on Provider initialization; this change does not add cross-reboot statistics history.

## Deployment
Install the release APK and reboot to replace loaded Hook code. Keep official Vector unchanged. Verify live allow/block/restore with the application force-stopped, cold bootstrap, and old LSPosed regression. Detailed evidence and measured limits are recorded in the NWL-19/20 validation document.
