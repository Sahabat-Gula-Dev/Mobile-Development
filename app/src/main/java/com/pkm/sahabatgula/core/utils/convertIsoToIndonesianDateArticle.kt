package com.pkm.sahabatgula.core.utils

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

fun convertIsoToIndonesianDateArticle(isoDate: String?): String {
    return try {
        val zonedDateTime = ZonedDateTime.parse(isoDate, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale("id", "ID"))
        zonedDateTime.format(formatter)
    } catch (e: Exception) {
        e.printStackTrace()
        "-"
    }
}
