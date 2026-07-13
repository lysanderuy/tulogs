package com.lysanderuy.tulogs.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SleepLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: SleepLog): Long

    @Update
    suspend fun update(log: SleepLog)

    @Query("SELECT * FROM sleep_logs ORDER BY bedtimeTimestamp DESC")
    fun getAllLogs(): Flow<List<SleepLog>>

    @Query("SELECT * FROM sleep_logs WHERE wakeTimestamp IS NULL ORDER BY bedtimeTimestamp DESC LIMIT 1")
    suspend fun getActiveSession(): SleepLog?

    @Query("SELECT * FROM sleep_logs WHERE id = :id")
    suspend fun getLogById(id: Long): SleepLog?
}