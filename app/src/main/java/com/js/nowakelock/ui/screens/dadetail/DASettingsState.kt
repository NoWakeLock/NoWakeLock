package com.js.nowakelock.ui.screens.dadetail

import com.js.nowakelock.data.db.Type

/**
 * Data class representing the settings state for a device automation item.
 * @param fullBlocked Whether the item is blocked
 * @param screenOffBlock Whether to only block when the screen is off
 * @param timeInterval Time interval in seconds for which to allow the item to run
 */
data class DASettingsState(
    val fullBlocked: Boolean = false,
    val screenOffBlock: Boolean = false,
    val timeInterval: Int = 0,
    val type: Type = Type.UnKnow,
)
