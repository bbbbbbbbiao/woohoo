package com.interview.assessment.data.remote

import com.interview.assessment.data.model.CharacterResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 *  Rick and Motry 网络请求接口
 */
interface RickAndMortyApi {

    /**
     *  获取角色列表
     *
     * @param page 需要请求的页码，默认请求第 1 页
     * @return 接口返回的分页信息和角色列表
     */
    @GET("api/character")
    suspend fun getCharacters(
        @Query("page") page : Int = 1
    ) : CharacterResponse
}