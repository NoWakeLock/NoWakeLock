@file:JvmName("DAsScreenKt")

package com.js.nowakelock.ui.screens.das

import androidx.compose.runtime.Composable
import com.js.nowakelock.data.db.Type
import org.koin.androidx.compose.koinViewModel
import org.koin.core.qualifier.named


@Composable
fun WakelockScreen(
    navigateToDADetail: (name: String, packageName: String) -> Unit = { _, _ -> },
    type: Type = Type.Wakelock,
    viewModel: DAsViewModel = koinViewModel(qualifier = named("WakelockViewModel")),
    isSearchActive: Boolean = false,
    onSearchActiveChange: (Boolean) -> Unit = {},
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {}
) {
    DAsScreen(
        type = type,
        viewModel = viewModel,
        navigateToDADetail = navigateToDADetail,
        isSearchActive = isSearchActive,
        onSearchActiveChange = onSearchActiveChange,
        searchQuery = searchQuery,
        onSearchQueryChange = onSearchQueryChange
    )
}

@Composable
fun AlarmScreen(
    navigateToDADetail: (name: String, packageName: String) -> Unit = { _, _ -> },
    type: Type = Type.Alarm,
    viewModel: DAsViewModel = koinViewModel(qualifier = named("AlarmViewModel")),
    isSearchActive: Boolean = false,
    onSearchActiveChange: (Boolean) -> Unit = {},
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {}
) {
    DAsScreen(
        type = type,
        viewModel = viewModel,
        navigateToDADetail = navigateToDADetail,
        isSearchActive = isSearchActive,
        onSearchActiveChange = onSearchActiveChange,
        searchQuery = searchQuery,
        onSearchQueryChange = onSearchQueryChange
    )
}

@Composable
fun ServiceScreen(
    navigateToDADetail: (name: String, packageName: String) -> Unit = { _, _ -> },
    type: Type = Type.Service,
    viewModel: DAsViewModel = koinViewModel(qualifier = named("ServiceViewModel")),
    isSearchActive: Boolean = false,
    onSearchActiveChange: (Boolean) -> Unit = {},
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {}
) {
    DAsScreen(
        type = type,
        viewModel = viewModel,
        navigateToDADetail = navigateToDADetail,
        isSearchActive = isSearchActive,
        onSearchActiveChange = onSearchActiveChange,
        searchQuery = searchQuery,
        onSearchQueryChange = onSearchQueryChange
    )
}
