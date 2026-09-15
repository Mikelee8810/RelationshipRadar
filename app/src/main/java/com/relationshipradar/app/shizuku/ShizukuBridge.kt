package com.relationshipradar.app.shizuku

import android.content.pm.PackageManager
import rikka.shizuku.Shizuku

/**
 * Phase 1 only reports status. Phase 3 will route elevated calls (call log without
 * dialer role, notification access setup, background whitelisting) through here.
 * Designed so a RootBridge could sit beside it later.
 */
object ShizukuBridge {
    const val REQUEST_CODE = 4242

    enum class Status { NOT_INSTALLED, NOT_RUNNING, PERMISSION_NEEDED, READY }

    fun status(): Status = try {
        if (!Shizuku.pingBinder()) Status.NOT_RUNNING
        else if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) Status.READY
        else Status.PERMISSION_NEEDED
    } catch (_: Throwable) {
        Status.NOT_INSTALLED
    }

    fun requestPermission() {
        if (status() == Status.PERMISSION_NEEDED && !Shizuku.shouldShowRequestPermissionRationale()) {
            Shizuku.requestPermission(REQUEST_CODE)
        }
    }
}
