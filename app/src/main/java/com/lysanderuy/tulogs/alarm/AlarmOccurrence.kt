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

    /**
     * A repeating alarm is always upcoming — [nextTrigger] only ever returns a
     * future day. A one-time alarm is upcoming only while its anchored date+time
     * hasn't passed yet; once it has, there's no "next occurrence" to roll to.
     */
    fun isUpcoming(alarm: Alarm, now: ZonedDateTime = ZonedDateTime.now()): Boolean =
        nextTrigger(alarm, now).isAfter(now)

    /**
     * Matches stock alarm-clock behavior: turning on a one-time alarm whose
     * date+time has already passed rolls it forward a day at a time until
     * its next occurrence is actually upcoming, rather than refusing to enable
     * it. Repeating alarms are always upcoming already, so they're untouched.
     */
    fun rollToUpcoming(alarm: Alarm, now: ZonedDateTime = ZonedDateTime.now()): Alarm {
        if (alarm.daysOfWeek.isNotEmpty()) return alarm
        var candidate = alarm
        while (!isUpcoming(candidate, now)) {
            candidate = candidate.copy(date = candidate.date.plusDays(1))
        }
        return candidate
    }
}
