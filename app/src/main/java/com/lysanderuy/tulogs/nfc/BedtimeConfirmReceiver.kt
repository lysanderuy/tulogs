package com.lysanderuy.tulogs.nfc

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.lysanderuy.tulogs.alarm.ScreenTrackingService
import com.lysanderuy.tulogs.data.SleepLogRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BedtimeConfirmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var sleepLogRepository: SleepLogRepository

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            BedtimeNotificationHelper.ACTION_CONFIRM_BEDTIME -> {
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        if (sleepLogRepository.hasActiveSession()) {
                            Log.d(TAG, "Bedtime confirm ignored — session already active")
                        } else {
                            sleepLogRepository.startSession(System.currentTimeMillis())
                            context.startForegroundService(
                                Intent(context, ScreenTrackingService::class.java)
                            )
                            Log.d(TAG, "Bedtime session started from notification")
                        }
                    } finally {
                        BedtimeNotificationHelper.dismissNotification(context)
                        pendingResult.finish()
                    }
                }
            }
            BedtimeNotificationHelper.ACTION_DISMISS_BEDTIME -> {
                BedtimeNotificationHelper.dismissNotification(context)
            }
        }
    }

    companion object {
        private const val TAG = "BEDTIME_NFC"
    }
}
