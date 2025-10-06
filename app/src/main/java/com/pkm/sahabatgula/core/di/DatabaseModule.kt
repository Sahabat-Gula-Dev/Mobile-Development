package com.pkm.sahabatgula.core.di

import android.content.Context
import androidx.room.Room
import com.pkm.sahabatgula.data.local.room.AppDatabase
import com.pkm.sahabatgula.data.local.room.ChatDao
import com.pkm.sahabatgula.data.local.room.MIGRATION_1_2
import com.pkm.sahabatgula.data.local.room.MIGRATION_2_3
import com.pkm.sahabatgula.data.local.room.MIGRATION_3_4
import com.pkm.sahabatgula.data.local.room.ProfileDao
import com.pkm.sahabatgula.data.local.room.SummaryDao
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
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "sahabat_gula_db"
        )
            .fallbackToDestructiveMigration() // hanya untuk dev
//            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
            .build()
    }

    @Provides
    fun provideProfileDao(appDatabase: AppDatabase): ProfileDao {
        return appDatabase.profileDao()
    }

    @Provides
    fun provideSummaryDao(appDatabase: AppDatabase): SummaryDao {
        return appDatabase.SummaryDao()
    }

    @Provides
    fun provideChatDao(appDatabase: AppDatabase): ChatDao {
        return appDatabase.ChatDao()
    }





    // Jika ada DAO lain nanti (misal: FoodDao), tambahkan resepnya di sini
    // @Provides
    // fun provideFoodDao(appDatabase: AppDatabase): FoodDao {
    //     return appDatabase.foodDao()
    // }
}