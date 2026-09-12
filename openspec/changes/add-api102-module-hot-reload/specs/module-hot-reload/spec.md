## ADDED Requirements

### Requirement: Official API102 Code Replacement
On a capable API102 framework, a reload-capable NoWakeLock generation SHALL support replacement by a newer compatible generation through the official module lifecycle without restarting its target process. Legacy entries SHALL remain isolated from API102-only linkage.

#### Scenario: Capable APK update
- **WHEN** a reload-capable generation receives a compatible APK update
- **THEN** the new generation installs or replaces the required hooks explicitly
- **AND** target PID and boot ID remain unchanged while executing build identity changes

#### Scenario: First migration or unsupported framework
- **WHEN** the running generation lacks hot-reload support or the framework refuses the operation
- **THEN** the application reports that new code is not active and explains the normal target-start requirement
- **AND** it does not automatically reboot or modify the framework

### Requirement: State and Resource Continuity
Reload SHALL preserve current rules, Android-user isolation, interval history, statistics and active wakelock accounting. Retired workers, callbacks and module-owned resource references SHALL be released through a bounded lifecycle. Hook decisions SHALL retain their current absence of storage I/O and mutex acquisition.

#### Scenario: Event crosses replacement
- **WHEN** an acquire or other in-flight event begins under the old generation and completes after replacement
- **THEN** the resulting event ordering and duration remain correct without duplicate accounting

#### Scenario: Provider and worker replacement
- **WHEN** a new generation takes ownership of statistics
- **THEN** existing rows are preserved, queued and late events have a defined owner, and the old worker terminates

#### Scenario: Unsafe retirement
- **WHEN** state or resource readiness cannot be validated before retirement
- **THEN** reload is refused without reporting success or leaving the old runtime disabled

### Requirement: Verified Per-Target Results
The application SHALL distinguish configuration activation from code replacement, and report asynchronous reload results plus loaded generation identity for all required targets. Reload SHALL not require the application to remain resident.

#### Scenario: Partial result
- **WHEN** one required target reloads and another fails or remains on old code
- **THEN** overall code-update status remains incomplete and identifies the affected target

#### Scenario: App exits after success
- **WHEN** all required targets have verified the new generation and the application exits
- **THEN** hooks and current rules remain effective for primary and work-profile users

#### Scenario: Same versionCode test builds
- **WHEN** two test APKs use the same versionCode
- **THEN** acceptance verifies a distinct executing build identity rather than inferring success from versionCode or rule revision alone
