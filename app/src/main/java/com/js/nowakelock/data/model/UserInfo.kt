package com.js.nowakelock.data.model

/**
 * Data class for user information display in UI
 */
data class UserInfo(
    val userId: Int,
    val displayName: String,
    val isSystem: Boolean = false
) {
    companion object {
        // Primary user ID
        fun createPrimaryUser(): UserInfo = UserInfo(
            userId = 0,
            displayName = "Primary User",
            isSystem = false
        )

        // Other user
        fun fromUserId(userId: Int): UserInfo = UserInfo(
            userId = userId,
            displayName = if (userId == 0) "Primary User" else "User $userId",
            isSystem = false
        )
    }
}