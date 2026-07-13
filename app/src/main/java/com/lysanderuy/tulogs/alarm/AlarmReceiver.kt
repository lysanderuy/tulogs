package com.lysanderuy.tulogs.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.lysanderuy.tulogs.data.local.AlarmDao
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var alarmDao: AlarmDao

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getLongExtra(AlarmScheduler.EXTRA_ALARM_ID, -1L)
        if (alarmId == -1L) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val alarm = alarmDao.getAlarmById(alarmId)
                if (alarm != null) {
                    if (alarm.daysOfWeek.isNotEmpty()) {
                        alarmScheduler.scheduleAlarm(alarm, skipToday = true)
                    } else {
                        alarmDao.update(alarm.copy(isEnabled = false))
                    }

                    val serviceIntent = Intent(context, AlarmRingingService::class.java).apply {
                        putExtra(AlarmRingingService.EXTRA_LABEL, alarm.label)
                    }
                    context.startForegroundService(serviceIntent)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
