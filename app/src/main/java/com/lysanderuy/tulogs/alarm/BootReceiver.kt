package com.lysanderuy.tulogs.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // TODO: once Room is wired up, fetch all enabled alarms
            // from the database and reschedule each one via AlarmScheduler.
            // For now, this just needs to exist so the manifest reference resolves.
        }
    }
}