package com.pkm.sahabatgula.data.repository

import android.util.Log
import com.pkm.sahabatgula.core.utils.DateConverter
import com.pkm.sahabatgula.data.local.room.ChatDao
import com.pkm.sahabatgula.data.local.room.ChatMessageEntity
import com.pkm.sahabatgula.data.local.room.ProfileDao
import com.pkm.sahabatgula.data.local.room.Sender
import com.pkm.sahabatgula.data.local.room.SummaryDao
import com.pkm.sahabatgula.data.remote.api.GeminiApiService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InsightRepository @Inject constructor(
    private val chatDao: ChatDao,
    private val profileDao: ProfileDao,
    private val summaryDao: SummaryDao,
    private val geminiApi: GeminiApiService
) {

    // Mengambil riwayat chat dari database lokal
    fun getChatHistory(): Flow<List<ChatMessageEntity>> {
        return chatDao.getAllMessages()
    }

    // Fungsi utama untuk mengirim pertanyaan ke Gemini
    suspend fun askGemini(userQuestion: String) {

        val userMessage = ChatMessageEntity(
            message = userQuestion,
            sender = Sender.USER,
            timestamp = System.currentTimeMillis()
        )
        chatDao.insertMessage(userMessage)

        try {
            // 2. Buat prompt dengan konteks data pengguna
            val systemPrompt = createSystemPrompt()
            val finalPrompt = systemPrompt + userQuestion

            // 3. Kirim prompt ke Gemini API
            val geminiResponse = geminiApi.generateResponse(finalPrompt)
            Log.d("GeminiResponse", geminiResponse)


            // 4. Simpan jawaban Gemini ke database lokal
            val geminiMessage = ChatMessageEntity(
                message = geminiResponse,
                sender = Sender.GEMINI,
                timestamp = System.currentTimeMillis()
            )
            chatDao.insertMessage(geminiMessage)

            val today = DateConverter.getTodayLocalFormatted()
            val summary = summaryDao.getSummaryByDateForInsight("DAILY", today)
            Log.d("DATA SUMMARY", summary.toString())

        } catch (e: Exception) {
            // 5. Jika terjadi error, simpan pesan error ke database
            val errorMessage = ChatMessageEntity(
                message = "Maaf, terjadi kesalahan saat memproses permintaan Anda. Coba lagi.",
                sender = Sender.GEMINI,
                timestamp = System.currentTimeMillis(),
                isError = true // Tandai sebagai pesan error
            )
            chatDao.insertMessage(errorMessage)
        }
    }

    private suspend fun createSystemPrompt(): String {
        // Ambil data dari repository
        val profile = profileDao.getProfile()
        val today = DateConverter.getTodayLocalFormatted()
        val summary = summaryDao.getSummaryByDateForInsight("DAILY", today)

        return """
        Anda adalah asisten kesehatan AI Sahabat Gula. Jawab pertanyaan pengguna dengan singkat, relevan, dan memotivasi dalam Bahasa Indonesia.

        Data pengguna:
        - Usia: ${profile?.age ?: "N/A"} tahun, Gender: ${profile?.gender ?: "N/A"}
        - Tinggi: ${profile?.height ?: "N/A"} cm, Berat: ${profile?.weight ?: "N/A"} kg
                - Risiko diabetes: ${if (profile?.risk_index ?: 0 > 15) "Tinggi" else "Rendah"}
        - Target Kalori: ${profile?.max_calories ?: "N/A"} kkal, Target Gula: ${profile?.max_sugar ?: "N/A"} g

        Ringkasan hari ini:
        - Kalori: ${summary?.calories ?: 0} kkal, Gula: ${summary?.sugar ?: 0} g
                - Terbakar: ${summary?.burned ?: 0} kkal
                - Karbo: ${summary?.carbs ?: 0} g, Protein: ${summary?.protein ?: 0} g, Lemak: ${summary?.fat ?: 0} g
                - Natrium: ${summary?.sodium ?: 0} mg, Serat: ${summary?.fiber ?: 0} g, Air: ${summary?.water ?: 0} ml

                Pertanyaan pengguna:
        """.trimIndent()
    }
}