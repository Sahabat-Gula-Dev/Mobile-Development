package com.pkm.sahabatgula.data.local.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database (entities = [ProfileEntity::class], version = 1, exportSchema = false)

abstract class AppDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
}
