package com.lysanderuy.tulogs.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.DayOfWeek
import java.time.LocalDate

// A one-time alarm (empty daysOfWeek) fires once on date and disables itself; otherwise it repeats weekly on daysOfWeek
@Entity(tableName = "alarms")
data class Alarm(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String = "",
    val hour: Int,
    val minute: Int,
    val isEnabled: Boolean = true,
    val label: String = "",
    val daysOfWeek: Set<DayOfWeek> = emptySet(),
    val date: LocalDate = LocalDate.now()
)