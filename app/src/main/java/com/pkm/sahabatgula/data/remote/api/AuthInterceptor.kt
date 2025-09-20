package com.pkm.sahabatgula.data.remote.api

import com.pkm.sahabatgula.data.local.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenManager: TokenManager): Interceptor{
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = tokenManager.getAccessToken()
        val requestBuilder = originalRequest.newBuilder()
        if(token != null) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }
        val newRequest = requestBuilder.build()

        return chain.proceed(newRequest)
    }

}