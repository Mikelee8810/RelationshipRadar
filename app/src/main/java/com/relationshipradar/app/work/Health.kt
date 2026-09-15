package com.relationshipradar.app.work

import android.Manifest
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.PowerManager
import androidx.core.content.ContextCompat
import com.relationshipradar.app.connectors.RadarNotificationListener
import com.relationshipradar.app.shizuku.ShizukuBridge

/**
 * Measures (never guesses) whether the app can actually do its job in the background.
 * Each check has a normal state; only exceptions get flagged.
 */
object Health {
    enum class Level { OK, ATTENTION, OFF }

    data class Check(val title: String, val detail: String, val level: Level, val fixHint: String? = null)

    data class Report(val checks: List<Check>) {
        val worst: Level get() = checks.maxOf { it.level }
    }

    fun check(context: Context, lastReminderRunAt: Long, lastScanRunAt: Long, listenerEnabled: Boolean = RadarNotificationListener.isEnabled(context)): Report {
        val pm = context.getSystemService(PowerManager::class.java)
        val usm = context.getSystemService(UsageStatsManager::class.java)
        val now = System.currentTimeMillis()
        val day = 86_400_000L

        fun granted(p: String) = ContextCompat.checkSelfPermission(context, p) == PackageManager.PERMISSION_GRANTED

        val battery = pm.isIgnoringBatteryOptimizations(context.packageName)
        val bucket = usm.appStandbyBucket
        val bucketName = when {
            bucket <= UsageStatsManager.STANDBY_BUCKET_ACTIVE -> "active"
            bucket <= UsageStatsManager.STANDBY_BUCKET_WORKING_SET -> "working set"
            bucket <= UsageStatsManager.STANDBY_BUCKET_FREQUENT -> "frequent"
            bucket <= UsageStatsManager.STANDBY_BUCKET_RARE -> "rare"
            else -> "restricted"
        }

        val checks = listOf(
            Check(
                "Notifications", if (granted(Manifest.permission.POST_NOTIFICATIONS)) "Allowed" else "Blocked — reminders can't reach you",
                if (granted(Manifest.permission.POST_NOTIFICATIONS)) Level.OK else Level.OFF,
            ),
            Check(
                "Battery optimisation", if (battery) "Exempt — background work runs on time" else "Not exempt — Android may delay scans and reminders",
                if (battery) Level.OK else Level.ATTENTION, "Tap Fix, or use Shizuku hardening",
            ),
            Check(
                "Standby bucket", "Currently: $bucketName",
                if (bucket <= UsageStatsManager.STANDBY_BUCKET_WORKING_SET) Level.OK else Level.ATTENTION, "Open the app now and then, or use Shizuku hardening",
            ),
            Check(
                "Daily reminder job", if (lastReminderRunAt == 0L) "Hasn't run yet" else if (now - lastReminderRunAt > 2 * day) "Last ran ${(now - lastReminderRunAt) / day} days ago" else "Ran recently",
                when { lastReminderRunAt == 0L -> Level.ATTENTION; now - lastReminderRunAt > 2 * day -> Level.ATTENTION; else -> Level.OK },
            ),
            Check(
                "Background scan", if (lastScanRunAt == 0L) "Hasn't run yet" else if (now - lastScanRunAt > day) "Last ran ${(now - lastScanRunAt) / 3_600_000L} h ago" else "Ran recently",
                when { lastScanRunAt == 0L -> Level.ATTENTION; now - lastScanRunAt > day -> Level.ATTENTION; else -> Level.OK },
            ),
            Check("Chat app listener", if (listenerEnabled) "Connected" else "Off — chat replies aren't being counted", if (listenerEnabled) Level.OK else Level.OFF),
            Check("Call log access", if (granted(Manifest.permission.READ_CALL_LOG)) "Allowed" else "Not granted", if (granted(Manifest.permission.READ_CALL_LOG)) Level.OK else Level.OFF),
            Check("SMS access", if (granted(Manifest.permission.READ_SMS)) "Allowed" else "Not granted", if (granted(Manifest.permission.READ_SMS)) Level.OK else Level.OFF),
            Check(
                "Shizuku", when (ShizukuBridge.status()) {
                    ShizukuBridge.Status.READY -> "Connected"
                    ShizukuBridge.Status.PERMISSION_NEEDED -> "Running, needs permission"
                    ShizukuBridge.Status.NOT_RUNNING -> "Installed but not running"
                    ShizukuBridge.Status.NOT_INSTALLED -> "Not installed (optional booster)"
                },
                if (ShizukuBridge.status() == ShizukuBridge.Status.READY) Level.OK else Level.ATTENTION,
            ),
        )
        return Report(checks)
    }
}
