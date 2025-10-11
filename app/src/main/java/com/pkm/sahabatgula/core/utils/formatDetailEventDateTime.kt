package com.pkm.sahabatgula.core.utils

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

fun formatEventDateTime(dateStr: String?, startStr: String?, endStr: String?): Pair<String, String> {
    val locale = Locale("id", "ID")
    val localDate = LocalDate.parse(dateStr)
    val titleFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", locale)
    val formattedDate = localDate.format(titleFormatter)

    val dayOfWeekFormatter = DateTimeFormatter.ofPattern("EEEE", locale)
    val dayOfWeek = localDate.format(dayOfWeekFormatter)

    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val startTime = startStr?.let { LocalTime.parse(it).format(timeFormatter) } ?: "-"
    val endTime = endStr?.let { LocalTime.parse(it).format(timeFormatter) } ?: "-"

    val formattedSubtitle = "$dayOfWeek, $startTime - $endTime WITA"
    return Pair(formattedDate, formattedSubtitle)
}

// Panggil fungsi untuk mendapatkan hasilnya
//val (tvTitleInfo, tvSubtitleInfo) = formatEventDateTime(eventDateString, eventStartString, eventEndString)

// Hasilnya:
// eventTitle akan berisi: "25 Oktober 2025"
// eventSubtitle akan berisi: "Sabtu, 09:00 - 11:00 WITA"