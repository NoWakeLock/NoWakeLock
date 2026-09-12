package com.js.nowakelock.data.counter

import java.util.concurrent.Semaphore

/** Lifecycle locking is confined to consumer management; producers only offer and signal. */
class ReloadableEventConsumer(
    private val signal: Semaphore,
    private val consume: () -> Unit
) {
    private val lifecycle = Any()
    @Volatile private var stopping = true
    private var worker: Thread? = null

    fun start() = synchronized(lifecycle) {
        stopping = false
        startLocked()
    }

    private fun startLocked() {
        if (worker != null) return
        worker = Thread({
            try {
                while (!stopping) {
                    signal.acquire()
                    if (!stopping) consume()
                }
            } finally {
                synchronized(lifecycle) {
                    worker = null
                    // A timed-out stop is a refusal, so the original runtime must continue.
                    if (!stopping) startLocked()
                }
            }
        }, "NWL-statistics").apply {
            isDaemon = true
            priority = Thread.MIN_PRIORITY
            contextClassLoader = ClassLoader.getSystemClassLoader()
            start()
        }
    }

    /** Never interrupt a database transaction. A timeout resumes the old consumer. */
    fun stop(timeoutMs: Long): Boolean {
        require(timeoutMs > 0)
        val thread = synchronized(lifecycle) {
            stopping = true
            signal.release()
            worker
        } ?: return true
        try {
            thread.join(timeoutMs)
        } catch (_: InterruptedException) {
            start()
            Thread.currentThread().interrupt()
            return false
        }
        if (!thread.isAlive) return true
        start()
        return false
    }
}
