# Module Check

The Module Check page helps you verify that NoWakeLock is correctly installed and running.

## Check Items

### Basic Checks

| Check Item | Description | Failure Reason |
|------------|-------------|----------------|
| **Xposed Framework Active** | Detect Xposed framework running status | Framework not installed or not activated |
| **Module Loaded** | Confirm NoWakeLock is recognized by framework | Module not checked or installation failed |
| **Module Activated** | Verify module is running in target process | Scope configuration error |

### Function Checks

| Check Item | Description | Failure Reason |
|------------|-------------|----------------|
| **Hook Working Normally** | Verify system call interception functionality | System version incompatible |
| **Configuration Read Success** | Confirm settings can be read | Permission issues or storage anomaly |
| **Database Normal** | Check database read/write functionality | Insufficient storage space or permission issues |

[Screenshot needed: Module check page - all successful]

## Status Descriptions

### ✅ Normal Status
All check items show green icons, indicating the module is working normally.

### ❌ Abnormal Status
Red icons indicate problems that need to be addressed.

### ⚠️ Warning Status
Yellow icons indicate partially limited functionality but basic usability.

## Troubleshooting

### Xposed Framework Issues

**Symptoms**: "Xposed Framework Active" shows ❌

**Solution Steps**:
1. Confirm LSPosed or EdXposed is installed
2. Check if framework manager shows "Activated"
3. Restart device
4. Verify framework version compatibility

### Module Loading Issues

**Symptoms**: "Module Loaded" shows ❌

**Solution Steps**:
1. Open Xposed Manager
2. Go to "Modules" page
3. Confirm NoWakeLock is checked
4. Restart device
5. Recheck

### Scope Configuration Issues

**Symptoms**: "Module Activated" shows ❌

**Solution Steps**:
1. Open LSPosed Manager
2. Click NoWakeLock module
3. Enter "Scope" settings
4. Confirm the following are selected:
   - `android` (System Framework)
   - `com.js.nowakelock` (Application itself)
5. Restart device

[Screenshot needed: LSPosed scope configuration]

### Hook Function Issues

**Symptoms**: "Hook Working Normally" shows ❌

**Possible Causes**:
- Unsupported Android version
- System customization causing interface changes
- SELinux policy restrictions

**Solutions**:
1. Check device compatibility
2. View Xposed logs:
   ```bash
   adb logcat | grep -i nowakelock
   ```
3. Try reinstalling module

### Configuration Reading Issues

**Symptoms**: "Configuration Read Success" shows ❌

**Solution Steps**:
1. Check storage permissions
2. Clear application data:
   ```bash
   # Note: This will delete all configurations
   adb shell pm clear com.js.nowakelock
   ```
3. Reopen application

### Database Issues

**Symptoms**: "Database Normal" shows ❌

**Solution Steps**:
1. Check storage space
2. Verify application permissions
3. Reset database:
   - Settings → Clear Data
   - Reconfigure

## Advanced Checks

### View System Logs
```bash
# View NoWakeLock related logs
adb logcat | grep -i nowakelock

# View Xposed logs
adb logcat | grep -i xposed
```

### Verify Hook Effects
1. Open any application
2. Switch to WakeLock page
3. Check if new WakeLock records appear

### Test Rule Functionality
1. Set a test rule
2. Trigger corresponding system behavior
3. Check if statistics data updates

## Performance Monitoring

### Resource Usage
The module check page also displays:
- CPU usage rate
- Memory usage
- Storage space usage

### Performance Metrics

| Metric | Normal Range | Description |
|--------|--------------|-------------|
| CPU Usage | < 5% | Hook processing overhead |
| Memory Usage | < 50MB | Cache and data usage |
| Storage Usage | < 100MB | Database and log size |

## Regular Checks

### Recommended Frequency
- **First Installation**: Check daily
- **Stable Operation**: Check weekly
- **After System Updates**: Check immediately

### Automatic Checks
The application automatically performs basic checks on startup and shows notifications when anomalies occur.

### Check Records
The application keeps check records for the last 30 days, viewable in settings.

## Contact Support

If checks continue to fail:

1. **Collect Information**:
   - Device model and Android version
   - Xposed framework type and version
   - Module check page screenshot

2. **Get Logs**:
   ```bash
   adb logcat -v time > nowakelock_log.txt
   ```

3. **Seek Help**:
   - [GitHub Issues](https://github.com/NoWakeLock/NoWakeLock/issues)
   - [Telegram Group](https://t.me/nowakelock)
   - [Discord Community](https://discord.gg/kewmG5AShQ)

!!! warning "Important Reminder"
    When module check fails, NoWakeLock functionality may not work properly. Please resolve all issues before proceeding with configuration.