package com.relationshipradar.app.connectors

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import com.relationshipradar.app.data.db.Direction
import com.relationshipradar.app.data.db.IdentifierType
import com.relationshipradar.app.data.db.Interaction
import com.relationshipradar.app.data.db.InteractionSource
import com.relationshipradar.app.data.db.InteractionType
import com.relationshipradar.app.data.repo.Repository

/**
 * Past calendar events you attended with known people count as in-person contact.
 * Reads: event id, start/end, your attendance status, attendee emails. Never titles or notes.
 */
class CalendarConnector(private val context: Context, private val repo: Repository) : Connector {
    override val id = "calendar"
    override val displayName = "Calendar"

    override fun isAvailable() =
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED

    override suspend fun scan(sinceMillis: Long): List<Interaction> {
        if (!isAvailable()) return emptyList()
        val now = System.currentTimeMillis()
        val out = mutableListOf<Interaction>()

        val uri = CalendarContract.Instances.CONTENT_URI.buildUpon().let {
            ContentUris.appendId(it, sinceMillis); ContentUris.appendId(it, now); it.build()
        }
        val events = mutableListOf<Triple<Long, Long, Long>>() // eventId, begin, end
        context.contentResolver.query(
            uri,
            arrayOf(CalendarContract.Instances.EVENT_ID, CalendarContract.Instances.BEGIN, CalendarContract.Instances.END, CalendarContract.Instances.SELF_ATTENDEE_STATUS),
            "${CalendarContract.Instances.END} <= ? AND ${CalendarContract.Instances.END} > ?",
            arrayOf(now.toString(), sinceMillis.toString()),
            null,
        )?.use { c ->
            while (c.moveToNext()) {
                val selfStatus = c.getInt(3)
                if (selfStatus == CalendarContract.Attendees.ATTENDEE_STATUS_DECLINED) continue
                events += Triple(c.getLong(0), c.getLong(1), c.getLong(2))
            }
        }

        for ((eventId, begin, end) in events) {
            val credited = mutableSetOf<Long>()
            context.contentResolver.query(
                CalendarContract.Attendees.CONTENT_URI,
                arrayOf(CalendarContract.Attendees.ATTENDEE_EMAIL, CalendarContract.Attendees.ATTENDEE_STATUS),
                "${CalendarContract.Attendees.EVENT_ID} = ?",
                arrayOf(eventId.toString()),
                null,
            )?.use { a ->
                while (a.moveToNext()) {
                    val email = a.getString(0)?.takeIf { it.isNotBlank() } ?: continue
                    if (a.getInt(1) == CalendarContract.Attendees.ATTENDEE_STATUS_DECLINED) continue
                    val person = repo.findPersonByIdentifier(IdentifierType.EMAIL, email) ?: continue
                    if (!credited.add(person.id)) continue
                    out += Interaction(
                        personId = person.id,
                        source = InteractionSource.CALENDAR,
                        type = InteractionType.IN_PERSON,
                        timestamp = end.coerceAtLeast(begin),
                        direction = Direction.OUTGOING,
                        countsTowardTimer = true,
                        note = "calendar",
                        externalId = "cal:$eventId:$begin:${person.id}",
                    )
                }
            }
        }
        return out
    }
}
