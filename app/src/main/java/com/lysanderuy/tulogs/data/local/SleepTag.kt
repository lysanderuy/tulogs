package com.lysanderuy.tulogs.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class TagType { BEDTIME, WAKE }

@Entity(tableName = "sleep_tags", indices = [Index(value = ["type"], unique = true)])
data class SleepTag(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val uid: String,
    val type: TagType
)
