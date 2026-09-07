package com.js.nowakelock.data.counter

import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger

class BoundedEventQueueTest {
    @Test fun `capacity bounds memory and rejection is observable`() {
        val queue = BoundedEventQueue<Int>(2)
        assertTrue(queue.offer(1)); assertTrue(queue.offer(2)); assertFalse(queue.offer(3))
        assertEquals(2, queue.size); assertEquals(1L, queue.rejected.get())
        assertEquals(1, queue.poll()); assertEquals(2, queue.poll()); assertNull(queue.poll())
        assertTrue(queue.offer(4)); assertEquals(4, queue.poll())
    }
    @Test fun `concurrent producers preserve their order and account for all submissions`() {
        val queue = BoundedEventQueue<Pair<Int, Int>>(1000)
        val done = CountDownLatch(4)
        val accepted = AtomicInteger()
        repeat(4) { producer -> Thread {
            try { repeat(10000) { if (queue.offer(producer to it)) accepted.incrementAndGet() } }
            finally { done.countDown() }
        }.start() }
        val last = IntArray(4) { -1 }
        var consumed = 0
        while (done.count > 0 || queue.size > 0) {
            val item = queue.poll() ?: continue
            assertTrue(item.second > last[item.first])
            last[item.first] = item.second
            consumed++
        }
        assertEquals(accepted.get(), consumed)
        assertEquals(40000L, consumed + queue.rejected.get())
    }
}
