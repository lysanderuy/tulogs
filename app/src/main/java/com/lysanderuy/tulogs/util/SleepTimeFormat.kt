package com.lysanderuy.tulogs.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val clockFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val dateLabelFormatter = DateTimeFormatter.ofPattern("EEE d MMM")

object SleepTimeFormat {

    fun formatClockTime(timestampMillis: Long): String =
        Instant.ofEpochMilli(timestampMillis).atZone(ZoneId.systemDefault()).format(clockFormatter)

    fun formatClockTime(time: LocalTime): String = time.format(clockFormatter)

    fun formatDateLabel(date: LocalDate): String = date.format(dateLabelFormatter).uppercase()

    fun formatDuration(durationMillis: Long): String {
        val totalMinutes = durationMillis / 60000
        return "${totalMinutes / 60}h ${totalMinutes % 60}m"
    }
}
