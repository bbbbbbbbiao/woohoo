package com.interview.assessment.ui.character

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.interview.assessment.data.repository.CharacterRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 角色列表页面的 ViewModel。
 *
 * 负责首次加载、失败重试和滚动分页，并通过 StateFlow 向页面提供状态。
 */
class CharacterViewModel(
    private val repository: CharacterRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CharacterUiState>(CharacterUiState.Loading)
    val uiState: StateFlow<CharacterUiState> = _uiState.asStateFlow()

    private var currentPage = 0
    private var requestInProgress = false
    private var hasNextPage = true

    init {
        refresh()
    }

    /** 清空旧分页状态并重新请求第一页。 */
    fun refresh() {
        if (requestInProgress) return

        currentPage = 0
        hasNextPage = true
        loadPage(page = 1, isFirstPage = true)
    }

    /** 当列表接近底部时请求下一页。 */
    fun loadNextPage() {
        if (requestInProgress || !hasNextPage || currentPage == 0) return
        loadPage(page = currentPage + 1, isFirstPage = false)
    }

    /** 清除已经由页面展示过的分页错误，避免 Toast 重复弹出。 */
    fun consumeLoadMoreError() {
        val state = _uiState.value
        if (state is CharacterUiState.Success && state.loadMoreError != null) {
            _uiState.value = state.copy(loadMoreError = null)
        }
    }

    /** 执行实际分页请求并合并新旧数据。 */
    private fun loadPage(page: Int, isFirstPage: Boolean) {
        viewModelScope.launch {
            requestInProgress = true

            val previousState = _uiState.value as? CharacterUiState.Success
            _uiState.value = if (isFirstPage) {
                CharacterUiState.Loading
            } else {
                previousState?.copy(isLoadingMore = true, loadMoreError = null)
                    ?: CharacterUiState.Loading
            }

            try {
                val response = repository.getCharacters(page)
                currentPage = page
                hasNextPage = response.info.next != null

                // 分页时把新角色追加到旧列表，并根据 ID 去重。
                val allCharacters = if (isFirstPage) {
                    response.results
                } else {
                    (previousState?.characters.orEmpty() + response.results)
                        .distinctBy { it.id }
                }

                _uiState.value = CharacterUiState.Success(
                    characters = allCharacters,
                    hasNextPage = hasNextPage
                )
            } catch (exception: CancellationException) {
                // 协程取消属于正常生命周期行为，必须继续向上抛出。
                throw exception
            } catch (exception: Exception) {
                val message = exception.localizedMessage ?: "加载失败，请检查网络后重试"

                _uiState.value = if (isFirstPage || previousState == null) {
                    CharacterUiState.Error(message)
                } else {
                    // 下一页失败时保留当前列表，只提示用户，不清空已经加载的数据。
                    previousState.copy(
                        isLoadingMore = false,
                        loadMoreError = message
                    )
                }
            } finally {
                requestInProgress = false
            }
        }
    }
}

/** 为 CharacterViewModel 注入 Repository 的工厂。 */
class CharacterViewModelFactory(
    private val repository: CharacterRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(CharacterViewModel::class.java)) {
            "未知的 ViewModel 类型：${modelClass.name}"
        }
        return CharacterViewModel(repository) as T
    }
}
