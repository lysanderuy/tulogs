package com.lysanderuy.tulogs.alarm

import com.lysanderuy.tulogs.data.local.Alarm
import java.time.ZonedDateTime

/**
 * Shared next-occurrence math for an [Alarm]. Used by both AlarmScheduler
 * (to know when to fire next) and the UI (to know which saved alarm is
 * soonest) so the two never drift apart.
 */
object AlarmOccurrence {

    /**
     * Empty [Alarm.daysOfWeek] means a one-time alarm anchored to [Alarm.date].
     * A non-empty set means a weekly repeat — a match is guaranteed within 7 days.
     */
    fun nextTrigger(alarm: Alarm, now: ZonedDateTime = ZonedDateTime.now(), skipToday: Boolean = false): ZonedDateTime {
        if (alarm.daysOfWeek.isEmpty()) {
            return alarm.date.atTime(alarm.hour, alarm.minute).atZone(now.zone)
        }
        val today = now.toLocalDate()
        for (offset in 0..7) {
            if (offset == 0 && skipToday) continue
            val candidateDate = today.plusDays(offset.toLong())
            if (!alarm.daysOfWeek.contains(candidateDate.dayOfWeek)) continue
            val candidate = candidateDate.atTime(alarm.hour, alarm.minute).atZone(now.zone)
            if (candidate.isAfter(now)) return candidate
        }
        error("Alarm ${alarm.id} has no repeat days configured")
    }
}
