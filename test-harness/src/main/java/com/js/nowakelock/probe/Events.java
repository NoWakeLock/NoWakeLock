package com.js.nowakelock.probe;

import android.content.Context;
import android.util.Log;

final class Events {
    static synchronized void add(Context context, String event) {
        String line = System.currentTimeMillis() + " " + event;
        Log.i("NWLProbe", line);
        String text = read(context) + line + "\n";
        String[] lines = text.split("\n");
        if (lines.length > 80) text = String.join("\n", java.util.Arrays.copyOfRange(lines, lines.length - 80, lines.length)) + "\n";
        context.getSharedPreferences("events", 0).edit().putString("log", text).commit();
    }

    static String read(Context context) {
        return context.getSharedPreferences("events", 0).getString("log", "");
    }

    static void clear(Context context) {
        context.getSharedPreferences("events", 0).edit().clear().commit();
    }
}
