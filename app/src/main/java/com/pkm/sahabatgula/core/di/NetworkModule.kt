package com.pkm.sahabatgula.core.di

import android.util.Log
import com.pkm.sahabatgula.BuildConfig
import com.pkm.sahabatgula.data.remote.api.ApiService
import com.pkm.sahabatgula.data.remote.api.GeminiRestApi
import com.pkm.sahabatgula.data.remote.api.NewsApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @BackendRetrofit
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    @Provides
    @RssRetrofit
    @Singleton
    fun provideRssRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.RSS_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @GeminiRetrofit
    fun provideGeminiRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService( @BackendRetrofit retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideNewsApiService(@RssRetrofit retrofit: Retrofit): NewsApiService {
        Log.d("RSS_RETROFIT_DEBUG", "Base URL NewsApiService = ${retrofit.baseUrl()}")
        return retrofit.create(NewsApiService::class.java)
    }


    @Provides
    @Singleton
    fun provideGeminiApi(@GeminiRetrofit retrofit: Retrofit): GeminiRestApi {
        return retrofit.create(GeminiRestApi::class.java)
    }

}

@Module
@InstallIn(SingletonComponent::class)
object ConstantsModule {

    @Provides
    @RssUrl
    fun provideRssUrl(): String = BuildConfig.RSS_URL

    @Provides
    @RssApiKey
    fun provideRssApiKey(): String = BuildConfig.RSS_API_KEY

    @Provides
    @DetikHealthRss
    fun provideDetikHealthRssUrl(): String = "https://health.detik.com/rss"
}
