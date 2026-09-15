package com.relationshipradar.app.engine

import com.relationshipradar.app.data.db.Category
import com.relationshipradar.app.data.db.Person
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class ReminderEngineTest {
    private val day = TimeUnit.DAYS.toMillis(1)
    private val now = 1_000L * day
    private val friend = Category(id = 1, name = "Friend", defaultIntervalDays = 30)
    private fun person(remind: Boolean = true, interval: Int? = null, snooze: Long? = null, pause: Long? = null) =
        Person(id = 1, displayName = "A", categoryId = 1, remindersEnabled = remind, reminderIntervalDays = interval, snoozedUntil = snooze, pausedUntil = pause, createdAt = now - 100 * day)

    @Test fun `reminders off means track only`() =
        assertEquals(RadarStatus.TRACK_ONLY, ReminderEngine.evaluate(person(remind = false), friend, now - day, now).status)

    @Test fun `no interval anywhere means track only`() =
        assertEquals(RadarStatus.TRACK_ONLY, ReminderEngine.evaluate(person(), friend.copy(defaultIntervalDays = null), now - day, now).status)

    @Test fun `recent effort is good`() =
        assertEquals(RadarStatus.GOOD, ReminderEngine.evaluate(person(), friend, now - 5 * day, now).status)

    @Test fun `within two days of due is due soon`() =
        assertEquals(RadarStatus.DUE_SOON, ReminderEngine.evaluate(person(), friend, now - 29 * day, now).status)

    @Test fun `past due is overdue`() =
        assertEquals(RadarStatus.OVERDUE, ReminderEngine.evaluate(person(), friend, now - 33 * day, now).status)

    @Test fun `a week past due is very overdue`() =
        assertEquals(RadarStatus.VERY_OVERDUE, ReminderEngine.evaluate(person(), friend, now - 40 * day, now).status)

    @Test fun `person override beats category default`() =
        assertEquals(RadarStatus.OVERDUE, ReminderEngine.evaluate(person(interval = 3), friend, now - 4 * day, now).status)

    @Test fun `never contacted counts from creation`() =
        assertEquals(RadarStatus.VERY_OVERDUE, ReminderEngine.evaluate(person(), friend, null, now).status)

    @Test fun `snooze hides but keeps clock`() {
        val r = ReminderEngine.evaluate(person(snooze = now + day), friend, now - 40 * day, now)
        assertEquals(RadarStatus.SNOOZED, r.status)
        assertEquals(now - 10 * day, r.dueAt)
    }

    @Test fun `pause wins over snooze`() =
        assertEquals(RadarStatus.PAUSED, ReminderEngine.evaluate(person(snooze = now + day, pause = Long.MAX_VALUE), friend, now - 40 * day, now).status)

    @Test fun `backup schedule fires on day 0 2 4 6 then stops`() {
        val due = now
        assertTrue(ReminderEngine.shouldNotify(due, 0, due))
        assertFalse(ReminderEngine.shouldNotify(due, 1, due + day))
        assertTrue(ReminderEngine.shouldNotify(due, 1, due + 2 * day))
        assertTrue(ReminderEngine.shouldNotify(due, 2, due + 4 * day))
        assertTrue(ReminderEngine.shouldNotify(due, 3, due + 6 * day))
        assertFalse(ReminderEngine.shouldNotify(due, 4, due + 30 * day))
    }

    @Test fun `not due yet never notifies`() = assertFalse(ReminderEngine.shouldNotify(now + day, 0, now))
}
