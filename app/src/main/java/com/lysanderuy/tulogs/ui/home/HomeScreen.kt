package com.lysanderuy.tulogs.ui.home

import android.os.SystemClock
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lysanderuy.tulogs.ui.theme.Amber500
import com.lysanderuy.tulogs.ui.theme.Ink700
import com.lysanderuy.tulogs.ui.theme.Ink800
import com.lysanderuy.tulogs.ui.theme.Mist600
import com.lysanderuy.tulogs.ui.theme.Paper50
import com.lysanderuy.tulogs.ui.theme.Periwinkle400
import com.lysanderuy.tulogs.ui.theme.TuLogsTheme
import com.lysanderuy.tulogs.ui.theme.TuLogsType

/**
 * Home screen UI state. Owned by HomeViewModel in the real app — this file
 * only defines the shape, so the composable stays previewable/testable
 * without needing Hilt or Room wired up.
 */
data class HomeUiState(
    val dateLabel: String,          // "TUE 14 JUL"
    val alarmTime: String,          // "6:30 AM"
    val alarmDays: String,          // "Weekdays"
    val isBedtimeLogged: Boolean,   // false = "before bed" state, true = "during sleep" state
    val bedtimeLoggedAt: String?,   // "11:02 PM" — only meaningful when isBedtimeLogged
    val lastNight: LastNightUiState?
)

data class LastNightUiState(
    val bedtime: String,            // "11:02 PM"
    val wake: String,               // "6:24 AM"
    val qualityRating: Int,         // 1..5, from SleepLog.qualityRating
    val screenOnAfterMinutes: Int?, // derived from screenOffTimestamp vs bedtimeTimestamp, nullable
    val duration: String            // "7h 42m" — derived from wakeTimestamp - bedtimeTimestamp
)

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onSettingsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    LaunchedEffect(uiState.isBedtimeLogged) {
        Log.d("NFC_PERF", "home_bedtime_logged=${uiState.isBedtimeLogged} t=${SystemClock.elapsedRealtime()}")
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        HomeHeader(
            dateLabel = uiState.dateLabel,
            onSettingsClick = onSettingsClick
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(24.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            StatusBlock(uiState)
        }

        uiState.lastNight?.let { LastNightFacts(it) }
    }
}

@Composable
private fun HomeHeader(
    dateLabel: String,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = dateLabel, style = TuLogsType.monoLabel)
        Icon(
            imageVector = Icons.Outlined.Settings,
            contentDescription = "Settings",
            tint = Mist600,
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .clickable(onClick = onSettingsClick)
        )
    }
}

/**
 * The one honest sentence on Home. Content — not just color — changes with
 * state: showing a countdown *after* the bedtime tap would be misleading,
 * so "before bed" and "during sleep" are genuinely different copy, not a
 * reskinned version of the same string.
 */
@Composable
private fun StatusBlock(uiState: HomeUiState) {
    Column {
        StatusEyebrow(
            label = if (uiState.isBedtimeLogged) "Bedtime logged" else "Tonight",
            confirmed = uiState.isBedtimeLogged
        )

        Spacer(modifier = Modifier.height(4.dp))

        val headline = buildAnnotatedString {
            if (uiState.isBedtimeLogged) {
                append("Tapped in at ")
                withStyle(SpanStyle(color = Paper50, fontWeight = TuLogsType.statusHeadlineEmphasisWeight)) {
                    append("${uiState.bedtimeLoggedAt}.")
                }
            } else {
                append("Alarm set for ")
                withStyle(SpanStyle(color = Paper50, fontWeight = TuLogsType.statusHeadlineEmphasisWeight)) {
                    append("${uiState.alarmTime}.")
                }
            }
        }
        Text(text = headline, style = TuLogsType.statusHeadline)

        Spacer(modifier = Modifier.height(10.dp))

        val sub = if (uiState.isBedtimeLogged) {
            "Alarm at ${uiState.alarmTime}"
        } else {
            "${uiState.alarmDays} · nothing else to do yet"
        }
        Text(text = sub, style = TuLogsType.statusSub)
    }
}

/**
 * Subtle status signal — hollow ring while waiting, filled amber (with a
 * soft glow) once confirmed. Deliberately just a dot, not an icon: it
 * reuses the same visual language as the quality dots below rather than
 * introducing new decorative artwork.
 */
@Composable
private fun StatusEyebrow(label: String, confirmed: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(if (confirmed) Amber500 else Color.Transparent)
                .border(
                    width = 1.5.dp,
                    color = if (confirmed) Amber500 else Mist600,
                    shape = CircleShape
                )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = TuLogsType.monoLabel,
            color = if (confirmed) Amber500 else Mist600
        )
    }
}

/**
 * Plain facts from last night — no framing as good/bad, just what happened.
 * Pulled from the most recently completed SleepLog row.
 */
@Composable
private fun LastNightFacts(lastNight: LastNightUiState) {
    Column {
        Text(
            text = buildAnnotatedString {
                append("You got ")
                withStyle(SpanStyle(color = Paper50, fontWeight = TuLogsType.statusHeadlineEmphasisWeight)) {
                    append(lastNight.duration)
                }
                append(" last night.")
            },
            style = TuLogsType.captionText,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalHairlines(Ink800)
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FactColumn(label = "Bedtime", value = lastNight.bedtime, modifier = Modifier.weight(1f))
            FactDivider()
            FactColumn(label = "Wake", value = lastNight.wake, modifier = Modifier.weight(1f))
            FactDivider()
            FactColumn(
                label = "Quality",
                value = qualityDots(lastNight.qualityRating),
                valueColor = Amber500,
                modifier = Modifier.weight(1f)
            )
        }

        lastNight.screenOnAfterMinutes?.let { minutes ->
            Text(
                text = buildAnnotatedString {
                    append("Screen stayed on ")
                    withStyle(SpanStyle(color = Periwinkle400, fontWeight = TuLogsType.statusHeadlineEmphasisWeight)) {
                        append("$minutes min")
                    }
                    append(" after bedtime tap, last night.")
                },
                style = TuLogsType.captionText,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            )
        }
    }
}

@Composable
private fun FactColumn(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = Paper50
) {
    Column(modifier = modifier) {
        Text(text = label, style = TuLogsType.monoLabel)
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = value, style = TuLogsType.factValue, color = valueColor)
    }
}

@Composable
private fun FactDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(32.dp)
            .background(Ink800)
    )
}

private fun qualityDots(rating: Int, total: Int = 5): String {
    val filled = "●".repeat(rating.coerceIn(0, total))
    val empty = "○".repeat((total - rating).coerceIn(0, total))
    return filled + empty
}

/**
 * Top + bottom hairlines only — a full box border reads as a floating card
 * whose side edges look arbitrary against the full-bleed layout on wider
 * screens. Full-width top/bottom rules read as section dividers instead.
 */
private fun Modifier.horizontalHairlines(color: Color, thickness: Dp = 1.dp): Modifier = drawBehind {
    val strokeWidthPx = thickness.toPx()
    drawLine(color = color, start = Offset(0f, 0f), end = Offset(size.width, 0f), strokeWidth = strokeWidthPx)
    drawLine(color = color, start = Offset(0f, size.height), end = Offset(size.width, size.height), strokeWidth = strokeWidthPx)
}

@Composable
fun HomeBottomNav(currentRoute: String, onNavigate: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalHairlines(Ink800)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        NavItem(
            label = "Home",
            icon = Icons.Outlined.Home,
            active = currentRoute == "home",
            onClick = { onNavigate("home") }
        )
        NavItem(
            label = "Alarms",
            icon = Icons.Outlined.AccessTime,
            active = currentRoute == "alarms",
            onClick = { onNavigate("alarms") }
        )
        NavItem(
            label = "Tags",
            icon = Icons.Outlined.Sell,
            active = currentRoute == "tags",
            onClick = { onNavigate("tags") }
        )
        NavItem(
            label = "Week",
            icon = Icons.Outlined.BarChart,
            active = currentRoute == "week",
            onClick = { onNavigate("week") }
        )
    }
}

@Composable
private fun NavItem(label: String, icon: ImageVector, active: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (active) Paper50 else Mist600,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = TuLogsType.navLabel,
            color = if (active) Paper50 else Mist600
        )
        if (active) {
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .size(3.dp)
                    .clip(CircleShape)
                    .background(Amber500)
            )
        }
    }
}

// ---------------------------------------------------------------------
// Previews — render instantly in Android Studio, no emulator required.
// ---------------------------------------------------------------------

private val previewIdleState = HomeUiState(
    dateLabel = "TUE 14 JUL",
    alarmTime = "06:30",
    alarmDays = "Weekdays",
    isBedtimeLogged = false,
    bedtimeLoggedAt = null,
    lastNight = LastNightUiState(
        bedtime = "23:02",
        wake = "06:24",
        qualityRating = 4,
        screenOnAfterMinutes = 32,
        duration = "7h 22m"
    )
)

private val previewSessionState = previewIdleState.copy(
    isBedtimeLogged = true,
    bedtimeLoggedAt = "23:02"
)

@Preview(name = "Home — before bed", showBackground = true, backgroundColor = 0xFF05070C)
@Composable
private fun HomeScreenIdlePreview() {
    TuLogsTheme {
        HomeScreen(uiState = previewIdleState)
    }
}

@Preview(name = "Home — during sleep", showBackground = true, backgroundColor = 0xFF05070C)
@Composable
private fun HomeScreenSessionPreview() {
    TuLogsTheme {
        HomeScreen(uiState = previewSessionState)
    }
}
