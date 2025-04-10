package com.js.nowakelock.data.model

import com.js.nowakelock.data.db.Type

/**
 * Represents detailed information about a Device Automation item (Wakelock/Alarm/Service)
 */
data class DAInfoEntry(
    val id: String,
    val name: String,
    val type: Type, // "wakelock", "alarm", "service"
    val packageName: String?,
    val safeToBlock: String, // "safe", "risky", "dangerous"
    val description: String,
    val recommendation: String?,
    val warning: String?,
    val pattern: String? = null,
    val tags: List<String> = emptyList()
)
