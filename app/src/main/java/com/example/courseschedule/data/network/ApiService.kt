package com.example.courseschedule.data.network

import retrofit2.http.GET
import retrofit2.http.Query

// 一言 API - 免费中文句子/格言
interface ApiService {

    @GET("https://v1.hitokoto.cn/")
    suspend fun getHitokoto(
        @Query("c") category: String = "i"  // i=诗词 k=励志 d=其他
    ): HitokotoDto
}
