package com.lysanderuy.tulogs.nfc

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import androidx.activity.ComponentActivity

class NfcForegroundDispatcher(private val activity: ComponentActivity) {

    private val nfcAdapter: NfcAdapter? = NfcAdapter.getDefaultAdapter(activity)

    fun enable() {
        val intent = Intent(activity, activity.javaClass).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(activity, 0, intent, PendingIntent.FLAG_MUTABLE)
        nfcAdapter?.enableForegroundDispatch(activity, pendingIntent, null, null)
    }

    fun disable() {
        nfcAdapter?.disableForegroundDispatch(activity)
    }

    fun readTagUid(intent: Intent): String? = NfcTagReader.readTagUid(intent)
}
