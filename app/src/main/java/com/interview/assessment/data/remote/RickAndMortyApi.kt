package com.interview.assessment.data.remote

import com.interview.assessment.data.model.Character
import com.interview.assessment.data.model.CharacterResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Rick and Morty 网络请求接口。
 */
interface RickAndMortyApi {

    /**
     * 获取角色列表。
     *
     * @param page 需要请求的页码，默认请求第 1 页
     * @return 接口返回的分页信息和角色列表
     */
    @GET("api/character")
    suspend fun getCharacters(
        @Query("page") page: Int = 1
    ): CharacterResponse

    /** 根据角色 ID 请求单个角色详情。 */
    @GET("api/character/{id}")
    suspend fun getCharacter(
        @Path("id") id: Int
    ): Character
}
