package com.relationshipradar.app.engine

import com.relationshipradar.app.data.db.Category
import com.relationshipradar.app.data.db.Person
import com.relationshipradar.app.data.db.ReminderBehavior
import java.util.concurrent.TimeUnit

/** Visual escalation levels. */
enum class RadarStatus { TRACK_ONLY, PAUSED, SNOOZED, GOOD, DUE_SOON, OVERDUE, VERY_OVERDUE }

data class PersonRadar(
    val person: Person,
    val category: Category?,
    val lastEffortAt: Long?,
    val intervalDays: Int?,
    val dueAt: Long?,
    val status: RadarStatus,
    val behavior: ReminderBehavior,
) {
    val daysSinceEffort: Long? get() = lastEffortAt?.let { TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - it) }
    val daysOverdue: Long? get() = dueAt?.let { TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - it) }
}

/**
 * Pure rules. No Android imports so it stays unit-testable.
 *
 * - Interval = person override, else category default. No interval = track only.
 * - Never contacted = due immediately (the clock starts when the person is created).
 * - Snooze/pause only affect notifications; the status colour still shows overdue in-app
 *   but the engine reports SNOOZED/PAUSED so lists can group them.
 */
object ReminderEngine {
    private val DAY = TimeUnit.DAYS.toMillis(1)
    const val DUE_SOON_WINDOW_DAYS = 2L
    const val VERY_OVERDUE_AFTER_DAYS = 7L

    /** Backup schedule: day 0, 2, 4, 6 then stop. */
    val BACKUP_OFFSETS_DAYS = listOf(0L, 2L, 4L, 6L)

    fun evaluate(person: Person, category: Category?, lastEffortAt: Long?, now: Long = System.currentTimeMillis()): PersonRadar {
        val interval = person.reminderIntervalDays ?: category?.defaultIntervalDays
        val behavior = person.reminderBehavior ?: category?.reminderBehavior ?: ReminderBehavior.ROUNDUP
        val base = lastEffortAt ?: person.createdAt
        val dueAt = interval?.let { base + it * DAY }

        val status = when {
            !person.remindersEnabled || interval == null || dueAt == null -> RadarStatus.TRACK_ONLY
            person.pausedUntil != null && person.pausedUntil > now -> RadarStatus.PAUSED
            person.snoozedUntil != null && person.snoozedUntil > now -> RadarStatus.SNOOZED
            else -> escalation(dueAt, now)
        }
        return PersonRadar(person, category, lastEffortAt, interval, dueAt, status, behavior)
    }

    fun escalation(dueAt: Long, now: Long): RadarStatus {
        val remainingDays = (dueAt - now) / DAY
        val overdueDays = (now - dueAt) / DAY
        return when {
            now < dueAt && remainingDays > DUE_SOON_WINDOW_DAYS -> RadarStatus.GOOD
            now < dueAt -> RadarStatus.DUE_SOON
            overdueDays >= VERY_OVERDUE_AFTER_DAYS -> RadarStatus.VERY_OVERDUE
            else -> RadarStatus.OVERDUE
        }
    }

    /**
     * Should we push a notification right now for this cycle?
     * [notificationsSent] is how many we've already sent for this dueAt.
     */
    fun shouldNotify(dueAt: Long, notificationsSent: Int, now: Long): Boolean {
        if (now < dueAt) return false
        if (notificationsSent >= BACKUP_OFFSETS_DAYS.size) return false
        val nextOffset = BACKUP_OFFSETS_DAYS[notificationsSent]
        return now >= dueAt + nextOffset * DAY
    }

    val RadarStatus.needsAttention: Boolean
        get() = this == RadarStatus.DUE_SOON || this == RadarStatus.OVERDUE || this == RadarStatus.VERY_OVERDUE
}
