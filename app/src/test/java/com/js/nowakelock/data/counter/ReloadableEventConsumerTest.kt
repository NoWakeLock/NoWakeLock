package com.js.nowakelock.data.counter

import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import java.util.concurrent.CopyOnWriteArrayList

class ReloadableEventConsumerTest {
    @Test fun `late old producers and new producers share one queue across retirement`() {
        val oldQueue = BoundedEventQueue<Array<Any>>(4096)
        val signal = Semaphore(0)
        val result = CopyOnWriteArrayList<Int>()
        val old = ReloadableEventConsumer(signal) { oldQueue.poll()?.let { result.add(it[0] as Int) } }
        old.start()
        oldQueue.offer(arrayOf(0)); signal.release()
        assertTrue(old.stop(1000))
        // In-flight old callback resumes after its worker has stopped.
        oldQueue.offer(arrayOf(1)); signal.release()
        val newQueue = BoundedEventQueue.adopt<Array<Any>>(oldQueue.transferState())
        val done = CountDownLatch(1)
        val next = ReloadableEventConsumer(signal) {
            newQueue.poll()?.let {
                result.add(it[0] as Int)
                if (it[0] == 100) done.countDown()
            }
        }
        try {
            next.start()
            for (i in 2..100) {
                assertTrue((if (i % 2 == 0) oldQueue else newQueue).offer(arrayOf(i)))
                signal.release()
            }
            assertTrue(done.await(2, TimeUnit.SECONDS))
            assertEquals((0..100).toList(), result.toList())
            assertEquals(0, newQueue.size)
            assertEquals(0L, newQueue.rejected.get())
        } finally { assertTrue(next.stop(1000)) }
    }

    @Test fun `timed out retirement refuses and preserves old consumer progress`() {
        val entered = CountDownLatch(1)
        val release = CountDownLatch(1)
        val next = CountDownLatch(1)
        val signal = Semaphore(0)
        var calls = 0
        val consumer = ReloadableEventConsumer(signal) {
            if (++calls == 1) { entered.countDown(); release.await() } else next.countDown()
        }
        try {
            consumer.start(); signal.release()
            assertTrue(entered.await(1, TimeUnit.SECONDS))
            assertFalse(consumer.stop(20))
            release.countDown(); signal.release()
            assertTrue(next.await(1, TimeUnit.SECONDS))
        } finally { release.countDown(); assertTrue(consumer.stop(1000)) }
    }
}
