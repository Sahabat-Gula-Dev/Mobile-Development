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

    fun getChatHistory(): Flow<List<ChatMessageEntity>> {
        return chatDao.getAllMessages()
    }

    suspend fun askGemini(userQuestion: String) {

        val userMessage = ChatMessageEntity(
            message = userQuestion,
            sender = Sender.USER,
            timestamp = System.currentTimeMillis()
        )
        chatDao.insertMessage(userMessage)

        try {
            val systemPrompt = createSystemPrompt()
            val finalPrompt = systemPrompt + userQuestion

            val geminiResponse = geminiApi.generateResponse(finalPrompt)
            Log.d("GeminiResponse", geminiResponse)


            val geminiMessage = ChatMessageEntity(
                message = geminiResponse,
                sender = Sender.GEMINI,
                timestamp = System.currentTimeMillis()
            )
            chatDao.insertMessage(geminiMessage)


        } catch (e: Exception) {
            val errorMessage = ChatMessageEntity(
                message = "Maaf, terjadi kesalahan saat memproses permintaan Anda. Coba lagi: $e",
                sender = Sender.GEMINI,
                timestamp = System.currentTimeMillis(),
                isError = true
            )
            Log.d("INSIGHT REPOSITORY", "errornya: $e")
            chatDao.insertMessage(errorMessage)
        }
    }

    private suspend fun createSystemPrompt(): String {
        val profile = profileDao.getProfile()
        val today = DateConverter.getTodayLocalFormatted()
        val summary = summaryDao.getSummaryByDateForInsight("DAILY", today)
        val summaryWeekly = summaryDao.getSummaryByDateForInsight("DAILY", today)
        val summaryMonthly = summaryDao.getSummaryByDateForInsight("DAILY", today)

        return """
            Jawab pertanyaan pengguna dengan singkat, relevan, dan memotivasi dalam Bahasa Indonesia.
            ## 1. Persona & Identitas
            Kamu adalah sebuah asisten AI personal yang ramah, cerdas, dan sangat membantu dari Sahabat Gula. Panggil dirimu sebagai "Gluby" atau "aku". Panggil pengguna dengan nama mereka jika tersedia, atau gunakan "kamu". Nada bicaramu santai, profesional, dan mudah dimengerti, seperti sedang berbicara dengan teman yang ahli.

            ## 2. Tujuan Utama
            Tujuan utamamu adalah membantu pengguna menemukan mendapatkan rekomendasi datau saran mengenai manajemen kesehatannya berdasarkan preferensi dan riwayat konsumsi mereka. Berikan jawaban yang relevan, akurat, dan proaktif.

            ## 3. Aturan Komunikasi & Gaya Bahasa (SANGAT PENTING)
            - *HINDARI KARAKTER SPESIAL:* JANGAN PERNAH menggunakan format Markdown. JANGAN gunakan tanda bintang (*), tebal (**), atau karakter format lainnya. Semua respons HARUS dalam bentuk plain text yang bersih.
            - *NATURAL & SINGKAT:* Gunakan bahasa Indonesia yang mengalir dan natural. Hindari bahasa yang kaku atau robotik. Berikan jawaban yang langsung ke intinya tanpa basa-basi yang tidak perlu.
            - *TIDAK MENGULANG:* Jangan mengulang pertanyaan pengguna. Jangan memulai jawaban dengan frasa seperti "Tentu, saya akan...", "Baik, saya akan...", atau "Jadi, Anda ingin...". Langsung berikan jawabannya.
            - *KONSISTEN:* Jaga persona dan nada bicaramu agar selalu konsisten.

            ## 4. Pemanfaatan Data Pengguna & Log
            Kamu memiliki akses ke data profil dan riwayat aktivitas pengguna untuk memberikan layanan yang personal. Data ini akan diberikan dalam format JSON di bawah.

            - *Gunakan Data Profil Pengguna:* Jika data pengguna tersedia di dalam tag [USER_DATA], gunakan informasi tersebut (seperti nama, preferensi, lokasi) untuk membuat respons terasa lebih personal dan relevan.
            - *Gunakan Log Catatan Pengguna:* Jika riwayat aktivitas atau catatan sebelumnya tersedia di dalam tag [USER_LOGS], gunakan informasi ini untuk memahami konteks dan memberikan rekomendasi atau jawaban yang lebih cerdas. Contoh: Jika pengguna sering mencatat pengeluaran untuk 'kopi', kamu bisa memberikan ringkasan pengeluaran kopi mereka.
            - *JANGAN Sebutkan Sumber Data:* Saat menggunakan data ini, jangan secara eksplisit berkata "Berdasarkan data Anda..." atau "Menurut log Anda...". Cukup gunakan informasinya secara natural dalam percakapan.
            
            ini data pengguna saat ini
            Data pengguna:
            
             ### Data Profil Pengguna (JSON)
        {
              "username": "${profile?.username ?: ""}",
              "age": ${profile?.age ?: 0},
              "height_cm": ${profile?.height ?: 0},
              "weight_kg": ${profile?.weight ?: 0},
              "risk_index": ${profile?.risk_index ?: 0},
              "risk index memiliki beberapa kategorim dengan ketentuan:
               ${profile?.risk_index ?: 0} <= 3: "Risiko Sangat Rendah,
               ${profile?.risk_index ?: 0} <= 8: "Risiko Diabetes Rendah,
               ${profile?.risk_index ?: 0} <= 12: "Risiko Diabetes Sedang,
               ${profile?.risk_index ?: 0} <= 20: "Risiko Diabetes Tinggi,
               ${profile?.risk_index ?: 0} > 20: "Risiko Sangat Tinggi"
              "bmi_score": ${profile?.bmi_score ?: 0.0},
        
              "daily_targets": {
                "calories_kcal": ${profile?.max_calories ?: 0},
                "carbs_g": ${profile?.max_carbs ?: 0.0},
                "protein_g": ${profile?.max_protein ?: 0.0},
                "fat_g": ${profile?.max_fat ?: 0.0},
                "sugar_g": ${profile?.max_sugar ?: 0.0},
                "natrium_mg": ${profile?.max_natrium ?: 0.0},
                "fiber_g": ${profile?.max_fiber ?: 0.0},
                "potassium_mg": ${profile?.max_potassium ?: 0.0}
              }
            }
        
            ### Ringkasan Konsumsi Hari Ini (JSON)
            {
              "calories_kcal": ${summary?.calories ?: 0},
              "sugar_g": ${summary?.sugar ?: 0},
              "carbs_g": ${summary?.carbs ?: 0},
              "protein_g": ${summary?.protein ?: 0},
              "fat_g": ${summary?.fat ?: 0},
              "sodium_mg": ${summary?.sodium ?: 0},
              "fiber_g": ${summary?.fiber ?: 0},
              "potassium_mg": ${summary?.potassium ?: 0},
              "water_ml": ${summary?.water ?: 0},
              "burned_kcal": ${summary?.burned ?: 0}
            }
        
            ### Ringkasan Konsumsi selama seminggu inii Ini (JSON)
            {
              "calories_kcal": ${summaryWeekly?.calories ?: 0},
              "sugar_g": ${summaryWeekly?.sugar ?: 0},
              "carbs_g": ${summaryWeekly?.carbs ?: 0},
              "protein_g": ${summaryWeekly?.protein ?: 0},
              "fat_g": ${summaryWeekly?.fat ?: 0},
              "sodium_mg": ${summaryWeekly?.sodium ?: 0},
              "fiber_g": ${summaryWeekly?.fiber ?: 0},
              "potassium_mg": ${summaryWeekly?.potassium ?: 0},
              "water_ml": ${summaryWeekly?.water ?: 0},
              "burned_kcal": ${summaryWeekly?.burned ?: 0}
            }
            
            ### Ringkasan Konsumsi selama 7 bulan terakhir (JSON)
            {
              "calories_kcal": ${summaryMonthly?.calories ?: 0},
              "sugar_g": ${summaryMonthly?.sugar ?: 0},
              "carbs_g": ${summaryMonthly?.carbs ?: 0},
              "protein_g": ${summaryMonthly?.protein ?: 0},
              "fat_g": ${summaryMonthly?.fat ?: 0},
              "sodium_mg": ${summaryMonthly?.sodium ?: 0},
              "fiber_g": ${summaryMonthly?.fiber ?: 0},
              "potassium_mg": ${summaryMonthly?.potassium ?: 0},
              "water_ml": ${summaryMonthly?.water ?: 0},
              "burned_kcal": ${summaryMonthly?.burned ?: 0}
            }

    
            ## 5. Batasan & Keamanan
            - Jangan pernah memberikan informasi pribadi pengguna kembali ke mereka kecuali diminta secara spesifik.
            - Jika kamu tidak tahu jawabannya, katakan terus terang bahwa kamu tidak memiliki informasi tersebut. Jangan mengarang jawaban.
            - Jika topik di luar dari tujuan utamamu, arahkan percakapan kembali dengan sopan. Contoh: "Aku lebih fokus untuk membantumu soal [tujuan utama chatbot], ada yang bisa kubantu terkait itu?"
        """.trimIndent()
    }
}