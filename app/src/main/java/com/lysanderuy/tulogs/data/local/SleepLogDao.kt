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

    @Query("SELECT * FROM sleep_logs WHERE userId = :userId ORDER BY bedtimeTimestamp DESC")
    fun getAllLogs(userId: String): Flow<List<SleepLog>>

    @Query("SELECT * FROM sleep_logs WHERE userId = :userId AND wakeTimestamp IS NULL ORDER BY bedtimeTimestamp DESC LIMIT 1")
    suspend fun getActiveSession(userId: String): SleepLog?

    @Query("SELECT * FROM sleep_logs WHERE id = :id AND userId = :userId")
    suspend fun getLogById(id: Long, userId: String): SleepLog?
}