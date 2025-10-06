package com.pkm.sahabatgula.data.remote.api

import android.util.Log
import com.pkm.sahabatgula.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import javax.inject.Inject

/**
 * Retrofit API interface untuk Gemini REST API v1
 */
interface GeminiRestApi {
    @POST("v1/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body body: GeminiRequest
    ): GeminiResponse
}

/**
 * Request & Response model untuk Gemini API
 */
data class GeminiRequest(
    val contents: List<Content>
)

data class Content(
    val parts: List<Part>
)

data class Part(
    val text: String
)

data class GeminiResponse(
    val candidates: List<Candidate>?
)

data class Candidate(
    val content: Content?
)

/**
 * Service utama untuk mengirim prompt ke Gemini dan menerima response.
 */
class GeminiApiService @Inject constructor() {

    // Ganti model di sini kalau mau pakai pro
    private val modelName = "gemini-2.5-flash"
    // atau: private val modelName = "gemini-1.5-pro"

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .client(OkHttpClient.Builder().build())
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    private val api by lazy {
        retrofit.create(GeminiRestApi::class.java)
    }

    /**
     * Kirim prompt ke Gemini & ambil jawaban.
     */
    suspend fun generateResponse(prompt: String): String = withContext(Dispatchers.IO) {
        try {
            val request = GeminiRequest(
                contents = listOf(
                    Content(parts = listOf(Part(text = prompt)))
                )
            )

            val response = api.generateContent(
                model = modelName,
                apiKey = BuildConfig.GEMINI_API_KEY,
                body = request
            )

            val result = response.candidates
                ?.firstOrNull()
                ?.content
                ?.parts
                ?.firstOrNull()
                ?.text

            result ?: "Maaf, saya tidak bisa memberikan respons saat ini."
        } catch (e: Exception) {
            Log.e("GeminiApiService", "Error generating response", e)
            "Terjadi kesalahan: ${e.message ?: "Tidak diketahui"}"
        }
    }
}
