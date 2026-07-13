package com.lysanderuy.tulogs.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.lysanderuy.tulogs.data.local.Alarm
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.ZonedDateTime
import javax.inject.Inject

class AlarmScheduler @Inject constructor(@ApplicationContext private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleAlarm(alarm: Alarm, skipToday: Boolean = false, now: ZonedDateTime = ZonedDateTime.now()) {
        val triggerAtMillis = AlarmOccurrence.nextTrigger(alarm, now, skipToday).toInstant().toEpochMilli()
        val pendingIntent = alarmPendingIntent(alarm)
        alarmManager.setAlarmClock(
            AlarmManager.AlarmClockInfo(triggerAtMillis, pendingIntent),
            pendingIntent
        )
    }

    fun cancelAlarm(alarm: Alarm) {
        alarmManager.cancel(alarmPendingIntent(alarm))
    }

    /**
     * Same construction used for both scheduling and cancelling an alarm's
     * PendingIntent — request code and extras must match exactly or
     * alarmManager.cancel() silently no-ops instead of cancelling.
     */
    private fun alarmPendingIntent(alarm: Alarm): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_ALARM_ID, alarm.id)
        }
        return PendingIntent.getBroadcast(
            context,
            alarm.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        const val EXTRA_ALARM_ID = "extra_alarm_id"
    }
}
