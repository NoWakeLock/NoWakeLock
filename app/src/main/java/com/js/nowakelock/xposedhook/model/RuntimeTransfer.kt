package com.js.nowakelock.xposedhook.model

import android.os.IBinder
import com.js.nowakelock.data.counter.BoundedEventQueue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Semaphore
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import java.util.IdentityHashMap
import android.content.Context
import android.os.Bundle

/** Only this data graph crosses generations. Wrappers, consumers and callbacks never do. */
object RuntimeTransfer {
    private var data = ConcurrentHashMap<String, Any>(mapOf(
        "schema" to 1,
        "queue" to BoundedEventQueue<Array<Any>>(4096).transferState(),
        "signal" to Semaphore(0),
        "recordCounters" to Array(8) { AtomicLong() },
        "booted" to AtomicBoolean(),
        "wakes" to ConcurrentHashMap<IBinder, Array<Any>>(),
        "wakeTimes" to ConcurrentHashMap<String, Long>(),
        "alarmTimes" to ConcurrentHashMap<String, Long>(),
        "providerCreated" to AtomicBoolean(),
        "ruleHead" to AtomicReference<Array<Any>>()
    ))
    var modern = false
        private set
    var reloaded = false
        private set
    @Volatile var retired = false
    @Volatile var systemHookObserved = false
    @Volatile var systemInstalled = false
    @Volatile var providerInstalled = false
    @Volatile var reloadFailure: String? = null
    fun enableModern() { modern = true }
    fun state(): ConcurrentHashMap<String, Any> = data
    @Suppress("UNCHECKED_CAST")
    fun adopt(value: Any?) {
        val next = value as? ConcurrentHashMap<String, Any> ?: error("Missing reload state")
        require(next["schema"] == 1) { "Unsupported reload state schema" }
        require(next["signal"] is Semaphore && next["queue"] is Array<*>)
        require(next["booted"] is AtomicBoolean && next["providerCreated"] is AtomicBoolean)
        require((next["recordCounters"] as? Array<*>)?.let { it.size == 8 && it.all { c -> c is AtomicLong } } == true)
        data = next
        modern = true
        reloaded = true
        retired = false
    }
    @Suppress("UNCHECKED_CAST") fun <T> get(key: String): T = data[key] as T
    fun put(key: String, value: Any) { data[key] = value }

    /** Lifecycle-only graph check; never run during a hook decision. */
    fun validateNeutralState() {
        val visited = IdentityHashMap<Any, Boolean>()
        val moduleLoader = RuntimeTransfer::class.java.classLoader
        fun visit(value: Any?) {
            if (value == null || visited.put(value, true) != null) return
            check(value.javaClass.classLoader !== moduleLoader) { "Module-owned reload value: ${value.javaClass.name}" }
            check(value !== moduleLoader) { "Module classloader in reload state" }
            when (value) {
                is Array<*> -> value.forEach { visit(it) }
                is Map<*, *> -> value.forEach { (k, v) -> visit(k); visit(v) }
                is Collection<*> -> value.forEach { visit(it) }
                is AtomicReference<*> -> visit(value.get())
                is Bundle -> value.keySet().forEach { visit(value.get(it)) }
                // Opaque platform handles contain framework-owned internals.
                is Context, is IBinder, is ClassLoader, is Semaphore,
                is java.util.concurrent.atomic.AtomicInteger, is AtomicLong, is AtomicBoolean,
                is String, is Number, is Boolean, is java.util.regex.Pattern -> Unit
                else -> error("Unsupported reload value: ${value.javaClass.name}")
            }
        }
        visit(data)
    }
}
