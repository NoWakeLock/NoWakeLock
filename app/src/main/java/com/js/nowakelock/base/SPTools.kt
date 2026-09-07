package com.js.nowakelock.base

import android.annotation.SuppressLint
import android.content.Context
import com.js.nowakelock.BasicApp


class SPTools {
    companion object {
        const val SP_NAME = "Nowakelock"

        private const val default_str = ""
        private const val default_bool = true

        // @SuppressLint("WorldReadableFiles")
        private var prefs = try {
            // MODE_WORLD_READABLE, New XSharedPreferences
            BasicApp.context.getSharedPreferences(SP_NAME, Context.MODE_WORLD_READABLE)
        } catch (e: SecurityException) {
            null
        }

        fun getString(key: String, defaultValue: String = default_str): String {

            return prefs?.getString(key, defaultValue) ?: defaultValue
        }

        // @SuppressLint("CommitPrefEdits")
        fun setString(key: String, value: String): Boolean {
            with(prefs?.edit() ?: return false) {
                putString(key, value)
//                apply()
                return commit()
            }
        }

        fun getBoolean(key: String, defaultValue: Boolean = default_bool): Boolean {
            return prefs?.getBoolean(key, defaultValue) ?: defaultValue
        }

        // @SuppressLint("CommitPrefEdits")
        fun setBoolean(key: String, value: Boolean): Boolean {
            with(prefs?.edit() ?: return false) {
                putBoolean(key, value)
//                apply()
                return commit()
            }
        }

        fun getInt(key: String, defaultValue: Int = 0): Int {
            return prefs?.getInt(key, defaultValue) ?: defaultValue
        }

        // @SuppressLint("CommitPrefEdits")
        fun setInt(key: String, value: Int): Boolean {
            with(prefs?.edit() ?: return false) {
                putInt(key, value)
                return commit()
            }
        }

        fun getLong(key: String, defaultValue: Long = 0): Long {
            return prefs?.getLong(key, defaultValue) ?: defaultValue
        }

        // @SuppressLint("CommitPrefEdits")
        fun setLong(key: String, value: Long): Boolean {
            with(prefs?.edit() ?: return false) {
                putLong(key, value)
                return commit()
            }
        }

        fun setSet(key: String, value: Set<String>): Boolean {
            with(prefs?.edit() ?: return false) {
                putStringSet(key, value)
                return commit()
            }
        }

        fun getSet(key: String, defaultValue: Set<String> = setOf()): Set<String> {
            return prefs?.getStringSet(key, defaultValue) ?: defaultValue
        }

        fun isAvailable(): Boolean {
            return prefs != null
        }

        fun publish(snapshot: com.js.nowakelock.data.config.ConfigSnapshot): Boolean {
            val current = prefs ?: return false
            return com.js.nowakelock.data.config.SharedPreferencesConfigBackend("legacy", current)
                .publish(snapshot)
        }
    }
}
