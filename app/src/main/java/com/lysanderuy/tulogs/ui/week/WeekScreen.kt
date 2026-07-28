package com.lysanderuy.tulogs.ui.week

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lysanderuy.tulogs.ui.theme.Amber500
import com.lysanderuy.tulogs.ui.theme.Ink700
import com.lysanderuy.tulogs.ui.theme.Ink800
import com.lysanderuy.tulogs.ui.theme.Ink950
import com.lysanderuy.tulogs.ui.theme.Mist600
import com.lysanderuy.tulogs.ui.theme.Paper50
import com.lysanderuy.tulogs.ui.theme.TuLogsType

@Composable
fun WeekScreen(uiState: WeekUiState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Ink950)
    ) {
        WeekHeader(dateRangeLabel = uiState.dateRangeLabel)

        if (!uiState.hasData) {
            EmptyState()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
            ) {
                AverageCallout(
                    averageDuration = uiState.averageDuration,
                    averageSubLabel = uiState.averageSubLabel
                )
                Spacer(modifier = Modifier.height(28.dp))
                BarChart(nights = uiState.nights)
                Spacer(modifier = Modifier.height(28.dp))
                NightlyList(nights = uiState.nights)
            }
        }
    }
}

@Composable
private fun WeekHeader(dateRangeLabel: String?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        Text(text = "Week", style = TuLogsType.statusHeadline)
        dateRangeLabel?.let { label ->
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = label, style = TuLogsType.monoLabel)
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "No nights logged yet", style = TuLogsType.statusHeadline)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tap in tonight and this page starts filling in.",
            style = TuLogsType.captionText,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AverageCallout(averageDuration: String?, averageSubLabel: String?) {
    Row(verticalAlignment = Alignment.Bottom) {
        Text(
            text = averageDuration ?: "—",
            style = TuLogsType.factValue.copy(fontSize = 38.sp, color = Paper50)
        )
        averageSubLabel?.let { label ->
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = TuLogsType.captionText.copy(fontSize = 15.sp),
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
    }
}

private val barWidth = 16.dp

@Composable
private fun BarChart(nights: List<NightUiState>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        nights.forEach { night -> BarColumn(night = night, modifier = Modifier.weight(1f)) }
    }
}

@Composable
private fun BarColumn(night: NightUiState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Box(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentAlignment = Alignment.BottomCenter
        ) {
            if (night.hasDuration) {
                Box(
                    modifier = Modifier
                        .width(barWidth)
                        .fillMaxHeight(night.barHeightFraction)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Amber500)
                )
            } else {
                Box(
                    modifier = Modifier
                        .width(barWidth)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .border(width = 1.dp, color = Ink700, shape = RoundedCornerShape(4.dp))
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = night.weekdayLetter, style = TuLogsType.monoLabel)
    }
}

@Composable
private fun NightlyList(nights: List<NightUiState>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = "THIS WEEK", style = TuLogsType.monoLabel)
        Spacer(modifier = Modifier.height(16.dp))
        nights.forEach { night ->
            NightRow(night)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Ink800)
            )
        }
    }
}

@Composable
private fun NightRow(night: NightUiState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = night.dateLabel, style = TuLogsType.captionText, color = Paper50)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = timeRangeLabel(night), style = TuLogsType.captionText)
        }
        Text(
            text = night.duration ?: "—",
            style = TuLogsType.factValue,
            color = if (night.duration != null) Paper50 else Mist600
        )
    }
}

private fun timeRangeLabel(night: NightUiState): String {
    return when {
        night.bedtime == null -> "No log"
        night.wake == null -> "${night.bedtime} → still sleeping"
        else -> "${night.bedtime} → ${night.wake}"
    }
}
