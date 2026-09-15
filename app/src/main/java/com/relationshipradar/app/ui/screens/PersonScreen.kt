package com.relationshipradar.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.relationshipradar.app.data.db.Interaction
import com.relationshipradar.app.data.db.ReminderBehavior
import com.relationshipradar.app.engine.PauseOption
import com.relationshipradar.app.engine.RadarStatus
import com.relationshipradar.app.engine.ReminderEngine
import com.relationshipradar.app.engine.SnoozeOption
import com.relationshipradar.app.ui.Format
import com.relationshipradar.app.ui.RadarViewModel
import com.relationshipradar.app.ui.SectionHeader
import com.relationshipradar.app.ui.StatusChip
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonScreen(vm: RadarViewModel, personId: Long, onLog: () -> Unit, onBack: () -> Unit) {
    val pwc by vm.person(personId).collectAsStateWithLifecycle(null)
    val interactions by vm.interactions(personId).collectAsStateWithLifecycle(emptyList())
    val identifiers by vm.identifiers(personId).collectAsStateWithLifecycle(emptyList())
    val categories by vm.categories.collectAsStateWithLifecycle()
    val p = pwc?.person ?: return
    val radar = ReminderEngine.evaluate(p, pwc?.category, interactions.firstOrNull { it.countsTowardTimer }?.timestamp)

    var dialog by remember { mutableStateOf<String?>(null) } // "snooze" | "pause" | "interval" | "archive" | "snoozeDate" | "pauseDate"
    var intervalText by remember(p.reminderIntervalDays) { mutableStateOf(p.reminderIntervalDays?.toString() ?: "") }
    var notes by remember(p.notes) { mutableStateOf(p.notes) }

    LazyColumn {
        item {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(p.displayName, style = MaterialTheme.typography.headlineMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    StatusChip(radar.status)
                    Text("Last effort: " + Format.ago(radar.lastEffortAt).lowercase(), style = MaterialTheme.typography.bodyMedium)
                }
                radar.dueAt?.let { if (radar.status != RadarStatus.TRACK_ONLY) Text("Due " + Format.date(it), style = MaterialTheme.typography.bodySmall) }
                p.snoozedUntil?.takeIf { it > System.currentTimeMillis() }?.let { Text("Snoozed " + Format.until(it), style = MaterialTheme.typography.bodySmall) }
                p.pausedUntil?.takeIf { it > System.currentTimeMillis() }?.let { Text("Paused " + Format.until(it), style = MaterialTheme.typography.bodySmall) }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(onClick = onLog, label = { Text("Log contact") })
                    if (radar.status != RadarStatus.TRACK_ONLY) {
                        AssistChip(onClick = { dialog = "snooze" }, label = { Text("Snooze") })
                        AssistChip(onClick = { dialog = "pause" }, label = { Text(if (radar.status == RadarStatus.PAUSED) "Unpause" else "Pause") })
                    }
                }
            }
        }

        item { SectionHeader("Category") }
        item {
            FlowChips(
                listOf("None") + categories.map { it.name },
                if (p.categoryId == null) 0 else categories.indexOfFirst { it.id == p.categoryId } + 1,
            ) { i -> vm.setCategory(personId, if (i == 0) null else categories[i - 1].id) }
        }

        item { SectionHeader("Reminders") }
        item {
            Column(Modifier.padding(horizontal = 16.dp)) {
                ToggleRow("Track this person", "Keeps a timeline even without reminders", p.trackingEnabled) { vm.updatePerson(p.copy(trackingEnabled = it)) }
                ToggleRow("Remind me", "Nudge when it's been too long", p.remindersEnabled) { vm.updatePerson(p.copy(remindersEnabled = it)) }
                if (p.remindersEnabled) {
                    val def = pwc?.category?.defaultIntervalDays
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            intervalText,
                            { intervalText = it.filter(Char::isDigit).take(4) },
                            label = { Text("Every N days") },
                            placeholder = { Text(def?.let { "$it (category default)" } ?: "no default") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(onClick = { vm.updatePerson(p.copy(reminderIntervalDays = intervalText.toIntOrNull())) }) { Text("Save") }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                        Text("Alert style:", modifier = Modifier.align(Alignment.CenterVertically))
                        FilterChip(p.reminderBehavior == null, { vm.updatePerson(p.copy(reminderBehavior = null)) }, { Text("Category default") })
                        FilterChip(p.reminderBehavior == ReminderBehavior.INDIVIDUAL, { vm.updatePerson(p.copy(reminderBehavior = ReminderBehavior.INDIVIDUAL)) }, { Text("Own alert") })
                        FilterChip(p.reminderBehavior == ReminderBehavior.ROUNDUP, { vm.updatePerson(p.copy(reminderBehavior = ReminderBehavior.ROUNDUP)) }, { Text("Roundup") })
                    }
                }
            }
        }

        item { SectionHeader("Notes") }
        item {
            Column(Modifier.padding(horizontal = 16.dp)) {
                OutlinedTextField(notes, { notes = it }, modifier = Modifier.fillMaxWidth(), minLines = 2, placeholder = { Text("Kids' names, what they're going through, gift ideas…") })
                if (notes != p.notes) TextButton(onClick = { vm.updatePerson(p.copy(notes = notes)) }) { Text("Save notes") }
            }
        }

        if (identifiers.isNotEmpty()) {
            item { SectionHeader("Known as") }
            items(identifiers, key = { it.id }) { Text("${it.type.name.lowercase()}: ${it.rawValue}", Modifier.padding(horizontal = 16.dp, vertical = 2.dp), style = MaterialTheme.typography.bodySmall) }
        }

        item { SectionHeader("Timeline · ${interactions.size}") }
        if (interactions.isEmpty()) {
            item { Text("Nothing logged yet. Tap “Log contact” to add the first one.", Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium) }
        }
        items(interactions, key = { it.id }) { i -> InteractionRow(i) { vm.deleteInteraction(i) } }

        item { HorizontalDivider(Modifier.padding(vertical = 16.dp)) }
        item {
            TextButton(onClick = { dialog = "archive" }, modifier = Modifier.padding(horizontal = 8.dp)) { Text("Archive ${p.displayName}") }
            Text("Archiving hides them but keeps every interaction and note.", Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(96.dp))
        }
    }

    when (dialog) {
        "snooze" -> OptionDialog("Snooze reminders", SnoozeOption.entries.map { it.label }, { dialog = null }) { i ->
            val opt = SnoozeOption.entries[i]
            if (opt == SnoozeOption.CUSTOM) dialog = "snoozeDate" else { vm.snooze(personId, opt.resolve()); dialog = null }
        }
        "pause" -> if (radar.status == RadarStatus.PAUSED) {
            vm.pause(personId, null); dialog = null
        } else OptionDialog("Pause reminders", PauseOption.entries.map { it.label }, { dialog = null }) { i ->
            val opt = PauseOption.entries[i]
            if (opt == PauseOption.UNTIL_DATE) dialog = "pauseDate" else { vm.pause(personId, opt.resolve()); dialog = null }
        }
        "snoozeDate", "pauseDate" -> {
            val state = rememberDatePickerState()
            DatePickerDialog(onDismissRequest = { dialog = null }, confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { utc ->
                        val day = java.time.Instant.ofEpochMilli(utc).atZone(ZoneId.of("UTC")).toLocalDate()
                        val local = day.atTime(9, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        if (dialog == "snoozeDate") vm.snooze(personId, local) else vm.pause(personId, local)
                    }
                    dialog = null
                }) { Text("OK") }
            }) { DatePicker(state) }
        }
        "archive" -> AlertDialog(
            onDismissRequest = { dialog = null },
            title = { Text("Archive ${p.displayName}?") },
            text = { Text("They disappear from the radar. History stays. You can restore them from Settings → Archived.") },
            confirmButton = { TextButton(onClick = { vm.archive(personId); dialog = null; onBack() }) { Text("Archive") } },
            dismissButton = { TextButton(onClick = { dialog = null }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun ToggleRow(title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked, onChange)
    }
}

@Composable
private fun InteractionRow(i: Interaction, onDelete: () -> Unit) {
    ListItem(
        headlineContent = { Text(i.type.name.lowercase().replace('_', ' ').replaceFirstChar(Char::uppercase) + if (i.note.isNotBlank()) " · ${i.note}" else "") },
        supportingContent = {
            Text((if (i.approximate) "~" else "") + Format.dateTime(i.timestamp) + " · " + i.source.name.lowercase() + if (!i.countsTowardTimer) " · doesn't count" else "")
        },
        trailingContent = { IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Delete") } },
    )
}

@Composable
fun OptionDialog(title: String, options: List<String>, onDismiss: () -> Unit, onPick: (Int) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                options.forEachIndexed { i, o ->
                    TextButton(onClick = { onPick(i) }, modifier = Modifier.fillMaxWidth()) { Text(o, Modifier.fillMaxWidth()) }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

