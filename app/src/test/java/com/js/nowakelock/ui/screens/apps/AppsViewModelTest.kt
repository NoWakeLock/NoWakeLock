package com.js.nowakelock.ui.screens.apps

import androidx.lifecycle.SavedStateHandle
import com.js.nowakelock.data.db.entity.AppDA
import com.js.nowakelock.data.db.entity.AppInfo
import com.js.nowakelock.data.model.AppWithStats
import com.js.nowakelock.data.model.UserInfo
import com.js.nowakelock.data.repository.appdas.AppDasRepo
import com.js.nowakelock.testutil.MainDispatcherRule
import com.js.nowakelock.ui.navigation.params.AppsScreenParams
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun previousSortFlowDoesNotOverwriteStateAfterSwitchingBack() = runTest {
        val repo = FakeAppDasRepo()
        val viewModel = AppsViewModel(
            appDasRepo = repo,
            savedStateHandle = SavedStateHandle(
                mapOf(
                    AppsScreenParams.CURRENT_USER_ID to 0,
                    AppsScreenParams.SORT_OPTION to SortOption.NAME
                )
            )
        )

        advanceUntilIdle()

        val alphabeticalApps = listOf(
            appWithStats(label = "Alpha", packageName = "pkg.alpha"),
            appWithStats(label = "Beta", packageName = "pkg.beta")
        )
        val countSortedApps = listOf(
            appWithStats(label = "Beta", packageName = "pkg.beta", wakelockCount = 5),
            appWithStats(label = "Alpha", packageName = "pkg.alpha", wakelockCount = 1)
        )

        repo.nameFlow.emit(alphabeticalApps)
        advanceUntilIdle()
        assertEquals(listOf("Alpha", "Beta"), viewModel.uiState.value.apps.labels())

        viewModel.changeSortOption(SortOption.COUNT)
        advanceUntilIdle()
        repo.countFlow.emit(countSortedApps)
        advanceUntilIdle()
        assertEquals(listOf("Beta", "Alpha"), viewModel.uiState.value.apps.labels())

        viewModel.changeSortOption(SortOption.NAME)
        advanceUntilIdle()
        repo.nameFlow.emit(alphabeticalApps)
        advanceUntilIdle()
        assertEquals(listOf("Alpha", "Beta"), viewModel.uiState.value.apps.labels())

        repo.countFlow.emit(countSortedApps)
        advanceUntilIdle()

        assertEquals(
            "After switching back to NAME, the stale COUNT collector must not update the screen anymore.",
            listOf("Alpha", "Beta"),
            viewModel.uiState.value.apps.labels()
        )
    }

    private fun List<AppWithStats>.labels(): List<String> = map { it.appInfo.label }

    private fun appWithStats(
        label: String,
        packageName: String,
        wakelockCount: Int = 0,
        alarmCount: Int = 0,
        serviceCount: Int = 0
    ): AppWithStats {
        return AppWithStats(
            appInfo = AppInfo(
                packageName = packageName,
                label = label,
                userId = 0
            ),
            wakelockCount = wakelockCount,
            alarmCount = alarmCount,
            serviceCount = serviceCount
        )
    }

    private class FakeAppDasRepo : AppDasRepo {
        val nameFlow = MutableSharedFlow<List<AppWithStats>>(replay = 1)
        val countFlow = MutableSharedFlow<List<AppWithStats>>(replay = 1)
        val timeFlow = MutableSharedFlow<List<AppWithStats>>(replay = 1)

        override fun getAppDAs(): Flow<List<AppDA>> = emptyFlow()

        override suspend fun getAppInfo(packageName: String, useId: Int): AppInfo {
            return AppInfo(packageName = packageName, userId = useId)
        }

        override suspend fun syncAppInfos() = Unit

        override suspend fun syncInfos() = Unit

        override fun getAppsWithStats(): Flow<List<AppWithStats>> = nameFlow

        override fun getAppsWithStatsSortedByName(): Flow<List<AppWithStats>> = nameFlow

        override fun getAppsWithStatsSortedByCount(): Flow<List<AppWithStats>> = countFlow

        override fun getAppsWithStatsSortedByTime(): Flow<List<AppWithStats>> = timeFlow

        override fun getUserAppsWithStats(): Flow<List<AppWithStats>> = nameFlow

        override fun getSystemAppsWithStats(): Flow<List<AppWithStats>> = nameFlow

        override fun getModifiedAppsWithStats(): Flow<List<AppWithStats>> = nameFlow

        override suspend fun getAvailableUsers(): List<UserInfo> = emptyList()
    }
}
