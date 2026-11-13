package com.interview.assessment.ui.character

import com.interview.assessment.data.model.Character

/** 角色列表页面可能出现的界面状态。 */
sealed interface CharacterUiState {

    /** 首次进入页面时正在加载数据。 */
    data object Loading : CharacterUiState

    /**
     * 数据加载成功。
     *
     * @param characters 已经加载的全部角色
     * @param hasNextPage 是否还能继续加载下一页
     * @param isLoadingMore 是否正在加载下一页
     * @param loadMoreError 下一页加载失败时需要显示的提示
     */
    data class Success(
        val characters: List<Character>,
        val hasNextPage: Boolean,
        val isLoadingMore: Boolean = false,
        val loadMoreError: String? = null
    ) : CharacterUiState

    /** 首次加载失败，此时页面显示错误区域和重试按钮。 */
    data class Error(
        val message: String
    ) : CharacterUiState
}
