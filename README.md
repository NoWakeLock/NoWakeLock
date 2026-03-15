# NoWakeLock

<div align="center">

![Android](https://img.shields.io/badge/platform-Android-green.svg)
![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg)
![Xposed](https://img.shields.io/badge/framework-Xposed-orange.svg)
![License](https://img.shields.io/github/license/NoWakeLock/NoWakeLock.svg)
![GitHub release](https://img.shields.io/github/v/release/NoWakeLock/NoWakeLock.svg)
![GitHub downloads](https://img.shields.io/github/downloads/NoWakeLock/NoWakeLock/total.svg)
![GitHub stars](https://img.shields.io/github/stars/NoWakeLock/NoWakeLock.svg)
![GitHub forks](https://img.shields.io/github/forks/NoWakeLock/NoWakeLock.svg)
![GitHub issues](https://img.shields.io/github/issues/NoWakeLock/NoWakeLock.svg)
![GitHub pull requests](https://img.shields.io/github/issues-pr/NoWakeLock/NoWakeLock.svg)
![Build Status](https://img.shields.io/github/actions/workflow/status/NoWakeLock/NoWakeLock/build.yml?branch=dev)
![Last Commit](https://img.shields.io/github/last-commit/NoWakeLock/NoWakeLock/dev)

**Take complete control of your Android device's power management**

[📖 Documentation](https://nowakelock.jasper1024.com/) • 
[🚀 Releases](https://github.com/NoWakeLock/NoWakeLock/releases/latest) • 
[💬 Telegram](https://t.me/nowakelock) • 
[🎮 Discord](https://discord.gg/kewmG5AShQ)

</div>

## 📑 Table of Contents

- [✨ Features](#-features)
- [📱 Compatibility](#-compatibility)
- [⚡ Quick Start](#-quick-start)
- [📥 Installation](#-installation)
- [🚀 Usage](#-usage)
- [🌍 Documentation](#-documentation)
- [⚠️ Important Notes](#️-important-notes)
- [🔧 Development](#-development)
- [🤝 Contributing](#-contributing)
- [💬 Community](#-community)
- [📄 License](#-license)
- [🙏 Acknowledgments](#-acknowledgments)

## ✨ Features

NoWakeLock empowers you to take **granular control** of your Android device's background activity, helping you **significantly extend battery life** while maintaining performance.

### 🎯 Core Capabilities

- **🔒 WakeLock Management** - Block or allow specific wakelocks with precision timing controls
- **⏰ Alarm Control** - Manage system and app alarms to prevent unnecessary wake-ups
- **⚙️ Service Monitoring** - Control background services and their resource usage
- **📱 Per-App Configuration** - Fine-tune settings for individual applications
- **🔍 Regex Support** - Use powerful regular expressions for flexible pattern matching
- **📊 Detailed Analytics** - Monitor real-time statistics and power consumption insights

### 🎨 Modern Experience

- **🎨 Material Design 3** - Clean, modern interface following latest Android design principles
- **🌙 Dark Theme** - Full dark mode support for comfortable usage
- **🚀 Performance Optimized** - Smooth, responsive UI with efficient background processing
- **📈 Real-time Statistics** - Live monitoring of blocked wakelocks, alarms, and services

### 🛡️ Advanced Features

- **✅ Module Status Check** - Comprehensive diagnostics for Xposed module health
- **💾 Backup & Restore** - Safeguard your configurations with export/import functionality
- **👥 Multi-User Support** - Full compatibility with Android's multi-user profiles
- **🔄 Boot Consistency** - Automatic statistics reset after device reboot for accurate data

## 📱 Compatibility

| Component | Requirement |
|-----------|-------------|
| **Android Version** | 7.0 (API 24) - 15 (API 35) |
| **Xposed Framework** | EdXposed, LSPosed |
| **Architecture** | ARM64, ARM32 |
| **Root Required** | Yes (for Xposed framework) |

### ⚠️ Known Limitations

- **Samsung OneUI**: Currently not supported due to extensive Android modifications
- **MIUI**: Partial support - some features may be limited
- **Stock Android**: Full compatibility guaranteed

## ⚡ Quick Start

### Prerequisites

1. **Root access** on your Android device
2. **Xposed Framework** installed (LSPosed recommended)
3. Android 7.0+ running on your device

### Installation Steps

1. **Download** the latest APK from [Releases](https://github.com/NoWakeLock/NoWakeLock/releases/latest)
2. **Install** the APK on your device
3. **Activate** the module in your Xposed manager
4. **Reboot** your device
5. **Open** NoWakeLock and verify module status

## 📥 Installation

<div align="center">

### Official Distribution Channels

[<img src="https://f-droid.org/badge/get-it-on.png" alt="Get it on F-Droid" height="80">](https://f-droid.org/packages/com.js.nowakelock/)
&nbsp;&nbsp;&nbsp;
[<img src="https://gitlab.com/IzzyOnDroid/repo/-/raw/master/assets/IzzyOnDroid.png" alt="Get it on IzzyOnDroid" height="80">](https://apt.izzysoft.de/fdroid/index/apk/com.js.nowakelock)
&nbsp;&nbsp;&nbsp;
[<img src="assets/badge_github.png" alt="Get it on GitHub" height="80">](https://github.com/NoWakeLock/NoWakeLock/releases/latest)

</div>

### Manual Installation

```bash
# Download latest release
wget https://github.com/NoWakeLock/NoWakeLock/releases/latest/download/NoWakeLock.apk

# Install via ADB
adb install NoWakeLock.apk
```

## 🚀 Usage

### Initial Setup

1. **Launch** NoWakeLock after installation
2. **Check Module Status** - Use the built-in diagnostics screen
3. **Review** the app list to see detected applications
4. **Configure** your first app by tapping on it

### Basic Configuration

- **Allow/Block Toggle** - Simple on/off control for wakelocks/alarms/services
- **Time Intervals** - Set minimum intervals between wakelock acquisitions
- **Regex Patterns** - Advanced users can define custom matching patterns

### Monitoring

- **Statistics Tab** - View real-time power consumption data
- **Per-App Details** - Detailed breakdown of each application's activity
- **Timeline View** - Historical data visualization

## 🌍 Documentation

Comprehensive documentation is available in multiple languages:

- 🇺🇸 **[English Documentation](https://nowakelock.jasper1024.com/en/)**
- 🇨🇳 **[中文文档](https://nowakelock.jasper1024.com/zh/)**
- 🇹🇼 **[繁體中文](https://nowakelock.jasper1024.com/zh-tw/)**
- 🇫🇷 **[Documentation Française](https://nowakelock.jasper1024.com/fr/)**
- 🇩🇪 **[Deutsche Dokumentation](https://nowakelock.jasper1024.com/de/)**

### 📚 Key Documentation Sections

- **[Getting Started Guide](https://nowakelock.jasper1024.com/en/getting-started/)** - Complete setup walkthrough
- **[Features Overview](https://nowakelock.jasper1024.com/en/features/)** - Detailed feature explanations
- **[Developer Documentation](https://nowakelock.jasper1024.com/en/developers/)** - Technical implementation details
- **[FAQ](https://nowakelock.jasper1024.com/en/reference/faq/)** - Frequently asked questions
- **[Troubleshooting](https://nowakelock.jasper1024.com/en/reference/troubleshooting/)** - Common issues and solutions

## ⚠️ Important Notes

### 🧪 Development Status

- **Beta Quality**: Active development with regular updates
- **Use at Own Risk**: While stable, unexpected behavior may occur
- **No Warranty**: Developers not responsible for device damage

### 🔄 Upgrading from v2.x

- **Breaking Changes**: v3.0+ is not compatible with previous versions
- **Fresh Start Required**: Clear all app data before upgrading
- **Backup Recommendation**: Export settings before major updates

### 🛡️ Privacy & Security

- **🔒 No Data Collection**: Zero telemetry or analytics
- **📱 Local Processing**: All data stays on your device
- **🔓 Open Source**: Full source code available for audit

## 🔧 Development

### 🏗️ Project Structure

```
NoWakeLock/
├── app/                    # Main Android application
├── docs/                   # Multi-language documentation
├── fastlane/              # Automated deployment
└── .github/workflows/     # CI/CD pipelines
```

### 🌿 Branch Strategy

- **`dev`** - Primary integration and release branch
- **`fix/*` / `feature/*`** - Topic branches for focused changes
- **Release tags** - Semantic version tags in `vX.Y.Z` format

### 🛠️ Building from Source

```bash
# Clone the repository
git clone https://github.com/NoWakeLock/NoWakeLock.git
cd NoWakeLock

# Switch to the main development branch
git checkout dev

# Build with Gradle
./gradlew assembleDebug
```

### ✅ Test Commands

```bash
# Unit tests
./gradlew -PuseLocalMavenBootstrap=true --offline :app:testDebugUnitTest

# Build instrumentation APKs
./gradlew -PuseLocalMavenBootstrap=true --offline :app:assembleDebug :app:assembleDebugAndroidTest

# Run connected-device app tests
powershell -ExecutionPolicy Bypass -File .\scripts\run-connected-android-tests.ps1 -SkipBootstrap
```

### 📋 Development Requirements

- **Android Studio** Arctic Fox or newer
- **JDK** 17 or newer
- **Android SDK** API 24-35
- **Git** for version control

## 🤝 Contributing

We welcome contributions from the community! Here's how you can help:

### 🐛 Bug Reports

1. **Check existing issues** before creating new ones
2. **Use issue templates** for consistent reporting
3. **Provide detailed logs** and device information
4. **Include reproduction steps**

### 💡 Feature Requests

1. **Search existing requests** to avoid duplicates
2. **Explain use cases** and expected behavior
3. **Consider implementation complexity**

### 🔧 Code Contributions

1. **Fork the repository** and create a feature branch
2. **Follow coding standards** (see [Developer Guide](https://nowakelock.jasper1024.com/en/developers/))
3. **Add tests** for new functionality
4. **Update documentation** as needed
5. **Submit pull request** with clear description

### 🌐 Translations

Help make NoWakeLock accessible worldwide:

- **Application strings**: Contribute via [Crowdin](https://crowdin.com/project/nowakelock) *(Coming Soon)*
- **Documentation**: Submit PRs for `/docs/` translations

## 💬 Community

Join our growing community of power management enthusiasts:

- **💬 [Telegram Group](https://t.me/nowakelock)** - General discussion and support
- **🎮 [Discord Server](https://discord.gg/kewmG5AShQ)** - Real-time chat and development updates
- **🐛 [GitHub Issues](https://github.com/NoWakeLock/NoWakeLock/issues)** - Bug reports and feature requests
- **📖 [Documentation](https://nowakelock.jasper1024.com/)** - Comprehensive guides and references

## 📄 License

NoWakeLock is released under the **GNU General Public License v3.0**.

```
This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
```

See the [LICENSE](LICENSE) file for the complete license text.

## 🙏 Acknowledgments

NoWakeLock builds upon the foundation and inspiration of several outstanding open-source projects:

- **[Amplify](https://github.com/rsteckler/unbounce-android)** - Original inspiration for wakelock management
- **[XPrivacyLua](https://github.com/M66B/XPrivacyLua)** - Privacy protection framework techniques
- **[GravityBox](https://github.com/GravityBox/GravityBox)** - Advanced Xposed module architecture

### 🏆 Special Thanks

- **Jasper Hale** ([@Jasper-1024](https://github.com/Jasper-1024)) - Lead developer and project maintainer
- **Community Contributors** - Bug reports, feature requests, and translations
- **Beta Testers** - Early adoption and feedback

### 📊 Project Statistics

<div align="center">

| Metric | Value |
|--------|-------|
| **Latest Release** | ![Latest Release](https://img.shields.io/github/v/release/NoWakeLock/NoWakeLock?display_name=tag&sort=semver) |
| **Total Downloads** | ![Downloads](https://img.shields.io/github/downloads/NoWakeLock/NoWakeLock/total?color=success) |
| **Community Size** | ![Stars](https://img.shields.io/github/stars/NoWakeLock/NoWakeLock?style=social) ![Forks](https://img.shields.io/github/forks/NoWakeLock/NoWakeLock?style=social) |
| **Development Activity** | ![Commits](https://img.shields.io/github/commit-activity/m/NoWakeLock/NoWakeLock) ![Contributors](https://img.shields.io/github/contributors/NoWakeLock/NoWakeLock) |

</div>

---

<div align="center">

### 📈 Project Growth

[![Stargazers over time](https://starchart.cc/NoWakeLock/NoWakeLock.svg?variant=adaptive)](https://starchart.cc/NoWakeLock/NoWakeLock)

**⭐ Star this repository if NoWakeLock helps extend your device's battery life!**

Made with ❤️ by the NoWakeLock community

</div>
