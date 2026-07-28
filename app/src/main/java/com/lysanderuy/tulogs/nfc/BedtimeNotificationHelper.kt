package com.lysanderuy.tulogs.nfc

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.lysanderuy.tulogs.R

object BedtimeNotificationHelper {

    const val CHANNEL_ID = "bedtime_confirm_channel"
    const val NOTIFICATION_ID = 3

    const val ACTION_CONFIRM_BEDTIME = "com.lysanderuy.tulogs.action.CONFIRM_BEDTIME"
    const val ACTION_DISMISS_BEDTIME = "com.lysanderuy.tulogs.action.DISMISS_BEDTIME"

    fun showBedtimeConfirmNotification(context: Context) {
        createNotificationChannel(context)

        val confirmIntent = Intent(context, BedtimeConfirmReceiver::class.java).apply {
            action = ACTION_CONFIRM_BEDTIME
        }
        val confirmPendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            confirmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dismissIntent = Intent(context, BedtimeConfirmReceiver::class.java).apply {
            action = ACTION_DISMISS_BEDTIME
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(context.getString(R.string.bedtime_notification_title))
            .setContentText(context.getString(R.string.bedtime_notification_text))
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .addAction(
                android.R.drawable.ic_menu_save,
                context.getString(R.string.bedtime_notification_confirm),
                confirmPendingIntent
            )
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                context.getString(R.string.bedtime_notification_dismiss),
                dismissPendingIntent
            )
            .build()

        context.getSystemService(NotificationManager::class.java)
            .notify(NOTIFICATION_ID, notification)
    }

    fun dismissNotification(context: Context) {
        context.getSystemService(NotificationManager::class.java)
            .cancel(NOTIFICATION_ID)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.bedtime_notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            )
            context.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }
}
