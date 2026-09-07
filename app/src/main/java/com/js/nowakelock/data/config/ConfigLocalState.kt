package com.js.nowakelock.data.config

import android.content.Context
import com.js.nowakelock.base.SPTools

class ConfigLocalState(context: Context) {
    private val preferences = context.getSharedPreferences("nwl_config_state", Context.MODE_PRIVATE)

    @Synchronized
    fun debug(): Boolean {
        if (!preferences.contains(XposedConfigKeys.DEBUG)) {
            val oldValue = SPTools.getBoolean(XposedConfigKeys.DEBUG, false)
            check(preferences.edit().putBoolean(XposedConfigKeys.DEBUG, oldValue).commit()) {
                "Unable to persist debug configuration"
            }
        }
        return preferences.getBoolean(XposedConfigKeys.DEBUG, false)
    }

    fun saveDebug(value: Boolean): Boolean = preferences.edit()
        .putBoolean(XposedConfigKeys.DEBUG, value).commit()

    @Synchronized
    fun nextRevision(floor: Long = 0): Long {
        val previous = maxOf(revision(), floor)
        check(previous < Long.MAX_VALUE) { "Revision exhausted" }
        val next = maxOf(System.currentTimeMillis(), previous + 1)
        check(preferences.edit().putLong(XposedConfigKeys.REVISION, next).commit()) {
            "Unable to persist configuration revision"
        }
        return next
    }

    fun revision(): Long = preferences.getLong(XposedConfigKeys.REVISION, 0)
    fun publishedRevision(): Long = preferences.getLong("published_revision", 0)
    fun markPublished(revision: Long) {
        check(preferences.edit().putLong("published_revision", revision).commit())
    }
}
