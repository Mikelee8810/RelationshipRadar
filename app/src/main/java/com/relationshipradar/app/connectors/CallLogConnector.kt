package com.relationshipradar.app.connectors

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CallLog
import androidx.core.content.ContextCompat
import com.relationshipradar.app.data.db.IdentifierType
import com.relationshipradar.app.data.db.Interaction
import com.relationshipradar.app.data.repo.Repository

/** Reads the system call log. Stores number-owner, time, direction, and whether it counts. Never the number itself. */
class CallLogConnector(private val context: Context, private val repo: Repository) : Connector {
    override val id = "calls"
    override val displayName = "Phone calls"

    override fun isAvailable() =
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED

    override suspend fun scan(sinceMillis: Long): List<Interaction> {
        if (!isAvailable()) return emptyList()
        val out = mutableListOf<Interaction>()
        context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            arrayOf(CallLog.Calls._ID, CallLog.Calls.NUMBER, CallLog.Calls.TYPE, CallLog.Calls.DURATION, CallLog.Calls.DATE),
            "${CallLog.Calls.DATE} > ?",
            arrayOf(sinceMillis.toString()),
            "${CallLog.Calls.DATE} ASC",
        )?.use { c ->
            while (c.moveToNext()) {
                val number = c.getString(1) ?: continue
                val person = repo.findPersonByIdentifier(IdentifierType.PHONE, number)
                if (person == null) {
                    // Unknown number: don't create people from spam. Just remember we saw it.
                    repo.notePendingIdentity(IdentifierType.PHONE, number, id, null, c.getLong(4))
                    continue
                }
                Mapping.call(person.id, c.getLong(0), c.getInt(2), c.getLong(3), c.getLong(4))?.let(out::add)
            }
        }
        return out
    }
}
