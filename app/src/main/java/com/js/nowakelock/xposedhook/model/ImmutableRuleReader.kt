package com.js.nowakelock.xposedhook.model

import com.js.nowakelock.data.config.ConfigBackendStatus
import com.js.nowakelock.data.config.XposedConfigKeys
import java.util.Collections
import java.util.regex.Pattern

/** Fully built on the writer thread. No mutable collection escapes this object. */
class ImmutableRuleReader private constructor(private val frame: Array<Any>,
    private val metadata: ConfigBackendStatus) : HookConfigReader {
    constructor(values: Map<String, *>, metadata: ConfigBackendStatus) : this(prepare(values), metadata)
    // Reuse the decision facade; do not allocate one object per intercepted call.
    val decision = XpNSP(this)
    @Suppress("UNCHECKED_CAST") val values = frame[0] as Map<String, Any>
    @Suppress("UNCHECKED_CAST") private val patterns = frame[1] as Map<String, List<Pattern>>
    fun transferState(): Array<Any> = frame
    companion object {
        fun fromPrepared(frame: Array<Any>, metadata: ConfigBackendStatus) = ImmutableRuleReader(frame, metadata)
        private fun prepare(source: Map<String, *>): Array<Any> {
            val values = HashMap<String, Any>()
            val patterns = HashMap<String, List<Pattern>>()
            source.forEach { (key, value) ->
                values[key] = when (value) {
                    is Boolean, is Long -> value
                    is Set<*> -> {
                        require(value.all { it is String })
                        Collections.unmodifiableSet(value.filterIsInstanceTo(HashSet<String>()))
                    }
                    else -> error("Unsupported rule value")
                }
                if (key.endsWith("_rE")) {
                    patterns[key] = Collections.unmodifiableList((value as Set<*>).mapTo(ArrayList()) { Pattern.compile(it as String) })
                }
            }
            return arrayOf(Collections.unmodifiableMap(values), Collections.unmodifiableMap(patterns))
        }
    }
    override val backendName get() = metadata.activeBackend
    override val isReadable get() = getLong(XposedConfigKeys.REVISION, 0) > 0
    override fun contains(key: String) = values.containsKey(key)
    override fun refresh() = Unit
    override fun getBoolean(key: String, defaultValue: Boolean) = values[key] as? Boolean ?: defaultValue
    override fun getLong(key: String, defaultValue: Long) = values[key] as? Long ?: defaultValue
    @Suppress("UNCHECKED_CAST")
    override fun getStringSet(key: String) = values[key] as? Set<String> ?: emptySet()
    override fun matches(key: String, name: String) = patterns[key]?.any { it.matcher(name).matches() } == true
    override fun status() = metadata.copy(observedRevision = getLong(XposedConfigKeys.REVISION, 0))
}
