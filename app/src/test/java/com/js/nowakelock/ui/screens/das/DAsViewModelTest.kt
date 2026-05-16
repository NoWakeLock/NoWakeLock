package com.js.nowakelock.ui.screens.das

import androidx.lifecycle.SavedStateHandle
import com.js.nowakelock.data.db.Type
import com.js.nowakelock.data.db.entity.St
import com.js.nowakelock.data.model.DAItem
import com.js.nowakelock.data.repository.daitem.DARepository
import com.js.nowakelock.testutil.MainDispatcherRule
import com.js.nowakelock.ui.navigation.params.DAsScreenParams
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
class DAsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun reorderWithSameItemsStillUpdatesUiState() = runTest {
        val repo = FakeDARepository()
        val viewModel = DAsViewModel(
            daRepository = repo,
            savedStateHandle = SavedStateHandle(
                mapOf(DAsScreenParams.SORT_OPTION to DASortOption.COUNT)
            )
        )

        advanceUntilIdle()

        val initialOrder = listOf(
            daItem(name = "Alpha", packageName = "pkg.alpha", count = 1),
            daItem(name = "Beta", packageName = "pkg.beta", count = 1)
        )
        val reordered = listOf(
            daItem(name = "Beta", packageName = "pkg.beta", count = 1),
            daItem(name = "Alpha", packageName = "pkg.alpha", count = 1)
        )

        repo.countFlow.emit(initialOrder)
        advanceUntilIdle()
        assertEquals(listOf("Alpha", "Beta"), viewModel.uiState.value.das.names())

        repo.countFlow.emit(reordered)
        advanceUntilIdle()

        assertEquals(
            "A changed list order must propagate to the UI even when the same items remain present.",
            listOf("Beta", "Alpha"),
            viewModel.uiState.value.das.names()
        )
    }

    @Test
    fun previousSortFlowDoesNotOverwriteUiState() = runTest {
        val repo = FakeDARepository()
        val viewModel = DAsViewModel(
            daRepository = repo,
            savedStateHandle = SavedStateHandle(
                mapOf(DAsScreenParams.SORT_OPTION to DASortOption.NAME)
            )
        )

        advanceUntilIdle()

        val alphabetical = listOf(
            daItem(name = "Alpha", packageName = "pkg.alpha", count = 1),
            daItem(name = "Beta", packageName = "pkg.beta", count = 5)
        )
        val countSorted = listOf(
            daItem(name = "Beta", packageName = "pkg.beta", count = 5),
            daItem(name = "Alpha", packageName = "pkg.alpha", count = 1)
        )

        repo.nameFlow.emit(alphabetical)
        advanceUntilIdle()
        assertEquals(listOf("Alpha", "Beta"), viewModel.uiState.value.das.names())

        viewModel.changeSortOption(DASortOption.COUNT)
        advanceUntilIdle()
        repo.countFlow.emit(countSorted)
        advanceUntilIdle()
        assertEquals(listOf("Beta", "Alpha"), viewModel.uiState.value.das.names())

        viewModel.changeSortOption(DASortOption.NAME)
        advanceUntilIdle()
        repo.nameFlow.emit(alphabetical)
        advanceUntilIdle()
        assertEquals(listOf("Alpha", "Beta"), viewModel.uiState.value.das.names())

        repo.countFlow.emit(countSorted)
        advanceUntilIdle()

        assertEquals(
            "After switching back to NAME, the stale COUNT collector must not overwrite the current list.",
            listOf("Alpha", "Beta"),
            viewModel.uiState.value.das.names()
        )
    }

    private fun List<DAItem>.names(): List<String> = map { it.name }

    private fun daItem(
        name: String,
        packageName: String,
        count: Int
    ): DAItem {
        return DAItem(
            name = name,
            packageName = packageName,
            userId = 0,
            type = Type.Wakelock,
            count = count
        )
    }

    private class FakeDARepository : DARepository {
        val nameFlow = MutableSharedFlow<List<DAItem>>(replay = 1)
        val countFlow = MutableSharedFlow<List<DAItem>>(replay = 1)
        val timeFlow = MutableSharedFlow<List<DAItem>>(replay = 1)

        override suspend fun getDAItemsSortedByName(
            packageName: String,
            userId: Int
        ): Flow<List<DAItem>> = nameFlow

        override suspend fun getDAItemsSortedByCount(
            packageName: String,
            userId: Int
        ): Flow<List<DAItem>> = countFlow

        override suspend fun getDAItemsSortedByTime(
            packageName: String,
            userId: Int
        ): Flow<List<DAItem>> = timeFlow

        override fun getSTs(type: Type): Flow<List<St>> = emptyFlow()

        override suspend fun updateDAItemSettings(setting: St) = Unit

        override suspend fun syncDB(packageName: String, userId: Int) = Unit

        override suspend fun syncEvents(
            packageName: String,
            userId: Int,
            startTime: Long,
            endTime: Long
        ) = Unit
    }
}
