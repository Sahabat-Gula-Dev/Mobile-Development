package com.pkm.sahabatgula.data.local.room

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// Migration dari versi 1 → 2
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Tambah tabel baru daily_summary, user_profile tetap aman
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS daily_summary (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                date TEXT NOT NULL,
                calories REAL,
                carbs REAL,
                protein REAL,
                fat REAL,
                sugar REAL,
                sodium REAL,
                fiber REAL,
                potassium REAL
            )
        """.trimIndent())
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Tambah kolom baru dengan default value supaya tidak error di data lama
        database.execSQL("ALTER TABLE daily_summary ADD COLUMN burned INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE daily_summary ADD COLUMN steps INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE daily_summary ADD COLUMN water INTEGER NOT NULL DEFAULT 0")
    }
}