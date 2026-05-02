package com.ggdover.wodapp.ui.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val dateTimeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("MMM d, yyyy · HH:mm").withZone(ZoneId.systemDefault())

private val dateOnlyFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault())

fun formatInstant(epochMillis: Long): String =
    dateTimeFormatter.format(Instant.ofEpochMilli(epochMillis))

fun formatDateOnly(epochMillis: Long): String =
    dateOnlyFormatter.format(Instant.ofEpochMilli(epochMillis))

fun epochAtStartOfDay(year: Int, month: Int, day: Int): Long {
    val z = ZoneId.systemDefault()
    return java.time.LocalDate.of(year, month, day)
        .atStartOfDay(z)
        .toInstant()
        .toEpochMilli()
}
