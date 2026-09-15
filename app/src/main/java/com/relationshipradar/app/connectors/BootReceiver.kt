package com.relationshipradar.app.connectors

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.relationshipradar.app.work.ReminderScheduler
import com.relationshipradar.app.work.ScanScheduler

/** Re-arms periodic work after a reboot. WorkManager usually survives, this is belt-and-braces. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            ReminderScheduler.ensureScheduled(context)
            ScanScheduler.ensureScheduled(context)
        }
    }
}
