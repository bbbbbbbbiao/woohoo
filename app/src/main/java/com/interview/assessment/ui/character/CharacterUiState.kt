package com.interview.assessment.ui.character

import com.interview.assessment.data.model.Character

/**
 *  角色列表页面可能出现的界面状态
 */

sealed interface CharacterUIState {
    /**
     *  正在加载角色数据
     */
    data object Loading : CharacterUIState

    /**
     *  角色数据加载成功
     *
     * @param characters 当前获取到的角色列表
     * @param hasNextPage 是否还有下一页，用于后续实现分页
     */
    data class Success(
        val characters : List<Character>,
        val hasNextPage : Boolean
    ) : CharacterUIState

    /**
     *  角色数据加载失败
     *
     * @param message 展示给用户的错误信息
     */
    data class Error(
        val message : String
    ) : CharacterUIState

}