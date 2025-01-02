package com.js.nowakelock.xposedhook

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers


object XpUtil {
    private const val Tag = "Xposed.NoWakeLock"
    const val authority = "com.js.nowakelock"

    private var log = true

    fun log(string: String) {
        if (log) {
            XposedBridge.log("$Tag: $string")
        }
    }
    fun error(thr: Throwable) {
        if (log) {
            Log.e(Tag, "", thr)
            XposedBridge.log("$Tag: ${thr.toString()}")
        }
    }

    fun getClass(name: String, classLoader: ClassLoader): Class<*>? {
        return try {
            XposedHelpers.findClass(name, classLoader)
        } catch (e: Throwable) {
            log("alarm getClass err: $e")
            null
        }
    }
}

fun Context.registerReceiver(intentFilter: IntentFilter, onReceive: (intent: Intent?) -> Unit): BroadcastReceiver {
    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            onReceive(intent)
        }
    }
    this.registerReceiver(receiver, intentFilter)
    return receiver
}