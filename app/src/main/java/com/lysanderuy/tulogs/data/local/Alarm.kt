package com.lysanderuy.tulogs.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * A one-time alarm (no repeat days) fires once on [date] at [hour]:[minute]
 * and disables itself afterward. A repeating alarm (non-empty [daysOfWeek])
 * ignores [date] and fires weekly on the selected days.
 */
@Entity(tableName = "alarms")
data class Alarm(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val hour: Int,
    val minute: Int,
    val isEnabled: Boolean = true,
    val label: String = "",
    val daysOfWeek: Set<DayOfWeek> = emptySet(),
    val date: LocalDate = LocalDate.now()
)