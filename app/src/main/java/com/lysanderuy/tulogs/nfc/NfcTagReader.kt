package com.lysanderuy.tulogs.nfc

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build

object NfcTagReader {

    fun readTagUid(intent: Intent): String? {
        if (intent.action != NfcAdapter.ACTION_TAG_DISCOVERED &&
            intent.action != NfcAdapter.ACTION_TECH_DISCOVERED
        ) {
            return null
        }
        val tag: Tag? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
        }
        return tag?.id?.joinToString(":") { byte -> "%02X".format(byte) }
    }
}
