package com.lysanderuy.tulogs.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lysanderuy.tulogs.alarm.AlarmOccurrence
import com.lysanderuy.tulogs.data.AlarmRepository
import com.lysanderuy.tulogs.data.SleepLogRepository
import com.lysanderuy.tulogs.data.local.Alarm
import com.lysanderuy.tulogs.data.local.SleepLog
import com.lysanderuy.tulogs.util.SleepTimeFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class HomeViewModel @Inject constructor(
    sleepLogRepository: SleepLogRepository,
    alarmRepository: AlarmRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        sleepLogRepository.allLogs,
        alarmRepository.allAlarms.map { alarms -> alarms.filter { it.isEnabled } }
    ) { logs, alarms ->
        buildUiState(logs, alarms)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(
            dateLabel = SleepTimeFormat.formatDateLabel(LocalDate.now()),
            alarmTime = "Not set",
            alarmDays = "",
            isBedtimeLogged = false,
            bedtimeLoggedAt = null,
            lastNight = null
        )
    )

    private fun buildUiState(logs: List<SleepLog>, alarms: List<Alarm>): HomeUiState {
        val dateLabel = SleepTimeFormat.formatDateLabel(LocalDate.now())

        val activeSession = logs.firstOrNull { it.wakeTimestamp == null }
        val isBedtimeLogged = activeSession != null
        val bedtimeLoggedAt = activeSession?.let { SleepTimeFormat.formatClockTime(it.bedtimeTimestamp) }

        val lastCompletedSession = logs.firstOrNull { it.wakeTimestamp != null }
        val lastNight = lastCompletedSession?.let { session ->
            LastNightUiState(
                bedtime = SleepTimeFormat.formatClockTime(session.bedtimeTimestamp),
                wake = SleepTimeFormat.formatClockTime(session.wakeTimestamp!!),
                qualityRating = 0,
                screenOnAfterMinutes = screenOnAfterMinutes(session),
                duration = SleepTimeFormat.formatDuration(session.wakeTimestamp!! - session.bedtimeTimestamp)
            )
        }

        val earliestAlarm = alarms.minByOrNull { AlarmOccurrence.nextTrigger(it).toInstant().toEpochMilli() }
        val alarmTime = earliestAlarm?.let { SleepTimeFormat.formatClockTime(LocalTime.of(it.hour, it.minute)) } ?: "Not set"
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

    private fun screenOnAfterMinutes(session: SleepLog): Int? {
        val firstScreenOn = session.firstScreenOnTimestamp ?: return null
        if (firstScreenOn <= session.bedtimeTimestamp) return null
        return ((firstScreenOn - session.bedtimeTimestamp) / 60000).toInt()
    }
}
