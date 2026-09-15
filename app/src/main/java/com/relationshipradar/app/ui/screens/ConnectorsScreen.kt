package com.relationshipradar.app.ui.screens

import android.Manifest
import android.content.Intent
import android.provider.Settings as SysSettings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.relationshipradar.app.connectors.RadarNotificationListener
import com.relationshipradar.app.ui.Format
import com.relationshipradar.app.ui.RadarViewModel
import com.relationshipradar.app.ui.SectionHeader
import com.relationshipradar.app.ui.Hint
import com.relationshipradar.app.ui.Sheet
import com.relationshipradar.app.ui.theme.Radar
import com.relationshipradar.app.work.ScanScheduler

/** Each source: what it reads (specific, per the privacy rule), permission state, on/off, last run. */
@Composable
fun ConnectorsScreen(vm: RadarViewModel) {
    val ctx = LocalContext.current
    val cursors by vm.cursors.collectAsStateWithLifecycle()
    var callsGranted by remember { mutableStateOf(vm.hasPermission(Manifest.permission.READ_CALL_LOG)) }
    var smsGranted by remember { mutableStateOf(vm.hasPermission(Manifest.permission.READ_SMS)) }
    var calGranted by remember { mutableStateOf(vm.hasPermission(Manifest.permission.READ_CALENDAR)) }
    var listenerOn by remember { mutableStateOf(RadarNotificationListener.isEnabled(ctx)) }
    var msg by remember { mutableStateOf<String?>(null) }

    val callsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok -> callsGranted = ok; if (ok) vm.scanNow { msg = it } }
    val calLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok -> calGranted = ok; if (ok) vm.scanNow { msg = it } }
    val smsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok -> smsGranted = ok; if (ok) vm.scanNow { msg = it } }

    fun cursor(id: String) = cursors.firstOrNull { it.connectorId == id }
    fun enabled(id: String) = cursor(id)?.enabled ?: true

    LazyColumn {
        item {
            Text(
                "It keeps who, which app, when, and whether you reached out. Never what was said.",
                Modifier.padding(horizontal = Radar.sp4.dp, vertical = Radar.sp2.dp),
                style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        item { SectionHeader("Phone calls") }
        item {
            SourceCard(
                what = "Reads your call log: number, time, length, direction. Outgoing calls and answered incoming calls count.",
                granted = callsGranted, enabled = enabled("calls"), last = cursor("calls"),
                onToggle = { vm.setConnectorEnabled("calls", it) },
                onGrant = { callsLauncher.launch(Manifest.permission.READ_CALL_LOG) },
            )
        }

        item { SectionHeader("Text messages") }
        item {
            SourceCard(
                what = "Reads SMS sender/recipient and time only — the message text is never requested. Sent texts count.",
                granted = smsGranted, enabled = enabled("sms"), last = cursor("sms"),
                onToggle = { vm.setConnectorEnabled("sms", it) },
                onGrant = { smsLauncher.launch(Manifest.permission.READ_SMS) },
            )
        }

        item { SectionHeader("Calendar") }
        item {
            SourceCard(
                what = "Past events you and a known contact both attended count as seeing them. Reads attendee emails and times only — never titles or notes.",
                granted = calGranted, enabled = enabled("calendar"), last = cursor("calendar"),
                onToggle = { vm.setConnectorEnabled("calendar", it) },
                onGrant = { calLauncher.launch(Manifest.permission.READ_CALENDAR) },
            )
        }

        item { SectionHeader("Chat apps (WhatsApp, Messenger, Instagram, Telegram, Signal, Discord, Slack, Teams)") }
        item {
            Column(Modifier.padding(horizontal = 16.dp)) {
                Text(
                    "Watches chat notifications for who sent a message and when. Your replies count. Message text is ignored.",
                    style = MaterialTheme.typography.bodySmall,
                )
                if (!listenerOn) {
                    Text("Android needs you to flip a switch for Relationship Radar in the next screen.", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
                    Button(onClick = { ctx.startActivity(Intent(SysSettings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }, Modifier.padding(top = 8.dp)) { Text("Open notification access") }
                    TextButton(onClick = { listenerOn = RadarNotificationListener.isEnabled(ctx) }) { Text("I turned it on — re-check") }
                } else {
                    Row(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                        Text("Connected ✓", Modifier.weight(1f))
                        Switch(enabled(RadarNotificationListener.CONNECTOR_ID), { vm.setConnectorEnabled(RadarNotificationListener.CONNECTOR_ID, it) })
                    }
                    cursor(RadarNotificationListener.CONNECTOR_ID)?.let { Text("Credited ${it.lastRunCount} messages so far", style = MaterialTheme.typography.bodySmall) }
                }
            }
        }

        item { SectionHeader("Scan") }
        item {
            Column(Modifier.padding(horizontal = 16.dp)) {
                Text("Calls, texts, and calendar are checked every 6 hours in the background. First run looks back one year.", style = MaterialTheme.typography.bodySmall)
                Button(onClick = { vm.scanNow { msg = it } }, Modifier.padding(top = 8.dp)) { Text("Scan now") }
                msg?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
            }
        }
    }
}


@Composable
private fun SourceCard(what: String, granted: Boolean, enabled: Boolean, last: com.relationshipradar.app.data.db.ConnectorCursor?, onToggle: (Boolean) -> Unit, onGrant: () -> Unit) {
    Sheet(Modifier.padding(horizontal = Radar.sp3.dp), padding = Radar.sp3) {
        Hint(what)
        if (!granted) {
            Button(onClick = onGrant, Modifier.padding(top = 8.dp)) { Text("Allow access") }
        } else {
            ListItem(
                headlineContent = { Text(if (enabled) "On" else "Off") },
                supportingContent = { Text(last?.takeIf { it.lastRunAt > 0 }?.let { "Last scan ${Format.dateTime(it.lastRunAt)} · ${it.lastRunCount} new" } ?: "Not scanned yet") },
                trailingContent = { Switch(enabled, onToggle) },
            )
        }
    }
}
