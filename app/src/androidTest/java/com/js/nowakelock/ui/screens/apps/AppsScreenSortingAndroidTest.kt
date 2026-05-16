package com.js.nowakelock.ui.screens.apps

import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.js.nowakelock.data.db.entity.AppDA
import com.js.nowakelock.data.db.entity.AppInfo
import com.js.nowakelock.data.model.AppWithStats
import com.js.nowakelock.data.model.UserInfo
import com.js.nowakelock.data.repository.appdas.AppDasRepo
import com.js.nowakelock.ui.navigation.params.AppsScreenParams
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppsScreenSortingAndroidTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun previousSortFlowDoesNotReorderVisibleListAfterSwitchingBack() {
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

        composeRule.setContent {
            MaterialTheme {
                AppsScreen(
                    viewModel = viewModel,
                    currentUserId = 0
                )
            }
        }

        SystemClock.sleep(400)

        val alphabeticalApps = listOf(
            appWithStats(label = "Alpha", packageName = "pkg.alpha"),
            appWithStats(label = "Beta", packageName = "pkg.beta", wakelockCount = 5)
        )
        val countSortedApps = listOf(
            appWithStats(label = "Beta", packageName = "pkg.beta", wakelockCount = 5),
            appWithStats(label = "Alpha", packageName = "pkg.alpha", wakelockCount = 1)
        )

        runBlocking { repo.nameFlow.emit(alphabeticalApps) }
        composeRule.waitForIdle()
        assertLabelOrder(topLabel = "Alpha", bottomLabel = "Beta")

        composeRule.runOnIdle {
            viewModel.changeSortOption(SortOption.COUNT)
        }
        SystemClock.sleep(300)
        runBlocking { repo.countFlow.emit(countSortedApps) }
        composeRule.waitForIdle()
        assertLabelOrder(topLabel = "Beta", bottomLabel = "Alpha")

        composeRule.runOnIdle {
            viewModel.changeSortOption(SortOption.NAME)
        }
        SystemClock.sleep(300)
        runBlocking { repo.nameFlow.emit(alphabeticalApps) }
        composeRule.waitForIdle()
        assertLabelOrder(topLabel = "Alpha", bottomLabel = "Beta")

        runBlocking { repo.countFlow.emit(countSortedApps) }
        composeRule.waitForIdle()

        assertLabelOrder(topLabel = "Alpha", bottomLabel = "Beta")
    }

    private fun assertLabelOrder(topLabel: String, bottomLabel: String) {
        val topNode = composeRule.onNodeWithText(topLabel).fetchSemanticsNode()
        val bottomNode = composeRule.onNodeWithText(bottomLabel).fetchSemanticsNode()

        assertTrue(
            "Expected '$topLabel' to appear above '$bottomLabel' in the list.",
            topNode.boundsInRoot.top < bottomNode.boundsInRoot.top
        )
    }

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
