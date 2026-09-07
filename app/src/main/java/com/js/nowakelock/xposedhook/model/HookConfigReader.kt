package com.js.nowakelock.xposedhook.model

import android.content.SharedPreferences
import com.js.nowakelock.data.config.ConfigBackendStatus
import com.js.nowakelock.data.config.XposedConfigKeys

interface HookConfigReader {
    fun capture(): HookConfigReader = this
    fun matches(key: String, name: String): Boolean = getStringSet(key).any { Regex(it).matches(name) }
    val backendName: String
    val isReadable: Boolean
    fun contains(key: String): Boolean
    fun refresh()
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean
    fun getLong(key: String, defaultValue: Long = 0): Long
    fun getStringSet(key: String): Set<String>
    fun status(): ConfigBackendStatus
}

class EmptyHookConfigReader : HookConfigReader {
    override val backendName: String = ConfigBackendStatus.BACKEND_NONE
    override val isReadable: Boolean = false

    override fun contains(key: String): Boolean = false
    override fun refresh() = Unit
    override fun getBoolean(key: String, defaultValue: Boolean): Boolean = defaultValue
    override fun getLong(key: String, defaultValue: Long): Long = defaultValue
    override fun getStringSet(key: String): Set<String> = emptySet()
    override fun status(): ConfigBackendStatus = ConfigBackendStatus()
}

class SharedPreferencesHookConfigReader(
    private val preferences: SharedPreferences,
    private val frameworkName: String?,
    private val frameworkVersion: String?
) : HookConfigReader {
    private val metadata = ConfigBackendStatus(remoteReadable = true,
        activeBackend = ConfigBackendStatus.BACKEND_REMOTE,
        frameworkName = frameworkName, frameworkVersion = frameworkVersion)
    @Volatile private var snapshot = ImmutableRuleReader(emptyMap<String, Any>(), metadata)
    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> refresh() }
    init {
        preferences.registerOnSharedPreferenceChangeListener(listener)
        refresh()
    }
    override fun capture(): HookConfigReader = snapshot
    @Synchronized fun accept(values: Map<String, *>): Long {
        val next = ImmutableRuleReader(values, metadata)
        val revision = next.status().observedRevision
        val current = snapshot.status().observedRevision
        require(revision > 0) { "Missing revision" }
        require(revision >= current) { "Stale configuration" }
        if (revision == current) require(next.values == snapshot.values) { "Conflicting revision" }
        snapshot = next
        lastError = null
        return revision
    }
    override val backendName: String = ConfigBackendStatus.BACKEND_REMOTE
    override val isReadable: Boolean
        get() = snapshot.isReadable

    @Volatile private var lastError: String? = null

    override fun contains(key: String): Boolean {
        return snapshot.contains(key)
    }

    override fun refresh() {
        try {
            val values = preferences.all.filterKeys { XposedConfigKeys.isOwned(it) && it != XposedConfigKeys.OWNED_KEYS }
            if ((values[XposedConfigKeys.REVISION] as? Long ?: 0) > 0) accept(values)
        } catch (e: Exception) { lastError = e.message }
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return snapshot.getBoolean(key, defaultValue)
    }

    override fun getLong(key: String, defaultValue: Long): Long {
        return snapshot.getLong(key, defaultValue)
    }

    override fun getStringSet(key: String): Set<String> {
        return snapshot.getStringSet(key)
    }

    override fun status(): ConfigBackendStatus {
        return snapshot.status().copy(remoteReadable = snapshot.isReadable, lastError = lastError)
    }
}
