// File: util/DateConverter.kt
package com.pkm.sahabatgula.core.utils

import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

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
