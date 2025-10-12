package com.pkm.sahabatgula.core.di

import com.pkm.sahabatgula.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BackendRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RssRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GeminiRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RssUrl

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RssApiKey

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DetikHealthRss




