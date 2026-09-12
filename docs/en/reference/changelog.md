# Changelog

This document records the version update history and important changes of NoWakeLock.

## [v3.0.11 Build 89] - Test release preparation (2026-09-12)

This version is for the GitHub **Pre-release** channel before a stable rollout.

### Changes and fixes

- Keep legacy Xposed and add an isolated official API 102 runtime for wakelocks, alarms and services. Intermediate API revisions are outside the compatibility target.
- Atomically publish and confirm live rule snapshots without a reboot or resident app; batch statistics in a bounded background queue with drop/failure diagnostics.
- Add API 102 manual and APK-triggered module reload, handing over rules, statistics and active locks, and verifying executing build identities.
- Fix publication with empty regex groups, accidental statistics clearing after read/cache failures, duplicate modern Service overload counts, inflated duration after a first blocked wakelock, and legacy bindService blocking return values.

### Upgrading and validation

- Back up rules. Matching v3.x signatures allow in-place updates; switching from local debug to CI signing may require backup and reinstall.
- The initial migration from a build without reload callbacks may still need a reboot. Later code reload requires framework support; legacy APK updates keep their normal restart requirement.
- The preceding 3.0.10 / 88 candidate passed OP13 / Vector 2.2 manual/automatic reload, active lock continuity, user 0/10 allow/block/restore tests and 120 unit tests.
- The final implementation still needs legacy-device, separate-Provider, pre-unlock and some concurrent-reload coverage. Previous candidate evidence does not establish installation acceptance of the new CI artifact.
- [Full bilingual release notes and test limits](https://github.com/NoWakeLock/NoWakeLock/blob/feature/dual-xposed-config-backend/fastlane/release-notes/3.0.11.md)

---

## [v3.0.10 Build 87] - 2026-05-17

- Fix app/event list sorting and filtering flows, with unit and device sorting tests.
- Later local compatibility candidates reused 3.0.10, reaching Build 88; their changes are covered by the 3.0.11 test release above.

---


## [v3.0.9 Build 86] - 2026-03-15

### 🐛 Fixes
- Fix cross-app minimum-interval state collisions for same-name WakeLock/Alarm entries
- Fix the Service-domain screen-off-only blocking rule not taking effect
- Fix timeout field clipping in the DA control section on narrow widths

### 🧪 Testing & Quality
- Add unit tests for `calculateTime`, backup JSON, preferences mapping, and wakelock counter/registry
- Add instrumentation coverage for MainActivity, `XProvider`, and narrow-width layouts
- Add connected-device automation scripts and validation docs

---
## [v3.0.8 Build 85] - Unreleased

### 🐛 Fixes
- Fix crash in multi-user app list on Android 16
- Improve Samsung/OneUI compatibility (OEM fallback + Nest accessor support for hooks)
- Remove `com.android.settings.provider` from the recommended LSPosed scope list
- Shorten fastlane metadata `short_description` to avoid truncation/warnings

---

## [v3.0.7 Build 84] - 2025-07-26

### 🔧 Improvements
- Switch Xposed dependency to JitPack and remove deprecated artifacts

---

## [v3.0.6 Build 83] - 2025-07-26

### 🔧 Improvements
- Improve GitHub Actions release workflow (tag/version handling)

---

## [v3.0.5 Build 82] - 2025-07-25

### ✨ New Features
- Add F-Droid reproducible builds support
- Use F-Droid compatible APK naming (`NoWakeLock-%v.apk`)
- Update docs: Android compatibility up to 16

---

## [v3.0.4 Build 81] - 2025-07-21

### 🌐 New Language Support
- **German (Deutsch)** - Complete German interface support
- **Traditional Chinese** - Support for Traditional Chinese (Taiwan region)
- Language selection interface includes new language options
- Improved localized string resources

### 🔧 Improvements
- Optimized language switching experience
- Enhanced multi-language resource configuration
- Improved localized string consistency

---

## [v3.0.3 Build 80] - 2025-05-20

### 🎉 Major Update
- **First stable release of 3.x series**, completely restructured
- **Material Design 3** brand new interface design
- **Android 7-16** compatibility support
- **Multi-user support** fully implemented
- **Event tracking system** comprehensively upgraded

### ✨ New Features
- **Jetpack Compose** UI framework integration
- **Application statistics** enhanced functionality
- **Global search** feature
- **Module detection** comprehensive status checking
- **User management** multi-user environment support

### ⚠️ Compatibility Notes
- **Samsung device known issues** - Some features may be limited
- **Upgrade recommendations**:
  1. Uninstall v2.0.x version
  2. Restart device
  3. Install v3.x version
  4. Restart device

### 🔄 Configuration Migration
- **v2.0 configuration incompatible** - Requires reconfiguration
- Recommend backing up important configuration before upgrading
- Provides configuration import/export functionality

---

## [v3.0.2 Build 77] - 2025-05-16

### ✨ New Features
- **Module check functionality** - Complete module status verification
- **Detection interface optimization** - Clearer status display
- **Database migration strategy** - Improved version upgrade handling

### 🐛 Fixes
- Fixed module detection issues on some devices
- Improved database upgrade flow
- Optimized user interface response speed

---

## [v3.0.1 Build 75-76] - 2025-05-05

### 🔧 Improvements
- **Unified Hook strategy** - Service, Alarm, WakeLock processing optimization
- **Performance optimization** - Reduced system resource usage
- **Code refactoring** - Improved code quality and maintainability

### 🐛 Fixes
- Fixed compatibility issues on some Android versions
- Improved Hook stability
- Optimized memory usage

---

## [v2.0.5 Build 62-63] - 2025-03

### ✨ New Features
- **Themed startup icon** - Support for dynamic themes
- **Error handling improvements** - ContentProvider data exception handling
- **Service Hook updates** - Support for Android API 29-40

### 🔧 Improvements
- Optimized data query performance
- Improved user interface response
- Enhanced error logging

---

## v2.x Series (Historical Versions)

### Main Features
- Basic WakeLock/Alarm/Service management
- Traditional UI design
- Android 7+ basic support
- Single-user environment

### Known Limitations
- Relatively simple UI design
- Relatively basic functionality
- No multi-user environment support
- Configuration incompatible with v3.x

---

## Version Comparison

### v3.x vs v2.x Main Differences

| Feature | v2.x | v3.x |
|---------|------|------|
| UI Framework | Traditional View | Jetpack Compose |
| Design Language | Material Design 2 | Material Design 3 |
| Android Support | 7-11 | 7-16 |
| Multi-user | ❌ | ✅ |
| Module Detection | Basic | Complete |
| Performance | General | Optimized |
| Configuration Compatibility | - | Incompatible with v2.x |

### Upgrade Recommendations

#### Upgrading from v2.x
1. **Backup configuration** - Record current rule settings
2. **Complete uninstall** - Uninstall v2.x and restart
3. **Fresh installation** - Install v3.x and reconfigure
4. **Gradual configuration** - Gradually restore settings based on backup

#### New Users
- Directly install the latest v3.x version
- Follow the [Quick Start](../getting-started/quick-start.md) guide for configuration

---

## Known Issues

### v3.0.3 Known Issues
- **Samsung devices** - Some features may be unstable
- **Android 15** - Some new features may not be fully supported
- **Memory usage** - May have performance impact on low-memory devices

### Temporary Solutions
- Samsung devices recommend using conservative configuration
- Android 15 users please pay attention to subsequent updates
- Low-memory devices recommend reducing the number of rules

---

## Development Roadmap

### Short-term Plans (v3.1.x)
- Samsung device compatibility improvements
- Android 15 complete support
- Performance optimization
- More preset configuration templates

### Medium-term Plans (v3.2.x)
- Cloud configuration sync (optional)
- Richer statistical analysis
- Plugin system
- API opening

### Long-term Plans (v4.x)
- Brand new architecture design
- Broader device support
- AI-assisted configuration
- Enterprise version features

---

## Getting Updates

### Official Channels
- [GitHub Releases](https://github.com/NoWakeLock/NoWakeLock/releases) - Latest versions
- [IzzyOnDroid](https://apt.izzysoft.de/fdroid/index/apk/com.js.nowakelock) - F-Droid repository

### Version Types
- **Stable** - Stable version, recommended for general users
- **Beta** - Beta version, early experience of new features
- **Dev** - Development version, for development testing only

### Update Notifications
- In-app notifications for new version releases
- Follow GitHub Releases for latest information
- Join community groups for first-hand news

!!! info "Version Policy"
    NoWakeLock follows semantic versioning. Major updates will be announced in advance to ensure users have sufficient time to prepare for upgrades.

!!! warning "Upgrade Reminder"
    Please backup configuration before major version upgrades. There may be incompatibilities between some versions.

