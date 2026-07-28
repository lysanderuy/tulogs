package com.lysanderuy.tulogs.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SleepLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: SleepLog): Long

    @Query("UPDATE sleep_logs SET wakeTimestamp = :wakeTimestamp WHERE id = :id")
    suspend fun setWakeTimestamp(id: Long, wakeTimestamp: Long)

    @Query("UPDATE sleep_logs SET screenOffTimestamp = :screenOffTimestamp WHERE id = :id AND screenOffTimestamp IS NULL")
    suspend fun setScreenOffTimestampIfUnset(id: Long, screenOffTimestamp: Long): Int

    @Query("SELECT * FROM sleep_logs WHERE userId = :userId ORDER BY bedtimeTimestamp DESC")
    fun getAllLogs(userId: String): Flow<List<SleepLog>>

    @Query("SELECT * FROM sleep_logs WHERE userId = :userId AND wakeTimestamp IS NULL ORDER BY bedtimeTimestamp DESC LIMIT 1")
    suspend fun getActiveSession(userId: String): SleepLog?

    @Query("SELECT * FROM sleep_logs WHERE id = :id AND userId = :userId")
    suspend fun getLogById(id: Long, userId: String): SleepLog?

    @Query("SELECT * FROM sleep_logs WHERE userId = :userId AND bedtimeTimestamp >= :since ORDER BY bedtimeTimestamp ASC")
    fun getLogsSince(userId: String, since: Long): Flow<List<SleepLog>>
}
