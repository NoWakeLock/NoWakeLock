package com.js.nowakelock.xposedhook.modern;

import android.content.Context;
import android.content.Intent;

/** Two system overloads that a ROM can nest for one application start request. */
public final class ServiceOverloadFixture {
    public final Context mContext;
    public ServiceOverloadFixture(Context context) { mContext = context; }
    public Object startServiceLocked(Object caller, Intent intent, String type, int pid, int uid,
            boolean foreground, String pkg, String feature, int user) { return null; }
    public Object startServiceLocked(Object caller, Intent intent, String type, int pid, int uid,
            boolean foreground, String pkg, String feature, int user, boolean extra) { return null; }
}
