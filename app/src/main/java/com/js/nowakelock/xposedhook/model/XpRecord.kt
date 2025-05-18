package com.js.nowakelock.xposedhook.model

import android.content.Context
import android.os.Bundle
import com.js.nowakelock.data.db.Type
import com.js.nowakelock.data.provider.ProviderMethod
import com.js.nowakelock.data.provider.getURI
import com.js.nowakelock.xposedhook.XpUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.locks.ReentrantReadWriteLock

/**
 * XpRecord handles communication with the Content Provider
 * to record events and statistics for tracked system activities.
 */
object XpRecord {
    // Cache mechanism for Content Provider calls
    private data class CpRequest(
        val context: Context,
        val method: String,
        val args: Bundle
    )
    
    // Thread-safe queue for CP requests with FIFO ordering
    private val requestQueue = ConcurrentLinkedQueue<CpRequest>()
    
    // Lock for synchronizing queue operations
    private val lock = ReentrantReadWriteLock()
    
    // Timer for processing the queue
    private var timer: Timer? = null
    
    // Maximum queue size to prevent memory issues
    private const val MAX_QUEUE_SIZE = 500
    
    // Delay for processing queue (in milliseconds)
    private const val QUEUE_PROCESS_DELAY = 1000L // 1 second

    private fun newEvent(
        name: String,
        packageName: String,
        type: Type,
        context: Context,
        userId: Int = 0,
        startTime: Long = System.currentTimeMillis(),
        isBlocked: Boolean,
        instanceId: String = ""
    ) = CoroutineScope(Dispatchers.Default).launch {
        val instanceIdKey = instanceId.ifEmpty {
            "${name}_${startTime}"
        }

        val args = Bundle().apply {
            putString("name", name)
            putString("type", type.value)
            putString("packageName", packageName)
            putInt("userId", userId)
            putLong("startTime", startTime)
            putBoolean("isBlocked", isBlocked)
            putString("instanceId", instanceIdKey)
        }

        getCPResult(context, ProviderMethod.NewEvent.value, args)
    }

    /**
     * Record a new event (non-blocked)
     *
     * @param name Event name
     * @param packageName Package name
     * @param type Event type
     * @param context Context
     * @param userId User ID
     * @param startTime Event start time
     * @param instanceId Unique instance ID based on IBinder hash
     * @return Bundle containing the eventKey
     */
    fun newEvent(
        name: String,
        packageName: String,
        type: Type,
        context: Context,
        userId: Int = 0,
        startTime: Long = System.currentTimeMillis(),
        instanceId: String = ""
    ) {
//        XpUtil.log("newEvent: $name, $packageName, $type, $userId, $startTime, $instanceId")
        newEvent(
            name = name,
            packageName = packageName,
            type = type,
            context = context,
            userId = userId,
            startTime = startTime,
            isBlocked = false,
            instanceId = instanceId
        )
    }

    /**
     * Record a new event (non-blocked)
     *
     * @param name Event name
     * @param packageName Package name
     * @param type Event type
     * @param context Context
     * @param userId User ID
     * @param startTime Event start time
     * @param instanceId Unique instance ID based on IBinder hash
     * @return Bundle containing the eventKey
     */
    fun blockEvent(
        name: String,
        packageName: String,
        type: Type,
        context: Context,
        userId: Int = 0,
        startTime: Long = System.currentTimeMillis(),
        instanceId: String = ""
    ) {
//        XpUtil.log("blockEvent: $name, $packageName, $type, $userId, $startTime, $instanceId")
        newEvent(
            name = name,
            packageName = packageName,
            type = type,
            context = context,
            userId = userId,
            startTime = startTime,
            isBlocked = true,
            instanceId = instanceId
        )
    }

    /**
     * Record event end by reconstructing the event key
     *
     * @param name Event name
     * @param packageName Package name
     * @param context Context
     * @param userId User ID
     * @param endTime Event end time
     * @param instanceId Unique instance ID based on IBinder hash
     */
    fun endEvent(
        name: String,
        packageName: String,
        type: Type,
        context: Context,
        userId: Int = 0,
        startTime: Long,
        endTime: Long = System.currentTimeMillis(),
        instanceId: String = ""
    ) = CoroutineScope(Dispatchers.Default).launch {
//        XpUtil.log("endEvent: $name, $packageName, $type, $userId, $startTime ,$endTime, $instanceId")
        if (type != Type.Wakelock && instanceId.isEmpty()) {
            return@launch
        }

        val args = Bundle().apply {
            putString("name", name)
            putString("type", Type.Wakelock.value)
            putString("packageName", packageName)
            putInt("userId", userId)
            putLong("startTime", startTime)
            putLong("endTime", endTime)
            putString("instanceId", instanceId)
        }

        getCPResult(context, ProviderMethod.EndEvent.value, args)
    }

    /**
     * Clear all counts and event data
     *
     * @param context Context
     * @param clearAll Whether to clear all data including statistics
     */
    fun clearData(
        context: Context, clearAll: Boolean = false
    ) = CoroutineScope(Dispatchers.Default).launch {
        val args = Bundle().apply {
            putBoolean("clearAll", clearAll)
        }

        getCPResult(context, ProviderMethod.ClearData.value, args)
    }

    /**
     * Check if hook is active
     *
     * @param context Context
     * @return Bundle with status information
     */
    fun checkHookActive(context: Context): Bundle? {
        // This method requires immediate return value, don't use caching
        return directCPResult(context, ProviderMethod.CheckHookActive.value, Bundle())
    }

    /**
     * Execute Content Provider call with caching
     * Requests are queued and processed in order to reduce IPC calls
     */
    private fun getCPResult(context: Context, method: String, args: Bundle): Bundle? {
        // Methods that require immediate result should bypass the cache
        if (method == ProviderMethod.CheckHookActive.value || 
            method == ProviderMethod.CheckHookEffectiveness.value ||
            method == ProviderMethod.CheckSharedPreferencesPath.value) {
            return directCPResult(context, method, args)
        }
        
        lock.writeLock().lock()
        try {
            // Check if queue is too large (unlikely but safety measure)
            if (requestQueue.size >= MAX_QUEUE_SIZE) {
                // Process queue immediately if too large
                processQueue()
            }
            
            // Add request to queue
            requestQueue.add(CpRequest(context, method, args))
            
            // Schedule processing if not already scheduled
            if (timer == null) {
                timer = Timer()
                timer?.schedule(object : TimerTask() {
                    override fun run() {
                        processQueue()
                    }
                }, QUEUE_PROCESS_DELAY)
            }
        } finally {
            lock.writeLock().unlock()
        }
        
        // Return null as the request will be processed later
        return null
    }
    
    /**
     * Direct CP call without caching - for methods requiring immediate response
     */
    private fun directCPResult(context: Context, method: String, args: Bundle): Bundle? {
        val contentResolver = context.contentResolver
        return contentResolver.call(getURI(), "NoWakelock", method, args)
    }
    
    /**
     * Process all queued CP requests in FIFO order
     */
    private fun processQueue() {
        val requests = mutableListOf<CpRequest>()
        
        lock.writeLock().lock()
        try {
            // Get all requests from queue while maintaining order
            while (requestQueue.isNotEmpty()) {
                requestQueue.poll()?.let { requests.add(it) }
            }
            
            // Reset timer
            timer?.cancel()
            timer = null
        } finally {
            lock.writeLock().unlock()
        }
        
        // Process all requests in order they were received
        for (request in requests) {
            try {
                val contentResolver = request.context.contentResolver
                contentResolver.call(getURI(), "NoWakelock", request.method, request.args)
            } catch (e: Exception) {
                XpUtil.log("Error in batch CP processing: ${e.message}")
            }
        }
    }
}