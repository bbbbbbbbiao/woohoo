package com.interview.assessment.data.repository

import com.interview.assessment.data.model.CharacterResponse
import com.interview.assessment.data.remote.RetrofitClient

/**
 *  角色数据仓库
 *  位于ViewModel 和 网络接口之间
 *  用于统一管理数据来源，避免 ViewModel 直接依赖 Retrofit
 */

class CharacterRepository {
    /**
     *  请求指定页码的角色数据
     *
     *  @param page 需要加载的页码
     *  @return API 返回的角色列表和分页信息
     */
    suspend fun getCharacters(page : Int = 1) : CharacterResponse {
        return RetrofitClient.api.getCharacters(page)
    }
}