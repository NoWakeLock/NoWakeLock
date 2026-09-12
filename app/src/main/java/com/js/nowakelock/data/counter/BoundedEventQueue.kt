package com.js.nowakelock.data.counter

import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/** Multi-producer/single-consumer. Producers never wait for the consumer or a mutex. */
class BoundedEventQueue<T> private constructor(val capacity: Int,
    private val queue: ConcurrentLinkedQueue<T>, private val reserved: AtomicInteger,
    val rejected: AtomicLong) {
    constructor(capacity: Int) : this(capacity, ConcurrentLinkedQueue(), AtomicInteger(), AtomicLong())
    init { require(capacity > 0) }
    /** Live shared data, never this module-defined wrapper or module-defined queue entries. */
    fun transferState(): Array<Any> = arrayOf(capacity, queue, reserved, rejected)
    companion object {
        @Suppress("UNCHECKED_CAST")
        fun <T> adopt(state: Array<Any>): BoundedEventQueue<T> {
            require(state.size == 4)
            return BoundedEventQueue(state[0] as Int, state[1] as ConcurrentLinkedQueue<T>,
                state[2] as AtomicInteger, state[3] as AtomicLong)
        }
    }
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
