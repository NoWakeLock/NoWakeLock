# NoWakeLock (Enhanced Fork)

<div align="center">

![Android](https://img.shields.io/badge/platform-Android-green.svg)
![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg)
![Framework](https://img.shields.io/badge/framework-Xposed%20%7C%20Shizuku-orange.svg)
![License](https://img.shields.io/github/license/NoWakeLock/NoWakeLock.svg)

**Take complete control of your Android device's power management — Now with Shizuku & Samsung Support!**

</div>

## 🎉 Fork Achievements & Major Updates

This fork of NoWakeLock introduces several highly anticipated features and compatibility improvements that expand the app's usability to a much wider range of devices.

### 🌟 Key Enhancements

- **Shizuku Support (No Root Required!)**: You can now use NoWakeLock without a rooted device! By leveraging Shizuku, the app can monitor and block wakelocks, alarms, and services via ADB/AppOps. It serves as an excellent fallback when the Xposed framework is unavailable.
- **Samsung OneUI Compatibility**: Fixed the longstanding issue where Samsung devices were not supported. By handling varying `ContentProvider` call signatures across different Android and OEM versions, NoWakeLock now fully supports Samsung OneUI!
- **Robust Fallbacks**: Bypasses ContentProvider caching and handles direct queries when operating in Shizuku mode to ensure seamless operation.

### 🙏 Special Thanks & Praise

A massive thank you to the original developer, **Jasper Hale ([@Jasper-1024](https://github.com/Jasper-1024))**, for creating such an incredible and well-architected project. The foundation of NoWakeLock is exceptionally robust, making it a joy to build upon. This fork is built directly on their hard work, and all original credit for the core application goes to the NoWakeLock team.

---

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

## ✨ Features

NoWakeLock empowers you to take **granular control** of your Android device's background activity, helping you **significantly extend battery life** while maintaining performance.

### 🎯 Core Capabilities

- **🔒 WakeLock Management** - Block or allow specific wakelocks with precision timing controls. *(Works via Xposed or Shizuku)*
- **⏰ Alarm Control** - Manage system and app alarms to prevent unnecessary wake-ups.
- **⚙️ Service Monitoring** - Control background services and their resource usage.
- **📱 Per-App Configuration** - Fine-tune settings for individual applications.
- **🔍 Regex Support** - Use powerful regular expressions for flexible pattern matching.
- **📊 Detailed Analytics** - Monitor real-time statistics and power consumption insights.

### 🎨 Modern Experience

- **🎨 Material Design 3** - Clean, modern interface following latest Android design principles.
- **🌙 Dark Theme** - Full dark mode support for comfortable usage.
- **🚀 Performance Optimized** - Smooth, responsive UI with efficient background processing.
- **📈 Real-time Statistics** - Live monitoring of blocked wakelocks, alarms, and services.

### 🛡️ Advanced Features

- **✅ Module Status Check** - Comprehensive diagnostics for Xposed & Shizuku health.
- **💾 Backup & Restore** - Safeguard your configurations with export/import functionality.
- **👥 Multi-User Support** - Full compatibility with Android's multi-user profiles.
- **🔄 Boot Consistency** - Automatic statistics reset after device reboot for accurate data.

## 📱 Compatibility

| Component | Requirement |
|-----------|-------------|
| **Android Version** | 7.0 (API 24) - 15 (API 35) |
| **Xposed Framework** | EdXposed, LSPosed (Optional if using Shizuku) |
| **Shizuku** | Supported as a Rootless Alternative! |
| **Architecture** | ARM64, ARM32 |
| **Root Required** | **NO** (If using Shizuku) / Yes (If using Xposed) |

### ✅ Newly Supported
- **Samsung OneUI**: Fully supported!
- **Non-Rooted Devices**: Supported via Shizuku!

## ⚡ Quick Start

### Prerequisites

1. **Shizuku** installed and activated (for non-root users), OR
2. **Root access** + **Xposed Framework** (LSPosed recommended)
3. Android 7.0+ running on your device

### Installation Steps

1. **Download** the latest APK from the Releases page.
2. **Install** the APK on your device.
3. **Activate** the module in your Xposed manager OR grant permissions via Shizuku.
4. **Reboot** your device (if using Xposed).
5. **Open** NoWakeLock and verify the module or Shizuku status.

## 📥 Installation

<div align="center">

### Official Distribution Channels

[<img src="https://f-droid.org/badge/get-it-on.png" alt="Get it on F-Droid" height="80">](https://f-droid.org/packages/com.js.nowakelock/)
&nbsp;&nbsp;&nbsp;
[<img src="https://gitlab.com/IzzyOnDroid/repo/-/raw/master/assets/IzzyOnDroid.png" alt="Get it on IzzyOnDroid" height="80">](https://apt.izzysoft.de/fdroid/index/apk/com.js.nowakelock)

</div>

### Manual Installation

```bash
# Install via ADB
adb install NoWakeLock.apk
```

## 🚀 Usage

### Initial Setup

1. **Launch** NoWakeLock after installation.
2. **Check Status** - Use the built-in diagnostics screen to verify Shizuku or Xposed is active.
3. **Review** the app list to see detected applications.
4. **Configure** your first app by tapping on it.

## 🌍 Documentation

Original comprehensive documentation is available in multiple languages:
- 🇺🇸 **[English Documentation](https://nowakelock.jasper1024.com/en/)**

## ⚠️ Important Notes

### 🧪 Development Status

- **Beta Quality**: Active development with regular updates
- **Use at Own Risk**: While stable, unexpected behavior may occur
- **No Warranty**: Developers not responsible for device damage

### 🛡️ Privacy & Security

- **🔒 No Data Collection**: Zero telemetry or analytics
- **📱 Local Processing**: All data stays on your device
- **🔓 Open Source**: Full source code available for audit

## 🤝 Contributing

We welcome contributions from the community! Check out the GitHub issues for bug reports and feature requests.

## 📄 License

NoWakeLock is released under the **GNU General Public License v3.0**.

See the [LICENSE](LICENSE) file for the complete license text.

## 🙏 Acknowledgments

- **Amplify** - Original inspiration for wakelock management
- **XPrivacyLua** - Privacy protection framework techniques
- **GravityBox** - Advanced Xposed module architecture

### 🏆 Special Thanks
Once again, a massive shoutout to **Jasper Hale ([@Jasper-1024](https://github.com/Jasper-1024))** and all original contributors for their phenomenal work on this project!
