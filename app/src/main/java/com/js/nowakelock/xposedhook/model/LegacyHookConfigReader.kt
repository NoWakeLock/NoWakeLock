package com.js.nowakelock.xposedhook.model

import android.os.SystemClock
import com.js.nowakelock.BuildConfig
import com.js.nowakelock.data.config.ConfigBackendStatus
import com.js.nowakelock.data.config.XposedConfigKeys
import de.robv.android.xposed.XSharedPreferences
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.LockSupport

/** Keep the legacy transport; filesystem work runs off the Hook thread. */
class LegacyHookConfigReader : HookConfigReader {
    private val metadata = ConfigBackendStatus(legacyReadable = true, activeBackend = ConfigBackendStatus.BACKEND_LEGACY)
    @Volatile private var snapshot = ImmutableRuleReader(emptyMap<String, Any>(), metadata)
    @Volatile private var nextRefresh = 0L
    private val requested = AtomicBoolean(true)
    private val worker = Thread({
        var prefs: XSharedPreferences? = null
        while (true) {
            if (!requested.getAndSet(false)) { LockSupport.park(); continue }
            try {
                val p = prefs ?: XSharedPreferences(BuildConfig.APPLICATION_ID, XposedConfigKeys.GROUP_NAME).also { prefs = it }
                p.reload()
                if (p.file.canRead()) snapshot = ImmutableRuleReader(p.all.filterKeys {
                    XposedConfigKeys.isOwned(it) && it != XposedConfigKeys.OWNED_KEYS
                }, metadata)
            } catch (_: Exception) { /* Retain the last valid snapshot. */ }
            nextRefresh = SystemClock.elapsedRealtime() + 30_000
        }
    }, "NWL-legacy-config").apply { isDaemon = true; start() }
    override fun capture(): HookConfigReader { refresh(); return snapshot }
    override fun refresh() {
        if (SystemClock.elapsedRealtime() >= nextRefresh && requested.compareAndSet(false, true)) LockSupport.unpark(worker)
    }
    override val backendName = ConfigBackendStatus.BACKEND_LEGACY
    override val isReadable get() = snapshot.isReadable
    override fun contains(key: String) = capture().contains(key)
    override fun getBoolean(key: String, defaultValue: Boolean) = capture().getBoolean(key, defaultValue)
    override fun getLong(key: String, defaultValue: Long) = capture().getLong(key, defaultValue)
    override fun getStringSet(key: String) = capture().getStringSet(key)
    override fun status() = snapshot.status().copy(legacyReadable = snapshot.isReadable)
}
