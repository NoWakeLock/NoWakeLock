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

/**
 * XpRecord handles communication with the Content Provider
 * to record events and statistics for tracked system activities.
 */
object XpRecord {

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