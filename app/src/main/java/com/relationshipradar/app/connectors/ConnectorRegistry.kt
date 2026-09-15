package com.relationshipradar.app.connectors

import android.content.Context
import com.relationshipradar.app.data.db.ConnectorCursor
import com.relationshipradar.app.data.repo.Repository

/** Runs every pull-style connector and advances its cursor. Push connectors (notifications) write directly. */
class ConnectorRegistry(context: Context, private val repo: Repository) {
    val connectors: List<Connector> = listOf(
        CallLogConnector(context, repo),
        SmsConnector(context, repo),
    )

    data class RunResult(val connectorId: String, val imported: Int, val skipped: String? = null)

    /** First scan looks back [initialLookbackDays]; later scans continue from the cursor. */
    suspend fun runAll(initialLookbackDays: Long = 365): List<RunResult> {
        val now = System.currentTimeMillis()
        return connectors.map { c ->
            val cursor = repo.cursor(c.id)
            if (cursor?.enabled == false) return@map RunResult(c.id, 0, "off")
            if (!c.isAvailable()) return@map RunResult(c.id, 0, "no permission")
            val since = cursor?.lastScannedAt?.takeIf { it > 0 } ?: (now - initialLookbackDays * 86_400_000L)
            val found = c.scan(since)
            repo.recordAll(found)
            // Cursor = newest seen row, not "now", so a row that lands late isn't skipped.
            val newest = found.maxOfOrNull { it.timestamp } ?: since
            repo.saveCursor(ConnectorCursor(c.id, newest, now, found.size, enabled = true))
            RunResult(c.id, found.size)
        }
    }
}
