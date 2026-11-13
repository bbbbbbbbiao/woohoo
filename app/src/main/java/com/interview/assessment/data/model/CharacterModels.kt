package com.interview.assessment.data.model

data class CharacterResponse(
    val info: PageInfo,
    val results: List<Character>
)

data class PageInfo(
    val count: Int,
    val pages: Int,
    val next: String?,
    val prev: String?
)

/**
 * API 返回的名称与链接组合，例如角色的出生地和当前位置。
 */
data class NamedResource(
    val name: String = "未知",
    val url: String = ""
)

/**
 * 单个角色的数据模型。
 *
 * 列表页使用 name、status、species、image；详情页额外展示性别、类型和地点。
 */
data class Character(
    val id: Int,
    val name: String,
    val status: String,
    val species: String,
    val type: String = "",
    val gender: String = "未知",
    val origin: NamedResource = NamedResource(),
    val location: NamedResource = NamedResource(),
    val image: String,
    val url: String
)
