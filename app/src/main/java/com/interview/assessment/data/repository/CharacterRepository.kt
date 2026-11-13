package com.interview.assessment.data.repository

import com.interview.assessment.data.model.Character
import com.interview.assessment.data.model.CharacterResponse
import com.interview.assessment.data.remote.RetrofitClient
import com.interview.assessment.data.remote.RickAndMortyApi

/**
 * 角色数据仓库契约。
 *
 * ViewModel 只依赖此接口，因此正式网络实现可以在测试时替换成假数据实现。
 */
interface CharacterRepository {
    suspend fun getCharacters(page: Int = 1): CharacterResponse
    suspend fun getCharacter(id: Int): Character
}

/** 使用 Retrofit API 获取数据的正式 Repository 实现。 */
class NetworkCharacterRepository(
    private val api: RickAndMortyApi = RetrofitClient.api
) : CharacterRepository {

    override suspend fun getCharacters(page: Int): CharacterResponse {
        return api.getCharacters(page)
    }

    override suspend fun getCharacter(id: Int): Character {
        return api.getCharacter(id)
    }
}

/**
 * 这个小型作业使用的轻量依赖容器。
 *
 * 避免为两个页面引入 Hilt，同时确保整个应用复用同一个 Repository。
 */
object RepositoryProvider {
    val characterRepository: CharacterRepository by lazy {
        NetworkCharacterRepository()
    }
}
