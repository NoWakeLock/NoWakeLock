package com.js.nowakelock.probe;

import android.content.Context;
import android.os.PowerManager;
import android.os.SystemClock;
import org.json.JSONObject;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

/** Finite, explicitly requested test load. No per-call logging or file writes. */
final class CurrentLatencyBenchmark {
    private static final AtomicBoolean running = new AtomicBoolean();
    private static volatile boolean cancelled;

    static void stop() { cancelled = true; }

    static void run(Context activity, int count, int intervalMs) {
        Context context = activity.getApplicationContext();
        if (count < 1 || count > 3000 || intervalMs < 0 || intervalMs > 1000 ||
                (long) count * intervalMs > 120000) {
            Events.add(context, "ERROR latency invalid count/interval (maximum 120s pacing)");
            return;
        }
        if (!running.compareAndSet(false, true)) {
            Events.add(context, "ERROR latency already running");
            return;
        }
        cancelled = false;
        context.deleteFile("latency.json");
        new Thread(() -> {
            PowerManager.WakeLock lock = context.getSystemService(PowerManager.class)
                    .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "NWLProbe:Wake");
            lock.setReferenceCounted(false);
            long[] acquire = new long[count], release = new long[count];
            int completed = 0;
            Events.add(context, "LATENCY_BEGIN count=" + count + " intervalMs=" + intervalMs);
            long began = SystemClock.elapsedRealtimeNanos();
            try {
                for (; completed < count && !cancelled; completed++) {
                    long start = SystemClock.elapsedRealtimeNanos();
                    lock.acquire(2000);
                    acquire[completed] = SystemClock.elapsedRealtimeNanos() - start;
                    start = SystemClock.elapsedRealtimeNanos();
                    lock.release();
                    release[completed] = SystemClock.elapsedRealtimeNanos() - start;
                    if (intervalMs > 0) Thread.sleep(intervalMs);
                }
                long elapsed = SystemClock.elapsedRealtimeNanos() - began;
                JSONObject result = new JSONObject().put("requested", count).put("completed", completed)
                        .put("cancelled", cancelled).put("interval_ms", intervalMs).put("elapsed_ns", elapsed)
                        .put("acquire_api", summary(Arrays.copyOf(acquire, completed)))
                        .put("release_api", summary(Arrays.copyOf(release, completed)));
                try (FileOutputStream stream = context.openFileOutput("latency.json", Context.MODE_PRIVATE)) {
                    stream.write(result.toString().getBytes(StandardCharsets.UTF_8));
                }
                Events.add(context, "LATENCY_DONE count=" + completed);
            } catch (Exception error) {
                Events.add(context, "ERROR latency " + error);
            } finally {
                if (lock.isHeld()) lock.release();
                running.set(false);
            }
        }, "NWLProbe-latency").start();
    }

    private static JSONObject summary(long[] data) throws Exception {
        Arrays.sort(data);
        long sum = 0;
        for (long value : data) sum += value;
        return new JSONObject().put("samples", data.length).put("mean_ns", data.length == 0 ? 0 : (double) sum / data.length)
                .put("p50_ns", percentile(data, .5)).put("p95_ns", percentile(data, .95))
                .put("p99_ns", percentile(data, .99)).put("max_ns", data.length == 0 ? 0 : data[data.length - 1]);
    }
    private static long percentile(long[] data, double quantile) {
        return data.length == 0 ? 0 : data[Math.max(0, (int) Math.ceil(data.length * quantile) - 1)];
    }
}
