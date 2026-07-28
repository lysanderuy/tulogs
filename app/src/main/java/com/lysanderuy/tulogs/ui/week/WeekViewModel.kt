package com.lysanderuy.tulogs.ui.week

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lysanderuy.tulogs.data.SleepLogRepository
import com.lysanderuy.tulogs.data.local.SleepLog
import com.lysanderuy.tulogs.util.SleepTimeFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

private const val windowDays = 7L

// Fixed reference so bar heights are comparable across weeks instead of rescaling per load
private const val maxReferenceDurationMillis = 12L * 60 * 60 * 1000

private val rangeDateFormatter = DateTimeFormatter.ofPattern("d MMM")

data class WeekUiState(
    val hasData: Boolean = false,
    val dateRangeLabel: String? = null,
    val averageDuration: String? = null,
    val averageSubLabel: String? = null,
    val nights: List<NightUiState> = emptyList()
)

data class NightUiState(
    val dateLabel: String,      // "TUE 14 JUL"
    val weekdayLetter: String,  // "T"
    val bedtime: String?,       // "23:02"
    val wake: String?,          // "06:24" — null while in progress or no log
    val duration: String?,      // "7h 22m" — null unless the session is complete
    val hasDuration: Boolean,
    val barHeightFraction: Float
)

@HiltViewModel
class WeekViewModel @Inject constructor(
    sleepLogRepository: SleepLogRepository
) : ViewModel() {

    val uiState: StateFlow<WeekUiState> = sleepLogRepository.recentLogs(windowDays)
        .map(::buildUiState)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = WeekUiState()
        )

    private fun buildUiState(logs: List<SleepLog>): WeekUiState {
        val today = LocalDate.now(ZoneId.systemDefault())
        val dates = (0 until windowDays).map { offset -> today.minusDays(windowDays - 1 - offset) }

        val latestLogByDate = logs
            .groupBy { log -> Instant.ofEpochMilli(log.bedtimeTimestamp).atZone(ZoneId.systemDefault()).toLocalDate() }
            .mapValues { (_, logsForDate) -> pickPrimarySession(logsForDate) }

        val nights = dates.map { date -> buildNightUiState(date, latestLogByDate[date]) }

        val completedDurationsMillis = dates.mapNotNull { date ->
            latestLogByDate[date]?.let { log -> log.wakeTimestamp?.let { wake -> wake - log.bedtimeTimestamp } }
        }

        return WeekUiState(
            hasData = logs.isNotEmpty(),
            dateRangeLabel = if (logs.isEmpty()) null else buildDateRangeLabel(dates),
            averageDuration = averageDurationLabel(completedDurationsMillis),
            averageSubLabel = averageSubLabel(completedDurationsMillis.size),
            nights = nights
        )
    }

    private fun buildNightUiState(date: LocalDate, log: SleepLog?): NightUiState {
        val bedtime = log?.let { SleepTimeFormat.formatClockTime(it.bedtimeTimestamp) }
        val wake = log?.wakeTimestamp?.let { SleepTimeFormat.formatClockTime(it) }
        val durationMillis = log?.wakeTimestamp?.let { it - log.bedtimeTimestamp }
        val duration = durationMillis?.let { SleepTimeFormat.formatDuration(it) }
        val barHeightFraction = durationMillis
            ?.let { (it.toFloat() / maxReferenceDurationMillis).coerceIn(0f, 1f) }
            ?: 0f

        return NightUiState(
            dateLabel = SleepTimeFormat.formatDateLabel(date),
            weekdayLetter = date.dayOfWeek.name.take(1),
            bedtime = bedtime,
            wake = wake,
            duration = duration,
            hasDuration = durationMillis != null,
            barHeightFraction = barHeightFraction
        )
    }

    private fun buildDateRangeLabel(dates: List<LocalDate>): String {
        val start = dates.first().format(rangeDateFormatter).uppercase()
        val end = dates.last().format(rangeDateFormatter).uppercase()
        return "$start – $end"
    }

    private fun averageDurationLabel(completedDurationsMillis: List<Long>): String? {
        if (completedDurationsMillis.isEmpty()) return null
        return SleepTimeFormat.formatDuration(completedDurationsMillis.sum() / completedDurationsMillis.size)
    }

    private fun averageSubLabel(completedNightCount: Int): String? {
        return when {
            completedNightCount == 0 -> null
            completedNightCount == windowDays.toInt() -> "avg this week"
            else -> "avg over $completedNightCount night${if (completedNightCount == 1) "" else "s"}"
        }
    }

    // Same-day sessions collapse to one row: longest wins, in-progress beats completed, ties break on earlier bedtime
    private fun pickPrimarySession(logsForDate: List<SleepLog>): SleepLog {
        return logsForDate.reduce { best, candidate ->
            val bestDuration = best.wakeTimestamp?.let { it - best.bedtimeTimestamp }
            val candidateDuration = candidate.wakeTimestamp?.let { it - candidate.bedtimeTimestamp }
            when {
                bestDuration == null && candidateDuration == null ->
                    if (candidate.bedtimeTimestamp < best.bedtimeTimestamp) candidate else best
                bestDuration == null -> best
                candidateDuration == null -> candidate
                candidateDuration > bestDuration -> candidate
                candidateDuration < bestDuration -> best
                else -> if (candidate.bedtimeTimestamp < best.bedtimeTimestamp) candidate else best
            }
        }
    }
}
