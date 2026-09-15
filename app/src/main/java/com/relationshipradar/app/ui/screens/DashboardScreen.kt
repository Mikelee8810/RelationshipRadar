package com.relationshipradar.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.relationshipradar.app.engine.PersonRadar
import com.relationshipradar.app.engine.RadarStatus
import com.relationshipradar.app.engine.ReminderEngine.needsAttention
import com.relationshipradar.app.ui.Avatar
import com.relationshipradar.app.ui.Format
import com.relationshipradar.app.ui.RadarViewModel
import com.relationshipradar.app.ui.SectionHeader
import com.relationshipradar.app.ui.Sheet
import com.relationshipradar.app.ui.StatusChip
import com.relationshipradar.app.ui.theme.Radar
import com.relationshipradar.app.ui.theme.StatusColors

private enum class Filter(val label: String) { ATTENTION("Needs you"), REMINDERS("Reminders"), ALL("Everyone") }

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DashboardScreen(vm: RadarViewModel, onOpenPerson: (Long) -> Unit, onOpenNewPeople: () -> Unit, onOpenWho: () -> Unit) {
    val radar by vm.radar.collectAsStateWithLifecycle()
    val uncategorized by vm.uncategorized.collectAsStateWithLifecycle()
    val pending by vm.pendingIdentities.collectAsStateWithLifecycle()
    var filter by rememberSaveable { mutableStateOf(Filter.ATTENTION) }

    val attention = radar.filter { it.status.needsAttention }
    val shown = when (filter) {
        Filter.ATTENTION -> attention
        Filter.REMINDERS -> radar.filter { it.status != RadarStatus.TRACK_ONLY }
        Filter.ALL -> radar
    }

    LazyColumn(contentPadding = PaddingValues(bottom = Radar.fabClearance.dp)) {
        // ---- Hero: one big honest number ---------------------------------------------------
        item {
            Column(Modifier.padding(horizontal = Radar.sp4.dp)) {
                AnimatedContent(
                    attention.size, label = "count",
                    transitionSpec = { (slideInVertically { it / 2 } + fadeIn()) togetherWith (slideOutVertically { -it / 2 } + fadeOut()) },
                ) { n ->
                    Text(
                        if (radar.isEmpty()) "Hi" else if (n == 0) "All good" else "$n",
                        style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Text(
                    when {
                        radar.isEmpty() -> "Add someone, or import your contacts in Settings."
                        attention.isEmpty() -> "Nobody needs a nudge right now."
                        attention.size == 1 -> "person to reach out to"
                        else -> "people to reach out to"
                    },
                    style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(Radar.sp4.dp))
        }

        if (uncategorized.isNotEmpty() || pending.isNotEmpty()) {
            item {
                Column(Modifier.padding(horizontal = Radar.sp3.dp), verticalArrangement = Arrangement.spacedBy(Radar.sp2.dp)) {
                    if (uncategorized.isNotEmpty()) Nudge("${uncategorized.size} new ${if (uncategorized.size == 1) "person" else "people"}", "Say who they are to you", onOpenNewPeople)
                    if (pending.isNotEmpty()) Nudge("${pending.size} unmatched ${if (pending.size == 1) "contact" else "contacts"}", "A number or chat that couldn't be placed", onOpenWho)
                }
                Spacer(Modifier.height(Radar.sp3.dp))
            }
        }

        item {
            Row(Modifier.padding(horizontal = Radar.sp3.dp), horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)) {
                Filter.entries.forEachIndexed { i, f ->
                    ToggleButton(
                        checked = filter == f, onCheckedChange = { filter = f },
                        shapes = when (i) { 0 -> ButtonGroupDefaults.connectedLeadingButtonShapes(); Filter.entries.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes(); else -> ButtonGroupDefaults.connectedMiddleButtonShapes() },
                    ) { Text(f.label) }
                }
            }
            Spacer(Modifier.height(Radar.sp2.dp))
        }

        if (radar.isEmpty()) {
            item { Text("Nobody here yet.", Modifier.padding(Radar.sp4.dp), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else if (shown.isEmpty()) {
            item { Text(if (filter == Filter.ATTENTION) "You're caught up." else "Turn on reminders for someone to see them here.", Modifier.padding(Radar.sp4.dp), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else {
            val groups = shown.groupBy { it.status }
            for (status in listOf(RadarStatus.VERY_OVERDUE, RadarStatus.OVERDUE, RadarStatus.DUE_SOON, RadarStatus.GOOD, RadarStatus.SNOOZED, RadarStatus.PAUSED, RadarStatus.TRACK_ONLY)) {
                val list = groups[status] ?: continue
                item(key = "h$status") { SectionHeader(StatusColors.label(status), Modifier.padding(top = Radar.sp2.dp)) }
                items(list, key = { it.person.id }) { r -> PersonRow(r, Modifier.animateItem()) { onOpenPerson(r.person.id) } }
            }
        }
    }
}

@Composable
private fun Nudge(title: String, sub: String, onClick: () -> Unit) {
    Sheet(Modifier.clickable(onClick = onClick)) {
        ListItem(
            headlineContent = { Text(title, style = MaterialTheme.typography.titleMedium) },
            supportingContent = { Text(sub) },
            trailingContent = { Text("Sort", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary) },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        )
    }
}

@Composable
fun PersonRow(r: PersonRadar, modifier: Modifier = Modifier, onClick: () -> Unit) {
    ListItem(
        modifier = modifier.clickable(onClick = onClick).padding(horizontal = Radar.sp2.dp),
        leadingContent = { Avatar(r.person.id, r.person.displayName, r.status) },
        headlineContent = { Text(r.person.displayName, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        supportingContent = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusChip(r.status)
                r.category?.let { Text("· ${it.name}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        },
        trailingContent = { Text(Format.ago(r.lastEffortAt), style = MaterialTheme.typography.labelLarge) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}
