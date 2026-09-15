package com.relationshipradar.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.relationshipradar.app.engine.PersonRadar
import com.relationshipradar.app.engine.RadarStatus
import com.relationshipradar.app.engine.ReminderEngine.needsAttention
import com.relationshipradar.app.ui.EmptyState
import com.relationshipradar.app.ui.Format
import com.relationshipradar.app.ui.RadarViewModel
import com.relationshipradar.app.ui.SectionHeader
import com.relationshipradar.app.ui.StatusChip
import com.relationshipradar.app.ui.StatusDot

private enum class Filter(val label: String) { ATTENTION("Needs attention"), REMINDERS("With reminders"), ALL("Everyone") }

@Composable
fun DashboardScreen(vm: RadarViewModel, onOpenPerson: (Long) -> Unit, onOpenNewPeople: () -> Unit) {
    val radar by vm.radar.collectAsStateWithLifecycle()
    val uncategorized by vm.uncategorized.collectAsStateWithLifecycle()
    var filter by rememberSaveable { mutableStateOf(Filter.ATTENTION) }

    val shown = when (filter) {
        Filter.ATTENTION -> radar.filter { it.status.needsAttention }
        Filter.REMINDERS -> radar.filter { it.status != RadarStatus.TRACK_ONLY }
        Filter.ALL -> radar
    }

    Column {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Filter.entries.forEach { f ->
                FilterChip(selected = filter == f, onClick = { filter = f }, label = { Text(f.label) })
            }
        }

        if (uncategorized.isNotEmpty()) {
            Card(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).clickable(onClick = onOpenNewPeople),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("${uncategorized.size} new ${if (uncategorized.size == 1) "person" else "people"}", style = MaterialTheme.typography.titleMedium)
                        Text("Tell the radar who they are to you", style = MaterialTheme.typography.bodySmall)
                    }
                    TextButton(onClick = onOpenNewPeople) { Text("Sort") }
                }
            }
        }

        if (radar.isEmpty()) {
            EmptyState("Nobody on the radar yet", "Import your contacts from Settings, or tap + to add someone by hand.")
        } else if (shown.isEmpty()) {
            EmptyState(
                if (filter == Filter.ATTENTION) "All caught up" else "Nothing here",
                if (filter == Filter.ATTENTION) "Nobody needs a nudge right now." else "Turn on reminders for a person to see them here.",
            )
        } else {
            LazyColumn(contentPadding = PaddingValues(bottom = 96.dp)) {
                val groups = shown.groupBy { it.status }
                for (status in listOf(RadarStatus.VERY_OVERDUE, RadarStatus.OVERDUE, RadarStatus.DUE_SOON, RadarStatus.GOOD, RadarStatus.SNOOZED, RadarStatus.PAUSED, RadarStatus.TRACK_ONLY)) {
                    val list = groups[status] ?: continue
                    item(key = "h$status") { SectionHeader(com.relationshipradar.app.ui.theme.StatusColors.label(status) + " · ${list.size}") }
                    items(list, key = { it.person.id }) { PersonRow(it) { onOpenPerson(it.person.id) } }
                }
            }
        }
    }
}

@Composable
fun PersonRow(r: PersonRadar, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StatusDot(r.status, 14)
        Column(Modifier.weight(1f)) {
            Text(r.person.displayName, style = MaterialTheme.typography.bodyLarge)
            val sub = buildString {
                append(r.category?.name ?: "No category")
                r.intervalDays?.let { append(" · every $it d") }
            }
            Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(Format.ago(r.lastEffortAt), style = MaterialTheme.typography.labelLarge)
            if (r.status != RadarStatus.TRACK_ONLY) StatusChip(r.status)
        }
    }
}
