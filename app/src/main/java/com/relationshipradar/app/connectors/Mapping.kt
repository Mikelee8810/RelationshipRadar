package com.relationshipradar.app.connectors

import com.relationshipradar.app.data.db.Direction
import com.relationshipradar.app.data.db.Interaction
import com.relationshipradar.app.data.db.InteractionSource
import com.relationshipradar.app.data.db.InteractionType

/**
 * Pure mapping rules from raw platform rows to our interaction model.
 * Kept free of Android classes so they're unit-testable.
 *
 * Privacy: none of these functions accept or keep message bodies.
 */
object Mapping {
    // android.provider.CallLog.Calls.TYPE values
    const val CALL_INCOMING = 1
    const val CALL_OUTGOING = 2
    const val CALL_MISSED = 3
    const val CALL_VOICEMAIL = 4
    const val CALL_REJECTED = 5
    const val CALL_BLOCKED = 6

    // android.provider.Telephony.Sms.TYPE values
    const val SMS_INBOX = 1
    const val SMS_SENT = 2
    const val SMS_DRAFT = 3
    const val SMS_OUTBOX = 4
    const val SMS_FAILED = 5
    const val SMS_QUEUED = 6

    /**
     * Outgoing calls always count (even unanswered). Incoming counts only if answered
     * (duration > 0). Missed / rejected / voicemail are kept for history but don't count.
     */
    fun call(personId: Long, callId: Long, type: Int, durationSec: Long, timestamp: Long): Interaction? {
        val (direction, counts) = when (type) {
            CALL_OUTGOING -> Direction.OUTGOING to true
            CALL_INCOMING -> if (durationSec > 0) Direction.INCOMING_ANSWERED to true else Direction.INCOMING_IGNORED to false
            CALL_MISSED, CALL_REJECTED, CALL_VOICEMAIL -> Direction.INCOMING_IGNORED to false
            else -> return null // blocked / unknown
        }
        return Interaction(
            personId = personId,
            source = InteractionSource.PHONE,
            type = InteractionType.CALL,
            timestamp = timestamp,
            direction = direction,
            countsTowardTimer = counts,
            externalId = "call:$callId",
        )
    }

    /** Sent / outbox / queued count. Inbox is history only. Drafts and failures are skipped. */
    fun sms(personId: Long, smsId: Long, type: Int, timestamp: Long): Interaction? {
        val (direction, counts) = when (type) {
            SMS_SENT, SMS_OUTBOX, SMS_QUEUED -> Direction.OUTGOING to true
            SMS_INBOX -> Direction.INCOMING_IGNORED to false
            else -> return null
        }
        return Interaction(
            personId = personId,
            source = InteractionSource.SMS,
            type = InteractionType.MESSAGE,
            timestamp = timestamp,
            direction = direction,
            countsTowardTimer = counts,
            externalId = "sms:$smsId",
        )
    }

    /** One message inside a messaging-style notification, already stripped of its text. */
    data class NotifMessage(val senderName: String?, val isFromUser: Boolean, val timestamp: Long)

    data class NotifCredit(val senderName: String, val timestamp: Long, val outgoing: Boolean)

    /**
     * Decide who gets credit from one notification.
     *
     * - Direct chat: user-sent messages credit the chat partner (the conversation title, or
     *   the single other sender). Partner-sent messages are recorded as non-counting history.
     * - Group chat: user-sent messages credit every *other* sender visible in the notification
     *   (we can't see the full member list from a notification). Nothing else is recorded.
     */
    fun notificationCredits(
        conversationTitle: String?,
        isGroup: Boolean,
        messages: List<NotifMessage>,
        sinceTimestamp: Long,
    ): List<NotifCredit> {
        val fresh = messages.filter { it.timestamp > sinceTimestamp }
        if (fresh.isEmpty()) return emptyList()
        val others = messages.mapNotNull { it.senderName?.takeIf { n -> n.isNotBlank() && !it.isFromUser } }.distinct()

        return if (!isGroup) {
            val partner = conversationTitle?.takeIf { it.isNotBlank() } ?: others.singleOrNull() ?: return emptyList()
            fresh.map { m -> NotifCredit(partner, m.timestamp, outgoing = m.isFromUser) }
        } else {
            val userSent = fresh.filter { it.isFromUser }
            if (userSent.isEmpty()) return emptyList()
            val latest = userSent.maxOf { it.timestamp }
            others.map { NotifCredit(it, latest, outgoing = true) }
        }
    }

    fun notificationInteraction(personId: Long, pkg: String, credit: NotifCredit, isGroup: Boolean): Interaction =
        Interaction(
            personId = personId,
            source = InteractionSource.NOTIFICATION,
            type = if (isGroup) InteractionType.GROUP_MESSAGE else InteractionType.MESSAGE,
            timestamp = credit.timestamp,
            direction = if (credit.outgoing) Direction.OUTGOING else Direction.INCOMING_IGNORED,
            countsTowardTimer = credit.outgoing,
            note = pkg.substringAfterLast('.'),
            externalId = "notif:$pkg:$personId:${credit.timestamp}:${if (credit.outgoing) "out" else "in"}",
        )
}
