package com.lysanderuy.tulogs.data.local

import androidx.room.TypeConverter
import java.time.DayOfWeek
import java.time.LocalDate

class Converters {

    @TypeConverter
    fun fromTagType(type: TagType): String = type.name

    @TypeConverter
    fun toTagType(value: String): TagType = TagType.valueOf(value)

    @TypeConverter
    fun fromDaysOfWeek(days: Set<DayOfWeek>): String = days.joinToString(",") { it.value.toString() }

    @TypeConverter
    fun toDaysOfWeek(value: String): Set<DayOfWeek> =
        if (value.isBlank()) emptySet() else value.split(",").map { DayOfWeek.of(it.toInt()) }.toSet()

    @TypeConverter
    fun fromLocalDate(date: LocalDate): Long = date.toEpochDay()

    @TypeConverter
    fun toLocalDate(value: Long): LocalDate = LocalDate.ofEpochDay(value)
}