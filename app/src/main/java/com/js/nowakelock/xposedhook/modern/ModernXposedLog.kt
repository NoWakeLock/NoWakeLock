package com.js.nowakelock.xposedhook.modern

import android.util.Log

internal object ModernXposedLog {
    @Volatile
    private var logger: (String) -> Unit = { message -> Log.i(TAG, message) }

    fun install(logger: (String) -> Unit) {
        this.logger = logger
    }

    fun info(message: String) {
        try {
            logger(message)
        } catch (_: Throwable) {
            Log.i(TAG, message)
        }
    }

    fun error(message: String, throwable: Throwable? = null) {
        val fullMessage = throwable?.message?.let { "$message: $it" } ?: message
        try {
            logger(fullMessage)
        } catch (_: Throwable) {
            Log.e(TAG, fullMessage, throwable)
        }
    }

    private const val TAG = "NoWakeLockModern"
}
