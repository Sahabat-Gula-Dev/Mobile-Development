package com.pkm.sahabatgula.data.remote.api

import com.pkm.sahabatgula.data.remote.model.NewsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query


interface NewsApiService {

    @GET("api.json")
    suspend fun getNewsFromRss(
        @Query("rss_url") rssUrl: String,
        @Query("count") count: Int = 30,
        @Query("api_key") apiKey: String
    ): Response<NewsResponse>

}
