package com.relationshipradar.app.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings as SysSettings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
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
import com.relationshipradar.app.ui.Format
import com.relationshipradar.app.ui.RadarViewModel
import com.relationshipradar.app.ui.SectionHeader
import com.relationshipradar.app.ui.ToggleRow
import com.relationshipradar.app.ui.Sheet
import com.relationshipradar.app.ui.Hint
import com.relationshipradar.app.ui.LinkRow
import com.relationshipradar.app.ui.theme.Radar
import com.relationshipradar.app.work.Notifications

@Composable
fun SettingsScreen(vm: RadarViewModel, onOpenCategories: () -> Unit, onOpenConnectors: () -> Unit, onOpenWho: () -> Unit, onOpenHealth: () -> Unit) {
    val pendingCount by vm.pendingIdentities.collectAsStateWithLifecycle()
    val ctx = LocalContext.current
    val s by vm.appSettings.collectAsStateWithLifecycle()
    val archived by vm.archived.collectAsStateWithLifecycle()
    var syncMsg by remember { mutableStateOf<String?>(null) }
    var contactsGranted by remember { mutableStateOf(vm.contacts.hasPermission()) }
    var notifGranted by remember { mutableStateOf(Notifications.canPost(ctx)) }

    val contactsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        contactsGranted = ok
        if (ok) vm.syncContacts { syncMsg = it }
    }
    val notifLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { notifGranted = it }

    fun openAppSettings() = ctx.startActivity(Intent(SysSettings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${ctx.packageName}")))

    LazyColumn {
        item { SectionHeader("Contacts") }
        item {
            Sheet(Modifier.padding(horizontal = Radar.sp3.dp), padding = Radar.sp3) {
                Hint("Every saved contact joins the circle. Reminders stay off until you turn them on.")
                if (contactsGranted) {
                    Button(onClick = { vm.syncContacts { syncMsg = it } }, Modifier.padding(top = 8.dp)) { Text("Import / refresh contacts") }
                    if (s.lastContactsSyncAt > 0) Text("Last import: " + Format.dateTime(s.lastContactsSyncAt), style = MaterialTheme.typography.bodySmall)
                } else {
                    Button(onClick = { contactsLauncher.launch(Manifest.permission.READ_CONTACTS) }, Modifier.padding(top = 8.dp)) { Text("Allow contacts access") }
                    TextButton(onClick = ::openAppSettings) { Text("Denied before? Open app settings") }
                }
                syncMsg?.let { Text(it, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp)) }
            }
        }

        item { SectionHeader("Sources") }
        item {
            Sheet(Modifier.padding(horizontal = Radar.sp3.dp)) {
                LinkRow("Calls, texts, calendar, chat apps", "What the radar watches and why", onOpenConnectors)
                LinkRow("Who is this?", if (pendingCount.isEmpty()) "Nothing to sort out" else "${pendingCount.size} unmatched numbers or chats", onOpenWho)
            }
        }

        item { SectionHeader("Notifications") }
        item {
            Sheet(Modifier.padding(horizontal = Radar.sp3.dp), padding = Radar.sp2) {
                if (!notifGranted) {
                    Hint("Reminders can't reach you until notifications are allowed.")
                    Button(onClick = { notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }, Modifier.padding(vertical = 8.dp)) { Text("Allow notifications") }
                }
                ToggleRow("Daily roundup", "One quiet summary of everyone who is due", s.roundupEnabled, vm::setRoundupEnabled)
                ToggleRow("Individual alerts", "Separate alert for high-priority people", s.individualAlertsEnabled, vm::setIndividualAlerts)
                Text("Roundup time: ${s.roundupHour}:00", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
                Slider(value = s.roundupHour.toFloat(), onValueChange = { vm.setRoundupHour(it.toInt()) }, valueRange = 6f..22f, steps = 15)
                Hint("Backup nudges: day 0, 2, 4, 6 after due — then it goes quiet and just shows the colour.")
            }
        }

        item { SectionHeader("Categories") }
        item { Sheet(Modifier.padding(horizontal = Radar.sp3.dp)) { LinkRow("Categories and default timers", "Best friend every 7 days, friend every 30…", onOpenCategories) } }

        item { SectionHeader("Background health & Shizuku") }
        item { Sheet(Modifier.padding(horizontal = Radar.sp3.dp)) { LinkRow("Is the radar actually running?", "Battery, standby, permissions, Shizuku one-tap hardening", onOpenHealth) } }

        item { SectionHeader("Archived · ${archived.size}") }
        if (archived.isEmpty()) item { Text("Nobody archived.", Modifier.padding(16.dp), style = MaterialTheme.typography.bodySmall) }
        items(archived, key = { it.person.id }) { pwc ->
            ListItem(
                headlineContent = { Text(pwc.person.displayName) },
                trailingContent = { TextButton(onClick = { vm.restore(pwc.person.id) }) { Text("Restore") } },
            )
        }
    }
}
