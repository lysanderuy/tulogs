package com.lysanderuy.tulogs.nfc

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import com.lysanderuy.tulogs.data.SleepLogRepository
import com.lysanderuy.tulogs.data.SleepTagRepository
import com.lysanderuy.tulogs.data.local.TagType
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Entry point for NFC scans when the app isn't foregrounded; MainActivity's dispatch takes priority while open
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
        // Theme.NoDisplay requires finish() before onResume() completes; the DB/notification
        // work below runs in a detached scope after this so it isn't cut short by finish().
        finish()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNfcIntent(intent)
        finish()
    }

    private fun handleNfcIntent(intent: Intent) {
        val uid = NfcTagReader.readTagUid(intent) ?: return

        Log.d(TAG, "Background NFC tag scanned, UID: $uid")

        val appContext = applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            val wakeTag = sleepTagRepository.getTagByType(TagType.WAKE)
            if (wakeTag != null && wakeTag.uid == uid) {
                Log.d(TAG, "Wake tag matched — ending session")
                wakeTagHandler.handleWakeScan(appContext)
            } else {
                val bedtimeTag = sleepTagRepository.getTagByType(TagType.BEDTIME)
                if (bedtimeTag != null && bedtimeTag.uid == uid && !sleepLogRepository.hasActiveSession()) {
                    Log.d(TAG, "Bedtime tag matched — showing confirm notification")
                    BedtimeNotificationHelper.showBedtimeConfirmNotification(appContext)
                }
            }
        }
    }

    companion object {
        private const val TAG = "NFC_DISPATCH"
    }
}
