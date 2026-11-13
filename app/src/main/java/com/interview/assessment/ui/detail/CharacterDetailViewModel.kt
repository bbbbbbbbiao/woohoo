package com.interview.assessment.ui.detail

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
 * 角色详情页的 ViewModel。
 *
 * 使用列表传来的角色 ID 请求单个角色接口，并保存旋转屏幕后的页面状态。
 */
class CharacterDetailViewModel(
    private val repository: CharacterRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CharacterDetailUiState>(CharacterDetailUiState.Idle)
    val uiState: StateFlow<CharacterDetailUiState> = _uiState.asStateFlow()

    private var currentCharacterId: Int? = null

    /** 请求详情；同一个角色已经成功加载时不会重复请求。 */
    fun loadCharacter(characterId: Int, forceRefresh: Boolean = false) {
        val alreadyLoaded = currentCharacterId == characterId &&
            _uiState.value is CharacterDetailUiState.Success
        if (alreadyLoaded && !forceRefresh) return

        currentCharacterId = characterId
        viewModelScope.launch {
            _uiState.value = CharacterDetailUiState.Loading
            try {
                val character = repository.getCharacter(characterId)
                _uiState.value = CharacterDetailUiState.Success(character)
            } catch (exception: CancellationException) {
                // 页面销毁导致的协程取消不应转换成错误页面。
                throw exception
            } catch (exception: Exception) {
                _uiState.value = CharacterDetailUiState.Error(
                    exception.localizedMessage ?: "详情加载失败，请检查网络后重试"
                )
            }
        }
    }
}

/** 为详情 ViewModel 注入同一个 Repository 的工厂。 */
class CharacterDetailViewModelFactory(
    private val repository: CharacterRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(CharacterDetailViewModel::class.java)) {
            "未知的 ViewModel 类型：${modelClass.name}"
        }
        return CharacterDetailViewModel(repository) as T
    }
}
