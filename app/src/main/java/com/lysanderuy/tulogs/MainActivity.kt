package com.lysanderuy.tulogs

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.lysanderuy.tulogs.data.TagPreferences
import com.lysanderuy.tulogs.ui.theme.TuLogsTheme
import kotlinx.coroutines.launch

enum class RegistrationMode { NONE, BEDTIME, WAKE }

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    private var nfcAdapter: NfcAdapter? = null
    private lateinit var tagPreferences: TagPreferences

    private var registrationModeGetter: () -> RegistrationMode = { RegistrationMode.NONE }
    private var onUidScanned: (String) -> Unit = {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        tagPreferences = TagPreferences(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        val alarmManager = getSystemService(AlarmManager::class.java)
        val canScheduleExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
        if (!canScheduleExact) {
            startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
        }

        val notificationManager = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= 34 && !notificationManager.canUseFullScreenIntent()) {
            val intent = Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).apply {
                data = Uri.parse("package:$packageName")
            }
            startActivity(intent)
        }

        setContent {
            TuLogsTheme {
                var mode by remember { mutableStateOf(RegistrationMode.NONE) }
                var lastUid by remember { mutableStateOf<String?>(null) }

                registrationModeGetter = { mode }
                onUidScanned = { uid -> lastUid = uid }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)) {
                        Text("Registration mode: ${mode.name}")
                        Text("Last scanned UID: ${lastUid ?: "none yet"}")
                        Button(onClick = { mode = RegistrationMode.BEDTIME }) {
                            Text("Register Bedtime Tag")
                        }
                        Button(onClick = { mode = RegistrationMode.WAKE }) {
                            Text("Register Wake Tag")
                        }
                    }
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
                Log.d("NFC_TEST", "NFC tag scanned, UID: $uid")
                onUidScanned(uid)

                when (registrationModeGetter()) {
                    RegistrationMode.BEDTIME -> lifecycleScope.launch { tagPreferences.setBedtimeTag(uid) }
                    RegistrationMode.WAKE -> lifecycleScope.launch { tagPreferences.setWakeTag(uid) }
                    RegistrationMode.NONE -> {}
                }
            }
        }
    }
}