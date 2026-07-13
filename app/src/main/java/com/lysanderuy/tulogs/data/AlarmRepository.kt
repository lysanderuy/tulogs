package com.lysanderuy.tulogs.data

import com.lysanderuy.tulogs.alarm.AlarmOccurrence
import com.lysanderuy.tulogs.alarm.AlarmScheduler
import com.lysanderuy.tulogs.data.local.Alarm
import com.lysanderuy.tulogs.data.local.AlarmDao
import java.time.LocalDateTime
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class AlarmRepository @Inject constructor(
    private val alarmDao: AlarmDao,
    private val alarmScheduler: AlarmScheduler
) {
    val allAlarms: Flow<List<Alarm>> = alarmDao.getAllAlarms()

    suspend fun getAlarmById(id: Long): Alarm? = alarmDao.getAlarmById(id)

    suspend fun saveAlarm(alarm: Alarm) {
        // A one-time alarm saved enabled rolls forward to its next occurrence
        // if the picked date+time has already passed — matches stock
        // alarm-clock behavior instead of refusing to save it.
        val toPersist = if (alarm.isEnabled) AlarmOccurrence.rollToUpcoming(alarm) else alarm
        val id = if (toPersist.id == 0L) alarmDao.insert(toPersist) else {
            alarmDao.update(toPersist)
            toPersist.id
        }
        val saved = toPersist.copy(id = id)
        if (saved.isEnabled) alarmScheduler.scheduleAlarm(saved) else alarmScheduler.cancelAlarm(saved)
    }

    suspend fun setEnabled(alarm: Alarm, isEnabled: Boolean) {
        val updated = if (isEnabled) {
            AlarmOccurrence.rollToUpcoming(alarm).copy(isEnabled = true)
        } else {
            alarm.copy(isEnabled = false)
        }
        alarmDao.update(updated)
        if (isEnabled) alarmScheduler.scheduleAlarm(updated) else alarmScheduler.cancelAlarm(updated)
    }

    suspend fun deleteAlarm(alarm: Alarm) {
        alarmScheduler.cancelAlarm(alarm)
        alarmDao.delete(alarm)
    }

    /**
     * Early-wake auto-cancel: called when a WAKE tap closes an active
     * SleepLog session before any alarm fired. Cancels today's remaining
     * enabled alarms and reschedules each for its next applicable day.
     */
    suspend fun cancelRemainingToday() {
        val now = LocalDateTime.now()
        alarmDao.getEnabledAlarms().first()
            .filter { alarm ->
                val scheduledToday = if (alarm.daysOfWeek.isEmpty()) {
                    alarm.date == now.toLocalDate()
                } else {
                    alarm.daysOfWeek.contains(now.dayOfWeek)
                }
                scheduledToday && alarm.hour * 60 + alarm.minute > now.hour * 60 + now.minute
            }
            .forEach { alarm ->
                alarmScheduler.cancelAlarm(alarm)
                if (alarm.daysOfWeek.isNotEmpty()) {
                    alarmScheduler.scheduleAlarm(alarm, skipToday = true)
                } else {
                    alarmDao.update(alarm.copy(isEnabled = false))
                }
            }
    }
}
