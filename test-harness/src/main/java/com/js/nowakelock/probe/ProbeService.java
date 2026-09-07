package com.js.nowakelock.probe;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

public final class ProbeService extends Service {
    private final Handler handler = new Handler(android.os.Looper.getMainLooper());

    @Override public void onCreate() {
        super.onCreate();
        Events.add(this, "SERVICE_CREATED");
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        Events.add(this, "SERVICE_STARTED id=" + startId);
        handler.removeCallbacksAndMessages(null);
        long duration = intent == null ? 0 : intent.getLongExtra("duration_ms", 0);
        if (duration > 0) handler.postDelayed(() -> {
            Events.add(this, "SERVICE_STOP_SELF id=" + startId);
            stopSelf(startId);
        }, duration);
        return START_NOT_STICKY;
    }

    @Override public IBinder onBind(Intent intent) {
        Events.add(this, "SERVICE_BOUND");
        return new Binder();
    }

    @Override public boolean onUnbind(Intent intent) {
        Events.add(this, "SERVICE_UNBOUND");
        return false;
    }

    @Override public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        Events.add(this, "SERVICE_DESTROYED");
        super.onDestroy();
    }
}
