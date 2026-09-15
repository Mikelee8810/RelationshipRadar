package com.relationshipradar.app.engine

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

enum class SnoozeOption(val label: String) {
    TOMORROW("Tomorrow"),
    LATER_THIS_WEEK("Later this week"),
    SUNDAY("Sunday"),
    NEXT_WEEK("Next week"),
    CUSTOM("Pick a date");

    /** Snoozes land at 9am local time. */
    fun resolve(today: LocalDate = LocalDate.now(), zone: ZoneId = ZoneId.systemDefault()): Long? {
        val date = when (this) {
            TOMORROW -> today.plusDays(1)
            LATER_THIS_WEEK -> {
                val fri = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY))
                if (fri == today) today.plusDays(1) else fri
            }
            SUNDAY -> today.with(TemporalAdjusters.next(DayOfWeek.SUNDAY))
            NEXT_WEEK -> today.with(TemporalAdjusters.next(DayOfWeek.MONDAY))
            CUSTOM -> return null
        }
        return date.atTime(LocalTime.of(9, 0)).atZone(zone).toInstant().toEpochMilli()
    }
}

enum class PauseOption(val label: String) {
    ONE_WEEK("1 week"),
    ONE_MONTH("1 month"),
    UNTIL_DATE("Until a date"),
    INDEFINITE("Indefinitely");

    fun resolve(today: LocalDate = LocalDate.now(), zone: ZoneId = ZoneId.systemDefault()): Long? = when (this) {
        ONE_WEEK -> today.plusWeeks(1).atStartOfDay(zone).toInstant().toEpochMilli()
        ONE_MONTH -> today.plusMonths(1).atStartOfDay(zone).toInstant().toEpochMilli()
        UNTIL_DATE -> null
        INDEFINITE -> Long.MAX_VALUE
    }
}
