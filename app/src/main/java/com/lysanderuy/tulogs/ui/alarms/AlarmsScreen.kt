package com.lysanderuy.tulogs.ui.alarms

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lysanderuy.tulogs.alarm.AlarmOccurrence
import com.lysanderuy.tulogs.data.local.Alarm
import com.lysanderuy.tulogs.ui.theme.Amber500
import com.lysanderuy.tulogs.ui.theme.Error500
import com.lysanderuy.tulogs.ui.theme.Ink700
import com.lysanderuy.tulogs.ui.theme.Ink800
import com.lysanderuy.tulogs.ui.theme.Ink900
import com.lysanderuy.tulogs.ui.theme.Ink950
import com.lysanderuy.tulogs.ui.theme.Mist400
import com.lysanderuy.tulogs.ui.theme.Mist600
import com.lysanderuy.tulogs.ui.theme.Paper50
import com.lysanderuy.tulogs.ui.theme.TuLogsType
import java.time.DayOfWeek
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.floor
import kotlinx.coroutines.delay

private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val clockFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
private val dateSummaryFormatter = DateTimeFormatter.ofPattern("MMM d")
private val weekOrder = listOf(
    DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY,
    DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
)
private val weekdaySet = setOf(
    DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
)
private val weekendSet = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
private val wheelItemHeight = 44.dp
private const val wheelVisibleCount = 5
private const val wheelRepeatCount = 100_000

@Composable
fun AlarmsScreen(
    uiState: AlarmsUiState,
    onSave: (Alarm) -> Unit,
    onSetEnabled: (Alarm, Boolean) -> Unit,
    onDelete: (Alarm) -> Unit,
    modifier: Modifier = Modifier
) {
    var editingAlarm by remember { mutableStateOf<Alarm?>(null) }
    var isEditing by remember { mutableStateOf(false) }
    var ringInMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(ringInMessage) {
        if (ringInMessage != null) {
            delay(2500)
            ringInMessage = null
        }
    }

    // Best-effort preview of what the repository will actually schedule —
    // rollToUpcoming is the same pure function it uses, so this matches
    // exactly without needing a round trip through Room.
    fun showRingInMessage(alarm: Alarm) {
        if (!alarm.isEnabled) return
        val trigger = AlarmOccurrence.nextTrigger(AlarmOccurrence.rollToUpcoming(alarm))
        ringInMessage = "Ring in ${formatCountdown(Duration.between(ZonedDateTime.now(), trigger))}"
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Ink950)
    ) {
        if (isEditing) {
            AlarmEditScreen(
                alarm = editingAlarm,
                onBack = { isEditing = false },
                onSave = { alarm ->
                    onSave(alarm)
                    showRingInMessage(alarm)
                    isEditing = false
                },
                onDelete = { alarm ->
                    onDelete(alarm)
                    isEditing = false
                }
            )
        } else {
            AlarmListScreen(
                alarms = uiState.alarms,
                onAddClick = {
                    editingAlarm = null
                    isEditing = true
                },
                onRowClick = { alarm ->
                    editingAlarm = alarm
                    isEditing = true
                },
                onSetEnabled = { alarm, enabled ->
                    onSetEnabled(alarm, enabled)
                    if (enabled) showRingInMessage(alarm.copy(isEnabled = true))
                }
            )
        }

        ringInMessage?.let { message ->
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Ink800)
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(text = message, style = TuLogsType.captionText.copy(color = Paper50))
            }
        }
    }
}

@Composable
private fun AlarmListScreen(
    alarms: List<Alarm>,
    onAddClick: () -> Unit,
    onRowClick: (Alarm) -> Unit,
    onSetEnabled: (Alarm, Boolean) -> Unit
) {
    var now by remember { mutableStateOf(ZonedDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = ZonedDateTime.now()
            delay(1000)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Alarms", style = TuLogsType.statusHeadline)
            IconCircle(icon = Icons.Outlined.Add, contentDescription = "Add alarm", onClick = onAddClick)
        }

        LiveClockBlock(now = now, alarms = alarms)

        if (alarms.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "No alarms yet", style = TuLogsType.statusHeadline)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Add one to get started — TuLogs will remind you with a tag tap, not a nag.",
                    style = TuLogsType.captionText,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
                alarms.forEach { alarm ->
                    AlarmRow(
                        alarm = alarm,
                        onClick = { onRowClick(alarm) },
                        onSetEnabled = { enabled -> onSetEnabled(alarm, enabled) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LiveClockBlock(now: ZonedDateTime, alarms: List<Alarm>) {
    val nextTrigger = alarms
        .filter { it.isEnabled && AlarmOccurrence.isUpcoming(it, now) }
        .map { AlarmOccurrence.nextTrigger(it, now) }
        .minByOrNull { it.toInstant().toEpochMilli() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = now.format(clockFormatter),
            style = TuLogsType.factValue.copy(fontSize = 40.sp, color = Paper50)
        )
        if (nextTrigger != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Ring in ${formatCountdown(Duration.between(now, nextTrigger))}",
                style = TuLogsType.captionText
            )
        }
    }
}

private fun formatCountdown(duration: Duration): String {
    val totalMinutes = duration.toMinutes().coerceAtLeast(0)
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
}

@Composable
private fun AlarmRow(
    alarm: Alarm,
    onClick: () -> Unit,
    onSetEnabled: (Boolean) -> Unit
) {
    val contentAlpha = if (alarm.isEnabled) 1f else 0.38f
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.alpha(contentAlpha)) {
            Text(
                text = LocalTime.of(alarm.hour, alarm.minute).format(timeFormatter),
                style = TuLogsType.factValue.copy(fontSize = 24.sp)
            )
            if (alarm.label.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = alarm.label, style = TuLogsType.captionText, color = Mist400)
            }
            Spacer(modifier = Modifier.height(6.dp))
            DayTicks(days = alarm.daysOfWeek, tickColor = if (alarm.isEnabled) Amber500 else Mist600)
        }
        Switch(
            checked = alarm.isEnabled,
            onCheckedChange = onSetEnabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Amber500,
                checkedTrackColor = Amber500.copy(alpha = 0.18f),
                checkedBorderColor = Amber500,
                uncheckedThumbColor = Mist400,
                uncheckedTrackColor = Ink800,
                uncheckedBorderColor = Ink700
            )
        )
    }
}

@Composable
private fun DayTicks(days: Set<DayOfWeek>, tickColor: Color) {
    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        weekOrder.forEach { day ->
            Box(
                modifier = Modifier
                    .width(14.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (day in days) tickColor else Ink700)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlarmEditScreen(
    alarm: Alarm?,
    onBack: () -> Unit,
    onSave: (Alarm) -> Unit,
    onDelete: (Alarm) -> Unit
) {
    val originalHour = remember { alarm?.hour ?: LocalTime.now().hour }
    val originalMinute = remember { alarm?.minute ?: LocalTime.now().minute }
    val originalLabel = remember { alarm?.label ?: "" }
    val originalDaysOfWeek = remember { alarm?.daysOfWeek ?: emptySet() }
    val originalDate = remember { alarm?.date ?: LocalDate.now() }

    var hour by remember { mutableStateOf(originalHour) }
    var minute by remember { mutableStateOf(originalMinute) }
    var label by remember { mutableStateOf(originalLabel) }
    var daysOfWeek by remember { mutableStateOf(originalDaysOfWeek) }
    var date by remember { mutableStateOf(originalDate) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showDiscardConfirm by remember { mutableStateOf(false) }

    fun buildAlarm() = Alarm(
        id = alarm?.id ?: 0L,
        hour = hour,
        minute = minute,
        isEnabled = alarm?.isEnabled ?: true,
        label = label,
        daysOfWeek = daysOfWeek,
        date = date
    )

    val hasChanges = hour != originalHour || minute != originalMinute || label != originalLabel ||
        daysOfWeek != originalDaysOfWeek || date != originalDate

    val requestExit: () -> Unit = {
        if (hasChanges) showDiscardConfirm = true else onBack()
    }

    BackHandler(onBack = requestExit)

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Close",
                    tint = Mist400,
                    modifier = Modifier.size(20.dp).clip(CircleShape).clickable(onClick = requestExit)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (alarm == null) "New alarm" else "Edit alarm",
                    style = TuLogsType.captionText.copy(color = Paper50)
                )
            }
            Icon(
                imageVector = Icons.Outlined.Check,
                contentDescription = "Save",
                tint = Amber500,
                modifier = Modifier.size(22.dp).clip(CircleShape).clickable { onSave(buildAlarm()) }
            )
        }

        HourMinuteWheelRow(
            hour = hour,
            minute = minute,
            onHourChange = { hour = it },
            onMinuteChange = { minute = it }
        )

        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = repeatSummaryLabel(daysOfWeek, date), style = TuLogsType.monoLabel)
                Icon(
                    imageVector = Icons.Outlined.CalendarToday,
                    contentDescription = "Pick date",
                    tint = Mist400,
                    modifier = Modifier.size(20.dp).clip(CircleShape).clickable { showDatePicker = true }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                weekOrder.forEach { day ->
                    DayCircle(
                        day = day,
                        selected = day in daysOfWeek,
                        onClick = {
                            daysOfWeek = if (day in daysOfWeek) daysOfWeek - day else daysOfWeek + day
                        }
                    )
                }
            }
        }

        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 24.dp)) {
            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                placeholder = { Text("e.g. Work") },
                label = { Text("Label") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        if (alarm != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Ink800)
                    .clickable { onDelete(alarm) }
                    .padding(vertical = 15.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Delete alarm",
                    style = TuLogsType.captionText.copy(color = Error500, fontWeight = FontWeight.Medium)
                )
            }
        }
    }

    if (showDiscardConfirm) {
        AlertDialog(
            onDismissRequest = { showDiscardConfirm = false },
            title = { Text("Discard changes?") },
            text = { Text("You have unsaved changes to this alarm.") },
            confirmButton = {
                TextButton(onClick = {
                    showDiscardConfirm = false
                    onSave(buildAlarm())
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDiscardConfirm = false
                    onBack()
                }) { Text("Discard") }
            }
        )
    }

    if (showDatePicker) {
        val todayEpochMillis = remember { LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli() }
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis >= todayEpochMillis
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        date = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

private fun repeatSummaryLabel(daysOfWeek: Set<DayOfWeek>, date: LocalDate): String {
    if (daysOfWeek.isEmpty()) {
        return if (date == LocalDate.now()) "Only once" else "Only once · ${date.format(dateSummaryFormatter)}"
    }
    return when (daysOfWeek) {
        weekdaySet -> "Weekdays"
        weekendSet -> "Weekends"
        DayOfWeek.entries.toSet() -> "Every day"
        else -> daysOfWeek.sorted().joinToString(", ") { it.name.take(3).lowercase().replaceFirstChar(Char::uppercase) }
    }
}

@Composable
private fun HourMinuteWheelRow(
    hour: Int,
    minute: Int,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        // Single highlight spanning both wheels, drawn once so it reads as
        // one connected pill instead of two separate per-wheel highlights.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(wheelItemHeight)
                .clip(RoundedCornerShape(12.dp))
                .background(Ink800)
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            WheelPicker(
                values = (0..23).toList(),
                selectedValue = hour,
                onValueChange = onHourChange,
                label = { it.toString().padStart(2, '0') },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            WheelPicker(
                values = (0..59).toList(),
                selectedValue = minute,
                onValueChange = onMinuteChange,
                label = { it.toString().padStart(2, '0') },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Scrolls through [values] endlessly in both directions by laying out many
 * repeats of the range and mapping each virtual index back with modulo —
 * LazyColumn only composes the visible window, so the huge item count costs
 * nothing at runtime.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WheelPicker(
    values: List<Int>,
    selectedValue: Int,
    onValueChange: (Int) -> Unit,
    label: (Int) -> String,
    modifier: Modifier = Modifier
) {
    val virtualCount = values.size * wheelRepeatCount
    val selectedIndex = values.indexOf(selectedValue).coerceAtLeast(0)
    val initialVirtualIndex = remember { (wheelRepeatCount / 2) * values.size + selectedIndex }
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = (initialVirtualIndex - wheelVisibleCount / 2).coerceAtLeast(0)
    )
    val flingBehavior = rememberSnapFlingBehavior(listState)
    val density = LocalDensity.current
    val itemHeightPx = with(density) { wheelItemHeight.toPx() }

    // Derived directly from firstVisibleItemIndex + pixel scroll offset —
    // since every item is the same fixed height, this pins down exactly
    // which item's band contains the viewport's center, with no dependency
    // on contentPadding-adjusted viewport offsets (which don't line up
    // cleanly and caused the picker to land a couple items off-center).
    val centeredIndex by remember {
        derivedStateOf {
            val fraction = listState.firstVisibleItemScrollOffset / itemHeightPx
            listState.firstVisibleItemIndex + floor(fraction + wheelVisibleCount / 2.0).toInt()
        }
    }

    LaunchedEffect(centeredIndex) {
        val value = values[centeredIndex % values.size]
        if (value != selectedValue) onValueChange(value)
    }

    Box(
        modifier = modifier.height(wheelItemHeight * wheelVisibleCount),
        contentAlignment = Alignment.Center
    ) {
        // No contentPadding needed: the virtual list is millions of items
        // deep, so we never scroll near a real boundary, and skipping it
        // avoids an edge-padding offset that was pushing the rendered
        // rows out of alignment with the fixed highlight bar.
        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            modifier = Modifier.fillMaxWidth()
        ) {
            items(count = virtualCount) { virtualIndex ->
                val value = values[virtualIndex % values.size]
                val isSelected = virtualIndex == centeredIndex
                Box(
                    modifier = Modifier.fillMaxWidth().height(wheelItemHeight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label(value),
                        style = TuLogsType.factValue.copy(
                            fontSize = if (isSelected) 28.sp else 20.sp,
                            color = if (isSelected) Amber500 else Mist600,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun DayCircle(day: DayOfWeek, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(if (selected) Amber500 else Color.Transparent)
            .border(width = 1.5.dp, color = if (selected) Amber500 else Ink700, shape = CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.name.take(1),
            style = TuLogsType.captionText.copy(
                color = if (selected) Ink950 else Mist400,
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}

@Composable
private fun IconCircle(icon: ImageVector, contentDescription: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(Amber500.copy(alpha = 0.18f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = contentDescription, tint = Amber500, modifier = Modifier.size(16.dp))
    }
}
