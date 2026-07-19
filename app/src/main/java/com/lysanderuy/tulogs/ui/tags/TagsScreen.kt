package com.lysanderuy.tulogs.ui.tags

import android.os.SystemClock
import android.util.Log
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lysanderuy.tulogs.data.local.TagType
import com.lysanderuy.tulogs.ui.theme.Amber500
import com.lysanderuy.tulogs.ui.theme.Ink800
import com.lysanderuy.tulogs.ui.theme.Ink950
import com.lysanderuy.tulogs.ui.theme.Mist200
import com.lysanderuy.tulogs.ui.theme.Mist600
import com.lysanderuy.tulogs.ui.theme.Periwinkle400
import com.lysanderuy.tulogs.ui.theme.TuLogsType

private enum class TagRowStatus { IDLE, WAITING, SAVING }

@Composable
fun TagsScreen(
    uiState: TagsUiState,
    registeringType: TagType?,
    awaitingConfirmation: TagType?,
    onScanClick: (TagType) -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(uiState) {
        Log.d(
            "NFC_PERF",
            "tags_ui_recomposed bedtimeUid=${uiState.bedtimeUid} wakeUid=${uiState.wakeUid} t=${SystemClock.elapsedRealtime()}"
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Ink950)
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = "Tags",
            style = TuLogsType.statusHeadline,
            modifier = Modifier.padding(vertical = 24.dp)
        )

        TagCard(
            role = TagType.BEDTIME.name,
            uid = uiState.bedtimeUid,
            status = tagRowStatus(TagType.BEDTIME, registeringType, awaitingConfirmation),
            onActionClick = { onScanClick(TagType.BEDTIME) }
        )
        Spacer(modifier = Modifier.height(14.dp))
        TagCard(
            role = TagType.WAKE.name,
            uid = uiState.wakeUid,
            status = tagRowStatus(TagType.WAKE, registeringType, awaitingConfirmation),
            onActionClick = { onScanClick(TagType.WAKE) }
        )

        Spacer(modifier = Modifier.weight(1f, fill = true))

        // Dev-only: raw UIDs aren't meaningful to end users, but seeing exactly
        // which physical tag got scanned is the fastest way to debug registration.
        Column(modifier = Modifier.padding(bottom = 16.dp)) {
            Text(
                text = "BEDTIME ${uiState.bedtimeUid ?: "—"}",
                style = TuLogsType.monoLabel.copy(fontSize = 9.sp)
            )
            Text(
                text = "WAKE ${uiState.wakeUid ?: "—"}",
                style = TuLogsType.monoLabel.copy(fontSize = 9.sp)
            )
        }
    }
}

private fun tagRowStatus(
    type: TagType,
    registeringType: TagType?,
    awaitingConfirmation: TagType?
): TagRowStatus = when {
    registeringType == type -> TagRowStatus.WAITING
    awaitingConfirmation == type -> TagRowStatus.SAVING
    else -> TagRowStatus.IDLE
}

@Composable
private fun TagCard(
    role: String,
    uid: String?,
    status: TagRowStatus,
    onActionClick: () -> Unit
) {
    val isRegistered = uid != null
    val isScanning = status == TagRowStatus.WAITING
    val borderColor = if (isScanning) Periwinkle400 else Ink800

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = role, style = TuLogsType.monoLabel)
            when (status) {
                TagRowStatus.WAITING -> Text(
                    text = "Cancel",
                    style = TuLogsType.captionText.copy(color = Periwinkle400, fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.clickable(onClick = onActionClick)
                )
                TagRowStatus.IDLE -> Text(
                    text = if (isRegistered) "Re-scan" else "Register",
                    style = TuLogsType.captionText.copy(color = Amber500, fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.clickable(onClick = onActionClick)
                )
                TagRowStatus.SAVING -> {}
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            StatusDot(status = status, isRegistered = isRegistered)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = when (status) {
                    TagRowStatus.WAITING -> "Waiting for tag…"
                    TagRowStatus.SAVING -> "Tag detected — saving…"
                    TagRowStatus.IDLE -> if (isRegistered) "Tag registered" else "Not registered"
                },
                style = TuLogsType.statusHeadline.copy(
                    fontSize = 16.sp,
                    lineHeight = 20.sp,
                    color = if (status == TagRowStatus.IDLE && isRegistered) Amber500 else Mist200
                )
            )
        }

        val caption = when (status) {
            TagRowStatus.WAITING -> "Hold your phone near the tag"
            TagRowStatus.SAVING -> "Confirming…"
            TagRowStatus.IDLE -> if (isRegistered) null else "Tap to scan a tag"
        }
        if (caption != null) {
            Text(
                text = caption,
                style = TuLogsType.captionText.copy(
                    fontSize = 12.5.sp,
                    color = if (isScanning) Periwinkle400 else Mist600
                ),
                modifier = Modifier.padding(start = 14.dp, top = 6.dp)
            )
        }
    }
}

@Composable
private fun StatusDot(status: TagRowStatus, isRegistered: Boolean) {
    val isScanning = status == TagRowStatus.WAITING
    val infiniteTransition = rememberInfiniteTransition(label = "tag-dot-pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tag-dot-pulse-alpha"
    )
    val isFilled = status == TagRowStatus.IDLE && isRegistered

    Box(
        modifier = Modifier
            .size(8.dp)
            .alpha(if (isScanning) pulseAlpha else 1f)
            .clip(CircleShape)
            .background(if (isFilled) Amber500 else Color.Transparent)
            .border(
                width = 1.5.dp,
                color = when {
                    isFilled -> Amber500
                    isScanning -> Periwinkle400
                    else -> Mist600
                },
                shape = CircleShape
            )
    )
}
