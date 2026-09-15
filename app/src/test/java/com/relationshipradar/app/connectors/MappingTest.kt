package com.relationshipradar.app.connectors

import com.relationshipradar.app.connectors.Mapping.NotifMessage
import com.relationshipradar.app.data.db.Direction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MappingTest {
    // ---- calls ----
    @Test fun `outgoing call counts even if unanswered`() {
        val i = Mapping.call(1, 10, Mapping.CALL_OUTGOING, 0, 5)!!
        assertEquals(Direction.OUTGOING, i.direction); assertTrue(i.countsTowardTimer)
    }
    @Test fun `answered incoming counts`() = assertTrue(Mapping.call(1, 10, Mapping.CALL_INCOMING, 42, 5)!!.countsTowardTimer)
    @Test fun `incoming with zero duration does not count`() = assertFalse(Mapping.call(1, 10, Mapping.CALL_INCOMING, 0, 5)!!.countsTowardTimer)
    @Test fun `missed rejected voicemail are history only`() {
        for (t in listOf(Mapping.CALL_MISSED, Mapping.CALL_REJECTED, Mapping.CALL_VOICEMAIL)) {
            val i = Mapping.call(1, 10, t, 0, 5)!!
            assertEquals(Direction.INCOMING_IGNORED, i.direction); assertFalse(i.countsTowardTimer)
        }
    }
    @Test fun `blocked calls are dropped`() = assertNull(Mapping.call(1, 10, Mapping.CALL_BLOCKED, 0, 5))
    @Test fun `call externalId is stable`() = assertEquals("call:10", Mapping.call(1, 10, Mapping.CALL_OUTGOING, 0, 5)!!.externalId)

    // ---- sms ----
    @Test fun `sent outbox queued count`() {
        for (t in listOf(Mapping.SMS_SENT, Mapping.SMS_OUTBOX, Mapping.SMS_QUEUED)) assertTrue(Mapping.sms(1, 3, t, 5)!!.countsTowardTimer)
    }
    @Test fun `inbox is history only`() = assertFalse(Mapping.sms(1, 3, Mapping.SMS_INBOX, 5)!!.countsTowardTimer)
    @Test fun `drafts and failures dropped`() {
        assertNull(Mapping.sms(1, 3, Mapping.SMS_DRAFT, 5)); assertNull(Mapping.sms(1, 3, Mapping.SMS_FAILED, 5))
    }

    // ---- notifications ----
    private val incoming = NotifMessage("Sarah", false, 100)
    private val reply = NotifMessage(null, true, 200)

    @Test fun `direct chat reply credits the partner as outgoing`() {
        val c = Mapping.notificationCredits("Sarah", false, listOf(incoming, reply), 0)
        assertEquals(2, c.size)
        assertEquals(Mapping.NotifCredit("Sarah", 200, true), c[1])
        assertFalse(c[0].outgoing)
    }
    @Test fun `direct chat with no title uses the single other sender`() {
        val c = Mapping.notificationCredits(null, false, listOf(incoming, reply), 0)
        assertEquals("Sarah", c.single { it.outgoing }.senderName)
    }
    @Test fun `already-seen messages are not re-credited`() =
        assertTrue(Mapping.notificationCredits("Sarah", false, listOf(incoming, reply), 200).isEmpty())

    @Test fun `group chat credits every visible member when user posts`() {
        val msgs = listOf(NotifMessage("Ann", false, 10), NotifMessage("Bob", false, 20), NotifMessage(null, true, 30))
        val c = Mapping.notificationCredits("Pizza crew", true, msgs, 0)
        assertEquals(setOf("Ann", "Bob"), c.map { it.senderName }.toSet())
        assertTrue(c.all { it.outgoing && it.timestamp == 30L })
    }
    @Test fun `group chat with no user message credits nobody`() {
        val msgs = listOf(NotifMessage("Ann", false, 10), NotifMessage("Bob", false, 20))
        assertTrue(Mapping.notificationCredits("Pizza crew", true, msgs, 0).isEmpty())
    }
    @Test fun `notification interaction never carries text`() {
        val i = Mapping.notificationInteraction(7, "com.whatsapp", Mapping.NotifCredit("Sarah", 200, true), false)
        assertEquals("whatsapp", i.note); assertTrue(i.countsTowardTimer)
    }
}
