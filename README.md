# 🔋 NoWakeLock Extended

<div align="center">

![Android](https://img.shields.io/badge/platform-Android%207.0%2B-green.svg?style=for-the-badge)
![Framework](https://img.shields.io/badge/framework-Shizuku%20%7C%20Xposed-orange.svg?style=for-the-badge)
![License](https://img.shields.io/github/license/Arora-Sir/NoWakeLock-Extended.svg?style=for-the-badge)
![GitHub release](https://img.shields.io/github/v/release/Arora-Sir/NoWakeLock-Extended.svg?style=for-the-badge)

**The ultimate power management tool for Android - now supercharged with Shizuku support and universal compatibility!**

</div>

## 🌟 The "Extended" Vision

Welcome to **NoWakeLock Extended**! Inspired by the phenomenal original project (which is still actively maintained and incredible!), this independently developed fork was created to expand upon its foundation. Our main goal: **to bring advanced battery-saving capabilities to everyone, including users without rooted devices and those on heavily customized OEMs.**

We took the brilliant architectural foundation of the original app and overhauled its core mechanisms to introduce entirely new ways to manage your device's power. 

### 🔥 What Makes This Fork Special?

1. **Rootless Operation (Shizuku Magic!)**  
   You no longer need a rooted device to take control of your battery! We engineered a brand-new backend that leverages **Shizuku** (via ADB/AppOps) to seamlessly monitor, manage, and block wakelocks, alarms, and background services. 

2. **Samsung OneUI Mastery**  
   Historically, heavily modified Android skins like Samsung's OneUI struggled with this type of system-level hooking. We rebuilt the `SettingsProviderHook` and implemented dynamic signature resolution to ensure **100% compatibility with Samsung devices!**

3. **Smart Fallback Architecture**  
   The app intelligently switches between Xposed (if available) and our custom Shizuku implementation. It dynamically bypasses caching mechanisms when operating rootless, ensuring real-time, accurate data.

4. **IPC Data Resiliency**  
   Fixed a critical logic flaw from the original app where serialization errors (like Shizuku unbinding) would maliciously command the internal database to wipe all user records. Your data and scores are now perfectly safe across disconnects!

---

## ✨ Core Features

Whether you are running full root or entirely rootless via Shizuku, NoWakeLock Extended grants you unparalleled control over your device's background activities:

- **🛡️ Granular WakeLock Control:** Block rogue apps from keeping your device awake.
- **⏰ Alarm Management:** Stop unnecessary wake-up alarms from draining your battery overnight.
- **⚙️ Service Optimization:** Monitor and forcefully halt resource-heavy background services.
- **🎯 Per-App Targeting:** Apply custom rules and regular expressions (Regex) to specific apps.
- **📊 Real-Time Analytics:** Beautiful Material You dashboards showing live power consumption and blocked events.
- **👥 Multi-User Ready:** Fully supports Android work profiles and secondary users.

## 📥 Installation

Currently, NoWakeLock Extended is distributed exclusively through our GitHub Releases to ensure you are getting the most authentic and secure build directly from the source.

1. Head over to the [**Releases Page**](../../releases/latest).
2. Download the latest `v3.1.0` APK (`NoWakeLock-Extended.apk`).
3. Install the APK on your Android device.

## 🚀 Getting Started

### Method 1: The Rootless Way (Shizuku) *[Recommended]*
1. Install [Shizuku](https://shizuku.rikka.app/) from the Play Store.
2. Follow Shizuku's guide to start it via Wireless Debugging or ADB.
3. Open NoWakeLock Extended, and grant it Shizuku permissions when prompted.
4. You are ready to go! The app will now intercept and manage power events rootlessly.

### Method 2: The Rooted Way (Xposed)
1. Ensure your device is rooted with Magisk/KernelSU.
2. Install the **LSPosed** framework.
3. Enable the NoWakeLock Extended module inside LSPosed and select the recommended System Framework targets.
4. Reboot your device.

## 🛠️ Building from Source

Want to compile the project yourself? We welcome developers!

```bash
# Clone the repository
git clone https://github.com/YourUsername/NoWakeLock-Extended.git
cd NoWakeLock-Extended

# Build the release APK using Gradle
./gradlew assembleRelease
```

## 🤝 Contributing

Love what we're doing? Contributions are highly encouraged! Whether it's adding a new feature, fixing a bug, or improving translations, feel free to open an Issue or submit a Pull Request. If this project helped you squeeze more life out of your battery, **please consider giving it a ⭐ Star!**

## 🙏 Inspiration & Credits

This project would not exist without the incredible groundwork laid by **Jasper Hale ([@Jasper-1024](https://github.com/Jasper-1024))** and the original NoWakeLock contributors. We were deeply inspired by their vision of open-source power management and respect their ongoing work. All core architectural credit goes to them, while the Shizuku integration, Samsung compatibility layer, and dynamic hooking mechanisms were engineered specifically for this extended fork. 

## 📄 License

NoWakeLock Extended is proud to remain open-source under the **GNU General Public License v3.0**. See the [LICENSE](LICENSE) file for more details.