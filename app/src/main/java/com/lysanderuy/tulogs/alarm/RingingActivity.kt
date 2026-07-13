package com.lysanderuy.tulogs.alarm

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.lysanderuy.tulogs.data.SleepLogRepository
import com.lysanderuy.tulogs.data.SleepTagRepository
import com.lysanderuy.tulogs.data.local.TagType
import com.lysanderuy.tulogs.nfc.NfcForegroundDispatcher
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class RingingActivity : ComponentActivity() {

    @Inject
    lateinit var sleepTagRepository: SleepTagRepository

    @Inject
    lateinit var sleepLogRepository: SleepLogRepository

    private val nfcDispatcher by lazy { NfcForegroundDispatcher(this) }
    private var onUidScanned: (String) -> Unit = {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }

        setContent {
            MaterialTheme {
                var status by remember { mutableStateOf("Tap your Wake tag to dismiss") }
                onUidScanned = { scannedUid ->
                    lifecycleScope.launch {
                        val savedWakeTag = sleepTagRepository.getTagByType(TagType.WAKE)
                        if (savedWakeTag != null && savedWakeTag.uid == scannedUid) {
                            sleepLogRepository.endActiveSession(System.currentTimeMillis())
                            status = "Dismissed!"
                            finish()
                        } else {
                            status = "Wrong tag, try again"
                        }
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Text(status, modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        nfcDispatcher.enable()
    }

    override fun onPause() {
        super.onPause()
        nfcDispatcher.disable()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val uid = nfcDispatcher.readTagUid(intent) ?: return
        onUidScanned(uid)
    }
}
