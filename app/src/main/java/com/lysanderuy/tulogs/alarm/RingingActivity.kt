package com.lysanderuy.tulogs.alarm

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.lysanderuy.tulogs.data.SleepLogRepository
import com.lysanderuy.tulogs.data.SleepTagRepository
import com.lysanderuy.tulogs.data.local.TagType
import com.lysanderuy.tulogs.nfc.NfcForegroundDispatcher
import com.lysanderuy.tulogs.nfc.WakeTagHandler
import com.lysanderuy.tulogs.ui.theme.Amber500
import com.lysanderuy.tulogs.ui.theme.Error500
import com.lysanderuy.tulogs.ui.theme.Ink950
import com.lysanderuy.tulogs.ui.theme.JakartaFamily
import com.lysanderuy.tulogs.ui.theme.Mist400
import com.lysanderuy.tulogs.ui.theme.Mist600
import com.lysanderuy.tulogs.ui.theme.OutfitFamily
import com.lysanderuy.tulogs.ui.theme.Paper50
import com.lysanderuy.tulogs.ui.theme.TuLogsTheme
import com.lysanderuy.tulogs.ui.theme.TuLogsType
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch
import javax.inject.Inject

private enum class RingingUiState { RINGING, WRONG_TAG }

private val ringingHourFormatter = DateTimeFormatter.ofPattern("h:mm")
private val ringingAmPmFormatter = DateTimeFormatter.ofPattern("a")

private val instructionStyle = TextStyle(
    fontFamily = OutfitFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 24.sp,
    lineHeight = 32.sp,
    textAlign = TextAlign.Center
)

private val instructionSubStyle = TextStyle(
    fontFamily = JakartaFamily,
    fontSize = 14.sp,
    lineHeight = 21.sp,
    color = Mist600,
    textAlign = TextAlign.Center
)

@AndroidEntryPoint
class RingingActivity : ComponentActivity() {

    @Inject
    lateinit var sleepTagRepository: SleepTagRepository

    @Inject
    lateinit var sleepLogRepository: SleepLogRepository

    @Inject
    lateinit var wakeTagHandler: WakeTagHandler

    private val nfcDispatcher by lazy { NfcForegroundDispatcher(this) }
    private var onUidScanned: (String) -> Unit = {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }

        val label = intent?.getStringExtra(EXTRA_LABEL).orEmpty()
        val now = LocalTime.now()

        setContent {
            TuLogsTheme {
                var uiState by remember { mutableStateOf(RingingUiState.RINGING) }
                onUidScanned = { scannedUid ->
                    lifecycleScope.launch {
                        val savedWakeTag = sleepTagRepository.getTagByType(TagType.WAKE)
                        if (savedWakeTag != null && savedWakeTag.uid == scannedUid) {
                            wakeTagHandler.handleWakeScan(this@RingingActivity)
                            finish()
                        } else {
                            uiState = RingingUiState.WRONG_TAG
                        }
                    }
                }

                RingingScreen(uiState = uiState, label = label, time = now)
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

    companion object {
        const val EXTRA_LABEL = "extra_label"
    }
}

@Composable
private fun RingingScreen(uiState: RingingUiState, label: String, time: LocalTime) {
    val isWrongTag = uiState == RingingUiState.WRONG_TAG
    val accentColor = if (isWrongTag) Error500 else Amber500

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Ink950),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RingingDot(pulsing = !isWrongTag, color = accentColor)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ALARM",
                    style = TuLogsType.monoLabel.copy(color = if (isWrongTag) Error500 else Mist600)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = time.format(ringingHourFormatter),
                    style = TuLogsType.factValue.copy(
                        fontSize = 60.sp,
                        letterSpacing = (-0.02).em,
                        color = Paper50,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = time.format(ringingAmPmFormatter),
                    style = TuLogsType.captionText.copy(fontSize = 24.sp, color = Mist400),
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            if (label.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = label, style = TuLogsType.captionText.copy(color = Mist400, fontSize = 14.sp))
            }

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = if (isWrongTag) "Wrong tag, try again." else "Tap your Wake tag to dismiss.",
                style = instructionStyle.copy(color = if (isWrongTag) Error500 else Paper50),
                modifier = Modifier.widthIn(max = 250.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (isWrongTag) "That's not your Wake tag." else "Unlock your phone, and tap your phone on WAKE tag.",
                style = instructionSubStyle,
                modifier = Modifier.widthIn(max = 230.dp)
            )
        }
    }
}

@Composable
private fun RingingDot(pulsing: Boolean, color: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "ringing-dot-pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ringing-dot-pulse-alpha"
    )

    Box(
        modifier = Modifier
            .size(6.dp)
            .alpha(if (pulsing) pulseAlpha else 1f)
            .clip(CircleShape)
            .background(color)
    )
}
