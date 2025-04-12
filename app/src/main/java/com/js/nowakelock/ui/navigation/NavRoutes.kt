package com.js.nowakelock.ui.navigation

import com.js.nowakelock.data.db.Type
import kotlinx.serialization.Serializable

/**
 * Navigation routes
 */
object NavRoutes {
    const val APPS = "apps"
    const val WAKELOCKS = "wakelocks"
    const val ALARMS = "alarms"
    const val SERVICES = "services"
    const val SETTINGS = "settings"

    const val DADETAIL = "DADetail"
}

/**
 * Serializable navigation destination for the DA detail screen
 * 
 * @param daName da name
 * @param packageName package name
 * @param userId user id
 * @param type da type
 */
@Serializable
data class DADetail(
    val daName: String,
    val packageName: String? = null,
    val userId: Int = 0,
    val type: String = Type.UnKnow.value
)