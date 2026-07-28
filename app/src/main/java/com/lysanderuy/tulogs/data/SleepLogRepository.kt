package com.lysanderuy.tulogs.data

import com.lysanderuy.tulogs.data.local.SleepLog
import com.lysanderuy.tulogs.data.local.SleepLogDao
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalCoroutinesApi::class)
class SleepLogRepository @Inject constructor(
    private val sleepLogDao: SleepLogDao,
    private val authRepository: AuthRepository
) {
    val allLogs: Flow<List<SleepLog>> = authRepository.currentUserIdFlow.flatMapLatest { userId ->
        if (userId == null) flowOf(emptyList()) else sleepLogDao.getAllLogs(userId)
    }

    fun recentLogs(windowDays: Long = 7): Flow<List<SleepLog>> = authRepository.currentUserIdFlow.flatMapLatest { userId ->
        if (userId == null) {
            flowOf(emptyList())
        } else {
            val since = LocalDate.now(ZoneId.systemDefault())
                .minusDays(windowDays - 1)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
            sleepLogDao.getLogsSince(userId, since)
        }
    }

    suspend fun hasActiveSession(): Boolean {
        val userId = authRepository.currentUserId ?: return false
        return sleepLogDao.getActiveSession(userId) != null
    }

    suspend fun startSession(bedtimeTimestamp: Long): Long {
        val userId = authRepository.currentUserId ?: return -1
        return sleepLogDao.insert(SleepLog(userId = userId, bedtimeTimestamp = bedtimeTimestamp))
    }

    suspend fun endActiveSession(wakeTimestamp: Long): Boolean {
        val userId = authRepository.currentUserId ?: return false
        val active = sleepLogDao.getActiveSession(userId) ?: return false
        sleepLogDao.setWakeTimestamp(active.id, wakeTimestamp)
        return true
    }

    suspend fun recordScreenOff(timestamp: Long): Boolean {
        val userId = authRepository.currentUserId ?: return false
        val active = sleepLogDao.getActiveSession(userId) ?: return false
        return sleepLogDao.setScreenOffTimestampIfUnset(active.id, timestamp) > 0
    }
}