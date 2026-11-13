package com.interview.assessment

import com.interview.assessment.data.model.Character
import com.interview.assessment.data.model.CharacterResponse
import com.interview.assessment.data.model.PageInfo
import com.interview.assessment.data.repository.CharacterRepository
import com.interview.assessment.ui.character.CharacterUiState
import com.interview.assessment.ui.character.CharacterViewModel
import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** CharacterViewModel 首次加载与分页行为测试。 */
@OptIn(ExperimentalCoroutinesApi::class)
class CharacterViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun initialLoad_success_exposesFirstPage() = runTest {
        val repository = FakeCharacterRepository(
            pages = mapOf(1 to Result.success(response(page = 1, hasNextPage = true)))
        )

        val viewModel = CharacterViewModel(repository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is CharacterUiState.Success)
        state as CharacterUiState.Success
        assertEquals(listOf(1), state.characters.map { it.id })
        assertTrue(state.hasNextPage)
        assertFalse(state.isLoadingMore)
    }

    @Test
    fun loadNextPage_success_appendsCharactersWithoutDuplicates() = runTest {
        val repository = FakeCharacterRepository(
            pages = mapOf(
                1 to Result.success(response(page = 1, hasNextPage = true)),
                2 to Result.success(response(page = 2, hasNextPage = false))
            )
        )
        val viewModel = CharacterViewModel(repository)
        advanceUntilIdle()

        viewModel.loadNextPage()
        advanceUntilIdle()

        val state = viewModel.uiState.value as CharacterUiState.Success
        assertEquals(listOf(1, 2), state.characters.map { it.id })
        assertFalse(state.hasNextPage)
    }

    @Test
    fun loadNextPage_failure_keepsExistingCharactersAndExposesMessage() = runTest {
        val repository = FakeCharacterRepository(
            pages = mapOf(
                1 to Result.success(response(page = 1, hasNextPage = true)),
                2 to Result.failure(IOException("测试网络断开"))
            )
        )
        val viewModel = CharacterViewModel(repository)
        advanceUntilIdle()

        viewModel.loadNextPage()
        advanceUntilIdle()

        val state = viewModel.uiState.value as CharacterUiState.Success
        assertEquals(listOf(1), state.characters.map { it.id })
        assertFalse(state.isLoadingMore)
        assertNotNull(state.loadMoreError)
    }

    /** 构造一页可预测的测试数据。 */
    private fun response(page: Int, hasNextPage: Boolean): CharacterResponse {
        return CharacterResponse(
            info = PageInfo(
                count = 2,
                pages = 2,
                next = if (hasNextPage) "https://example.com?page=${page + 1}" else null,
                prev = if (page > 1) "https://example.com?page=${page - 1}" else null
            ),
            results = listOf(
                Character(
                    id = page,
                    name = "角色$page",
                    status = "Alive",
                    species = "Human",
                    image = "https://example.com/$page.jpg",
                    url = "https://example.com/character/$page"
                )
            )
        )
    }

    /** 可按页返回成功或失败结果的假 Repository。 */
    private class FakeCharacterRepository(
        private val pages: Map<Int, Result<CharacterResponse>>
    ) : CharacterRepository {

        override suspend fun getCharacters(page: Int): CharacterResponse {
            return pages.getValue(page).getOrThrow()
        }

        override suspend fun getCharacter(id: Int): Character {
            error("本测试不调用详情接口")
        }
    }
}
