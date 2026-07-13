package com.lysanderuy.tulogs.data

import com.lysanderuy.tulogs.data.local.SleepLog
import com.lysanderuy.tulogs.data.local.SleepLogDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SleepLogRepository @Inject constructor(
    private val sleepLogDao: SleepLogDao
) {
    val allLogs: Flow<List<SleepLog>> = sleepLogDao.getAllLogs()

    suspend fun startSession(bedtimeTimestamp: Long): Long {
        return sleepLogDao.insert(SleepLog(bedtimeTimestamp = bedtimeTimestamp))
    }

    suspend fun endActiveSession(wakeTimestamp: Long) {
        val active = sleepLogDao.getActiveSession()
        if (active != null) {
            sleepLogDao.update(active.copy(wakeTimestamp = wakeTimestamp))
        }
    }
}