package com.relationshipradar.app.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings as SysSettings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.relationshipradar.app.shizuku.ShellCommands
import com.relationshipradar.app.shizuku.ShizukuBridge
import com.relationshipradar.app.ui.HealthDot
import com.relationshipradar.app.ui.RadarViewModel
import com.relationshipradar.app.ui.SectionHeader
import com.relationshipradar.app.work.Health

/**
 * "Is the radar actually running?" — measured, not assumed. Normal = green dot, nothing to do.
 * Shizuku hardening is the one-tap fix; every command is allow-listed and touches only this app.
 */
@Composable
fun HealthScreen(vm: RadarViewModel) {
    val ctx = LocalContext.current
    var report by remember { mutableStateOf(vm.health()) }
    var shizuku by remember { mutableStateOf(ShizukuBridge.status(ctx)) }
    var log by remember { mutableStateOf<List<String>>(emptyList()) }
    var busy by remember { mutableStateOf(false) }

    // Re-measure whenever we come back from a Settings screen.
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner) {
        val obs = LifecycleEventObserver { _, e -> if (e == Lifecycle.Event.ON_RESUME) { report = vm.health(); shizuku = ShizukuBridge.status(ctx) } }
        owner.lifecycle.addObserver(obs)
        onDispose { owner.lifecycle.removeObserver(obs) }
    }

    fun refresh() { report = vm.health(); shizuku = ShizukuBridge.status(ctx) }
    fun run(cmds: List<ShellCommands.Command>) {
        busy = true
        vm.runShizuku(cmds) { log = it; busy = false; refresh() }
    }

    LazyColumn {
        item {
            val worst = report.worst
            Row(Modifier.padding(16.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                HealthDot(worst)
                Text(
                    when (worst) { Health.Level.OK -> "Everything's running"; Health.Level.ATTENTION -> "Mostly fine — a few things to tighten"; Health.Level.OFF -> "Something important is off" },
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        }

        item { SectionHeader("Shizuku boost") }
        item {
            Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    when (shizuku) {
                        ShizukuBridge.Status.READY -> "Connected. One tap below tells Android to stop throttling this app."
                        ShizukuBridge.Status.PERMISSION_NEEDED -> "Shizuku is running. Grant access to unlock one-tap hardening."
                        ShizukuBridge.Status.NOT_RUNNING -> "Shizuku is installed but not started. Open the Shizuku app and start it (wireless debugging or ADB)."
                        ShizukuBridge.Status.NOT_INSTALLED -> "Shizuku isn't installed. Everything works without it; the fixes below just need a Settings trip each."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
                when (shizuku) {
                    ShizukuBridge.Status.PERMISSION_NEEDED -> Button(onClick = { ShizukuBridge.requestPermission() }) { Text("Grant Shizuku access") }
                    ShizukuBridge.Status.READY -> {
                        Button(onClick = { run(ShellCommands.hardening) }, modifier = Modifier.fillMaxWidth(), enabled = !busy) { Text(if (busy) "Working…" else "Harden background (battery, standby, bg run)") }
                        OutlinedButton(onClick = { run(listOf(ShellCommands.Command.ALLOW_NOTIFICATION_LISTENER)) }, modifier = Modifier.fillMaxWidth(), enabled = !busy) { Text("Enable chat-app listener") }
                        OutlinedButton(
                            enabled = !busy,
                            onClick = { run(listOf(ShellCommands.Command.GRANT_CONTACTS, ShellCommands.Command.GRANT_CALL_LOG, ShellCommands.Command.GRANT_SMS, ShellCommands.Command.GRANT_CALENDAR, ShellCommands.Command.GRANT_NOTIFICATIONS)) },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text("Grant all permissions") }
                        Text("Only these fixed commands can run, and only on this app. Nothing else.", style = MaterialTheme.typography.bodySmall)
                    }
                    else -> TextButton(onClick = ::refresh) { Text("Re-check") }
                }
                log.forEach { Text(it, style = MaterialTheme.typography.bodySmall) }
            }
        }

        item { SectionHeader("Checks") }
        items(report.checks) { c ->
            ListItem(
                leadingContent = { HealthDot(c.level) },
                headlineContent = { Text(c.title) },
                supportingContent = { Text(c.detail + (c.fixHint?.takeIf { c.level != Health.Level.OK }?.let { "\n$it" } ?: "")) },
                trailingContent = {
                    if (c.level != Health.Level.OK) when (c.title) {
                        "Battery optimisation" -> TextButton(onClick = {
                            ctx.startActivity(Intent(SysSettings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:${ctx.packageName}")))
                        }) { Text("Fix") }
                        "Chat app listener" -> TextButton(onClick = { ctx.startActivity(Intent(SysSettings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }) { Text("Fix") }
                        "Notifications", "Call log access", "SMS access" -> TextButton(onClick = {
                            ctx.startActivity(Intent(SysSettings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${ctx.packageName}")))
                        }) { Text("Fix") }
                        else -> {}
                    }
                },
            )
        }
        item { TextButton(onClick = ::refresh, Modifier.padding(8.dp)) { Text("Re-measure") } }
    }
}

