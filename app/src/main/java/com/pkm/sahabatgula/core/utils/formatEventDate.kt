package com.pkm.sahabatgula.core.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

fun formatEventDate(isoDate: String?): String {
    if (isoDate.isNullOrEmpty()) return "Tanggal tidak tersedia"

    return try {
        // 🟡 Input dari Supabase: "2025-10-04"
        val inputFormatter = DateTimeFormatter.ISO_LOCAL_DATE
        val date = LocalDate.parse(isoDate, inputFormatter)

        // ✨ Output format Indonesia: "04 Oktober 2025"
        val outputFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale("id", "ID"))
        date.format(outputFormatter)

    } catch (e: Exception) {
        "Format tanggal salah"
    }
}