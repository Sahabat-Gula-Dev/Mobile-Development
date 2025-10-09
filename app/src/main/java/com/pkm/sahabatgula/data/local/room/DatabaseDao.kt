package com.pkm.sahabatgula.data.local.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = :userId")
    suspend fun getProfileByUserId(userId: String): ProfileEntity?

    @Query("SELECT * FROM user_profile LIMIT 1")
    suspend fun getProfile(): ProfileEntity?

    @Upsert
    suspend fun upsertProfile(profile: ProfileEntity)

    @Update
    suspend fun updateProfile(profile: ProfileEntity)

    @Delete
    suspend fun deleteProfile(profile: ProfileEntity)

    @Query("SELECT * FROM user_profile LIMIT 1")
    fun observeProfile(): Flow<ProfileEntity?>

    @Query("SELECT * FROM user_profile LIMIT 1")
    fun getLocalProfile(): ProfileEntity?

    @Query("DELETE FROM user_profile")
    suspend fun clearAll()

}

@Dao
interface SummaryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(summaries: List<SummaryEntity>)

    @Query("SELECT * FROM summary WHERE type = :type AND date = :date LIMIT 1")
    fun getSummaryByDate(type: String, date: String): Flow<SummaryEntity?>

    @Query("SELECT * FROM summary WHERE type = :type AND date = :date LIMIT 1")
    suspend fun getSummaryByDateForInsight(type: String, date: String): SummaryEntity

    @Query("SELECT * FROM summary WHERE type = :type ORDER BY date DESC")
    fun getSummaryByType(type: String): Flow<List<SummaryEntity>>

    @Query("SELECT * FROM summary WHERE type = 'DAILY' ORDER BY date DESC LIMIT 1")
    fun getLatestDailySummary(): Flow<SummaryEntity?>

    @Query("SELECT * FROM summary")
    suspend fun getAll(): List<SummaryEntity>

    @Query("DELETE FROM summary WHERE type = :type")
    suspend fun clearByType(type: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(summaries: List<SummaryEntity>)

    @Query("SELECT * FROM summary WHERE type = 'WEEKLY' ORDER BY date ASC")
    fun getAllWeeklySummary(): Flow<List<SummaryEntity>>

    @Query("SELECT * FROM summary WHERE type = 'MONTHLY' ORDER BY date ASC")
    fun getAllMonthlySummary(): Flow<List<SummaryEntity>>

    // clear
    @Query("DELETE FROM summary")
    suspend fun clearAllSumary()
}

@Dao
interface ChatDao {

    // Fungsi untuk memasukkan pesan baru ke database
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    // Fungsi untuk mengambil semua pesan, diurutkan dari yang paling lama
    // Menggunakan Flow agar UI bisa update secara otomatis saat ada pesan baru
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessageEntity>>

    // (Opsional) Fungsi untuk menghapus riwayat percakapan
    @Query("DELETE FROM chat_messages")
    suspend fun clearHistory()
}