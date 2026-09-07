package com.js.nowakelock.data.counter

import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/** Multi-producer/single-consumer. Producers never wait for the consumer or a mutex. */
class BoundedEventQueue<T>(val capacity: Int) {
    init { require(capacity > 0) }
    private val queue = ConcurrentLinkedQueue<T>()
    private val reserved = AtomicInteger()
    val rejected = AtomicLong()
    val size: Int get() = reserved.get()
    fun offer(value: T): Boolean {
        // Bound retry work under extreme producer contention as well as memory usage.
        repeat(16) {
            val count = reserved.get()
            if (count >= capacity) { rejected.incrementAndGet(); return false }
            if (reserved.compareAndSet(count, count + 1)) {
                queue.offer(value)
                return true
            }
        }
        rejected.incrementAndGet()
        return false
    }
    fun poll(): T? = queue.poll()?.also { reserved.decrementAndGet() }
}
