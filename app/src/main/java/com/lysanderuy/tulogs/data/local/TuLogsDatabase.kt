package com.lysanderuy.tulogs.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Alarm::class, SleepTag::class, SleepLog::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class TuLogsDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
    abstract fun sleepTagDao(): SleepTagDao
    abstract fun sleepLogDao(): SleepLogDao
}