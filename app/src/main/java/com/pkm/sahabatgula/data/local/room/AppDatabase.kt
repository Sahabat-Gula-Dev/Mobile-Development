package com.pkm.sahabatgula.data.local.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database (entities = [ProfileEntity::class, SummaryEntity::class], version = 9, exportSchema = false)

abstract class AppDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun SummaryDao(): SummaryDao

}
