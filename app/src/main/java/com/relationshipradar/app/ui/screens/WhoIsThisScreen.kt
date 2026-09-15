package com.relationshipradar.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.relationshipradar.app.data.db.IdentifierType
import com.relationshipradar.app.data.db.PendingIdentity
import com.relationshipradar.app.ui.EmptyState
import com.relationshipradar.app.ui.Format
import com.relationshipradar.app.ui.RadarViewModel

/**
 * Uncertain identity matches. The rule: ask once, remember the answer, never silently merge.
 * Suggestions (same display name) are offered first but still need a tap.
 */
@Composable
fun WhoIsThisScreen(vm: RadarViewModel) {
    val pending by vm.pendingIdentities.collectAsStateWithLifecycle()
    val radar by vm.radar.collectAsStateWithLifecycle()
    var linking by remember { mutableStateOf<PendingIdentity?>(null) }

    if (pending.isEmpty()) {
        EmptyState("Nothing to sort out", "When a call, text, or chat comes from someone the radar can't place, it'll ask here.")
        return
    }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(pending, key = { it.id }) { p ->
            val suggested = radar.firstOrNull { it.person.id == p.suggestedPersonId }
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(display(p), style = MaterialTheme.typography.titleMedium)
                    Text("${p.source} · seen ${p.seenCount}× · last ${Format.ago(p.lastSeenAt).lowercase()}", style = MaterialTheme.typography.bodySmall)
                    if (suggested != null) {
                        Button(onClick = { vm.resolvePending(p, suggested.person.id) }, Modifier.fillMaxWidth()) { Text("This is ${suggested.person.displayName}") }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { linking = p }) { Text("Pick a person") }
                        TextButton(onClick = { vm.resolvePendingAsNew(p, defaultName(p)) }) { Text("New person") }
                        TextButton(onClick = { vm.ignorePending(p) }) { Text("Ignore") }
                    }
                }
            }
        }
    }

    linking?.let { p ->
        var query by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { linking = null },
            title = { Text("Who is ${display(p)}?") },
            text = {
                Column {
                    OutlinedTextField(query, { query = it }, label = { Text("Search") }, singleLine = true)
                    val matches = radar.filter { query.isBlank() || it.person.displayName.contains(query, true) }.take(6)
                    LazyColumn(Modifier.height((matches.size * 48).coerceAtMost(288).dp)) {
                        items(matches, key = { it.person.id }) { r ->
                            ListItem(headlineContent = { Text(r.person.displayName) }, modifier = Modifier.fillMaxWidth().clickable { vm.resolvePending(p, r.person.id); linking = null })
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { linking = null }) { Text("Cancel") } },
        )
    }
}

private fun display(p: PendingIdentity) = when (p.type) {
    IdentifierType.HANDLE -> p.rawValue.substringAfter(':')
    else -> p.rawValue
}

private fun defaultName(p: PendingIdentity) = if (p.type == IdentifierType.HANDLE) display(p) else "Unknown (${p.rawValue})"
