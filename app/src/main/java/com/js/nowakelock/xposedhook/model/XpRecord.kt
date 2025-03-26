package com.js.nowakelock.xposedhook.model

import android.content.Context
import android.os.Bundle
import com.js.nowakelock.data.db.Type
import com.js.nowakelock.data.provider.ProviderMethod
import com.js.nowakelock.data.provider.getURI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * XpRecord handles communication with the Content Provider
 * to record events and statistics for tracked system activities.
 */
object XpRecord {
    /**
     * Record a new event (non-blocked)
     *
     * @param name Event name
     * @param packageName Package name
     * @param type Event type
     * @param context Context
     * @param userId User ID
     * @param startTime Event start time
     * @return Bundle containing the eventKey
     */
    fun addEvent(
        name: String, packageName: String, type: Type,
        context: Context, userId: Int = 0, startTime: Long = System.currentTimeMillis()
    ): Bundle? {
        val args = Bundle().apply {
            putString("name", name)
            putString("type", type.value)
            putString("packageName", packageName)
            putInt("userId", userId)
            putLong("startTime", startTime)
            putBoolean("isBlocked", false)
        }
        
        return getCPResult(context, ProviderMethod.RecordEvent.value, args)
    }
    
    /**
     * Record a blocked event
     *
     * @param name Event name
     * @param packageName Package name
     * @param type Event type
     * @param context Context
     * @param userId User ID
     * @param startTime Event start time
     */
    fun blockEvent(
        name: String, packageName: String, type: Type,
        context: Context, userId: Int = 0, startTime: Long = System.currentTimeMillis()
    ) = CoroutineScope(Dispatchers.Default).launch {
        val args = Bundle().apply {
            putString("name", name)
            putString("type", type.value)
            putString("packageName", packageName)
            putInt("userId", userId)
            putLong("startTime", startTime)
            putBoolean("isBlocked", true)
        }
        
        getCPResult(context, ProviderMethod.RecordEvent.value, args)
    }
    
    /**
     * Record event end using event key
     *
     * @param name Event name
     * @param packageName Package name
     * @param context Context
     * @param userId User ID
     * @param endTime Event end time
     * @param eventKey Unique event key for direct lookup
     */
    fun endEventWithKey(
        name: String, packageName: String,
        context: Context, userId: Int = 0, endTime: Long = System.currentTimeMillis(),
        eventKey: String
    ) = CoroutineScope(Dispatchers.Default).launch {
        val args = Bundle().apply {
            putString("name", name)
            putString("type", Type.Wakelock.value)
            putString("packageName", packageName)
            putInt("userId", userId)
            putLong("endTime", endTime)
            putString("eventKey", eventKey)
        }
        
        getCPResult(context, ProviderMethod.EndEvent.value, args)
    }
    
    /**
     * Record event end by reconstructing the event key
     *
     * @param name Event name
     * @param packageName Package name
     * @param context Context
     * @param userId User ID
     * @param endTime Event end time
     * @param startTime Original event start time
     */
    fun endEvent(
        name: String, packageName: String,
        context: Context, userId: Int = 0, endTime: Long = System.currentTimeMillis(),
        startTime: Long = 0
    ) = CoroutineScope(Dispatchers.Default).launch {
        val args = Bundle().apply {
            putString("name", name)
            putString("type", Type.Wakelock.value)
            putString("packageName", packageName)
            putInt("userId", userId)
            putLong("endTime", endTime)
            putLong("startTime", startTime)
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
        return getCPResult(context, ProviderMethod.CheckHookActive.value, Bundle())
    }

    /**
     * Execute Content Provider call
     */
    private fun getCPResult(context: Context, method: String, args: Bundle): Bundle? {
        val contentResolver = context.contentResolver
        return contentResolver.call(getURI(), "NoWakelock", method, args)
    }
}