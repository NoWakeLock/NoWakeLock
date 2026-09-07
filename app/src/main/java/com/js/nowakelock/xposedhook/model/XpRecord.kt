package com.js.nowakelock.xposedhook.model

import android.content.Context
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import com.js.nowakelock.data.counter.BoundedEventQueue
import com.js.nowakelock.data.db.Type
import com.js.nowakelock.data.provider.ProviderMethod
import com.js.nowakelock.data.provider.XProvider
import com.js.nowakelock.data.provider.getURI
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.locks.LockSupport

/** Hooks submit events only. The single worker owns communication and persistence. */
object XpRecord {
    data class Event(val generation: Long, val context: Context, val method: String, val args: Bundle)
    private val generation = AtomicLong()
    private val sequence = AtomicLong()
    private val queue = BoundedEventQueue<Event>(4096)
    private val submitted = AtomicLong()
    private val failed = AtomicLong()
    private val batches = AtomicLong()
    private val directEvents = AtomicLong()
    private val providerEvents = AtomicLong()
    private val lastRuntimeReport = AtomicLong()
    @Volatile private var localProvider: XProvider? = null
    @Volatile private var lastError: String? = null
    private val worker = Thread({
        while (true) {
            val first = queue.poll()
            if (first == null) { LockSupport.park(); continue }
            // Coalesce a short burst without a periodic idle timer.
            LockSupport.parkNanos(25_000_000L)
            val batch = ArrayList<Event>(256)
            batch.add(first)
            while (batch.size < 256) batch.add(queue.poll() ?: break)
            try {
                val provider = localProvider
                if (provider != null && batch.none { it.method == ProviderMethod.ClearData.value }) {
                    provider.recordBatch(batch)
                    directEvents.addAndGet(batch.size.toLong())
                } else {
                    // The provider may be hosted in a different process on other ROMs.
                    batch.filter { it.generation == generation.get() }.forEach {
                        it.context.contentResolver.call(getURI(), "NoWakelock", it.method, it.args)
                        providerEvents.incrementAndGet()
                    }
                }
                batches.incrementAndGet()
                if (runtimeReportDue()) reportRuntime(first.context)
            } catch (e: Exception) {
                localProvider?.invalidateDuration()
                failed.addAndGet(batch.size.toLong())
                lastError = e.toString()
                Log.e("NoWakeLockRecord", "Statistics batch failed", e)
            }
        }
    }, "NWL-statistics").apply { isDaemon = true; priority = Thread.MIN_PRIORITY; start() }

    fun attachProvider(provider: XProvider) { localProvider = provider }
    fun currentGeneration(): Long = generation.get()
    fun clearGeneration() { generation.incrementAndGet() }
    fun diagnostics(): Bundle = Bundle().apply {
        putLong("recordSubmitted", submitted.get())
        putLong("recordDropped", queue.rejected.get())
        putLong("recordFailed", failed.get())
        putLong("recordBatches", batches.get())
        putLong("recordDirect", directEvents.get())
        putLong("recordProviderCalls", providerEvents.get())
        putInt("recordPending", queue.size)
        putLong("recordGeneration", generation.get())
        putString("recordLastError", lastError)
        putBoolean("recordIncomplete", queue.rejected.get() > 0 || failed.get() > 0)
    }

    private fun submit(context: Context, method: String, args: Bundle, epoch: Long) {
        submitted.incrementAndGet()
        if (queue.offer(Event(epoch, context, method, args))) LockSupport.unpark(worker)
    }
    private fun newEvent(name: String, packageName: String, type: Type, context: Context,
                         userId: Int, startTime: Long, isBlocked: Boolean, instanceId: String) {
        val epoch = generation.get()
        val enqueuedAt = SystemClock.elapsedRealtimeNanos()
        val args = Bundle().apply {
            putLong("__recordEnqueuedAt", enqueuedAt)
            putString("name", name); putString("packageName", packageName); putString("type", type.value)
            putInt("userId", userId); putLong("startTime", startTime); putBoolean("isBlocked", isBlocked)
            putString("instanceId", instanceId.ifEmpty { name + "_" + startTime + "_" + sequence.incrementAndGet() })
        }
        submit(context, ProviderMethod.NewEvent.value, args, epoch)
    }
    fun newEvent(name: String, packageName: String, type: Type, context: Context, userId: Int = 0,
                 startTime: Long = System.currentTimeMillis(), instanceId: String = "") {
        newEvent(name, packageName, type, context, userId, startTime, false, instanceId)
    }
    fun blockEvent(name: String, packageName: String, type: Type, context: Context, userId: Int = 0,
                   startTime: Long = System.currentTimeMillis(), instanceId: String = "") {
        newEvent(name, packageName, type, context, userId, startTime, true, instanceId)
    }
    fun endEvent(name: String, packageName: String, type: Type, context: Context, userId: Int = 0,
                 startTime: Long, endTime: Long = System.currentTimeMillis(), instanceId: String = "") {
        if (type != Type.Wakelock || instanceId.isEmpty()) return
        val epoch = generation.get()
        val enqueuedAt = SystemClock.elapsedRealtimeNanos()
        submit(context, ProviderMethod.EndEvent.value, Bundle().apply {
            putLong("__recordEnqueuedAt", enqueuedAt)
            putString("name", name); putString("packageName", packageName); putString("type", type.value)
            putInt("userId", userId); putLong("startTime", startTime); putLong("endTime", endTime)
            putString("instanceId", instanceId)
        }, epoch)
    }
    fun clearData(context: Context, clearAll: Boolean = false) {
        submit(context, ProviderMethod.ClearData.value, Bundle().apply { putBoolean("clearAll", clearAll) }, generation.get())
    }
    fun checkHookActive(context: Context): Bundle? =
        context.contentResolver.call(getURI(), "NoWakelock", ProviderMethod.CheckHookActive.value, Bundle())

    fun runtimeReportDue(): Boolean = SystemClock.elapsedRealtime() - lastRuntimeReport.get() >= 10_000
    fun reportRuntime(context: Context) {
        val now = SystemClock.elapsedRealtime()
        val last = lastRuntimeReport.get()
        if (now - last < 10_000 || !lastRuntimeReport.compareAndSet(last, now)) return
        try {
            val args = XpNSP.getInstance().backendStatus().toBundle().apply {
                putString("process", "system_server")
                putLong("observedAt", now)
                putString("hooks", com.js.nowakelock.xposedhook.HookInstallRegistry.summary())
            }
            context.contentResolver.call(getURI(), "NoWakelock", ProviderMethod.ReportRuntime.value, args)
        } catch (e: Exception) { lastError = e.toString() }
    }
}
