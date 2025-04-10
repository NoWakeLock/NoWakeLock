package com.js.nowakelock.ui.screens.dadetail

/**
 * Data class representing the settings state for a device automation item.
 * @param isBlocked Whether the item is blocked
 * @param sleepOnly Whether to only block when the device is in sleep mode
 * @param screenOffOnly Whether to only block when the screen is off
 * @param timeInterval Time interval in seconds for which to allow the item to run
 */
data class DASettingsState(
    val isBlocked: Boolean = false,
    val sleepOnly: Boolean = false,
    val screenOffOnly: Boolean = false,
    val timeInterval: Int = 0
)
