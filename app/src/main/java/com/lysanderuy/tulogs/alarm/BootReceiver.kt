package com.lysanderuy.tulogs.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.lysanderuy.tulogs.data.local.AlarmDao
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var alarmDao: AlarmDao

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                alarmDao.getEnabledAlarms().first().forEach { alarm ->
                    alarmScheduler.scheduleAlarm(alarm)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
