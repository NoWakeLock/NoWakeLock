# Quick Start

5-minute quick configuration guide to start optimizing your device's battery life with NoWakeLock.

## Step 1: Verify Installation

Open NoWakeLock and go to the "Module Check" page:

- ✅ All items show green = Installation successful
- ❌ Any red items = Check the [Installation Guide](installation.md)

[Screenshot needed: Module check success interface]

## Step 2: View Application List

1. Click the "Apps" tab at the bottom
2. Browse the installed applications list
3. Note the statistics on the right side

[Screenshot needed: Application list interface]

**List Information**:
- Application name and icon
- WakeLock/Alarm/Service statistics
- Last activity time

## Step 3: Analyze Battery Issues

⚠️ **Important**: Before configuring any rules, you must first confirm that your device actually has battery issues.

### Use Problem Analysis Tools
- Review [Problem Analysis Guide](problem-analysis.md) for complete analysis workflow
- Use BetterBatteryStats or NoWakeLock built-in statistics to confirm issues
- Only configure WakeLocks that are confirmed to be problematic

### Quick Problem Application Identification
1. Click the "WakeLocks" tab
2. Focus on items with high "Count" and "Duration"
3. These are the main battery drain sources

[Screenshot needed: WakeLock list]

## Step 4: Set Basic Rules

### Configure Problem WakeLocks
1. Click on abnormal WakeLock items
2. Select handling method:
   - **Limit** - Timeout release (recommended for beginners)
   - **Block** - Completely prevent (use with caution)

[Screenshot needed: Rule setting interface]

## Observe Effects

### Check After 24 Hours
1. **Apps interface** - View blocking effects
2. **App functionality** - Confirm important functions work normally
3. **Battery usage** - System battery statistics

### Key Indicators
- Block count increases ✅
- App notifications work normally ✅  
- Battery life improves ✅

[Screenshot needed: Statistics charts]

## Common Issues

### App Cannot Receive Notifications
**Solution**:
1. Find the app's push-related WakeLocks
2. Change to "Allow" mode
3. Observe for several hours

### No Effect After Setting
**Check Items**:
1. Is module working normally
2. Are rules applied correctly
3. Has app restarted

### Device Becomes Slow or Laggy
**Immediate Action**:
1. Temporarily disable all rules
2. Gradually restore important apps
3. Avoid blocking system critical services

## Next Steps

### Advanced Configuration
- [WakeLock Management](../features/wakelocks.md) - Detailed WakeLock control
- [Rule System](../features/rules-regex.md) - Regular expression matching

### Advanced Features
- [Alarm Management](../features/alarms.md) - Scheduled task control
- [Service Management](../features/services.md) - Background service management
- [Application Management](../features/app-management.md) - Configuration by application

### Getting Help
- [FAQ](../reference/faq.md) - Most common questions
- [Troubleshooting](../reference/troubleshooting.md) - Problem diagnosis guide

!!! tip "Usage Recommendations"
    It's recommended to start with conservative settings and adjust gradually. Ensure important app functions work normally before proceeding with more aggressive optimizations.