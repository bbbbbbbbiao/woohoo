package com.interview.assessment.data.model

data class CharacterResponse(
    val info : PageInfo,
    val results : List<Character>
)

data class PageInfo (
    val count : Int,
    val pages : Int,
    val next : String?,
    val prev : String?
)

data class Character(
    val id : Int,
    val name : String,
    val status : String,
    val image : String,
    val url : String
)
