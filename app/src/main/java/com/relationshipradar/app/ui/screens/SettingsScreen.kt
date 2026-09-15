package com.relationshipradar.app.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings as SysSettings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
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
import com.relationshipradar.app.shizuku.ShizukuBridge
import com.relationshipradar.app.ui.Format
import com.relationshipradar.app.ui.RadarViewModel
import com.relationshipradar.app.ui.SectionHeader
import com.relationshipradar.app.work.Notifications

@Composable
fun SettingsScreen(vm: RadarViewModel, onOpenCategories: () -> Unit) {
    val ctx = LocalContext.current
    val s by vm.appSettings.collectAsStateWithLifecycle()
    val archived by vm.archived.collectAsStateWithLifecycle()
    var syncMsg by remember { mutableStateOf<String?>(null) }
    var contactsGranted by remember { mutableStateOf(vm.contacts.hasPermission()) }
    var notifGranted by remember { mutableStateOf(Notifications.canPost(ctx)) }
    var shizuku by remember { mutableStateOf(ShizukuBridge.status()) }

    val contactsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        contactsGranted = ok
        if (ok) vm.syncContacts { syncMsg = it }
    }
    val notifLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { notifGranted = it }

    fun openAppSettings() = ctx.startActivity(Intent(SysSettings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${ctx.packageName}")))

    LazyColumn {
        item { SectionHeader("Contacts") }
        item {
            Column(Modifier.padding(horizontal = 16.dp)) {
                Text("Every saved contact becomes a tracked person. Reminders stay off until you turn them on.", style = MaterialTheme.typography.bodySmall)
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

        item { SectionHeader("Notifications") }
        item {
            Column(Modifier.padding(horizontal = 16.dp)) {
                if (!notifGranted) {
                    Text("Reminders can't reach you until notifications are allowed.", style = MaterialTheme.typography.bodySmall)
                    Button(onClick = { notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }, Modifier.padding(vertical = 8.dp)) { Text("Allow notifications") }
                }
                ToggleLine("Daily roundup", "One quiet summary of everyone who is due", s.roundupEnabled, vm::setRoundupEnabled)
                ToggleLine("Individual alerts", "Separate alert for high-priority people", s.individualAlertsEnabled, vm::setIndividualAlerts)
                Text("Roundup time: ${s.roundupHour}:00", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
                Slider(value = s.roundupHour.toFloat(), onValueChange = { vm.setRoundupHour(it.toInt()) }, valueRange = 6f..22f, steps = 15)
                Text("Backup nudges: day 0, 2, 4, 6 after due — then the app goes quiet and just shows the colour.", style = MaterialTheme.typography.bodySmall)
            }
        }

        item { SectionHeader("Categories") }
        item { ListItem(headlineContent = { Text("Edit categories and default timers") }, modifier = Modifier.clickable(onClick = onOpenCategories)) }

        item { SectionHeader("Shizuku") }
        item {
            Column(Modifier.padding(horizontal = 16.dp)) {
                val label = when (shizuku) {
                    ShizukuBridge.Status.NOT_INSTALLED -> "Not installed — deeper Android access (call log, background reliability) arrives in Phase 3."
                    ShizukuBridge.Status.NOT_RUNNING -> "Installed but not running. Start it from the Shizuku app."
                    ShizukuBridge.Status.PERMISSION_NEEDED -> "Running. Tap to grant RelationshipRadar access."
                    ShizukuBridge.Status.READY -> "Connected ✓"
                }
                Text(label, style = MaterialTheme.typography.bodyMedium)
                if (shizuku == ShizukuBridge.Status.PERMISSION_NEEDED) {
                    Button(onClick = { ShizukuBridge.requestPermission(); shizuku = ShizukuBridge.status() }, Modifier.padding(top = 8.dp)) { Text("Grant Shizuku access") }
                }
                TextButton(onClick = { shizuku = ShizukuBridge.status() }) { Text("Re-check") }
            }
        }

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

@Composable
private fun ToggleLine(title: String, sub: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(sub) },
        trailingContent = { Switch(checked, onChange) },
        modifier = Modifier.fillMaxWidth(),
    )
}
