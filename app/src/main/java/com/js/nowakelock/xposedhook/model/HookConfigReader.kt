package com.js.nowakelock.xposedhook.model

import android.content.SharedPreferences
import com.js.nowakelock.data.config.ConfigBackendStatus
import com.js.nowakelock.data.config.XposedConfigKeys
import java.util.concurrent.atomic.AtomicReference

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
    private val frameworkVersion: String?,
    private val sharedHead: AtomicReference<Array<Any>> = AtomicReference()
) : HookConfigReader {
    private val metadata = ConfigBackendStatus(remoteReadable = true,
        activeBackend = ConfigBackendStatus.BACKEND_REMOTE,
        frameworkName = frameworkName, frameworkVersion = frameworkVersion)
    @Volatile private var snapshot = ImmutableRuleReader(emptyMap<String, Any>(), metadata)
    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> refresh() }
    init {
        sharedHead.compareAndSet(null, snapshot.transferState())
        preferences.registerOnSharedPreferenceChangeListener(listener)
        refresh()
    }
    fun dispose() { preferences.unregisterOnSharedPreferenceChangeListener(listener) }
    fun resume() { preferences.registerOnSharedPreferenceChangeListener(listener); refresh() }
    override fun capture(): ImmutableRuleReader {
        val frame = sharedHead.get()
        val cached = snapshot
        if (cached.transferState() === frame) return cached
        return ImmutableRuleReader.fromPrepared(frame, metadata).also { snapshot = it }
    }
    fun accept(values: Map<String, *>): Long {
        val next = ImmutableRuleReader(values, metadata)
        val revision = next.status().observedRevision
        require(revision > 0) { "Missing revision" }
        while (true) {
            val previous = sharedHead.get()
            val current = ImmutableRuleReader.fromPrepared(previous, metadata)
            require(revision >= current.status().observedRevision) { "Stale configuration" }
            if (revision == current.status().observedRevision) require(next.values == current.values) { "Conflicting revision" }
            if (sharedHead.compareAndSet(previous, next.transferState())) break
        }
        lastError = null
        return revision
    }
    override val backendName: String = ConfigBackendStatus.BACKEND_REMOTE
    override val isReadable: Boolean
        get() = capture().isReadable

    @Volatile private var lastError: String? = null

    override fun contains(key: String): Boolean {
        return capture().contains(key)
    }

    override fun refresh() {
        try {
            val values = preferences.all.filterKeys { XposedConfigKeys.isOwned(it) && it != XposedConfigKeys.OWNED_KEYS }
            if ((values[XposedConfigKeys.REVISION] as? Long ?: 0) > 0) accept(values)
        } catch (e: Exception) { lastError = e.message }
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return capture().getBoolean(key, defaultValue)
    }

    override fun getLong(key: String, defaultValue: Long): Long {
        return capture().getLong(key, defaultValue)
    }

    override fun getStringSet(key: String): Set<String> {
        return capture().getStringSet(key)
    }

    override fun status(): ConfigBackendStatus {
        val current = capture()
        return current.status().copy(remoteReadable = current.isReadable, lastError = lastError)
    }
}
