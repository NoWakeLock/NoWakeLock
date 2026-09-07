## ADDED Requirements
### Requirement: Atomic runtime rule activation
The module SHALL publish a complete immutable configuration snapshot without requiring a Hook reader to acquire a mutex or perform I/O. API102 activation SHALL acknowledge the actual system runtime revision through the existing Provider; official remote preferences SHALL remain the durable bootstrap source.

#### Scenario: Update overlaps a decision
- **WHEN** a rule update arrives while a Hook decision is running
- **THEN** the running decision uses its captured snapshot and subsequent decisions use the newly published snapshot after acknowledgement.

#### Scenario: Application exits
- **WHEN** the application is force-stopped after successful activation
- **THEN** system rules continue operating and a later system restart restores the persisted rules without launching the application.

#### Scenario: Legacy framework
- **WHEN** the legacy framework supplies configuration
- **THEN** its existing transport remains in use and completes refresh outside the Hook decision path.
