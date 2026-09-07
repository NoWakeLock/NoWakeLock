package com.js.nowakelock.xposedhook.model

import com.js.nowakelock.data.config.ConfigBackendStatus
import com.js.nowakelock.data.config.XposedConfigKeys
import java.util.Collections

/** Fully built on the writer thread. No mutable collection escapes this object. */
class ImmutableRuleReader(values: Map<String, *>, private val metadata: ConfigBackendStatus) : HookConfigReader {
    // Reuse the decision facade; do not allocate one object per intercepted call.
    val decision = XpNSP(this)
    val values: Map<String, Any> = Collections.unmodifiableMap(values.mapValues { (_, value) ->
        when (value) {
            is Boolean, is Long -> value
            is Set<*> -> {
                require(value.all { it is String })
                Collections.unmodifiableSet(HashSet(value.filterIsInstance<String>()))
            }
            else -> error("Unsupported rule value")
        }
    })
    private val patterns = this.values.filterKeys { it.endsWith("_rE") }.mapValues { (_, v) ->
        (v as Set<*>).map { Regex(it as String) }
    }
    override val backendName get() = metadata.activeBackend
    override val isReadable get() = getLong(XposedConfigKeys.REVISION, 0) > 0
    override fun contains(key: String) = values.containsKey(key)
    override fun refresh() = Unit
    override fun getBoolean(key: String, defaultValue: Boolean) = values[key] as? Boolean ?: defaultValue
    override fun getLong(key: String, defaultValue: Long) = values[key] as? Long ?: defaultValue
    @Suppress("UNCHECKED_CAST")
    override fun getStringSet(key: String) = values[key] as? Set<String> ?: emptySet()
    override fun matches(key: String, name: String) = patterns[key]?.any { it.matches(name) } == true
    override fun status() = metadata.copy(observedRevision = getLong(XposedConfigKeys.REVISION, 0))
}
