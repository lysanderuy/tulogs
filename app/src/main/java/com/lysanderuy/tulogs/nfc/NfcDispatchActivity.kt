package com.lysanderuy.tulogs.nfc

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.lysanderuy.tulogs.data.SleepLogRepository
import com.lysanderuy.tulogs.data.SleepTagRepository
import com.lysanderuy.tulogs.data.local.TagType
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

/**
 * Transparent entry point for NFC tag scans when the app is not in the foreground.
 * Foreground dispatch in [com.lysanderuy.tulogs.MainActivity] takes priority while the app is open.
 */
@AndroidEntryPoint
class NfcDispatchActivity : ComponentActivity() {

    @Inject
    lateinit var sleepTagRepository: SleepTagRepository

    @Inject
    lateinit var sleepLogRepository: SleepLogRepository

    @Inject
    lateinit var wakeTagHandler: WakeTagHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleNfcIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNfcIntent(intent)
    }

    private fun handleNfcIntent(intent: Intent) {
        val uid = NfcTagReader.readTagUid(intent)
        if (uid == null) {
            finish()
            return
        }

        Log.d(TAG, "Background NFC tag scanned, UID: $uid")

        lifecycleScope.launch {
            val wakeTag = sleepTagRepository.getTagByType(TagType.WAKE)
            if (wakeTag != null && wakeTag.uid == uid) {
                Log.d(TAG, "Wake tag matched — ending session")
                wakeTagHandler.handleWakeScan(this@NfcDispatchActivity)
            } else {
                val bedtimeTag = sleepTagRepository.getTagByType(TagType.BEDTIME)
                if (bedtimeTag != null && bedtimeTag.uid == uid && !sleepLogRepository.hasActiveSession()) {
                    Log.d(TAG, "Bedtime tag matched — showing confirm notification")
                    BedtimeNotificationHelper.showBedtimeConfirmNotification(this@NfcDispatchActivity)
                }
            }
            finish()
        }
    }

    companion object {
        private const val TAG = "NFC_DISPATCH"
    }
}
