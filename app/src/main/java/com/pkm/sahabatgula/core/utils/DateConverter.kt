// File: util/DateConverter.kt
package com.pkm.sahabatgula.util

import android.util.Log
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object DateConverter {

    private val WitaZoneId = ZoneId.of("Asia/Makassar")

    /**
     * FUNGSI KUNCI: Mengubah timestamp UTC dari API menjadi string tanggal LOKAL.
     * Contoh Input: "2025-09-25T20:30:00.123Z"
     * Contoh Output: "2025-09-26"
     */
    fun convertUtcTimestampToLocalDateString(utcTimestamp: String): String {
        try {
            val instant = Instant.parse(utcTimestamp)
            return DateTimeFormatter.ISO_LOCAL_DATE
                .withZone(WitaZoneId)
                .format(instant)
        } catch (e: Exception) {
//             Jika formatnya hanya tanggal (misal "2025-09-25"), langsung kembalikan
//            if (utcTimestamp.length <= 10) return utcTimestamp
            // Fallback jika ada error lain
            Log.d("DateConverter", "Error converting timestamp: $e")
            return getTodayLocalFormatted()
        }
    }

    /**
     * Fungsi untuk mendapatkan tanggal "hari ini" menurut jam lokal.
     * Ini digunakan saat MEMBACA data.
     */
    fun getTodayLocalFormatted(): String {
        return LocalDate.now(WitaZoneId).format(DateTimeFormatter.ISO_LOCAL_DATE)
    }
}