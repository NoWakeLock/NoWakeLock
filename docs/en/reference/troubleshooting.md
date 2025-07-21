# Troubleshooting

Systematic problem diagnosis and solution guide.

## Diagnostic Process

### Step 1: Basic Checks

#### Module Status Verification
1. Open NoWakeLock → "Module Check"
2. Confirm all items show ✅
3. If any ❌ items, handle according to prompts

[Screenshot needed: Module check failure example]

#### Xposed Framework Check
```bash
# Check LSPosed status
adb shell am start -n org.lsposed.manager/.ui.activity.MainActivity

# View module list
adb shell pm list packages | grep nowakelock
```

#### Basic Permission Check
- Storage permission
- Query all packages permission (Android 11+)
- Accessibility service permission (if needed)

### Step 2: Function Testing

#### WakeLock Testing
1. Set a simple WakeLock restriction rule
2. Open corresponding app to trigger WakeLock
3. Check statistics page for interception records

#### Rule Effectiveness Testing
1. Create test rule: intercept specific WakeLock
2. Observe target app behavior changes
3. Check event logs to confirm rule execution

## Common Problem Categories

### Installation Issues

#### Module Cannot Load
**Symptoms**: Module check shows "Module not loaded"

**Diagnostic Steps**:
1. Confirm Xposed framework running normally
2. Check if module is checked in manager
3. Verify app signature is correct

**Solutions**:
```bash
# Reinstall module
adb uninstall com.js.nowakelock
adb install nowakelock.apk

# Clear framework cache
# In LSPosed: "Settings" → "Clear Cache"
```

#### Hook Function Failed
**Symptoms**: Module loads but Hook doesn't work

**Possible Causes**:
- System version incompatible
- Scope configuration error
- SELinux policy restrictions

**Solutions**:
1. Confirm scope includes `android`
2. Check SELinux status:
   ```bash
   adb shell getenforce
   # If Enforcing, may affect Hook functionality
   ```
3. View Xposed logs:
   ```bash
   adb logcat | grep -E "(Xposed|nowakelock)"
   ```

### Function Issues

#### Rules Not Taking Effect
**Symptoms**: No interception effect after setting rules

**Checklist**:
- [ ] Rules enabled
- [ ] Match conditions correct
- [ ] Target app restarted
- [ ] No conflicting rules

**Debug Methods**:
1. Use simple exact matching for testing
2. Check rule priority
3. View matching logs

#### App Function Abnormal
**Symptoms**: App cannot work normally after setting rules

**Immediate Action**:
1. Disable related rules
2. Restart problem app
3. Gradually restore rules

**Root Solution**:
1. Analyze critical components app depends on
2. Adjust rule scope or parameters
3. Use "Restrict" instead of "Intercept"

#### Statistics Data Abnormal
**Symptoms**: Statistics data shows abnormally or doesn't update

**Check Items**:
1. Database status
   ```bash
   adb shell ls -la /data/data/com.js.nowakelock/databases/
   ```
2. Storage space
   ```bash
   adb shell df /data
   ```
3. App permissions

**Fix Methods**:
```bash
# Clear database (Note: will lose historical data)
adb shell pm clear com.js.nowakelock
```

### Performance Issues

#### System Lag
**Symptoms**: System response slows after installing NoWakeLock

**Performance Analysis**:
```bash
# CPU usage
adb shell top | grep nowakelock

# Memory usage
adb shell dumpsys meminfo com.js.nowakelock
```

**Optimization Solutions**:
1. Reduce number of rules
2. Simplify regular expressions
3. Adjust statistics frequency

#### Increased Battery Consumption
**Symptoms**: Module itself consumes battery

**Diagnostic Methods**:
1. Check background activity
   ```bash
   adb shell dumpsys battery
   ```
2. Analyze WakeLock usage
   ```bash
   adb shell dumpsys power | grep nowakelock
   ```

**Solutions**:
- Check for abnormal loop tasks
- Optimize database query frequency
- Confirm no memory leaks

### Compatibility Issues

#### Specific App Conflicts
**Symptoms**: Some apps conflict with NoWakeLock

**Identification Methods**:
1. System log analysis
2. App crash reports
3. ANR (Application Not Responding) logs

**Handling Strategy**:
```yaml
Temporary solution:
  - Add app to whitelist
  - Disable related rules

Long-term solution:
  - Analyze conflict causes
  - Adjust Hook strategy
  - Update compatibility code
```

#### System Version Compatibility
**Symptoms**: Function abnormal on new Android versions

**Adaptation Check**:
1. API change analysis
2. Permission model changes
3. Security policy updates

**Downgrade Plan**:
- Disable incompatible features
- Use alternative implementations
- Wait for version updates

## Log Analysis

### Collecting Logs

#### System Logs
```bash
# Complete logs
adb logcat -v time > full_log.txt

# NoWakeLock related
adb logcat | grep -i nowakelock > nowakelock_log.txt

# Xposed related
adb logcat | grep -i xposed > xposed_log.txt
```

#### App Logs
```bash
# Specific process logs
adb logcat --pid=$(adb shell pidof com.js.nowakelock)

# Crash logs
adb logcat | grep -E "(FATAL|AndroidRuntime)"
```

### Log Analysis

#### Key Error Identifiers
```
E/Xposed: Hook failed
E/NoWakeLock: Database error
W/ActivityManager: Unable to start service
```

#### Performance Issue Identifiers
```
W/Choreographer: Skipped frames
I/Timeline: Timeline: Activity_idle
W/InputDispatcher: Application is not responding
```

### Log Cleanup
```bash
# Clear logs
adb logcat -c

# Set log level
adb shell setprop log.tag.NoWakeLock VERBOSE
```

## Data Recovery

### Configuration Backup
```bash
# Backup configuration
adb backup -f backup.ab com.js.nowakelock

# Extract database
adb shell cp /data/data/com.js.nowakelock/databases/app_database /sdcard/
adb pull /sdcard/app_database ./
```

### Configuration Restore
```bash
# Restore backup
adb restore backup.ab

# Manual database restore
adb push ./app_database /sdcard/
adb shell cp /sdcard/app_database /data/data/com.js.nowakelock/databases/
```

### Reset Options

#### Soft Reset (Keep Configuration)
1. App settings → Clear cache
2. Restart app

#### Hard Reset (Clear All Data)
```bash
adb shell pm clear com.js.nowakelock
```

#### Complete Reset (Reinstall)
```bash
adb uninstall com.js.nowakelock
# Reinstall and configure
```

## Advanced Debugging

### Hook Debugging

#### Enable Verbose Logging
Enable "Debug Mode" in app settings, will output detailed Hook information.

#### Hook Testing Tools
```kotlin
// Test specific Hook points
fun testWakeLockHook() {
    // Manually trigger WakeLock acquisition
    // Observe if Hook is called
}
```

### Performance Analysis

#### CPU Analysis
```bash
# Performance monitoring
adb shell am start -n com.android.shell/.BugreportStorageProvider

# Thread analysis
adb shell ps -T | grep nowakelock
```

#### Memory Analysis
```bash
# Memory details
adb shell dumpsys meminfo com.js.nowakelock

# Memory leak detection
adb shell am dumpheap com.js.nowakelock /sdcard/heap.hprof
```

### Database Debugging

#### Database Check
```sql
-- Connect database
sqlite3 app_database

-- Check table structure
.schema

-- View data
SELECT * FROM app_info LIMIT 10;
SELECT * FROM wakelock_info LIMIT 10;
```

#### Data Consistency Check
```sql
-- Check orphaned records
SELECT * FROM events WHERE app_id NOT IN (SELECT id FROM apps);

-- Statistics data validation
SELECT package_name, COUNT(*) FROM events GROUP BY package_name;
```

## Preventive Measures

### Regular Maintenance

#### Weekly Checks
- Module status verification
- Rule effectiveness evaluation
- Performance metrics monitoring

#### Monthly Maintenance
- Clean historical data
- Update rule configuration
- Backup important settings

### Monitoring Setup

#### Performance Monitoring
Set performance thresholds, auto-alert when exceeded:
- CPU usage > 5%
- Memory usage > 100MB
- Database size > 500MB

#### Function Monitoring
Regularly test key functions:
- Rule matching accuracy
- Statistics data integrity
- App function normality

## Professional Support

### Community Support
- **Telegram**: [@nowakelock](https://t.me/nowakelock)
- **Discord**: [NoWakelock Community](https://discord.gg/kewmG5AShQ)
- **GitHub**: [Issues](https://github.com/NoWakeLock/NoWakeLock/issues)

### Issue Report Template
```markdown
## Environment Information
- Device: [Brand Model]
- Android Version: [Version]
- Xposed Framework: [LSPosed/EdXposed Version]
- NoWakeLock Version: [Version]

## Problem Description
[Detailed description of problem phenomena]

## Reproduction Steps
1. [Step one]
2. [Step two]
3. [Problem occurs]

## Expected Result
[Expected normal behavior]

## Actual Result
[Actual abnormal behavior that occurred]

## Related Logs
```log
[Paste related logs]
```

## Other Information
[Any other relevant information]
```

!!! warning "Data Safety"
    When troubleshooting, always backup important configurations first. Some operations may cause data loss.

!!! tip "Debug Suggestions"
    For complex problems, recommend step-by-step investigation, starting from simplest configuration and gradually increasing complexity to pinpoint problem root cause.