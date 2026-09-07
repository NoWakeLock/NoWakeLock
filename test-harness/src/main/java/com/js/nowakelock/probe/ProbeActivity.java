package com.js.nowakelock.probe;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.EditText;
import android.text.InputType;

public final class ProbeActivity extends Activity {
    private final Handler handler = new Handler(android.os.Looper.getMainLooper());
    private PowerManager.WakeLock wake;
    private boolean binding;
    private TextView log;
    private EditText wakeDuration, alarmDelay, serviceDuration, bindDuration;
    private final Runnable release = this::releaseWake;
    private final AlarmManager.OnAlarmListener alarmListener = () -> Events.add(this, "ALARM_DELIVERED listener=true");
    private final Runnable unbind = this::unbindProbe;
    private final Runnable refresh = new Runnable() {
        @Override public void run() {
            log.setText(Events.read(ProbeActivity.this));
            handler.postDelayed(this, 1000);
        }
    };
    private final ServiceConnection connection = new ServiceConnection() {
        @Override public void onServiceConnected(ComponentName name, IBinder binder) {
            Events.add(ProbeActivity.this, "SERVICE_CONNECTED");
        }
        @Override public void onServiceDisconnected(ComponentName name) {
            Events.add(ProbeActivity.this, "SERVICE_DISCONNECTED");
        }
    };

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        column.setPadding(padding, padding, padding, padding);
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.addView(column);
        setContentView(scroll);
        scroll.setOnApplyWindowInsetsListener((view, insets) -> {
            view.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(),
                    insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
            return insets;
        });
        TextView title = new TextView(this);
        title.setText("NoWakeLock Probe\n时长单位：毫秒，0 = 手动结束（最长定时 3600000ms）。\n请求返回不代表实际执行，请对照模块统计。普通 Service 在后台也可能受 Android 限制。");
        title.setTextSize(17);
        column.addView(title);
        wakeDuration = input(column, "唤醒锁自动释放 ms（0 手动）", "0");
        button(column, "申请唤醒锁", "wake");
        button(column, "释放唤醒锁", "release");
        alarmDelay = input(column, "一次性闹钟延迟 ms", "3000");
        button(column, "安排闹钟（替换上次）", "alarm");
        button(column, "安排前台广播闹钟（队列对照）", "alarm-foreground");
        button(column, "安排监听器闹钟（直接回调对照）", "alarm-listener");
        button(column, "取消闹钟", "cancel");
        serviceDuration = input(column, "Service 自动停止 ms（0 手动）", "0");
        button(column, "启动 Service", "start");
        button(column, "停止 Service", "service-stop");
        bindDuration = input(column, "自动解绑 ms（0 手动）", "0");
        button(column, "绑定 Service", "bind");
        button(column, "解绑 Service", "unbind");
        button(column, "全部停止 / 取消", "stop");
        button(column, "精确闹钟授权", "permission");
        button(column, "清空日志（先停止）", "reset");
        log = new TextView(this);
        log.setTextSize(12);
        log.setTextIsSelectable(true);
        column.addView(log);
        if (state == null) consume(getIntent());
    }

    private void button(LinearLayout column, String title, String command) {
        Button button = new Button(this);
        button.setText(title);
        button.setOnClickListener(view -> execute(command, -1));
        column.addView(button);
    }

    private EditText input(LinearLayout column, String label, String value) {
        TextView caption = new TextView(this);
        caption.setText(label);
        column.addView(caption);
        EditText field = new EditText(this);
        field.setInputType(InputType.TYPE_CLASS_NUMBER);
        field.setSingleLine(true);
        field.setText(value);
        column.addView(field);
        return field;
    }

    private long duration(EditText field, long override) {
        long value = override >= 0 ? override : Long.parseLong(field.getText().toString());
        if (value < 0 || value > 3_600_000) throw new IllegalArgumentException("时长范围 0..3600000 ms");
        return value;
    }

    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        consume(intent);
    }

    private void consume(Intent intent) {
        String command = intent.getStringExtra("command");
        long value = intent.getLongExtra("duration_ms", -1);
        intent.removeExtra("command");
        intent.removeExtra("duration_ms");
        if ("latency".equals(command)) {
            int count = intent.getIntExtra("count", 300);
            int interval = intent.getIntExtra("interval_ms", 100);
            intent.removeExtra("count");
            intent.removeExtra("interval_ms");
            CurrentLatencyBenchmark.run(this, count, interval);
            return;
        }
        if (command != null) handler.post(() -> execute(command, value));
    }

    private PendingIntent alarmIntent() {
        return alarmIntent(false);
    }
    private PendingIntent alarmIntent(boolean foreground) {
        return PendingIntent.getBroadcast(this, 1,
                new Intent(this, AlarmReceiver.class).setAction("com.js.nowakelock.probe.ALARM")
                    .addFlags(foreground ? Intent.FLAG_RECEIVER_FOREGROUND : 0),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private void execute(String command, long override) {
        try {
            switch (command) {
                case "burst":
                    int count = (int) Math.min(3000, Math.max(1, override));
                    long began = SystemClock.elapsedRealtimeNanos();
                    for (int i = 0; i < count; i++) {
                        PowerManager.WakeLock item = getSystemService(PowerManager.class)
                            .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "NWLProbe:Wake");
                        item.acquire(1000);
                        item.release();
                    }
                    Events.add(this, "BURST_COMPLETED count=" + count + " elapsedNs=" + (SystemClock.elapsedRealtimeNanos() - began));
                    break;
                case "wake":
                    long wakeMs = duration(wakeDuration, override);
                    releaseWake();
                    wake = getSystemService(PowerManager.class).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "NWLProbe:Wake");
                    wake.setReferenceCounted(false);
                    Events.add(this, "WAKE_REQUEST durationMs=" + wakeMs);
                    if (wakeMs == 0) wake.acquire(); else wake.acquire(wakeMs + 1000);
                    Events.add(this, "WAKE_API_RETURNED localIsHeld=" + wake.isHeld() + " (not system proof)");
                    if (wakeMs > 0) handler.postDelayed(release, wakeMs);
                    break;
                case "release": releaseWake(); break;
                case "alarm":
                case "alarm-foreground":
                case "alarm-listener":
                    long delayMs = duration(alarmDelay, override);
                    AlarmManager alarms = getSystemService(AlarmManager.class);
                    if (Build.VERSION.SDK_INT >= 31 && !alarms.canScheduleExactAlarms()) {
                        Events.add(this, "ALARM_PERMISSION_REQUIRED (no request sent)");
                        break;
                    }
                    alarms.cancel(alarmIntent());
                    alarms.cancel(alarmListener);
                    Events.add(this, "ALARM_REQUEST delayMs=" + delayMs + " foreground=" + command.equals("alarm-foreground"));
                    if (command.equals("alarm-listener")) {
                        alarms.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + delayMs,
                            "com.js.nowakelock.probe.ALARM", alarmListener, handler);
                    } else {
                        alarms.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + delayMs, alarmIntent(command.equals("alarm-foreground")));
                    }
                    Events.add(this, "ALARM_API_RETURNED");
                    break;
                case "cancel":
                    getSystemService(AlarmManager.class).cancel(alarmListener);
                    getSystemService(AlarmManager.class).cancel(alarmIntent());
                    Events.add(this, "ALARM_CANCEL_REQUESTED");
                    break;
                case "start":
                    long serviceMs = duration(serviceDuration, override);
                    Events.add(this, "SERVICE_START_REQUEST durationMs=" + serviceMs);
                    ComponentName result = startService(new Intent(this, ProbeService.class).putExtra("duration_ms", serviceMs));
                    Events.add(this, "SERVICE_START_API_RETURNED " + result);
                    break;
                case "service-stop":
                    Events.add(this, "SERVICE_STOP_REQUEST returned=" + stopService(new Intent(this, ProbeService.class)));
                    break;
                case "bind":
                    long bindMs = duration(bindDuration, override);
                    unbindProbe();
                    Events.add(this, "SERVICE_BIND_REQUEST durationMs=" + bindMs);
                    binding = bindService(new Intent(this, ProbeService.class), connection, BIND_AUTO_CREATE);
                    Events.add(this, "SERVICE_BIND_API_RETURNED " + binding);
                    if (bindMs > 0) handler.postDelayed(unbind, bindMs);
                    break;
                case "unbind": unbindProbe(); break;
                case "stop": stopAll(); break;
                case "reset": stopAll(); Events.clear(this); break;
                case "permission":
                    if (Build.VERSION.SDK_INT >= 31) startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:" + getPackageName())));
                    else Events.add(this, "ALARM_PERMISSION_NOT_REQUIRED");
                    break;
                default: Events.add(this, "UNKNOWN_COMMAND " + command);
            }
        } catch (RuntimeException error) {
            Events.add(this, "ERROR " + command + " " + error);
        }
        log.setText(Events.read(this));
    }

    private void releaseWake() {
        handler.removeCallbacks(release);
        if (wake != null) {
            if (wake.isHeld()) wake.release();
            wake = null;
            Events.add(this, "WAKE_RELEASE_REQUESTED");
        }
    }

    private void unbindProbe() {
        handler.removeCallbacks(unbind);
        if (binding) {
            unbindService(connection);
            binding = false;
            Events.add(this, "SERVICE_UNBIND_REQUESTED");
        }
    }

    private void stopAll() {
        CurrentLatencyBenchmark.stop();
        getSystemService(AlarmManager.class).cancel(alarmListener);
        releaseWake();
        unbindProbe();
        getSystemService(AlarmManager.class).cancel(alarmIntent());
        stopService(new Intent(this, ProbeService.class));
        Events.add(this, "STOP_ALL_REQUESTED");
    }

    @Override protected void onResume() { super.onResume(); handler.post(refresh); }
    @Override protected void onPause() { handler.removeCallbacks(refresh); super.onPause(); }
    @Override protected void onDestroy() {
        CurrentLatencyBenchmark.stop();
        releaseWake();
        unbindProbe();
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
