package com.relationshipradar.app.connectors

import com.relationshipradar.app.data.db.Interaction

/**
 * A source of interactions (phone, SMS, notification listener, ...).
 * Connectors resolve people themselves via the repository's identifier lookup and hand back
 * ready-to-store [Interaction]s. The engine never cares where an interaction came from.
 */
interface Connector {
    val id: String
    val displayName: String
    /** Whether the runtime permission / access this connector needs is currently granted. */
    fun isAvailable(): Boolean
    /** Pull everything since [sinceMillis]. Must be idempotent (stable externalIds). */
    suspend fun scan(sinceMillis: Long): List<Interaction>
}
