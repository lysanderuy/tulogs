package com.lysanderuy.tulogs.alarm

import com.lysanderuy.tulogs.data.local.Alarm
import java.time.ZonedDateTime

// Shared next-occurrence math, used by both AlarmScheduler and the UI so they never drift apart
object AlarmOccurrence {

    // Empty daysOfWeek = one-time alarm anchored to date; non-empty = weekly repeat
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

    // A repeating alarm is always upcoming; a one-time alarm stops being upcoming once its date+time passes
    fun isUpcoming(alarm: Alarm, now: ZonedDateTime = ZonedDateTime.now()): Boolean =
        nextTrigger(alarm, now).isAfter(now)

    // Matches stock alarm-clock behavior: a past one-time alarm rolls forward a day at a time until upcoming
    fun rollToUpcoming(alarm: Alarm, now: ZonedDateTime = ZonedDateTime.now()): Alarm {
        if (alarm.daysOfWeek.isNotEmpty()) return alarm
        var candidate = alarm
        while (!isUpcoming(candidate, now)) {
            candidate = candidate.copy(date = candidate.date.plusDays(1))
        }
        return candidate
    }
}
