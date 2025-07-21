# Problem Analysis Guide

Before configuring any rules, you must first confirm that your device actually has battery issues. This guide explains how to use tools to analyze and identify problems.

!!! warning "Important Principle"
    Only after confirming through data analysis that a specific WakeLock/Alarm/Service is actually causing standby battery drain should you consider implementing restrictions. Do not configure based on assumptions or preconceptions.

## Data Analysis Tools

### Recommended Analysis Tools

1. **BetterBatteryStats (BBS)** - First choice, provides most comprehensive analysis
2. **NoWakeLock Built-in Statistics** - Alternative when BBS is unavailable
3. **Combined Use** - For more accurate diagnostic results

## Using BetterBatteryStats (BBS) for Diagnosis

### Diagnostic Steps

1. **Install BBS** and grant necessary permissions
2. **Set monitoring period** - Recommend monitoring complete standby cycles
3. **Analyze key data**:
   - Kernel Wakelocks (system-level wake locks)
   - Partial Wakelocks (application-level wake locks)
   - Wake frequency and duration for each app

### Problem Identification Criteria

- Review abnormal wake activity during device standby periods
- Identify WakeLocks with excessive duration or frequency
- Confirm these activities actually cause battery consumption issues

## Using NoWakeLock Built-in Data Analysis

**When BBS is unavailable**, you can use NoWakeLock's own statistical data:

### Data Viewing Methods

1. **WakeLock List** - View WakeLock activity statistics for each app
2. **Sort by Duration** - Identify abnormally long-duration WakeLocks
3. **Sort by Frequency** - Discover abnormally high-frequency WakeLocks
4. **Active Status** - Monitor currently held WakeLocks

### Abnormality Identification Criteria

- WakeLocks showing active during device standby periods
- Single WakeLock cumulative duration far exceeding other similar apps
- Frequent WakeLock acquisition when app is not in use
- Abnormal WakeLock activity during nighttime hours

## Troubleshooting Workflow

### Step 1: Confirm Problem Exists

1. **Use analysis tools to monitor** a complete usage cycle (at least 24 hours)
   - Prioritize using BBS for comprehensive monitoring
   - Use NoWakeLock built-in statistics when BBS is unavailable
2. **Compare standby period** WakeLock activity with battery consumption
3. **Confirm abnormalities** - Look for WakeLocks active when device is not in use

### Step 2: Precisely Locate Problem Source

1. **Identify specific apps and services** causing abnormal WakeLocks
2. **Analyze patterns** - Check if issues occur during specific time periods or conditions
3. **Assess impact** - Confirm actual impact level of the WakeLock on battery life

### Step 3: Minimize Intervention

1. **Prioritize app settings** - Check if there are relevant options within the app
2. **Test restriction effects** - Use limit mode rather than complete blocking
3. **Monitor results** - Continue monitoring effects after configuration

!!! tip "Diagnostic Key Points"
    - Problems must actually exist, not be theoretical
    - Intervention must be targeted, not preventive
    - Verify app functionality remains normal after each configuration

## Effect Verification

### Post-Configuration Monitoring

**Necessary Steps**:
1. **Continue data monitoring** effects after configuration
   - Prioritize using BBS for comprehensive evaluation
   - Also use NoWakeLock built-in statistics as reference
2. **Verify app functionality** - Confirm all app functions still work normally
3. **Evaluate battery improvement** - Compare actual battery consumption before and after configuration

**Rollback Preparation**:
- If app functionality is affected, immediately cancel configuration
- If battery improvement is not significant, re-evaluate necessity of restrictions

## Next Steps

After problem analysis completion:

1. [WakeLock Management](../features/wakelocks.md) - Configure specific WakeLock rules
2. [Quick Start](quick-start.md) - 5-minute quick configuration guide
3. [FAQ](../reference/faq.md) - Solutions when encountering problems