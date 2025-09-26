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

}

@Dao
interface SummaryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(summary: List<SummaryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(summaries: List<SummaryEntity>)

    @Query("SELECT * FROM summary WHERE type = :type ORDER BY date DESC")
    fun getSummaryByType(type: String): Flow<List<SummaryEntity>>

    @Query("DELETE FROM summary WHERE type = :type")
    suspend fun clearByType(type: String)

    @Query("SELECT * FROM summary WHERE type = :type AND date = :date LIMIT 1")
    fun getSummaryByDate(type: String, date: String): Flow<SummaryEntity?>


    companion object
}