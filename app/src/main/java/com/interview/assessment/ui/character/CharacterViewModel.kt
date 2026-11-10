package com.interview.assessment.ui.character

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.interview.assessment.data.model.Character
import com.interview.assessment.data.repository.CharacterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 *  角色列表页面的ViewModel
 */
class CharacterViewModel : ViewModel() {
    /**
     *  数据仓库
     *
     *  viewModel 通过 Repository 获取数据
     *  不直接操作 Retrofit 网络客户端
     */
    private val repository = CharacterRepository()

    /**
     *  ViewModel 内部可以修改的页面状态
     *
     *  初始状态为Loading， 表示页面正在准备加载数据
     */
    private val _uiState = MutableStateFlow<CharacterUIState>(CharacterUIState.Loading)

    /**
     *  Activity 可以观察但不能直接修改的页面状态
     */
    val uiState : StateFlow<CharacterUIState> = _uiState.asStateFlow()

    /**
     *  viewModel 创建后自动加载第一页角色数据
     */
    init {
        loadCharacters()
    }

    /**
     *  加载第一页角色数据
     */
    fun loadCharacters() {
        viewModelScope.launch {
            // 请求开始前切换为加载状态
            _uiState.value = CharacterUIState.Loading

            try {
                // 通过 Respository 请求第一页数据
                val response = repository.getCharacters(page = 1)

                // 请求成功，将角色列表交给页面显示
                _uiState.value = CharacterUIState.Success(
                    characters = response.results,
                    hasNextPage = response.info.next != null
                )
            } catch(exception : Exception) {
                // 请求失败，用户可以看到的错误状态
                _uiState.value = CharacterUIState.Error(
                    message = exception.localizedMessage ?: "加载失败，请检查网络"
                )
            }
        }
    }
}