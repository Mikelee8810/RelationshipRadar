package com.relationshipradar.app.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.relationshipradar.app.RadarApp
import java.util.concurrent.TimeUnit

/** Pulls call log + SMS on a schedule. Cheap because each connector continues from its cursor. */
class ScanWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val app = RadarApp.from(applicationContext)
        app.connectors.runAll()
        app.settings.markScanRun(System.currentTimeMillis())
        com.relationshipradar.app.widget.RadarWidget.refresh(applicationContext)
        return Result.success()
    }
}

object ScanScheduler {
    private const val PERIODIC = "connector_scan"
    private const val NOW = "connector_scan_now"

    fun ensureScheduled(context: Context) {
        val request = PeriodicWorkRequestBuilder<ScanWorker>(6, TimeUnit.HOURS)
            .setConstraints(Constraints.Builder().setRequiresBatteryNotLow(true).build())
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(PERIODIC, ExistingPeriodicWorkPolicy.KEEP, request)
    }

    fun scanNow(context: Context) {
        WorkManager.getInstance(context).enqueueUniqueWork(NOW, ExistingWorkPolicy.REPLACE, OneTimeWorkRequestBuilder<ScanWorker>().build())
    }
}
