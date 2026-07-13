package com.lysanderuy.tulogs.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lysanderuy.tulogs.alarm.AlarmOccurrence
import com.lysanderuy.tulogs.data.SleepLogRepository
import com.lysanderuy.tulogs.data.local.Alarm
import com.lysanderuy.tulogs.data.local.AlarmDao
import com.lysanderuy.tulogs.data.local.SleepLog
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

private val dateLabelFormatter = DateTimeFormatter.ofPattern("EEE d MMM")
private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

@HiltViewModel
class HomeViewModel @Inject constructor(
    sleepLogRepository: SleepLogRepository,
    alarmDao: AlarmDao
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        sleepLogRepository.allLogs,
        alarmDao.getEnabledAlarms()
    ) { logs, alarms ->
        buildUiState(logs, alarms)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(
            dateLabel = LocalDate.now().format(dateLabelFormatter).uppercase(),
            alarmTime = "Not set",
            alarmDays = "",
            isBedtimeLogged = false,
            bedtimeLoggedAt = null,
            lastNight = null
        )
    )

    private fun buildUiState(logs: List<SleepLog>, alarms: List<Alarm>): HomeUiState {
        val dateLabel = LocalDate.now().format(dateLabelFormatter).uppercase()

        val activeSession = logs.firstOrNull { it.wakeTimestamp == null }
        val isBedtimeLogged = activeSession != null
        val bedtimeLoggedAt = activeSession?.let { formatTimestamp(it.bedtimeTimestamp) }

        val lastCompletedSession = logs.firstOrNull { it.wakeTimestamp != null }
        val lastNight = lastCompletedSession?.let { session ->
            LastNightUiState(
                bedtime = formatTimestamp(session.bedtimeTimestamp),
                wake = formatTimestamp(session.wakeTimestamp!!),
                qualityRating = 0,
                screenOnAfterMinutes = screenOnAfterMinutes(session),
                duration = formatDuration(session.wakeTimestamp!! - session.bedtimeTimestamp)
            )
        }

        val earliestAlarm = alarms.minByOrNull { AlarmOccurrence.nextTrigger(it).toInstant().toEpochMilli() }
        val alarmTime = earliestAlarm?.let { LocalTime.of(it.hour, it.minute).format(timeFormatter) } ?: "Not set"
        val alarmDays = earliestAlarm?.let { it.label.ifBlank { formatDays(it.daysOfWeek) } } ?: ""

        return HomeUiState(
            dateLabel = dateLabel,
            alarmTime = alarmTime,
            alarmDays = alarmDays,
            isBedtimeLogged = isBedtimeLogged,
            bedtimeLoggedAt = bedtimeLoggedAt,
            lastNight = lastNight
        )
    }

    private fun formatDays(days: Set<DayOfWeek>): String {
        val weekdays = setOf(
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
        )
        val weekend = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
        return when (days) {
            emptySet<DayOfWeek>() -> "Once"
            weekdays -> "Weekdays"
            weekend -> "Weekends"
            DayOfWeek.entries.toSet() -> "Every day"
            else -> days.sorted().joinToString(", ") { it.name.take(3).lowercase().replaceFirstChar(Char::uppercase) }
        }
    }

    private fun formatDuration(durationMillis: Long): String {
        val totalMinutes = durationMillis / 60000
        return "${totalMinutes / 60}h ${totalMinutes % 60}m"
    }

    private fun screenOnAfterMinutes(session: SleepLog): Int? {
        val firstScreenOn = session.firstScreenOnTimestamp ?: return null
        if (firstScreenOn <= session.bedtimeTimestamp) return null
        return ((firstScreenOn - session.bedtimeTimestamp) / 60000).toInt()
    }

    private fun formatTimestamp(timestamp: Long): String {
        return Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .format(timeFormatter)
    }
}
