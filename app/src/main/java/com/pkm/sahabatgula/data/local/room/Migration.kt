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

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // 1. Buat tabel baru dengan definisi kolom yang diubah (calories jadi nullable)
        database.execSQL("""
            CREATE TABLE daily_summary_new (
                date TEXT NOT NULL PRIMARY KEY,
                calories INTEGER,
                carbs REAL NOT NULL,
                protein REAL NOT NULL,
                fat REAL NOT NULL,
                sugar REAL NOT NULL,
                sodium REAL NOT NULL,
                fiber REAL NOT NULL,
                potassium REAL NOT NULL,
                burned INTEGER NOT NULL,
                steps INTEGER NOT NULL,
                water INTEGER NOT NULL
            )
        """.trimIndent())

        // 2. Copy data lama ke tabel baru
        database.execSQL("""
            INSERT INTO daily_summary_new (
                date, calories, carbs, protein, fat, sugar, sodium, fiber, potassium, burned, steps, water
            )
            SELECT date, calories, carbs, protein, fat, sugar, sodium, fiber, potassium, burned, steps, water
            FROM daily_summary
        """.trimIndent())

        // 3. Hapus tabel lama
        database.execSQL("DROP TABLE daily_summary")

        // 4. Rename tabel baru → jadi nama asli
        database.execSQL("ALTER TABLE daily_summary_new RENAME TO daily_summary")
    }
}
