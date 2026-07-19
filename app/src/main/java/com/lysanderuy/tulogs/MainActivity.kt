package com.lysanderuy.tulogs

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.lysanderuy.tulogs.data.AlarmRepository
import com.lysanderuy.tulogs.data.SleepTagRepository
import com.lysanderuy.tulogs.data.SleepLogRepository
import com.lysanderuy.tulogs.data.local.TagType
import com.lysanderuy.tulogs.nfc.NfcForegroundDispatcher
import com.lysanderuy.tulogs.ui.alarms.AlarmsScreen
import com.lysanderuy.tulogs.ui.alarms.AlarmsViewModel
import com.lysanderuy.tulogs.ui.home.HomeBottomNav
import com.lysanderuy.tulogs.ui.home.HomeScreen
import com.lysanderuy.tulogs.ui.home.HomeViewModel
import com.lysanderuy.tulogs.ui.tags.TagsScreen
import com.lysanderuy.tulogs.ui.tags.TagsViewModel
import com.lysanderuy.tulogs.ui.theme.Ink950
import com.lysanderuy.tulogs.ui.theme.TuLogsTheme
import com.lysanderuy.tulogs.ui.theme.TuLogsType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var sleepTagRepository: SleepTagRepository

    @Inject
    lateinit var sleepLogRepository: SleepLogRepository

    @Inject
    lateinit var alarmRepository: AlarmRepository

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    private val nfcDispatcher by lazy { NfcForegroundDispatcher(this) }

    private var registrationModeGetter: () -> TagType? = { null }
    private var onUidScanned: (String) -> Unit = {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
                val navController = rememberNavController()
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route ?: "home"

                Scaffold(
                    containerColor = Ink950,
                    bottomBar = {
                        HomeBottomNav(
                            currentRoute = currentRoute,
                            onNavigate = { route ->
                                navController.navigate(route) {
                                    popUpTo("home") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("home") {
                            val homeViewModel: HomeViewModel by viewModels()
                            val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()
                            HomeScreen(uiState = uiState)
                        }
                        composable("tags") {
                            val tagsViewModel: TagsViewModel by viewModels()
                            val uiState by tagsViewModel.uiState.collectAsStateWithLifecycle()
                            var registeringType by remember { mutableStateOf<TagType?>(null) }
                            var awaitingConfirmation by remember { mutableStateOf<TagType?>(null) }

                            registrationModeGetter = { registeringType }
                            onUidScanned = {
                                awaitingConfirmation = registeringType
                                registeringType = null
                            }

                            // awaitingConfirmation is separate from registeringType so the row keeps
                            // showing feedback until uiState actually reflects the new UID, instead of
                            // flipping back to idle the instant the tag is scanned but before the DB
                            // write + Flow emission has caught up.
                            LaunchedEffect(uiState, awaitingConfirmation) {
                                val type = awaitingConfirmation ?: return@LaunchedEffect
                                val confirmed = when (type) {
                                    TagType.BEDTIME -> uiState.bedtimeUid != null
                                    TagType.WAKE -> uiState.wakeUid != null
                                }
                                if (confirmed) awaitingConfirmation = null
                            }

                            // Navigating away from Tags must not leave a stale registration
                            // mode active — e.g. Home's passive BEDTIME-scan-to-start-session
                            // check relies on registrationModeGetter() returning null.
                            DisposableEffect(Unit) {
                                onDispose {
                                    registrationModeGetter = { null }
                                    onUidScanned = {}
                                }
                            }

                            TagsScreen(
                                uiState = uiState,
                                registeringType = registeringType,
                                awaitingConfirmation = awaitingConfirmation,
                                onScanClick = { type ->
                                    registeringType = if (registeringType == type) null else type
                                }
                            )
                        }
                        composable("alarms") {
                            val alarmsViewModel: AlarmsViewModel by viewModels()
                            val uiState by alarmsViewModel.uiState.collectAsStateWithLifecycle()
                            AlarmsScreen(
                                uiState = uiState,
                                onSave = alarmsViewModel::saveAlarm,
                                onSetEnabled = alarmsViewModel::setEnabled,
                                onDelete = alarmsViewModel::deleteAlarm
                            )
                        }
                        composable("week") {
                            ComingSoonScreen()
                        }
                    }
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
        Log.d("NFC_TEST", "NFC tag scanned, UID: $uid")
        // elapsedRealtime() is monotonic and unaffected by wall-clock changes — required for
        // measuring a round trip, unlike System.currentTimeMillis().
        Log.d("NFC_PERF", "tag_detected uid=$uid t=${SystemClock.elapsedRealtime()}")
        vibrateTagDetected()
        // Capture the mode before onUidScanned runs — it resets registeringType synchronously.
        val registrationMode = registrationModeGetter()
        onUidScanned(uid)

        when (registrationMode) {
            TagType.BEDTIME -> lifecycleScope.launch {
                Log.d("NFC_PERF", "db_write_start type=BEDTIME t=${SystemClock.elapsedRealtime()}")
                sleepTagRepository.registerTag(uid, TagType.BEDTIME)
                Log.d("NFC_PERF", "db_write_done type=BEDTIME t=${SystemClock.elapsedRealtime()}")
            }
            TagType.WAKE -> lifecycleScope.launch {
                Log.d("NFC_PERF", "db_write_start type=WAKE t=${SystemClock.elapsedRealtime()}")
                sleepTagRepository.registerTag(uid, TagType.WAKE)
                Log.d("NFC_PERF", "db_write_done type=WAKE t=${SystemClock.elapsedRealtime()}")
            }
            null -> {
                // Not in registration mode — check if this is a real BEDTIME or WAKE tag scan
                lifecycleScope.launch {
                    Log.d("NFC_PERF", "passive_check_start t=${SystemClock.elapsedRealtime()}")
                    val bedtimeTag = sleepTagRepository.getTagByType(TagType.BEDTIME)
                    if (bedtimeTag != null && bedtimeTag.uid == uid) {
                        sleepLogRepository.startSession(System.currentTimeMillis())
                        Log.d("NFC_TEST", "Sleep session started")
                        Log.d("NFC_PERF", "session_started t=${SystemClock.elapsedRealtime()}")
                    }

                    val wakeTag = sleepTagRepository.getTagByType(TagType.WAKE)
                    if (wakeTag != null && wakeTag.uid == uid) {
                        val wokeNaturally = sleepLogRepository.endActiveSession(System.currentTimeMillis())
                        if (wokeNaturally) {
                            Log.d("NFC_TEST", "Natural wake — cancelling remaining alarms for today")
                            alarmRepository.cancelRemainingToday()
                        }
                    }
                }
            }
        }
    }

    private fun vibrateTagDetected() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(VibratorManager::class.java)
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Vibrator::class.java)
        }
        vibrator?.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE))
    }
}

@Composable
private fun ComingSoonScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Ink950),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Coming soon", style = TuLogsType.statusSub)
    }
}
