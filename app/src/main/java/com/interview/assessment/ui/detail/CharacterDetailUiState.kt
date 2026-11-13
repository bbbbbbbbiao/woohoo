package com.interview.assessment.ui.detail

import com.interview.assessment.data.model.Character

/** 角色详情页的界面状态。 */
sealed interface CharacterDetailUiState {
    data object Idle : CharacterDetailUiState
    data object Loading : CharacterDetailUiState
    data class Success(val character: Character) : CharacterDetailUiState
    data class Error(val message: String) : CharacterDetailUiState
}
