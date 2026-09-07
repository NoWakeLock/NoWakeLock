"""Device smoke test. Install APK first; NoWakeLock rules for the probe must allow it."""
import argparse
import subprocess
import time
import xml.etree.ElementTree as ET

p = argparse.ArgumentParser()
p.add_argument('--adb', default='adb')
p.add_argument('--serial', required=True)
p.add_argument('--blocked', action='store_true', help='Expect existing NoWakeLock rules to block all three types')
p.add_argument('--service-only', action='store_true', help='Run only actual Service lifecycle/return-value checks')
alarm_mode = p.add_mutually_exclusive_group()
alarm_mode.add_argument('--foreground-alarm', action='store_true', help='Use foreground broadcast queue as an explicit alarm delivery control')
alarm_mode.add_argument('--listener-alarm', action='store_true', help='Use direct OnAlarmListener delivery, independently of broadcast queues')
args = p.parse_args()
base = [args.adb, '-s', args.serial]

def adb(*words):
    r = subprocess.run(base + list(words), capture_output=True, text=True, encoding='utf8', timeout=30)
    if r.returncode:
        raise RuntimeError(r.stderr or r.stdout)
    return r.stdout

def command(name, duration=0):
    if name == 'alarm' and args.listener_alarm: name = 'alarm-listener'
    if name == 'alarm' and args.foreground_alarm: name = 'alarm-foreground'
    adb('shell', 'am', 'start', '-W', '-n', 'com.js.nowakelock.probe/.ProbeActivity',
        '--es', 'command', name, '--el', 'duration_ms', str(duration))
    time.sleep(0.4)

def events():
    xml = adb('shell', 'run-as', 'com.js.nowakelock.probe', 'cat', 'shared_prefs/events.xml')
    return ET.fromstring(xml).findtext("string[@name='log']", '')

def held():
    return any('PARTIAL_WAKE_LOCK' in line and 'NWLProbe:Wake' in line
               for line in adb('shell', 'dumpsys', 'power').splitlines())

try:
    adb('shell', 'input', 'keyevent', 'KEYCODE_WAKEUP')
    adb('shell', 'wm', 'dismiss-keyguard')
    command('reset')
    if not args.service_only:
        command('wake')
        probe_pid = adb('shell', 'pidof', 'com.js.nowakelock.probe').strip()
        time.sleep(6)
        current_pid = adb('shell', 'pidof com.js.nowakelock.probe || true').strip()
        assert probe_pid and current_pid == probe_pid, 'Probe process exited or restarted during manual wake test'
        assert held() == (not args.blocked), 'System wakelock state differs from expected rule'
        command('release')
        assert not held(), 'Wake lock remained after manual release'
        command('alarm', 1000)
        time.sleep(7)
        assert 'ALARM_PERMISSION_REQUIRED' not in events(), 'Grant exact alarm permission before running'
        assert ('ALARM_DELIVERED' in events()) == (not args.blocked), 'Alarm delivery differs from expected rule'
    command('start')
    command('bind')
    time.sleep(6)
    text = events()
    assert 'ERROR ' not in text, 'Framework call raised an exception:\n' + text
    if args.blocked:
        assert 'SERVICE_START_API_RETURNED null' in text, text
        assert 'SERVICE_BIND_API_RETURNED false' in text, text
    assert ('SERVICE_STARTED' in text) == (not args.blocked), text
    assert ('SERVICE_CONNECTED' in text) == (not args.blocked), text
    assert 'SERVICE_DESTROYED' not in text, 'Manual service ended before explicit stop/unbind'
    command('service-stop')
    assert 'SERVICE_DESTROYED' not in events(), 'Bound service destroyed before unbind'
    command('unbind')
    assert ('SERVICE_DESTROYED' in events()) == (not args.blocked), events()
    if not args.blocked:
        command('reset')
        if not args.service_only:
            command('wake', 500)
            time.sleep(1)
            assert not held(), 'Timed wake lock not released'
        command('start', 500)
        time.sleep(1)
        assert 'SERVICE_DESTROYED' in events(), 'Timed service not stopped'
        command('reset')
        command('bind', 500)
        time.sleep(1)
        assert 'SERVICE_UNBOUND' in events() and 'SERVICE_DESTROYED' in events(), events()
        if not args.service_only:
            command('reset')
            command('alarm', 1000)
            command('cancel')
            time.sleep(7)
            assert 'ALARM_DELIVERED' not in events(), 'Cancelled alarm was delivered'
    print('PASS', 'service-only' if args.service_only else 'all', 'blocked' if args.blocked else 'allowed', flush=True)
finally:
    try:
        print(events(), flush=True)
    finally:
        command('stop')
