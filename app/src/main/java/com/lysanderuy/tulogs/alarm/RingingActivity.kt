package com.lysanderuy.tulogs.alarm

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class RingingActivity : ComponentActivity() {

    @Inject
    lateinit var sleepTagRepository: SleepTagRepository

    @Inject
    lateinit var sleepLogRepository: SleepLogRepository

    private var nfcAdapter: NfcAdapter? = null
    private var onUidScanned: (String) -> Unit = {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

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
        val intent = Intent(this, javaClass).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE)
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, null, null)
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.action == NfcAdapter.ACTION_TAG_DISCOVERED ||
            intent.action == NfcAdapter.ACTION_TECH_DISCOVERED
        ) {
            val tag: Tag? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            }
            tag?.let {
                val uid = it.id.joinToString(":") { byte -> "%02X".format(byte) }
                onUidScanned(uid)
            }
        }
    }
}