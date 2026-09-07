# Change: Atomic configuration activation and batched statistics

## Why
NWL-18 reproduces stale system_server rules on stock Vector API102. Per-event Provider calls also add unnecessary work to statistics recording.

## What Changes
- NWL-19: immutable snapshots, API102 authenticated push and activation acknowledgement, official preferences for persistence; legacy configuration transport retained with background snapshot refresh.
- NWL-20: bounded non-waiting event submission, ordered background processing and database batching, clear-generation barrier.
- User approved implementation on 2026-09-07 and requested completion plus API102 device tests.

## Impact
- Configuration readers, existing Provider communication, statistics recorder and tests.
- No Vector modification, no new Binder discovery protocol, no app background service, no expanded statistics retention feature.
