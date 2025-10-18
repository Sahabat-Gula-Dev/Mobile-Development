package com.pkm.sahabatgula.core.utils

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

object DateConverter {
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun getTodayLocalFormatted(): String {
        return LocalDate.now(ZoneId.of("Asia/Makassar")).format(formatter)
    }

    // Server kirim "yyyy-MM-dd" as 00:00 UTC
    fun convertServerDateToLocal(serverDate: String?): String {
        val utcDate = LocalDate.parse(serverDate, DateTimeFormatter.ISO_DATE)
        val utcStart = utcDate.atStartOfDay(ZoneOffset.UTC)
        val localDate = utcStart.withZoneSameInstant(ZoneId.of("Asia/Makassar")).toLocalDate()
        return localDate.format(formatter)
    }

}

fun newsTimeConverter(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale.forLanguageTag("id-ID"))
        val date: Date = inputFormat.parse(dateString)!!
        outputFormat.format(date)
    } catch (e: Exception) {
        e.printStackTrace()
        dateString
    }
}

fun convertIsoToIndonesianDateArticle(isoDate: String?): String {
    return try {
        val zonedDateTime = ZonedDateTime.parse(isoDate, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.forLanguageTag("id-ID"))
        zonedDateTime.format(formatter)
    } catch (e: Exception) {
        e.printStackTrace()
        "-"
    }
}

fun formatEventDateTime(dateStr: String?, startStr: String?, endStr: String?): Pair<String, String> {
    val locale = Locale.forLanguageTag("id-ID")
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

    // Panggil fungsi untuk mendapatkan hasilnya
//val (tvTitleInfo, tvSubtitleInfo) = formatEventDateTime(eventDateString, eventStartString, eventEndString)

// Hasilnya:
// eventTitle akan berisi: "25 Oktober 2025"
// eventSubtitle akan berisi: "Sabtu, 09:00 - 11:00 WITA"
}

fun formatEventDate(isoDate: String?): String {
    if (isoDate.isNullOrEmpty()) return "Tanggal tidak tersedia"

    return try {
        // Input dari Supabase: "2025-10-04"
        val inputFormatter = DateTimeFormatter.ISO_LOCAL_DATE
        val date = LocalDate.parse(isoDate, inputFormatter)

        // Output format Indonesia: "04 Oktober 2025"
        val outputFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.forLanguageTag("id-ID"))
        date.format(outputFormatter)

    } catch (e: Exception) {
        "Format tanggal salah"
    }
}

fun dateFormatterHistory(input: String): String {
    val parsedDate = LocalDate.parse(input, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    val outputFormatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", Locale.forLanguageTag("id-ID"))
    val todayFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.forLanguageTag("id-ID"))
    val today = LocalDate.now()
    return if (parsedDate == today) {
        "Hari ini, ${parsedDate.format(todayFormatter)}"
    } else {
        parsedDate.format(outputFormatter)
    }
}


fun convertUtcToLocalDateOnly(utcString: String): String {
    val utcFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
    val localZoned = ZonedDateTime.parse(utcString, utcFormatter)
        .withZoneSameInstant(ZoneId.of("Asia/Makassar"))
    return localZoned.toLocalDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
}


