package com.js.nowakelock.data.config

interface XposedConfigBackend {
    val name: String
    val isReadable: Boolean
    fun publish(snapshot: ConfigSnapshot): Boolean

    fun putBoolean(key: String, value: Boolean): Boolean
    fun putLong(key: String, value: Long): Boolean
    fun putStringSet(key: String, value: Set<String>): Boolean
}
