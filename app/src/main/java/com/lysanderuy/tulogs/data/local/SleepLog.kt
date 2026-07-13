package com.lysanderuy.tulogs.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sleep_logs")
data class SleepLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bedtimeTimestamp: Long,
    val wakeTimestamp: Long? = null,
    val screenOffTimestamp: Long? = null,
    val firstScreenOnTimestamp: Long? = null
)