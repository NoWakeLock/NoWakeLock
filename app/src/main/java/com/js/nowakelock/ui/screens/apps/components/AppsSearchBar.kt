package com.js.nowakelock.ui.screens.apps.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Search bar component for apps screen
 * Using TextField from Material Design 3 for a simpler and more intuitive experience
 * Directly displays the search field without requiring expansion
 */
@Composable
fun AppsSearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onActiveChange: (Boolean) -> Unit, // Kept for API compatibility
    isSearchActive: Boolean, // Kept for API compatibility
    modifier: Modifier = Modifier
) {
    // Simple TextField implementation following MD3 guidelines
    TextField(
        value = searchQuery,
        onValueChange = { 
            onSearchQueryChange(it)
            // Call onSearch to keep real-time filtering
            onSearch(it)
        },
        placeholder = { Text("Search apps") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search"
            )
        },
        trailingIcon = {
            if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { onSearchQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear search"
                    )
                }
            }
        },
        // Using single line TextField with MD3 styling
        singleLine = true,
        // Apply colors from the Material theme
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
            unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
        ),
        shape = MaterialTheme.shapes.medium,
        // Fill max width with some padding on the sides
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
} 