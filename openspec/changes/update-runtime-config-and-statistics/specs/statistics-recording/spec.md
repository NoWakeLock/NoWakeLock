## ADDED Requirements
### Requirement: Bounded background statistics processing
Hook event submission SHALL be bounded and SHALL NOT wait on a mutex, Binder call or database operation. A background consumer SHALL preserve event ordering and batch writes. Overload SHALL be observable. Existing statistic meaning and retention boundaries SHALL remain unchanged.

#### Scenario: Statistics are cleared with pending events
- **WHEN** statistics are cleared while older events remain queued
- **THEN** those older events cannot restore cleared statistics after the clear completes.

#### Scenario: No foreground application
- **WHEN** the application is not running
- **THEN** recording continues in the system runtime and the application can query the recorded results when opened later.
