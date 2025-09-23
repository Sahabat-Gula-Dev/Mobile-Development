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
    suspend fun getProfile(): ProfileEntity

    @Upsert
    suspend fun upsertProfile(profile: ProfileEntity)

    @Update
    suspend fun updateProfile(profile: ProfileEntity)

    @Delete
    suspend fun deleteProfile(profile: ProfileEntity)

}

@Dao
interface DailySummaryDao {
    @Query("SELECT * FROM daily_summary WHERE date = :date LIMIT 1")
    fun getSummaryByDate(date: String): Flow<DailySummaryEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(summary: DailySummaryEntity)
}