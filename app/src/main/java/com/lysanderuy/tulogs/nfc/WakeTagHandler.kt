package com.lysanderuy.tulogs.nfc

import android.content.Context
import android.content.Intent
import android.util.Log
import com.lysanderuy.tulogs.alarm.AlarmRingingService
import com.lysanderuy.tulogs.alarm.ScreenTrackingService
import com.lysanderuy.tulogs.data.AlarmRepository
import com.lysanderuy.tulogs.data.SleepLogRepository
import javax.inject.Inject

class WakeTagHandler @Inject constructor(
    private val sleepLogRepository: SleepLogRepository,
    private val alarmRepository: AlarmRepository,
) {
    suspend fun handleWakeScan(context: Context) {
        val wokeNaturally = sleepLogRepository.endActiveSession(System.currentTimeMillis())
        context.stopService(Intent(context, ScreenTrackingService::class.java))
        context.stopService(Intent(context, AlarmRingingService::class.java))
        if (wokeNaturally) {
            Log.d(TAG, "Natural wake — cancelling remaining alarms for today")
            alarmRepository.cancelRemainingToday()
        }
        Log.d(TAG, "Wake tag handled")
    }

    companion object {
        private const val TAG = "WAKE_NFC"
    }
}
