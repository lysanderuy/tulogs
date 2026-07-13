package com.lysanderuy.tulogs.ui.tags

import android.os.SystemClock
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.lysanderuy.tulogs.data.local.TagType
import com.lysanderuy.tulogs.ui.theme.Amber500
import com.lysanderuy.tulogs.ui.theme.Ink800
import com.lysanderuy.tulogs.ui.theme.Ink950
import com.lysanderuy.tulogs.ui.theme.Mist600
import com.lysanderuy.tulogs.ui.theme.Paper50
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
            .padding(24.dp)
    ) {
        Text(text = "Tags", style = TuLogsType.statusHeadline)
        Spacer(modifier = Modifier.height(24.dp))
        TagRow(
            label = "Bedtime",
            uid = uiState.bedtimeUid,
            status = tagRowStatus(TagType.BEDTIME, registeringType, awaitingConfirmation),
            onScanClick = { onScanClick(TagType.BEDTIME) }
        )
        Spacer(modifier = Modifier.height(16.dp))
        TagRow(
            label = "Wake",
            uid = uiState.wakeUid,
            status = tagRowStatus(TagType.WAKE, registeringType, awaitingConfirmation),
            onScanClick = { onScanClick(TagType.WAKE) }
        )
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
private fun TagRow(
    label: String,
    uid: String?,
    status: TagRowStatus,
    onScanClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Ink800)
            .padding(16.dp)
    ) {
        Text(text = label, style = TuLogsType.monoLabel)
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = uid ?: "Not set", style = TuLogsType.factValue, color = Paper50)
        Spacer(modifier = Modifier.height(12.dp))
        when (status) {
            TagRowStatus.WAITING -> Text(text = "Waiting for tag…", style = TuLogsType.captionText, color = Amber500)
            TagRowStatus.SAVING -> Text(text = "Tag detected — saving…", style = TuLogsType.captionText, color = Amber500)
            TagRowStatus.IDLE -> Text(
                text = "Scan to register",
                style = TuLogsType.captionText,
                color = Mist600,
                modifier = Modifier.clickable(onClick = onScanClick)
            )
        }
    }
}
