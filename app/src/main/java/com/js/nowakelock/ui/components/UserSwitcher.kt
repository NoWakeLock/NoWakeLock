package com.js.nowakelock.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.js.nowakelock.data.model.UserInfo

/**
 * User switcher component that displays a dropdown menu with available users
 * ensure the layout size is consistent, avoid the search icon position change when the screen is switched
 *
 * @param currentUserId The ID of the currently selected user
 * @param availableUsers List of available users to display in the dropdown
 * @param onUserChanged Callback that is triggered when a user is selected
 */
@Composable
fun UserSwitcher(
    currentUserId: Int,
    availableUsers: List<UserInfo>,
    onUserChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    // find the current selected user
    val currentUser = availableUsers.find { it.userId == currentUserId }
        ?: UserInfo.createPrimaryUser()
    
    Box(modifier = modifier) {
        IconButton(
            onClick = { expanded = true },
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .size(48.dp)
        ) {
            // display the user icon, use a fixed size to ensure layout consistency
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "User: ${currentUser.displayName}",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.5f)
        ) {
            Text(
                text = "Select User",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            Divider()
            
            availableUsers.forEach { user ->
                DropdownMenuItem(
                    text = { Text(user.displayName) },
                    onClick = {
                        onUserChanged(user.userId)
                        expanded = false
                    },
                    leadingIcon = {
                        if (user.userId == currentUserId) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )
            }
        }
    }
} 