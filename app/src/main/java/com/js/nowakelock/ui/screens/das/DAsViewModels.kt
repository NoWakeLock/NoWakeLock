package com.js.nowakelock.ui.screens.das

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import com.js.nowakelock.data.db.Type
import org.koin.androidx.compose.koinViewModel
import org.koin.core.qualifier.named


@Composable
fun WakelockScreen(
    navigateToDADetail: (name: String, packageName: String) -> Unit = { _, _ -> },
    type: Type = Type.Wakelock,
//    viewModel: DAsViewModel = koinViewModel(qualifier = named("WakelockViewModel"))  // 使用命名限定符
    viewModel: DAsViewModel = koinViewModel(qualifier = named("WakelockViewModel"))

) {
    DAsScreen(
        type = type,
        viewModel = viewModel,
        navigateToDADetail = navigateToDADetail
    )
}

@Composable
fun AlarmScreen(
    navigateToDADetail: (name: String, packageName: String) -> Unit = { _, _ -> },
    type: Type = Type.Alarm,
    viewModel: DAsViewModel = koinViewModel(qualifier = named("AlarmViewModel"))  // 使用命名限定符
) {
    DAsScreen(
        type = type,
        viewModel = viewModel,
        navigateToDADetail = navigateToDADetail
    )
}

@Composable
fun ServiceScreen(
    navigateToDADetail: (name: String, packageName: String) -> Unit = { _, _ -> },
    type: Type = Type.Service,
    viewModel: DAsViewModel = koinViewModel(qualifier = named("ServiceViewModel"))  // 使用命名限定符
) {
    DAsScreen(
        type = type,
        viewModel = viewModel,
        navigateToDADetail = navigateToDADetail
    )
}
