package com.js.nowakelock.probe;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public final class AlarmReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        Events.add(context, "ALARM_DELIVERED");
    }
}
