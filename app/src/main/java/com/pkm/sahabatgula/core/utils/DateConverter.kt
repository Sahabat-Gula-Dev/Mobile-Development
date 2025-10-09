// File: util/DateConverter.kt
package com.pkm.sahabatgula.core.utils

import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

object DateConverter {
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun getTodayLocalFormatted(): String {
        return LocalDate.now(ZoneId.of("Asia/Makassar")).format(formatter)
    }

    fun getTodayLocal(): LocalDate {
        return LocalDate.now(ZoneId.of("Asia/Makassar"))
    }

    // Server kirim "yyyy-MM-dd" as 00:00 UTC
    fun convertServerDateToLocal(serverDate: String?): String {
        val utcDate = LocalDate.parse(serverDate, DateTimeFormatter.ISO_DATE)
        val utcStart = utcDate.atStartOfDay(ZoneOffset.UTC)
        val localDate = utcStart.withZoneSameInstant(ZoneId.of("Asia/Makassar")).toLocalDate()
        return localDate.format(formatter)
    }

    fun convertServerDateToYearMonth(serverDate: String?): String {
        if (serverDate.isNullOrBlank()) return "-"
        val localDateString = convertServerDateToLocal(serverDate)
        val localDate = LocalDate.parse(localDateString, formatter)
        return DateTimeFormatter.ofPattern("yyyy-MM-dd").format(localDate)
    }


}
