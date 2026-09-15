package com.relationshipradar.app.shizuku

import com.relationshipradar.app.BuildConfig

/**
 * The ONLY things the elevated process will ever execute. Callers pass an ID, never a string.
 * Every command touches only this app's own package. Nothing here reads other apps' data.
 */
object ShellCommands {
    private const val PKG = BuildConfig.APPLICATION_ID
    private const val LISTENER = "$PKG/com.relationshipradar.app.connectors.RadarNotificationListener"

    enum class Command(val id: Int, val label: String, val argv: List<String>) {
        /** Flip the notification-access switch for us, no Settings trip. */
        ALLOW_NOTIFICATION_LISTENER(1, "Enable notification access", listOf("cmd", "notification", "allow_listener", LISTENER)),
        /** Exempt from Doze so the 6-hourly scan and the 9am reminder actually fire. */
        BATTERY_WHITELIST(2, "Exempt from battery optimisation", listOf("dumpsys", "deviceidle", "whitelist", "+$PKG")),
        /** Keep us in the "active" standby bucket so WorkManager isn't deferred for hours. */
        STANDBY_ACTIVE(3, "Set standby bucket to active", listOf("am", "set-standby-bucket", PKG, "active")),
        /** Allow background execution op explicitly. */
        ALLOW_BACKGROUND(4, "Allow running in background", listOf("appops", "set", PKG, "RUN_ANY_IN_BACKGROUND", "allow")),
        /** Grant our declared runtime permissions without the dialogs. */
        GRANT_CALL_LOG(5, "Grant call log", listOf("pm", "grant", PKG, "android.permission.READ_CALL_LOG")),
        GRANT_SMS(6, "Grant SMS", listOf("pm", "grant", PKG, "android.permission.READ_SMS")),
        GRANT_CONTACTS(7, "Grant contacts", listOf("pm", "grant", PKG, "android.permission.READ_CONTACTS")),
        GRANT_NOTIFICATIONS(8, "Grant notifications", listOf("pm", "grant", PKG, "android.permission.POST_NOTIFICATIONS")),
        GRANT_CALENDAR(9, "Grant calendar", listOf("pm", "grant", PKG, "android.permission.READ_CALENDAR"));

        companion object { fun byId(id: Int) = entries.firstOrNull { it.id == id } }
    }

    enum class Query(val id: Int, val argv: List<String>) {
        STANDBY_BUCKET(1, listOf("am", "get-standby-bucket", PKG)),
        DEVICEIDLE_WHITELIST(2, listOf("dumpsys", "deviceidle", "whitelist")),
        LISTENER_STATUS(3, listOf("cmd", "notification", "get_approved_assistant"));

        companion object { fun byId(id: Int) = entries.firstOrNull { it.id == id } }
    }

    /** The full "make me reliable" bundle, in order. */
    val hardening = listOf(Command.BATTERY_WHITELIST, Command.STANDBY_ACTIVE, Command.ALLOW_BACKGROUND)
}
