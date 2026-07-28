package com.lysanderuy.tulogs.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.lysanderuy.tulogs.R
import com.lysanderuy.tulogs.data.SleepLogRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ScreenTrackingService : Service() {

    @Inject
    lateinit var sleepLogRepository: SleepLogRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val screenOffReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d(TAG, "Screen off detected")
            serviceScope.launch {
                val recorded = sleepLogRepository.recordScreenOff(System.currentTimeMillis())
                Log.d(TAG, if (recorded) "screenOffTimestamp recorded" else "screenOffTimestamp skipped (no active session or already recorded)")
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        registerReceiver(screenOffReceiver, IntentFilter(Intent.ACTION_SCREEN_OFF))
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started")
        createNotificationChannel()

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("TuLogs")
            .setContentText("Tracking sleep session")
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")
        unregisterReceiver(screenOffReceiver)
        serviceScope.cancel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                NotificationManager.IMPORTANCE_MIN
            } else {
                NotificationManager.IMPORTANCE_LOW
            }
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Sleep Tracking",
                importance
            ).apply {
                setSound(null, null)
                enableVibration(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val TAG = "SCREEN_TRACK"
        const val CHANNEL_ID = "screen_tracking_channel"
        const val NOTIFICATION_ID = 2
    }
}
