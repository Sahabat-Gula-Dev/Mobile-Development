package com.pkm.sahabatgula.core.di

import android.content.Context
import androidx.room.Room
import com.pkm.sahabatgula.data.local.room.AppDatabase
import com.pkm.sahabatgula.data.local.room.ProfileDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton // Kita hanya butuh satu instance database untuk seluruh aplikasi
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "sahabat_gula_db" // Nama file database Anda
        ).build()
    }

    @Provides // DAO tidak perlu @Singleton karena AppDatabase sudah singleton
    fun provideProfileDao(appDatabase: AppDatabase): ProfileDao {
        return appDatabase.profileDao() // Resep untuk membuat ProfileDao
    }

    // Jika Anda punya DAO lain nanti (misal: FoodDao), tambahkan resepnya di sini
    // @Provides
    // fun provideFoodDao(appDatabase: AppDatabase): FoodDao {
    //     return appDatabase.foodDao()
    // }
}