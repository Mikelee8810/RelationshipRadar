package com.relationshipradar.app.connectors

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Telephony
import androidx.core.content.ContextCompat
import com.relationshipradar.app.data.db.IdentifierType
import com.relationshipradar.app.data.db.Interaction
import com.relationshipradar.app.data.repo.Repository

/** Reads SMS metadata only — the BODY column is never requested. */
class SmsConnector(private val context: Context, private val repo: Repository) : Connector {
    override val id = "sms"
    override val displayName = "Text messages (SMS)"

    override fun isAvailable() =
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED

    override suspend fun scan(sinceMillis: Long): List<Interaction> {
        if (!isAvailable()) return emptyList()
        val out = mutableListOf<Interaction>()
        context.contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            arrayOf(Telephony.Sms._ID, Telephony.Sms.ADDRESS, Telephony.Sms.TYPE, Telephony.Sms.DATE),
            "${Telephony.Sms.DATE} > ?",
            arrayOf(sinceMillis.toString()),
            "${Telephony.Sms.DATE} ASC",
        )?.use { c ->
            while (c.moveToNext()) {
                val address = c.getString(1) ?: continue
                val person = repo.findPersonByIdentifier(IdentifierType.PHONE, address)
                if (person == null) {
                    if (c.getInt(2) != Mapping.SMS_INBOX) { // only note numbers the *user* texted
                        repo.notePendingIdentity(IdentifierType.PHONE, address, id, null, c.getLong(3))
                    }
                    continue
                }
                Mapping.sms(person.id, c.getLong(0), c.getInt(2), c.getLong(3))?.let(out::add)
            }
        }
        return out
    }
}
