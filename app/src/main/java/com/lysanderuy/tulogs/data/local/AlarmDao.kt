package com.lysanderuy.tulogs.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(alarm: Alarm): Long

    @Update
    suspend fun update(alarm: Alarm)

    @Delete
    suspend fun delete(alarm: Alarm)

    @Query("SELECT * FROM alarms WHERE userId = :userId ORDER BY hour, minute")
    fun getAllAlarms(userId: String): Flow<List<Alarm>>

    @Query("SELECT * FROM alarms WHERE userId = :userId AND isEnabled = 1")
    fun getEnabledAlarms(userId: String): Flow<List<Alarm>>

    @Query("SELECT * FROM alarms WHERE id = :id AND userId = :userId")
    suspend fun getAlarmById(id: Long, userId: String): Alarm?
}