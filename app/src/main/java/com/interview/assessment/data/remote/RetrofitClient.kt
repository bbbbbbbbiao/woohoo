package com.interview.assessment.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 *  Retrofit 网络客户端
 *
 * 使用 object 创建单例，确保整个应用只创建一套 Retrofit 和 API 对象，
 * 避免每次请求时重复创建网络客户端。
 */

object RetrofitClient {
    /**
     *  API 的基础地址
     *
     *  Retrofit 要求基础地址必须斜杠结尾
     */

    private const val BASE_URL = "https://rickandmortyapi.com/"

    /**
     *  对外提供 RickAndMortyApi 实例
     */
    val api: RickAndMortyApi by lazy {
        Retrofit.Builder()
            // 设置 API 基础地址
            .baseUrl(BASE_URL)

            // 使用 Gson 将 JSON 自动转换成 Kotlin 数据类
            .addConverterFactory(GsonConverterFactory.create())

            // 创建 Retrofit 实例
            .build()

            // 根据 RickAndMortyApi 接口生成网络请求实现
            .create(RickAndMortyApi::class.java)
    }


}