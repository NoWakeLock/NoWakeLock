package com.js.nowakelock.data.config

import android.content.SharedPreferences

class SharedPreferencesConfigBackend(
    override val name: String,
    private val preferences: SharedPreferences
) : XposedConfigBackend {
    override val isReadable: Boolean = true

    override fun publish(snapshot: ConfigSnapshot): Boolean {
        val editor = preferences.edit()
        val owned = preferences.all.keys.filter(XposedConfigKeys::isOwned)
        owned.filterNot { it in snapshot.values }.forEach(editor::remove)
        snapshot.values.forEach { (key, value) ->
            when (value) {
                is Boolean -> editor.putBoolean(key, value)
                is Long -> editor.putLong(key, value)
                // RemotePreferences serializes the concrete collection across processes.
                // Kotlin's EmptySet (including its R8 name) is not known to the framework.
                is Set<*> -> editor.putStringSet(key, value.filterIsInstanceTo(HashSet<String>()))
                else -> error("Unsupported configuration value for $key")
            }
        }
        editor.putLong(XposedConfigKeys.REVISION, snapshot.revision)
        return editor.commit()
    }

    override fun putBoolean(key: String, value: Boolean): Boolean {
        return preferences.edit().putBoolean(key, value).commit()
    }

    override fun putLong(key: String, value: Long): Boolean {
        return preferences.edit().putLong(key, value).commit()
    }

    override fun putStringSet(key: String, value: Set<String>): Boolean {
        return preferences.edit().putStringSet(key, HashSet(value)).commit()
    }
}
