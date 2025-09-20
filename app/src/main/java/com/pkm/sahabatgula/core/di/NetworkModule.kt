package com.pkm.sahabatgula.core.di

import com.pkm.sahabatgula.BuildConfig
import com.pkm.sahabatgula.data.remote.api.ApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
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
            .build()
    }

    @Provides
    @Singleton // Memastikan hanya ada satu instance Retrofit
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL) // Mengambil Base URL dari BuildConfig
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient) // Menggunakan OkHttpClient yang sudah kita buat di atas
            .build()
    }

    @Provides
    @Singleton // Memastikan hanya ada satu instance ApiService
    fun provideApiService(retrofit: Retrofit): ApiService {
        // Hilt sekarang tahu cara membuat ApiService: dengan menggunakan Retrofit
        return retrofit.create(ApiService::class.java)
    }
}